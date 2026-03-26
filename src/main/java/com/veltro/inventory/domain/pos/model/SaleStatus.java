package com.veltro.inventory.domain.pos.model;

/**
 * Sale lifecycle states (B2-01).
 *
 * <p>ADR-006: State Pattern is implemented via {@link com.veltro.inventory.domain.pos.model.state.SaleState}.
 * This enum is the persistence representation of the current state.
 */
public enum SaleStatus {
    /**
     * Sale is being built (items can be added/modified/removed).
     */
    IN_PROGRESS,

    /**
     * Sale has been confirmed and stock deducted.
     */
    COMPLETED,

    /**
     * Sale has been voided (stock reverted).
     */
    VOIDED
}
