package com.veltro.inventory.infrastructure.adapters.persistence;

import com.veltro.inventory.domain.inventory.model.InventoryMovementEntity;
import com.veltro.inventory.domain.inventory.ports.InventoryMovementRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA adapter implementing the {@link InventoryMovementRepository} output port.
 *
 * {@code findByInventoryId} uses the B-Tree index
 * {@code idx_movements_inventory} created in V2 migration.
 */
@Repository
public interface InventoryMovementJpaRepository
        extends JpaRepository<InventoryMovementEntity, Long>, InventoryMovementRepository {
}
