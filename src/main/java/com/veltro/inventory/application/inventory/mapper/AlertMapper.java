package com.veltro.inventory.application.inventory.mapper;

import com.veltro.inventory.application.inventory.dto.AlertResponse;
import com.veltro.inventory.domain.inventory.model.AlertEntity;
import java.time.OffsetDateTime;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface AlertMapper {

    @Mapping(target = "productId", expression = "java(entity.getProduct() != null ? entity.getProduct().getId() : null)")
    @Mapping(target = "productName", expression = "java(entity.getProduct() != null ? entity.getProduct().getName() : null)")
    @Mapping(target = "type", expression = "java(entity.getType() != null ? entity.getType().name() : null)")
    @Mapping(target = "severity", expression = "java(entity.getSeverity() != null ? entity.getSeverity().name() : null)")
    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "instantToOffset")
    AlertResponse toResponse(AlertEntity entity);

    @Named("instantToOffset")
    default OffsetDateTime instantToOffset(java.time.Instant instant) {
        return instant != null ? instant.atOffset(java.time.ZoneOffset.UTC) : null;
    }
}
