package com.rpay.domain.order;

import com.rpay.domain.common.EntityId;

/**
 * Unique identifier for Order entity.
 * Format: order_<uuid>
 */
public final class OrderId extends EntityId {
    private static final String PREFIX = "order_";

    public OrderId(String value) {
        super(value);
    }

    public OrderId() {
        super();
    }

    @Override
    protected String generateId() {
        return PREFIX + super.generateId();
    }

    public static OrderId of(String value) {
        return new OrderId(value);
    }

    public static OrderId generate() {
        return new OrderId();
    }
}
