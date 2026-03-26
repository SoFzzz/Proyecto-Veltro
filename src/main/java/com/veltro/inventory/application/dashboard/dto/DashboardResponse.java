package com.veltro.inventory.application.dashboard.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO for dashboard KPIs (B3-02).
 *
 * <p>Aggregates key performance indicators using the Facade Pattern.
 *
 * @param todaySales total sales amount for today
 * @param todaySalesCount number of completed sales today
 * @param averageTicket average sale amount (todaySales / todaySalesCount)
 * @param outOfStockProducts count of products with zero stock
 * @param outOfStockProductList list of out-of-stock product details
 * @param estimatedMonthlyProfit estimated profit based on current month sales
 * @param lowStockAlertCount count of active low stock alerts
 * @param recentSales list of recent sales for the dashboard table
 */
public record DashboardResponse(
        BigDecimal todaySales,
        long todaySalesCount,
        BigDecimal averageTicket,
        long outOfStockProducts,
        List<OutOfStockProduct> outOfStockProductList,
        BigDecimal estimatedMonthlyProfit,
        long lowStockAlertCount,
        List<RecentSale> recentSales
) {
    /**
     * Product with zero stock.
     *
     * @param productId product identifier
     * @param productName product name
     * @param sku product SKU
     */
    public record OutOfStockProduct(
            Long productId,
            String productName,
            String sku
    ) {}

    /**
     * Recent sale summary for dashboard display.
     *
     * @param saleId sale identifier
     * @param saleNumber formatted sale number
     * @param total sale total amount
     * @param itemCount number of items in sale
     * @param cashierId cashier who processed the sale
     * @param completedAt timestamp when sale was completed
     */
    public record RecentSale(
            Long saleId,
            String saleNumber,
            BigDecimal total,
            int itemCount,
            Long cashierId,
            java.time.OffsetDateTime completedAt
    ) {}
}
