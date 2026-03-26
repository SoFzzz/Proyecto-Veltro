package com.veltro.inventory.application.pos.dto;

import com.veltro.inventory.domain.pos.model.PaymentMethod;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Request DTO for confirming a sale (B2-01 | POST /api/v1/sales/{id}/confirm).
 *
 * <p>The {@code amountReceived} field is nullable at the DTO level but validated in
 * {@link com.veltro.inventory.application.pos.service.SaleService#confirm}:
 * <ul>
 *   <li>For CASH payments: must be non-null and >= total (throws {@link com.veltro.inventory.exception.InvalidPaymentException})</li>
 *   <li>For other payment methods: should be null (no change calculated)</li>
 * </ul>
 */
public record ConfirmSaleRequest(
        @NotNull(message = "Payment method is required")
        PaymentMethod paymentMethod,

        BigDecimal amountReceived  // nullable, validated in service for CASH
) {
}
