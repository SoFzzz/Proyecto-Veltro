package com.veltro.inventory.application.scanner.strategy;

import com.veltro.inventory.application.scanner.dto.ProductSuggestionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * AI-powered vision scanning strategy (B3-01).
 *
 * <p>This strategy uses AI (OpenAI Vision API) to analyze product images
 * and suggest matching products from the catalog, or propose new product
 * data for unrecognized items.
 *
 * <p><strong>Current Status:</strong> Placeholder implementation that throws
 * {@link UnsupportedOperationException} until an OpenAI API key is configured.
 * Once configured, this will integrate with the OpenAI Vision API to:
 * <ul>
 *   <li>Analyze uploaded product images</li>
 *   <li>Match against existing catalog products</li>
 *   <li>Suggest product names and prices for new items</li>
 *   <li>Detect barcodes in images if present</li>
 * </ul>
 *
 * <p>To enable AI vision:
 * <ol>
 *   <li>Obtain an OpenAI API key with Vision API access</li>
 *   <li>Configure {@code veltro.ai.openai.api-key} in application properties</li>
 *   <li>This strategy will automatically become available</li>
 * </ol>
 *
 * @see ScannerStrategy
 */
@Slf4j
@Component
public class AiVisionStrategy implements ScannerStrategy {

    private static final String TYPE = "AI_VISION";

    @Value("${veltro.ai.openai.api-key:}")
    private String openAiApiKey;

    /**
     * Processes an image file using AI vision.
     *
     * <p>Currently throws {@link UnsupportedOperationException} as the
     * OpenAI API integration is not yet implemented.
     *
     * @param input the image file (MultipartFile)
     * @return product suggestions based on image analysis
     * @throws UnsupportedOperationException if API key is not configured
     * @throws IllegalArgumentException if input is not a MultipartFile
     */
    @Override
    public ProductSuggestionResponse process(Object input) {
        if (!(input instanceof MultipartFile)) {
            throw new IllegalArgumentException("AiVisionStrategy requires a MultipartFile input");
        }

        if (!isApiKeyConfigured()) {
            log.warn("AI Vision requested but OpenAI API key is not configured");
            throw new UnsupportedOperationException(
                    "AI Vision is not available. Configure 'veltro.ai.openai.api-key' to enable this feature."
            );
        }

        MultipartFile image = (MultipartFile) input;
        log.info("Processing image: {} ({} bytes)", image.getOriginalFilename(), image.getSize());

        // TODO: Implement OpenAI Vision API integration
        // 1. Validate image format (JPEG, PNG, WebP)
        // 2. Resize/compress if needed (OpenAI has size limits)
        // 3. Call OpenAI Vision API with structured prompt
        // 4. Parse response into ProductSuggestionResponse
        // 5. Optionally match against existing catalog products

        throw new UnsupportedOperationException(
                "AI Vision processing is not yet implemented. OpenAI Vision API integration pending."
        );
    }

    @Override
    public String getType() {
        return TYPE;
    }

    /**
     * AI Vision strategy supports MultipartFile inputs (image files).
     *
     * @param input the input to check
     * @return true if input is a non-empty MultipartFile
     */
    @Override
    public boolean supports(Object input) {
        if (!(input instanceof MultipartFile file)) {
            return false;
        }
        return !file.isEmpty() && isImageContentType(file.getContentType());
    }

    /**
     * Checks if the OpenAI API key is configured.
     *
     * @return true if API key is present and non-blank
     */
    public boolean isApiKeyConfigured() {
        return openAiApiKey != null && !openAiApiKey.isBlank();
    }

    /**
     * Checks if this strategy is available for use.
     *
     * @return true if API key is configured
     */
    public boolean isAvailable() {
        return isApiKeyConfigured();
    }

    private boolean isImageContentType(String contentType) {
        if (contentType == null) {
            return false;
        }
        return contentType.startsWith("image/");
    }
}
