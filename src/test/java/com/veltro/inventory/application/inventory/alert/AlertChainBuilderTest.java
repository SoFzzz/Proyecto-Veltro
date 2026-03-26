package com.veltro.inventory.application.inventory.alert;

import com.veltro.inventory.domain.inventory.model.AlertEntity;
import com.veltro.inventory.domain.inventory.model.AlertType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link AlertChainBuilder} (B2-03).
 * 
 * Tests the configuration class that builds the Chain of Responsibility
 * for stock alert handlers.
 */
class AlertChainBuilderTest {

    @Test
    @DisplayName("builds complete alert handler chain with proper sequence")
    void alertHandlerChain_buildsCompleteChain() {
        // Arrange
        AlertChainBuilder builder = new AlertChainBuilder();

        // Act
        AlertHandler chain = builder.alertHandlerChain();

        // Assert - chain should start with OutOfStockHandler
        assertThat(chain).isInstanceOf(OutOfStockHandler.class);
    }

    @Test
    @DisplayName("chain processes out of stock scenario correctly")
    void alertHandlerChain_outOfStockScenario_createsCorrectAlert() {
        // Arrange
        AlertChainBuilder builder = new AlertChainBuilder();
        AlertHandler chain = builder.alertHandlerChain();
        StockEvaluationContext context = new StockEvaluationContext(
                1L, "Product", 0, 2, 5, 20); // current=0, critical=2, min=5, overstock=20

        // Act
        chain.handle(context);

        // Assert - should create OUT_OF_STOCK alert
        List<AlertEntity> alerts = context.getGeneratedAlerts();
        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).getType()).isEqualTo(AlertType.OUT_OF_STOCK);
    }

    @Test
    @DisplayName("chain processes low stock scenario correctly")
    void alertHandlerChain_lowStockScenario_createsCorrectAlert() {
        // Arrange
        AlertChainBuilder builder = new AlertChainBuilder();
        AlertHandler chain = builder.alertHandlerChain();
        StockEvaluationContext context = new StockEvaluationContext(
                2L, "Widget", 4, 1, 5, 20); // current=4, critical=1, min=5, overstock=20

        // Act
        chain.handle(context);

        // Assert - should create LOW_STOCK alert
        List<AlertEntity> alerts = context.getGeneratedAlerts();
        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).getType()).isEqualTo(AlertType.LOW_STOCK);
    }

    @Test
    @DisplayName("chain processes overstock scenario correctly")
    void alertHandlerChain_overstockScenario_createsCorrectAlert() {
        // Arrange
        AlertChainBuilder builder = new AlertChainBuilder();
        AlertHandler chain = builder.alertHandlerChain();
        StockEvaluationContext context = new StockEvaluationContext(
                3L, "Gadget", 25, 2, 5, 20); // current=25, critical=2, min=5, overstock=20

        // Act
        chain.handle(context);

        // Assert - should create OVERSTOCK alert
        List<AlertEntity> alerts = context.getGeneratedAlerts();
        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).getType()).isEqualTo(AlertType.OVERSTOCK);
    }

    @Test
    @DisplayName("chain handles multiple alert conditions correctly")
    void alertHandlerChain_multipleConditions_createsAllApplicableAlerts() {
        // Arrange
        AlertChainBuilder builder = new AlertChainBuilder();
        AlertHandler chain = builder.alertHandlerChain();
        
        // This scenario shouldn't create multiple alerts simultaneously in practice,
        // but tests that all handlers execute
        StockEvaluationContext context1 = new StockEvaluationContext(
                4L, "Item1", 0, 2, 5, 20); // OUT_OF_STOCK
        StockEvaluationContext context2 = new StockEvaluationContext(
                5L, "Item2", 3, 1, 5, 20); // LOW_STOCK
        StockEvaluationContext context3 = new StockEvaluationContext(
                6L, "Item3", 30, 2, 5, 20); // OVERSTOCK

        // Act
        chain.handle(context1);
        chain.handle(context2);
        chain.handle(context3);

        // Assert
        assertThat(context1.getGeneratedAlerts()).hasSize(1);
        assertThat(context1.getGeneratedAlerts().get(0).getType()).isEqualTo(AlertType.OUT_OF_STOCK);
        
        assertThat(context2.getGeneratedAlerts()).hasSize(1);
        assertThat(context2.getGeneratedAlerts().get(0).getType()).isEqualTo(AlertType.LOW_STOCK);
        
        assertThat(context3.getGeneratedAlerts()).hasSize(1);
        assertThat(context3.getGeneratedAlerts().get(0).getType()).isEqualTo(AlertType.OVERSTOCK);
    }

    @Test
    @DisplayName("chain handles no alert conditions correctly")
    void alertHandlerChain_normalStock_createsNoAlerts() {
        // Arrange
        AlertChainBuilder builder = new AlertChainBuilder();
        AlertHandler chain = builder.alertHandlerChain();
        StockEvaluationContext context = new StockEvaluationContext(
                7L, "Normal Product", 10, 2, 5, 20); // current=10, all conditions normal

        // Act
        chain.handle(context);

        // Assert - should create no alerts
        List<AlertEntity> alerts = context.getGeneratedAlerts();
        assertThat(alerts).isEmpty();
    }
}