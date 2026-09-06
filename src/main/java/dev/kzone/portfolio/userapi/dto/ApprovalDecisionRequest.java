package dev.kzone.portfolio.userapi.dto;

import dev.kzone.portfolio.userapi.domain.ApprovalAction;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ApprovalDecisionRequest(
        @NotNull ApprovalAction action,
        @Size(max = 500) String comment
) {
}
