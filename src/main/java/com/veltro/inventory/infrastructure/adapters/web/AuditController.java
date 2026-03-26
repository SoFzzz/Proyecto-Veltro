package com.veltro.inventory.infrastructure.adapters.web;

import com.veltro.inventory.application.audit.dto.AuditFilterRequest;
import com.veltro.inventory.application.audit.dto.AuditRecordResponse;
import com.veltro.inventory.application.audit.service.ForensicAuditService;
import com.veltro.inventory.domain.audit.model.AuditEntityType;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

/**
 * REST controller for forensic audit records (B3-03).
 * 
 * <p>All endpoints require ADMIN role. Provides paginated and filtered access
 * to the append-only audit trail.
 * 
 * <p>Security is already configured globally in SecurityConfig:
 * {@code .requestMatchers("/api/v1/audit/**").hasRole("ADMIN")}
 * 
 * <p>The {@code @PreAuthorize("hasRole('ADMIN')")} annotations are kept for
 * explicitness and IDE visibility.
 */
@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AuditController {

    private final ForensicAuditService auditService;

    /**
     * Lists audit records with optional filters and pagination.
     * 
     * <p>Query parameters:
     * <ul>
     *   <li>{@code entityType} - filter by entity type (SALE, PURCHASE_ORDER, INVENTORY)</li>
     *   <li>{@code action} - filter by action (CONFIRM, VOID, RECEIVE, ADJUST)</li>
     *   <li>{@code username} - filter by actor username</li>
     *   <li>{@code startDate} - filter by date range start (ISO 8601)</li>
     *   <li>{@code endDate} - filter by date range end (ISO 8601)</li>
     *   <li>{@code page}, {@code size}, {@code sort} - pagination parameters</li>
     * </ul>
     * 
     * @param filter filter criteria (all optional)
     * @param pageable pagination parameters
     * @return paginated audit records
     */
    @GetMapping
    public Page<AuditRecordResponse> findAll(
            @ModelAttribute AuditFilterRequest filter,
            Pageable pageable) {
        return auditService.findAll(filter, pageable);
    }

    /**
     * Retrieves a single audit record by ID.
     * 
     * @param id the audit record ID
     * @return the audit record
     * @throws com.veltro.inventory.exception.NotFoundException if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<AuditRecordResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(auditService.findById(id));
    }

    /**
     * Retrieves all audit records for a specific entity instance.
     * 
     * <p>Example: {@code GET /api/v1/audit/entity/SALE/1042} returns all
     * audit records for Sale #1042 (confirm, void, etc.).
     * 
     * @param type the entity type
     * @param entityId the entity ID
     * @return list of audit records ordered by created date DESC
     */
    @GetMapping("/entity/{type}/{entityId}")
    public ResponseEntity<List<AuditRecordResponse>> findByEntity(
            @PathVariable("type") AuditEntityType type,
            @PathVariable Long entityId) {
        return ResponseEntity.ok(auditService.findByEntityTypeAndEntityId(type, entityId));
    }

    /**
     * Extracts client IP address from HTTP request.
     * 
     * <p>Checks {@code X-Forwarded-For} header first (for proxied requests),
     * then falls back to {@code request.getRemoteAddr()}.
     * 
     * <p>This is a utility method used by controllers that trigger auditable
     * operations (SaleController, PurchaseOrderController, InventoryController).
     * It's placed here for discoverability but can be moved to a shared utility
     * class if needed.
     * 
     * @param request the HTTP servlet request
     * @return the client IP address (IPv4 or IPv6), or null if unavailable
     */
    public static String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For can contain multiple IPs: "client, proxy1, proxy2"
            // Take the first one (client IP)
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
