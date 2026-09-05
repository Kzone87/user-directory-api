package dev.kzone.portfolio.userapi.dto;

import dev.kzone.portfolio.userapi.domain.Customer;
import dev.kzone.portfolio.userapi.domain.CustomerStatus;

import java.time.LocalDateTime;

public record CustomerResponse(
        Long id,
        String companyName,
        String contactName,
        String email,
        String phone,
        CustomerStatus status,
        String memo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CustomerResponse from(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getCompanyName(),
                customer.getContactName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getStatus(),
                customer.getMemo(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }
}
