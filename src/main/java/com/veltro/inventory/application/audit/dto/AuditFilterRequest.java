package com.veltro.inventory.application.audit.dto;

import com.veltro.inventory.domain.audit.model.AuditAction;
import com.veltro.inventory.domain.audit.model.AuditEntityType;

import java.time.Instant;

/**
 * Filter request for querying audit records (B3-03).
 * 
 * <p>All fields are optional. Null values are ignored by the repository query.
 * 
 * @param entityType optional entity type filter (SALE, PURCHASE_ORDER, INVENTORY)
 * @param action optional action filter (CONFIRM, VOID, RECEIVE, ADJUST)
 * @param username optional username filter
 * @param startDate optional start date (inclusive)
 * @param endDate optional end date (inclusive)
 */
public record AuditFilterRequest(
        AuditEntityType entityType,
        AuditAction action,
        String username,
        Instant startDate,
        Instant endDate
) {
    /**
     * Creates an empty filter (returns all records).
     */
    public static AuditFilterRequest empty() {
        return new AuditFilterRequest(null, null, null, null, null);
    }
}
