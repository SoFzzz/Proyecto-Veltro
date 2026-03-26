package com.veltro.inventory.domain.catalog.ports;

import com.veltro.inventory.domain.catalog.model.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Output port for product persistence.
 *
 * Pure Java interface — zero Spring infrastructure imports, except for Spring Data
 * pagination types which are value types and permitted in the domain port boundary.
 * Implemented by {@code ProductJpaRepository} in the infrastructure layer.
 */
public interface ProductRepository {

    Optional<ProductEntity> findByIdAndActiveTrue(Long id);

    Optional<ProductEntity> findByBarcodeAndActiveTrue(String barcode);

    /** Returns a paginated slice of active products (AC-07). */
    Page<ProductEntity> findAllByActiveTrue(Pageable pageable);

    ProductEntity save(ProductEntity product);
}
