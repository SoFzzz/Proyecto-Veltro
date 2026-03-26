package com.veltro.inventory.domain.purchasing.model;

import com.veltro.inventory.domain.iam.model.UserEntity;
import com.veltro.inventory.domain.purchasing.model.state.PendingState;
import com.veltro.inventory.domain.purchasing.model.state.PartialState;
import com.veltro.inventory.domain.purchasing.model.state.PurchaseOrderState;
import com.veltro.inventory.domain.purchasing.model.state.ReceivedState;
import com.veltro.inventory.domain.purchasing.model.state.VoidedState;
import com.veltro.inventory.domain.shared.AbstractAuditableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Purchase order entity for B2-04 with State Pattern (ADR-006).
 *
 * Lifecycle: PENDING → PARTIAL → RECEIVED (terminal) or VOIDED (terminal)
 * Follows the same pattern as SaleEntity but for purchasing workflow.
 */
@Entity
@Table(name = "purchase_order")
@Getter
@Setter
@NoArgsConstructor
public class PurchaseOrderEntity extends AbstractAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Business identifier in format PO-YYYY-NNNNNN
     */
    @Column(name = "order_number", nullable = false, unique = true, length = 20)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private SupplierEntity supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by", nullable = false)
    private UserEntity requestedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Setter(AccessLevel.NONE)
    private PurchaseOrderStatus status;

    /**
     * Custom setter to sync transient state with persisted status (State Pattern).
     */
    public void setStatus(PurchaseOrderStatus status) {
        this.status = status;
        initializeState();
    }

    /**
     * Total order amount calculated from details.
     * ADR-005: NUMERIC(19,4) for monetary precision.
     */
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /**
     * ADR-002: Optimistic locking for concurrent updates.
     */
    @Version
    @Column(nullable = false)
    private Long version;

    @OneToMany(mappedBy = "purchaseOrder", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = false)
    private List<PurchaseOrderDetailEntity> details = new ArrayList<>();

    /**
     * Transient state object for State Pattern (not persisted).
     */
    @Transient
    private PurchaseOrderState state;

    @PostLoad
    @PostPersist
    @PostUpdate
    private void initializeState() {
        this.state = switch (this.status) {
            case PENDING -> new PendingState();
            case PARTIAL -> new PartialState();
            case RECEIVED -> new ReceivedState();
            case VOIDED -> new VoidedState();
        };
    }

    // State Pattern delegation methods

    public void addItem(PurchaseOrderDetailEntity detail) {
        state.addItem(this, detail);
    }

    public void removeItem(Long detailId) {
        state.removeItem(this, detailId);
    }

    public void receivePartial(List<PurchaseOrderState.ReceivedItem> receivedItems) {
        state.receivePartial(this, receivedItems);
    }

    public void voidOrder() {
        state.voidOrder(this);
    }

    // Business logic methods

    /**
     * Recalculates the total amount from active details.
     */
    public void recalculateTotals() {
        this.total = details.stream()
                .filter(d -> d.isActive())
                .map(d -> d.getUnitCost().multiply(BigDecimal.valueOf(d.getRequestedQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Checks if all order details have been fully received.
     *
     * @return true if all details are fully received
     */
    public boolean isFullyReceived() {
        return details.stream()
                .filter(d -> d.isActive())
                .allMatch(PurchaseOrderDetailEntity::isFullyReceived);
    }

    /**
     * Creates a clone of this order for the Prototype Pattern.
     * Used for the /clone endpoint to create a new draft order based on an existing one.
     *
     * @return new purchase order entity with cloned details
     */
    public PurchaseOrderEntity cloneForNewOrder() {
        PurchaseOrderEntity clone = new PurchaseOrderEntity();
        clone.setSupplier(this.supplier);
        clone.setStatus(PurchaseOrderStatus.PENDING);
        clone.setNotes(this.notes != null ? "Cloned from " + this.orderNumber : null);
        
        // Clone all active details
        for (PurchaseOrderDetailEntity detail : this.details) {
            if (detail.isActive()) {
                PurchaseOrderDetailEntity clonedDetail = detail.cloneForNewOrder();
                clonedDetail.setPurchaseOrder(clone);
                clone.details.add(clonedDetail);
            }
        }
        
        clone.recalculateTotals();
        return clone;
    }

    @Override
    public String toString() {
        return "PurchaseOrderEntity{" +
                "id=" + id +
                ", orderNumber='" + orderNumber + '\'' +
                ", status=" + status +
                ", total=" + total +
                '}';
    }
}