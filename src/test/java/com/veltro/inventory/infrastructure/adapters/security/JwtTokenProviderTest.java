package com.veltro.inventory.infrastructure.adapters.security;

import com.veltro.inventory.infrastructure.adapters.config.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link JwtTokenProvider}.
 *
 * No Spring context loaded — tests the token generation and validation logic
 * in isolation using a test JwtProperties instance.
 */
class JwtTokenProviderTest {

    // Minimum 256-bit secret for HMAC-SHA256
    private static final String TEST_SECRET =
            "test-secret-for-unit-tests-minimum-256-bits-padding!!";

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties(TEST_SECRET, 900L, 604800L);
        jwtTokenProvider = new JwtTokenProvider(props);
    }

    private UserDetails buildUserDetails(String username, String role) {
        return User.builder()
                .username(username)
                .password("hashed")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + role)))
                .build();
    }

    // -------------------------------------------------------------------------
    // Access token tests
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("generateAccessToken returns a non-blank token")
    void generateAccessToken_returnsNonBlankToken() {
        UserDetails user = buildUserDetails("alice", "ADMIN");
        String token = jwtTokenProvider.generateAccessToken(user);
        assertThat(token).isNotBlank();
    }

    @Test
    @DisplayName("isValidAccessToken returns true for a freshly generated access token")
    void isValidAccessToken_freshToken_returnsTrue() {
        UserDetails user = buildUserDetails("alice", "ADMIN");
        String token = jwtTokenProvider.generateAccessToken(user);
        assertThat(jwtTokenProvider.isValidAccessToken(token)).isTrue();
    }

    @Test
    @DisplayName("isValidAccessToken returns false for a refresh token")
    void isValidAccessToken_withRefreshToken_returnsFalse() {
        UserDetails user = buildUserDetails("alice", "ADMIN");
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);
        assertThat(jwtTokenProvider.isValidAccessToken(refreshToken)).isFalse();
    }

    @Test
    @DisplayName("extractUsername returns the correct subject from an access token")
    void extractUsername_accessToken_returnsCorrectUsername() {
        UserDetails user = buildUserDetails("alice", "ADMIN");
        String token = jwtTokenProvider.generateAccessToken(user);
        assertThat(jwtTokenProvider.extractUsername(token)).isEqualTo("alice");
    }

    @Test
    @DisplayName("extractRole returns the correct role from an access token")
    void extractRole_accessToken_returnsCorrectRole() {
        UserDetails user = buildUserDetails("alice", "ADMIN");
        String token = jwtTokenProvider.generateAccessToken(user);
        assertThat(jwtTokenProvider.extractRole(token)).isEqualTo("ADMIN");
    }

    // -------------------------------------------------------------------------
    // Refresh token tests
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("generateRefreshToken returns a non-blank token")
    void generateRefreshToken_returnsNonBlankToken() {
        UserDetails user = buildUserDetails("bob", "CASHIER");
        String token = jwtTokenProvider.generateRefreshToken(user);
        assertThat(token).isNotBlank();
    }

    @Test
    @DisplayName("isValidRefreshToken returns true for a freshly generated refresh token")
    void isValidRefreshToken_freshToken_returnsTrue() {
        UserDetails user = buildUserDetails("bob", "CASHIER");
        String token = jwtTokenProvider.generateRefreshToken(user);
        assertThat(jwtTokenProvider.isValidRefreshToken(token)).isTrue();
    }

    @Test
    @DisplayName("isValidRefreshToken returns false for an access token")
    void isValidRefreshToken_withAccessToken_returnsFalse() {
        UserDetails user = buildUserDetails("bob", "CASHIER");
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        assertThat(jwtTokenProvider.isValidRefreshToken(accessToken)).isFalse();
    }

    @Test
    @DisplayName("extractUsername returns the correct subject from a refresh token")
    void extractUsername_refreshToken_returnsCorrectUsername() {
        UserDetails user = buildUserDetails("bob", "CASHIER");
        String token = jwtTokenProvider.generateRefreshToken(user);
        assertThat(jwtTokenProvider.extractUsername(token)).isEqualTo("bob");
    }

    // -------------------------------------------------------------------------
    // Expired / tampered token tests
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("isValidAccessToken returns false for a completely invalid string")
    void isValidAccessToken_garbage_returnsFalse() {
        assertThat(jwtTokenProvider.isValidAccessToken("not.a.jwt")).isFalse();
    }

    @Test
    @DisplayName("isValidAccessToken returns false for a blank string")
    void isValidAccessToken_blank_returnsFalse() {
        assertThat(jwtTokenProvider.isValidAccessToken("")).isFalse();
    }

    @Test
    @DisplayName("Access token generated with a different secret fails validation")
    void isValidAccessToken_differentSecret_returnsFalse() {
        JwtProperties otherProps = new JwtProperties(
                "different-secret-totally-different-from-original-key!!", 900L, 604800L);
        JwtTokenProvider otherProvider = new JwtTokenProvider(otherProps);

        UserDetails user = buildUserDetails("alice", "ADMIN");
        String tokenFromOther = otherProvider.generateAccessToken(user);

        assertThat(jwtTokenProvider.isValidAccessToken(tokenFromOther)).isFalse();
    }

    @Test
    @DisplayName("Token expired immediately when expiration is 0 seconds")
    void isValidAccessToken_expiredToken_returnsFalse() throws InterruptedException {
        JwtProperties shortProps = new JwtProperties(TEST_SECRET, 0L, 0L);
        JwtTokenProvider shortProvider = new JwtTokenProvider(shortProps);
        UserDetails user = buildUserDetails("alice", "ADMIN");

        String token = shortProvider.generateAccessToken(user);
        // Wait 1 second to ensure expiration
        Thread.sleep(1_100);

        assertThat(shortProvider.isValidAccessToken(token)).isFalse();
    }
}
