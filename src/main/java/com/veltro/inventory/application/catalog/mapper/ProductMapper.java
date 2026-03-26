package com.veltro.inventory.application.catalog.mapper;

import com.veltro.inventory.application.catalog.dto.CreateProductRequest;
import com.veltro.inventory.application.catalog.dto.ProductResponse;
import com.veltro.inventory.application.catalog.dto.UpdateProductRequest;
import com.veltro.inventory.domain.catalog.model.ProductEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * MapStruct mapper for {@link ProductEntity} ↔ product DTOs.
 *
 * ADR-005: Monetary BigDecimal fields are converted to String with exactly
 * 4 decimal places to prevent precision loss in JSON serialization.
 */
@Mapper(componentModel = "spring")
public interface ProductMapper {

    /**
     * Maps entity to response DTO.
     * Monetary fields are formatted to 4 decimal place strings per ADR-005.
     * {@code categoryId} and {@code categoryName} are sourced from the lazy association.
     */
    @Mapping(target = "costPrice", source = "costPrice", qualifiedByName = "decimalToString")
    @Mapping(target = "salePrice", source = "salePrice", qualifiedByName = "decimalToString")
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    ProductResponse toResponse(ProductEntity entity);

    /**
     * Maps a create request to a new entity.
     * {@code id}, {@code category}, and audit fields are excluded;
     * the service resolves the {@code category} from the repository.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    ProductEntity toEntity(CreateProductRequest request);

    /**
     * Applies an update request to an existing entity in-place.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntity(UpdateProductRequest request, @MappingTarget ProductEntity entity);

    /**
     * Formats a BigDecimal to a String with exactly 4 decimal places (ADR-005).
     */
    @Named("decimalToString")
    default String decimalToString(BigDecimal value) {
        if (value == null) {
            return null;
        }
        return value.setScale(4, RoundingMode.HALF_UP).toPlainString();
    }
}
