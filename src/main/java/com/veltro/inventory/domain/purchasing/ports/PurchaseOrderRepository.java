package com.veltro.inventory.domain.purchasing.ports;

import com.veltro.inventory.domain.purchasing.model.PurchaseOrderEntity;

import java.util.List;
import java.util.Optional;

/**
 * Output port for {@link PurchaseOrderEntity} persistence (B2-04).
 *
 * <p><b>IMPORTANT:</b> Pure Java interface — ZERO Spring imports allowed (domain layer restriction).
 */
public interface PurchaseOrderRepository {

    /**
     * Saves a purchase order entity.
     *
     * @param order the purchase order to save
     * @return the saved purchase order
     */
    PurchaseOrderEntity save(PurchaseOrderEntity order);

    /**
     * Finds a purchase order by ID where active=true.
     *
     * @param id the purchase order ID
     * @return the purchase order if found and active
     */
    Optional<PurchaseOrderEntity> findByIdAndActiveTrue(Long id);

    /**
     * Finds a purchase order by order number where active=true.
     *
     * @param orderNumber the order number
     * @return the purchase order if found and active
     */
    Optional<PurchaseOrderEntity> findByOrderNumberAndActiveTrue(String orderNumber);

    /**
     * Finds all active purchase orders for a supplier.
     *
     * @param supplierId the supplier ID
     * @return list of active purchase orders
     */
    List<PurchaseOrderEntity> findBySupplierIdAndActiveTrue(Long supplierId);

    /**
     * Finds all active purchase orders.
     *
     * @return list of active purchase orders
     */
    List<PurchaseOrderEntity> findAllByActiveTrue();

    /**
     * Gets the next value from the purchase_order_number_seq PostgreSQL sequence.
     *
     * @return the next sequence value
     */
    Long getNextOrderSequenceValue();
}