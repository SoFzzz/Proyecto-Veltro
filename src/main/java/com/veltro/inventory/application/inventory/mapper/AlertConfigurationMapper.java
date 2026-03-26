package com.veltro.inventory.application.inventory.mapper;

import com.veltro.inventory.application.inventory.dto.AlertConfigurationResponse;
import com.veltro.inventory.domain.inventory.model.AlertConfigurationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AlertConfigurationMapper {

    @Mapping(target = "productId", expression = "java(entity.getProduct() != null ? entity.getProduct().getId() : null)")
    AlertConfigurationResponse toResponse(AlertConfigurationEntity entity);
}
