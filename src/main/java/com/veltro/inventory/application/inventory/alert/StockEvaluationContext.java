package com.veltro.inventory.application.inventory.alert;

import com.veltro.inventory.domain.inventory.model.AlertEntity;
import java.util.ArrayList;
import java.util.List;

public class StockEvaluationContext {

    private final Long productId;
    private final String productName;
    private final int currentStock;
    private final int criticalStock;
    private final int minStock;
    private final int overstockThreshold;
    private final List<AlertEntity> generatedAlerts = new ArrayList<>();

    public StockEvaluationContext(Long productId,
                                  String productName,
                                  int currentStock,
                                  int criticalStock,
                                  int minStock,
                                  int overstockThreshold) {
        this.productId = productId;
        this.productName = productName;
        this.currentStock = currentStock;
        this.criticalStock = criticalStock;
        this.minStock = minStock;
        this.overstockThreshold = overstockThreshold;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public int getCurrentStock() {
        return currentStock;
    }

    public int getCriticalStock() {
        return criticalStock;
    }

    public int getMinStock() {
        return minStock;
    }

    public int getOverstockThreshold() {
        return overstockThreshold;
    }

    public List<AlertEntity> getGeneratedAlerts() {
        return generatedAlerts;
    }

    public void addAlert(AlertEntity alert) {
        this.generatedAlerts.add(alert);
    }
}
