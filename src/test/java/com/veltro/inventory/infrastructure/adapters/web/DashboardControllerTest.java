package com.veltro.inventory.infrastructure.adapters.web;

import com.veltro.inventory.application.dashboard.dto.DashboardResponse;
import com.veltro.inventory.application.dashboard.service.DashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DashboardController} (B3-02).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DashboardController")
class DashboardControllerTest {

    @Mock
    private DashboardService dashboardService;

    private DashboardController dashboardController;

    @BeforeEach
    void setUp() {
        dashboardController = new DashboardController(dashboardService);
    }

    @Test
    @DisplayName("getDashboard returns dashboard response")
    void getDashboard_returnsResponse() {
        // Arrange
        DashboardResponse mockResponse = new DashboardResponse(
                BigDecimal.valueOf(1000.00),
                5L,
                BigDecimal.valueOf(200.00),
                2L,
                List.of(
                        new DashboardResponse.OutOfStockProduct(1L, "Product A", "SKU001"),
                        new DashboardResponse.OutOfStockProduct(2L, "Product B", "SKU002")
                ),
                BigDecimal.valueOf(1000.00),
                3L,
                List.of(
                        new DashboardResponse.RecentSale(1L, "SALE-001", BigDecimal.valueOf(200.00), 3, 100L, OffsetDateTime.now())
                )
        );
        when(dashboardService.getDashboard()).thenReturn(mockResponse);

        // Act
        ResponseEntity<DashboardResponse> response = dashboardController.getDashboard();

        // Assert
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().todaySales()).isEqualByComparingTo(BigDecimal.valueOf(1000.00));
        assertThat(response.getBody().todaySalesCount()).isEqualTo(5L);
        assertThat(response.getBody().outOfStockProducts()).isEqualTo(2L);
        verify(dashboardService).getDashboard();
    }

    @Test
    @DisplayName("getDashboard calls service")
    void getDashboard_callsService() {
        // Arrange
        DashboardResponse mockResponse = new DashboardResponse(
                BigDecimal.ZERO, 0L, BigDecimal.ZERO, 0L, List.of(),
                BigDecimal.ZERO, 0L, List.of()
        );
        when(dashboardService.getDashboard()).thenReturn(mockResponse);

        // Act
        dashboardController.getDashboard();

        // Assert
        verify(dashboardService).getDashboard();
    }
}
