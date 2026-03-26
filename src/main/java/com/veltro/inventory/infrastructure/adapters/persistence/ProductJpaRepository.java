package com.veltro.inventory.infrastructure.adapters.persistence;

import com.veltro.inventory.domain.catalog.model.ProductEntity;
import com.veltro.inventory.domain.catalog.ports.ProductRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA adapter for {@link ProductRepository}.
 *
 * {@code findByBarcodeAndActiveTrue} uses the B-Tree index created in
 * V1 migration ({@code idx_products_barcode}) for O(log n) lookups.
 */
@Repository
public interface ProductJpaRepository
        extends JpaRepository<ProductEntity, Long>, ProductRepository {
}
