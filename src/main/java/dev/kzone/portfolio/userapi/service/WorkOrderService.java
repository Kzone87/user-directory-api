package dev.kzone.portfolio.userapi.service;

import dev.kzone.portfolio.userapi.domain.ApprovalAction;
import dev.kzone.portfolio.userapi.domain.ApprovalDecision;
import dev.kzone.portfolio.userapi.domain.Customer;
import dev.kzone.portfolio.userapi.domain.WorkOrder;
import dev.kzone.portfolio.userapi.domain.WorkOrderApproval;
import dev.kzone.portfolio.userapi.domain.WorkOrderStatus;
import dev.kzone.portfolio.userapi.dto.ApprovalDecisionRequest;
import dev.kzone.portfolio.userapi.dto.ApprovalRequest;
import dev.kzone.portfolio.userapi.dto.WorkOrderActivityResponse;
import dev.kzone.portfolio.userapi.dto.WorkOrderApprovalResponse;
import dev.kzone.portfolio.userapi.dto.WorkOrderCreateRequest;
import dev.kzone.portfolio.userapi.dto.WorkOrderResponse;
import dev.kzone.portfolio.userapi.exception.WorkOrderNotFoundException;
import dev.kzone.portfolio.userapi.exception.WorkOrderTransitionException;
import dev.kzone.portfolio.userapi.mapper.WorkOrderActivityMapper;
import dev.kzone.portfolio.userapi.mapper.WorkOrderApprovalMapper;
import dev.kzone.portfolio.userapi.mapper.WorkOrderMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class WorkOrderService {
    private final WorkOrderMapper workOrderMapper;
    private final WorkOrderActivityMapper workOrderActivityMapper;
    private final WorkOrderApprovalMapper workOrderApprovalMapper;
    private final CustomerService customerService;

    public WorkOrderService(
            WorkOrderMapper workOrderMapper,
            WorkOrderActivityMapper workOrderActivityMapper,
            WorkOrderApprovalMapper workOrderApprovalMapper,
            CustomerService customerService
    ) {
        this.workOrderMapper = workOrderMapper;
        this.workOrderActivityMapper = workOrderActivityMapper;
        this.workOrderApprovalMapper = workOrderApprovalMapper;
        this.customerService = customerService;
    }

    public List<WorkOrderResponse> list(WorkOrderStatus status) {
        return workOrderMapper.findAll(status).stream().map(WorkOrderResponse::from).toList();
    }

    public WorkOrderResponse get(long id) {
        return WorkOrderResponse.from(requireWorkOrder(id));
    }

    public List<WorkOrderActivityResponse> activities(long id) {
        requireWorkOrder(id);
        return workOrderActivityMapper.findByWorkOrderId(id).stream()
                .map(WorkOrderActivityResponse::from)
                .toList();
    }

    public List<WorkOrderApprovalResponse> approvals(long id) {
        requireWorkOrder(id);
        return workOrderApprovalMapper.findByWorkOrderId(id).stream()
                .map(WorkOrderApprovalResponse::from)
                .toList();
    }

    @Transactional
    public WorkOrderResponse create(WorkOrderCreateRequest request, String actor) {
        Customer customer = customerService.requireCustomer(request.customerId());
        WorkOrder workOrder = new WorkOrder(
                null,
                request.title().trim(),
                customer.getId(),
                customer.getCompanyName(),
                request.assignee().trim(),
                WorkOrderStatus.RECEIVED,
                request.priority(),
                request.dueDate(),
                null,
                null
        );
        workOrderMapper.insert(workOrder);
        workOrderActivityMapper.insert(
                workOrder.getId(),
                actor,
                "CREATED",
                null,
                WorkOrderStatus.RECEIVED,
                "업무가 접수되었습니다."
        );
        return get(workOrder.getId());
    }

    @Transactional
    public WorkOrderResponse transition(long id, WorkOrderStatus nextStatus, String actor) {
        WorkOrder current = requireWorkOrder(id);
        if (!current.getStatus().canTransitionTo(nextStatus)) {
            throw new WorkOrderTransitionException(
                    "Invalid work order transition: " + current.getStatus() + " -> " + nextStatus
            );
        }

        int changed = workOrderMapper.updateStatus(id, current.getStatus(), nextStatus);
        if (changed == 0) {
            throw new WorkOrderTransitionException("Work order changed concurrently. Refresh and retry.");
        }

        workOrderActivityMapper.insert(
                id,
                actor,
                "STATUS_CHANGED",
                current.getStatus(),
                nextStatus,
                "업무 상태가 " + current.getStatus() + "에서 " + nextStatus + "로 변경되었습니다."
        );
        return get(id);
    }

    @Transactional
    public WorkOrderApprovalResponse requestApproval(long id, ApprovalRequest request, String actor) {
        WorkOrder current = requireWorkOrder(id);
        if (current.getStatus() != WorkOrderStatus.IN_PROGRESS) {
            throw new WorkOrderTransitionException("Only IN_PROGRESS work can request approval.");
        }

        WorkOrderApproval approval = new WorkOrderApproval();
        approval.setWorkOrderId(id);
        approval.setRequestedBy(actor);
        approval.setRequestComment(cleanComment(request.comment()));
        approval.setDecision(ApprovalDecision.PENDING);
        workOrderApprovalMapper.insert(approval);

        int changed = workOrderMapper.updateStatus(id, WorkOrderStatus.IN_PROGRESS, WorkOrderStatus.WAITING_APPROVAL);
        if (changed == 0) {
            throw new WorkOrderTransitionException("Work order changed concurrently. Refresh and retry.");
        }

        workOrderActivityMapper.insert(
                id,
                actor,
                "APPROVAL_REQUESTED",
                WorkOrderStatus.IN_PROGRESS,
                WorkOrderStatus.WAITING_APPROVAL,
                "업무 승인 요청이 생성되었습니다."
        );

        return workOrderApprovalMapper.findByWorkOrderId(id).stream()
                .findFirst()
                .map(WorkOrderApprovalResponse::from)
                .orElseThrow(() -> new IllegalStateException("Approval was not persisted."));
    }

    @Transactional
    public WorkOrderResponse decideApproval(long id, ApprovalDecisionRequest request, String actor) {
        WorkOrder current = requireWorkOrder(id);
        if (current.getStatus() != WorkOrderStatus.WAITING_APPROVAL) {
            throw new WorkOrderTransitionException("Work order is not waiting for approval.");
        }

        WorkOrderApproval pending = workOrderApprovalMapper.findLatestPending(id)
                .orElseThrow(() -> new WorkOrderTransitionException("Pending approval was not found."));
        ApprovalDecision decision = request.action() == ApprovalAction.APPROVE
                ? ApprovalDecision.APPROVED
                : ApprovalDecision.REJECTED;
        WorkOrderStatus nextStatus = request.action() == ApprovalAction.APPROVE
                ? WorkOrderStatus.APPROVED
                : WorkOrderStatus.IN_PROGRESS;

        int approvalChanged = workOrderApprovalMapper.decide(
                pending.getId(),
                decision,
                actor,
                cleanComment(request.comment())
        );
        if (approvalChanged == 0) {
            throw new WorkOrderTransitionException("Approval changed concurrently. Refresh and retry.");
        }

        int statusChanged = workOrderMapper.updateStatus(id, WorkOrderStatus.WAITING_APPROVAL, nextStatus);
        if (statusChanged == 0) {
            throw new WorkOrderTransitionException("Work order changed concurrently. Refresh and retry.");
        }

        workOrderActivityMapper.insert(
                id,
                actor,
                request.action() == ApprovalAction.APPROVE ? "APPROVED" : "REJECTED",
                WorkOrderStatus.WAITING_APPROVAL,
                nextStatus,
                request.action() == ApprovalAction.APPROVE
                        ? "업무 승인 요청이 승인되었습니다."
                        : "업무 승인 요청이 반려되었습니다."
        );
        return get(id);
    }

    private WorkOrder requireWorkOrder(long id) {
        return workOrderMapper.findById(id).orElseThrow(() -> new WorkOrderNotFoundException(id));
    }

    private String cleanComment(String value) {
        if (value == null) return null;
        String cleaned = value.trim();
        return cleaned.isEmpty() ? null : cleaned;
    }
}
