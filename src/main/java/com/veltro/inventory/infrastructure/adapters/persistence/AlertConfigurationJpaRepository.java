package com.veltro.inventory.infrastructure.adapters.persistence;

import com.veltro.inventory.domain.inventory.model.AlertConfigurationEntity;
import com.veltro.inventory.domain.inventory.ports.AlertConfigurationRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlertConfigurationJpaRepository
        extends JpaRepository<AlertConfigurationEntity, Long>, AlertConfigurationRepository {

    @Override
    Optional<AlertConfigurationEntity> findByProductIdAndActiveTrue(Long productId);
}
