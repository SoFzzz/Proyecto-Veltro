package com.veltro.inventory.application.inventory.alert;

import com.veltro.inventory.domain.inventory.model.AlertEntity;
import com.veltro.inventory.domain.inventory.model.AlertSeverity;
import com.veltro.inventory.domain.inventory.model.AlertType;

public class OverstockHandler implements AlertHandler {

    private AlertHandler next;

    @Override
    public void setNext(AlertHandler handler) {
        this.next = handler;
    }

    @Override
    public void handle(StockEvaluationContext context) {
        int threshold = context.getOverstockThreshold();
        if (threshold > 0 && context.getCurrentStock() > threshold) {
            AlertEntity alert = new AlertEntity();
            alert.setType(AlertType.OVERSTOCK);
            alert.setSeverity(AlertSeverity.INFO);
            alert.setMessage("Product " + context.getProductName() + " exceeds overstock threshold");
            context.addAlert(alert);
        }
        if (next != null) {
            next.handle(context);
        }
    }
}
