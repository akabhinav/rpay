package com.rpay.domain.order;

import com.rpay.domain.common.BaseEntity;
import com.rpay.domain.common.Money;
import com.rpay.domain.customer.CustomerId;
import com.rpay.domain.merchant.MerchantId;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Order entity represents a purchase order created by merchant.
 * An order can have multiple payment attempts.
 *
 * Business Rules:
 * - Order amount cannot be changed once created
 * - Order can expire after a configurable timeout
 * - Order status transitions must follow defined rules
 */
public class Order extends BaseEntity<OrderId> {
    private final MerchantId merchantId;
    private final CustomerId customerId;
    private final Money amount;
    private final String receipt;
    private final String notes;
    private OrderStatus status;
    private final Instant expiresAt;
    private Integer paymentAttempts;
    private final Map<String, String> metadata;

    private Order(Builder builder) {
        super(builder.id != null ? builder.id : OrderId.generate());
        this.merchantId = builder.merchantId;
        this.customerId = builder.customerId;
        this.amount = builder.amount;
        this.receipt = builder.receipt;
        this.notes = builder.notes;
        this.status = OrderStatus.CREATED;
        this.expiresAt = builder.expiresAt != null ? builder.expiresAt :
                         Instant.now().plusSeconds(900); // Default 15 minutes
        this.paymentAttempts = 0;
        this.metadata = new HashMap<>(builder.metadata);

        validate();
    }

    private void validate() {
        if (merchantId == null) {
            throw new IllegalArgumentException("Merchant ID cannot be null");
        }
        if (amount == null || !amount.isPositive()) {
            throw new IllegalArgumentException("Order amount must be positive");
        }
        if (expiresAt.isBefore(Instant.now())) {
            throw new IllegalArgumentException("Expiry time cannot be in the past");
        }
    }

    public void markAsAttempted() {
        validateTransition(OrderStatus.ATTEMPTED);
        this.status = OrderStatus.ATTEMPTED;
        this.paymentAttempts++;
        markAsUpdated();
    }

    public void markAsPaid() {
        validateTransition(OrderStatus.PAID);
        this.status = OrderStatus.PAID;
        markAsUpdated();
    }

    public void cancel() {
        validateTransition(OrderStatus.CANCELLED);
        this.status = OrderStatus.CANCELLED;
        markAsUpdated();
    }

    public void expire() {
        if (isExpired() && !status.isTerminal()) {
            this.status = OrderStatus.EXPIRED;
            markAsUpdated();
        }
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean canAcceptPayment() {
        return (status == OrderStatus.CREATED || status == OrderStatus.ATTEMPTED)
               && !isExpired();
    }

    private void validateTransition(OrderStatus newStatus) {
        if (!status.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                String.format("Cannot transition order from %s to %s", status, newStatus));
        }
    }

    // Getters
    public MerchantId getMerchantId() { return merchantId; }
    public CustomerId getCustomerId() { return customerId; }
    public Money getAmount() { return amount; }
    public String getReceipt() { return receipt; }
    public String getNotes() { return notes; }
    public OrderStatus getStatus() { return status; }
    public Instant getExpiresAt() { return expiresAt; }
    public Integer getPaymentAttempts() { return paymentAttempts; }
    public Map<String, String> getMetadata() { return new HashMap<>(metadata); }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private OrderId id;
        private MerchantId merchantId;
        private CustomerId customerId;
        private Money amount;
        private String receipt;
        private String notes;
        private Instant expiresAt;
        private Map<String, String> metadata = new HashMap<>();

        public Builder id(OrderId id) {
            this.id = id;
            return this;
        }

        public Builder merchantId(MerchantId merchantId) {
            this.merchantId = merchantId;
            return this;
        }

        public Builder customerId(CustomerId customerId) {
            this.customerId = customerId;
            return this;
        }

        public Builder amount(Money amount) {
            this.amount = amount;
            return this;
        }

        public Builder receipt(String receipt) {
            this.receipt = receipt;
            return this;
        }

        public Builder notes(String notes) {
            this.notes = notes;
            return this;
        }

        public Builder expiresAt(Instant expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public Builder metadata(Map<String, String> metadata) {
            this.metadata = metadata;
            return this;
        }

        public Order build() {
            return new Order(this);
        }
    }
}
