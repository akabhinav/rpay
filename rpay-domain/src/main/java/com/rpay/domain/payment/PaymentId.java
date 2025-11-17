package com.rpay.domain.payment;

import com.rpay.domain.common.EntityId;

/**
 * Unique identifier for Payment entity.
 * Format: pay_<uuid>
 */
public final class PaymentId extends EntityId {
    private static final String PREFIX = "pay_";

    public PaymentId(String value) {
        super(value);
    }

    public PaymentId() {
        super();
    }

    @Override
    protected String generateId() {
        return PREFIX + super.generateId();
    }

    public static PaymentId of(String value) {
        return new PaymentId(value);
    }

    public static PaymentId generate() {
        return new PaymentId();
    }
}
