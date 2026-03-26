package com.veltro.inventory.infrastructure.adapters.web;

import com.veltro.inventory.application.inventory.dto.AlertResponse;
import com.veltro.inventory.application.inventory.dto.UpdateAlertConfigurationRequest;
import com.veltro.inventory.application.inventory.service.AlertConfigurationService;
import com.veltro.inventory.application.inventory.service.AlertService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;
    private final AlertConfigurationService configurationService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
    public Page<AlertResponse> listAlerts(Pageable pageable) {
        return alertService.listActiveAlerts(pageable);
    }

    @GetMapping("/unread/count")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE','CASHIER')")
    public long unreadCount() {
        return alertService.unreadCount();
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        alertService.markAsRead(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping("/{id}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> resolve(@PathVariable Long id) {
        alertService.markAsResolved(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/configuration/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
    public ResponseEntity<?> getConfiguration(@PathVariable Long productId) {
        return ResponseEntity.ok(configurationService.getConfiguration(productId));
    }

    @PutMapping("/configuration/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
    public ResponseEntity<?> updateConfiguration(
            @PathVariable Long productId,
            @Valid @RequestBody UpdateAlertConfigurationRequest request) {
        return ResponseEntity.ok(configurationService.updateConfiguration(productId, request));
    }
}
