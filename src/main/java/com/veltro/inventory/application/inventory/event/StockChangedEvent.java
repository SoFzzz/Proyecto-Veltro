package com.veltro.inventory.application.inventory.event;

import java.time.OffsetDateTime;

public record StockChangedEvent(
        Long productId,
        String productName,
        int previousStock,
        int newStock,
        String reason,
        OffsetDateTime occurredAt) {
}
