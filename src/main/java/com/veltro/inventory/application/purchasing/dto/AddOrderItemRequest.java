package com.veltro.inventory.application.purchasing.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Request DTO for adding an item to a purchase order (B2-04).
 * 
 * <p>Renamed from AddItemRequest to avoid naming conflict with POS module.
 */
public record AddOrderItemRequest(
        @NotNull(message = "Product ID is required")
        Long productId,

        @NotNull(message = "Requested quantity is required")
        @Positive(message = "Requested quantity must be greater than 0")
        Integer requestedQuantity,

        @NotNull(message = "Unit cost is required")
        @DecimalMin(value = "0.0001", message = "Unit cost must be greater than zero")
        BigDecimal unitCost
) {
}