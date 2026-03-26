package com.veltro.inventory.application.scanner.strategy;

import com.veltro.inventory.application.scanner.dto.ProductSuggestionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link BarcodeStrategy} (B3-01).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BarcodeStrategy")
class BarcodeStrategyTest {

    private BarcodeStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new BarcodeStrategy();
    }

    @Test
    @DisplayName("getType returns BARCODE")
    void getType_returnsBarcodeType() {
        assertThat(strategy.getType()).isEqualTo("BARCODE");
    }

    @Test
    @DisplayName("supports returns true for non-blank strings")
    void supports_stringInput_returnsTrue() {
        assertThat(strategy.supports("1234567890123")).isTrue();
    }

    @Test
    @DisplayName("supports returns false for blank strings")
    void supports_blankString_returnsFalse() {
        assertThat(strategy.supports("")).isFalse();
        assertThat(strategy.supports("   ")).isFalse();
    }

    @Test
    @DisplayName("supports returns false for null")
    void supports_null_returnsFalse() {
        assertThat(strategy.supports(null)).isFalse();
    }

    @Test
    @DisplayName("supports returns false for non-string types")
    void supports_nonString_returnsFalse() {
        assertThat(strategy.supports(123)).isFalse();
        assertThat(strategy.supports(new MockMultipartFile("file", new byte[0]))).isFalse();
    }

    @Test
    @DisplayName("process returns empty response (barcode lookup handled elsewhere)")
    void process_barcode_returnsEmptyResponse() {
        ProductSuggestionResponse response = strategy.process("1234567890123");
        
        assertThat(response).isNotNull();
        assertThat(response.suggestions()).isEmpty();
        assertThat(response.strategyUsed()).isEqualTo("BARCODE");
    }
}
