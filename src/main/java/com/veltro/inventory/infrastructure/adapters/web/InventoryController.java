package com.veltro.inventory.infrastructure.adapters.web;

import com.veltro.inventory.application.inventory.dto.InventoryMovementResponse;
import com.veltro.inventory.application.inventory.dto.InventoryResponse;
import com.veltro.inventory.application.inventory.dto.StockAdjustmentRequest;
import com.veltro.inventory.application.inventory.dto.StockEntryRequest;
import com.veltro.inventory.application.inventory.dto.StockExitRequest;
import com.veltro.inventory.application.inventory.dto.UpdateStockLimitsRequest;
import com.veltro.inventory.application.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for inventory management (B1-04).
 *
 * Role rules (RF-12):
 * <ul>
 *   <li>GET endpoints: ADMIN, WAREHOUSE, CASHIER.</li>
 *   <li>POST / PUT (mutations): ADMIN or WAREHOUSE only.</li>
 * </ul>
 *
 * All endpoints are keyed on {@code productId} — the natural key for inventory
 * from the caller's perspective (1-to-1 with products).
 *
 * AC-04: {@code POST /exit} will return HTTP 422 with error code
 * "INSUFFICIENT_STOCK" if the requested quantity exceeds the current stock.
 *
 * AC-07: {@code GET /movements} returns a {@link Page} with pagination metadata.
 */
@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    // -------------------------------------------------------------------------
    // GET endpoints — readable by all authenticated roles
    // -------------------------------------------------------------------------

    /**
     * Returns the current stock record for the given product.
     * Returns 404 when the product has no inventory row.
     */
    @GetMapping("/{productId}")
    public ResponseEntity<InventoryResponse> getByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(inventoryService.findByProductId(productId));
    }

    /**
     * Paginated movement history for the given product's inventory (AC-07).
     * Default: page=0, size=20, newest-first.
     */
    @GetMapping("/{productId}/movements")
    public ResponseEntity<Page<InventoryMovementResponse>> getMovements(
            @PathVariable Long productId,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(inventoryService.getMovements(productId, pageable));
    }

    // -------------------------------------------------------------------------
    // POST / PUT endpoints — ADMIN or WAREHOUSE only
    // -------------------------------------------------------------------------

    /**
     * Records a stock entry (goods received, manual addition).
     * Increases {@code currentStock} by the given quantity.
     */
    @PostMapping("/{productId}/entry")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    public ResponseEntity<InventoryResponse> recordEntry(
            @PathVariable Long productId,
            @Valid @RequestBody StockEntryRequest request) {
        return ResponseEntity.ok(inventoryService.recordEntry(productId, request));
    }

    /**
     * Records a stock exit (shrinkage, manual removal).
     * Returns HTTP 422 with "INSUFFICIENT_STOCK" if quantity > currentStock (AC-04).
     */
    @PostMapping("/{productId}/exit")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    public ResponseEntity<InventoryResponse> recordExit(
            @PathVariable Long productId,
            @Valid @RequestBody StockExitRequest request) {
        return ResponseEntity.ok(inventoryService.recordExit(productId, request));
    }

    /**
     * Sets stock to a specific absolute value (physical count correction).
     * A non-blank reason is required to maintain an auditable trail.
     */
    @PostMapping("/{productId}/adjustment")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    public ResponseEntity<InventoryResponse> recordAdjustment(
            @PathVariable Long productId,
            @Valid @RequestBody StockAdjustmentRequest request) {
        return ResponseEntity.ok(inventoryService.recordAdjustment(productId, request));
    }

    /**
     * Updates the min/max stock alert thresholds for the given product.
     */
    @PutMapping("/{productId}/limits")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    public ResponseEntity<InventoryResponse> updateLimits(
            @PathVariable Long productId,
            @Valid @RequestBody UpdateStockLimitsRequest request) {
        return ResponseEntity.ok(inventoryService.updateLimits(productId, request));
    }
}
