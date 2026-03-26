package com.veltro.inventory.application.catalog.service;

import com.veltro.inventory.application.catalog.dto.CreateProductRequest;
import com.veltro.inventory.application.catalog.dto.ProductResponse;
import com.veltro.inventory.application.catalog.mapper.ProductMapper;
import com.veltro.inventory.application.inventory.service.InventoryService;
import com.veltro.inventory.domain.catalog.model.ProductEntity;
import com.veltro.inventory.domain.catalog.ports.CategoryRepository;
import com.veltro.inventory.domain.catalog.ports.ProductRepository;
import com.veltro.inventory.exception.InvalidPriceException;
import com.veltro.inventory.exception.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ProductService} (B1-03).
 *
 * Exercises price validation and barcode-not-found paths in isolation
 * (no Spring context, no database). All collaborators are Mockito mocks.
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private ProductService productService;

    // -------------------------------------------------------------------------
    // validatePrice — InvalidPriceException
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("create throws InvalidPriceException when salePrice < costPrice")
    void create_salePriceLessThanCostPrice_throwsInvalidPriceException() {
        CreateProductRequest request = new CreateProductRequest(
                "Widget",
                "123456789",
                "WGT-001",
                "A test widget",
                new BigDecimal("10.0000"),   // costPrice
                new BigDecimal("9.9999"),    // salePrice — violates constraint
                null
        );

        assertThatThrownBy(() -> productService.create(request))
                .isInstanceOf(InvalidPriceException.class)
                .hasMessageContaining("Sale price")
                .hasMessageContaining("cost price");
    }

    @Test
    @DisplayName("create succeeds when salePrice equals costPrice")
    void create_salePriceEqualsCostPrice_succeeds() {
        BigDecimal price = new BigDecimal("5.0000");
        CreateProductRequest request = new CreateProductRequest(
                "Widget",
                "123456789",
                "WGT-001",
                "A test widget",
                price,
                price,
                null
        );

        ProductEntity entity = new ProductEntity();
        ProductResponse stubResponse = new ProductResponse(
                1L, "Widget", "123456789", "WGT-001", "A test widget",
                "5.0000", "5.0000", null, null, true);

        when(productMapper.toEntity(any(CreateProductRequest.class))).thenReturn(entity);
        when(productRepository.save(any(ProductEntity.class))).thenReturn(entity);
        when(productMapper.toResponse(any(ProductEntity.class))).thenReturn(stubResponse);

        ProductResponse result = productService.create(request);

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Widget");
    }

    @Test
    @DisplayName("create succeeds when salePrice is greater than costPrice")
    void create_salePriceGreaterThanCostPrice_succeeds() {
        CreateProductRequest request = new CreateProductRequest(
                "Widget",
                "123456789",
                "WGT-001",
                "A test widget",
                new BigDecimal("5.0000"),
                new BigDecimal("9.9999"),
                null
        );

        ProductEntity entity = new ProductEntity();
        ProductResponse stubResponse = new ProductResponse(
                1L, "Widget", "123456789", "WGT-001", "A test widget",
                "5.0000", "9.9999", null, null, true);

        when(productMapper.toEntity(any(CreateProductRequest.class))).thenReturn(entity);
        when(productRepository.save(any(ProductEntity.class))).thenReturn(entity);
        when(productMapper.toResponse(any(ProductEntity.class))).thenReturn(stubResponse);

        ProductResponse result = productService.create(request);

        assertThat(result.salePrice()).isEqualTo("9.9999");
    }

    // -------------------------------------------------------------------------
    // findByBarcode — NotFoundException
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findByBarcode throws NotFoundException when barcode does not exist")
    void findByBarcode_unknownBarcode_throwsNotFoundException() {
        when(productRepository.findByBarcodeAndActiveTrue("UNKNOWN-BARCODE"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findByBarcode("UNKNOWN-BARCODE"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("UNKNOWN-BARCODE");
    }

    @Test
    @DisplayName("findByBarcode returns ProductResponse when barcode exists")
    void findByBarcode_knownBarcode_returnsResponse() {
        ProductEntity entity = new ProductEntity();
        ProductResponse stubResponse = new ProductResponse(
                42L, "Chip", "BARCODE-001", "CHI-001", null,
                "1.0000", "2.0000", null, null, true);

        when(productRepository.findByBarcodeAndActiveTrue("BARCODE-001"))
                .thenReturn(Optional.of(entity));
        when(productMapper.toResponse(entity)).thenReturn(stubResponse);

        ProductResponse result = productService.findByBarcode("BARCODE-001");

        assertThat(result.id()).isEqualTo(42L);
        assertThat(result.barcode()).isEqualTo("BARCODE-001");
    }

    // -------------------------------------------------------------------------
    // findById — NotFoundException
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findById throws NotFoundException when product does not exist or is inactive")
    void findById_unknownId_throwsNotFoundException() {
        when(productRepository.findByIdAndActiveTrue(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("999");
    }
}
