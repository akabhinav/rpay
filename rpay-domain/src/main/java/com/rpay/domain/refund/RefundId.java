package com.rpay.domain.refund;

import com.rpay.domain.common.EntityId;

/**
 * Unique identifier for Refund entity.
 * Format: rfnd_<uuid>
 */
public final class RefundId extends EntityId {
    private static final String PREFIX = "rfnd_";

    public RefundId(String value) {
        super(value);
    }

    public RefundId() {
        super();
    }

    @Override
    protected String generateId() {
        return PREFIX + super.generateId();
    }

    public static RefundId of(String value) {
        return new RefundId(value);
    }

    public static RefundId generate() {
        return new RefundId();
    }
}
