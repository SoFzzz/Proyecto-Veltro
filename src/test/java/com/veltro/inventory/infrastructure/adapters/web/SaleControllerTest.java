package com.veltro.inventory.infrastructure.adapters.web;

import com.veltro.inventory.application.pos.dto.AddItemRequest;
import com.veltro.inventory.application.pos.dto.ConfirmSaleRequest;
import com.veltro.inventory.application.pos.dto.ModifyItemRequest;
import com.veltro.inventory.application.pos.dto.SaleResponse;
import com.veltro.inventory.application.pos.service.SaleService;
import com.veltro.inventory.domain.pos.model.PaymentMethod;
import com.veltro.inventory.domain.pos.model.SaleStatus;
import com.veltro.inventory.exception.InvalidPaymentException;
import com.veltro.inventory.exception.InvalidStateTransitionException;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link SaleController} (B2-01).
 *
 * Pure unit testing approach using direct method calls instead of MockMvc.
 */
@ExtendWith(MockitoExtension.class)
class SaleControllerTest {

    @Mock
    private SaleService saleService;
    
    private SaleController controller;

    @BeforeEach
    void setUp() {
        controller = new SaleController(saleService);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static SaleResponse stubSaleResponse(Long id, String saleNumber, String status) {
        return new SaleResponse(
                id,
                saleNumber,
                SaleStatus.valueOf(status),
                100L,
                "100.0000",
                "100.0000",
                null,
                null,
                PaymentMethod.CASH,
                null,
                List.of(),
                0L,
                null
        );
    }

    // -------------------------------------------------------------------------
    // POST /sales/start
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /sales/start returns 201 with new sale")
    void startSale_returns201() {
        SaleResponse response = stubSaleResponse(1L, "VLT-2026-000001", "IN_PROGRESS");
        when(saleService.startSale()).thenReturn(response);

        ResponseEntity<SaleResponse> result = controller.startSale();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().id()).isEqualTo(1L);
        assertThat(result.getBody().saleNumber()).isEqualTo("VLT-2026-000001");
        assertThat(result.getBody().status()).isEqualTo(SaleStatus.IN_PROGRESS);
        verify(saleService).startSale();
    }

    @Test
    @DisplayName("POST /sales/start delegates to SaleService")
    void startSale_delegatesToService() {
        SaleResponse response = stubSaleResponse(2L, "VLT-2026-000002", "IN_PROGRESS");
        when(saleService.startSale()).thenReturn(response);

        controller.startSale();

        verify(saleService).startSale();
    }

    // -------------------------------------------------------------------------
    // GET /sales/{id}
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /sales/{id} returns 200 with sale when found")
    void findById_existingSale_returns200() {
        SaleResponse response = stubSaleResponse(1L, "VLT-2026-000001", "IN_PROGRESS");
        when(saleService.findById(1L)).thenReturn(response);

        ResponseEntity<SaleResponse> result = controller.findById(1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().id()).isEqualTo(1L);
        assertThat(result.getBody().saleNumber()).isEqualTo("VLT-2026-000001");
        verify(saleService).findById(1L);
    }

