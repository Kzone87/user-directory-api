package dev.kzone.portfolio.userapi.service;

import dev.kzone.portfolio.userapi.domain.WorkOrder;
import dev.kzone.portfolio.userapi.mapper.WorkOrderMapper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class WorkOrderReportService {
    private static final String[] HEADERS = {
            "ID", "Title", "Customer", "Assignee", "Status", "Priority", "Due Date", "Created At", "Updated At"
    };

    private final WorkOrderMapper workOrderMapper;

    public WorkOrderReportService(WorkOrderMapper workOrderMapper) {
        this.workOrderMapper = workOrderMapper;
    }

    public byte[] csv(LocalDate from, LocalDate to) {
        List<WorkOrder> orders = reportRows(from, to);
        StringBuilder csv = new StringBuilder("\uFEFF");
        csv.append(String.join(",", HEADERS)).append('\n');
        for (WorkOrder order : orders) {
            String[] cells = rowValues(order);
            for (int index = 0; index < cells.length; index++) {
                if (index > 0) csv.append(',');
                csv.append(csvCell(cells[index]));
            }
            csv.append('\n');
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] xlsx(LocalDate from, LocalDate to) {
        List<WorkOrder> orders = reportRows(from, to);
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("work-orders");
            Row header = sheet.createRow(0);
            for (int index = 0; index < HEADERS.length; index++) {
                header.createCell(index).setCellValue(HEADERS[index]);
            }
            int rowIndex = 1;
            for (WorkOrder order : orders) {
                Row row = sheet.createRow(rowIndex++);
                String[] values = rowValues(order);
                for (int index = 0; index < values.length; index++) {
                    row.createCell(index).setCellValue(values[index]);
                }
            }
            for (int index = 0; index < HEADERS.length; index++) sheet.autoSizeColumn(index);
            workbook.write(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to create XLSX report.", exception);
        }
    }

    private List<WorkOrder> reportRows(LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("from must be on or before to");
        }
        return workOrderMapper.findForReport(from, to);
    }

    private String[] rowValues(WorkOrder order) {
        return new String[]{
                String.valueOf(order.getId()),
                safeText(order.getTitle()),
                safeText(order.getCustomerName()),
                safeText(order.getAssignee()),
                String.valueOf(order.getStatus()),
                String.valueOf(order.getPriority()),
                order.getDueDate() == null ? "" : order.getDueDate().toString(),
                order.getCreatedAt() == null ? "" : order.getCreatedAt().toString(),
                order.getUpdatedAt() == null ? "" : order.getUpdatedAt().toString()
        };
    }

    private String safeText(String value) {
        if (value == null) return "";
        if (value.matches("^[=+@\\t\\r].*")) return "'" + value;
        if (value.startsWith("-") && !value.matches("^-\\d+(?:[.,]\\d+)?$")) return "'" + value;
        return value;
    }

    private String csvCell(String value) {
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n") || escaped.contains("\r")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }
}
