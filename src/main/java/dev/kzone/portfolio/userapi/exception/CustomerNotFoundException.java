package dev.kzone.portfolio.userapi.exception;

public class CustomerNotFoundException extends RuntimeException {
    public CustomerNotFoundException(long id) {
        super("Customer not found: " + id);
    }
}
