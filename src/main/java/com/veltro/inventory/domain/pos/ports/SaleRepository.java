package com.veltro.inventory.domain.pos.ports;

import com.veltro.inventory.domain.pos.model.SaleEntity;

import java.util.Optional;

/**
 * Output port for {@link SaleEntity} persistence (B2-01).
 *
 * <p><b>IMPORTANT:</b> Pure Java interface — ZERO Spring imports allowed (domain layer restriction).
 */
public interface SaleRepository {

    /**
     * Saves a sale entity.
     *
     * @param sale the sale to save
     * @return the saved sale
     */
    SaleEntity save(SaleEntity sale);

    /**
     * Finds a sale by ID where active=true.
     *
     * @param id the sale ID
     * @return the sale if found and active
     */
    Optional<SaleEntity> findByIdAndActiveTrue(Long id);

    /**
     * Gets the next value from the sale_number_seq PostgreSQL sequence.
     *
     * @return the next sequence value
     */
    Long getNextSaleSequenceValue();
}
