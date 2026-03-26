package com.veltro.inventory.infrastructure.adapters.security;

import com.veltro.inventory.infrastructure.adapters.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * Generates and validates HMAC-SHA256 signed JWTs (B1-02).
 *
 * <ul>
 *   <li>Access tokens expire in {@code jwt.access-token-expiration} seconds (15 min default).</li>
 *   <li>Refresh tokens expire in {@code jwt.refresh-token-expiration} seconds (7 days default).</li>
 * </ul>
 *
 * The {@code type} claim distinguishes ACCESS from REFRESH tokens so that a refresh
 * token cannot be used as a bearer token on protected endpoints.
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_TYPE = "type";
    private static final String TYPE_ACCESS = "ACCESS";
    private static final String TYPE_REFRESH = "REFRESH";

    private final SecretKey signingKey;
    private final long accessExpSeconds;
    private final long refreshExpSeconds;

    public JwtTokenProvider(JwtProperties props) {
        this.signingKey = Keys.hmacShaKeyFor(
                props.secret().getBytes(StandardCharsets.UTF_8));
        this.accessExpSeconds = props.accessTokenExpiration();
        this.refreshExpSeconds = props.refreshTokenExpiration();
    }

    // -------------------------------------------------------------------------
    // Token generation
    // -------------------------------------------------------------------------

    /**
     * Creates an ACCESS token for the authenticated user.
     * Contains claims: {@code sub} (username), {@code role}, {@code type=ACCESS}.
     */
    public String generateAccessToken(UserDetails userDetails) {
        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .orElse("");

        return buildToken(userDetails.getUsername(),
                Map.of(CLAIM_ROLE, role, CLAIM_TYPE, TYPE_ACCESS),
                accessExpSeconds);
    }

    /**
     * Creates a REFRESH token for the authenticated user.
     * Contains claims: {@code sub} (username), {@code type=REFRESH}.
     */
    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(userDetails.getUsername(),
                Map.of(CLAIM_TYPE, TYPE_REFRESH),
                refreshExpSeconds);
    }

    private String buildToken(String subject, Map<String, Object> extraClaims, long expirationSeconds) {
        long nowMs = System.currentTimeMillis();
        return Jwts.builder()
                .subject(subject)
                .claims(extraClaims)
                .issuedAt(new Date(nowMs))
                .expiration(new Date(nowMs + expirationSeconds * 1_000L))
                .signWith(signingKey)
                .compact();
    }

    // -------------------------------------------------------------------------
    // Token validation and claim extraction
    // -------------------------------------------------------------------------

    /**
     * Returns {@code true} if the token has a valid signature, is not expired,
     * and its {@code type} claim equals {@code ACCESS}.
     */
    public boolean isValidAccessToken(String token) {
        return isValidTokenOfType(token, TYPE_ACCESS);
    }

    /**
     * Returns {@code true} if the token has a valid signature, is not expired,
     * and its {@code type} claim equals {@code REFRESH}.
     */
    public boolean isValidRefreshToken(String token) {
        return isValidTokenOfType(token, TYPE_REFRESH);
    }

    private boolean isValidTokenOfType(String token, String expectedType) {
        try {
            Claims claims = parseClaims(token);
            return expectedType.equals(claims.get(CLAIM_TYPE, String.class));
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("Invalid JWT [{}]: {}", expectedType, ex.getMessage());
            return false;
        }
    }

    /**
     * Extracts the username ({@code sub} claim) from a token.
     * Caller must validate the token before trusting this value.
     */
    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Extracts the role claim from an ACCESS token.
     */
    public String extractRole(String token) {
        return parseClaims(token).get(CLAIM_ROLE, String.class);
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
