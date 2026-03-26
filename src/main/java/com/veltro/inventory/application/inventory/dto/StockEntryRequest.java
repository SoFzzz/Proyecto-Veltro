package com.veltro.inventory.application.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * Request to record a stock entry (e.g. goods received, manual addition).
 *
 * {@code quantity} must be at least 1 — the DB also enforces {@code quantity > 0}.
 */
public record StockEntryRequest(
        @Min(value = 1, message = "Entry quantity must be at least 1")
        int quantity,

        @Size(max = 500, message = "Reason must not exceed 500 characters")
        String reason
) {
}
