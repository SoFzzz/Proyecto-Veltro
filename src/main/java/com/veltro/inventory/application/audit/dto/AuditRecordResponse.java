package com.veltro.inventory.application.audit.dto;

import com.veltro.inventory.domain.audit.model.AuditAction;
import com.veltro.inventory.domain.audit.model.AuditEntityType;

import java.time.Instant;

/**
 * Response DTO for audit records (B3-03).
 * 
 * <p>Exposes audit record data to ADMIN users via REST API.
 * JSON fields (previousData, newData) are returned as raw strings for frontend
 * to parse and display (e.g., with a diff viewer).
 * 
 * @param id audit record ID
 * @param entityType type of entity that was modified
 * @param entityId ID of the modified entity
 * @param action action performed
 * @param previousData JSON snapshot before operation (nullable)
 * @param newData JSON snapshot after operation (nullable)
 * @param username username of the actor
 * @param ipAddress client IP address (nullable)
 * @param createdAt timestamp when the audit record was created
 */
public record AuditRecordResponse(
        Long id,
        AuditEntityType entityType,
        Long entityId,
        AuditAction action,
        String previousData,
        String newData,
        String username,
        String ipAddress,
        Instant createdAt
) {
}
