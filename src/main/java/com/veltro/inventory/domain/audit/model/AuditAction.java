package com.veltro.inventory.domain.audit.model;

/**
 * Type of action performed on the audited entity.
 * 
 * <p>Phase 3 (B3-03) — Forensic Audit initial actions:
 * <ul>
 *   <li>CONFIRM — Sale confirmation</li>
 *   <li>VOID — Sale or Purchase Order voiding</li>
 *   <li>RECEIVE — Purchase Order reception</li>
 *   <li>ADJUST — Manual inventory adjustment</li>
 * </ul>
 * 
 * <p>Extensible to CREATE, UPDATE, DELETE for other entities in future phases.
 */
public enum AuditAction {
    CONFIRM,
    VOID,
    RECEIVE,
    ADJUST
}
