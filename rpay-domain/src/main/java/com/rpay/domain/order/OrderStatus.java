package com.rpay.domain.order;

/**
 * Represents the lifecycle states of an order.
 */
public enum OrderStatus {
    /**
     * Order created and awaiting payment
     */
    CREATED,

    /**
     * Payment is being attempted
     */
    ATTEMPTED,

    /**
     * Order successfully paid
     */
    PAID,

    /**
     * Order cancelled
     */
    CANCELLED,

    /**
     * Order expired (payment window closed)
     */
    EXPIRED;

    public boolean canTransitionTo(OrderStatus targetStatus) {
        return switch (this) {
            case CREATED -> targetStatus == ATTEMPTED || targetStatus == CANCELLED || targetStatus == EXPIRED;
            case ATTEMPTED -> targetStatus == PAID || targetStatus == CANCELLED || targetStatus == EXPIRED;
            case PAID, CANCELLED, EXPIRED -> false; // Terminal states
        };
    }

    public boolean isTerminal() {
        return this == PAID || this == CANCELLED || this == EXPIRED;
    }
}
