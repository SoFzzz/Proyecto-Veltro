package com.veltro.inventory.application.purchasing.mapper;

import com.veltro.inventory.application.purchasing.dto.PurchaseOrderDetailResponse;
import com.veltro.inventory.domain.purchasing.model.PurchaseOrderDetailEntity;
import com.veltro.inventory.application.shared.dto.AuditInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * MapStruct mapper for {@link PurchaseOrderDetailEntity} ↔ {@link PurchaseOrderDetailResponse} (B2-04).
 *
 * <p>ADR-005: Monetary fields are converted to String with 4 decimal places.
 */
@Mapper(componentModel = "spring")
public abstract class PurchaseOrderDetailMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "unitCost", source = "unitCost", qualifiedByName = "bigDecimalToString")
    @Mapping(target = "subtotal", expression = "java(calculateSubtotal(entity))")
    @Mapping(target = "auditInfo", expression = "java(toAuditInfo(entity))")
    public abstract PurchaseOrderDetailResponse toResponse(PurchaseOrderDetailEntity entity);

    /**
     * Converts BigDecimal to String with 4 decimal places (ADR-005).
     */
    @Named("bigDecimalToString")
    protected String bigDecimalToString(BigDecimal value) {
        return value != null ? value.setScale(4, RoundingMode.HALF_UP).toPlainString() : null;
    }

    /**
     * Calculates subtotal as unitCost * requestedQuantity.
     */
    protected String calculateSubtotal(PurchaseOrderDetailEntity entity) {
        if (entity.getUnitCost() == null || entity.getRequestedQuantity() == null) {
            return "0.0000";
        }
        BigDecimal subtotal = entity.getUnitCost().multiply(BigDecimal.valueOf(entity.getRequestedQuantity()));
        return bigDecimalToString(subtotal);
    }

    /**
     * Extracts audit information from entity.
     */
    protected AuditInfo toAuditInfo(PurchaseOrderDetailEntity entity) {
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