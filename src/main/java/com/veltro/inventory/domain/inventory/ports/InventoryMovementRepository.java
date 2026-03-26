package com.veltro.inventory.domain.inventory.ports;

import com.veltro.inventory.domain.inventory.model.InventoryMovementEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Output port for inventory movement persistence.
 *
 * Movements are append-only — no update or delete operations are exposed.
 * Implemented by {@code InventoryMovementJpaRepository} in the infrastructure layer.
 */
public interface InventoryMovementRepository {

    InventoryMovementEntity save(InventoryMovementEntity movement);

    /** Returns a paginated history of movements for a given inventory record (AC-07). */
    Page<InventoryMovementEntity> findByInventoryId(Long inventoryId, Pageable pageable);
}
