package com.veltro.inventory.infrastructure.adapters.persistence;

import com.veltro.inventory.domain.pos.model.SaleEntity;
import com.veltro.inventory.domain.pos.ports.SaleRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * JPA repository for {@link SaleEntity} (B2-01).
 *
 * <p>Extends the domain {@link SaleRepository} port and provides
 * a native query to fetch the next value from the PostgreSQL sequence.
 */
@Repository
public interface SaleJpaRepository extends JpaRepository<SaleEntity, Long>, SaleRepository {

    /**
     * Fetches the next value from the sale_number_seq PostgreSQL sequence.
     *
     * @return the next sequence value
     */
    @Query(value = "SELECT nextval('sale_number_seq')", nativeQuery = true)
    Long getNextSaleSequenceValue();
}
