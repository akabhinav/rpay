package com.rpay.domain.payment.gateway;

/**
 * Response from payment gateway operations
 */
public record PaymentGatewayResponse(
    boolean success,
    String gatewayTransactionId,
    PaymentGatewayStatus status,
    String message,
    String errorCode,
    String redirectUrl  // For redirect-based flows
) {
    public static PaymentGatewayResponse success(String gatewayTransactionId, String message) {
        return new PaymentGatewayResponse(
            true,
            gatewayTransactionId,
            PaymentGatewayStatus.SUCCESS,
            message,
            null,
            null
        );
    }

    public static PaymentGatewayResponse pending(String gatewayTransactionId, String redirectUrl) {
        return new PaymentGatewayResponse(
            true,
            gatewayTransactionId,
            PaymentGatewayStatus.PENDING,
            "Payment pending user action",
            null,
            redirectUrl
        );
    }

    public static PaymentGatewayResponse failure(String errorCode, String message) {
        return new PaymentGatewayResponse(
            false,
            null,
            PaymentGatewayStatus.FAILED,
            message,
            errorCode,
            null
        );
    }

    public enum PaymentGatewayStatus {
        SUCCESS,
        PENDING,
        FAILED,
        CANCELLED
    }
}
