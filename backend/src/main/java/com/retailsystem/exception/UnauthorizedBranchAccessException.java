package com.retailsystem.exception;

import org.springframework.security.access.AccessDeniedException;

/**
 * Named per the spec's required custom exception list. Extends Spring
 * Security's AccessDeniedException so it's caught by the existing
 * handler (403) without needing a new @ExceptionHandler method.
 */
public class UnauthorizedBranchAccessException extends AccessDeniedException {
    public UnauthorizedBranchAccessException(String message) {
        super(message);
    }
}
