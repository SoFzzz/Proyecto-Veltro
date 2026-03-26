package com.veltro.inventory.domain.audit.model;

/**
 * Type of entity being audited.
 * 
 * <p>Phase 3 (B3-03) — Forensic Audit initial scope:
 * <ul>
 *   <li>SALE — sale confirmations and voidings</li>
 *   <li>PURCHASE_ORDER — order receptions and voidings</li>
 *   <li>INVENTORY — manual stock adjustments</li>
 * </ul>
 * 
 * <p>Extensible to other entities (PRODUCT, USER, SUPPLIER) in future phases.
 */
public enum AuditEntityType {
    SALE,
    PURCHASE_ORDER,
    INVENTORY
}
