package com.retailsystem.exception;

/** Named per the spec's required custom exception list. Maps to 404 via the same handler as ResourceNotFoundException. */
public class CustomerNotFoundException extends ResourceNotFoundException {
    public CustomerNotFoundException(Object customerId) {
        super("Customer", "id", customerId);
    }
}
