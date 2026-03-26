package com.veltro.inventory.application.pos.listener;

import com.veltro.inventory.application.inventory.dto.StockEntryRequest;
import com.veltro.inventory.application.inventory.service.InventoryService;
import com.veltro.inventory.application.pos.event.SaleItemInfo;
import com.veltro.inventory.application.pos.event.SaleVoidedEvent;
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

class RestoreStockSaleListenerTest {
    @Mock
    private InventoryService inventoryService;

    private RestoreStockSaleListener listener;

    @BeforeEach
    void setup() { 
        MockitoAnnotations.openMocks(this);
        listener = new RestoreStockSaleListener(inventoryService);
    }

    @Test
    void onSaleVoided_singleItem_callsRecordEntryOnce() {
        SaleItemInfo item = new SaleItemInfo(2L, "Juice", 4, new BigDecimal("3.50"), new BigDecimal("14.00"));
        SaleVoidedEvent event = new SaleVoidedEvent(7L, "SALE-007", "admin", null, new BigDecimal("14.00"), List.of(item));
        listener.onSaleVoided(event);
        verify(inventoryService, times(1)).recordEntry(eq(2L), eq(new StockEntryRequest(4, "Voided sale SALE-007")));
    }

    @Test
    void onSaleVoided_multipleItems_callsRecordEntryForEachItem() {
        SaleItemInfo i1 = new SaleItemInfo(4L, "Butter", 1, new BigDecimal("20.00"), new BigDecimal("20.00"));
        SaleItemInfo i2 = new SaleItemInfo(5L, "Soda", 6, new BigDecimal("2.00"), new BigDecimal("12.00"));
        SaleVoidedEvent event = new SaleVoidedEvent(8L, "SALE-008", "user", null, BigDecimal.ONE, List.of(i1, i2));
        listener.onSaleVoided(event);
        verify(inventoryService).recordEntry(eq(4L), eq(new StockEntryRequest(1, "Voided sale SALE-008")));
        verify(inventoryService).recordEntry(eq(5L), eq(new StockEntryRequest(6, "Voided sale SALE-008")));
        verifyNoMoreInteractions(inventoryService);
    }

    @Test
    void onSaleVoided_emptyItemsList_doesNotCallRecordEntry() {
        SaleVoidedEvent event = new SaleVoidedEvent(10L, "SALE-009", "admin", null, BigDecimal.ZERO, Collections.emptyList());
        listener.onSaleVoided(event);
        verifyNoInteractions(inventoryService);
    }

    @Test
    void onSaleVoided_passesCorrectReasonWithSaleNumber() {
        SaleItemInfo item = new SaleItemInfo(99L, "Tea", 5, BigDecimal.ONE, BigDecimal.ONE);
        SaleVoidedEvent event = new SaleVoidedEvent(13L, "NUM-VOID-42", "sys", null, BigDecimal.ONE, List.of(item));
        listener.onSaleVoided(event);

        ArgumentCaptor<StockEntryRequest> captor = ArgumentCaptor.forClass(StockEntryRequest.class);
        verify(inventoryService).recordEntry(eq(99L), captor.capture());
        assertThat(captor.getValue().reason()).contains("NUM-VOID-42");
    }
}
