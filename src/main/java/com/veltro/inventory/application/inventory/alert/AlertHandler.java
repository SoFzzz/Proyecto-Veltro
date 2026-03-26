package com.veltro.inventory.application.inventory.alert;

public interface AlertHandler {

    void setNext(AlertHandler handler);

    void handle(StockEvaluationContext context);
}
