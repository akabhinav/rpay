package com.rpay.domain.common;

import java.time.Instant;

/**
 * Base class for all domain entities.
 * Provides common fields and behavior.
 */
public abstract class BaseEntity<ID extends EntityId> {
    protected final ID id;
    protected final Instant createdAt;
    protected Instant updatedAt;
    protected long version; // For optimistic locking

    protected BaseEntity(ID id, Instant createdAt, Instant updatedAt, long version) {
        if (id == null) {
            throw new IllegalArgumentException("Entity ID cannot be null");
        }
        this.id = id;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
        this.version = version;
    }

    protected BaseEntity(ID id) {
        this(id, Instant.now(), Instant.now(), 0);
    }

    public ID getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public long getVersion() {
        return version;
    }

    protected void markAsUpdated() {
        this.updatedAt = Instant.now();
        this.version++;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseEntity<?> that = (BaseEntity<?>) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
