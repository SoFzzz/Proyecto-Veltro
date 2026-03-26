package com.veltro.inventory.application.dashboard.service;

import com.veltro.inventory.application.dashboard.dto.DashboardResponse;
import com.veltro.inventory.domain.inventory.model.AlertType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Dashboard service implementing Facade Pattern (B3-02).
 *
 * <p>Aggregates KPIs from multiple data sources into a single response:
 * <ul>
 *   <li>Today's sales and average ticket from POS module</li>
 *   <li>Out-of-stock products from Inventory module</li>
 *   <li>Low stock alerts from Alert module</li>
 *   <li>Estimated monthly profit calculation</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final DashboardQueryRepository dashboardQueryRepository;

    /**
     * Retrieves dashboard KPIs (Facade Pattern).
     *
     * @return aggregated dashboard response
     */
    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        log.info("Generating dashboard KPIs");

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        // Today's sales metrics
        BigDecimal todaySales = dashboardQueryRepository.sumTodaySales(startOfDay, endOfDay);
        if (todaySales == null) {
            todaySales = BigDecimal.ZERO;
        }

        long todaySalesCount = dashboardQueryRepository.countTodaySales(startOfDay, endOfDay);

        BigDecimal averageTicket = todaySalesCount > 0
                ? todaySales.divide(BigDecimal.valueOf(todaySalesCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Out of stock products
        List<DashboardResponse.OutOfStockProduct> outOfStockList = dashboardQueryRepository.findOutOfStockProducts();
        long outOfStockCount = outOfStockList.size();

        // Low stock alerts count
        long lowStockAlertCount = dashboardQueryRepository.countActiveAlertsByType(AlertType.LOW_STOCK);

        // Estimated monthly profit (based on current month sales)
        LocalDate firstOfMonth = today.withDayOfMonth(1);
        LocalDateTime startOfMonth = firstOfMonth.atStartOfDay();
        BigDecimal monthSales = dashboardQueryRepository.sumSalesBetween(startOfMonth, endOfDay);
        if (monthSales == null) {
            monthSales = BigDecimal.ZERO;
        }
        
        // Estimate: assume 20% profit margin (this could be refined with actual cost data)
        BigDecimal estimatedMonthlyProfit = monthSales.multiply(BigDecimal.valueOf(0.20))
                .setScale(2, RoundingMode.HALF_UP);

        // Recent sales (last 10)
        List<DashboardResponse.RecentSale> recentSales = dashboardQueryRepository.findRecentSales(10);

        DashboardResponse response = new DashboardResponse(
                todaySales.setScale(2, RoundingMode.HALF_UP),
                todaySalesCount,
                averageTicket,
                outOfStockCount,
                outOfStockList,
                estimatedMonthlyProfit,
                lowStockAlertCount,
                recentSales
        );

        log.info("Dashboard generated: todaySales={}, salesCount={}, outOfStock={}, lowStockAlerts={}",
                todaySales, todaySalesCount, outOfStockCount, lowStockAlertCount);

        return response;
    }
}
