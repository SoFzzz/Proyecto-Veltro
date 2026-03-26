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
 * Unit tests for {@link ReceivedState} (B2-04 - State Pattern).
 *
 * <p>Tests that RECEIVED state is terminal - no operations are allowed.
 */
class ReceivedStateTest {

    private ReceivedState state;
    private PurchaseOrderEntity order;

    @BeforeEach
    void setUp() {
        state = new ReceivedState();
        
        SupplierEntity supplier = new SupplierEntity();
        supplier.setId(10L);
        supplier.setTaxId("12345678901");
        supplier.setCompanyName("Test Supplier");
        
        order = new PurchaseOrderEntity();
        order.setId(1L);
        order.setOrderNumber("PO-2026-000003");
        order.setStatus(PurchaseOrderStatus.RECEIVED);
        order.setSupplier(supplier);
    }

    @Test
    @DisplayName("addItem throws InvalidStateTransitionException")
    void addItem_receivedState_throwsInvalidStateTransition() {
        PurchaseOrderDetailEntity detail = new PurchaseOrderDetailEntity();

        assertThatThrownBy(() -> state.addItem(order, detail))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("Cannot add items to purchase order in RECEIVED status")
                .hasMessageContaining("already been fully received");
    }

    @Test
    @DisplayName("removeItem throws InvalidStateTransitionException")
    void removeItem_receivedState_throwsInvalidStateTransition() {
        assertThatThrownBy(() -> state.removeItem(order, 1L))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("Cannot remove items from purchase order in RECEIVED status")
                .hasMessageContaining("already been fully received");
    }

    @Test
    @DisplayName("receivePartial throws InvalidStateTransitionException")
    void receivePartial_receivedState_throwsInvalidStateTransition() {
        List<PurchaseOrderState.ReceivedItem> receivedItems = List.of(
                new PurchaseOrderState.ReceivedItem(1L, 5)
        );

        assertThatThrownBy(() -> state.receivePartial(order, receivedItems))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("Cannot receive more items for purchase order in RECEIVED status")
                .hasMessageContaining("already been fully received");
    }

    @Test
    @DisplayName("voidOrder throws InvalidStateTransitionException")
    void voidOrder_receivedState_throwsInvalidStateTransition() {
        assertThatThrownBy(() -> state.voidOrder(order))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("Cannot void purchase order in RECEIVED status")
                .hasMessageContaining("Received orders cannot be voided");
    }

    @Test
    @DisplayName("receivePartial with empty list throws InvalidStateTransitionException")
    void receivePartial_emptyList_throwsInvalidStateTransition() {
        List<PurchaseOrderState.ReceivedItem> emptyList = List.of();

        assertThatThrownBy(() -> state.receivePartial(order, emptyList))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("Cannot receive more items for purchase order in RECEIVED status")
                .hasMessageContaining("already been fully received");
    }
}