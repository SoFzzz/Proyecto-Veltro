package com.veltro.inventory.application.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request record for creating a new category.
 */
public record CreateCategoryRequest(

        @NotBlank(message = "Category name must not be blank")
        @Size(max = 100, message = "Category name must not exceed 100 characters")
        String name,

        String description,

        /** Optional — null means this is a root category. */
        Long parentCategoryId
) {
}
