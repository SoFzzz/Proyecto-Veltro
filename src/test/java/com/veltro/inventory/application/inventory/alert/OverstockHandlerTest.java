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
 * Unit tests for {@link OverstockHandler} (B2-03).
 * 
 * Tests the Chain of Responsibility handler that creates OVERSTOCK alerts
 * when current stock exceeds the overstock threshold.
 */
@ExtendWith(MockitoExtension.class)
class OverstockHandlerTest {

    @Mock
    private AlertHandler nextHandler;

    @Test
    @DisplayName("creates OVERSTOCK alert when current stock exceeds overstock threshold")
    void handle_stockAboveOverstockThreshold_createsOverstockAlert() {
        // Arrange
        OverstockHandler handler = new OverstockHandler();
        handler.setNext(nextHandler);
        StockEvaluationContext context = new StockEvaluationContext(
                1L, "Test Product", 25, 2, 5, 20); // current=25, critical=2, min=5, overstock=20

        // Act
        handler.handle(context);

        // Assert
        List<AlertEntity> alerts = context.getGeneratedAlerts();
        assertThat(alerts).hasSize(1);
        
        AlertEntity alert = alerts.get(0);
        assertThat(alert.getType()).isEqualTo(AlertType.OVERSTOCK);
        assertThat(alert.getSeverity()).isEqualTo(AlertSeverity.INFO);
        assertThat(alert.getMessage()).isEqualTo("Product Test Product exceeds overstock threshold");
        
        verify(nextHandler).handle(context);
    }

    @Test
    @DisplayName("creates OVERSTOCK alert when current stock significantly exceeds threshold")
    void handle_stockSignificantlyAboveThreshold_createsOverstockAlert() {
        // Arrange
        OverstockHandler handler = new OverstockHandler();
        handler.setNext(nextHandler);
        StockEvaluationContext context = new StockEvaluationContext(
                2L, "Widget", 100, 1, 10, 50); // current=100, critical=1, min=10, overstock=50

        // Act
        handler.handle(context);

        // Assert
        List<AlertEntity> alerts = context.getGeneratedAlerts();
        assertThat(alerts).hasSize(1);
        
        AlertEntity alert = alerts.get(0);
        assertThat(alert.getType()).isEqualTo(AlertType.OVERSTOCK);
        assertThat(alert.getSeverity()).isEqualTo(AlertSeverity.INFO);
        assertThat(alert.getMessage()).isEqualTo("Product Widget exceeds overstock threshold");
        
        verify(nextHandler).handle(context);
    }

    @Test
    @DisplayName("does not create alert when current stock equals overstock threshold")
    void handle_stockEqualsThreshold_doesNotCreateAlert() {
        // Arrange
        OverstockHandler handler = new OverstockHandler();
        handler.setNext(nextHandler);
        StockEvaluationContext context = new StockEvaluationContext(
                3L, "Gadget", 20, 2, 5, 20); // current=20, critical=2, min=5, overstock=20

        // Act
        handler.handle(context);

        // Assert
        List<AlertEntity> alerts = context.getGeneratedAlerts();
        assertThat(alerts).isEmpty();
        
        verify(nextHandler).handle(context);
    }

    @Test
    @DisplayName("does not create alert when current stock is below overstock threshold")
    void handle_stockBelowThreshold_doesNotCreateAlert() {
        // Arrange
        OverstockHandler handler = new OverstockHandler();
        handler.setNext(nextHandler);
        StockEvaluationContext context = new StockEvaluationContext(
                4L, "Item", 15, 2, 5, 20); // current=15, critical=2, min=5, overstock=20

        // Act
        handler.handle(context);

        // Assert
        List<AlertEntity> alerts = context.getGeneratedAlerts();
        assertThat(alerts).isEmpty();
        
        verify(nextHandler).handle(context);
    }

    @Test
    @DisplayName("does not create alert when overstock threshold is zero or not configured")
    void handle_thresholdZero_doesNotCreateAlert() {
        // Arrange
        OverstockHandler handler = new OverstockHandler();
        handler.setNext(nextHandler);
        StockEvaluationContext context = new StockEvaluationContext(
                5L, "Product", 100, 2, 5, 0); // current=100, critical=2, min=5, overstock=0

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
        OverstockHandler handler = new OverstockHandler();
        // No next handler set
        StockEvaluationContext context = new StockEvaluationContext(
                6L, "Product", 50, 2, 5, 30);

        // Act
        handler.handle(context);

        // Assert - should complete without throwing exception
        List<AlertEntity> alerts = context.getGeneratedAlerts();
        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).getType()).isEqualTo(AlertType.OVERSTOCK);
    }
}