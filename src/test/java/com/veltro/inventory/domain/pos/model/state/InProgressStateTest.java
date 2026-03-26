package com.veltro.inventory.domain.pos.model.state;

import com.veltro.inventory.domain.pos.model.PaymentMethod;
import com.veltro.inventory.domain.pos.model.SaleDetailEntity;
import com.veltro.inventory.domain.pos.model.SaleEntity;
import com.veltro.inventory.domain.pos.model.SaleStatus;
import com.veltro.inventory.exception.InvalidStateTransitionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link InProgressState} (B2-01 - State Pattern).
 *
 * <p>Tests that IN_PROGRESS state allows item operations and confirm, but rejects void.
 */
class InProgressStateTest {

    private InProgressState state;
    private SaleEntity sale;

    @BeforeEach
    void setUp() {
        state = new InProgressState();
        sale = new SaleEntity();
        sale.setId(1L);
        sale.setSaleNumber("VLT-2026-000001");
        sale.setStatus(SaleStatus.IN_PROGRESS);
        sale.setCashierId(10L);
    }

    @Test
    @DisplayName("addItem adds detail to sale")
    void addItem_validDetail_addsToSale() {
        SaleDetailEntity detail = createDetail(100L, "Widget", 3, new BigDecimal("25.5000"));

        state.addItem(sale, detail);

        assertThat(sale.getDetails()).hasSize(1);
        assertThat(sale.getDetails().get(0)).isEqualTo(detail);
        assertThat(detail.getSale()).isEqualTo(sale);
    }

    @Test
    @DisplayName("modifyItem updates quantity")
    void modifyItem_validItemId_updatesQuantity() {
        SaleDetailEntity detail = createDetail(100L, "Widget", 2, new BigDecimal("10.0000"));
        detail.setId(5L);
        sale.getDetails().add(detail);

        state.modifyItem(sale, 5L, 5);

        assertThat(detail.getQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("removeItem sets detail to inactive")
    void removeItem_validItemId_softDeletes() {
        SaleDetailEntity detail = createDetail(100L, "Widget", 4, new BigDecimal("15.0000"));
        detail.setId(5L);
        sale.getDetails().add(detail);

        state.removeItem(sale, 5L);

        assertThat(detail.isActive()).isFalse();
    }

    @Test
    @DisplayName("confirm transitions to COMPLETED")
    void confirm_validSale_transitionsToCompleted() {
        state.confirm(sale, PaymentMethod.CASH);

        assertThat(sale.getStatus()).isEqualTo(SaleStatus.COMPLETED);
        assertThat(sale.getPaymentMethod()).isEqualTo(PaymentMethod.CASH);
        assertThat(sale.getCompletedAt()).isNotNull();
    }

    @Test
    @DisplayName("voidSale throws InvalidStateTransitionException")
    void voidSale_inProgressState_throwsInvalidStateTransition() {
        assertThatThrownBy(() -> state.voidSale(sale))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("IN_PROGRESS");
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private SaleDetailEntity createDetail(Long productId, String productName, int quantity, BigDecimal unitPrice) {
        SaleDetailEntity detail = new SaleDetailEntity();
        detail.setProductId(productId);
        detail.setProductName(productName);
        detail.setQuantity(quantity);
        detail.setUnitPrice(unitPrice);
        detail.calculateSubtotal();
        return detail;
    }
}
