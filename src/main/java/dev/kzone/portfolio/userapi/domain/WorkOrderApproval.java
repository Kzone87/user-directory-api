package dev.kzone.portfolio.userapi.domain;

import java.time.LocalDateTime;

public class WorkOrderApproval {
    private Long id;
    private Long workOrderId;
    private String requestedBy;
    private LocalDateTime requestedAt;
    private String requestComment;
    private ApprovalDecision decision;
    private String decidedBy;
    private LocalDateTime decidedAt;
    private String decisionComment;

    public WorkOrderApproval() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getWorkOrderId() { return workOrderId; }
    public void setWorkOrderId(Long workOrderId) { this.workOrderId = workOrderId; }
    public String getRequestedBy() { return requestedBy; }
    public void setRequestedBy(String requestedBy) { this.requestedBy = requestedBy; }
    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }
    public String getRequestComment() { return requestComment; }
    public void setRequestComment(String requestComment) { this.requestComment = requestComment; }
    public ApprovalDecision getDecision() { return decision; }
    public void setDecision(ApprovalDecision decision) { this.decision = decision; }
    public String getDecidedBy() { return decidedBy; }
    public void setDecidedBy(String decidedBy) { this.decidedBy = decidedBy; }
    public LocalDateTime getDecidedAt() { return decidedAt; }
    public void setDecidedAt(LocalDateTime decidedAt) { this.decidedAt = decidedAt; }
    public String getDecisionComment() { return decisionComment; }
    public void setDecisionComment(String decisionComment) { this.decisionComment = decisionComment; }
}
