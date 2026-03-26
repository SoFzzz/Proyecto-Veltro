package com.veltro.inventory.application.inventory.service;

import com.veltro.inventory.application.audit.command.AuditCommandExecutor;
import com.veltro.inventory.application.inventory.dto.InventoryResponse;
import com.veltro.inventory.application.inventory.dto.StockAdjustmentRequest;
import com.veltro.inventory.application.inventory.dto.StockEntryRequest;
import com.veltro.inventory.application.inventory.dto.StockExitRequest;
import com.veltro.inventory.application.inventory.dto.UpdateStockLimitsRequest;
import com.veltro.inventory.application.inventory.mapper.InventoryMapper;
import com.veltro.inventory.application.inventory.mapper.InventoryMovementMapper;
import com.veltro.inventory.domain.catalog.model.ProductEntity;
import com.veltro.inventory.domain.inventory.model.InventoryEntity;
import com.veltro.inventory.domain.inventory.model.InventoryMovementEntity;
import com.veltro.inventory.domain.inventory.model.MovementType;
import com.veltro.inventory.domain.inventory.ports.InventoryMovementRepository;
import com.veltro.inventory.domain.inventory.ports.InventoryRepository;
import com.veltro.inventory.exception.InsufficientStockException;
import com.veltro.inventory.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link InventoryService} (B1-04).
 *
 * Tests entry/exit/adjustment/limits operations and the auto-create factory
 * in isolation (no Spring context, no database).
 */
