package com.veltro.inventory.application.scanner.service;

import com.veltro.inventory.application.scanner.strategy.AiVisionStrategy;
import com.veltro.inventory.application.scanner.strategy.BarcodeStrategy;
import com.veltro.inventory.application.scanner.strategy.ScannerStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ScannerService} (B3-01).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ScannerService")
class ScannerServiceTest {

    @Mock
    private AiVisionStrategy aiVisionStrategy;

    private BarcodeStrategy barcodeStrategy;
    private ScannerService scannerService;

    @BeforeEach
    void setUp() {
        barcodeStrategy = new BarcodeStrategy();
        List<ScannerStrategy> strategies = List.of(barcodeStrategy, aiVisionStrategy);
        scannerService = new ScannerService(strategies, aiVisionStrategy);
    }

    @Test
    @DisplayName("isAiVisionAvailable returns false when API key not configured")
    void isAiVisionAvailable_noApiKey_returnsFalse() {
        when(aiVisionStrategy.isAvailable()).thenReturn(false);
        assertThat(scannerService.isAiVisionAvailable()).isFalse();
    }

    @Test
    @DisplayName("isAiVisionAvailable returns true when API key is configured")
    void isAiVisionAvailable_withApiKey_returnsTrue() {
        when(aiVisionStrategy.isAvailable()).thenReturn(true);
        assertThat(scannerService.isAiVisionAvailable()).isTrue();
    }

    @Test
    @DisplayName("getStrategyStatus returns correct status for all strategies")
    void getStrategyStatus_returnsAllStrategyStatus() {
        when(aiVisionStrategy.getType()).thenReturn("AI_VISION");
        when(aiVisionStrategy.isAvailable()).thenReturn(false);

        Map<String, Boolean> status = scannerService.getStrategyStatus();

        assertThat(status).containsKey("BARCODE");
        assertThat(status).containsKey("AI_VISION");
        assertThat(status.get("BARCODE")).isTrue();
        assertThat(status.get("AI_VISION")).isFalse();
    }

    @Test
    @DisplayName("findStrategy returns BarcodeStrategy for string input")
    void findStrategy_stringInput_returnsBarcodeStrategy() {
        // BarcodeStrategy supports strings, so it's found first without needing aiVisionStrategy
        ScannerStrategy found = scannerService.findStrategy("1234567890123");
        
        assertThat(found).isNotNull();
        assertThat(found.getType()).isEqualTo("BARCODE");
    }

    @Test
    @DisplayName("findStrategy returns AiVisionStrategy for MultipartFile input")
    void findStrategy_multipartFileInput_returnsAiVisionStrategy() {
        MockMultipartFile imageFile = new MockMultipartFile(
                "image", "test.jpg", "image/jpeg", new byte[]{1, 2, 3}
        );
        when(aiVisionStrategy.supports(imageFile)).thenReturn(true);
        when(aiVisionStrategy.getType()).thenReturn("AI_VISION");

        ScannerStrategy found = scannerService.findStrategy(imageFile);

        assertThat(found).isNotNull();
        assertThat(found.getType()).isEqualTo("AI_VISION");
    }

    @Test
    @DisplayName("findStrategy returns null when no strategy supports input")
    void findStrategy_unsupportedInput_returnsNull() {
        when(aiVisionStrategy.supports(123)).thenReturn(false);

        ScannerStrategy found = scannerService.findStrategy(123);

        assertThat(found).isNull();
    }

    @Test
    @DisplayName("processImage delegates to AiVisionStrategy")
    void processImage_delegatesToAiVisionStrategy() {
        MockMultipartFile imageFile = new MockMultipartFile(
                "image", "test.jpg", "image/jpeg", new byte[]{1, 2, 3}
        );
        when(aiVisionStrategy.process(imageFile))
                .thenThrow(new UnsupportedOperationException("AI Vision not available"));

        assertThatThrownBy(() -> scannerService.processImage(imageFile))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("AI Vision not available");
    }
}
