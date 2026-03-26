package com.veltro.inventory.infrastructure.adapters.web;

import com.veltro.inventory.application.purchasing.dto.CreateSupplierRequest;
import com.veltro.inventory.application.purchasing.dto.SupplierResponse;
import com.veltro.inventory.application.purchasing.dto.UpdateSupplierRequest;
import com.veltro.inventory.application.purchasing.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for supplier management (B2-04).
 *
 * <p>Role-based access control:
 * <ul>
 *   <li>GET endpoints: ADMIN, WAREHOUSE (for viewing supplier information)</li>
 *   <li>POST/PUT/DELETE: ADMIN only (supplier management is admin-only)</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    // -------------------------------------------------------------------------
    // GET endpoints
    // -------------------------------------------------------------------------

    /**
     * Returns all active suppliers.
     *
     * @return list of suppliers
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    public ResponseEntity<List<SupplierResponse>> findAll() {
        List<SupplierResponse> suppliers = supplierService.findAll();
        return ResponseEntity.ok(suppliers);
    }

    /**
     * Returns a supplier by ID.
     *
     * @param id supplier ID
     * @return supplier response
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    public ResponseEntity<SupplierResponse> findById(@PathVariable Long id) {
        SupplierResponse supplier = supplierService.findById(id);
        return ResponseEntity.ok(supplier);
    }

    /**
     * Returns a supplier by tax ID.
     *
     * @param taxId tax ID
     * @return supplier response
     */
    @GetMapping("/tax-id/{taxId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    public ResponseEntity<SupplierResponse> findByTaxId(@PathVariable String taxId) {
        SupplierResponse supplier = supplierService.findByTaxId(taxId);
        return ResponseEntity.ok(supplier);
    }

    // -------------------------------------------------------------------------
    // POST endpoints
    // -------------------------------------------------------------------------

    /**
     * Creates a new supplier.
     *
     * @param request create supplier request
     * @return created supplier response
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SupplierResponse> create(@Valid @RequestBody CreateSupplierRequest request) {
        SupplierResponse supplier = supplierService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(supplier);
    }

    // -------------------------------------------------------------------------
    // PUT endpoints
    // -------------------------------------------------------------------------

    /**
     * Updates an existing supplier.
     *
     * @param id supplier ID
     * @param request update supplier request
     * @return updated supplier response
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SupplierResponse> update(@PathVariable Long id, 
                                                   @Valid @RequestBody UpdateSupplierRequest request) {
        SupplierResponse supplier = supplierService.update(id, request);
        return ResponseEntity.ok(supplier);
    }

    // -------------------------------------------------------------------------
    // DELETE endpoints
    // -------------------------------------------------------------------------

    /**
     * Soft deletes a supplier (sets active = false).
     *
     * @param id supplier ID
     * @return no content response
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        supplierService.delete(id);
        return ResponseEntity.noContent().build();
    }
}