package com.veltro.inventory.domain.catalog.model;

import com.veltro.inventory.domain.shared.AbstractAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import lombok.ToString;

import java.math.BigDecimal;

/**
 * JPA entity representing a product in the catalog (B1-03).
 *
 * ADR-005: {@code costPrice} and {@code salePrice} use NUMERIC(19,4) precision.
 * AC-05:   Soft-delete via {@code active} flag inherited from {@link AbstractAuditableEntity}.
 * AC-06:   No sensitive fields; {@code passwordHash} does not exist on this entity.
 *
 * The business constraint {@code salePrice >= costPrice} is validated in the
 * service layer (ProductService) and throws {@code InvalidPriceException}.
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@Entity
@Table(name = "products")
public class ProductEntity extends AbstractAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    // Unique barcode — B-Tree index created in V1 migration (idx_products_barcode)
    @Column(name = "barcode", unique = true, length = 100)
    private String barcode;

    @Column(name = "sku", length = 100)
    private String sku;

    @Column(name = "description")
    private String description;

    // ADR-005: monetary fields use NUMERIC(19,4)
    @Column(name = "cost_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal costPrice;

    @Column(name = "sale_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal salePrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private CategoryEntity category;
}
