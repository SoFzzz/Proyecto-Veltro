package com.veltro.inventory.application.inventory.dto;

import jakarta.validation.constraints.Min;

/**
 * Request to update the stock alert thresholds for an inventory record.
 *
 * Both values must be zero or positive. Business logic in the service may
 * optionally warn when {@code minStock > maxStock}, but does not reject it
 * as that constraint is not encoded in the SDD spec for B1-04.
 */
public record UpdateStockLimitsRequest(
        @Min(value = 0, message = "Minimum stock must be zero or greater")
        int minStock,

        @Min(value = 0, message = "Maximum stock must be zero or greater")
        int maxStock
) {
}
