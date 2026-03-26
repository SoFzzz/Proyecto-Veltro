package com.veltro.inventory.infrastructure.adapters.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Typed configuration properties for JWT token generation (B1-02).
 *
 * Bound from the {@code jwt.*} namespace in application.yml.
 * The secret must be at least 256 bits (32 chars) for HMAC-SHA256.
 *
 * @param secret                 HMAC-SHA256 signing secret. Override via JWT_SECRET env var in prod.
 * @param accessTokenExpiration  Access token lifetime in seconds (default: 900 = 15 min).
 * @param refreshTokenExpiration Refresh token lifetime in seconds (default: 604800 = 7 days).
 */
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        String secret,
        long accessTokenExpiration,
        long refreshTokenExpiration
) {
}
