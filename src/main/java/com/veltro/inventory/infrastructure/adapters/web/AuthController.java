package com.veltro.inventory.infrastructure.adapters.web;

import com.veltro.inventory.application.iam.dto.ChangePasswordRequest;
import com.veltro.inventory.application.iam.dto.LoginRequest;
import com.veltro.inventory.application.iam.dto.LoginResponse;
import com.veltro.inventory.application.iam.dto.RefreshRequest;
import com.veltro.inventory.application.iam.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST controller for IAM endpoints (B1-02).
 *
 * All endpoints are under {@code /api/v1/auth}.
 * {@code /login} and {@code /refresh} are public (see SecurityConfig).
 * {@code /logout} and {@code /change-password} require a valid Bearer token.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Authenticates a user and returns an access + refresh token pair.
     *
     * @return HTTP 200 with {@link LoginResponse} on success.
     *         HTTP 401 if credentials are invalid (thrown by AuthenticationManager).
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * Exchanges a valid refresh token for a new access token.
     *
     * @return HTTP 200 with a new {@link LoginResponse} containing the new access token.
     *         HTTP 400 if the refresh token is missing or invalid.
     */
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    /**
     * Stateless logout — the server has no session to invalidate.
     * The client is responsible for discarding its tokens.
     *
     * @return HTTP 200 with a confirmation message.
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(
            @AuthenticationPrincipal UserDetails userDetails) {

        authService.logout(userDetails.getUsername());
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Logged out successfully. Please discard your tokens."));
    }

    /**
     * Changes the authenticated user's password.
     *
     * @return HTTP 200 on success.
     *         HTTP 400 if current password is wrong or validation fails.
     */
    @PutMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request) {

        authService.changePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Password changed successfully."));
    }
}
