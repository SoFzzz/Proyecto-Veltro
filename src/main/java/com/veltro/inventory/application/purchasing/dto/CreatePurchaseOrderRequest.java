package com.veltro.inventory.application.purchasing.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a new purchase order (B2-04).
 */
public record CreatePurchaseOrderRequest(
        @NotNull(message = "Supplier ID is required")
        Long supplierId,

        @Size(max = 1000, message = "Notes must not exceed 1000 characters")
        String notes
) {
}