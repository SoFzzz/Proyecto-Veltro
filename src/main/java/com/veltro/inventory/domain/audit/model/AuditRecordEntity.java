package com.veltro.inventory.domain.audit.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * Append-only forensic audit trail for critical operations (B3-03).
 *
 * <p>This entity is intentionally NOT a subclass of AbstractAuditableEntity because:
 * <ul>
 *   <li>No update lifecycle — {@code updatedAt}/{@code updatedBy} are not needed</li>
 *   <li>No soft-delete — {@code active} flag is not needed</li>
 *   <li>No {@code @CreatedBy} — {@code username} field manually set from SecurityContextHolder</li>
 * </ul>
 *
 * <p>Audit records are immutable once persisted. They capture before/after JSON snapshots
 * of critical operations (Sale confirm/void, PurchaseOrder receive/void, Inventory adjustments).
 *
 * <p>Username retrieval follows the same pattern as {@code VeltroAuditorAware}: reads from
 * {@code SecurityContextHolder}, falls back to "SYSTEM" for unauthenticated contexts.
 *
 * <p>IP address is captured from {@code HttpServletRequest} in controllers and passed via
 * {@code AuditContext}.
 *
 * @see com.veltro.inventory.application.audit.command.AuditCommandExecutor
 * @see com.veltro.inventory.infrastructure.adapters.config.VeltroAuditorAware
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "audit_record")
@EntityListeners(AuditingEntityListener.class)
public class AuditRecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 50)
    private AuditEntityType entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 50)
    private AuditAction action;

    /**
     * JSON snapshot of entity state BEFORE the operation.
     * Nullable for CREATE actions.
     */
    @Column(name = "previous_data", columnDefinition = "TEXT")
    private String previousData;

    /**
     * JSON snapshot of entity state AFTER the operation.
     * Nullable for DELETE actions.
     */
    @Column(name = "new_data", columnDefinition = "TEXT")
    private String newData;

    /**
     * Username of the actor who performed the operation.
     * Set manually from {@code SecurityContextHolder} in {@code AuditCommandExecutor}.
     * Falls back to "SYSTEM" for unauthenticated contexts.
     */
    @Column(name = "username", nullable = false, length = 100)
    private String username;

    /**
     * Client IP address (IPv4 or IPv6).
     * Captured from {@code HttpServletRequest} in controllers, passed via {@code AuditContext}.
     * Nullable for operations not triggered by HTTP requests.
     */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /**
     * Timestamp when the audit record was created.
     * Auto-populated by {@code AuditingEntityListener}.
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
