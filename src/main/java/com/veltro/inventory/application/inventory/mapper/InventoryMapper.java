package com.veltro.inventory.application.inventory.mapper;

import com.veltro.inventory.application.inventory.dto.InventoryResponse;
import com.veltro.inventory.domain.inventory.model.InventoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for {@link InventoryEntity} ↔ inventory DTOs.
 */
@Mapper(componentModel = "spring")
public interface InventoryMapper {

    /**
     * Maps entity to response DTO.
     * Product name and id are sourced from the lazy association.
     */
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    InventoryResponse toResponse(InventoryEntity entity);
}
