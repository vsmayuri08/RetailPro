package com.retailsystem.exception;

/** Named per the spec's required custom exception list. Maps to 404 via the same handler as ResourceNotFoundException. */
public class ProductNotFoundException extends ResourceNotFoundException {
    public ProductNotFoundException(Object productId) {
        super("Product", "id", productId);
    }
}
