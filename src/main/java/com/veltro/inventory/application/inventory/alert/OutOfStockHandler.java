package com.veltro.inventory.application.inventory.alert;

import com.veltro.inventory.domain.inventory.model.AlertEntity;
import com.veltro.inventory.domain.inventory.model.AlertSeverity;
import com.veltro.inventory.domain.inventory.model.AlertType;

public class OutOfStockHandler implements AlertHandler {

    private AlertHandler next;

    @Override
    public void setNext(AlertHandler handler) {
        this.next = handler;
    }

    @Override
    public void handle(StockEvaluationContext context) {
        if (context.getCurrentStock() <= context.getCriticalStock()) {
            AlertEntity alert = new AlertEntity();
            alert.setType(AlertType.OUT_OF_STOCK);
            alert.setSeverity(AlertSeverity.CRITICAL);
            alert.setMessage("Product " + context.getProductName() + " is out of stock");
            context.addAlert(alert);
        }
        if (next != null) {
            next.handle(context);
        }
    }
}
