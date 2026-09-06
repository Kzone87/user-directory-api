package dev.kzone.portfolio.userapi.domain;

import java.util.EnumSet;
import java.util.Set;

public enum WorkOrderStatus {
    RECEIVED,
    IN_PROGRESS,
    WAITING_APPROVAL,
    APPROVED,
    DONE,
    CANCELLED;

    public boolean canTransitionTo(WorkOrderStatus next) {
        if (next == null || next == this) {
            return false;
        }
        Set<WorkOrderStatus> allowed = switch (this) {
            case RECEIVED -> EnumSet.of(IN_PROGRESS, CANCELLED);
            case IN_PROGRESS -> EnumSet.of(CANCELLED);
            case WAITING_APPROVAL -> EnumSet.noneOf(WorkOrderStatus.class);
            case APPROVED -> EnumSet.of(DONE, CANCELLED);
            case DONE, CANCELLED -> EnumSet.noneOf(WorkOrderStatus.class);
        };
        return allowed.contains(next);
    }
}
