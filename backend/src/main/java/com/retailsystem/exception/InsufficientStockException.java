package com.retailsystem.exception;

/** Named per the spec's required custom exception list. Maps to 400 via the same handler as BadRequestException. */
public class InsufficientStockException extends BadRequestException {
    public InsufficientStockException(String message) {
        super(message);
    }
}
