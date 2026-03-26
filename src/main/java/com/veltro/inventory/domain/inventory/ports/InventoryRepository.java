package com.veltro.inventory.domain.inventory.ports;

import com.veltro.inventory.domain.inventory.model.InventoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Output port for inventory persistence.
 *
 * Pure Java interface — no Spring infrastructure imports beyond the Spring Data
 * pagination value types permitted at the domain port boundary.
 * Implemented by {@code InventoryJpaRepository} in the infrastructure layer.
 */
public interface InventoryRepository {

    Optional<InventoryEntity> findByIdAndActiveTrue(Long id);

    Optional<InventoryEntity> findByProductIdAndActiveTrue(Long productId);

    /** Returns a paginated slice of active inventory records (AC-07). */
    Page<InventoryEntity> findAllByActiveTrue(Pageable pageable);

    InventoryEntity save(InventoryEntity inventory);
}
