package com.veltro.inventory.infrastructure.adapters.persistence;

import com.veltro.inventory.domain.pos.model.SaleDetailEntity;
import com.veltro.inventory.domain.pos.ports.SaleDetailRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA repository for {@link SaleDetailEntity} (B2-01).
 *
 * <p>Extends the domain {@link SaleDetailRepository} port.
 * <b>NOTE:</b> This repository does NOT have a delete method — soft delete (AC-05)
 * is enforced via {@code active=false} in the domain layer.
 */
@Repository
public interface SaleDetailJpaRepository extends JpaRepository<SaleDetailEntity, Long>, SaleDetailRepository {
    // No additional methods needed — port interface provides all required queries
}
