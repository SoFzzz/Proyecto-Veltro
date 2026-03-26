package com.veltro.inventory.application.pos.event;

import java.math.BigDecimal;

/**
 * Item information snapshot for sale events (B2-01).
 *
 * <p>Used by {@link SaleCompletedEvent} and {@link SaleVoidedEvent} to carry
 * product and quantity details for downstream listeners (B2-02).
 */
public record SaleItemInfo(
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {
}
