package com.veltro.inventory.application.scanner.strategy;

import com.veltro.inventory.application.scanner.dto.ProductSuggestionResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * Strategy Pattern interface for product scanning (B3-01).
 *
 * <p>Defines a common contract for different scanning strategies:
 * <ul>
 *   <li>{@link BarcodeStrategy} - Traditional barcode/QR scanning (handled by frontend)</li>
 *   <li>{@link AiVisionStrategy} - AI-powered image recognition for unlabeled products</li>
 * </ul>
 *
 * <p>This pattern allows the system to swap scanning algorithms at runtime
 * based on the type of input (barcode string vs. product image).
 *
 * @see BarcodeStrategy
 * @see AiVisionStrategy
 */
public interface ScannerStrategy {

    /**
     * Processes the input and returns product suggestions.
     *
     * @param input the input to process (barcode string or image file)
     * @return product suggestion response
     * @throws UnsupportedOperationException if the strategy is not available
     */
    ProductSuggestionResponse process(Object input);

    /**
     * Returns the type identifier for this strategy.
     *
     * @return strategy type name
     */
    String getType();

    /**
     * Checks if this strategy can handle the given input.
     *
     * @param input the input to check
     * @return true if this strategy can process the input
     */
    boolean supports(Object input);
}