    @Test
    @DisplayName("GET /sales/{id} throws NotFoundException when sale not found")
    void findById_missingSale_throwsNotFoundException() {
        when(saleService.findById(999L)).thenThrow(new NotFoundException("Sale not found with id: 999"));

        assertThatThrownBy(() -> controller.findById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Sale not found with id: 999");

        verify(saleService).findById(999L);
    }

    // -------------------------------------------------------------------------
    // POST /sales/{id}/items
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /sales/{id}/items returns 200 with updated sale")
    void addItem_validRequest_returns200() {
        SaleResponse response = stubSaleResponse(1L, "VLT-2026-000001", "IN_PROGRESS");
        when(saleService.addItem(eq(1L), any(AddItemRequest.class))).thenReturn(response);

        AddItemRequest request = new AddItemRequest(10L, 2);
        ResponseEntity<SaleResponse> result = controller.addItem(1L, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().id()).isEqualTo(1L);
        verify(saleService).addItem(eq(1L), any(AddItemRequest.class));
    }

    @Test
    @DisplayName("POST /sales/{id}/items delegates to SaleService")
    void addItem_delegatesToService() {
        SaleResponse response = stubSaleResponse(1L, "VLT-2026-000001", "IN_PROGRESS");
        when(saleService.addItem(eq(5L), any(AddItemRequest.class))).thenReturn(response);

        AddItemRequest request = new AddItemRequest(15L, 3);
        controller.addItem(5L, request);

        verify(saleService).addItem(5L, request);
    }

    // -------------------------------------------------------------------------
    // PUT /sales/{id}/items/{itemId}
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("PUT /sales/{id}/items/{itemId} returns 200 with updated sale")
    void modifyItem_validRequest_returns200() {
        SaleResponse response = stubSaleResponse(1L, "VLT-2026-000001", "IN_PROGRESS");
        when(saleService.modifyItem(eq(1L), eq(2L), any(ModifyItemRequest.class))).thenReturn(response);

        ModifyItemRequest request = new ModifyItemRequest(5);
        ResponseEntity<SaleResponse> result = controller.modifyItem(1L, 2L, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().id()).isEqualTo(1L);
        verify(saleService).modifyItem(eq(1L), eq(2L), any(ModifyItemRequest.class));
    }

    @Test
    @DisplayName("PUT /sales/{id}/items/{itemId} delegates to SaleService")
    void modifyItem_delegatesToService() {
        SaleResponse response = stubSaleResponse(1L, "VLT-2026-000001", "IN_PROGRESS");
        when(saleService.modifyItem(eq(3L), eq(4L), any(ModifyItemRequest.class))).thenReturn(response);

        ModifyItemRequest request = new ModifyItemRequest(7);
        controller.modifyItem(3L, 4L, request);

        verify(saleService).modifyItem(3L, 4L, request);
    }

    // -------------------------------------------------------------------------
    // DELETE /sales/{id}/items/{itemId}
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("DELETE /sales/{id}/items/{itemId} returns 200 with updated sale")
    void removeItem_validIds_returns200() {
        SaleResponse response = stubSaleResponse(1L, "VLT-2026-000001", "IN_PROGRESS");
        when(saleService.removeItem(1L, 2L)).thenReturn(response);

        ResponseEntity<SaleResponse> result = controller.removeItem(1L, 2L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().id()).isEqualTo(1L);
        verify(saleService).removeItem(1L, 2L);
    }

    @Test
    @DisplayName("DELETE /sales/{id}/items/{itemId} delegates to SaleService")
    void removeItem_delegatesToService() {
        SaleResponse response = stubSaleResponse(1L, "VLT-2026-000001", "IN_PROGRESS");
        when(saleService.removeItem(6L, 8L)).thenReturn(response);

        controller.removeItem(6L, 8L);

        verify(saleService).removeItem(6L, 8L);
    }

    // -------------------------------------------------------------------------
    // POST /sales/{id}/confirm
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /sales/{id}/confirm returns 200 with confirmed sale")
    void confirm_validRequest_returns200() {
        SaleResponse response = stubSaleResponse(1L, "VLT-2026-000001", "COMPLETED");
        when(saleService.confirm(eq(1L), any(ConfirmSaleRequest.class))).thenReturn(response);

        ConfirmSaleRequest request = new ConfirmSaleRequest(PaymentMethod.CASH, new BigDecimal("100.00"));
        ResponseEntity<SaleResponse> result = controller.confirm(1L, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().id()).isEqualTo(1L);
        assertThat(result.getBody().status()).isEqualTo(SaleStatus.COMPLETED);
        verify(saleService).confirm(eq(1L), any(ConfirmSaleRequest.class));
    }

    @Test
    @DisplayName("POST /sales/{id}/confirm delegates to SaleService")
    void confirm_delegatesToService() {
        SaleResponse response = stubSaleResponse(1L, "VLT-2026-000001", "COMPLETED");
        when(saleService.confirm(eq(7L), any(ConfirmSaleRequest.class))).thenReturn(response);

        ConfirmSaleRequest request = new ConfirmSaleRequest(PaymentMethod.CARD, new BigDecimal("150.00"));
        controller.confirm(7L, request);

        verify(saleService).confirm(7L, request);
    }

    @Test
    @DisplayName("POST /sales/{id}/confirm handles service exceptions")
    void confirm_serviceThrowsException_propagatesException() {
        when(saleService.confirm(eq(1L), any(ConfirmSaleRequest.class)))
                .thenThrow(new InvalidPaymentException("Insufficient payment amount"));

        ConfirmSaleRequest request = new ConfirmSaleRequest(PaymentMethod.CASH, new BigDecimal("50.00"));

        assertThatThrownBy(() -> controller.confirm(1L, request))
                .isInstanceOf(InvalidPaymentException.class)
                .hasMessage("Insufficient payment amount");

        verify(saleService).confirm(eq(1L), any(ConfirmSaleRequest.class));
    }

    // -------------------------------------------------------------------------
    // POST /sales/{id}/void
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /sales/{id}/void returns 200 with voided sale")
    void voidSale_validId_returns200() {
        SaleResponse response = stubSaleResponse(1L, "VLT-2026-000001", "VOIDED");
        when(saleService.voidSale(1L)).thenReturn(response);

        ResponseEntity<SaleResponse> result = controller.voidSale(1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().id()).isEqualTo(1L);
        assertThat(result.getBody().status()).isEqualTo(SaleStatus.VOIDED);
        verify(saleService).voidSale(1L);
    }

    @Test
    @DisplayName("POST /sales/{id}/void delegates to SaleService")
    void voidSale_delegatesToService() {
        SaleResponse response = stubSaleResponse(1L, "VLT-2026-000001", "VOIDED");
        when(saleService.voidSale(9L)).thenReturn(response);

        controller.voidSale(9L);

        verify(saleService).voidSale(9L);
    }

    @Test
    @DisplayName("POST /sales/{id}/void handles service exceptions")
    void voidSale_serviceThrowsException_propagatesException() {
        when(saleService.voidSale(1L))
                .thenThrow(new InvalidStateTransitionException("Cannot void a sale that is not completed"));

        assertThatThrownBy(() -> controller.voidSale(1L))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessage("Cannot void a sale that is not completed");

        verify(saleService).voidSale(1L);
    }
}