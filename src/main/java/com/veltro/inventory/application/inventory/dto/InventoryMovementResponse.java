package com.veltro.inventory.application.inventory.dto;

import java.time.Instant;

/**
 * Response record for a single inventory movement entry (B1-04).
 *
 * Append-only: no update fields. {@code createdAt} and {@code createdBy}
 * are populated automatically by Spring Data JPA auditing via
 * {@link com.veltro.inventory.infrastructure.adapters.config.VeltroAuditorAware}.
 */
public record InventoryMovementResponse(
        Long id,
        Long inventoryId,
        String movementType,
        Integer quantity,
        Integer previousStock,
        Integer newStock,
        String reason,
        Instant createdAt,
        String createdBy
) {
}
