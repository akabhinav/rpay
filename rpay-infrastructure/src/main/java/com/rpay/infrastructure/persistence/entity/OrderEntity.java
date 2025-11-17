package com.rpay.infrastructure.persistence.entity;

import com.rpay.domain.order.OrderStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * JPA entity for Order
 */
@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_order_merchant", columnList = "merchant_id"),
    @Index(name = "idx_order_status", columnList = "status"),
    @Index(name = "idx_order_receipt", columnList = "receipt"),
    @Index(name = "idx_order_expires", columnList = "expires_at"),
    @Index(name = "idx_order_created", columnList = "created_at")
})
@Getter
@Setter
public class OrderEntity {
    @Id
    @Column(length = 50)
    private String id;

    @Column(name = "merchant_id", nullable = false, length = 50)
    private String merchantId;

    @Column(name = "customer_id", length = 50)
    private String customerId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(length = 100)
    private String receipt;

    @Column(length = 1000)
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "payment_attempts", nullable = false)
    private Integer paymentAttempts = 0;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
