package com.veltro.inventory.application.catalog.service;

import com.veltro.inventory.application.catalog.dto.CreateProductRequest;
import com.veltro.inventory.application.catalog.dto.ProductResponse;
import com.veltro.inventory.application.catalog.dto.UpdateProductRequest;
import com.veltro.inventory.application.catalog.mapper.ProductMapper;
import com.veltro.inventory.application.inventory.service.InventoryService;
import com.veltro.inventory.domain.catalog.model.CategoryEntity;
import com.veltro.inventory.domain.catalog.model.ProductEntity;
import com.veltro.inventory.domain.catalog.ports.CategoryRepository;
import com.veltro.inventory.domain.catalog.ports.ProductRepository;
import com.veltro.inventory.exception.InvalidPriceException;
import com.veltro.inventory.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service for product management (B1-03).
 *
 * Owns all {@link Transactional} boundaries for product operations.
 * Enforces the domain price constraint: salePrice must be >= costPrice.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    private final InventoryService inventoryService;

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    /**
     * Returns a paginated page of active products (AC-07).
     */
    @Transactional(readOnly = true)
    public Page<ProductResponse> findAll(Pageable pageable) {
        return productRepository.findAllByActiveTrue(pageable)
                .map(productMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        return productMapper.toResponse(requireActive(id));
    }

    /**
     * Looks up a product by its barcode — used by the POS scanner (UC-01).
     * Uses the B-Tree index on {@code barcode} created in V1 migration.
     */
    @Transactional(readOnly = true)
    public ProductResponse findByBarcode(String barcode) {
        ProductEntity entity = productRepository.findByBarcodeAndActiveTrue(barcode)
                .orElseThrow(() -> new NotFoundException(
                        "Product not found with barcode: " + barcode));
        return productMapper.toResponse(entity);
    }

    // -------------------------------------------------------------------------
    // Commands
    // -------------------------------------------------------------------------

    @Transactional
    public ProductResponse create(CreateProductRequest request) {
        validatePrice(request.costPrice(), request.salePrice());

        ProductEntity entity = productMapper.toEntity(request);
        resolveCategory(entity, request.categoryId());

        ProductEntity saved = productRepository.save(entity);
        inventoryService.createForProduct(saved);
        log.info("Product created: id={}, barcode={}", saved.getId(), saved.getBarcode());
        return productMapper.toResponse(saved);
    }

    @Transactional
    public ProductResponse update(Long id, UpdateProductRequest request) {
        validatePrice(request.costPrice(), request.salePrice());

        ProductEntity entity = requireActive(id);
        productMapper.updateEntity(request, entity);
        resolveCategory(entity, request.categoryId());

        ProductEntity saved = productRepository.save(entity);
        log.info("Product updated: id={}", saved.getId());
        return productMapper.toResponse(saved);
    }

    /**
     * Soft-deletes a product (AC-05). Sets {@code active=false};
     * the record is retained for audit and purchasing history.
     */
    @Transactional
    public void deactivate(Long id) {
        ProductEntity entity = requireActive(id);
        entity.setActive(false);
        productRepository.save(entity);
        log.info("Product deactivated: id={}", id);
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /**
     * Enforces the domain constraint: salePrice must be >= costPrice.
     * Throws {@link InvalidPriceException} (mapped to HTTP 422) if violated.
     */
    private void validatePrice(java.math.BigDecimal costPrice, java.math.BigDecimal salePrice) {
        if (salePrice.compareTo(costPrice) < 0) {
            throw new InvalidPriceException(
                    "Sale price (" + salePrice + ") must be greater than or equal to cost price (" + costPrice + ").");
        }
    }

    private ProductEntity requireActive(Long id) {
        return productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + id));
    }

    private void resolveCategory(ProductEntity entity, Long categoryId) {
        if (categoryId != null) {
            CategoryEntity category = categoryRepository.findByIdAndActiveTrue(categoryId)
                    .orElseThrow(() -> new NotFoundException("Category not found with id: " + categoryId));
            entity.setCategory(category);
        } else {
            entity.setCategory(null);
        }
    }
}
