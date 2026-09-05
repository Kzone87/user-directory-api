package dev.kzone.portfolio.userapi.controller;

import dev.kzone.portfolio.userapi.domain.CustomerStatus;
import dev.kzone.portfolio.userapi.dto.CustomerCreateRequest;
import dev.kzone.portfolio.userapi.dto.CustomerResponse;
import dev.kzone.portfolio.userapi.dto.CustomerUpdateRequest;
import dev.kzone.portfolio.userapi.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {
    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public List<CustomerResponse> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) CustomerStatus status
    ) {
        return customerService.list(keyword, status);
    }

    @GetMapping("/{id}")
    public CustomerResponse get(@PathVariable long id) {
        return customerService.get(id);
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CustomerCreateRequest request) {
        CustomerResponse created = customerService.create(request);
        return ResponseEntity.created(URI.create("/api/customers/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public CustomerResponse update(
            @PathVariable long id,
            @Valid @RequestBody CustomerUpdateRequest request
    ) {
        return customerService.update(id, request);
    }
}
