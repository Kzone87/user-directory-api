package dev.kzone.portfolio.userapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record WorkOrderCreateRequest(
        @NotBlank @Size(max = 160) String title,
        @NotNull @Positive Long customerId,
        @NotBlank @Size(max = 100) String assignee
) {
}
