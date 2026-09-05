package dev.kzone.portfolio.userapi.service;

import dev.kzone.portfolio.userapi.domain.WorkOrder;
import dev.kzone.portfolio.userapi.domain.WorkOrderStatus;
import dev.kzone.portfolio.userapi.dto.WorkOrderActivityResponse;
import dev.kzone.portfolio.userapi.dto.WorkOrderCreateRequest;
import dev.kzone.portfolio.userapi.dto.WorkOrderResponse;
import dev.kzone.portfolio.userapi.exception.WorkOrderNotFoundException;
import dev.kzone.portfolio.userapi.exception.WorkOrderTransitionException;
import dev.kzone.portfolio.userapi.mapper.WorkOrderActivityMapper;
import dev.kzone.portfolio.userapi.mapper.WorkOrderMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class WorkOrderService {
    private final WorkOrderMapper workOrderMapper;
    private final WorkOrderActivityMapper workOrderActivityMapper;

    public WorkOrderService(
            WorkOrderMapper workOrderMapper,
            WorkOrderActivityMapper workOrderActivityMapper
    ) {
        this.workOrderMapper = workOrderMapper;
        this.workOrderActivityMapper = workOrderActivityMapper;
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

    @Transactional
    public WorkOrderResponse create(WorkOrderCreateRequest request, String actor) {
        WorkOrder workOrder = new WorkOrder(
                null,
                request.title().trim(),
                request.customerName().trim(),
                request.assignee().trim(),
                WorkOrderStatus.RECEIVED,
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

    private WorkOrder requireWorkOrder(long id) {
        return workOrderMapper.findById(id).orElseThrow(() -> new WorkOrderNotFoundException(id));
    }
}
