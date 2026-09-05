package dev.kzone.portfolio.userapi.domain;

import java.time.LocalDateTime;

public class WorkOrder {
    private Long id;
    private String title;
    private Long customerId;
    private String customerName;
    private String assignee;
    private WorkOrderStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public WorkOrder() {
    }

    public WorkOrder(
            Long id,
            String title,
            Long customerId,
            String customerName,
            String assignee,
            WorkOrderStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.title = title;
        this.customerId = customerId;
        this.customerName = customerName;
        this.assignee = assignee;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getAssignee() { return assignee; }
    public void setAssignee(String assignee) { this.assignee = assignee; }
    public WorkOrderStatus getStatus() { return status; }
    public void setStatus(WorkOrderStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
