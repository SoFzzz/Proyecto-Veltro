package com.veltro.inventory.application.inventory.mapper;

import com.veltro.inventory.application.inventory.dto.InventoryMovementResponse;
import com.veltro.inventory.domain.inventory.model.InventoryMovementEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for {@link InventoryMovementEntity} ↔ movement DTOs.
 *
 * {@code movementType} is the string representation of the enum
 * (MapStruct maps enum → String by default via {@code name()}).
 */
@Mapper(componentModel = "spring")
public interface InventoryMovementMapper {

    @Mapping(target = "inventoryId", source = "inventory.id")
    @Mapping(target = "movementType", source = "movementType")
    InventoryMovementResponse toResponse(InventoryMovementEntity entity);
}
