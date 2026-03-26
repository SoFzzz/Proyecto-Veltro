package com.veltro.inventory.application.catalog.service;

import com.veltro.inventory.application.catalog.dto.CategoryResponse;
import com.veltro.inventory.application.catalog.dto.CreateCategoryRequest;
import com.veltro.inventory.application.catalog.dto.UpdateCategoryRequest;
import com.veltro.inventory.application.catalog.mapper.CategoryMapper;
import com.veltro.inventory.domain.catalog.model.CategoryEntity;
import com.veltro.inventory.domain.catalog.ports.CategoryRepository;
import com.veltro.inventory.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Application service for category management (B1-03).
 *
 * Owns all {@link Transactional} boundaries for category operations.
 * Delegates persistence to the {@link CategoryRepository} domain port.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    /**
     * Returns all root categories (those with no parent) with their subcategory
     * trees populated. This is the primary tree-view endpoint.
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> findRoots() {
        return categoryRepository.findAllByParentCategoryIsNullAndActiveTrue()
                .stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse findById(Long id) {
        CategoryEntity entity = requireActive(id);
        return categoryMapper.toResponse(entity);
    }

    // -------------------------------------------------------------------------
    // Commands
    // -------------------------------------------------------------------------

    @Transactional
    public CategoryResponse create(CreateCategoryRequest request) {
        CategoryEntity entity = categoryMapper.toEntity(request);

        if (request.parentCategoryId() != null) {
            CategoryEntity parent = requireActive(request.parentCategoryId());
            entity.setParentCategory(parent);
        }

        CategoryEntity saved = categoryRepository.save(entity);
        log.info("Category created: id={}, name={}", saved.getId(), saved.getName());
        return categoryMapper.toResponse(saved);
    }

    @Transactional
    public CategoryResponse update(Long id, UpdateCategoryRequest request) {
        CategoryEntity entity = requireActive(id);
        categoryMapper.updateEntity(request, entity);

        if (request.parentCategoryId() != null) {
            CategoryEntity parent = requireActive(request.parentCategoryId());
            entity.setParentCategory(parent);
        } else {
            entity.setParentCategory(null);
        }

        CategoryEntity saved = categoryRepository.save(entity);
        log.info("Category updated: id={}, name={}", saved.getId(), saved.getName());
        return categoryMapper.toResponse(saved);
    }

    /**
     * Soft-deletes a category (AC-05). The record remains in the database
     * with {@code active=false} and will not appear in active listings.
     */
    @Transactional
    public void deactivate(Long id) {
        CategoryEntity entity = requireActive(id);
        entity.setActive(false);
        categoryRepository.save(entity);
        log.info("Category deactivated: id={}", id);
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private CategoryEntity requireActive(Long id) {
        return categoryRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + id));
    }
}
