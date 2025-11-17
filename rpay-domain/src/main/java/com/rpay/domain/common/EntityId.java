package com.rpay.domain.common;

import java.io.Serializable;
import java.util.UUID;

/**
 * Base class for all entity identifiers.
 * Provides type-safe IDs across the domain.
 */
public abstract class EntityId implements Serializable {
    private final String value;

    protected EntityId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Entity ID cannot be null or blank");
        }
        this.value = value;
    }

    protected EntityId() {
        this.value = generateId();
    }

    public String getValue() {
        return value;
    }

    protected String generateId() {
        return UUID.randomUUID().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntityId entityId = (EntityId) o;
        return value.equals(entityId.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}
