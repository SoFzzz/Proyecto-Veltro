package com.veltro.inventory.application.pos.listener;

import com.veltro.inventory.application.inventory.dto.StockExitRequest;
import com.veltro.inventory.application.inventory.service.InventoryService;
import com.veltro.inventory.application.pos.event.SaleCompletedEvent;
import com.veltro.inventory.application.pos.event.SaleItemInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class DeductStockSaleListenerTest {
    @Mock
    private InventoryService inventoryService;

    private DeductStockSaleListener listener;

    @BeforeEach
    void setup() { 
        MockitoAnnotations.openMocks(this);
        listener = new DeductStockSaleListener(inventoryService);
    }

    @Test
    void onSaleCompleted_singleItem_callsRecordExitOnce() {
        SaleItemInfo item = new SaleItemInfo(5L, "Milk", 3, new BigDecimal("8.50"), new BigDecimal("25.50"));
        SaleCompletedEvent event = new SaleCompletedEvent(1L, "SALE-001", 21L, BigDecimal.TEN, null, null, List.of(item));
        listener.onSaleCompleted(event);
        verify(inventoryService, times(1)).recordExit(eq(5L), eq(new StockExitRequest(3, "Sale SALE-001")));
    }

    @Test
    void onSaleCompleted_multipleItems_callsRecordExitForEachItem() {
        SaleItemInfo i1 = new SaleItemInfo(3L, "Bread", 2, new BigDecimal("5.00"), new BigDecimal("10.00"));
        SaleItemInfo i2 = new SaleItemInfo(8L, "Eggs", 1, new BigDecimal("12.00"), new BigDecimal("12.00"));
        SaleCompletedEvent event = new SaleCompletedEvent(2L, "SALE-002", 42L, BigDecimal.ZERO, null, null, List.of(i1, i2));
        listener.onSaleCompleted(event);
        verify(inventoryService).recordExit(eq(3L), eq(new StockExitRequest(2, "Sale SALE-002")));
        verify(inventoryService).recordExit(eq(8L), eq(new StockExitRequest(1, "Sale SALE-002")));
        verifyNoMoreInteractions(inventoryService);
    }

    @Test
    void onSaleCompleted_emptyItemsList_doesNotCallRecordExit() {
        SaleCompletedEvent event = new SaleCompletedEvent(5L, "SALE-003", 33L, BigDecimal.ZERO, null, null, Collections.emptyList());
        listener.onSaleCompleted(event);
        verifyNoInteractions(inventoryService);
    }

    @Test
    void onSaleCompleted_passesCorrectReasonWithSaleNumber() {
        SaleItemInfo item = new SaleItemInfo(10L, "Sugar", 1, BigDecimal.ONE, BigDecimal.ONE);
        SaleCompletedEvent event = new SaleCompletedEvent(8L, "MY-TEST-SALE", 70L, BigDecimal.ONE, null, null, List.of(item));
        listener.onSaleCompleted(event);

        ArgumentCaptor<StockExitRequest> captor = ArgumentCaptor.forClass(StockExitRequest.class);
        verify(inventoryService).recordExit(eq(10L), captor.capture());
        assertThat(captor.getValue().reason()).contains("MY-TEST-SALE");
    }
}
