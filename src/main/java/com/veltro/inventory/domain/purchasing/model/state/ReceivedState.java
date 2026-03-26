package com.veltro.inventory.domain.purchasing.model.state;

import com.veltro.inventory.domain.purchasing.model.PurchaseOrderDetailEntity;
import com.veltro.inventory.domain.purchasing.model.PurchaseOrderEntity;
import com.veltro.inventory.exception.InvalidStateTransitionException;

import java.util.List;

/**
 * Terminal state - order has been fully received.
 * No operations are allowed in this state.
 */
public class ReceivedState implements PurchaseOrderState {

    @Override
    public void addItem(PurchaseOrderEntity order, PurchaseOrderDetailEntity detail) {
        throw new InvalidStateTransitionException(
                "Cannot add items to purchase order in RECEIVED status. Order has already been fully received."
        );
    }

    @Override
    public void removeItem(PurchaseOrderEntity order, Long detailId) {
        throw new InvalidStateTransitionException(
                "Cannot remove items from purchase order in RECEIVED status. Order has already been fully received."
        );
    }

    @Override
    public void receivePartial(PurchaseOrderEntity order, List<ReceivedItem> receivedItems) {
        throw new InvalidStateTransitionException(
                "Cannot receive more items for purchase order in RECEIVED status. Order has already been fully received."
        );
    }

    @Override
    public void voidOrder(PurchaseOrderEntity order) {
        throw new InvalidStateTransitionException(
                "Cannot void purchase order in RECEIVED status. Received orders cannot be voided."
        );
    }
}