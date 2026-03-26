package com.veltro.inventory.domain.purchasing.model;

import com.veltro.inventory.domain.catalog.model.ProductEntity;
import com.veltro.inventory.domain.shared.AbstractAuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Purchase order detail entity for B2-04.
 *
 * Represents individual items in a purchase order with requested vs received quantities.
 * Unit cost is a historical snapshot and does NOT update ProductEntity.costPrice.
 */
@Entity
@Table(name = "purchase_order_detail")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderDetailEntity extends AbstractAuditableEntity {

    // Override active field from parent to provide custom access (pattern from SaleDetailEntity)
    private boolean active = true;
    
    public boolean isActive() { 
        return active; 
    }
    
    public void setActive(boolean active) { 
        this.active = active; 
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrderEntity purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @Column(name = "requested_quantity", nullable = false)
    private Integer requestedQuantity;

    @Column(name = "received_quantity", nullable = false)
    private Integer receivedQuantity = 0;

    /**
     * Historical snapshot of unit cost at the time of order creation.
     * This does NOT update ProductEntity.costPrice per project requirements.
     */
    @Column(name = "unit_cost", nullable = false, precision = 19, scale = 4)
    private BigDecimal unitCost;

    /**
     * Calculates remaining quantity to be received.
     *
     * @return requestedQuantity - receivedQuantity
     */
    public Integer getRemainingQuantity() {
        return requestedQuantity - receivedQuantity;
    }

    /**
     * Checks if this detail has been fully received.
     *
     * @return true if receivedQuantity >= requestedQuantity
     */
    public boolean isFullyReceived() {
        return receivedQuantity >= requestedQuantity;
    }

    /**
     * Creates a clone for the Prototype pattern (order cloning).
     * Resets received_quantity to 0 and clears associations.
     *
     * @return new detail entity for cloned order
     */
    public PurchaseOrderDetailEntity cloneForNewOrder() {
        PurchaseOrderDetailEntity clone = new PurchaseOrderDetailEntity();
        clone.product = this.product;
        clone.requestedQuantity = this.requestedQuantity;
        clone.receivedQuantity = 0;  // Reset for new order
        clone.unitCost = this.unitCost;
        // Note: purchaseOrder, id, audit fields are null (will be set on save)
        return clone;
    }

    @Override
    public String toString() {
        return "PurchaseOrderDetailEntity{" +
                "id=" + id +
                ", requestedQuantity=" + requestedQuantity +
                ", receivedQuantity=" + receivedQuantity +
                ", unitCost=" + unitCost +
                '}';
    }
}