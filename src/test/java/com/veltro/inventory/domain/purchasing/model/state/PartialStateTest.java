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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link PartialState} (B2-04 - State Pattern).
 *
 * <p>Tests that PARTIAL state only allows voiding and has placeholder for receivePartial.
 * Adding/removing items is not allowed.
 */
class PartialStateTest {

    private PartialState state;
    private PurchaseOrderEntity order;

    @BeforeEach
    void setUp() {
        state = new PartialState();
        
        SupplierEntity supplier = new SupplierEntity();
        supplier.setId(10L);
        supplier.setTaxId("12345678901");
        supplier.setCompanyName("Test Supplier");
        
        order = new PurchaseOrderEntity();
        order.setId(1L);
        order.setOrderNumber("PO-2026-000002");
        order.setStatus(PurchaseOrderStatus.PARTIAL);
        order.setSupplier(supplier);
    }

    @Test
    @DisplayName("addItem throws InvalidStateTransitionException")
    void addItem_partialState_throwsInvalidStateTransition() {
        PurchaseOrderDetailEntity detail = new PurchaseOrderDetailEntity();

        assertThatThrownBy(() -> state.addItem(order, detail))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("Cannot add items to purchase order in PARTIAL status");
    }

    @Test
    @DisplayName("removeItem throws InvalidStateTransitionException")
    void removeItem_partialState_throwsInvalidStateTransition() {
        assertThatThrownBy(() -> state.removeItem(order, 1L))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("Cannot remove items from purchase order in PARTIAL status");
    }

    @Test
    @DisplayName("voidOrder transitions to VOIDED status")
    void voidOrder_partialState_transitionsToVoided() {
        state.voidOrder(order);

        assertThat(order.getStatus()).isEqualTo(PurchaseOrderStatus.VOIDED);
    }

    @Test
    @DisplayName("receivePartial throws InvalidStateTransitionException (placeholder)")
    void receivePartial_partialState_throwsInvalidStateTransition() {
        List<PurchaseOrderState.ReceivedItem> receivedItems = List.of(
                new PurchaseOrderState.ReceivedItem(1L, 3)
        );

        assertThatThrownBy(() -> state.receivePartial(order, receivedItems))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("receivePartial not yet implemented");
    }

    @Test
    @DisplayName("receivePartial with empty list throws InvalidStateTransitionException")
    void receivePartial_emptyList_throwsInvalidStateTransition() {
        List<PurchaseOrderState.ReceivedItem> emptyList = List.of();

        assertThatThrownBy(() -> state.receivePartial(order, emptyList))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("receivePartial not yet implemented");
    }
}