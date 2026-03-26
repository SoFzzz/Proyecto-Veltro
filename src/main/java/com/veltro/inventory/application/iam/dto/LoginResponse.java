package com.veltro.inventory.application.iam.dto;

/**
 * Response payload for successful authentication.
 * Returned by {@code POST /api/v1/auth/login} and {@code POST /api/v1/auth/refresh}.
 */
public record LoginResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        String username,
        String role
) {
    /**
     * Convenience factory. {@code tokenType} is always "Bearer".
     */
    public static LoginResponse of(
            String accessToken,
            String refreshToken,
            long expiresIn,
            String username,
            String role) {
        return new LoginResponse(accessToken, refreshToken, "Bearer", expiresIn, username, role);
    }
}
