package com.veltro.inventory.domain.purchasing.model;

/**
 * Purchase order status enumeration for B2-04.
 *
 * Lifecycle: PENDING → PARTIAL → RECEIVED (terminal) or VOIDED (terminal)
 *
 * As per VeltroBase.md specification and project corrections.
 */
public enum PurchaseOrderStatus {

    /**
     * Order created, awaiting merchandise delivery.
     * Initial state when purchase order is created.
     */
    PENDING,

    /**
     * Some items have been received, others are still pending.
     * Transition from PENDING when partial delivery occurs.
     */
    PARTIAL,

    /**
     * All items have been fully received.
     * Terminal state - no further transitions allowed.
     */
    RECEIVED,

    /**
     * Order has been cancelled/voided.
     * Terminal state - no further transitions allowed.
     */
    VOIDED
}