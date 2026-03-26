package com.veltro.inventory.domain.pos.model;

import com.veltro.inventory.domain.pos.model.state.CompletedState;
import com.veltro.inventory.domain.pos.model.state.InProgressState;
import com.veltro.inventory.domain.pos.model.state.SaleState;
import com.veltro.inventory.domain.pos.model.state.VoidedState;
import com.veltro.inventory.domain.shared.AbstractAuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AccessLevel;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sale")
@Getter
@Setter
@NoArgsConstructor
public class SaleEntity extends AbstractAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sale_number", nullable = false, unique = true, length = 20)
    private String saleNumber;

@Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Setter(AccessLevel.NONE)
    private SaleStatus status;

    public void setStatus(SaleStatus status) {
        this.status = status;
        initializeState();
    }

    @Column(name = "cashier_id", nullable = false)
    private Long cashierId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 20)
    private PaymentMethod paymentMethod;

    @Column(name = "amount_received", precision = 19, scale = 4)
    private BigDecimal amountReceived;

    @Column(name = "change", precision = 19, scale = 4)
    private BigDecimal change;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    @OneToMany(mappedBy = "sale", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = false)
    private List<SaleDetailEntity> details = new ArrayList<>();

    @Transient
    private SaleState state;

    @PostLoad
    @PostPersist
    @PostUpdate
    private void initializeState() {
        this.state = switch (this.status) {
            case IN_PROGRESS -> new InProgressState();
            case COMPLETED -> new CompletedState();
            case VOIDED -> new VoidedState();
        };
    }

    public void addItem(SaleDetailEntity detail) {
        state.addItem(this, detail);
    }

    public void modifyItem(Long detailId, Integer newQuantity) {
        state.modifyItem(this, detailId, newQuantity);
    }

    public void removeItem(Long detailId) {
        state.removeItem(this, detailId);
    }

    public void confirm(PaymentMethod paymentMethod) {
        state.confirm(this, paymentMethod);
    }

    public void voidSale() {
        state.voidSale(this);
    }

    public void recalculateTotals() {
        this.subtotal = details.stream()
                .map(d -> d.getSubtotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.total = this.subtotal;
    }
}
