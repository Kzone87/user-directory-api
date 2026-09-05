package dev.kzone.portfolio.userapi.service;

import dev.kzone.portfolio.userapi.dto.OperationsAnalyticsResponse;
import dev.kzone.portfolio.userapi.mapper.AnalyticsMapper;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsService {
    private final AnalyticsMapper analyticsMapper;

    public AnalyticsService(AnalyticsMapper analyticsMapper) {
        this.analyticsMapper = analyticsMapper;
    }

    public OperationsAnalyticsResponse operations() {
        return new OperationsAnalyticsResponse(
                analyticsMapper.countCustomers(),
                analyticsMapper.countActiveCustomers(),
                analyticsMapper.countOpenWorkOrders(),
                analyticsMapper.countOverdueWorkOrders(),
                analyticsMapper.countDoneThisMonth(),
                analyticsMapper.statusDistribution(),
                analyticsMapper.priorityDistribution(),
                analyticsMapper.workloadByAssignee(),
                analyticsMapper.completedTrend()
        );
    }
}
