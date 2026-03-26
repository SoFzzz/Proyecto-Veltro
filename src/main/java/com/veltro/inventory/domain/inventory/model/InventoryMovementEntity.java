package com.veltro.inventory.domain.inventory.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * Append-only audit trail for every stock movement (B1-04).
 *
 * This entity is intentionally NOT a subclass of {@link com.veltro.inventory.domain.shared.AbstractAuditableEntity}
 * because it has no update lifecycle (no {@code updatedAt}/{@code updatedBy}) and no
 * soft-delete ({@code active}) — the DB table has neither column.
 *
 * Audit population: {@code @EntityListeners(AuditingEntityListener.class)} is declared
 * directly on this class so that Spring Data JPA still processes the {@code @CreatedDate}
 * and {@code @CreatedBy} annotations via {@link VeltroAuditorAware} — the same mechanism
 * used by {@link com.veltro.inventory.domain.shared.AbstractAuditableEntity}.
 * {@code createdAt} and {@code createdBy} are NEVER set manually in service code.
 *
 * The DB constraint {@code quantity > 0} is enforced at the database level (V2 migration).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "inventory_movements")
@EntityListeners(AuditingEntityListener.class)
public class InventoryMovementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id", nullable = false)
    private InventoryEntity inventory;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 20)
    private MovementType movementType;

    /** Always positive — the DB enforces {@code quantity > 0}. */
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "previous_stock", nullable = false)
    private Integer previousStock;

    @Column(name = "new_stock", nullable = false)
    private Integer newStock;

    @Column(name = "reason")
    private String reason;

    /**
     * Populated automatically by {@link AuditingEntityListener} via
     * {@link com.veltro.inventory.infrastructure.adapters.config.VeltroAuditorAware}.
     * Not manually assigned.
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Populated automatically by {@link AuditingEntityListener} via
     * {@link com.veltro.inventory.infrastructure.adapters.config.VeltroAuditorAware}.
     * Not manually assigned.
     */
    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false, length = 100)
    private String createdBy;
}
