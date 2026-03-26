package com.veltro.inventory.domain.pos.model;

/**
 * Payment method types for sale confirmation (B2-01).
 */
public enum PaymentMethod {
    /**
     * Cash payment - requires amountReceived to calculate change.
     */
    CASH,

    /**
     * Card payment (credit/debit).
     */
    CARD,

    /**
     * Yape mobile payment.
     */
    YAPE,

    /**
     * Plin mobile payment.
     */
    PLIN
}
