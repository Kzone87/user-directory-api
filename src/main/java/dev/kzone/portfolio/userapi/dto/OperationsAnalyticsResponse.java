package dev.kzone.portfolio.userapi.dto;

import java.util.List;

public record OperationsAnalyticsResponse(
        long totalCustomers,
        long activeCustomers,
        long openWorkOrders,
        long overdueWorkOrders,
        long doneThisMonth,
        List<AnalyticsBucket> statusDistribution,
        List<AnalyticsBucket> priorityDistribution,
        List<AnalyticsBucket> workloadByAssignee,
        List<AnalyticsBucket> completedTrend
) {
}
