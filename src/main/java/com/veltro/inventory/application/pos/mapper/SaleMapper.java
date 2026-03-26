package com.veltro.inventory.application.pos.mapper;

import com.veltro.inventory.application.pos.dto.SaleDetailResponse;
import com.veltro.inventory.application.pos.dto.SaleResponse;
import com.veltro.inventory.application.shared.dto.AuditInfo;
import com.veltro.inventory.domain.pos.model.SaleEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MapStruct mapper for {@link SaleEntity} ↔ {@link SaleResponse} (B2-01).
 *
 * <p>Monetary fields ({@code subtotal}, {@code total}, {@code amountReceived}, {@code change})
 * are converted from {@link BigDecimal} to {@link String} with 4 decimal places (ADR-005).
 *
 * <p>Only active details are included in the response (AC-05).
 */
@Mapper(componentModel = "spring")
public abstract class SaleMapper {

    @Autowired
    protected SaleDetailMapper saleDetailMapper;

    @Mapping(target = "subtotal", source = "subtotal", qualifiedByName = "bigDecimalToString")
    @Mapping(target = "total", source = "total", qualifiedByName = "bigDecimalToString")
    @Mapping(target = "amountReceived", source = "amountReceived", qualifiedByName = "bigDecimalToString")
    @Mapping(target = "change", source = "change", qualifiedByName = "bigDecimalToString")
    @Mapping(target = "details", expression = "java(toActiveDetails(entity))")
    @Mapping(target = "auditInfo", expression = "java(toAuditInfo(entity))")
    public abstract SaleResponse toResponse(SaleEntity entity);

    /**
     * Converts BigDecimal to String with 4 decimal places (ADR-005).
     */
    @Named("bigDecimalToString")
    protected String bigDecimalToString(BigDecimal value) {
        return value != null ? value.setScale(4, RoundingMode.HALF_UP).toPlainString() : null;
    }

    /**
     * Filters and maps only active details (AC-05).
     */
    protected List<SaleDetailResponse> toActiveDetails(SaleEntity entity) {
        return entity.getDetails().stream()
                .filter(d -> d.isActive())
                .map(saleDetailMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Extracts audit information from entity.
     */
    protected AuditInfo toAuditInfo(SaleEntity entity) {
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
    protected LocalDateTime instantToLocalDateTime(Instant instant) {
        return instant != null ? LocalDateTime.ofInstant(instant, ZoneId.systemDefault()) : null;
    }
}
