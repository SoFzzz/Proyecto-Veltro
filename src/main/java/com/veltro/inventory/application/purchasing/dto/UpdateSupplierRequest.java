package com.veltro.inventory.application.purchasing.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating an existing supplier (B2-04).
 */
public record UpdateSupplierRequest(
        @Size(max = 200, message = "Supplier name must not exceed 200 characters")
        String name,

        @Email(message = "Invalid email format")
        @Size(max = 200, message = "Email must not exceed 200 characters")
        String email,

        @Size(max = 20, message = "Phone must not exceed 20 characters")
        String phone,

        @Size(max = 500, message = "Address must not exceed 500 characters")
        String address,

        @Size(max = 500, message = "Notes must not exceed 500 characters")
        String notes
) {
}