@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InventoryMovementRepository movementRepository;

    @Mock
    private InventoryMapper inventoryMapper;

    @Mock
    private InventoryMovementMapper movementMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private AuditCommandExecutor auditCommandExecutor;

    private InventoryService inventoryService;

    @BeforeEach
    void setUp() {
        inventoryService = new InventoryService(inventoryRepository, movementRepository, inventoryMapper, movementMapper, eventPublisher, auditCommandExecutor);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static ProductEntity stubProduct(Long id, String name) {
        ProductEntity p = new ProductEntity();
        p.setId(id);
        p.setName(name);
        return p;
    }

    private static InventoryEntity stubInventory(Long id, Long productId, int stock) {
        ProductEntity product = stubProduct(productId, "Widget");
        InventoryEntity inv = new InventoryEntity();
        inv.setId(id);
        inv.setProduct(product);
        inv.setCurrentStock(stock);
        inv.setMinStock(0);
        inv.setMaxStock(100);
        inv.setActive(true);
        return inv;
    }

    private static InventoryResponse stubResponse(Long id, Long productId, int stock) {
        return new InventoryResponse(id, productId, "Widget", stock, 0, 100, true, 0L);
    }

    // -------------------------------------------------------------------------
    // findByProductId
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findByProductId returns InventoryResponse when inventory exists")
    void findByProductId_existingProduct_returnsResponse() {
        InventoryEntity inv = stubInventory(1L, 10L, 50);
        InventoryResponse expected = stubResponse(1L, 10L, 50);

        when(inventoryRepository.findByProductIdAndActiveTrue(10L)).thenReturn(Optional.of(inv));
        when(inventoryMapper.toResponse(inv)).thenReturn(expected);

        InventoryResponse result = inventoryService.findByProductId(10L);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("findByProductId throws NotFoundException when no inventory exists for product")
    void findByProductId_unknownProduct_throwsNotFoundException() {
        when(inventoryRepository.findByProductIdAndActiveTrue(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.findByProductId(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    // -------------------------------------------------------------------------
    // recordEntry
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("recordEntry increases currentStock by requested quantity")
    void recordEntry_validRequest_increasesStock() {
        InventoryEntity inv = stubInventory(1L, 10L, 20);
        InventoryEntity savedInv = stubInventory(1L, 10L, 25);
        InventoryResponse expected = stubResponse(1L, 10L, 25);
        StockEntryRequest request = new StockEntryRequest(5, "restock");

        when(inventoryRepository.findByProductIdAndActiveTrue(10L)).thenReturn(Optional.of(inv));
        when(inventoryRepository.save(inv)).thenReturn(savedInv);
        when(movementRepository.save(any(InventoryMovementEntity.class))).thenAnswer(i -> i.getArgument(0));
        when(inventoryMapper.toResponse(savedInv)).thenReturn(expected);

        InventoryResponse result = inventoryService.recordEntry(10L, request);

        assertThat(result.currentStock()).isEqualTo(25);
        assertThat(inv.getCurrentStock()).isEqualTo(25);
    }

    @Test
    @DisplayName("recordEntry saves an ENTRY movement record")
    void recordEntry_validRequest_savesEntryMovement() {
        InventoryEntity inv = stubInventory(1L, 10L, 10);
        InventoryEntity savedInv = stubInventory(1L, 10L, 13);
        StockEntryRequest request = new StockEntryRequest(3, "received goods");

        when(inventoryRepository.findByProductIdAndActiveTrue(10L)).thenReturn(Optional.of(inv));
        when(inventoryRepository.save(inv)).thenReturn(savedInv);
        when(movementRepository.save(any(InventoryMovementEntity.class))).thenAnswer(i -> i.getArgument(0));
        when(inventoryMapper.toResponse(any())).thenReturn(stubResponse(1L, 10L, 13));

        inventoryService.recordEntry(10L, request);

        ArgumentCaptor<InventoryMovementEntity> captor = ArgumentCaptor.forClass(InventoryMovementEntity.class);
        verify(movementRepository).save(captor.capture());
        InventoryMovementEntity saved = captor.getValue();

        assertThat(saved.getMovementType()).isEqualTo(MovementType.ENTRY);
        assertThat(saved.getQuantity()).isEqualTo(3);
        assertThat(saved.getPreviousStock()).isEqualTo(10);
        assertThat(saved.getNewStock()).isEqualTo(13);
        assertThat(saved.getReason()).isEqualTo("received goods");
    }

    // -------------------------------------------------------------------------
    // recordExit — AC-04
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("recordExit decreases currentStock by requested quantity")
    void recordExit_sufficientStock_decreasesStock() {
        InventoryEntity inv = stubInventory(1L, 10L, 30);
        InventoryEntity savedInv = stubInventory(1L, 10L, 20);
        InventoryResponse expected = stubResponse(1L, 10L, 20);
        StockExitRequest request = new StockExitRequest(10, "shrinkage");

        when(inventoryRepository.findByProductIdAndActiveTrue(10L)).thenReturn(Optional.of(inv));
        when(inventoryRepository.save(inv)).thenReturn(savedInv);
        when(movementRepository.save(any(InventoryMovementEntity.class))).thenAnswer(i -> i.getArgument(0));
        when(inventoryMapper.toResponse(savedInv)).thenReturn(expected);

        InventoryResponse result = inventoryService.recordExit(10L, request);

        assertThat(result.currentStock()).isEqualTo(20);
    }

    @Test
    @DisplayName("recordExit with quantity == currentStock reduces stock to zero (AC-04 boundary)")
    void recordExit_exactCurrentStock_reducesToZero() {
        InventoryEntity inv = stubInventory(1L, 10L, 5);
        InventoryEntity savedInv = stubInventory(1L, 10L, 0);
        StockExitRequest request = new StockExitRequest(5, "cleared");

        when(inventoryRepository.findByProductIdAndActiveTrue(10L)).thenReturn(Optional.of(inv));
        when(inventoryRepository.save(inv)).thenReturn(savedInv);
        when(movementRepository.save(any(InventoryMovementEntity.class))).thenAnswer(i -> i.getArgument(0));
        when(inventoryMapper.toResponse(savedInv)).thenReturn(stubResponse(1L, 10L, 0));

        InventoryResponse result = inventoryService.recordExit(10L, request);

        assertThat(result.currentStock()).isEqualTo(0);
    }

    @Test
    @DisplayName("recordExit throws InsufficientStockException when quantity > currentStock (AC-04)")
    void recordExit_insufficientStock_throwsInsufficientStockException() {
        InventoryEntity inv = stubInventory(1L, 10L, 3);
        StockExitRequest request = new StockExitRequest(10, "overshoot");

        when(inventoryRepository.findByProductIdAndActiveTrue(10L)).thenReturn(Optional.of(inv));

        assertThatThrownBy(() -> inventoryService.recordExit(10L, request))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Widget");
    }

    @Test
    @DisplayName("recordExit saves an EXIT movement record")
    void recordExit_sufficientStock_savesExitMovement() {
        InventoryEntity inv = stubInventory(1L, 10L, 20);
        InventoryEntity savedInv = stubInventory(1L, 10L, 15);
        StockExitRequest request = new StockExitRequest(5, "sale");

        when(inventoryRepository.findByProductIdAndActiveTrue(10L)).thenReturn(Optional.of(inv));
        when(inventoryRepository.save(inv)).thenReturn(savedInv);
        when(movementRepository.save(any(InventoryMovementEntity.class))).thenAnswer(i -> i.getArgument(0));
        when(inventoryMapper.toResponse(any())).thenReturn(stubResponse(1L, 10L, 15));

        inventoryService.recordExit(10L, request);

        ArgumentCaptor<InventoryMovementEntity> captor = ArgumentCaptor.forClass(InventoryMovementEntity.class);
        verify(movementRepository).save(captor.capture());
        InventoryMovementEntity saved = captor.getValue();

        assertThat(saved.getMovementType()).isEqualTo(MovementType.EXIT);
        assertThat(saved.getQuantity()).isEqualTo(5);
        assertThat(saved.getPreviousStock()).isEqualTo(20);
        assertThat(saved.getNewStock()).isEqualTo(15);
    }

    // -------------------------------------------------------------------------
    // recordAdjustment
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("recordAdjustment sets stock to the absolute new value")
    void recordAdjustment_validRequest_setsAbsoluteStock() {
        InventoryEntity inv = stubInventory(1L, 10L, 30);
        InventoryEntity savedInv = stubInventory(1L, 10L, 45);
        StockAdjustmentRequest request = new StockAdjustmentRequest(45, "physical count");

        when(inventoryRepository.findByProductIdAndActiveTrue(10L)).thenReturn(Optional.of(inv));
        when(inventoryRepository.save(inv)).thenReturn(savedInv);
        when(movementRepository.save(any(InventoryMovementEntity.class))).thenAnswer(i -> i.getArgument(0));
        when(inventoryMapper.toResponse(savedInv)).thenReturn(stubResponse(1L, 10L, 45));

        InventoryResponse result = inventoryService.recordAdjustment(10L, request);

        assertThat(result.currentStock()).isEqualTo(45);
        assertThat(inv.getCurrentStock()).isEqualTo(45);
    }

    @Test
    @DisplayName("recordAdjustment saves ADJUSTMENT movement with correct delta")
    void recordAdjustment_validRequest_savesAdjustmentMovement() {
        InventoryEntity inv = stubInventory(1L, 10L, 20);
        InventoryEntity savedInv = stubInventory(1L, 10L, 35);
        StockAdjustmentRequest request = new StockAdjustmentRequest(35, "recount");

        when(inventoryRepository.findByProductIdAndActiveTrue(10L)).thenReturn(Optional.of(inv));
        when(inventoryRepository.save(inv)).thenReturn(savedInv);
        when(movementRepository.save(any(InventoryMovementEntity.class))).thenAnswer(i -> i.getArgument(0));
        when(inventoryMapper.toResponse(any())).thenReturn(stubResponse(1L, 10L, 35));

        inventoryService.recordAdjustment(10L, request);

        ArgumentCaptor<InventoryMovementEntity> captor = ArgumentCaptor.forClass(InventoryMovementEntity.class);
        verify(movementRepository).save(captor.capture());
        InventoryMovementEntity saved = captor.getValue();

        assertThat(saved.getMovementType()).isEqualTo(MovementType.ADJUSTMENT);
        assertThat(saved.getQuantity()).isEqualTo(15); // |35 - 20|
        assertThat(saved.getPreviousStock()).isEqualTo(20);
        assertThat(saved.getNewStock()).isEqualTo(35);
    }

    @Test
    @DisplayName("recordAdjustment with no-change uses sentinel quantity 1 to satisfy DB constraint")
    void recordAdjustment_noChange_usesSentinelQuantity() {
        InventoryEntity inv = stubInventory(1L, 10L, 10);
        InventoryEntity savedInv = stubInventory(1L, 10L, 10);
        StockAdjustmentRequest request = new StockAdjustmentRequest(10, "confirm count");

        when(inventoryRepository.findByProductIdAndActiveTrue(10L)).thenReturn(Optional.of(inv));
        when(inventoryRepository.save(inv)).thenReturn(savedInv);
        when(movementRepository.save(any(InventoryMovementEntity.class))).thenAnswer(i -> i.getArgument(0));
        when(inventoryMapper.toResponse(any())).thenReturn(stubResponse(1L, 10L, 10));

        inventoryService.recordAdjustment(10L, request);

        ArgumentCaptor<InventoryMovementEntity> captor = ArgumentCaptor.forClass(InventoryMovementEntity.class);
        verify(movementRepository).save(captor.capture());

        assertThat(captor.getValue().getQuantity()).isEqualTo(1);
    }

    // -------------------------------------------------------------------------
    // updateLimits
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("updateLimits sets minStock and maxStock on the inventory record")
    void updateLimits_validRequest_updatesThresholds() {
        InventoryEntity inv = stubInventory(1L, 10L, 20);
        InventoryEntity savedInv = stubInventory(1L, 10L, 20);
        savedInv.setMinStock(5);
        savedInv.setMaxStock(200);
        UpdateStockLimitsRequest request = new UpdateStockLimitsRequest(5, 200);

        when(inventoryRepository.findByProductIdAndActiveTrue(10L)).thenReturn(Optional.of(inv));
        when(inventoryRepository.save(inv)).thenReturn(savedInv);
        when(inventoryMapper.toResponse(savedInv))
                .thenReturn(new InventoryResponse(1L, 10L, "Widget", 20, 5, 200, true, 0L));

        InventoryResponse result = inventoryService.updateLimits(10L, request);

        assertThat(result.minStock()).isEqualTo(5);
        assertThat(result.maxStock()).isEqualTo(200);
        assertThat(inv.getMinStock()).isEqualTo(5);
        assertThat(inv.getMaxStock()).isEqualTo(200);
    }

    // -------------------------------------------------------------------------
    // createForProduct
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("createForProduct creates an InventoryEntity with zero initial stock")
    void createForProduct_newProduct_createsZeroStockInventory() {
        ProductEntity product = stubProduct(7L, "New Product");
        InventoryEntity savedInv = stubInventory(99L, 7L, 0);

        when(inventoryRepository.save(any(InventoryEntity.class))).thenReturn(savedInv);

        InventoryEntity result = inventoryService.createForProduct(product);

        ArgumentCaptor<InventoryEntity> captor = ArgumentCaptor.forClass(InventoryEntity.class);
        verify(inventoryRepository).save(captor.capture());
        InventoryEntity persisted = captor.getValue();

        assertThat(persisted.getCurrentStock()).isEqualTo(0);
        assertThat(persisted.getMinStock()).isEqualTo(0);
        assertThat(persisted.getMaxStock()).isEqualTo(0);
        assertThat(persisted.getProduct()).isEqualTo(product);
        assertThat(result.getId()).isEqualTo(99L);
    }
}
