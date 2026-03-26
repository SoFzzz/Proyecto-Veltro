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
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link CompletedState} (B2-01 - State Pattern).
 *
 * <p>Tests that COMPLETED state rejects all item operations and confirm, but allows void.
 */
class CompletedStateTest {

    private CompletedState state;
    private SaleEntity sale;

    @BeforeEach
    void setUp() {
        state = new CompletedState();
        sale = new SaleEntity();
        sale.setId(1L);
        sale.setSaleNumber("VLT-2026-000001");
        sale.setStatus(SaleStatus.COMPLETED);
        sale.setCashierId(10L);
        sale.setCompletedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("addItem throws InvalidStateTransitionException")
    void addItem_completedSale_throwsInvalidStateTransition() {
        SaleDetailEntity detail = new SaleDetailEntity();

        assertThatThrownBy(() -> state.addItem(sale, detail))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("COMPLETED");
    }

    @Test
    @DisplayName("modifyItem throws InvalidStateTransitionException")
    void modifyItem_completedSale_throwsInvalidStateTransition() {
        assertThatThrownBy(() -> state.modifyItem(sale, 999L, 5))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("COMPLETED");
    }

    @Test
    @DisplayName("removeItem throws InvalidStateTransitionException")
    void removeItem_completedSale_throwsInvalidStateTransition() {
        assertThatThrownBy(() -> state.removeItem(sale, 999L))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("COMPLETED");
    }

    @Test
    @DisplayName("confirm throws InvalidStateTransitionException")
    void confirm_completedSale_throwsInvalidStateTransition() {
        assertThatThrownBy(() -> state.confirm(sale, PaymentMethod.CASH))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("COMPLETED");
    }

    @Test
    @DisplayName("voidSale transitions to VOIDED")
    void voidSale_completedSale_transitionsToVoided() {
        state.voidSale(sale);

        assertThat(sale.getStatus()).isEqualTo(SaleStatus.VOIDED);
    }
}
