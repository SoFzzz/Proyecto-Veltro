package com.veltro.inventory.application.purchasing.dto;

/**
 * Response DTO for a supplier (B2-04).
 */
public record SupplierResponse(
        Long id,
        String name,
        String taxId,
        String email,
        String phone,
        String address,
        String notes,
        boolean active
) {
}