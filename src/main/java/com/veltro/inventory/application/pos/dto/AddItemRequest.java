package com.veltro.inventory.application.pos.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request DTO for adding an item to a sale (B2-01 | POST /api/v1/sales/{id}/items).
 */
public record AddItemRequest(
        @NotNull(message = "Product ID is required")
        Long productId,

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be greater than 0")
        Integer quantity
) {
}
