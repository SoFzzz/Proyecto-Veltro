package com.veltro.inventory.domain.inventory.ports;

import com.veltro.inventory.domain.inventory.model.AlertConfigurationEntity;
import java.util.Optional;

public interface AlertConfigurationRepository {

    Optional<AlertConfigurationEntity> findByProductIdAndActiveTrue(Long productId);

    AlertConfigurationEntity save(AlertConfigurationEntity configuration);
}
