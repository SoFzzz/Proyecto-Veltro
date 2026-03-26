package com.veltro.inventory.application.pos.event;

import com.veltro.inventory.domain.pos.model.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Event published when a sale is confirmed (B2-01).
 *
 * <p>Listeners in B2-02 will handle inventory deduction based on this event.
 * The {@link com.veltro.inventory.application.pos.service.SaleService#confirm} method
 * publishes this event after successfully transitioning the sale to COMPLETED status.
 */
public record SaleCompletedEvent(
        Long saleId,
        String saleNumber,
        Long cashierId,
        BigDecimal total,
        PaymentMethod paymentMethod,
        LocalDateTime completedAt,
        List<SaleItemInfo> items
) {
}
