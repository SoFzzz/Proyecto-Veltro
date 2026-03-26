package com.veltro.inventory.exception;

/**
 * Thrown when a product's salePrice is less than its costPrice.
 *
 * Mapped to HTTP 422 Unprocessable Content by {@code GlobalExceptionHandler}.
 */
public class InvalidPriceException extends RuntimeException {

    public InvalidPriceException(String message) {
        super(message);
    }
}
