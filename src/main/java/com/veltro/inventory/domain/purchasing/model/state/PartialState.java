package com.veltro.inventory.domain.purchasing.model.state;

import com.veltro.inventory.domain.purchasing.model.PurchaseOrderDetailEntity;
import com.veltro.inventory.domain.purchasing.model.PurchaseOrderEntity;
import com.veltro.inventory.exception.InvalidStateTransitionException;

import java.util.List;

/**
 * Partial state - some items received, others pending.
 * Only allows receiving more merchandise or voiding.
 */
public class PartialState implements PurchaseOrderState {

    @Override
    public void addItem(PurchaseOrderEntity order, PurchaseOrderDetailEntity detail) {
        throw new InvalidStateTransitionException(
                "Cannot add items to purchase order in PARTIAL status. " +
                "Order has already received some merchandise."
        );
    }

    @Override
    public void removeItem(PurchaseOrderEntity order, Long detailId) {
        throw new InvalidStateTransitionException(
                "Cannot remove items from purchase order in PARTIAL status. " +
                "Order has already received some merchandise."
        );
    }

    @Override
    public void receivePartial(PurchaseOrderEntity order, List<ReceivedItem> receivedItems) {
        // TODO: Implement logic in subsequent step - this requires more complex business logic
        // For now, throw to indicate not implemented
        throw new InvalidStateTransitionException("receivePartial not yet implemented");
    }

    @Override
    public void voidOrder(PurchaseOrderEntity order) {
        // Transition to VOIDED
        order.setStatus(com.veltro.inventory.domain.purchasing.model.PurchaseOrderStatus.VOIDED);
    }
}