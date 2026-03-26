package com.veltro.inventory.application.catalog.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Request record for creating a new product.
 *
 * Client-side price sanity is validated here with {@code @DecimalMin}.
 * The stricter domain constraint {@code salePrice >= costPrice} is enforced
 * in {@code ProductService} and throws {@code InvalidPriceException}.
 */
public record CreateProductRequest(

        @NotBlank(message = "Product name must not be blank")
        @Size(max = 200, message = "Product name must not exceed 200 characters")
        String name,

        @Size(max = 100, message = "Barcode must not exceed 100 characters")
        String barcode,

        @Size(max = 100, message = "SKU must not exceed 100 characters")
        String sku,

        String description,

        @NotNull(message = "Cost price is required")
        @DecimalMin(value = "0.0001", message = "Cost price must be greater than zero")
        BigDecimal costPrice,

        @NotNull(message = "Sale price is required")
        @DecimalMin(value = "0.0001", message = "Sale price must be greater than zero")
        BigDecimal salePrice,

        Long categoryId
) {
}
