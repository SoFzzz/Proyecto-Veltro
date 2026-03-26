package com.veltro.inventory.infrastructure.adapters.web;

import com.veltro.inventory.application.inventory.dto.InventoryMovementResponse;
import com.veltro.inventory.application.inventory.dto.InventoryResponse;
import com.veltro.inventory.application.inventory.dto.StockAdjustmentRequest;
import com.veltro.inventory.application.inventory.dto.StockEntryRequest;
import com.veltro.inventory.application.inventory.dto.StockExitRequest;
import com.veltro.inventory.application.inventory.dto.UpdateStockLimitsRequest;
import com.veltro.inventory.application.inventory.service.InventoryService;
import com.veltro.inventory.exception.InsufficientStockException;
import com.veltro.inventory.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link InventoryController} (B1-04).
 *
 * Pure unit testing approach using direct method calls instead of MockMvc.
 */
@ExtendWith(MockitoExtension.class)
class InventoryControllerTest {

    @Mock
    private InventoryService inventoryService;
    
    private InventoryController controller;

    @BeforeEach
    void setUp() {
        controller = new InventoryController(inventoryService);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static InventoryResponse stubInventory() {
        return new InventoryResponse(1L, 10L, "Widget A", 50, 5, 200, true, 0L);
    }

    private static InventoryMovementResponse stubMovement() {
        return new InventoryMovementResponse(
                1L, 1L, "ENTRY", 10, 40, 50, "restock", Instant.now(), "admin");
    }

    // -------------------------------------------------------------------------
    // GET /{productId}
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /inventory/{productId} returns 200 with inventory")
    void getByProductId_existingProduct_returns200() {
        when(inventoryService.findByProductId(10L)).thenReturn(stubInventory());

        ResponseEntity<InventoryResponse> response = controller.getByProductId(10L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().productId()).isEqualTo(10L);
        assertThat(response.getBody().productName()).isEqualTo("Widget A");
        assertThat(response.getBody().currentStock()).isEqualTo(50);
        verify(inventoryService).findByProductId(10L);
    }

    @Test
    @DisplayName("GET /inventory/{productId} throws NotFoundException when product not found")
    void getByProductId_missingProduct_throwsNotFoundException() {
        when(inventoryService.findByProductId(999L))
                .thenThrow(new NotFoundException("Inventory not found for product: 999"));

        assertThatThrownBy(() -> controller.getByProductId(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Inventory not found for product: 999");

        verify(inventoryService).findByProductId(999L);
    }

    // -------------------------------------------------------------------------
    // GET /{productId}/movements
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /inventory/{productId}/movements returns paginated results")
    void getMovements_validProduct_returnsPaginatedResults() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<InventoryMovementResponse> page = new PageImpl<>(List.of(stubMovement()), pageable, 1);
        when(inventoryService.getMovements(10L, pageable)).thenReturn(page);

        ResponseEntity<Page<InventoryMovementResponse>> response = controller.getMovements(10L, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getContent().get(0).inventoryId()).isEqualTo(1L);
        assertThat(response.getBody().getContent().get(0).movementType()).isEqualTo("ENTRY");
        verify(inventoryService).getMovements(10L, pageable);
    }

    @Test
    @DisplayName("GET /inventory/{productId}/movements returns empty page when no movements exist")
    void getMovements_noMovements_returnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<InventoryMovementResponse> page = new PageImpl<>(List.of(), pageable, 0);
        when(inventoryService.getMovements(10L, pageable)).thenReturn(page);

        ResponseEntity<Page<InventoryMovementResponse>> response = controller.getMovements(10L, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).isEmpty();
        verify(inventoryService).getMovements(10L, pageable);
    }

    // -------------------------------------------------------------------------
    // POST /{productId}/entry
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /inventory/{productId}/entry returns 200 with updated inventory")
    void recordEntry_validRequest_returns200() {
        when(inventoryService.recordEntry(eq(10L), any(StockEntryRequest.class))).thenReturn(stubInventory());

        StockEntryRequest request = new StockEntryRequest(25, "Restock from supplier");
        ResponseEntity<InventoryResponse> response = controller.recordEntry(10L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().productId()).isEqualTo(10L);
        verify(inventoryService).recordEntry(eq(10L), any(StockEntryRequest.class));
    }

    @Test
    @DisplayName("POST /inventory/{productId}/entry delegates to InventoryService")
    void recordEntry_delegatesToService() {
        when(inventoryService.recordEntry(eq(5L), any(StockEntryRequest.class))).thenReturn(stubInventory());

        StockEntryRequest request = new StockEntryRequest(10, "Test entry");
        controller.recordEntry(5L, request);

        verify(inventoryService).recordEntry(5L, request);
    }

    // -------------------------------------------------------------------------
    // POST /{productId}/exit
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /inventory/{productId}/exit returns 200 with updated inventory")
    void recordExit_validRequest_returns200() {
        when(inventoryService.recordExit(eq(10L), any(StockExitRequest.class))).thenReturn(stubInventory());

        StockExitRequest request = new StockExitRequest(15, "Sale to customer");
        ResponseEntity<InventoryResponse> response = controller.recordExit(10L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().productId()).isEqualTo(10L);
        verify(inventoryService).recordExit(eq(10L), any(StockExitRequest.class));
    }

    @Test
    @DisplayName("POST /inventory/{productId}/exit throws InsufficientStockException when stock insufficient")
    void recordExit_insufficientStock_throwsException() {
        doThrow(new InsufficientStockException("Insufficient stock. Current: 5, requested: 10"))
                .when(inventoryService).recordExit(eq(10L), any(StockExitRequest.class));

        StockExitRequest request = new StockExitRequest(10, "Sale attempt");

        assertThatThrownBy(() -> controller.recordExit(10L, request))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessage("Insufficient stock. Current: 5, requested: 10");

        verify(inventoryService).recordExit(eq(10L), any(StockExitRequest.class));
    }

    // -------------------------------------------------------------------------
    // POST /{productId}/adjustment
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /inventory/{productId}/adjustment returns 200 with updated inventory")
    void recordAdjustment_validRequest_returns200() {
        when(inventoryService.recordAdjustment(eq(10L), any(StockAdjustmentRequest.class))).thenReturn(stubInventory());

        StockAdjustmentRequest request = new StockAdjustmentRequest(45, "Physical count correction");
        ResponseEntity<InventoryResponse> response = controller.recordAdjustment(10L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().productId()).isEqualTo(10L);
        verify(inventoryService).recordAdjustment(eq(10L), any(StockAdjustmentRequest.class));
    }

    @Test
    @DisplayName("POST /inventory/{productId}/adjustment delegates to InventoryService")
    void recordAdjustment_delegatesToService() {
        when(inventoryService.recordAdjustment(eq(7L), any(StockAdjustmentRequest.class))).thenReturn(stubInventory());

        StockAdjustmentRequest request = new StockAdjustmentRequest(30, "Inventory adjustment");
        controller.recordAdjustment(7L, request);

        verify(inventoryService).recordAdjustment(7L, request);
    }

    // -------------------------------------------------------------------------
    // PUT /{productId}/limits
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("PUT /inventory/{productId}/limits returns 200 with updated inventory")
    void updateLimits_validRequest_returns200() {
        when(inventoryService.updateLimits(eq(10L), any(UpdateStockLimitsRequest.class))).thenReturn(stubInventory());

        UpdateStockLimitsRequest request = new UpdateStockLimitsRequest(10, 150);
        ResponseEntity<InventoryResponse> response = controller.updateLimits(10L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().productId()).isEqualTo(10L);
        verify(inventoryService).updateLimits(eq(10L), any(UpdateStockLimitsRequest.class));
    }

    @Test
    @DisplayName("PUT /inventory/{productId}/limits delegates to InventoryService")
    void updateLimits_delegatesToService() {
        when(inventoryService.updateLimits(eq(3L), any(UpdateStockLimitsRequest.class))).thenReturn(stubInventory());

        UpdateStockLimitsRequest request = new UpdateStockLimitsRequest(5, 100);
        controller.updateLimits(3L, request);

        verify(inventoryService).updateLimits(3L, request);
    }

    @Test
    @DisplayName("PUT /inventory/{productId}/limits throws NotFoundException when product not found")
    void updateLimits_missingProduct_throwsNotFoundException() {
        doThrow(new NotFoundException("Inventory not found for product: 999"))
                .when(inventoryService).updateLimits(eq(999L), any(UpdateStockLimitsRequest.class));

        UpdateStockLimitsRequest request = new UpdateStockLimitsRequest(5, 100);

        assertThatThrownBy(() -> controller.updateLimits(999L, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Inventory not found for product: 999");

        verify(inventoryService).updateLimits(999L, request);
    }

    // -------------------------------------------------------------------------
    // Edge cases and error handling
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Methods handle service exceptions appropriately")
    void methodsHandleServiceExceptions() {
        // Test that exceptions from service are propagated correctly
        when(inventoryService.findByProductId(any())).thenThrow(new RuntimeException("Database error"));

        assertThatThrownBy(() -> controller.getByProductId(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database error");
    }

    @Test
    @DisplayName("All methods delegate correctly to service layer")
    void allMethodsDelegateToService() {
        // Setup mocks for all service calls
        when(inventoryService.findByProductId(any())).thenReturn(stubInventory());
        when(inventoryService.getMovements(any(), any())).thenReturn(new PageImpl<>(List.of()));
        when(inventoryService.recordEntry(any(), any())).thenReturn(stubInventory());
        when(inventoryService.recordExit(any(), any())).thenReturn(stubInventory());
        when(inventoryService.recordAdjustment(any(), any())).thenReturn(stubInventory());
        when(inventoryService.updateLimits(any(), any())).thenReturn(stubInventory());

        // Call all controller methods
        controller.getByProductId(1L);
        controller.getMovements(1L, PageRequest.of(0, 20));
        controller.recordEntry(1L, new StockEntryRequest(10, "test"));
        controller.recordExit(1L, new StockExitRequest(5, "test"));
        controller.recordAdjustment(1L, new StockAdjustmentRequest(15, "test"));
        controller.updateLimits(1L, new UpdateStockLimitsRequest(5, 100));

        // Verify all service calls were made
        verify(inventoryService).findByProductId(1L);
        verify(inventoryService).getMovements(eq(1L), any(Pageable.class));
        verify(inventoryService).recordEntry(eq(1L), any(StockEntryRequest.class));
        verify(inventoryService).recordExit(eq(1L), any(StockExitRequest.class));
        verify(inventoryService).recordAdjustment(eq(1L), any(StockAdjustmentRequest.class));
        verify(inventoryService).updateLimits(eq(1L), any(UpdateStockLimitsRequest.class));
    }
}