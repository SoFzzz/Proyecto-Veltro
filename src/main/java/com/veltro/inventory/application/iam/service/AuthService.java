package com.veltro.inventory.application.iam.service;

import com.veltro.inventory.application.iam.dto.ChangePasswordRequest;
import com.veltro.inventory.application.iam.dto.LoginRequest;
import com.veltro.inventory.application.iam.dto.LoginResponse;
import com.veltro.inventory.application.iam.dto.RefreshRequest;
import com.veltro.inventory.domain.iam.model.UserEntity;
import com.veltro.inventory.domain.iam.ports.UserRepository;
import com.veltro.inventory.exception.NotFoundException;
import com.veltro.inventory.infrastructure.adapters.config.JwtProperties;
import com.veltro.inventory.infrastructure.adapters.security.CustomUserDetailsService;
import com.veltro.inventory.infrastructure.adapters.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service for IAM operations (B1-02).
 *
 * <ul>
 *   <li>{@link #login} — authenticates credentials, issues Access + Refresh tokens.</li>
 *   <li>{@link #refresh} — validates a Refresh token, issues a new Access token.</li>
 *   <li>{@link #logout} — stateless: no server-side action needed; documented for clarity.</li>
 *   <li>{@link #changePassword} — validates current password, hashes and persists the new one.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProperties jwtProperties;

    // -------------------------------------------------------------------------
    // Login
    // -------------------------------------------------------------------------

    /**
     * Authenticates username/password and returns a token pair.
     * Delegates credential validation to Spring Security's {@link AuthenticationManager}.
     */
    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.username());

        String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .orElse("");

        log.info("User '{}' logged in successfully", request.username());

        return LoginResponse.of(
                accessToken,
                refreshToken,
                jwtProperties.accessTokenExpiration(),
                request.username(),
                role);
    }

    // -------------------------------------------------------------------------
    // Refresh
    // -------------------------------------------------------------------------

    /**
     * Validates a Refresh token and issues a new Access token.
     * The Refresh token itself is NOT rotated (stateless strategy).
     */
    public LoginResponse refresh(RefreshRequest request) {
        String token = request.refreshToken();

        if (!jwtTokenProvider.isValidRefreshToken(token)) {
            throw new IllegalArgumentException("Refresh token is invalid or expired.");
        }

        String username = jwtTokenProvider.extractUsername(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        String newAccessToken = jwtTokenProvider.generateAccessToken(userDetails);

        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .orElse("");

        log.debug("Access token refreshed for user '{}'", username);

        return LoginResponse.of(
                newAccessToken,
                token,
                jwtProperties.accessTokenExpiration(),
                username,
                role);
    }

    // -------------------------------------------------------------------------
    // Logout (stateless — documented no-op on the server)
    // -------------------------------------------------------------------------

    /**
     * Stateless logout. The client is responsible for discarding both tokens.
     * This method exists for future extension (e.g., token blocklist) without
     * changing the controller contract.
     */
    public void logout(String username) {
        log.info("User '{}' logged out (stateless — client must discard tokens)", username);
    }

    // -------------------------------------------------------------------------
    // Change password
    // -------------------------------------------------------------------------

    /**
     * Validates the current password and persists the new BCrypt hash.
     * BCrypt cost factor is defined by the {@link PasswordEncoder} bean (12).
     */
    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        UserEntity user = userRepository.findByUsernameAndActiveTrue(username)
                .orElseThrow(() -> new NotFoundException("User not found: " + username));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        log.info("Password changed successfully for user '{}'", username);
    }
}
