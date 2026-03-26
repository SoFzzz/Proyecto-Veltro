package com.veltro.inventory.application.pos.dto;

import com.veltro.inventory.application.shared.dto.AuditInfo;
import com.veltro.inventory.domain.pos.model.PaymentMethod;
import com.veltro.inventory.domain.pos.model.SaleStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for sale (B2-01).
 */
public record SaleResponse(
        Long id,
        String saleNumber,
        SaleStatus status,
        Long cashierId,
        String subtotal,
        String total,
        String amountReceived,
        String change,
        PaymentMethod paymentMethod,
        LocalDateTime completedAt,
        List<SaleDetailResponse> details,
        Long version,
        AuditInfo auditInfo
) {
}
