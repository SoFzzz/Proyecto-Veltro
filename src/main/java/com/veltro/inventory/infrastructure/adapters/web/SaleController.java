package com.veltro.inventory.infrastructure.adapters.web;

import com.veltro.inventory.application.pos.dto.AddItemRequest;
import com.veltro.inventory.application.pos.dto.ConfirmSaleRequest;
import com.veltro.inventory.application.pos.dto.ModifyItemRequest;
import com.veltro.inventory.application.pos.dto.SaleResponse;
import com.veltro.inventory.application.pos.service.SaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for sale (POS) management (B2-01).
 *
 * <p>Role rules:
 * <ul>
 *   <li>Most endpoints: CASHIER role required</li>
 *   <li>POST /api/v1/sales/{id}/void: ADMIN role only</li>
 * </ul>
 *
 * <p>All mutations publish domain events for downstream listeners (B2-02).
 */
@RestController
@RequestMapping("/api/v1/sales")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService saleService;

    // -------------------------------------------------------------------------
    // POST /api/v1/sales/start — Start new sale (CASHIER)
    // -------------------------------------------------------------------------

    /**
     * Starts a new sale in IN_PROGRESS status.
     *
     * @return 201 CREATED with the new sale
     */
    @PostMapping("/start")
    @PreAuthorize("hasRole('CASHIER')")
    public ResponseEntity<SaleResponse> startSale() {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(saleService.startSale());
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/sales/{id} — Get sale by ID (CASHIER)
    // -------------------------------------------------------------------------

    /**
     * Retrieves a sale by ID.
     *
     * @param id the sale ID
     * @return 200 OK with the sale
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CASHIER')")
    public ResponseEntity<SaleResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(saleService.findById(id));
    }

    // -------------------------------------------------------------------------
    // POST /api/v1/sales/{id}/items — Add item to sale (CASHIER)
    // -------------------------------------------------------------------------

    /**
     * Adds an item to the sale cart.
     *
     * @param id      the sale ID
     * @param request the item to add (productId, quantity)
     * @return 200 OK with the updated sale
     */
    @PostMapping("/{id}/items")
    @PreAuthorize("hasRole('CASHIER')")
    public ResponseEntity<SaleResponse> addItem(
            @PathVariable Long id,
            @Valid @RequestBody AddItemRequest request) {
        return ResponseEntity.ok(saleService.addItem(id, request));
    }

    // -------------------------------------------------------------------------
    // PUT /api/v1/sales/{id}/items/{itemId} — Modify item quantity (CASHIER)
    // -------------------------------------------------------------------------

    /**
     * Modifies the quantity of an existing item in the sale.
     *
     * @param id      the sale ID
     * @param itemId  the detail ID to modify
     * @param request the new quantity
     * @return 200 OK with the updated sale
     */
    @PutMapping("/{id}/items/{itemId}")
    @PreAuthorize("hasRole('CASHIER')")
    public ResponseEntity<SaleResponse> modifyItem(
            @PathVariable Long id,
            @PathVariable Long itemId,
            @Valid @RequestBody ModifyItemRequest request) {
        return ResponseEntity.ok(saleService.modifyItem(id, itemId, request));
    }

    // -------------------------------------------------------------------------
    // DELETE /api/v1/sales/{id}/items/{itemId} — Remove item (CASHIER)
    // -------------------------------------------------------------------------

    /**
     * Removes an item from the sale (soft delete via active=false per AC-05).
     *
     * @param id     the sale ID
     * @param itemId the detail ID to remove
     * @return 200 OK with the updated sale
     */
    @DeleteMapping("/{id}/items/{itemId}")
    @PreAuthorize("hasRole('CASHIER')")
    public ResponseEntity<SaleResponse> removeItem(
            @PathVariable Long id,
            @PathVariable Long itemId) {
        return ResponseEntity.ok(saleService.removeItem(id, itemId));
    }

    // -------------------------------------------------------------------------
    // POST /api/v1/sales/{id}/confirm — Confirm sale (CASHIER)
    // -------------------------------------------------------------------------

    /**
     * Confirms the sale (transitions to COMPLETED).
     *
     * <p>For CASH payments, {@code amountReceived} must be provided and >= total.
     * Publishes {@link com.veltro.inventory.application.pos.event.SaleCompletedEvent}
     * for downstream listeners (B2-02) to handle inventory deduction.
     *
     * @param id      the sale ID
     * @param request payment method and amount received
     * @return 200 OK with the confirmed sale
     */
    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasRole('CASHIER')")
    public ResponseEntity<SaleResponse> confirm(
            @PathVariable Long id,
            @Valid @RequestBody ConfirmSaleRequest request) {
        return ResponseEntity.ok(saleService.confirm(id, request));
    }

    // -------------------------------------------------------------------------
    // POST /api/v1/sales/{id}/void — Void completed sale (ADMIN only)
    // -------------------------------------------------------------------------

    /**
     * Voids a completed sale (transitions to VOIDED).
     *
     * <p>Only COMPLETED sales can be voided (enforced by State Pattern).
     * Publishes {@link com.veltro.inventory.application.pos.event.SaleVoidedEvent}
     * for downstream listeners (B2-02) to handle stock reversal.
     *
     * @param id the sale ID
     * @return 200 OK with the voided sale
     */
    @PostMapping("/{id}/void")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SaleResponse> voidSale(@PathVariable Long id) {
        return ResponseEntity.ok(saleService.voidSale(id));
    }
}
