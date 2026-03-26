package com.veltro.inventory.application.purchasing.listener;

import com.veltro.inventory.application.inventory.dto.StockEntryRequest;
import com.veltro.inventory.application.inventory.service.InventoryService;
import com.veltro.inventory.application.purchasing.event.OrderReceivedEvent;
import com.veltro.inventory.application.purchasing.event.ReceivedItemInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Listener that handles inventory increments when purchase orders are received (B2-04).
 *
 * <p>This is the purchasing module's implementation of the Observer pattern.
 * When a {@link OrderReceivedEvent} is published, this listener calls
 * {@link InventoryService#recordEntry(Long, StockEntryRequest)} for each received item.
 *
 * <p><b>No {@code @Transactional}:</b> The listener relies on the InventoryService's
 * existing transaction management. If inventory updates fail, the exception will
 * propagate back to the service that published the event, allowing for proper rollback.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IncrementStockOrderListener {

    private final InventoryService inventoryService;

    /**
     * Handles order received events by incrementing stock for each received item.
     *
     * @param event the order received event containing received items
     */
    @EventListener
    public void onOrderReceived(OrderReceivedEvent event) {
        if (event.items() == null || event.items().isEmpty()) {
            log.info("Order {} contains no items — nothing to increment", event.orderNumber());
            return;
        }

        for (ReceivedItemInfo item : event.items()) {
            inventoryService.recordEntry(
                item.productId(),
                new StockEntryRequest(item.receivedQuantity(), "Purchase Order " + event.orderNumber())
            );
            log.info("Added {} units of product {} from order {}", 
                     item.receivedQuantity(), item.productId(), event.orderNumber());
        }
    }
}