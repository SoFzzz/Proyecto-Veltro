package com.veltro.inventory.domain.purchasing.model.state;

import com.veltro.inventory.domain.purchasing.model.PurchaseOrderDetailEntity;
import com.veltro.inventory.domain.purchasing.model.PurchaseOrderEntity;
import com.veltro.inventory.exception.InvalidStateTransitionException;

import java.util.List;

/**
 * Pending state - order created, awaiting merchandise.
 * Allows adding/removing items and receiving merchandise.
 */
public class PendingState implements PurchaseOrderState {

    @Override
    public void addItem(PurchaseOrderEntity order, PurchaseOrderDetailEntity detail) {
        detail.setPurchaseOrder(order);
        order.getDetails().add(detail);
        order.recalculateTotals();
    }

    @Override
    public void removeItem(PurchaseOrderEntity order, Long detailId) {
        // Soft delete by setting active = false (AC-05)
        order.getDetails().stream()
                .filter(d -> d.getId() != null && d.getId().equals(detailId))
                .findFirst()
                .ifPresent(detail -> detail.setActive(false));
        order.recalculateTotals();
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