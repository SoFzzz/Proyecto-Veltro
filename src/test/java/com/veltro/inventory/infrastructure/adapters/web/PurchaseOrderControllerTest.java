package com.veltro.inventory.infrastructure.adapters.web;

import com.veltro.inventory.application.purchasing.dto.AddOrderItemRequest;
import com.veltro.inventory.application.purchasing.dto.CreatePurchaseOrderRequest;
import com.veltro.inventory.application.purchasing.dto.PurchaseOrderResponse;
import com.veltro.inventory.application.purchasing.service.PurchaseOrderService;
import com.veltro.inventory.application.shared.dto.AuditInfo;
import com.veltro.inventory.domain.purchasing.model.PurchaseOrderStatus;
import com.veltro.inventory.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link PurchaseOrderController} (B2-04).
 *
 * Pure unit testing approach using direct method calls instead of MockMvc.
 */
@ExtendWith(MockitoExtension.class)
class PurchaseOrderControllerTest {

    @Mock
    private PurchaseOrderService purchaseOrderService;
    
    private PurchaseOrderController controller;

    @BeforeEach
    void setUp() {
        controller = new PurchaseOrderController(purchaseOrderService);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static PurchaseOrderResponse stubPurchaseOrder() {
        AuditInfo auditInfo = new AuditInfo(
                LocalDateTime.now(), "system",
                LocalDateTime.now(), "system"
        );
        return new PurchaseOrderResponse(
                1L, "PO-2026-000001", PurchaseOrderStatus.PENDING,
                1L, "Test Supplier Corp", "127.50", "Test notes",
                List.of(), 1L, auditInfo
        );
    }

    // -------------------------------------------------------------------------
    // GET /purchase-orders — list all
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /purchase-orders returns 200 with order list")
    void findAll_returns200WithList() {
        when(purchaseOrderService.findAll()).thenReturn(List.of(stubPurchaseOrder()));

        ResponseEntity<List<PurchaseOrderResponse>> response = controller.findAll();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).id()).isEqualTo(1L);
        assertThat(response.getBody().get(0).orderNumber()).isEqualTo("PO-2026-000001");
        verify(purchaseOrderService).findAll();
    }

    @Test
    @DisplayName("GET /purchase-orders returns empty list when no orders exist")
    void findAll_noOrders_returnsEmptyList() {
        when(purchaseOrderService.findAll()).thenReturn(List.of());

        ResponseEntity<List<PurchaseOrderResponse>> response = controller.findAll();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEmpty();
        verify(purchaseOrderService).findAll();
    }

    // -------------------------------------------------------------------------
    // GET /purchase-orders?supplierId={id}
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /purchase-orders?supplierId=1 returns 200 with supplier orders")
    void findBySupplier_returns200WithSupplierOrders() {
        when(purchaseOrderService.findBySupplier(1L)).thenReturn(List.of(stubPurchaseOrder()));

        ResponseEntity<List<PurchaseOrderResponse>> response = controller.findBySupplier(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).supplierId()).isEqualTo(1L);
        verify(purchaseOrderService).findBySupplier(1L);
    }

    @Test
    @DisplayName("GET /purchase-orders?supplierId=999 returns empty list for non-existing supplier")
    void findBySupplier_nonExistingSupplier_returnsEmptyList() {
        when(purchaseOrderService.findBySupplier(999L)).thenReturn(List.of());

        ResponseEntity<List<PurchaseOrderResponse>> response = controller.findBySupplier(999L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEmpty();
        verify(purchaseOrderService).findBySupplier(999L);
    }

    // -------------------------------------------------------------------------
    // GET /purchase-orders/{id}
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /purchase-orders/{id} returns 200 with order when found")
    void findById_existingOrder_returns200() {
        when(purchaseOrderService.findById(1L)).thenReturn(stubPurchaseOrder());

        ResponseEntity<PurchaseOrderResponse> response = controller.findById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(1L);
        assertThat(response.getBody().orderNumber()).isEqualTo("PO-2026-000001");
        verify(purchaseOrderService).findById(1L);
    }

    @Test
    @DisplayName("GET /purchase-orders/{id} throws NotFoundException when order not found")
    void findById_missingOrder_throwsNotFoundException() {
        when(purchaseOrderService.findById(999L))
                .thenThrow(new NotFoundException("Purchase order not found with id: 999"));

        assertThatThrownBy(() -> controller.findById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Purchase order not found with id: 999");

        verify(purchaseOrderService).findById(999L);
    }

    // -------------------------------------------------------------------------
    // GET /purchase-orders/number/{orderNumber}
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /purchase-orders/number/{orderNumber} returns 200 with order when found")
    void findByOrderNumber_existingOrder_returns200() {
        when(purchaseOrderService.findByOrderNumber("PO-2026-000001")).thenReturn(stubPurchaseOrder());

        ResponseEntity<PurchaseOrderResponse> response = controller.findByOrderNumber("PO-2026-000001");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().orderNumber()).isEqualTo("PO-2026-000001");
        verify(purchaseOrderService).findByOrderNumber("PO-2026-000001");
    }

    @Test
    @DisplayName("GET /purchase-orders/number/{orderNumber} throws NotFoundException when order not found")
    void findByOrderNumber_missingOrder_throwsNotFoundException() {
        when(purchaseOrderService.findByOrderNumber("INVALID"))
                .thenThrow(new NotFoundException("Purchase order not found with number: INVALID"));

        assertThatThrownBy(() -> controller.findByOrderNumber("INVALID"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Purchase order not found with number: INVALID");

        verify(purchaseOrderService).findByOrderNumber("INVALID");
    }

    // -------------------------------------------------------------------------
    // POST /purchase-orders
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /purchase-orders with valid request returns 201")
    void create_validRequest_returns201() {
        when(purchaseOrderService.create(any(CreatePurchaseOrderRequest.class))).thenReturn(stubPurchaseOrder());

        CreatePurchaseOrderRequest request = new CreatePurchaseOrderRequest(1L, "Test purchase order");
        ResponseEntity<PurchaseOrderResponse> response = controller.create(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(1L);
        verify(purchaseOrderService).create(any(CreatePurchaseOrderRequest.class));
    }

    @Test
    @DisplayName("POST /purchase-orders delegates to PurchaseOrderService")
    void create_delegatesToService() {
        when(purchaseOrderService.create(any(CreatePurchaseOrderRequest.class))).thenReturn(stubPurchaseOrder());

        CreatePurchaseOrderRequest request = new CreatePurchaseOrderRequest(2L, "Another order");
        controller.create(request);

        verify(purchaseOrderService).create(request);
    }

    // -------------------------------------------------------------------------
    // POST /purchase-orders/{orderId}/items
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /purchase-orders/{orderId}/items returns 200 with updated order")
    void addItem_validRequest_returns200() {
        when(purchaseOrderService.addItem(eq(1L), any(AddOrderItemRequest.class))).thenReturn(stubPurchaseOrder());

        AddOrderItemRequest request = new AddOrderItemRequest(10L, 5, new BigDecimal("12.50"));
        ResponseEntity<PurchaseOrderResponse> response = controller.addItem(1L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(1L);
        verify(purchaseOrderService).addItem(eq(1L), any(AddOrderItemRequest.class));
    }

    @Test
    @DisplayName("POST /purchase-orders/{orderId}/items delegates to PurchaseOrderService")
    void addItem_delegatesToService() {
        when(purchaseOrderService.addItem(eq(5L), any(AddOrderItemRequest.class))).thenReturn(stubPurchaseOrder());

        AddOrderItemRequest request = new AddOrderItemRequest(15L, 3, new BigDecimal("8.75"));
        controller.addItem(5L, request);

        verify(purchaseOrderService).addItem(5L, request);
    }

    // -------------------------------------------------------------------------
    // POST /purchase-orders/{sourceOrderId}/clone
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /purchase-orders/{sourceOrderId}/clone returns 201 with cloned order")
    void cloneOrder_validId_returns201() {
        when(purchaseOrderService.cloneOrder(1L)).thenReturn(stubPurchaseOrder());

        ResponseEntity<PurchaseOrderResponse> response = controller.cloneOrder(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        verify(purchaseOrderService).cloneOrder(1L);
    }

    @Test
    @DisplayName("POST /purchase-orders/{sourceOrderId}/clone delegates to PurchaseOrderService")
    void cloneOrder_delegatesToService() {
        when(purchaseOrderService.cloneOrder(7L)).thenReturn(stubPurchaseOrder());

        controller.cloneOrder(7L);

        verify(purchaseOrderService).cloneOrder(7L);
    }

    // -------------------------------------------------------------------------
    // PUT /purchase-orders/{orderId}/receive
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("PUT /purchase-orders/{orderId}/receive returns 200 with updated order")
    void markAsReceived_validId_returns200() {
        when(purchaseOrderService.markAsReceived(1L)).thenReturn(stubPurchaseOrder());

        ResponseEntity<PurchaseOrderResponse> response = controller.markAsReceived(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        verify(purchaseOrderService).markAsReceived(1L);
    }

    @Test
    @DisplayName("PUT /purchase-orders/{orderId}/receive delegates to PurchaseOrderService")
    void markAsReceived_delegatesToService() {
        when(purchaseOrderService.markAsReceived(3L)).thenReturn(stubPurchaseOrder());

        controller.markAsReceived(3L);

        verify(purchaseOrderService).markAsReceived(3L);
    }

    // -------------------------------------------------------------------------
    // PUT /purchase-orders/{orderId}/void
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("PUT /purchase-orders/{orderId}/void returns 200 with voided order")
    void voidOrder_validId_returns200() {
        when(purchaseOrderService.voidOrder(1L)).thenReturn(stubPurchaseOrder());

        ResponseEntity<PurchaseOrderResponse> response = controller.voidOrder(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        verify(purchaseOrderService).voidOrder(1L);
    }

    @Test
    @DisplayName("PUT /purchase-orders/{orderId}/void delegates to PurchaseOrderService")
    void voidOrder_delegatesToService() {
        when(purchaseOrderService.voidOrder(4L)).thenReturn(stubPurchaseOrder());

        controller.voidOrder(4L);

        verify(purchaseOrderService).voidOrder(4L);
    }

    // -------------------------------------------------------------------------
    // DELETE /purchase-orders/{orderId}/items/{detailId}
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("DELETE /purchase-orders/{orderId}/items/{detailId} returns 200 with updated order")
    void removeItem_validIds_returns200() {
        when(purchaseOrderService.removeItem(1L, 2L)).thenReturn(stubPurchaseOrder());

        ResponseEntity<PurchaseOrderResponse> response = controller.removeItem(1L, 2L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        verify(purchaseOrderService).removeItem(1L, 2L);
    }

    @Test
    @DisplayName("DELETE /purchase-orders/{orderId}/items/{detailId} delegates to PurchaseOrderService")
    void removeItem_delegatesToService() {
        when(purchaseOrderService.removeItem(6L, 8L)).thenReturn(stubPurchaseOrder());

        controller.removeItem(6L, 8L);

        verify(purchaseOrderService).removeItem(6L, 8L);
    }

    @Test
    @DisplayName("DELETE /purchase-orders/{orderId}/items/{detailId} throws NotFoundException when item not found")
    void removeItem_missingItem_throwsNotFoundException() {
        doThrow(new NotFoundException("Order item not found"))
                .when(purchaseOrderService).removeItem(1L, 999L);

        assertThatThrownBy(() -> controller.removeItem(1L, 999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Order item not found");

        verify(purchaseOrderService).removeItem(1L, 999L);
    }
}