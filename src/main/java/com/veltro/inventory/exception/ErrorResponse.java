package com.veltro.inventory.exception;

import java.time.Instant;

/**
 * Immutable error payload returned by the API for all error responses.
 *
 * {@code success} is always {@code false} for error responses.
 * {@code errors} is populated only for validation failures (HTTP 400).
 */
public record ErrorResponse(
        boolean success,
        String error,
        String message,
        String timestamp,
        String path
) {
    /**
     * Convenience factory for building a standard error response.
     */
    public static ErrorResponse of(String errorCode, String message, String path) {
        return new ErrorResponse(false, errorCode, message, Instant.now().toString(), path);
    }
}
