package com.veltro.inventory.domain.pos.ports;

import com.veltro.inventory.domain.pos.model.SaleDetailEntity;

import java.util.Optional;

/**
 * Output port for {@link SaleDetailEntity} persistence (B2-01).
 *
 * <p><b>IMPORTANT:</b> Pure Java interface — ZERO Spring imports allowed (domain layer restriction).
 * <p><b>NOTE:</b> This repository does NOT have a delete method — soft delete (AC-05) is enforced via active=false.
 */
public interface SaleDetailRepository {

    /**
     * Saves a sale detail entity.
     *
     * @param detail the detail to save
     * @return the saved detail
     */
    SaleDetailEntity save(SaleDetailEntity detail);

    /**
     * Finds a sale detail by ID, sale ID, and active=true.
     *
     * @param id     the detail ID
     * @param saleId the sale ID
     * @return the detail if found and active
     */
    Optional<SaleDetailEntity> findByIdAndSaleIdAndActiveTrue(Long id, Long saleId);
}
