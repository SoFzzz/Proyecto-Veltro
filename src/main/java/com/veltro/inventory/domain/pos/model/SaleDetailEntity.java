package com.veltro.inventory.domain.pos.model;

import com.veltro.inventory.domain.shared.AbstractAuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "sale_detail")
@Getter
@Setter
@NoArgsConstructor
public class SaleDetailEntity extends AbstractAuditableEntity {
    private boolean active = true; // Needed for soft delete and tests
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    private SaleEntity sale;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal subtotal;

    @Version
    @Column(nullable = false)
    private Long version;

    public void calculateSubtotal() {
        this.subtotal = this.unitPrice.multiply(BigDecimal.valueOf(this.quantity));
    }
}
