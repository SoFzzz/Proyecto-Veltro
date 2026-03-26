package com.veltro.inventory.application.scanner.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link AiVisionStrategy} (B3-01).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AiVisionStrategy")
class AiVisionStrategyTest {

    private AiVisionStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new AiVisionStrategy();
    }

    @Test
    @DisplayName("getType returns AI_VISION")
    void getType_returnsAiVisionType() {
        assertThat(strategy.getType()).isEqualTo("AI_VISION");
    }

    @Test
    @DisplayName("supports returns true for non-empty image files")
    void supports_imageFile_returnsTrue() {
        MultipartFile imageFile = new MockMultipartFile(
                "image", "test.jpg", "image/jpeg", new byte[]{1, 2, 3}
        );
        assertThat(strategy.supports(imageFile)).isTrue();
    }

    @Test
    @DisplayName("supports returns false for empty files")
    void supports_emptyFile_returnsFalse() {
        MultipartFile emptyFile = new MockMultipartFile(
                "image", "test.jpg", "image/jpeg", new byte[0]
        );
        assertThat(strategy.supports(emptyFile)).isFalse();
    }

    @Test
    @DisplayName("supports returns false for non-image content types")
    void supports_nonImageContentType_returnsFalse() {
        MultipartFile textFile = new MockMultipartFile(
                "file", "test.txt", "text/plain", new byte[]{1, 2, 3}
        );
        assertThat(strategy.supports(textFile)).isFalse();
    }

    @Test
    @DisplayName("supports returns false for null content type")
    void supports_nullContentType_returnsFalse() {
        MultipartFile nullContentType = new MockMultipartFile(
                "file", "test.bin", null, new byte[]{1, 2, 3}
        );
        assertThat(strategy.supports(nullContentType)).isFalse();
    }

    @Test
    @DisplayName("supports returns false for non-MultipartFile types")
    void supports_nonMultipartFile_returnsFalse() {
        assertThat(strategy.supports("string")).isFalse();
        assertThat(strategy.supports(123)).isFalse();
        assertThat(strategy.supports(null)).isFalse();
    }

    @Test
    @DisplayName("isApiKeyConfigured returns false when API key is empty")
    void isApiKeyConfigured_emptyKey_returnsFalse() {
        ReflectionTestUtils.setField(strategy, "openAiApiKey", "");
        assertThat(strategy.isApiKeyConfigured()).isFalse();
    }

    @Test
    @DisplayName("isApiKeyConfigured returns false when API key is null")
    void isApiKeyConfigured_nullKey_returnsFalse() {
        ReflectionTestUtils.setField(strategy, "openAiApiKey", null);
        assertThat(strategy.isApiKeyConfigured()).isFalse();
    }

    @Test
    @DisplayName("isApiKeyConfigured returns true when API key is set")
    void isApiKeyConfigured_validKey_returnsTrue() {
        ReflectionTestUtils.setField(strategy, "openAiApiKey", "sk-test-key-123");
        assertThat(strategy.isApiKeyConfigured()).isTrue();
    }

    @Test
    @DisplayName("isAvailable returns false when API key is not configured")
    void isAvailable_noApiKey_returnsFalse() {
        ReflectionTestUtils.setField(strategy, "openAiApiKey", "");
        assertThat(strategy.isAvailable()).isFalse();
    }

    @Test
    @DisplayName("process throws UnsupportedOperationException when API key is not configured")
    void process_noApiKey_throwsUnsupportedOperationException() {
        ReflectionTestUtils.setField(strategy, "openAiApiKey", "");
        MultipartFile imageFile = new MockMultipartFile(
                "image", "test.jpg", "image/jpeg", new byte[]{1, 2, 3}
        );

        assertThatThrownBy(() -> strategy.process(imageFile))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("AI Vision is not available");
    }

    @Test
    @DisplayName("process throws IllegalArgumentException for non-MultipartFile input")
    void process_nonMultipartFile_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> strategy.process("not a file"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("MultipartFile");
    }

    @Test
    @DisplayName("process throws UnsupportedOperationException even with API key (not implemented yet)")
    void process_withApiKey_throwsUnsupportedOperationException() {
        ReflectionTestUtils.setField(strategy, "openAiApiKey", "sk-test-key-123");
        MultipartFile imageFile = new MockMultipartFile(
                "image", "test.jpg", "image/jpeg", new byte[]{1, 2, 3}
        );

        assertThatThrownBy(() -> strategy.process(imageFile))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("not yet implemented");
    }
}
