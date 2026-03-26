package com.veltro.inventory.domain.pos.model.state;

import com.veltro.inventory.domain.pos.model.PaymentMethod;
import com.veltro.inventory.domain.pos.model.SaleDetailEntity;
import com.veltro.inventory.domain.pos.model.SaleEntity;
import com.veltro.inventory.exception.InvalidStateTransitionException;

/**
 * VOIDED state implementation (B2-01 | ADR-006).
 *
 * <p>Terminal state - no transitions allowed.
 * All operations throw {@link InvalidStateTransitionException}.
 */
public class VoidedState implements SaleState {

    @Override
    public void addItem(SaleEntity sale, SaleDetailEntity detail) {
        throw new InvalidStateTransitionException(
                String.format("Sale %s is VOIDED. No operations are allowed on a voided sale.",
                        sale.getSaleNumber()));
    }

    @Override
    public void modifyItem(SaleEntity sale, Long detailId, Integer newQuantity) {
        throw new InvalidStateTransitionException(
                String.format("Sale %s is VOIDED. No operations are allowed on a voided sale.",
                        sale.getSaleNumber()));
    }

    @Override
    public void removeItem(SaleEntity sale, Long detailId) {
        throw new InvalidStateTransitionException(
                String.format("Sale %s is VOIDED. No operations are allowed on a voided sale.",
                        sale.getSaleNumber()));
    }

    @Override
    public void confirm(SaleEntity sale, PaymentMethod paymentMethod) {
        throw new InvalidStateTransitionException(
                String.format("Sale %s is VOIDED. No operations are allowed on a voided sale.",
                        sale.getSaleNumber()));
    }

    @Override
    public void voidSale(SaleEntity sale) {
        throw new InvalidStateTransitionException(
                String.format("Sale %s is already VOIDED. Cannot void again.",
                        sale.getSaleNumber()));
    }
}
