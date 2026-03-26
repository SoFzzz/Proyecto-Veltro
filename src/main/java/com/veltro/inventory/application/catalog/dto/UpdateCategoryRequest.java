package com.veltro.inventory.application.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request record for updating an existing category.
 */
public record UpdateCategoryRequest(

        @NotBlank(message = "Category name must not be blank")
        @Size(max = 100, message = "Category name must not exceed 100 characters")
        String name,

        String description,

        /** Optional — set to null to move category to root level. */
        Long parentCategoryId
) {
}
