package com.veltro.inventory.exception;

/**
 * Exception thrown when payment details are invalid (B2-01).
 *
 * <p>Thrown by {@link com.veltro.inventory.application.pos.service.SaleService#confirm}
 * when cash payment validation fails (amount received is null or less than total).
 *
 * <p>Mapped to HTTP 422 UNPROCESSABLE_ENTITY with error code "INVALID_PAYMENT"
 * by {@link com.veltro.inventory.infrastructure.adapters.web.GlobalExceptionHandler}.
 */
public class InvalidPaymentException extends RuntimeException {

    public InvalidPaymentException(String message) {
        super(message);
    }
}
