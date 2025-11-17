package com.rpay.domain.merchant;

import com.rpay.domain.common.EntityId;

/**
 * Unique identifier for Merchant entity.
 * Format: merch_<uuid>
 */
public final class MerchantId extends EntityId {
    private static final String PREFIX = "merch_";

    public MerchantId(String value) {
        super(value);
    }

    public MerchantId() {
        super();
    }

    @Override
    protected String generateId() {
        return PREFIX + super.generateId();
    }

    public static MerchantId of(String value) {
        return new MerchantId(value);
    }

    public static MerchantId generate() {
        return new MerchantId();
    }
}
