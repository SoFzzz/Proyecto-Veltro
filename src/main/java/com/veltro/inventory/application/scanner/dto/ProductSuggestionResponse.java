package com.veltro.inventory.application.scanner.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO for AI-powered product suggestions (B3-01).
 *
 * <p>Contains a list of suggested products based on image analysis,
 * each with a confidence score indicating the likelihood of a match.
 *
 * @param suggestions list of product matches with confidence scores
 * @param processingTimeMs time taken to process the image in milliseconds
 * @param strategyUsed identifier of the strategy that produced these results
 */
public record ProductSuggestionResponse(
        List<SuggestedProduct> suggestions,
        long processingTimeMs,
        String strategyUsed
) {
    /**
     * A suggested product match from AI analysis.
     *
     * @param productId existing product ID (null if new product suggested)
     * @param productName product name
     * @param confidence confidence score (0.0 to 1.0)
     * @param suggestedPrice AI-suggested price (for new products)
     * @param barcode barcode if detected
     */
    public record SuggestedProduct(
            Long productId,
            String productName,
            double confidence,
            BigDecimal suggestedPrice,
            String barcode
    ) {}

    /**
     * Creates an empty response for strategies that return no results.
     *
     * @param strategyUsed the strategy identifier
     * @param processingTimeMs processing time
     * @return empty response
     */
    public static ProductSuggestionResponse empty(String strategyUsed, long processingTimeMs) {
        return new ProductSuggestionResponse(List.of(), processingTimeMs, strategyUsed);
    }
}
