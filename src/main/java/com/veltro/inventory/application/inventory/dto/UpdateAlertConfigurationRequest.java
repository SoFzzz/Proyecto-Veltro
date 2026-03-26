package com.veltro.inventory.application.inventory.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateAlertConfigurationRequest(
        @NotNull
        @Min(0)
        Integer criticalStock,

        @NotNull
        @Min(1)
        Integer minStock,

        @NotNull
        @Min(1)
        @Max(100000)
        Integer overstockThreshold) {
}
