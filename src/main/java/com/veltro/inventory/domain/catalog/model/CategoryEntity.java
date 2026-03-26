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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing a product category.
 *
 * Implements the Composite Pattern via a self-referencing relationship:
 * each category may have a parent and zero-or-more child subcategories.
 * This allows an arbitrarily deep category hierarchy.
 *
 * Soft-delete is inherited from {@link AbstractAuditableEntity} (active flag).
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true, exclude = "subCategories")
@Entity
@Table(name = "categories")
public class CategoryEntity extends AbstractAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description")
    private String description;

    // Composite Pattern — parent link (leaf or node)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id")
    private CategoryEntity parentCategory;

    // Composite Pattern — children (empty list for leaf nodes)
    @OneToMany(mappedBy = "parentCategory", fetch = FetchType.LAZY)
    private List<CategoryEntity> subCategories = new ArrayList<>();
}
