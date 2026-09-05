package dev.kzone.portfolio.userapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WorkOrderCreateRequest(
        @NotBlank @Size(max = 160) String title,
        @NotBlank @Size(max = 120) String customerName,
        @NotBlank @Size(max = 100) String assignee
) {
}
