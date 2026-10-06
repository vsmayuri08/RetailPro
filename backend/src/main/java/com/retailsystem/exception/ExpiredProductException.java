package com.retailsystem.exception;

/** Named per the spec's required custom exception list. Maps to 400 via the same handler as BadRequestException. */
public class ExpiredProductException extends BadRequestException {
    public ExpiredProductException(String message) {
        super(message);
    }
}
