package com.veltro.inventory.application.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request to set stock to a specific absolute value (physical count correction).
 *
 * {@code newStock} replaces {@code currentStock} entirely; it must be >= 0.
 * A reason is required to maintain an auditable trail of every adjustment.
 */
public record StockAdjustmentRequest(
        @Min(value = 0, message = "New stock must be zero or greater")
        int newStock,

        @NotBlank(message = "Reason is required for stock adjustments")
        @Size(max = 500, message = "Reason must not exceed 500 characters")
        String reason
) {
}
