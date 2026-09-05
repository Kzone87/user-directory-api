package dev.kzone.portfolio.userapi.service;

import dev.kzone.portfolio.userapi.dto.UserResponse;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class UserExcelService {
    public byte[] createWorkbook(List<UserResponse> users) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Users");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("Name");
            header.createCell(2).setCellValue("Email");
            header.createCell(3).setCellValue("Created At");

            for (int index = 0; index < users.size(); index++) {
                UserResponse user = users.get(index);
                Row row = sheet.createRow(index + 1);
                row.createCell(0).setCellValue(user.id());
                row.createCell(1).setCellValue(user.name());
                row.createCell(2).setCellValue(user.email());
                row.createCell(3).setCellValue(user.createdAt() == null ? "" : user.createdAt().toString());
            }

            for (int column = 0; column < 4; column++) {
                sheet.autoSizeColumn(column);
            }
            workbook.write(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to create Excel workbook", exception);
        }
    }
}
