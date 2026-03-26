package com.veltro.inventory.application.inventory.alert;

import com.veltro.inventory.domain.inventory.model.AlertEntity;
import com.veltro.inventory.domain.inventory.model.AlertSeverity;
import com.veltro.inventory.domain.inventory.model.AlertType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link LowStockHandler} (B2-03).
 * 
 * Tests the Chain of Responsibility handler that creates LOW_STOCK alerts
 * when current stock is above critical but at or below minimum stock.
 */
@ExtendWith(MockitoExtension.class)
class LowStockHandlerTest {

    @Mock
    private AlertHandler nextHandler;

    @Test
    @DisplayName("creates LOW_STOCK alert when current stock is above critical but at minimum stock")
    void handle_stockAtMinimum_createsLowStockAlert() {
        // Arrange
        LowStockHandler handler = new LowStockHandler();
        handler.setNext(nextHandler);
        StockEvaluationContext context = new StockEvaluationContext(
                1L, "Test Product", 5, 2, 5, 20); // current=5, critical=2, min=5, overstock=20

        // Act
        handler.handle(context);

        // Assert
        List<AlertEntity> alerts = context.getGeneratedAlerts();
        assertThat(alerts).hasSize(1);
        
        AlertEntity alert = alerts.get(0);
        assertThat(alert.getType()).isEqualTo(AlertType.LOW_STOCK);
        assertThat(alert.getSeverity()).isEqualTo(AlertSeverity.WARNING);
        assertThat(alert.getMessage()).isEqualTo("Product Test Product is below minimum stock");
        
        verify(nextHandler).handle(context);
    }

    @Test
    @DisplayName("creates LOW_STOCK alert when current stock is below minimum but above critical")
    void handle_stockBelowMinimumButAboveCritical_createsLowStockAlert() {
        // Arrange
        LowStockHandler handler = new LowStockHandler();
        handler.setNext(nextHandler);
        StockEvaluationContext context = new StockEvaluationContext(
                2L, "Widget", 3, 1, 5, 15); // current=3, critical=1, min=5, overstock=15

        // Act
        handler.handle(context);

        // Assert
        List<AlertEntity> alerts = context.getGeneratedAlerts();
        assertThat(alerts).hasSize(1);
        
        AlertEntity alert = alerts.get(0);
        assertThat(alert.getType()).isEqualTo(AlertType.LOW_STOCK);
        assertThat(alert.getSeverity()).isEqualTo(AlertSeverity.WARNING);
        assertThat(alert.getMessage()).isEqualTo("Product Widget is below minimum stock");
        
        verify(nextHandler).handle(context);
    }

    @Test
    @DisplayName("does not create alert when current stock is above minimum")
    void handle_stockAboveMinimum_doesNotCreateAlert() {
        // Arrange
        LowStockHandler handler = new LowStockHandler();
        handler.setNext(nextHandler);
        StockEvaluationContext context = new StockEvaluationContext(
                3L, "Gadget", 10, 2, 5, 20); // current=10, critical=2, min=5, overstock=20

        // Act
        handler.handle(context);

        // Assert
        List<AlertEntity> alerts = context.getGeneratedAlerts();
        assertThat(alerts).isEmpty();
        
        verify(nextHandler).handle(context);
    }

    @Test
    @DisplayName("does not create alert when current stock is at or below critical level")
    void handle_stockAtOrBelowCritical_doesNotCreateAlert() {
        // Arrange
        LowStockHandler handler = new LowStockHandler();
        handler.setNext(nextHandler);
        StockEvaluationContext context = new StockEvaluationContext(
                4L, "Item", 1, 1, 5, 20); // current=1, critical=1, min=5, overstock=20

        // Act
        handler.handle(context);

        // Assert
        List<AlertEntity> alerts = context.getGeneratedAlerts();
        assertThat(alerts).isEmpty();
        
        verify(nextHandler).handle(context);
    }

    @Test
    @DisplayName("continues chain execution without next handler")
    void handle_noNextHandler_completesWithoutError() {
        // Arrange
        LowStockHandler handler = new LowStockHandler();
        // No next handler set
        StockEvaluationContext context = new StockEvaluationContext(
                5L, "Product", 3, 1, 5, 20);

        // Act
        handler.handle(context);

        // Assert - should complete without throwing exception
        List<AlertEntity> alerts = context.getGeneratedAlerts();
        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).getType()).isEqualTo(AlertType.LOW_STOCK);
    }
}