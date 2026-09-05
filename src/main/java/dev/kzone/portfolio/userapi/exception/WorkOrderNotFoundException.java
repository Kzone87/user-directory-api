package dev.kzone.portfolio.userapi.exception;

public class WorkOrderNotFoundException extends RuntimeException {
    public WorkOrderNotFoundException(long id) {
        super("Work order not found: " + id);
    }
}
