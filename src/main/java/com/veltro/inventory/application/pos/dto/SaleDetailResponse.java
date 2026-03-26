package com.veltro.inventory.application.pos.dto;

import com.veltro.inventory.application.shared.dto.AuditInfo;

/**
 * Response DTO for sale detail (B2-01).
 */
public record SaleDetailResponse(
        Long id,
        Long productId,
        String productName,
        Integer quantity,
        String unitPrice,
        String subtotal,
        Long version,
        AuditInfo auditInfo
) {
}
