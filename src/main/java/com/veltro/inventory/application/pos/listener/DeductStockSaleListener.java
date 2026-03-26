package com.veltro.inventory.application.pos.listener;

import com.veltro.inventory.application.inventory.dto.StockExitRequest;
import com.veltro.inventory.application.inventory.service.InventoryService;
import com.veltro.inventory.application.pos.event.SaleCompletedEvent;
import com.veltro.inventory.application.pos.event.SaleItemInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeductStockSaleListener {
    private final InventoryService inventoryService;

    @EventListener
    public void onSaleCompleted(SaleCompletedEvent event) {
        if (event.items() == null || event.items().isEmpty()) {
            log.info("Sale {} contains no items — nothing to deduct", event.saleNumber());
            return;
        }
        for (SaleItemInfo item : event.items()) {
            inventoryService.recordExit(
                item.productId(),
                new StockExitRequest(item.quantity(), "Sale " + event.saleNumber())
            );
            log.info("Deducted {} units of product {} for sale {}", item.quantity(), item.productId(), event.saleNumber());
        }
    }
}
