package com.veltro.inventory.application.dashboard.service;

import com.veltro.inventory.application.dashboard.dto.DashboardResponse;
import com.veltro.inventory.domain.inventory.model.AlertType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Query interface for dashboard-specific data (B3-02).
 *
 * <p>Provides optimized queries for dashboard KPIs without coupling
 * to specific repository implementations.
 */
public interface DashboardQueryRepository {

    /**
     * Sums total sales for completed sales within a date range.
     *
     * @param startOfDay start of the period
     * @param endOfDay end of the period
     * @return sum of all completed sale totals
     */
    BigDecimal sumTodaySales(LocalDateTime startOfDay, LocalDateTime endOfDay);

    /**
     * Counts completed sales within a date range.
     *
     * @param startOfDay start of the period
     * @param endOfDay end of the period
     * @return count of completed sales
     */
    long countTodaySales(LocalDateTime startOfDay, LocalDateTime endOfDay);

    /**
     * Sums total sales for completed sales within a date range.
     *
     * @param startDate start of the period
     * @param endDate end of the period
     * @return sum of all completed sale totals
     */
    BigDecimal sumSalesBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Finds all products with zero stock.
     *
     * @return list of out-of-stock products
     */
    List<DashboardResponse.OutOfStockProduct> findOutOfStockProducts();

    /**
     * Counts active (unresolved) alerts of a specific type.
     *
     * @param type the alert type
     * @return count of active alerts
     */
    long countActiveAlertsByType(AlertType type);

    /**
     * Finds the most recent completed sales.
     *
     * @param limit maximum number of sales to return
     * @return list of recent sales
     */
    List<DashboardResponse.RecentSale> findRecentSales(int limit);
}
