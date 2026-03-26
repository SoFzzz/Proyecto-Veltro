package com.veltro.inventory.infrastructure.adapters.persistence;

import com.veltro.inventory.domain.iam.model.UserEntity;
import com.veltro.inventory.domain.iam.ports.UserRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA adapter for the {@link UserRepository} domain port.
 *
 * Extends {@link JpaRepository} for full CRUD and paging support, and
 * implements the domain port so the application layer depends only on the
 * port interface — never on this infrastructure class directly.
 */
@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, Long>, UserRepository {
    // Spring Data generates all method implementations declared in UserRepository.
    // Additional query methods specific to the JPA adapter can be added here.
}
