package dev.kzone.portfolio.userapi.dto;

import dev.kzone.portfolio.userapi.domain.WorkOrder;
import dev.kzone.portfolio.userapi.domain.WorkOrderStatus;

import java.time.LocalDateTime;

public record WorkOrderResponse(
        Long id,
        String title,
        Long customerId,
        String customerName,
        String assignee,
        WorkOrderStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static WorkOrderResponse from(WorkOrder workOrder) {
        return new WorkOrderResponse(
                workOrder.getId(),
                workOrder.getTitle(),
                workOrder.getCustomerId(),
                workOrder.getCustomerName(),
                workOrder.getAssignee(),
                workOrder.getStatus(),
                workOrder.getCreatedAt(),
                workOrder.getUpdatedAt()
        );
    }
}
