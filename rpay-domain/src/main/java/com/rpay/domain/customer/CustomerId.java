package com.rpay.domain.customer;

import com.rpay.domain.common.EntityId;

/**
 * Unique identifier for Customer entity.
 * Format: cust_<uuid>
 */
public final class CustomerId extends EntityId {
    private static final String PREFIX = "cust_";

    public CustomerId(String value) {
        super(value);
    }

    public CustomerId() {
        super();
    }

    @Override
    protected String generateId() {
        return PREFIX + super.generateId();
    }

    public static CustomerId of(String value) {
        return new CustomerId(value);
    }

    public static CustomerId generate() {
        return new CustomerId();
    }
}
