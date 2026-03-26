package com.veltro.inventory.application.scanner.strategy;

import com.veltro.inventory.application.scanner.dto.ProductSuggestionResponse;
import org.springframework.stereotype.Component;

/**
 * Barcode scanning strategy (B3-01).
 *
 * <p>This strategy is a no-op placeholder because barcode/QR scanning
 * is handled entirely by the frontend using device cameras. The backend
 * receives the decoded barcode string and looks up products directly
 * via the Catalog module's existing barcode search endpoint.
 *
 * <p>This class exists to complete the Strategy Pattern implementation
 * and could be extended in the future for server-side barcode image
 * processing if needed.
 *
 * @see ScannerStrategy
 */
@Component
public class BarcodeStrategy implements ScannerStrategy {

    private static final String TYPE = "BARCODE";

    /**
     * Processes a barcode string.
     *
     * <p>Note: In practice, barcode lookup is handled by the ProductController's
     * findByBarcode endpoint. This method exists for pattern completeness.
     *
     * @param input the barcode string
     * @return empty response (actual lookup uses ProductService)
     */
    @Override
    public ProductSuggestionResponse process(Object input) {
        long startTime = System.currentTimeMillis();
        // Barcode lookup is handled by ProductService.findByBarcode()
        // This strategy is a placeholder for the pattern
        return ProductSuggestionResponse.empty(TYPE, System.currentTimeMillis() - startTime);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    /**
     * Barcode strategy supports String inputs (barcode values).
     *
     * @param input the input to check
     * @return true if input is a non-null String
     */
    @Override
    public boolean supports(Object input) {
        return input instanceof String && !((String) input).isBlank();
    }
}
