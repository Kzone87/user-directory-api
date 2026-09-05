package dev.kzone.portfolio.userapi.dto;

import dev.kzone.portfolio.userapi.domain.WorkOrderStatus;
import jakarta.validation.constraints.NotNull;

public record WorkOrderStatusUpdateRequest(@NotNull WorkOrderStatus status) {
}
