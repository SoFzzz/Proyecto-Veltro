package com.veltro.inventory.domain.pos.model.state;

import com.veltro.inventory.domain.pos.model.PaymentMethod;
import com.veltro.inventory.domain.pos.model.SaleDetailEntity;
import com.veltro.inventory.domain.pos.model.SaleEntity;

/**
 * State Pattern interface for {@link SaleEntity} lifecycle (B2-01 | ADR-006).
 *
 * <p>Each state implements valid transitions and throws {@link com.veltro.inventory.exception.InvalidStateTransitionException}
 * for illegal operations.
 *
 * <p><b>IMPORTANT:</b> This is a pure Java interface with <b>zero Spring imports</b> (domain layer restriction).
 */
public interface SaleState {

    /**
     * Adds an item (detail) to the sale.
     *
     * @param sale   the sale entity
     * @param detail the detail to add
     * @throws com.veltro.inventory.exception.InvalidStateTransitionException if adding items is not allowed in this state
     */
    void addItem(SaleEntity sale, SaleDetailEntity detail);

    /**
     * Modifies the quantity of an existing item.
     *
     * @param sale        the sale entity
     * @param detailId    the detail ID to modify
     * @param newQuantity the new quantity (must be > 0)
     * @throws com.veltro.inventory.exception.InvalidStateTransitionException if modifying items is not allowed in this state
     */
    void modifyItem(SaleEntity sale, Long detailId, Integer newQuantity);

    /**
     * Removes an item from the sale (soft delete via active=false per AC-05).
     *
     * @param sale     the sale entity
     * @param detailId the detail ID to remove
     * @throws com.veltro.inventory.exception.InvalidStateTransitionException if removing items is not allowed in this state
     */
    void removeItem(SaleEntity sale, Long detailId);

    /**
     * Confirms the sale (transitions to COMPLETED).
     *
     * @param sale          the sale entity
     * @param paymentMethod the payment method used
     * @throws com.veltro.inventory.exception.InvalidStateTransitionException if confirmation is not allowed in this state
     */
    void confirm(SaleEntity sale, PaymentMethod paymentMethod);

    /**
     * Voids the sale (transitions to VOIDED).
     *
     * @param sale the sale entity
     * @throws com.veltro.inventory.exception.InvalidStateTransitionException if voiding is not allowed in this state
     */
    void voidSale(SaleEntity sale);
}
