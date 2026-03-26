package com.veltro.inventory.domain.pos.model.state;

import com.veltro.inventory.domain.pos.model.PaymentMethod;
import com.veltro.inventory.domain.pos.model.SaleDetailEntity;
import com.veltro.inventory.domain.pos.model.SaleEntity;
import com.veltro.inventory.domain.pos.model.SaleStatus;
import com.veltro.inventory.exception.InvalidStateTransitionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link VoidedState} (B2-01 - State Pattern).
 *
 * <p>Tests that VOIDED is a terminal state - all operations are rejected.
 */
class VoidedStateTest {

    private VoidedState state;
    private SaleEntity sale;

    @BeforeEach
    void setUp() {
        state = new VoidedState();
        sale = new SaleEntity();
        sale.setId(1L);
        sale.setSaleNumber("VLT-2026-000001");
        sale.setStatus(SaleStatus.VOIDED);
        sale.setCashierId(10L);
    }

    @Test
    @DisplayName("addItem throws InvalidStateTransitionException")
    void addItem_voidedSale_throwsInvalidStateTransition() {
        SaleDetailEntity detail = new SaleDetailEntity();

        assertThatThrownBy(() -> state.addItem(sale, detail))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("VOIDED");
    }

    @Test
    @DisplayName("modifyItem throws InvalidStateTransitionException")
    void modifyItem_voidedSale_throwsInvalidStateTransition() {
        assertThatThrownBy(() -> state.modifyItem(sale, 999L, 5))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("VOIDED");
    }

    @Test
    @DisplayName("removeItem throws InvalidStateTransitionException")
    void removeItem_voidedSale_throwsInvalidStateTransition() {
        assertThatThrownBy(() -> state.removeItem(sale, 999L))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("VOIDED");
    }

    @Test
    @DisplayName("confirm throws InvalidStateTransitionException")
    void confirm_voidedSale_throwsInvalidStateTransition() {
        assertThatThrownBy(() -> state.confirm(sale, PaymentMethod.CASH))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("VOIDED");
    }

    @Test
    @DisplayName("voidSale throws InvalidStateTransitionException")
    void voidSale_voidedSale_throwsInvalidStateTransition() {
        assertThatThrownBy(() -> state.voidSale(sale))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("VOIDED");
    }
}
