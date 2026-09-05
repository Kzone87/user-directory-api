package dev.kzone.portfolio.userapi.dto;

import dev.kzone.portfolio.userapi.domain.WorkOrderActivity;
import dev.kzone.portfolio.userapi.domain.WorkOrderStatus;

import java.time.LocalDateTime;

public record WorkOrderActivityResponse(
        long id,
        long workOrderId,
        String actor,
        String action,
        WorkOrderStatus fromStatus,
        WorkOrderStatus toStatus,
        String detail,
        LocalDateTime createdAt
) {
    public static WorkOrderActivityResponse from(WorkOrderActivity activity) {
        return new WorkOrderActivityResponse(
                activity.getId(),
                activity.getWorkOrderId(),
                activity.getActor(),
                activity.getAction(),
                activity.getFromStatus(),
                activity.getToStatus(),
                activity.getDetail(),
                activity.getCreatedAt()
        );
    }
}
