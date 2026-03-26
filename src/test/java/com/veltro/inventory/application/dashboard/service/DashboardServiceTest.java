package com.veltro.inventory.application.dashboard.service;

import com.veltro.inventory.application.dashboard.dto.DashboardResponse;
import com.veltro.inventory.domain.inventory.model.AlertType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DashboardService} (B3-02).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DashboardService")
class DashboardServiceTest {

    @Mock
    private DashboardQueryRepository dashboardQueryRepository;

    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardService(dashboardQueryRepository);
    }

    @Test
    @DisplayName("getDashboard aggregates all KPIs correctly")
    void getDashboard_aggregatesAllKpis() {
        // Arrange
        when(dashboardQueryRepository.sumTodaySales(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(BigDecimal.valueOf(1000.00));
        when(dashboardQueryRepository.countTodaySales(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(5L);
        when(dashboardQueryRepository.findOutOfStockProducts())
                .thenReturn(List.of(
                        new DashboardResponse.OutOfStockProduct(1L, "Product A", "SKU001"),
                        new DashboardResponse.OutOfStockProduct(2L, "Product B", "SKU002")
                ));
        when(dashboardQueryRepository.countActiveAlertsByType(AlertType.LOW_STOCK))
                .thenReturn(3L);
        when(dashboardQueryRepository.sumSalesBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(BigDecimal.valueOf(5000.00));
        when(dashboardQueryRepository.findRecentSales(anyInt()))
                .thenReturn(List.of(
                        new DashboardResponse.RecentSale(1L, "SALE-001", BigDecimal.valueOf(200.00), 3, 100L, OffsetDateTime.now())
                ));

        // Act
        DashboardResponse response = dashboardService.getDashboard();

        // Assert
        assertThat(response.todaySales()).isEqualByComparingTo(BigDecimal.valueOf(1000.00));
        assertThat(response.todaySalesCount()).isEqualTo(5L);
        assertThat(response.averageTicket()).isEqualByComparingTo(BigDecimal.valueOf(200.00)); // 1000/5
        assertThat(response.outOfStockProducts()).isEqualTo(2L);
        assertThat(response.outOfStockProductList()).hasSize(2);
        assertThat(response.lowStockAlertCount()).isEqualTo(3L);
        assertThat(response.estimatedMonthlyProfit()).isEqualByComparingTo(BigDecimal.valueOf(1000.00)); // 5000 * 0.20
        assertThat(response.recentSales()).hasSize(1);
    }

    @Test
    @DisplayName("getDashboard handles zero sales correctly")
    void getDashboard_zeroSales_returnsZeroAverageTicket() {
        // Arrange
        when(dashboardQueryRepository.sumTodaySales(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(BigDecimal.ZERO);
        when(dashboardQueryRepository.countTodaySales(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(0L);
        when(dashboardQueryRepository.findOutOfStockProducts())
                .thenReturn(List.of());
        when(dashboardQueryRepository.countActiveAlertsByType(AlertType.LOW_STOCK))
                .thenReturn(0L);
        when(dashboardQueryRepository.sumSalesBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(BigDecimal.ZERO);
        when(dashboardQueryRepository.findRecentSales(anyInt()))
                .thenReturn(List.of());

        // Act
        DashboardResponse response = dashboardService.getDashboard();

        // Assert
        assertThat(response.todaySales()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.todaySalesCount()).isEqualTo(0L);
        assertThat(response.averageTicket()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.outOfStockProducts()).isEqualTo(0L);
        assertThat(response.estimatedMonthlyProfit()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("getDashboard handles null sales sum")
    void getDashboard_nullSalesSum_treatedAsZero() {
        // Arrange
        when(dashboardQueryRepository.sumTodaySales(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(null);
        when(dashboardQueryRepository.countTodaySales(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(0L);
        when(dashboardQueryRepository.findOutOfStockProducts())
                .thenReturn(List.of());
        when(dashboardQueryRepository.countActiveAlertsByType(AlertType.LOW_STOCK))
                .thenReturn(0L);
        when(dashboardQueryRepository.sumSalesBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(null);
        when(dashboardQueryRepository.findRecentSales(anyInt()))
                .thenReturn(List.of());

        // Act
        DashboardResponse response = dashboardService.getDashboard();

        // Assert
        assertThat(response.todaySales()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.estimatedMonthlyProfit()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("getDashboard queries recent sales with limit 10")
    void getDashboard_queriesRecentSalesWithLimit() {
        // Arrange
        when(dashboardQueryRepository.sumTodaySales(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(BigDecimal.ZERO);
        when(dashboardQueryRepository.countTodaySales(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(0L);
        when(dashboardQueryRepository.findOutOfStockProducts())
                .thenReturn(List.of());
        when(dashboardQueryRepository.countActiveAlertsByType(AlertType.LOW_STOCK))
                .thenReturn(0L);
        when(dashboardQueryRepository.sumSalesBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(BigDecimal.ZERO);
        when(dashboardQueryRepository.findRecentSales(10))
                .thenReturn(List.of());

        // Act
        dashboardService.getDashboard();

        // Assert
        verify(dashboardQueryRepository).findRecentSales(10);
    }
}
