package com.veltro.inventory.domain.purchasing.ports;

import com.veltro.inventory.domain.purchasing.model.SupplierEntity;

import java.util.List;
import java.util.Optional;

/**
 * Output port for {@link SupplierEntity} persistence (B2-04).
 *
 * <p><b>IMPORTANT:</b> Pure Java interface — ZERO Spring imports allowed (domain layer restriction).
 */
public interface SupplierRepository {

    /**
     * Saves a supplier entity.
     *
     * @param supplier the supplier to save
     * @return the saved supplier
     */
    SupplierEntity save(SupplierEntity supplier);

    /**
     * Finds a supplier by ID where active=true.
     *
     * @param id the supplier ID
     * @return the supplier if found and active
     */
    Optional<SupplierEntity> findByIdAndActiveTrue(Long id);

    /**
     * Finds a supplier by tax ID where active=true.
     *
     * @param taxId the supplier tax ID
     * @return the supplier if found and active
     */
    Optional<SupplierEntity> findByTaxIdAndActiveTrue(String taxId);

    /**
     * Finds all active suppliers.
     *
     * @return list of active suppliers
     */
    List<SupplierEntity> findAllByActiveTrue();

    /**
     * Checks if a tax ID already exists for an active supplier (excluding the given ID).
     *
     * @param taxId the tax ID to check
     * @param excludeId the ID to exclude from the check (null for new suppliers)
     * @return true if tax ID exists
     */
    boolean existsByTaxIdAndActiveTrueAndIdNot(String taxId, Long excludeId);
}