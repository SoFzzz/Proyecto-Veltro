package com.veltro.inventory.application.scanner.service;

import com.veltro.inventory.application.scanner.dto.ProductSuggestionResponse;
import com.veltro.inventory.application.scanner.strategy.AiVisionStrategy;
import com.veltro.inventory.application.scanner.strategy.ScannerStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Application service for product scanning operations (B3-01).
 *
 * <p>Coordinates scanner strategies (barcode, AI vision) and provides
 * a unified interface for product identification. Uses the Strategy Pattern
 * to delegate to the appropriate scanner implementation based on input type.
 *
 * @see ScannerStrategy
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScannerService {

    private final List<ScannerStrategy> strategies;
    private final AiVisionStrategy aiVisionStrategy;

    /**
     * Processes an image using AI vision to identify products.
     *
     * @param image the product image file
     * @return product suggestions based on AI analysis
     * @throws UnsupportedOperationException if AI vision is not configured
     */
    public ProductSuggestionResponse processImage(MultipartFile image) {
        log.info("Processing image scan: {} ({} bytes)", image.getOriginalFilename(), image.getSize());
        return aiVisionStrategy.process(image);
    }

    /**
     * Checks if AI vision scanning is available.
     *
     * @return true if OpenAI API key is configured
     */
    public boolean isAiVisionAvailable() {
        return aiVisionStrategy.isAvailable();
    }

    /**
     * Gets the status of all scanner strategies.
     *
     * @return map of strategy type to availability status
     */
    public Map<String, Boolean> getStrategyStatus() {
        return strategies.stream()
                .collect(Collectors.toMap(
                        ScannerStrategy::getType,
                        strategy -> {
                            if (strategy instanceof AiVisionStrategy aiStrategy) {
                                return aiStrategy.isAvailable();
                            }
                            return true; // Other strategies are always available
                        }
                ));
    }

    /**
     * Finds a strategy that supports the given input.
     *
     * @param input the input to process
     * @return the appropriate strategy, or null if none found
     */
    public ScannerStrategy findStrategy(Object input) {
        return strategies.stream()
                .filter(strategy -> strategy.supports(input))
                .findFirst()
                .orElse(null);
    }
}
