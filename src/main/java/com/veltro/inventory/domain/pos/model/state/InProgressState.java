package com.veltro.inventory.domain.pos.model.state;

import com.veltro.inventory.domain.pos.model.PaymentMethod;
import com.veltro.inventory.domain.pos.model.SaleDetailEntity;
import com.veltro.inventory.domain.pos.model.SaleEntity;
import com.veltro.inventory.domain.pos.model.SaleStatus;
import com.veltro.inventory.exception.InvalidStateTransitionException;
import com.veltro.inventory.exception.NotFoundException;

import java.time.LocalDateTime;

/**
 * IN_PROGRESS state implementation (B2-01 | ADR-006).
 *
 * <p>In this state:
 * - Items can be added/modified/removed
 * - Sale can be confirmed (→ COMPLETED)
 * - Sale CANNOT be voided (must confirm first)
 */
public class InProgressState implements SaleState {

    @Override
    public void addItem(SaleEntity sale, SaleDetailEntity detail) {
        sale.getDetails().add(detail);
        detail.setSale(sale);
    }

    @Override
    public void modifyItem(SaleEntity sale, Long detailId, Integer newQuantity) {
        SaleDetailEntity detail = sale.getDetails().stream()
                .filter(d -> d.getId().equals(detailId) && d.isActive())
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Sale detail not found with id: " + detailId));
        
        detail.setQuantity(newQuantity);
        detail.calculateSubtotal();
    }

    @Override
    public void removeItem(SaleEntity sale, Long detailId) {
        SaleDetailEntity detail = sale.getDetails().stream()
                .filter(d -> d.getId().equals(detailId) && d.isActive())
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Sale detail not found with id: " + detailId));
        
        // AC-05: Soft delete, not physical removal
        detail.setActive(false);
    }

    @Override
    public void confirm(SaleEntity sale, PaymentMethod paymentMethod) {
        sale.setStatus(SaleStatus.COMPLETED);
        sale.setPaymentMethod(paymentMethod);
        sale.setCompletedAt(LocalDateTime.now());
    }

    @Override
    public void voidSale(SaleEntity sale) {
        throw new InvalidStateTransitionException(
                String.format("Sale %s is in IN_PROGRESS status. Only completed sales can be voided.",
                        sale.getSaleNumber()));
    }
}
