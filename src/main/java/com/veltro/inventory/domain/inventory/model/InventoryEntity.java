package com.veltro.inventory.domain.inventory.model;

import com.veltro.inventory.domain.catalog.model.ProductEntity;
import com.veltro.inventory.domain.shared.AbstractAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * JPA entity representing the stock record for a single product (B1-04).
 *
 * ADR-002: {@code version} field enables optimistic locking — two concurrent
 * transactions on the same inventory row will have the second throw
 * {@code OptimisticLockException} (AC-03).
 *
 * The DB-level CHECK constraint {@code current_stock >= 0} is the last
 * safety net; the service layer validates before any DML (AC-04).
 *
 * AC-05: Soft-delete via {@code active} flag inherited from
 * {@link AbstractAuditableEntity}.
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@Entity
@Table(name = "inventory")
public class InventoryEntity extends AbstractAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** One inventory record per product — UNIQUE constraint in DB. */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private ProductEntity product;

    /** Current on-hand stock. Must never go negative (AC-04). */
    @Column(name = "current_stock", nullable = false)
    private Integer currentStock = 0;

    /** Minimum stock threshold; triggers low-stock alert when breached. */
    @Column(name = "min_stock", nullable = false)
    private Integer minStock = 0;

    /** Maximum stock threshold; triggers overstock alert when exceeded. */
    @Column(name = "max_stock", nullable = false)
    private Integer maxStock = 0;

    /**
     * ADR-002: Optimistic locking version.
     * JPA increments this on every UPDATE; a stale read causes
     * {@code OptimisticLockException} on flush (AC-03).
     */
    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}
