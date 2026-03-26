package com.veltro.inventory.infrastructure.adapters.web;

import com.veltro.inventory.application.scanner.dto.ProductSuggestionResponse;
import com.veltro.inventory.application.scanner.service.ScannerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * REST controller for AI-powered product scanning (B3-01).
 *
 * <p>Provides endpoints for:
 * <ul>
 *   <li>AI vision scanning - analyze product images to identify or suggest products</li>
 *   <li>Scanner status - check availability of scanning strategies</li>
 * </ul>
 *
 * <p>Note: Traditional barcode scanning is handled by the frontend using device cameras.
 * The decoded barcode is then looked up via {@code GET /api/v1/products/barcode/{barcode}}.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/scanner")
@RequiredArgsConstructor
public class ScannerController {

    private final ScannerService scannerService;

    /**
     * Analyzes a product image using AI vision.
     *
     * <p>POST /api/v1/scanner/ai
     *
     * <p>Accepts an image file and returns product suggestions based on AI analysis.
     * Returns 501 if AI vision is not configured.
     *
     * @param image the product image file (JPEG, PNG, WebP)
     * @return product suggestions with confidence scores
     */
    @PostMapping(value = "/ai", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER', 'WAREHOUSE')")
    public ResponseEntity<ProductSuggestionResponse> scanWithAi(
            @RequestParam("image") MultipartFile image
    ) {
        log.info("AI scan request received: {} ({} bytes)",
                image.getOriginalFilename(), image.getSize());

        if (image.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            ProductSuggestionResponse response = scannerService.processImage(image);
            return ResponseEntity.ok(response);
        } catch (UnsupportedOperationException e) {
            log.warn("AI Vision not available: {}", e.getMessage());
            return ResponseEntity.status(501).build();
        }
    }

    /**
     * Gets the status of all scanner strategies.
     *
     * <p>GET /api/v1/scanner/status
     *
     * @return map of strategy type to availability status
     */
    @GetMapping("/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER', 'WAREHOUSE')")
    public ResponseEntity<Map<String, Boolean>> getStatus() {
        Map<String, Boolean> status = scannerService.getStrategyStatus();
        return ResponseEntity.ok(status);
    }

    /**
     * Checks if AI vision is available.
     *
     * <p>GET /api/v1/scanner/ai/available
     *
     * @return true if AI vision is configured and ready
     */
    @GetMapping("/ai/available")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER', 'WAREHOUSE')")
    public ResponseEntity<Map<String, Boolean>> isAiAvailable() {
        boolean available = scannerService.isAiVisionAvailable();
        return ResponseEntity.ok(Map.of("available", available));
    }
}
