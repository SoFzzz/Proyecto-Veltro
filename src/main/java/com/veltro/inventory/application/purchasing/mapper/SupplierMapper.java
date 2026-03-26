package com.veltro.inventory.application.purchasing.mapper;

import com.veltro.inventory.application.purchasing.dto.CreateSupplierRequest;
import com.veltro.inventory.application.purchasing.dto.SupplierResponse;
import com.veltro.inventory.application.purchasing.dto.UpdateSupplierRequest;
import com.veltro.inventory.domain.purchasing.model.SupplierEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * MapStruct mapper for {@link SupplierEntity} ↔ supplier DTOs (B2-04).
 */
@Mapper(componentModel = "spring")
public interface SupplierMapper {

    /**
     * Maps entity to response DTO.
     */
    @Mapping(target = "name", source = "companyName")
    SupplierResponse toResponse(SupplierEntity entity);

    /**
     * Maps a create request to a new entity.
     * {@code id} and audit fields are excluded; set by framework.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "companyName", source = "name")
    SupplierEntity toEntity(CreateSupplierRequest request);

    /**
     * Applies an update request to an existing entity in-place.
     * taxId cannot be updated (immutable after creation).
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "taxId", ignore = true)
    @Mapping(target = "companyName", source = "name")
    void updateEntity(UpdateSupplierRequest request, @MappingTarget SupplierEntity entity);
}