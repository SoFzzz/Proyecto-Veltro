package com.veltro.inventory.application.pos.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request DTO for modifying an item quantity in a sale (B2-01 | PUT /api/v1/sales/{id}/items/{itemId}).
 */
public record ModifyItemRequest(
        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be greater than 0")
        Integer quantity
) {
}
