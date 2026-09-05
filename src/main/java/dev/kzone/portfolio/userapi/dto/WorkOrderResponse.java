package dev.kzone.portfolio.userapi.dto;

import dev.kzone.portfolio.userapi.domain.WorkOrder;
import dev.kzone.portfolio.userapi.domain.WorkOrderPriority;
import dev.kzone.portfolio.userapi.domain.WorkOrderStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record WorkOrderResponse(
        Long id,
        String title,
        Long customerId,
        String customerName,
        String assignee,
        WorkOrderStatus status,
        WorkOrderPriority priority,
        LocalDate dueDate,
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
                workOrder.getPriority(),
                workOrder.getDueDate(),
                workOrder.getCreatedAt(),
                workOrder.getUpdatedAt()
        );
    }
}
