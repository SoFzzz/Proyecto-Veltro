package com.veltro.inventory.application.inventory.dto;

public record AlertConfigurationResponse(
        Long productId,
        Integer criticalStock,
        Integer minStock,
        Integer overstockThreshold) {
}
