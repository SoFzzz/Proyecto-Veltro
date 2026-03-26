package com.veltro.inventory.infrastructure.adapters.web;

import com.veltro.inventory.application.iam.dto.ChangePasswordRequest;
import com.veltro.inventory.application.iam.dto.LoginRequest;
import com.veltro.inventory.application.iam.dto.LoginResponse;
import com.veltro.inventory.application.iam.dto.RefreshRequest;
import com.veltro.inventory.application.iam.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link AuthController} (B1-02).
 *
 * Pure unit testing approach using direct method calls instead of MockMvc.
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;
    
    private AuthController controller;

    @BeforeEach
    void setUp() {
        controller = new AuthController(authService);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static LoginResponse stubResponse() {
        return LoginResponse.of("access.token.here", "refresh.token.here", 900L, "alice", "ADMIN");
    }

    private static UserDetails adminUser() {
        return User.withUsername("alice").password("irrelevant").roles("ADMIN").build();
    }

    // -------------------------------------------------------------------------
    // POST /login
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /login with valid credentials returns 200 and token pair")
    void login_validCredentials_returns200() {
        when(authService.login(any(LoginRequest.class))).thenReturn(stubResponse());

        LoginRequest request = new LoginRequest("alice", "secret123");
        ResponseEntity<LoginResponse> response = controller.login(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().accessToken()).isEqualTo("access.token.here");
        assertThat(response.getBody().refreshToken()).isEqualTo("refresh.token.here");
        assertThat(response.getBody().username()).isEqualTo("alice");
        assertThat(response.getBody().role()).isEqualTo("ADMIN");
        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("POST /login delegates to AuthService")
    void login_delegatesToAuthService() {
        when(authService.login(any(LoginRequest.class))).thenReturn(stubResponse());

        LoginRequest request = new LoginRequest("testuser", "testpass");
        controller.login(request);

        verify(authService).login(request);
    }

    @Test
    @DisplayName("POST /login handles service exceptions")
    void login_serviceThrowsException_propagatesException() {
        doThrow(new IllegalArgumentException("Invalid credentials"))
                .when(authService).login(any(LoginRequest.class));

        LoginRequest request = new LoginRequest("alice", "wrongpassword");

        assertThatThrownBy(() -> controller.login(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid credentials");

        verify(authService).login(any(LoginRequest.class));
    }

    // -------------------------------------------------------------------------
    // POST /refresh
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /refresh with valid refresh token returns 200 and new access token")
    void refresh_validToken_returns200() {
        when(authService.refresh(any(RefreshRequest.class))).thenReturn(stubResponse());

        RefreshRequest request = new RefreshRequest("valid.refresh.token");
        ResponseEntity<LoginResponse> response = controller.refresh(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().accessToken()).isEqualTo("access.token.here");
        verify(authService).refresh(any(RefreshRequest.class));
    }

    @Test
    @DisplayName("POST /refresh delegates to AuthService")
    void refresh_delegatesToAuthService() {
        when(authService.refresh(any(RefreshRequest.class))).thenReturn(stubResponse());

        RefreshRequest request = new RefreshRequest("refresh.token");
        controller.refresh(request);

        verify(authService).refresh(request);
    }

    @Test
    @DisplayName("POST /refresh handles service exceptions")
    void refresh_serviceThrowsException_propagatesException() {
        doThrow(new IllegalArgumentException("Refresh token is invalid or expired."))
                .when(authService).refresh(any(RefreshRequest.class));

        RefreshRequest request = new RefreshRequest("expired.token");

        assertThatThrownBy(() -> controller.refresh(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Refresh token is invalid or expired.");

        verify(authService).refresh(any(RefreshRequest.class));
    }

    // -------------------------------------------------------------------------
    // POST /logout
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /logout with authenticated user returns 200 and success message")
    void logout_authenticated_returns200() {
        doNothing().when(authService).logout("alice");

        UserDetails userDetails = adminUser();
        ResponseEntity<Map<String, Object>> response = controller.logout(userDetails);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(true);
        assertThat(response.getBody().get("message")).isEqualTo("Logged out successfully. Please discard your tokens.");
        verify(authService).logout("alice");
    }

    @Test
    @DisplayName("POST /logout delegates to AuthService with correct username")
    void logout_delegatesToAuthService() {
        doNothing().when(authService).logout("testuser");

        UserDetails userDetails = User.withUsername("testuser").password("irrelevant").roles("USER").build();
        controller.logout(userDetails);

        verify(authService).logout("testuser");
    }

    // -------------------------------------------------------------------------
    // PUT /change-password
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("PUT /change-password with valid request returns 200 and success message")
    void changePassword_valid_returns200() {
        doNothing().when(authService).changePassword(eq("alice"), any(ChangePasswordRequest.class));

        UserDetails userDetails = adminUser();
        ChangePasswordRequest request = new ChangePasswordRequest("oldPass1", "newPass1");
        ResponseEntity<Map<String, Object>> response = controller.changePassword(userDetails, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(true);
        assertThat(response.getBody().get("message")).isEqualTo("Password changed successfully.");
        verify(authService).changePassword(eq("alice"), any(ChangePasswordRequest.class));
    }

    @Test
    @DisplayName("PUT /change-password delegates to AuthService with correct parameters")
    void changePassword_delegatesToAuthService() {
        doNothing().when(authService).changePassword(eq("testuser"), any(ChangePasswordRequest.class));

        UserDetails userDetails = User.withUsername("testuser").password("irrelevant").roles("USER").build();
        ChangePasswordRequest request = new ChangePasswordRequest("currentPassword", "newPassword");
        controller.changePassword(userDetails, request);

        verify(authService).changePassword("testuser", request);
    }

    @Test
    @DisplayName("PUT /change-password handles service exceptions")
    void changePassword_serviceThrowsException_propagatesException() {
        doThrow(new IllegalArgumentException("Current password is incorrect."))
                .when(authService).changePassword(eq("alice"), any(ChangePasswordRequest.class));

        UserDetails userDetails = adminUser();
        ChangePasswordRequest request = new ChangePasswordRequest("wrongPass", "newPass1");

        assertThatThrownBy(() -> controller.changePassword(userDetails, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Current password is incorrect.");

        verify(authService).changePassword(eq("alice"), any(ChangePasswordRequest.class));
    }
}