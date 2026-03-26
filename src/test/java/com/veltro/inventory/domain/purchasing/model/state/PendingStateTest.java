package com.veltro.inventory.domain.purchasing.model.state;

import com.veltro.inventory.domain.purchasing.model.PurchaseOrderDetailEntity;
import com.veltro.inventory.domain.purchasing.model.PurchaseOrderEntity;
import com.veltro.inventory.domain.purchasing.model.PurchaseOrderStatus;
import com.veltro.inventory.domain.purchasing.model.SupplierEntity;
import com.veltro.inventory.domain.catalog.model.ProductEntity;
import com.veltro.inventory.exception.InvalidStateTransitionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link PendingState} (B2-04 - State Pattern).
 *
 * <p>Tests that PENDING state allows item operations and voiding, but has placeholder for receivePartial.
 */
class PendingStateTest {

    private PendingState state;
    private PurchaseOrderEntity order;
    private SupplierEntity supplier;
    private ProductEntity product;

    @BeforeEach
    void setUp() {
        state = new PendingState();
        
        // Create test supplier
        supplier = new SupplierEntity();
        supplier.setId(10L);
        supplier.setTaxId("12345678901");
        supplier.setCompanyName("Test Supplier");
        
        // Create test product
        product = new ProductEntity();
        product.setId(100L);
        product.setName("Widget");
        
        // Create test order
        order = new PurchaseOrderEntity();
        order.setId(1L);
        order.setOrderNumber("PO-2026-000001");
        order.setStatus(PurchaseOrderStatus.PENDING);
        order.setSupplier(supplier);
    }

    @Test
    @DisplayName("addItem adds detail to purchase order")
    void addItem_validDetail_addsToOrder() {
        PurchaseOrderDetailEntity detail = createDetail(product, 10, new BigDecimal("25.5000"));

        state.addItem(order, detail);

        assertThat(order.getDetails()).hasSize(1);
        assertThat(order.getDetails().get(0)).isEqualTo(detail);
        assertThat(detail.getPurchaseOrder()).isEqualTo(order);
    }

    @Test
    @DisplayName("addItem recalculates order totals")
    void addItem_validDetail_recalculatesTotals() {
        PurchaseOrderDetailEntity detail = createDetail(product, 5, new BigDecimal("10.0000"));

        state.addItem(order, detail);

        // Total should be 5 * 10.0000 = 50.0000
        assertThat(order.getTotal()).isEqualByComparingTo(new BigDecimal("50.0000"));
    }

    @Test
    @DisplayName("removeItem sets detail to inactive")
    void removeItem_validItemId_softDeletes() {
        PurchaseOrderDetailEntity detail = createDetail(product, 3, new BigDecimal("15.0000"));
        detail.setId(5L);
        order.getDetails().add(detail);

        state.removeItem(order, 5L);

        assertThat(detail.isActive()).isFalse();
    }

    @Test
    @DisplayName("removeItem recalculates order totals")
    void removeItem_validItemId_recalculatesTotals() {
        ProductEntity product2 = new ProductEntity();
        product2.setId(200L);
        product2.setName("Widget B");
        
        PurchaseOrderDetailEntity detail1 = createDetail(product, 2, new BigDecimal("10.0000"));
        detail1.setId(5L);
        PurchaseOrderDetailEntity detail2 = createDetail(product2, 3, new BigDecimal("20.0000"));
        detail2.setId(6L);
        order.getDetails().add(detail1);
        order.getDetails().add(detail2);
        order.recalculateTotals(); // Initial total: (2*10) + (3*20) = 80

        state.removeItem(order, 5L);

        // After removing detail1, total should be 3*20 = 60.0000
        assertThat(order.getTotal()).isEqualByComparingTo(new BigDecimal("60.0000"));
    }

    @Test
    @DisplayName("voidOrder transitions to VOIDED status")
    void voidOrder_pendingState_transitionsToVoided() {
        state.voidOrder(order);

        assertThat(order.getStatus()).isEqualTo(PurchaseOrderStatus.VOIDED);
    }

    @Test
    @DisplayName("receivePartial throws InvalidStateTransitionException (placeholder)")
    void receivePartial_pendingState_throwsInvalidStateTransition() {
        List<PurchaseOrderState.ReceivedItem> receivedItems = List.of(
                new PurchaseOrderState.ReceivedItem(1L, 5)
        );

        assertThatThrownBy(() -> state.receivePartial(order, receivedItems))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("receivePartial not yet implemented");
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private PurchaseOrderDetailEntity createDetail(ProductEntity product, int quantity, BigDecimal unitCost) {
        PurchaseOrderDetailEntity detail = new PurchaseOrderDetailEntity();
        detail.setProduct(product);
        detail.setRequestedQuantity(quantity);
        detail.setUnitCost(unitCost);
        return detail;
    }
}