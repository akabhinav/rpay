package com.rpay.application.dto;

import java.math.BigDecimal;
import java.util.Map;

/**
 * DTO for creating an order
 */
public record CreateOrderRequest(
    String merchantId,
    BigDecimal amount,
    String currency,
    String customerId,
    String customerEmail,
    String customerPhone,
    String receipt,
    String notes,
    Map<String, String> metadata
) {
    public CreateOrderRequest {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("Currency is required");
        }
    }
}
