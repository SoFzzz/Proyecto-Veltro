package com.veltro.inventory.infrastructure.adapters.persistence;

import com.veltro.inventory.domain.purchasing.model.SupplierEntity;
import com.veltro.inventory.domain.purchasing.ports.SupplierRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * JPA repository for {@link SupplierEntity} (B2-04).
 *
 * <p>Extends the domain {@link SupplierRepository} port and provides
 * query methods with Spring Data JPA.
 */
@Repository
public interface SupplierJpaRepository extends JpaRepository<SupplierEntity, Long>, SupplierRepository {

    /**
     * Checks if a tax ID already exists for an active supplier (excluding the given ID).
     *
     * @param taxId the tax ID to check
     * @param excludeId the ID to exclude from the check (null for new suppliers)
     * @return true if tax ID exists
     */
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM SupplierEntity s " +
           "WHERE s.taxId = :taxId AND s.active = true AND (:excludeId IS NULL OR s.id != :excludeId)")
    boolean existsByTaxIdAndActiveTrueAndIdNot(@Param("taxId") String taxId, @Param("excludeId") Long excludeId);
}