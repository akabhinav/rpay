package com.rpay.domain.payment.gateway;

/**
 * Response from refund operations
 */
public record RefundGatewayResponse(
    boolean success,
    String gatewayRefundId,
    String message,
    String errorCode
) {
    public static RefundGatewayResponse success(String gatewayRefundId, String message) {
        return new RefundGatewayResponse(true, gatewayRefundId, message, null);
    }

    public static RefundGatewayResponse failure(String errorCode, String message) {
        return new RefundGatewayResponse(false, null, message, errorCode);
    }
}
