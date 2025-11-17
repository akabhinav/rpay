package com.rpay.application.dto;

import com.rpay.domain.payment.gateway.PaymentDetails;

/**
 * DTO for creating a payment
 */
public record CreatePaymentRequest(
    String orderId,
    String paymentMethod,
    PaymentDetails paymentDetails,
    String callbackUrl
) {
    public CreatePaymentRequest {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("Order ID is required");
        }
        if (paymentMethod == null || paymentMethod.isBlank()) {
            throw new IllegalArgumentException("Payment method is required");
        }
        if (paymentDetails == null) {
            throw new IllegalArgumentException("Payment details are required");
        }
    }
}
