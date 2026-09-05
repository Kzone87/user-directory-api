package dev.kzone.portfolio.userapi.dto;

import dev.kzone.portfolio.userapi.domain.CustomerStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CustomerUpdateRequest(
        @NotBlank @Size(max = 120) String companyName,
        @Size(max = 100) String contactName,
        @Email @Size(max = 255) String email,
        @Size(max = 30) String phone,
        @NotNull CustomerStatus status,
        @Size(max = 1000) String memo
) {
}
