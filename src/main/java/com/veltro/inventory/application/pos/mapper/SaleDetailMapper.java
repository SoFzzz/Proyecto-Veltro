package com.veltro.inventory.application.pos.mapper;

import com.veltro.inventory.application.pos.dto.SaleDetailResponse;
import com.veltro.inventory.application.shared.dto.AuditInfo;
import com.veltro.inventory.domain.pos.model.SaleDetailEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * MapStruct mapper for {@link SaleDetailEntity} ↔ {@link SaleDetailResponse} (B2-01).
 *
 * <p>Monetary fields ({@code unitPrice}, {@code subtotal}) are converted from {@link BigDecimal}
 * to {@link String} with 4 decimal places (ADR-005).
 */
@Mapper(componentModel = "spring")
public interface SaleDetailMapper {

    @Mapping(target = "unitPrice", source = "unitPrice", qualifiedByName = "bigDecimalToString")
    @Mapping(target = "subtotal", source = "subtotal", qualifiedByName = "bigDecimalToString")
    @Mapping(target = "auditInfo", source = ".")
    SaleDetailResponse toResponse(SaleDetailEntity entity);

    /**
     * Converts BigDecimal to String with 4 decimal places (ADR-005).
     */
    @Named("bigDecimalToString")
    default String bigDecimalToString(BigDecimal value) {
        return value != null ? value.setScale(4, java.math.RoundingMode.HALF_UP).toPlainString() : null;
    }

    /**
     * Extracts audit information from entity.
     */
    default AuditInfo toAuditInfo(SaleDetailEntity entity) {
        return new AuditInfo(
                instantToLocalDateTime(entity.getCreatedAt()),
                entity.getCreatedBy(),
                instantToLocalDateTime(entity.getUpdatedAt()),
                entity.getUpdatedBy()
        );
    }

    /**
     * Converts Instant to LocalDateTime in system default zone.
     */
    default LocalDateTime instantToLocalDateTime(Instant instant) {
        return instant != null ? LocalDateTime.ofInstant(instant, ZoneId.systemDefault()) : null;
    }
}
