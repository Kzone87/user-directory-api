package dev.kzone.portfolio.userapi.dto;

import jakarta.validation.constraints.Size;

public record ApprovalRequest(
        @Size(max = 500) String comment
) {
}
