package dev.kzone.portfolio.userapi.dto;

import dev.kzone.portfolio.userapi.domain.WorkOrderPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record WorkOrderCreateRequest(
        @NotBlank @Size(max = 160) String title,
        @NotNull @Positive Long customerId,
        @NotBlank @Size(max = 100) String assignee,
        @NotNull WorkOrderPriority priority,
        LocalDate dueDate
) {
}
