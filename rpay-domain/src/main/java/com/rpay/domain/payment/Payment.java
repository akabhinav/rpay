package com.rpay.domain.payment;

import com.rpay.domain.common.BaseEntity;
import com.rpay.domain.common.Money;
import com.rpay.domain.customer.CustomerId;
import com.rpay.domain.merchant.MerchantId;
import com.rpay.domain.order.OrderId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Payment entity represents an actual payment transaction.
 * Core aggregate root in the payment domain.
 *
 * Business Rules:
 * - Payment must be associated with an order
 * - Status transitions must follow strict state machine
 * - Payment can be partially or fully refunded
 * - Failed payments cannot be retried (new payment required)
 */
public class Payment extends BaseEntity<PaymentId> {
    private final MerchantId merchantId;
    private final OrderId orderId;
    private final CustomerId customerId;
    private final Money amount;
    private Money refundedAmount;
    private PaymentStatus status;
    private PaymentMethod method;
    private String gatewayTransactionId;
    private String errorCode;
    private String errorDescription;
    private final List<PaymentEvent> events;
    private final Map<String, String> metadata;
    private Instant capturedAt;
    private Instant failedAt;

    private Payment(Builder builder) {
        super(builder.id != null ? builder.id : PaymentId.generate());
        this.merchantId = builder.merchantId;
        this.orderId = builder.orderId;
        this.customerId = builder.customerId;
        this.amount = builder.amount;
        this.refundedAmount = Money.zero(amount.currency());
        this.status = PaymentStatus.CREATED;
        this.method = builder.method;
        this.events = new ArrayList<>();
        this.metadata = new HashMap<>(builder.metadata);

        validate();
        addEvent("payment.created", "Payment created");
    }

    private void validate() {
        if (merchantId == null) {
            throw new IllegalArgumentException("Merchant ID cannot be null");
        }
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
        if (amount == null || !amount.isPositive()) {
            throw new IllegalArgumentException("Payment amount must be positive");
        }
    }

    public void startProcessing(String gatewayTransactionId) {
        validateTransition(PaymentStatus.PROCESSING);
        this.status = PaymentStatus.PROCESSING;
        this.gatewayTransactionId = gatewayTransactionId;
        addEvent("payment.processing", "Payment processing started");
        markAsUpdated();
    }

    public void requireAuthorization() {
        validateTransition(PaymentStatus.PENDING_AUTHORIZATION);
        this.status = PaymentStatus.PENDING_AUTHORIZATION;
        addEvent("payment.pending_authorization", "Payment requires user authorization");
        markAsUpdated();
    }

    public void authorize() {
        validateTransition(PaymentStatus.AUTHORIZED);
        this.status = PaymentStatus.AUTHORIZED;
        addEvent("payment.authorized", "Payment authorized");
        markAsUpdated();
    }

    public void capture() {
        validateTransition(PaymentStatus.CAPTURED);
        this.status = PaymentStatus.CAPTURED;
        this.capturedAt = Instant.now();
        addEvent("payment.captured", "Payment captured successfully");
        markAsUpdated();
    }

    public void fail(String errorCode, String errorDescription) {
        validateTransition(PaymentStatus.FAILED);
        this.status = PaymentStatus.FAILED;
        this.errorCode = errorCode;
        this.errorDescription = errorDescription;
        this.failedAt = Instant.now();
        addEvent("payment.failed", String.format("Payment failed: %s", errorDescription));
        markAsUpdated();
    }

    public void cancel() {
        validateTransition(PaymentStatus.CANCELLED);
        this.status = PaymentStatus.CANCELLED;
        addEvent("payment.cancelled", "Payment cancelled");
        markAsUpdated();
    }

    public void refund(Money refundAmount) {
        if (status != PaymentStatus.CAPTURED) {
            throw new IllegalStateException("Only captured payments can be refunded");
        }

        Money totalRefunded = this.refundedAmount.add(refundAmount);
        if (totalRefunded.isGreaterThan(amount)) {
            throw new IllegalArgumentException("Refund amount exceeds payment amount");
        }

        this.refundedAmount = totalRefunded;

        if (this.refundedAmount.equals(amount)) {
            validateTransition(PaymentStatus.REFUNDED);
            this.status = PaymentStatus.REFUNDED;
            addEvent("payment.refunded", "Payment fully refunded");
        } else {
            addEvent("payment.refunded_partial",
                    String.format("Partial refund: %s", refundAmount));
        }
        markAsUpdated();
    }

    public boolean isFullyRefunded() {
        return refundedAmount.equals(amount);
    }

    public boolean isPartiallyRefunded() {
        return refundedAmount.isPositive() && !isFullyRefunded();
    }

    public Money getRefundableAmount() {
        return amount.subtract(refundedAmount);
    }

    private void validateTransition(PaymentStatus newStatus) {
        if (!status.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                String.format("Cannot transition payment from %s to %s", status, newStatus));
        }
    }

    private void addEvent(String eventType, String description) {
        events.add(new PaymentEvent(eventType, description, Instant.now()));
    }

    // Getters
    public MerchantId getMerchantId() { return merchantId; }
    public OrderId getOrderId() { return orderId; }
    public CustomerId getCustomerId() { return customerId; }
    public Money getAmount() { return amount; }
    public Money getRefundedAmount() { return refundedAmount; }
    public PaymentStatus getStatus() { return status; }
    public PaymentMethod getMethod() { return method; }
    public String getGatewayTransactionId() { return gatewayTransactionId; }
    public String getErrorCode() { return errorCode; }
    public String getErrorDescription() { return errorDescription; }
    public List<PaymentEvent> getEvents() { return new ArrayList<>(events); }
    public Map<String, String> getMetadata() { return new HashMap<>(metadata); }
    public Instant getCapturedAt() { return capturedAt; }
    public Instant getFailedAt() { return failedAt; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private PaymentId id;
        private MerchantId merchantId;
        private OrderId orderId;
        private CustomerId customerId;
        private Money amount;
        private PaymentMethod method;
        private Map<String, String> metadata = new HashMap<>();

        public Builder id(PaymentId id) {
            this.id = id;
            return this;
        }

        public Builder merchantId(MerchantId merchantId) {
            this.merchantId = merchantId;
            return this;
        }

        public Builder orderId(OrderId orderId) {
            this.orderId = orderId;
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

        public Builder method(PaymentMethod method) {
            this.method = method;
            return this;
        }

        public Builder metadata(Map<String, String> metadata) {
            this.metadata = metadata;
            return this;
        }

        public Payment build() {
            return new Payment(this);
        }
    }

    /**
     * Value object representing payment lifecycle events
     */
    public record PaymentEvent(
        String eventType,
        String description,
        Instant occurredAt
    ) {}
}
