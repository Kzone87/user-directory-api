package dev.kzone.portfolio.userapi.controller;

import dev.kzone.portfolio.userapi.service.WorkOrderReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
public class WorkOrderReportController {
    private static final MediaType XLSX = MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );

    private final WorkOrderReportService workOrderReportService;

    public WorkOrderReportController(WorkOrderReportService workOrderReportService) {
        this.workOrderReportService = workOrderReportService;
    }

    @GetMapping(value = "/work-orders.csv", produces = "text/csv;charset=UTF-8")
    public ResponseEntity<byte[]> csv(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        validateRange(from, to);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=work-orders-report.csv")
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .body(workOrderReportService.csv(from, to));
    }

    @GetMapping(value = "/work-orders.xlsx")
    public ResponseEntity<byte[]> xlsx(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        validateRange(from, to);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=work-orders-report.xlsx")
                .contentType(XLSX)
                .body(workOrderReportService.xlsx(from, to));
    }

    private void validateRange(LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from must be on or before to");
        }
    }
}
