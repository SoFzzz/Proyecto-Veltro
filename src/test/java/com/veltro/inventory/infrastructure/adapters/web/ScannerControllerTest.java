package com.veltro.inventory.infrastructure.adapters.web;

import com.veltro.inventory.application.scanner.dto.ProductSuggestionResponse;
import com.veltro.inventory.application.scanner.service.ScannerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ScannerController} (B3-01).
 *
 * Tests the REST controller endpoints for AI-powered product scanning.
 * Note: This uses pure unit testing approach rather than @WebMvcTest
 * to avoid Spring Boot test dependencies.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ScannerController")
class ScannerControllerTest {

    @Mock
    private ScannerService scannerService;

    private ScannerController scannerController;

    @BeforeEach
    void setUp() {
        scannerController = new ScannerController(scannerService);
    }

    @Test
    @DisplayName("scanWithAi returns product suggestions for valid image")
    void scanWithAi_validImage_returnsSuggestions() {
        // Arrange
        MockMultipartFile imageFile = new MockMultipartFile(
                "image", "product.jpg", "image/jpeg", new byte[]{1, 2, 3, 4, 5}
        );
        ProductSuggestionResponse.SuggestedProduct product =
                new ProductSuggestionResponse.SuggestedProduct(1L, "Test Product", 0.95, BigDecimal.valueOf(19.99), "1234567890123");
        ProductSuggestionResponse mockResponse =
                new ProductSuggestionResponse(List.of(product), 150L, "AI_VISION");

        when(scannerService.processImage(any(MultipartFile.class))).thenReturn(mockResponse);

        // Act
        ResponseEntity<ProductSuggestionResponse> response = scannerController.scanWithAi(imageFile);

        // Assert
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().suggestions()).hasSize(1);
        assertThat(response.getBody().suggestions().getFirst().productId()).isEqualTo(1L);
        assertThat(response.getBody().suggestions().getFirst().confidence()).isEqualTo(0.95);
        verify(scannerService).processImage(any(MultipartFile.class));
    }

    @Test
    @DisplayName("scanWithAi returns 400 for empty image")
    void scanWithAi_emptyImage_returnsBadRequest() {
        // Arrange
        MockMultipartFile emptyFile = new MockMultipartFile(
                "image", "empty.jpg", "image/jpeg", new byte[]{}
        );

        // Act
        ResponseEntity<ProductSuggestionResponse> response = scannerController.scanWithAi(emptyFile);

        // Assert
        assertThat(response.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    @DisplayName("scanWithAi returns 501 when AI Vision is not available")
    void scanWithAi_aiNotAvailable_returnsNotImplemented() {
        // Arrange
        MockMultipartFile imageFile = new MockMultipartFile(
                "image", "product.jpg", "image/jpeg", new byte[]{1, 2, 3}
        );
        when(scannerService.processImage(any(MultipartFile.class)))
                .thenThrow(new UnsupportedOperationException("AI Vision not configured"));

        // Act
        ResponseEntity<ProductSuggestionResponse> response = scannerController.scanWithAi(imageFile);

        // Assert
        assertThat(response.getStatusCode().value()).isEqualTo(501);
    }

    @Test
    @DisplayName("getStatus returns scanner strategy status")
    void getStatus_returnsStrategyStatus() {
        // Arrange
        Map<String, Boolean> mockStatus = Map.of("BARCODE", true, "AI_VISION", false);
        when(scannerService.getStrategyStatus()).thenReturn(mockStatus);

        // Act
        ResponseEntity<Map<String, Boolean>> response = scannerController.getStatus();

        // Assert
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsEntry("BARCODE", true);
        assertThat(response.getBody()).containsEntry("AI_VISION", false);
        verify(scannerService).getStrategyStatus();
    }

    @Test
    @DisplayName("isAiAvailable returns availability status when not configured")
    void isAiAvailable_notConfigured_returnsFalse() {
        // Arrange
        when(scannerService.isAiVisionAvailable()).thenReturn(false);

        // Act
        ResponseEntity<Map<String, Boolean>> response = scannerController.isAiAvailable();

        // Assert
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsEntry("available", false);
        verify(scannerService).isAiVisionAvailable();
    }

    @Test
    @DisplayName("isAiAvailable returns true when AI is configured")
    void isAiAvailable_configured_returnsTrue() {
        // Arrange
        when(scannerService.isAiVisionAvailable()).thenReturn(true);

        // Act
        ResponseEntity<Map<String, Boolean>> response = scannerController.isAiAvailable();

        // Assert
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsEntry("available", true);
    }
}
