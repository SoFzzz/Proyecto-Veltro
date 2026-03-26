package com.veltro.inventory.application.purchasing.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Event published when a purchase order is received (B2-04).
 *
 * <p>Listeners will handle inventory increment based on this event.
 * This event is published after successfully receiving merchandise and
 * transitioning the order to RECEIVED status.
 */
public record OrderReceivedEvent(
        Long orderId,
        String orderNumber,
        Long supplierId,
        String supplierName,
        BigDecimal total,
        LocalDateTime receivedAt,
        String receivedBy,
        List<ReceivedItemInfo> items
) {
}