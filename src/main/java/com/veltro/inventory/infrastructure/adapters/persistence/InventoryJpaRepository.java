package com.veltro.inventory.infrastructure.adapters.persistence;

import com.veltro.inventory.domain.inventory.model.InventoryEntity;
import com.veltro.inventory.domain.inventory.ports.InventoryRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA adapter implementing the {@link InventoryRepository} output port.
 *
 * Spring Data JPA derives all declared query methods from method names.
 * The {@code findByProductIdAndActiveTrue} method uses the UNIQUE index on
 * {@code inventory.product_id} created in V2 migration — effectively an
 * index scan on every lookup.
 */
@Repository
public interface InventoryJpaRepository
        extends JpaRepository<InventoryEntity, Long>, InventoryRepository {
}
