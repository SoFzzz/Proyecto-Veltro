package com.veltro.inventory.application.inventory.listener;

import com.veltro.inventory.application.inventory.event.StockChangedEvent;
import com.veltro.inventory.application.inventory.service.AlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EvaluateStockAlertsListener {

    private final AlertService alertService;

    @EventListener
    public void onStockChanged(StockChangedEvent event) {
        if (event == null || event.productId() == null) {
            log.warn("Received StockChangedEvent without product information");
            return;
        }

        try {
            alertService.evaluateStock(event.productId());
            log.info("Stock alerts evaluated for product {}", event.productId());
        } catch (RuntimeException ex) {
            log.error("Failed to evaluate alerts for product {}", event.productId(), ex);
            throw ex;
        }
    }
}
