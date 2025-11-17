package com.rpay.domain.refund;

/**
 * Status of refund request
 */
public enum RefundStatus {
    INITIATED,
    PROCESSING,
    COMPLETED,
    FAILED,
    CANCELLED
}
