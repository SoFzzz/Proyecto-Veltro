package com.veltro.inventory.infrastructure.adapters.persistence;

import com.veltro.inventory.domain.purchasing.model.PurchaseOrderEntity;
import com.veltro.inventory.domain.purchasing.ports.PurchaseOrderRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * JPA repository for {@link PurchaseOrderEntity} (B2-04).
 *
 * <p>Extends the domain {@link PurchaseOrderRepository} port and provides
 * a native query to fetch the next value from the PostgreSQL sequence.
 */
@Repository
public interface PurchaseOrderJpaRepository extends JpaRepository<PurchaseOrderEntity, Long>, PurchaseOrderRepository {

    /**
     * Fetches the next value from the purchase_order_number_seq PostgreSQL sequence.
     *
     * @return the next sequence value
     */
    @Query(value = "SELECT nextval('purchase_order_number_seq')", nativeQuery = true)
    Long getNextOrderSequenceValue();
}