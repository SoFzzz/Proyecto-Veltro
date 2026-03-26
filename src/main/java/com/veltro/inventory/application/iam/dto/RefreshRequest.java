package com.veltro.inventory.application.iam.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Payload for {@code POST /api/v1/auth/refresh}.
 */
public record RefreshRequest(

        @NotBlank(message = "Refresh token is required")
        String refreshToken
) {
}
