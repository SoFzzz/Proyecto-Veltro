package com.veltro.inventory.application.inventory.service;

import com.veltro.inventory.application.inventory.dto.AlertConfigurationResponse;
import com.veltro.inventory.application.inventory.dto.UpdateAlertConfigurationRequest;
import com.veltro.inventory.application.inventory.mapper.AlertConfigurationMapper;
import com.veltro.inventory.domain.inventory.model.AlertConfigurationEntity;
import com.veltro.inventory.domain.inventory.ports.AlertConfigurationRepository;
import com.veltro.inventory.domain.inventory.ports.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertConfigurationService {

    private static final int DEFAULT_CRITICAL_STOCK = 0;
    private static final int DEFAULT_MIN_STOCK = 5;
    private static final int DEFAULT_OVERSTOCK_THRESHOLD = 100;

    private final AlertConfigurationRepository configurationRepository;
    private final InventoryRepository inventoryRepository;
    private final AlertConfigurationMapper configurationMapper;

    @Transactional(readOnly = true)
    public AlertConfigurationResponse getConfiguration(Long productId) {
        return configurationRepository.findByProductIdAndActiveTrue(productId)
                .map(configurationMapper::toResponse)
                .orElseGet(() -> configurationMapper.toResponse(createDefaultConfiguration(productId)));
    }

    @Transactional
    public AlertConfigurationResponse updateConfiguration(Long productId, UpdateAlertConfigurationRequest request) {
        AlertConfigurationEntity config = configurationRepository.findByProductIdAndActiveTrue(productId)
                .orElseGet(() -> createDefaultConfiguration(productId));

        config.setCriticalStock(request.criticalStock());
        config.setMinStock(request.minStock());
        config.setOverstockThreshold(request.overstockThreshold());

        AlertConfigurationEntity saved = configurationRepository.save(config);
        log.info("Alert configuration updated for product {}", productId);
        return configurationMapper.toResponse(saved);
    }

    private AlertConfigurationEntity createDefaultConfiguration(Long productId) {
        var inventory = inventoryRepository.findByProductIdAndActiveTrue(productId)
                .orElseThrow(() -> new IllegalStateException("Inventory not found for product " + productId));

        int minStock = (inventory.getMinStock() != null && inventory.getMinStock() > 0)
                ? inventory.getMinStock()
                : DEFAULT_MIN_STOCK;
        int maxStock = (inventory.getMaxStock() != null && inventory.getMaxStock() > 0)
                ? inventory.getMaxStock()
                : DEFAULT_OVERSTOCK_THRESHOLD;

        AlertConfigurationEntity config = new AlertConfigurationEntity();
        config.setProduct(inventory.getProduct());
        config.setCriticalStock(DEFAULT_CRITICAL_STOCK);
        config.setMinStock(minStock);
        config.setOverstockThreshold(maxStock);

        return configurationRepository.save(config);
    }
}
