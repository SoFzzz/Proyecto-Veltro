package com.veltro.inventory.application.catalog.mapper;

import com.veltro.inventory.application.catalog.dto.CategoryResponse;
import com.veltro.inventory.application.catalog.dto.CreateCategoryRequest;
import com.veltro.inventory.application.catalog.dto.UpdateCategoryRequest;
import com.veltro.inventory.domain.catalog.model.CategoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * MapStruct mapper for {@link CategoryEntity} ↔ category DTOs.
 *
 * {@code componentModel = "spring"} registers the generated implementation
 * as a Spring bean, injectable via {@code @RequiredArgsConstructor}.
 */
@Mapper(componentModel = "spring")
public interface CategoryMapper {

    /**
     * Maps entity to response DTO.
     * The {@code parentCategoryId} is extracted from the parent entity's id (nullable).
     * The recursive {@code subCategories} list is mapped by the same mapper instance.
     */
    @Mapping(target = "parentCategoryId", source = "parentCategory.id")
    CategoryResponse toResponse(CategoryEntity entity);

    /**
     * Maps a create request to a new entity.
     * {@code id}, {@code parentCategory}, and audit fields are excluded here;
     * the service resolves {@code parentCategory} from the repository.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parentCategory", ignore = true)
    @Mapping(target = "subCategories", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    CategoryEntity toEntity(CreateCategoryRequest request);

    /**
     * Applies an update request to an existing entity in-place.
     * Parent and audit fields are managed by the service.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parentCategory", ignore = true)
    @Mapping(target = "subCategories", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntity(UpdateCategoryRequest request, @MappingTarget CategoryEntity entity);
}
