package com.veltro.inventory.domain.pos.model.state;

import com.veltro.inventory.domain.pos.model.PaymentMethod;
import com.veltro.inventory.domain.pos.model.SaleDetailEntity;
import com.veltro.inventory.domain.pos.model.SaleEntity;
import com.veltro.inventory.domain.pos.model.SaleStatus;
import com.veltro.inventory.exception.InvalidStateTransitionException;

/**
 * COMPLETED state implementation (B2-01 | ADR-006).
 *
 * <p>In this state:
 * - Items CANNOT be added/modified/removed (sale is final)
 * - Sale CANNOT be confirmed again
 * - Sale CAN be voided (→ VOIDED)
 */
public class CompletedState implements SaleState {

    @Override
    public void addItem(SaleEntity sale, SaleDetailEntity detail) {
        throw new InvalidStateTransitionException(
                String.format("Sale %s is already COMPLETED. Cannot add items to a completed sale.",
                        sale.getSaleNumber()));
    }

    @Override
    public void modifyItem(SaleEntity sale, Long detailId, Integer newQuantity) {
        throw new InvalidStateTransitionException(
                String.format("Sale %s is already COMPLETED. Cannot modify items in a completed sale.",
                        sale.getSaleNumber()));
    }

    @Override
    public void removeItem(SaleEntity sale, Long detailId) {
        throw new InvalidStateTransitionException(
                String.format("Sale %s is already COMPLETED. Cannot remove items from a completed sale.",
                        sale.getSaleNumber()));
    }

    @Override
    public void confirm(SaleEntity sale, PaymentMethod paymentMethod) {
        throw new InvalidStateTransitionException(
                String.format("Sale %s is already in COMPLETED status. It cannot be confirmed again.",
                        sale.getSaleNumber()));
    }

    @Override
    public void voidSale(SaleEntity sale) {
        sale.setStatus(SaleStatus.VOIDED);
    }
}
