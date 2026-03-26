package com.veltro.inventory.application.report.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO for profitability report data (B3-02).
 *
 * <p>Contains sales and profitability metrics for a date range.
 *
 * @param startDate start of the report period
 * @param endDate end of the report period
 * @param totalSales total sales revenue
 * @param totalCost estimated total cost of goods sold
 * @param grossProfit gross profit (totalSales - totalCost)
 * @param profitMargin profit margin percentage
 * @param salesCount number of completed sales
 * @param itemsSold total items sold
 * @param productBreakdown profitability by product
 */
public record ProfitabilityReport(
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal totalSales,
        BigDecimal totalCost,
        BigDecimal grossProfit,
        BigDecimal profitMargin,
        long salesCount,
        long itemsSold,
        List<ProductProfitability> productBreakdown
) {
    /**
     * Profitability metrics for a single product.
     *
     * @param productId product identifier
     * @param productName product name
     * @param sku product SKU
     * @param quantitySold units sold
     * @param revenue total revenue from this product
     * @param cost total cost for this product
     * @param profit profit from this product
     * @param profitMargin profit margin percentage
     */
    public record ProductProfitability(
            Long productId,
            String productName,
            String sku,
            int quantitySold,
            BigDecimal revenue,
            BigDecimal cost,
            BigDecimal profit,
            BigDecimal profitMargin
    ) {}
}
