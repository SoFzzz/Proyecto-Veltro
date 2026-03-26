package com.veltro.inventory.domain.purchasing.model.state;

import com.veltro.inventory.domain.purchasing.model.PurchaseOrderDetailEntity;
import com.veltro.inventory.domain.purchasing.model.PurchaseOrderEntity;

import java.util.List;

/**
 * State Pattern interface for {@link PurchaseOrderEntity} lifecycle (B2-04 | ADR-006).
 *
 * Lifecycle: PENDING → PARTIAL → RECEIVED (terminal) or VOIDED (terminal)
 *
 * <p>Each state implements valid transitions and throws {@link com.veltro.inventory.exception.InvalidStateTransitionException}
 * for illegal operations.
 *
 * <p><b>IMPORTANT:</b> This is a pure Java interface with <b>zero Spring imports</b> (domain layer restriction).
 */
public interface PurchaseOrderState {

    /**
     * Adds an item (detail) to the purchase order.
     *
     * @param order  the purchase order entity
     * @param detail the detail to add
     * @throws com.veltro.inventory.exception.InvalidStateTransitionException if adding items is not allowed in this state
     */
    void addItem(PurchaseOrderEntity order, PurchaseOrderDetailEntity detail);

    /**
     * Removes an item from the purchase order (soft delete via active=false per AC-05).
     *
     * @param order    the purchase order entity
     * @param detailId the detail ID to remove
     * @throws com.veltro.inventory.exception.InvalidStateTransitionException if removing items is not allowed in this state
     */
    void removeItem(PurchaseOrderEntity order, Long detailId);

    /**
     * Receives partial or complete merchandise delivery.
     * This method handles both partial and full reception based on received quantities.
     *
     * @param order         the purchase order entity
     * @param receivedItems list of items with their received quantities
     * @throws com.veltro.inventory.exception.InvalidStateTransitionException if receiving is not allowed in this state
     */
    void receivePartial(PurchaseOrderEntity order, List<ReceivedItem> receivedItems);

    /**
     * Voids the purchase order (transitions to VOIDED).
     *
     * @param order the purchase order entity
     * @throws com.veltro.inventory.exception.InvalidStateTransitionException if voiding is not allowed in this state
     */
    void voidOrder(PurchaseOrderEntity order);

    /**
     * Simple record to represent received items during reception.
     * This avoids dependency on application layer DTOs from domain layer.
     */
    record ReceivedItem(Long detailId, Integer receivedQuantity) {}
}