package com.veltro.inventory.application.inventory.service;

import com.veltro.inventory.application.inventory.dto.InventoryMovementResponse;
import com.veltro.inventory.application.inventory.dto.InventoryResponse;
import com.veltro.inventory.application.inventory.dto.StockAdjustmentRequest;
import com.veltro.inventory.application.inventory.dto.StockEntryRequest;
import com.veltro.inventory.application.inventory.dto.StockExitRequest;
import com.veltro.inventory.application.inventory.dto.UpdateStockLimitsRequest;
import com.veltro.inventory.application.inventory.mapper.InventoryMapper;
import com.veltro.inventory.application.inventory.mapper.InventoryMovementMapper;
import com.veltro.inventory.domain.catalog.model.ProductEntity;
import com.veltro.inventory.domain.inventory.model.InventoryEntity;
import com.veltro.inventory.domain.inventory.model.InventoryMovementEntity;
import com.veltro.inventory.domain.inventory.model.MovementType;
import com.veltro.inventory.domain.inventory.ports.InventoryMovementRepository;
import com.veltro.inventory.domain.inventory.ports.InventoryRepository;
import com.veltro.inventory.exception.InsufficientStockException;
import com.veltro.inventory.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service for inventory management (B1-04).
 *
 * Owns all {@link Transactional} boundaries for inventory operations.
 *
 * Every mutating operation (entry, exit, adjustment) records an
 * {@link InventoryMovementEntity} as an append-only audit trail.
 * {@code createdAt} and {@code createdBy} on movements are populated
 * automatically by Spring Data JPA auditing via {@code VeltroAuditorAware} —
 * they are never set manually in this class.
 *
 * AC-04: {@link #recordExit} validates that stock never goes below zero.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryMovementRepository movementRepository;
    private final InventoryMapper inventoryMapper;
    private final InventoryMovementMapper movementMapper;

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    /**
     * Returns the inventory record for the given product.
     * Throws {@link NotFoundException} (HTTP 404) when the product has no inventory row.
     */
    @Transactional(readOnly = true)
    public InventoryResponse findByProductId(Long productId) {
        return inventoryMapper.toResponse(requireByProductId(productId));
    }

    /**
     * Returns a paginated movement history for the given product's inventory (AC-07).
     */
    @Transactional(readOnly = true)
    public Page<InventoryMovementResponse> getMovements(Long productId, Pageable pageable) {
        InventoryEntity inventory = requireByProductId(productId);
        return movementRepository.findByInventoryId(inventory.getId(), pageable)
                .map(movementMapper::toResponse);
    }

    // -------------------------------------------------------------------------
    // Commands — called by InventoryController
    // -------------------------------------------------------------------------

    /**
     * Records a stock entry (goods received, manual addition).
     * Increases {@code currentStock} by {@code request.quantity()} and
     * persists an ENTRY movement.
     */
    @Transactional
    public InventoryResponse recordEntry(Long productId, StockEntryRequest request) {
        InventoryEntity inventory = requireByProductId(productId);

        int previousStock = inventory.getCurrentStock();
        int newStock = previousStock + request.quantity();

        inventory.setCurrentStock(newStock);
        InventoryEntity saved = inventoryRepository.save(inventory);

        persistMovement(saved, MovementType.ENTRY, request.quantity(), previousStock, newStock, request.reason());

        log.info("Stock ENTRY: productId={}, qty={}, stock {} -> {}",
                productId, request.quantity(), previousStock, newStock);
        return inventoryMapper.toResponse(saved);
    }

    /**
     * Records a stock exit (shrinkage, manual removal).
     * Validates that stock will not go negative (AC-04).
     * Throws {@link InsufficientStockException} (HTTP 422) if insufficient stock.
     */
    @Transactional
    public InventoryResponse recordExit(Long productId, StockExitRequest request) {
        InventoryEntity inventory = requireByProductId(productId);

        int previousStock = inventory.getCurrentStock();
        if (previousStock - request.quantity() < 0) {
            throw new InsufficientStockException(
                    inventory.getProduct().getName(), previousStock, request.quantity());
        }

        int newStock = previousStock - request.quantity();
        inventory.setCurrentStock(newStock);
        InventoryEntity saved = inventoryRepository.save(inventory);

        persistMovement(saved, MovementType.EXIT, request.quantity(), previousStock, newStock, request.reason());

        log.info("Stock EXIT: productId={}, qty={}, stock {} -> {}",
                productId, request.quantity(), previousStock, newStock);
        return inventoryMapper.toResponse(saved);
    }

    /**
     * Sets stock to an absolute value (physical count correction).
     * The quantity stored in the movement record is always positive:
     * {@code |newStock - previousStock|}, with the direction captured in
     * the ADJUSTMENT type.
     */
    @Transactional
    public InventoryResponse recordAdjustment(Long productId, StockAdjustmentRequest request) {
        InventoryEntity inventory = requireByProductId(productId);

        int previousStock = inventory.getCurrentStock();
        int newStock = request.newStock();
        int delta = Math.abs(newStock - previousStock);

        // If stock is unchanged we still record the movement as confirmation of count.
        int quantityForRecord = (delta == 0) ? 0 : delta;

        // The DB constraint requires quantity > 0; use 1 as the sentinel for no-change adjustments.
        if (quantityForRecord == 0) {
            quantityForRecord = 1;
        }

        inventory.setCurrentStock(newStock);
        InventoryEntity saved = inventoryRepository.save(inventory);

        persistMovement(saved, MovementType.ADJUSTMENT, quantityForRecord, previousStock, newStock, request.reason());

        log.info("Stock ADJUSTMENT: productId={}, stock {} -> {}",
                productId, previousStock, newStock);
        return inventoryMapper.toResponse(saved);
    }

    /**
     * Updates the min/max stock thresholds used for alert evaluation (B2-03).
     */
    @Transactional
    public InventoryResponse updateLimits(Long productId, UpdateStockLimitsRequest request) {
        InventoryEntity inventory = requireByProductId(productId);

        inventory.setMinStock(request.minStock());
        inventory.setMaxStock(request.maxStock());
        InventoryEntity saved = inventoryRepository.save(inventory);

        log.info("Stock limits updated: productId={}, min={}, max={}",
                productId, request.minStock(), request.maxStock());
        return inventoryMapper.toResponse(saved);
    }

    // -------------------------------------------------------------------------
    // Package-scoped factory — called by ProductService on product creation
    // -------------------------------------------------------------------------

    /**
     * Creates an initial {@link InventoryEntity} with zero stock for a newly
     * created product. Called within the same transaction as the product save,
     * so both entities are persisted atomically.
     *
     * Must be called inside an active transaction (the caller owns it).
     */
    @Transactional
    public InventoryEntity createForProduct(ProductEntity product) {
        InventoryEntity inventory = new InventoryEntity();
        inventory.setProduct(product);
        inventory.setCurrentStock(0);
        inventory.setMinStock(0);
        inventory.setMaxStock(0);
        InventoryEntity saved = inventoryRepository.save(inventory);
        log.info("Inventory record created: productId={}, inventoryId={}", product.getId(), saved.getId());
        return saved;
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private InventoryEntity requireByProductId(Long productId) {
        return inventoryRepository.findByProductIdAndActiveTrue(productId)
                .orElseThrow(() -> new NotFoundException(
                        "Inventory not found for product id: " + productId));
    }

    /**
     * Builds and saves an {@link InventoryMovementEntity}.
     * {@code createdAt} and {@code createdBy} are NOT set here — Spring Data JPA
     * auditing via {@code @CreatedDate}/{@code @CreatedBy} + {@code AuditingEntityListener}
     * populates them automatically on persist.
     */
    private void persistMovement(InventoryEntity inventory, MovementType type,
                                  int quantity, int previousStock, int newStock, String reason) {
        InventoryMovementEntity movement = new InventoryMovementEntity();
        movement.setInventory(inventory);
        movement.setMovementType(type);
        movement.setQuantity(quantity);
        movement.setPreviousStock(previousStock);
        movement.setNewStock(newStock);
        movement.setReason(reason);
        movementRepository.save(movement);
    }
}
