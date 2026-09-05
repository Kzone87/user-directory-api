package dev.kzone.portfolio.userapi.domain;

import java.time.LocalDateTime;

public class WorkOrderActivity {
    private Long id;
    private Long workOrderId;
    private String actor;
    private String action;
    private WorkOrderStatus fromStatus;
    private WorkOrderStatus toStatus;
    private String detail;
    private LocalDateTime createdAt;

    public WorkOrderActivity() {
    }

    public WorkOrderActivity(
            Long id,
            Long workOrderId,
            String actor,
            String action,
            WorkOrderStatus fromStatus,
            WorkOrderStatus toStatus,
            String detail,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.workOrderId = workOrderId;
        this.actor = actor;
        this.action = action;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.detail = detail;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getWorkOrderId() { return workOrderId; }
    public void setWorkOrderId(Long workOrderId) { this.workOrderId = workOrderId; }
    public String getActor() { return actor; }
    public void setActor(String actor) { this.actor = actor; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public WorkOrderStatus getFromStatus() { return fromStatus; }
    public void setFromStatus(WorkOrderStatus fromStatus) { this.fromStatus = fromStatus; }
    public WorkOrderStatus getToStatus() { return toStatus; }
    public void setToStatus(WorkOrderStatus toStatus) { this.toStatus = toStatus; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
