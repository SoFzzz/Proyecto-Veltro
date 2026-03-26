package com.veltro.inventory.domain.catalog.ports;

import com.veltro.inventory.domain.catalog.model.CategoryEntity;

import java.util.List;
import java.util.Optional;

/**
 * Output port for category persistence.
 *
 * Pure Java interface — zero Spring or infrastructure imports.
 * Implemented by {@code CategoryJpaRepository} in the infrastructure layer.
 */
public interface CategoryRepository {

    Optional<CategoryEntity> findByIdAndActiveTrue(Long id);

    /** Returns all root categories (those with no parent) that are active. */
    List<CategoryEntity> findAllByParentCategoryIsNullAndActiveTrue();

    /** Returns all active categories (for admin/reporting use). */
    List<CategoryEntity> findAllByActiveTrue();

    CategoryEntity save(CategoryEntity category);
}
