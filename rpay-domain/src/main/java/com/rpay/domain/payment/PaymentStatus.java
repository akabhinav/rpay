package com.rpay.domain.payment;

/**
 * Represents the lifecycle states of a payment.
 * State transitions follow strict rules to maintain data integrity.
 */
public enum PaymentStatus {
    /**
     * Payment created but not yet attempted
     */
    CREATED,

    /**
     * Payment is being processed by payment gateway
     */
    PROCESSING,

    /**
     * Payment requires additional authentication (3DS, OTP)
     */
    PENDING_AUTHORIZATION,

    /**
     * Payment successfully completed
     */
    CAPTURED,

    /**
     * Payment authorized but not yet captured (for two-step payments)
     */
    AUTHORIZED,

    /**
     * Payment failed due to various reasons
     */
    FAILED,

    /**
     * Payment cancelled by user or system
     */
    CANCELLED,

    /**
     * Payment refunded (full or partial)
     */
    REFUNDED,

    /**
     * Payment timed out
     */
    EXPIRED;

    /**
     * Check if payment can transition to target status
     */
    public boolean canTransitionTo(PaymentStatus targetStatus) {
        return switch (this) {
            case CREATED -> targetStatus == PROCESSING || targetStatus == CANCELLED || targetStatus == EXPIRED;
            case PROCESSING -> targetStatus == PENDING_AUTHORIZATION || targetStatus == CAPTURED ||
                              targetStatus == AUTHORIZED || targetStatus == FAILED;
            case PENDING_AUTHORIZATION -> targetStatus == CAPTURED || targetStatus == FAILED ||
                                         targetStatus == CANCELLED;
            case AUTHORIZED -> targetStatus == CAPTURED || targetStatus == CANCELLED || targetStatus == EXPIRED;
            case CAPTURED -> targetStatus == REFUNDED;
            case FAILED, CANCELLED, REFUNDED, EXPIRED -> false; // Terminal states
        };
    }

    public boolean isTerminal() {
        return this == CAPTURED || this == FAILED || this == CANCELLED || this == REFUNDED || this == EXPIRED;
    }

    public boolean isSuccessful() {
        return this == CAPTURED || this == AUTHORIZED;
    }
}
