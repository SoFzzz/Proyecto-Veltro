package com.veltro.inventory.infrastructure.adapters.web;

import com.veltro.inventory.application.purchasing.dto.AddOrderItemRequest;
import com.veltro.inventory.application.purchasing.dto.CreatePurchaseOrderRequest;
import com.veltro.inventory.application.purchasing.dto.PurchaseOrderResponse;
import com.veltro.inventory.application.purchasing.service.PurchaseOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for purchase order management (B2-04).
 *
 * <p>Role-based access control:
 * <ul>
 *   <li>GET endpoints: ADMIN, WAREHOUSE (for viewing purchase orders)</li>
 *   <li>POST/PUT/DELETE: ADMIN, WAREHOUSE (purchase operations)</li>
 * </ul>
 *
 * <p>Endpoints implement State Pattern delegation and Prototype Pattern for cloning.
 */
@RestController
@RequestMapping("/api/v1/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    // -------------------------------------------------------------------------
    // GET endpoints
    // -------------------------------------------------------------------------

    /**
     * Returns all active purchase orders.
     *
     * @return list of purchase orders
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    public ResponseEntity<List<PurchaseOrderResponse>> findAll() {
        List<PurchaseOrderResponse> orders = purchaseOrderService.findAll();
        return ResponseEntity.ok(orders);
    }

    /**
     * Returns all active purchase orders for a specific supplier.
     *
     * @param supplierId supplier ID
     * @return list of purchase orders for the supplier
     */
    @GetMapping(params = "supplierId")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    public ResponseEntity<List<PurchaseOrderResponse>> findBySupplier(@RequestParam Long supplierId) {
        List<PurchaseOrderResponse> orders = purchaseOrderService.findBySupplier(supplierId);
        return ResponseEntity.ok(orders);
    }

    /**
     * Returns a purchase order by ID.
     *
     * @param id purchase order ID
     * @return purchase order response
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    public ResponseEntity<PurchaseOrderResponse> findById(@PathVariable Long id) {
        PurchaseOrderResponse order = purchaseOrderService.findById(id);
        return ResponseEntity.ok(order);
    }

    /**
     * Returns a purchase order by order number.
     *
     * @param orderNumber order number
     * @return purchase order response
     */
    @GetMapping("/number/{orderNumber}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    public ResponseEntity<PurchaseOrderResponse> findByOrderNumber(@PathVariable String orderNumber) {
        PurchaseOrderResponse order = purchaseOrderService.findByOrderNumber(orderNumber);
        return ResponseEntity.ok(order);
    }

    // -------------------------------------------------------------------------
    // POST endpoints
    // -------------------------------------------------------------------------

    /**
     * Creates a new purchase order in PENDING status.
     *
     * @param request create purchase order request
     * @return created purchase order response
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    public ResponseEntity<PurchaseOrderResponse> create(@Valid @RequestBody CreatePurchaseOrderRequest request) {
        PurchaseOrderResponse order = purchaseOrderService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    /**
     * Adds an item to a purchase order (State Pattern delegation).
     *
     * @param orderId order ID
     * @param request item to add
     * @return updated purchase order response
     */
    @PostMapping("/{orderId}/items")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    public ResponseEntity<PurchaseOrderResponse> addItem(@PathVariable Long orderId,
                                                         @Valid @RequestBody AddOrderItemRequest request) {
        PurchaseOrderResponse order = purchaseOrderService.addItem(orderId, request);
        return ResponseEntity.ok(order);
    }

    /**
     * Creates a clone of an existing purchase order (Prototype Pattern).
     *
     * @param sourceOrderId source order ID to clone
     * @return cloned purchase order response
     */
    @PostMapping("/{sourceOrderId}/clone")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    public ResponseEntity<PurchaseOrderResponse> cloneOrder(@PathVariable Long sourceOrderId) {
        PurchaseOrderResponse clonedOrder = purchaseOrderService.cloneOrder(sourceOrderId);
        return ResponseEntity.status(HttpStatus.CREATED).body(clonedOrder);
    }

    // -------------------------------------------------------------------------
    // PUT endpoints
    // -------------------------------------------------------------------------

    /**
     * Marks an entire purchase order as received and publishes inventory events.
     *
     * @param orderId order ID
     * @return updated purchase order response
     */
    @PutMapping("/{orderId}/receive")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    public ResponseEntity<PurchaseOrderResponse> markAsReceived(@PathVariable Long orderId) {
        PurchaseOrderResponse order = purchaseOrderService.markAsReceived(orderId);
        return ResponseEntity.ok(order);
    }

    /**
     * Voids a purchase order (State Pattern delegation).
     *
     * @param orderId order ID
     * @return voided purchase order response
     */
    @PutMapping("/{orderId}/void")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    public ResponseEntity<PurchaseOrderResponse> voidOrder(@PathVariable Long orderId) {
        PurchaseOrderResponse order = purchaseOrderService.voidOrder(orderId);
        return ResponseEntity.ok(order);
    }

    // -------------------------------------------------------------------------
    // DELETE endpoints
    // -------------------------------------------------------------------------

    /**
     * Removes an item from a purchase order (soft delete - State Pattern delegation).
     *
     * @param orderId order ID
     * @param detailId item detail ID to remove
     * @return updated purchase order response
     */
    @DeleteMapping("/{orderId}/items/{detailId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    public ResponseEntity<PurchaseOrderResponse> removeItem(@PathVariable Long orderId,
                                                            @PathVariable Long detailId) {
        PurchaseOrderResponse order = purchaseOrderService.removeItem(orderId, detailId);
        return ResponseEntity.ok(order);
    }
}