package com.veltro.inventory.application.inventory.alert;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AlertChainBuilder {

    @Bean
    public AlertHandler alertHandlerChain() {
        AlertHandler outOfStock = new OutOfStockHandler();
        AlertHandler lowStock = new LowStockHandler();
        AlertHandler overstock = new OverstockHandler();

        outOfStock.setNext(lowStock);
        lowStock.setNext(overstock);

        return outOfStock;
    }
}
