package com.veltro.inventory.domain.purchasing.model.state;

import com.veltro.inventory.domain.purchasing.model.PurchaseOrderDetailEntity;
import com.veltro.inventory.domain.purchasing.model.PurchaseOrderEntity;
import com.veltro.inventory.domain.purchasing.model.PurchaseOrderStatus;
import com.veltro.inventory.domain.purchasing.model.SupplierEntity;
import com.veltro.inventory.exception.InvalidStateTransitionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link VoidedState} (B2-04 - State Pattern).
 *
 * <p>Tests that VOIDED state is terminal - no operations are allowed.
 */
class VoidedStateTest {

    private VoidedState state;
    private PurchaseOrderEntity order;

    @BeforeEach
    void setUp() {
        state = new VoidedState();
        
        SupplierEntity supplier = new SupplierEntity();
        supplier.setId(10L);
        supplier.setTaxId("12345678901");
        supplier.setCompanyName("Test Supplier");
        
        order = new PurchaseOrderEntity();
        order.setId(1L);
        order.setOrderNumber("PO-2026-000004");
        order.setStatus(PurchaseOrderStatus.VOIDED);
        order.setSupplier(supplier);
    }

    @Test
    @DisplayName("addItem throws InvalidStateTransitionException")
    void addItem_voidedState_throwsInvalidStateTransition() {
        PurchaseOrderDetailEntity detail = new PurchaseOrderDetailEntity();

        assertThatThrownBy(() -> state.addItem(order, detail))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("Cannot add items to purchase order in VOIDED status")
                .hasMessageContaining("Order has been cancelled");
    }

    @Test
    @DisplayName("removeItem throws InvalidStateTransitionException")
    void removeItem_voidedState_throwsInvalidStateTransition() {
        assertThatThrownBy(() -> state.removeItem(order, 1L))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("Cannot remove items from purchase order in VOIDED status")
                .hasMessageContaining("Order has been cancelled");
    }

    @Test
    @DisplayName("receivePartial throws InvalidStateTransitionException")
    void receivePartial_voidedState_throwsInvalidStateTransition() {
        List<PurchaseOrderState.ReceivedItem> receivedItems = List.of(
                new PurchaseOrderState.ReceivedItem(1L, 5)
        );

        assertThatThrownBy(() -> state.receivePartial(order, receivedItems))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("Cannot receive items for purchase order in VOIDED status")
                .hasMessageContaining("Order has been cancelled");
    }

    @Test
    @DisplayName("voidOrder throws InvalidStateTransitionException")
    void voidOrder_voidedState_throwsInvalidStateTransition() {
        assertThatThrownBy(() -> state.voidOrder(order))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("Purchase order is already in VOIDED status");
    }

    @Test
    @DisplayName("receivePartial with empty list throws InvalidStateTransitionException")
    void receivePartial_emptyList_throwsInvalidStateTransition() {
        List<PurchaseOrderState.ReceivedItem> emptyList = List.of();

        assertThatThrownBy(() -> state.receivePartial(order, emptyList))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("Cannot receive items for purchase order in VOIDED status")
                .hasMessageContaining("Order has been cancelled");
    }
}