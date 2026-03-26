package com.veltro.inventory.application.pos.service;

import com.veltro.inventory.application.audit.command.AuditCommandExecutor;
import com.veltro.inventory.application.audit.command.AuditContext;
import com.veltro.inventory.application.pos.dto.AddItemRequest;
import com.veltro.inventory.application.pos.dto.ConfirmSaleRequest;
import com.veltro.inventory.application.pos.dto.ModifyItemRequest;
import com.veltro.inventory.application.pos.dto.SaleResponse;
import com.veltro.inventory.application.pos.event.SaleCompletedEvent;
import com.veltro.inventory.application.pos.event.SaleItemInfo;
import com.veltro.inventory.application.pos.event.SaleVoidedEvent;
import com.veltro.inventory.application.pos.mapper.SaleMapper;
import com.veltro.inventory.domain.audit.model.AuditAction;
import com.veltro.inventory.domain.audit.model.AuditEntityType;
import com.veltro.inventory.domain.catalog.model.ProductEntity;
import com.veltro.inventory.domain.catalog.ports.ProductRepository;
import com.veltro.inventory.domain.pos.model.PaymentMethod;
import com.veltro.inventory.domain.pos.model.SaleDetailEntity;
import com.veltro.inventory.domain.pos.model.SaleEntity;
import com.veltro.inventory.domain.pos.model.SaleStatus;
import com.veltro.inventory.domain.pos.ports.SaleRepository;
import com.veltro.inventory.exception.InvalidPaymentException;
import com.veltro.inventory.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Application service for sale (POS) management (B2-01).
 *
 * <p>Owns all {@link Transactional} boundaries for sale operations.
 * Enforces the State Pattern (ADR-006) via delegation to {@link SaleEntity}.
 * Publishes domain events for downstream listeners (B2-02).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SaleService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final SaleMapper saleMapper;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final AuditCommandExecutor auditCommandExecutor;

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public SaleResponse findById(Long saleId) {
        SaleEntity sale = saleRepository.findByIdAndActiveTrue(saleId)
                .orElseThrow(() -> new NotFoundException("Sale not found with id: " + saleId));
        return saleMapper.toResponse(sale);
    }

    // -------------------------------------------------------------------------
    // Commands
    // -------------------------------------------------------------------------

    /**
     * Starts a new sale (IN_PROGRESS status).
     *
     * @return the created sale with generated sale number
     */
    @Transactional
    public SaleResponse startSale() {
        Long userId = getCurrentUserId();
        Long sequenceValue = saleRepository.getNextSaleSequenceValue();
        String saleNumber = generateSaleNumber(sequenceValue);

        SaleEntity sale = new SaleEntity();
        sale.setSaleNumber(saleNumber);
        sale.setStatus(SaleStatus.IN_PROGRESS);
        sale.setCashierId(userId);
        sale.setSubtotal(BigDecimal.ZERO);
        sale.setTotal(BigDecimal.ZERO);

        SaleEntity saved = saleRepository.save(sale);
        log.info("Sale started: {} by user {}", saleNumber, userId);
        return saleMapper.toResponse(saved);
    }

    /**
     * Adds an item to the sale cart.
     *
     * @param saleId  the sale ID
     * @param request the item to add (productId, quantity)
     * @return the updated sale
     */
    @Transactional
    public SaleResponse addItem(Long saleId, AddItemRequest request) {
        SaleEntity sale = saleRepository.findByIdAndActiveTrue(saleId)
                .orElseThrow(() -> new NotFoundException("Sale not found with id: " + saleId));

        ProductEntity product = productRepository.findByIdAndActiveTrue(request.productId())
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + request.productId()));

        // Create detail with product snapshot
        SaleDetailEntity detail = new SaleDetailEntity();
        detail.setProductId(product.getId());
        detail.setProductName(product.getName());
        detail.setQuantity(request.quantity());
        detail.setUnitPrice(product.getSalePrice());
        detail.calculateSubtotal();

        // State pattern validates and adds
        sale.addItem(detail);
        sale.recalculateTotals();

        SaleEntity saved = saleRepository.save(sale);
        log.info("Item added to sale {}: product {} x{}", sale.getSaleNumber(), product.getName(), request.quantity());
        return saleMapper.toResponse(saved);
    }

    /**
     * Modifies the quantity of an existing item in the sale.
     *
     * @param saleId   the sale ID
     * @param detailId the detail ID to modify
     * @param request  the new quantity
     * @return the updated sale
     */
    @Transactional
    public SaleResponse modifyItem(Long saleId, Long detailId, ModifyItemRequest request) {
        SaleEntity sale = saleRepository.findByIdAndActiveTrue(saleId)
                .orElseThrow(() -> new NotFoundException("Sale not found with id: " + saleId));

        // State pattern validates and modifies
        sale.modifyItem(detailId, request.quantity());
        sale.recalculateTotals();

        SaleEntity saved = saleRepository.save(sale);
        log.info("Item {} modified in sale {}: new quantity {}", detailId, sale.getSaleNumber(), request.quantity());
        return saleMapper.toResponse(saved);
    }

    /**
     * Removes an item from the sale (soft delete via active=false per AC-05).
     *
     * @param saleId   the sale ID
     * @param detailId the detail ID to remove
     * @return the updated sale
     */
    @Transactional
    public SaleResponse removeItem(Long saleId, Long detailId) {
        SaleEntity sale = saleRepository.findByIdAndActiveTrue(saleId)
                .orElseThrow(() -> new NotFoundException("Sale not found with id: " + saleId));

        // State pattern validates and soft-deletes
        sale.removeItem(detailId);
        sale.recalculateTotals();

        SaleEntity saved = saleRepository.save(sale);
        log.info("Item {} removed from sale {}", detailId, sale.getSaleNumber());
        return saleMapper.toResponse(saved);
    }

    /**
     * Confirms the sale (transitions to COMPLETED).
     *
     * <p>Validates payment details for CASH transactions (amount received must be >= total).
     * Publishes {@link SaleCompletedEvent} for downstream listeners (B2-02) to handle inventory deduction.
     * Creates forensic audit record (B3-03) capturing before/after state.
     *
     * @param saleId  the sale ID
     * @param request payment method and amount received
     * @return the confirmed sale
     * @throws InvalidPaymentException if cash payment validation fails
     */
    @Transactional
    public SaleResponse confirm(Long saleId, ConfirmSaleRequest request) {
        SaleEntity sale = saleRepository.findByIdAndActiveTrue(saleId)
                .orElseThrow(() -> new NotFoundException("Sale not found with id: " + saleId));

        // Capture state BEFORE confirmation for audit (B3-03)
        final Map<String, Object> beforeSnapshot = buildSaleSnapshot(sale);

        // Validate cash payment (B2-01 requirement)
        if (request.paymentMethod() == PaymentMethod.CASH) {
            if (request.amountReceived() == null) {
                throw new InvalidPaymentException("Amount received is required for cash payments");
            }
            if (request.amountReceived().compareTo(sale.getTotal()) < 0) {
                throw new InvalidPaymentException(
                        "Amount received must be greater than or equal to total for cash payments");
            }
            sale.setAmountReceived(request.amountReceived());
            sale.setChange(request.amountReceived().subtract(sale.getTotal()));
        }

        // State pattern validates and transitions
        sale.confirm(request.paymentMethod());

        SaleEntity saved = saleRepository.save(sale);

        // Publish event for listeners (B2-02 will handle inventory deduction)
        applicationEventPublisher.publishEvent(buildSaleCompletedEvent(saved));

        // Create forensic audit record (B3-03)
        auditCommandExecutor.execute(
                AuditEntityType.SALE,
                saved.getId(),
                AuditAction.CONFIRM,
                () -> beforeSnapshot,
                () -> saved,
                (result) -> buildSaleSnapshot(saved),
                AuditContext.empty()
        );

        log.info("Sale {} confirmed with payment method {}", sale.getSaleNumber(), request.paymentMethod());
        return saleMapper.toResponse(saved);
    }

    /**
     * Voids a completed sale (transitions to VOIDED).
     *
     * <p>Only COMPLETED sales can be voided (enforced by State Pattern).
     * Publishes {@link SaleVoidedEvent} for downstream listeners (B2-02) to handle stock reversal.
     * Creates forensic audit record (B3-03) capturing before/after state.
     *
     * @param saleId the sale ID
     * @return the voided sale
     */
    @Transactional
    public SaleResponse voidSale(Long saleId) {
        SaleEntity sale = saleRepository.findByIdAndActiveTrue(saleId)
                .orElseThrow(() -> new NotFoundException("Sale not found with id: " + saleId));

        // Capture state BEFORE voiding for audit (B3-03)
        final Map<String, Object> beforeSnapshot = buildSaleSnapshot(sale);

        // State pattern handles validation (only COMPLETED can be voided)
        sale.voidSale();

        SaleEntity saved = saleRepository.save(sale);

        // Publish event — listener in B2-02 will handle stock reversal
        applicationEventPublisher.publishEvent(buildSaleVoidedEvent(saved));

        // Create forensic audit record (B3-03)
        auditCommandExecutor.execute(
                AuditEntityType.SALE,
                saved.getId(),
                AuditAction.VOID,
                () -> beforeSnapshot,
                () -> saved,
                (result) -> buildSaleSnapshot(saved),
                AuditContext.empty()
        );

        log.info("Sale {} voided successfully", sale.getSaleNumber());
        return saleMapper.toResponse(saved);
    }

    // -------------------------------------------------------------------------
    // Helper Methods
    // -------------------------------------------------------------------------

    private String generateSaleNumber(Long sequenceValue) {
        int year = LocalDateTime.now().getYear();
        return String.format("VLT-%d-%06d", year, sequenceValue);
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails userDetails) {
            // Extract user ID from UserDetails (assuming username is numeric user ID for now)
            // In production, CustomUserDetails should expose userId directly
            return 1L; // Placeholder — should be extracted from custom UserDetails
        }
        return 1L; // Fallback for system operations
    }

    private SaleCompletedEvent buildSaleCompletedEvent(SaleEntity sale) {
        List<SaleItemInfo> items = sale.getDetails().stream()
                .filter(d -> d.isActive())
                .map(d -> new SaleItemInfo(
                        d.getProductId(),
                        d.getProductName(),
                        d.getQuantity(),
                        d.getUnitPrice(),
                        d.getSubtotal()
                ))
                .collect(Collectors.toList());

        return new SaleCompletedEvent(
                sale.getId(),
                sale.getSaleNumber(),
                sale.getCashierId(),
                sale.getTotal(),
                sale.getPaymentMethod(),
                sale.getCompletedAt(),
                items
        );
    }

    private SaleVoidedEvent buildSaleVoidedEvent(SaleEntity sale) {
        List<SaleItemInfo> items = sale.getDetails().stream()
                .filter(d -> d.isActive())
                .map(d -> new SaleItemInfo(
                        d.getProductId(),
                        d.getProductName(),
                        d.getQuantity(),
                        d.getUnitPrice(),
                        d.getSubtotal()
                ))
                .collect(Collectors.toList());

        String voidedBy = SecurityContextHolder.getContext().getAuthentication().getName();

        return new SaleVoidedEvent(
                sale.getId(),
                sale.getSaleNumber(),
                voidedBy,
                LocalDateTime.now(),
                sale.getTotal(),
                items
        );
    }

    /**
     * Builds a snapshot map of sale state for forensic audit (B3-03).
     *
     * @param sale the sale entity to snapshot
     * @return map containing sale state for audit record
     */
    private Map<String, Object> buildSaleSnapshot(SaleEntity sale) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", sale.getId());
        snapshot.put("saleNumber", sale.getSaleNumber());
        snapshot.put("status", sale.getStatus() != null ? sale.getStatus().name() : null);
        snapshot.put("cashierId", sale.getCashierId());
        snapshot.put("subtotal", sale.getSubtotal());
        snapshot.put("total", sale.getTotal());
        snapshot.put("paymentMethod", sale.getPaymentMethod() != null ? sale.getPaymentMethod().name() : null);
        snapshot.put("amountReceived", sale.getAmountReceived());
        snapshot.put("change", sale.getChange());
        snapshot.put("completedAt", sale.getCompletedAt());

        // Capture active details
        List<Map<String, Object>> details = sale.getDetails().stream()
                .filter(SaleDetailEntity::isActive)
                .map(d -> {
                    Map<String, Object> detailMap = new LinkedHashMap<>();
                    detailMap.put("id", d.getId());
                    detailMap.put("productId", d.getProductId());
                    detailMap.put("productName", d.getProductName());
                    detailMap.put("quantity", d.getQuantity());
                    detailMap.put("unitPrice", d.getUnitPrice());
                    detailMap.put("subtotal", d.getSubtotal());
                    return detailMap;
                })
                .collect(Collectors.toList());
        snapshot.put("details", details);

        return snapshot;
    }
}
