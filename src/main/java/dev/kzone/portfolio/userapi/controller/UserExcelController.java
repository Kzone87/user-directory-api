package dev.kzone.portfolio.userapi.controller;

import dev.kzone.portfolio.userapi.dto.UserResponse;
import dev.kzone.portfolio.userapi.service.UserExcelService;
import dev.kzone.portfolio.userapi.service.UserService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserExcelController {
    private static final MediaType XLSX = MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );

    private final UserService userService;
    private final UserExcelService userExcelService;

    public UserExcelController(UserService userService, UserExcelService userExcelService) {
        this.userService = userService;
        this.userExcelService = userExcelService;
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String emailDomain
    ) {
        List<UserResponse> users = userService.search(keyword, emailDomain);
        byte[] workbook = userExcelService.createWorkbook(users);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename("users.xlsx", StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(XLSX)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(workbook);
    }
}
