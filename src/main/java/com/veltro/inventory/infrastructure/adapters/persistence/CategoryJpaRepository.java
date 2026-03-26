package com.veltro.inventory.infrastructure.adapters.persistence;

import com.veltro.inventory.domain.catalog.model.CategoryEntity;
import com.veltro.inventory.domain.catalog.ports.CategoryRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA adapter for {@link CategoryRepository}.
 *
 * Method names are derived from the port interface — Spring Data generates
 * the queries automatically. No additional annotations required.
 */
@Repository
public interface CategoryJpaRepository
        extends JpaRepository<CategoryEntity, Long>, CategoryRepository {
}
