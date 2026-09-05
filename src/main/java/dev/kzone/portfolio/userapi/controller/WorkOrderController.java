package dev.kzone.portfolio.userapi.controller;

import dev.kzone.portfolio.userapi.domain.WorkOrderStatus;
import dev.kzone.portfolio.userapi.dto.WorkOrderActivityResponse;
import dev.kzone.portfolio.userapi.dto.WorkOrderCreateRequest;
import dev.kzone.portfolio.userapi.dto.WorkOrderResponse;
import dev.kzone.portfolio.userapi.dto.WorkOrderStatusUpdateRequest;
import dev.kzone.portfolio.userapi.service.WorkOrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/work-orders")
public class WorkOrderController {
    private final WorkOrderService workOrderService;

    public WorkOrderController(WorkOrderService workOrderService) {
        this.workOrderService = workOrderService;
    }

    @GetMapping
    public List<WorkOrderResponse> list(@RequestParam(required = false) WorkOrderStatus status) {
        return workOrderService.list(status);
    }

    @GetMapping("/{id}")
    public WorkOrderResponse get(@PathVariable long id) {
        return workOrderService.get(id);
    }

    @GetMapping("/{id}/activities")
    public List<WorkOrderActivityResponse> activities(@PathVariable long id) {
        return workOrderService.activities(id);
    }

    @PostMapping
    public ResponseEntity<WorkOrderResponse> create(
            @Valid @RequestBody WorkOrderCreateRequest request,
            Authentication authentication
    ) {
        WorkOrderResponse created = workOrderService.create(request, authentication.getName());
        return ResponseEntity.created(URI.create("/api/work-orders/" + created.id())).body(created);
    }

    @PatchMapping("/{id}/status")
    public WorkOrderResponse transition(
            @PathVariable long id,
            @Valid @RequestBody WorkOrderStatusUpdateRequest request,
            Authentication authentication
    ) {
        return workOrderService.transition(id, request.status(), authentication.getName());
    }
}
