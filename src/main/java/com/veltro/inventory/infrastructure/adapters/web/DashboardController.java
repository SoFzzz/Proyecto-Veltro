package com.veltro.inventory.infrastructure.adapters.web;

import com.veltro.inventory.application.dashboard.dto.DashboardResponse;
import com.veltro.inventory.application.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for dashboard KPIs (B3-02).
 *
 * <p>Provides aggregated KPIs using the Facade Pattern:
 * <ul>
 *   <li>Today's sales and average ticket</li>
 *   <li>Out-of-stock products count</li>
 *   <li>Estimated monthly profit</li>
 *   <li>Active low stock alerts</li>
 * </ul>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Retrieves dashboard KPIs.
     *
     * <p>GET /api/v1/dashboard
     *
     * @return aggregated dashboard response with KPIs
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER', 'WAREHOUSE')")
    public ResponseEntity<DashboardResponse> getDashboard() {
        log.info("Dashboard request received");
        DashboardResponse response = dashboardService.getDashboard();
        return ResponseEntity.ok(response);
    }
}
