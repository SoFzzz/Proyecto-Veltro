package com.veltro.inventory.application.catalog.dto;

/**
 * Response record for a product.
 *
 * ADR-005: Monetary fields ({@code costPrice}, {@code salePrice}) are exposed as
 * {@code String} with 4 decimal places to avoid precision loss in JSON serialization.
 *
 * AC-06: No {@code passwordHash} or any security-sensitive field is present.
 */
public record ProductResponse(
        Long id,
        String name,
        String barcode,
        String sku,
        String description,
        /** ADR-005 — formatted as "0.0000" */
        String costPrice,
        /** ADR-005 — formatted as "0.0000" */
        String salePrice,
        Long categoryId,
        String categoryName,
        boolean active
) {
}
