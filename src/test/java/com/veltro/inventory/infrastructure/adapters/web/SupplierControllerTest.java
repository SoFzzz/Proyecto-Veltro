package com.veltro.inventory.infrastructure.adapters.web;

import com.veltro.inventory.application.purchasing.dto.CreateSupplierRequest;
import com.veltro.inventory.application.purchasing.dto.SupplierResponse;
import com.veltro.inventory.application.purchasing.dto.UpdateSupplierRequest;
import com.veltro.inventory.application.purchasing.service.SupplierService;
import com.veltro.inventory.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

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
 * Unit test for {@link SupplierController} (B2-04).
 *
 * Pure unit testing approach using direct method calls instead of MockMvc.
 */
@ExtendWith(MockitoExtension.class)
class SupplierControllerTest {

    @Mock
    private SupplierService supplierService;
    
    private SupplierController controller;

    @BeforeEach
    void setUp() {
        controller = new SupplierController(supplierService);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static SupplierResponse stubSupplier() {
        return new SupplierResponse(
                1L, "Test Supplier Corp", "12345678901",
                "contact@testsupplier.com", "555-1234",
                "123 Business St", "Test notes", true
        );
    }

    // -------------------------------------------------------------------------
    // GET /suppliers — list all
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /suppliers returns 200 with supplier list")
    void findAll_returns200WithList() {
        when(supplierService.findAll()).thenReturn(List.of(stubSupplier()));

        ResponseEntity<List<SupplierResponse>> response = controller.findAll();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).id()).isEqualTo(1L);
        assertThat(response.getBody().get(0).name()).isEqualTo("Test Supplier Corp");
        assertThat(response.getBody().get(0).taxId()).isEqualTo("12345678901");
        verify(supplierService).findAll();
    }

    @Test
    @DisplayName("GET /suppliers returns empty list when no suppliers exist")
    void findAll_noSuppliers_returnsEmptyList() {
        when(supplierService.findAll()).thenReturn(List.of());

        ResponseEntity<List<SupplierResponse>> response = controller.findAll();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEmpty();
        verify(supplierService).findAll();
    }

    // -------------------------------------------------------------------------
    // GET /suppliers/{id}
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /suppliers/{id} returns 200 with supplier when found")
    void findById_existingSupplier_returns200() {
        when(supplierService.findById(1L)).thenReturn(stubSupplier());

        ResponseEntity<SupplierResponse> response = controller.findById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(1L);
        assertThat(response.getBody().name()).isEqualTo("Test Supplier Corp");
        verify(supplierService).findById(1L);
    }

    @Test
    @DisplayName("GET /suppliers/{id} throws NotFoundException when supplier not found")
    void findById_missingSupplier_throwsNotFoundException() {
        when(supplierService.findById(999L))
                .thenThrow(new NotFoundException("Supplier not found with id: 999"));

        assertThatThrownBy(() -> controller.findById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Supplier not found with id: 999");

        verify(supplierService).findById(999L);
    }

    // -------------------------------------------------------------------------
    // GET /suppliers/tax-id/{taxId}
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /suppliers/tax-id/{taxId} returns 200 with supplier when found")
    void findByTaxId_existingTaxId_returns200() {
        when(supplierService.findByTaxId("12345678901")).thenReturn(stubSupplier());

        ResponseEntity<SupplierResponse> response = controller.findByTaxId("12345678901");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().taxId()).isEqualTo("12345678901");
        assertThat(response.getBody().name()).isEqualTo("Test Supplier Corp");
        verify(supplierService).findByTaxId("12345678901");
    }

    @Test
    @DisplayName("GET /suppliers/tax-id/{taxId} throws NotFoundException when tax ID not found")
    void findByTaxId_missingTaxId_throwsNotFoundException() {
        when(supplierService.findByTaxId("INVALID"))
                .thenThrow(new NotFoundException("Supplier not found with tax ID: INVALID"));

        assertThatThrownBy(() -> controller.findByTaxId("INVALID"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Supplier not found with tax ID: INVALID");

        verify(supplierService).findByTaxId("INVALID");
    }

    // -------------------------------------------------------------------------
    // POST /suppliers
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /suppliers with valid request returns 201")
    void create_validRequest_returns201() {
        when(supplierService.create(any(CreateSupplierRequest.class))).thenReturn(stubSupplier());

        CreateSupplierRequest request = new CreateSupplierRequest(
                "Test Supplier Corp", "12345678901",
                "contact@testsupplier.com", "555-1234",
                "123 Business St", "Test notes"
        );
        ResponseEntity<SupplierResponse> response = controller.create(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(1L);
        assertThat(response.getBody().name()).isEqualTo("Test Supplier Corp");
        verify(supplierService).create(any(CreateSupplierRequest.class));
    }

    @Test
    @DisplayName("POST /suppliers delegates to SupplierService")
    void create_delegatesToService() {
        when(supplierService.create(any(CreateSupplierRequest.class))).thenReturn(stubSupplier());

        CreateSupplierRequest request = new CreateSupplierRequest(
                "New Supplier", "98765432109",
                "info@newsupplier.com", "555-9876",
                "456 Supply Ave", "New supplier notes"
        );
        controller.create(request);

        verify(supplierService).create(request);
    }

    // -------------------------------------------------------------------------
    // PUT /suppliers/{id}
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("PUT /suppliers/{id} with valid request returns 200")
    void update_validRequest_returns200() {
        when(supplierService.update(eq(1L), any(UpdateSupplierRequest.class))).thenReturn(stubSupplier());

        UpdateSupplierRequest request = new UpdateSupplierRequest(
                "Updated Supplier Corp",
                "updated@testsupplier.com", "555-1234",
                "123 Updated St", "Updated notes"
        );
        ResponseEntity<SupplierResponse> response = controller.update(1L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(1L);
        verify(supplierService).update(eq(1L), any(UpdateSupplierRequest.class));
    }

    @Test
    @DisplayName("PUT /suppliers/{id} delegates to SupplierService")
    void update_delegatesToService() {
        when(supplierService.update(eq(5L), any(UpdateSupplierRequest.class))).thenReturn(stubSupplier());

        UpdateSupplierRequest request = new UpdateSupplierRequest(
                "Modified Supplier",
                "modified@supplier.com", "555-5555",
                "789 Modified Blvd", "Modified notes"
        );
        controller.update(5L, request);

        verify(supplierService).update(5L, request);
    }

    @Test
    @DisplayName("PUT /suppliers/{id} throws NotFoundException when supplier not found")
    void update_missingSupplier_throwsNotFoundException() {
        doThrow(new NotFoundException("Supplier not found with id: 999"))
                .when(supplierService).update(eq(999L), any(UpdateSupplierRequest.class));

        UpdateSupplierRequest request = new UpdateSupplierRequest(
                "Test Supplier",
                "test@supplier.com", "555-1234",
                "123 Test St", "Test notes"
        );

        assertThatThrownBy(() -> controller.update(999L, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Supplier not found with id: 999");

        verify(supplierService).update(999L, request);
    }

    // -------------------------------------------------------------------------
    // DELETE /suppliers/{id}
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("DELETE /suppliers/{id} returns 204")
    void delete_validId_returns204() {
        doNothing().when(supplierService).delete(1L);

        ResponseEntity<Void> response = controller.delete(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
        verify(supplierService).delete(1L);
    }

    @Test
    @DisplayName("DELETE /suppliers/{id} delegates to SupplierService")
    void delete_delegatesToService() {
        doNothing().when(supplierService).delete(7L);

        controller.delete(7L);

        verify(supplierService).delete(7L);
    }

    @Test
    @DisplayName("DELETE /suppliers/{id} throws NotFoundException when supplier not found")
    void delete_missingSupplier_throwsNotFoundException() {
        doThrow(new NotFoundException("Supplier not found with id: 999"))
                .when(supplierService).delete(999L);

        assertThatThrownBy(() -> controller.delete(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Supplier not found with id: 999");

        verify(supplierService).delete(999L);
    }
}