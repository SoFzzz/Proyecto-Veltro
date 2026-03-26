package com.veltro.inventory.infrastructure.adapters.web;

import com.veltro.inventory.application.catalog.dto.CategoryResponse;
import com.veltro.inventory.application.catalog.dto.CreateCategoryRequest;
import com.veltro.inventory.application.catalog.service.CategoryService;
import com.veltro.inventory.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link CategoryController} (B1-03).
 *
 * Pure unit testing approach using direct method calls instead of MockMvc.
 */
@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;
    
    private CategoryController controller;

    @BeforeEach
    void setUp() {
        controller = new CategoryController(categoryService);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static CategoryResponse stubRootCategory() {
        return new CategoryResponse(1L, "Electronics", "Electronic goods", null, true,
                List.of(new CategoryResponse(2L, "Phones", null, 1L, true, List.of())));
    }

    private static CategoryResponse stubSubCategory() {
        return new CategoryResponse(2L, "Phones", null, 1L, true, List.of());
    }

    // -------------------------------------------------------------------------
    // GET /categories — list roots
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /categories returns 200 with root category list")
    void listRoots_returns200WithList() {
        when(categoryService.findRoots()).thenReturn(List.of(stubRootCategory()));

        ResponseEntity<List<CategoryResponse>> response = controller.listRoots();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).id()).isEqualTo(1L);
        assertThat(response.getBody().get(0).name()).isEqualTo("Electronics");
        assertThat(response.getBody().get(0).subCategories()).hasSize(1);
        assertThat(response.getBody().get(0).subCategories().get(0).name()).isEqualTo("Phones");
        verify(categoryService).findRoots();
    }

    @Test
    @DisplayName("GET /categories returns empty list when no categories exist")
    void listRoots_noCategories_returnsEmptyList() {
        when(categoryService.findRoots()).thenReturn(List.of());

        ResponseEntity<List<CategoryResponse>> response = controller.listRoots();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEmpty();
        verify(categoryService).findRoots();
    }

    // -------------------------------------------------------------------------
    // GET /categories/{id}
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /categories/{id} returns 200 with category when found")
    void findById_existingCategory_returns200() {
        when(categoryService.findById(1L)).thenReturn(stubRootCategory());

        ResponseEntity<CategoryResponse> response = controller.findById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(1L);
        assertThat(response.getBody().name()).isEqualTo("Electronics");
        assertThat(response.getBody().parentCategoryId()).isNull();
        verify(categoryService).findById(1L);
    }

    @Test
    @DisplayName("GET /categories/{id} throws NotFoundException when category does not exist")
    void findById_missingCategory_throwsNotFoundException() {
        when(categoryService.findById(999L))
                .thenThrow(new NotFoundException("Category not found with id: 999"));

        assertThatThrownBy(() -> controller.findById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Category not found with id: 999");

        verify(categoryService).findById(999L);
    }

    // -------------------------------------------------------------------------
    // POST /categories
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /categories with valid request returns 201")
    void create_validRequest_returns201() {
        when(categoryService.create(any(CreateCategoryRequest.class)))
                .thenReturn(stubRootCategory());

        CreateCategoryRequest request = new CreateCategoryRequest("Electronics", "Electronic goods", null);
        ResponseEntity<CategoryResponse> response = controller.create(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(1L);
        assertThat(response.getBody().name()).isEqualTo("Electronics");
        verify(categoryService).create(any(CreateCategoryRequest.class));
    }

    @Test
    @DisplayName("POST /categories with subcategory returns 201")
    void create_subCategory_returns201() {
        when(categoryService.create(any(CreateCategoryRequest.class)))
                .thenReturn(stubSubCategory());

        CreateCategoryRequest request = new CreateCategoryRequest("Phones", null, 1L);
        ResponseEntity<CategoryResponse> response = controller.create(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().parentCategoryId()).isEqualTo(1L);
        verify(categoryService).create(request);
    }

    @Test
    @DisplayName("POST /categories delegates to CategoryService")
    void create_delegatesToService() {
        when(categoryService.create(any(CreateCategoryRequest.class)))
                .thenReturn(stubRootCategory());

        CreateCategoryRequest request = new CreateCategoryRequest("Test Category", "Description", null);
        controller.create(request);

        verify(categoryService).create(request);
    }

    // -------------------------------------------------------------------------
    // PUT /categories/{id}/deactivate
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("PUT /categories/{id}/deactivate returns 204")
    void deactivate_validId_returns204() {
        doNothing().when(categoryService).deactivate(1L);

        ResponseEntity<Void> response = controller.deactivate(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
        verify(categoryService).deactivate(1L);
    }

    @Test
    @DisplayName("PUT /categories/{id}/deactivate throws NotFoundException when category not found")
    void deactivate_missingCategory_throwsNotFoundException() {
        doThrow(new NotFoundException("Category not found with id: 999"))
                .when(categoryService).deactivate(999L);

        assertThatThrownBy(() -> controller.deactivate(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Category not found with id: 999");

        verify(categoryService).deactivate(999L);
    }

    @Test
    @DisplayName("PUT /categories/{id}/deactivate delegates to CategoryService")
    void deactivate_delegatesToService() {
        doNothing().when(categoryService).deactivate(5L);

        controller.deactivate(5L);

        verify(categoryService).deactivate(5L);
    }
}