package dev.kzone.portfolio.userapi.dto;

import dev.kzone.portfolio.userapi.domain.ApprovalDecision;
import dev.kzone.portfolio.userapi.domain.WorkOrderApproval;

import java.time.LocalDateTime;

public record WorkOrderApprovalResponse(
        Long id,
        Long workOrderId,
        String requestedBy,
        LocalDateTime requestedAt,
        String requestComment,
        ApprovalDecision decision,
        String decidedBy,
        LocalDateTime decidedAt,
        String decisionComment
) {
    public static WorkOrderApprovalResponse from(WorkOrderApproval approval) {
        return new WorkOrderApprovalResponse(
                approval.getId(),
                approval.getWorkOrderId(),
                approval.getRequestedBy(),
                approval.getRequestedAt(),
                approval.getRequestComment(),
                approval.getDecision(),
                approval.getDecidedBy(),
                approval.getDecidedAt(),
                approval.getDecisionComment()
        );
    }
}
