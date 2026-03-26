package com.veltro.inventory.domain.iam.model;

/**
 * Roles available in the Veltro system (RF-12).
 *
 * <ul>
 *   <li>ADMIN — full access: user management, sale voiding, reports, audit.</li>
 *   <li>WAREHOUSE — product and supplier CRUD, purchase orders, inventory movements.</li>
 *   <li>CASHIER — scan products, register sales, check stock, view own sales.</li>
 * </ul>
 */
public enum Role {
    ADMIN,
    WAREHOUSE,
    CASHIER
}
