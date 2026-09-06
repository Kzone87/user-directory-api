package dev.kzone.portfolio.userapi.controller;

import dev.kzone.portfolio.userapi.dto.OperationsAnalyticsResponse;
import dev.kzone.portfolio.userapi.service.AnalyticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {
    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/operations")
    public OperationsAnalyticsResponse operations() {
        return analyticsService.operations();
    }
}
