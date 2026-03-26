package com.veltro.inventory.application.inventory.listener;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.veltro.inventory.application.inventory.event.StockChangedEvent;
import com.veltro.inventory.application.inventory.service.AlertService;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EvaluateStockAlertsListenerTest {

    @Mock
    private AlertService alertService;

    private EvaluateStockAlertsListener listener;

    @BeforeEach
    void setUp() {
        listener = new EvaluateStockAlertsListener(alertService);
    }

    @Test
    @DisplayName("handles null events gracefully")
    void nullEvent() {
        listener.onStockChanged(null);
    }

    @Test
    @DisplayName("invokes alert service when product id present")
    void invokesAlertService() {
        StockChangedEvent event = new StockChangedEvent(10L, "Test", 5, 7, "reason", OffsetDateTime.now());

        listener.onStockChanged(event);

        verify(alertService).evaluateStock(10L);
    }

    @Test
    @DisplayName("propagates exceptions from alert service")
    void propagatesExceptions() {
        StockChangedEvent event = new StockChangedEvent(5L, "Test", 5, 3, "reason", OffsetDateTime.now());
        doThrow(new IllegalStateException("boom")).when(alertService).evaluateStock(5L);

        assertThatThrownBy(() -> listener.onStockChanged(event))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("boom");
    }
}
