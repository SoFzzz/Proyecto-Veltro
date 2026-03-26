package com.veltro.inventory.application.inventory.service;

import com.veltro.inventory.application.inventory.alert.AlertHandler;
import com.veltro.inventory.application.inventory.alert.StockEvaluationContext;
import com.veltro.inventory.application.inventory.dto.AlertResponse;
import com.veltro.inventory.application.inventory.mapper.AlertMapper;
import com.veltro.inventory.domain.catalog.model.ProductEntity;
import com.veltro.inventory.domain.inventory.model.AlertConfigurationEntity;
import com.veltro.inventory.domain.inventory.model.AlertEntity;
import com.veltro.inventory.domain.inventory.model.AlertSeverity;
import com.veltro.inventory.domain.inventory.model.AlertType;
import com.veltro.inventory.domain.inventory.model.InventoryEntity;
import com.veltro.inventory.domain.inventory.ports.AlertConfigurationRepository;
import com.veltro.inventory.domain.inventory.ports.AlertRepository;
import com.veltro.inventory.domain.inventory.ports.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AlertService} (B2-03).
 * 
 * Tests the smart auto-resolution logic and alert management operations.
 */
@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private AlertConfigurationRepository configurationRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private AlertMapper alertMapper;

    @Mock
    private AlertHandler alertHandlerChain;

    private AlertService alertService;

    @BeforeEach
    void setUp() {
        alertService = new AlertService(alertRepository, configurationRepository, inventoryRepository, alertMapper, alertHandlerChain);
    }

    // -------------------------------------------------------------------------
    // Helper Methods
    // -------------------------------------------------------------------------

    private ProductEntity createProduct(Long id, String name) {
        ProductEntity product = new ProductEntity();
        product.setId(id);
        product.setName(name);
        return product;
    }

    private InventoryEntity createInventory(Long productId, String productName, int currentStock, int minStock, int maxStock) {
        ProductEntity product = createProduct(productId, productName);
        InventoryEntity inventory = new InventoryEntity();
        inventory.setId(1L);
        inventory.setProduct(product);
        inventory.setCurrentStock(currentStock);
        inventory.setMinStock(minStock);
        inventory.setMaxStock(maxStock);
        inventory.setActive(true);
        return inventory;
    }

    private AlertEntity createAlert(Long id, AlertType type, Long productId, boolean resolved, boolean read) {
        AlertEntity alert = new AlertEntity();
        alert.setId(id);
        alert.setType(type);
        alert.setProduct(createProduct(productId, "Test Product"));
        alert.setSeverity(AlertSeverity.WARNING);
        alert.setMessage("Test alert");
        alert.setResolved(resolved);
        alert.setRead(read);
        alert.setCreatedAt(Instant.now());
        alert.setActive(true);
        return alert;
    }

    // -------------------------------------------------------------------------
    // evaluateStock Tests
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("evaluateStock creates new alert when condition applies and no existing alert")
    void evaluateStock_newCondition_createsNewAlert() {
        // Arrange
        Long productId = 1L;
        InventoryEntity inventory = createInventory(productId, "Test Product", 3, 5, 20);
        
        when(inventoryRepository.findByProductIdAndActiveTrue(productId)).thenReturn(Optional.of(inventory));
        when(configurationRepository.findByProductIdAndActiveTrue(productId)).thenReturn(Optional.empty());
        when(alertRepository.findByProductIdAndResolvedFalse(productId)).thenReturn(List.of());
        
        doAnswer(invocation -> {
            StockEvaluationContext context = invocation.getArgument(0);
            AlertEntity alert = new AlertEntity();
            alert.setType(AlertType.LOW_STOCK);
            alert.setSeverity(AlertSeverity.WARNING);
            alert.setMessage("Low stock alert");
            context.addAlert(alert);
            return null;
        }).when(alertHandlerChain).handle(any(StockEvaluationContext.class));

        // Act
        alertService.evaluateStock(productId);

        // Assert
        verify(alertHandlerChain).handle(any(StockEvaluationContext.class));
        verify(alertRepository).save(any(AlertEntity.class));
        verify(alertRepository, never()).save(argThat(alert -> alert.isResolved()));
    }

    @Test
    @DisplayName("evaluateStock resolves existing alert when condition no longer applies")
    void evaluateStock_conditionResolved_resolvesExistingAlert() {
        // Arrange
        Long productId = 1L;
        InventoryEntity inventory = createInventory(productId, "Test Product", 10, 5, 20);
        AlertEntity existingAlert = createAlert(1L, AlertType.LOW_STOCK, productId, false, false);
        
        when(inventoryRepository.findByProductIdAndActiveTrue(productId)).thenReturn(Optional.of(inventory));
        when(configurationRepository.findByProductIdAndActiveTrue(productId)).thenReturn(Optional.empty());
        when(alertRepository.findByProductIdAndResolvedFalse(productId)).thenReturn(List.of(existingAlert));
        
        // No alerts generated (condition resolved)
        doNothing().when(alertHandlerChain).handle(any(StockEvaluationContext.class));

        // Act
        alertService.evaluateStock(productId);

        // Assert
        ArgumentCaptor<AlertEntity> alertCaptor = ArgumentCaptor.forClass(AlertEntity.class);
        verify(alertRepository).save(alertCaptor.capture());
        
        AlertEntity savedAlert = alertCaptor.getValue();
        assertThat(savedAlert.isResolved()).isTrue();
        assertThat(savedAlert.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("evaluateStock keeps alert active when condition persists")
    void evaluateStock_conditionPersists_keepsAlertActive() {
        // Arrange
        Long productId = 1L;
        InventoryEntity inventory = createInventory(productId, "Test Product", 3, 5, 20);
        AlertEntity existingAlert = createAlert(1L, AlertType.LOW_STOCK, productId, false, false);
        
        when(inventoryRepository.findByProductIdAndActiveTrue(productId)).thenReturn(Optional.of(inventory));
        when(configurationRepository.findByProductIdAndActiveTrue(productId)).thenReturn(Optional.empty());
        when(alertRepository.findByProductIdAndResolvedFalse(productId)).thenReturn(List.of(existingAlert));
        
        doAnswer(invocation -> {
            StockEvaluationContext context = invocation.getArgument(0);
            AlertEntity alert = new AlertEntity();
            alert.setType(AlertType.LOW_STOCK);
            alert.setSeverity(AlertSeverity.WARNING);
            alert.setMessage("Low stock alert");
            context.addAlert(alert);
            return null;
        }).when(alertHandlerChain).handle(any(StockEvaluationContext.class));

        // Act
        alertService.evaluateStock(productId);

        // Assert
        verify(alertHandlerChain).handle(any(StockEvaluationContext.class));
        // Should not save existing alert (not resolved)
        verify(alertRepository, never()).save(eq(existingAlert));
        // Should not create new alert (already exists)
        verify(alertRepository, never()).save(argThat(alert -> alert.getId() == null));
    }

    @Test
    @DisplayName("evaluateStock uses custom configuration when available")
    void evaluateStock_withCustomConfiguration_usesConfigurationThresholds() {
        // Arrange
        Long productId = 1L;
        InventoryEntity inventory = createInventory(productId, "Test Product", 8, 10, 50);
        AlertConfigurationEntity config = new AlertConfigurationEntity();
        config.setCriticalStock(2);
        config.setMinStock(15);
        config.setOverstockThreshold(100);
        
        when(inventoryRepository.findByProductIdAndActiveTrue(productId)).thenReturn(Optional.of(inventory));
        when(configurationRepository.findByProductIdAndActiveTrue(productId)).thenReturn(Optional.of(config));
        when(alertRepository.findByProductIdAndResolvedFalse(productId)).thenReturn(List.of());

        // Act
        alertService.evaluateStock(productId);

        // Assert
        ArgumentCaptor<StockEvaluationContext> contextCaptor = ArgumentCaptor.forClass(StockEvaluationContext.class);
        verify(alertHandlerChain).handle(contextCaptor.capture());
        
        StockEvaluationContext context = contextCaptor.getValue();
        assertThat(context.getCurrentStock()).isEqualTo(8);
        assertThat(context.getCriticalStock()).isEqualTo(2);
        assertThat(context.getMinStock()).isEqualTo(15);
        assertThat(context.getOverstockThreshold()).isEqualTo(100);
    }

    @Test
    @DisplayName("evaluateStock throws IllegalStateException when inventory not found")
    void evaluateStock_inventoryNotFound_throwsException() {
        // Arrange
        Long productId = 99L;
        when(inventoryRepository.findByProductIdAndActiveTrue(productId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> alertService.evaluateStock(productId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Inventory not found for product 99");
    }

    // -------------------------------------------------------------------------
    // Other Alert Operations
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("listActiveAlerts returns paginated alerts sorted by severity and creation time")
    void listActiveAlerts_withPagination_returnsSortedAlerts() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        AlertEntity alert1 = createAlert(1L, AlertType.OUT_OF_STOCK, 1L, false, false);
        AlertEntity alert2 = createAlert(2L, AlertType.LOW_STOCK, 2L, false, true);
        Page<AlertEntity> alertPage = new PageImpl<>(List.of(alert1, alert2), pageable, 2);
        
        AlertResponse response1 = new AlertResponse(1L, 1L, "Product 1", "OUT_OF_STOCK", 
                "CRITICAL", "Out of stock", false, false, OffsetDateTime.now());
        AlertResponse response2 = new AlertResponse(2L, 2L, "Product 2", "LOW_STOCK", 
                "WARNING", "Low stock", false, true, OffsetDateTime.now());
        
        when(alertRepository.findByResolvedFalseOrderBySeverityDescCreatedAtAsc(pageable)).thenReturn(alertPage);
        when(alertMapper.toResponse(alert1)).thenReturn(response1);
        when(alertMapper.toResponse(alert2)).thenReturn(response2);

        // Act
        Page<AlertResponse> result = alertService.listActiveAlerts(pageable);

        // Assert
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent().get(0).id()).isEqualTo(1L);
        assertThat(result.getContent().get(1).id()).isEqualTo(2L);
    }

    @Test
    @DisplayName("markAsRead sets read flag to true")
    void markAsRead_existingAlert_setsReadFlag() {
        // Arrange
        Long alertId = 1L;
        AlertEntity alert = createAlert(alertId, AlertType.LOW_STOCK, 1L, false, false);
        
        when(alertRepository.findByIdAndActiveTrue(alertId)).thenReturn(Optional.of(alert));

        // Act
        alertService.markAsRead(alertId);

        // Assert
        ArgumentCaptor<AlertEntity> alertCaptor = ArgumentCaptor.forClass(AlertEntity.class);
        verify(alertRepository).save(alertCaptor.capture());
        
        AlertEntity savedAlert = alertCaptor.getValue();
        assertThat(savedAlert.isRead()).isTrue();
        assertThat(savedAlert.getId()).isEqualTo(alertId);
    }

    @Test
    @DisplayName("markAsResolved sets resolved flag to true")
    void markAsResolved_existingAlert_setsResolvedFlag() {
        // Arrange
        Long alertId = 1L;
        AlertEntity alert = createAlert(alertId, AlertType.LOW_STOCK, 1L, false, false);
        
        when(alertRepository.findByIdAndActiveTrue(alertId)).thenReturn(Optional.of(alert));

        // Act
        alertService.markAsResolved(alertId);

        // Assert
        ArgumentCaptor<AlertEntity> alertCaptor = ArgumentCaptor.forClass(AlertEntity.class);
        verify(alertRepository).save(alertCaptor.capture());
        
        AlertEntity savedAlert = alertCaptor.getValue();
        assertThat(savedAlert.isResolved()).isTrue();
        assertThat(savedAlert.getId()).isEqualTo(alertId);
    }

    @Test
    @DisplayName("unreadCount returns count of unread and unresolved alerts")
    void unreadCount_returnsCorrectCount() {
        // Arrange
        when(alertRepository.countByReadFalseAndResolvedFalse()).thenReturn(5L);

        // Act
        long count = alertService.unreadCount();

        // Assert
        assertThat(count).isEqualTo(5L);
        verify(alertRepository).countByReadFalseAndResolvedFalse();
    }

    @Test
    @DisplayName("markAsRead throws exception when alert not found")
    void markAsRead_alertNotFound_throwsException() {
        // Arrange
        Long alertId = 99L;
        when(alertRepository.findByIdAndActiveTrue(alertId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> alertService.markAsRead(alertId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Alert not found");
    }

    @Test
    @DisplayName("markAsResolved throws exception when alert not found")
    void markAsResolved_alertNotFound_throwsException() {
        // Arrange
        Long alertId = 99L;
        when(alertRepository.findByIdAndActiveTrue(alertId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> alertService.markAsResolved(alertId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Alert not found");
    }
}