package dev.kzone.portfolio.userapi.service;

import dev.kzone.portfolio.userapi.domain.Customer;
import dev.kzone.portfolio.userapi.domain.CustomerStatus;
import dev.kzone.portfolio.userapi.dto.CustomerCreateRequest;
import dev.kzone.portfolio.userapi.dto.CustomerResponse;
import dev.kzone.portfolio.userapi.dto.CustomerUpdateRequest;
import dev.kzone.portfolio.userapi.exception.CustomerNotFoundException;
import dev.kzone.portfolio.userapi.mapper.CustomerMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CustomerService {
    private final CustomerMapper customerMapper;

    public CustomerService(CustomerMapper customerMapper) {
        this.customerMapper = customerMapper;
    }

    public List<CustomerResponse> list(String keyword, CustomerStatus status) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        return customerMapper.findAll(normalizedKeyword, status).stream()
                .map(CustomerResponse::from)
                .toList();
    }

    public CustomerResponse get(long id) {
        return CustomerResponse.from(requireCustomer(id));
    }

    @Transactional
    public CustomerResponse create(CustomerCreateRequest request) {
        Customer customer = new Customer(
                null,
                request.companyName().trim(),
                normalize(request.contactName()),
                normalizeEmail(request.email()),
                normalize(request.phone()),
                request.status(),
                normalize(request.memo()),
                null,
                null
        );
        customerMapper.insert(customer);
        return get(customer.getId());
    }

    @Transactional
    public CustomerResponse update(long id, CustomerUpdateRequest request) {
        requireCustomer(id);
        Customer customer = new Customer(
                id,
                request.companyName().trim(),
                normalize(request.contactName()),
                normalizeEmail(request.email()),
                normalize(request.phone()),
                request.status(),
                normalize(request.memo()),
                null,
                null
        );
        customerMapper.update(customer);
        return get(id);
    }

    public Customer requireCustomer(long id) {
        return customerMapper.findById(id).orElseThrow(() -> new CustomerNotFoundException(id));
    }

    private String normalize(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeEmail(String value) {
        String normalized = normalize(value);
        return normalized == null ? null : normalized.toLowerCase();
    }
}
