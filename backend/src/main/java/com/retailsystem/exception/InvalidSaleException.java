package com.retailsystem.exception;

/** Named per the spec's required custom exception list. Maps to 400 via the same handler as BadRequestException. */
public class InvalidSaleException extends BadRequestException {
    public InvalidSaleException(String message) {
        super(message);
    }
}
