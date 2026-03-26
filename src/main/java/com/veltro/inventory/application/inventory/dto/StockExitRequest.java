package com.veltro.inventory.application.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * Request to record a stock exit (e.g. shrinkage, manual removal).
 *
 * {@code quantity} must be at least 1. The service validates that
 * {@code currentStock - quantity >= 0} and throws
 * {@link com.veltro.inventory.exception.InsufficientStockException} (HTTP 422) if not (AC-04).
 */
public record StockExitRequest(
        @Min(value = 1, message = "Exit quantity must be at least 1")
        int quantity,

        @Size(max = 500, message = "Reason must not exceed 500 characters")
        String reason
) {
}
