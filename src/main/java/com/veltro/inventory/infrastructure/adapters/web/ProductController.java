package com.veltro.inventory.infrastructure.adapters.web;

import com.veltro.inventory.application.catalog.dto.CreateProductRequest;
import com.veltro.inventory.application.catalog.dto.ProductResponse;
import com.veltro.inventory.application.catalog.dto.UpdateProductRequest;
import com.veltro.inventory.application.catalog.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for product catalog management (B1-03).
 *
 * Role rules (RF-12):
 * <ul>
 *   <li>GET /products and GET /products/{id}: ADMIN, WAREHOUSE, CASHIER.</li>
 *   <li>GET /products/barcode/{barcode}: any authenticated user (primary POS path — UC-01).</li>
 *   <li>POST / PUT: ADMIN or WAREHOUSE only.</li>
 * </ul>
 *
 * AC-07: {@code GET /products} returns a {@link Page} with pagination metadata.
 */
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // -------------------------------------------------------------------------
    // GET endpoints
    // -------------------------------------------------------------------------

    /**
     * Paginated product listing (AC-07).
     * Defaults: page=0, size=20, sort=id,asc.
     */
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> listProducts(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(productService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    /**
     * Barcode lookup — the primary endpoint called by the POS scanner (UC-01).
     * Returns 404 when no active product matches the barcode.
     */
    @GetMapping("/barcode/{barcode}")
    public ResponseEntity<ProductResponse> findByBarcode(@PathVariable String barcode) {
        return ResponseEntity.ok(productService.findByBarcode(barcode));
    }

    // -------------------------------------------------------------------------
    // POST / PUT endpoints — ADMIN or WAREHOUSE only
    // -------------------------------------------------------------------------

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    public ResponseEntity<ProductResponse> create(
            @Valid @RequestBody CreateProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    public ResponseEntity<ProductResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request) {
        return ResponseEntity.ok(productService.update(id, request));
    }

    /**
     * Soft-deletes a product (AC-05). Sets {@code active=false}; the record
     * is preserved in the database for audit, sales history, and purchase orders.
     */
    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        productService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
