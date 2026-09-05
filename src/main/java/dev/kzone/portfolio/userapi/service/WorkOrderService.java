package dev.kzone.portfolio.userapi.service;

import dev.kzone.portfolio.userapi.domain.WorkOrder;
import dev.kzone.portfolio.userapi.domain.WorkOrderStatus;
import dev.kzone.portfolio.userapi.dto.WorkOrderCreateRequest;
import dev.kzone.portfolio.userapi.dto.WorkOrderResponse;
import dev.kzone.portfolio.userapi.exception.WorkOrderNotFoundException;
import dev.kzone.portfolio.userapi.exception.WorkOrderTransitionException;
import dev.kzone.portfolio.userapi.mapper.WorkOrderMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class WorkOrderService {
    private final WorkOrderMapper workOrderMapper;

    public WorkOrderService(WorkOrderMapper workOrderMapper) {
        this.workOrderMapper = workOrderMapper;
    }

    public List<WorkOrderResponse> list(WorkOrderStatus status) {
        return workOrderMapper.findAll(status).stream().map(WorkOrderResponse::from).toList();
    }

    public WorkOrderResponse get(long id) {
        return WorkOrderResponse.from(requireWorkOrder(id));
    }

    @Transactional
    public WorkOrderResponse create(WorkOrderCreateRequest request) {
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
        return get(workOrder.getId());
    }

    @Transactional
    public WorkOrderResponse transition(long id, WorkOrderStatus nextStatus) {
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
        return get(id);
    }

    private WorkOrder requireWorkOrder(long id) {
        return workOrderMapper.findById(id).orElseThrow(() -> new WorkOrderNotFoundException(id));
    }
}
