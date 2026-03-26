package com.veltro.inventory.application.inventory.dto;

import java.time.OffsetDateTime;

public record AlertResponse(
        Long id,
        Long productId,
        String productName,
        String type,
        String severity,
        String message,
        boolean read,
        boolean resolved,
        OffsetDateTime createdAt) {
}
