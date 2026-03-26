package com.veltro.inventory.application.audit.mapper;

import com.veltro.inventory.application.audit.dto.AuditRecordResponse;
import com.veltro.inventory.domain.audit.model.AuditRecordEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/**
 * MapStruct mapper for AuditRecordEntity ↔ DTOs (B3-03).
 * 
 * <p>Component model is SPRING, so this mapper is auto-registered as a Spring bean
 * and can be injected via constructor.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AuditRecordMapper {

    /**
     * Maps AuditRecordEntity to AuditRecordResponse DTO.
     * 
     * @param entity the audit record entity
     * @return the response DTO
     */
    AuditRecordResponse toResponse(AuditRecordEntity entity);
}
