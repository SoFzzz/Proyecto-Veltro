package com.veltro.inventory.application.purchasing.dto;

import com.veltro.inventory.application.shared.dto.AuditInfo;
import com.veltro.inventory.domain.purchasing.model.PurchaseOrderStatus;

import java.util.List;

/**
 * Response DTO for purchase order (B2-04).
 * 
 * <p>ADR-005: Monetary fields are exposed as String with 4 decimal places.
 */
public record PurchaseOrderResponse(
        Long id,
        String orderNumber,
        PurchaseOrderStatus status,
        Long supplierId,
        String supplierName,
        String total,
        String notes,
        List<PurchaseOrderDetailResponse> details,
        Long version,
        AuditInfo auditInfo
) {
}