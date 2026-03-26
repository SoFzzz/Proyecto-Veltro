package com.veltro.inventory.domain.purchasing.model.state;

import com.veltro.inventory.domain.purchasing.model.PurchaseOrderDetailEntity;
import com.veltro.inventory.domain.purchasing.model.PurchaseOrderEntity;
import com.veltro.inventory.exception.InvalidStateTransitionException;

import java.util.List;

/**
 * Terminal state - order has been voided/cancelled.
 * No operations are allowed in this state.
 */
public class VoidedState implements PurchaseOrderState {

    @Override
    public void addItem(PurchaseOrderEntity order, PurchaseOrderDetailEntity detail) {
        throw new InvalidStateTransitionException(
                "Cannot add items to purchase order in VOIDED status. Order has been cancelled."
        );
    }

    @Override
    public void removeItem(PurchaseOrderEntity order, Long detailId) {
        throw new InvalidStateTransitionException(
                "Cannot remove items from purchase order in VOIDED status. Order has been cancelled."
        );
    }

    @Override
    public void receivePartial(PurchaseOrderEntity order, List<ReceivedItem> receivedItems) {
        throw new InvalidStateTransitionException(
                "Cannot receive items for purchase order in VOIDED status. Order has been cancelled."
        );
    }

    @Override
    public void voidOrder(PurchaseOrderEntity order) {
        throw new InvalidStateTransitionException(
                "Purchase order is already in VOIDED status."
        );
    }
}