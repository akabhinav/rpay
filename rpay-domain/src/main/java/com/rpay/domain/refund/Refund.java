package com.rpay.domain.refund;

import com.rpay.domain.common.BaseEntity;
import com.rpay.domain.common.Money;
import com.rpay.domain.merchant.MerchantId;
import com.rpay.domain.payment.PaymentId;

/**
 * Refund entity represents a refund transaction.
 * Refunds are always associated with a payment.
 */
public class Refund extends BaseEntity<RefundId> {
    private final PaymentId paymentId;
    private final MerchantId merchantId;
    private final Money amount;
    private RefundStatus status;
    private String gatewayRefundId;
    private String reason;
    private String notes;
    private String errorCode;
    private String errorDescription;

    private Refund(Builder builder) {
        super(builder.id != null ? builder.id : RefundId.generate());
        this.paymentId = builder.paymentId;
        this.merchantId = builder.merchantId;
        this.amount = builder.amount;
        this.status = RefundStatus.INITIATED;
        this.reason = builder.reason;
        this.notes = builder.notes;

        validate();
    }

    private void validate() {
        if (paymentId == null) {
            throw new IllegalArgumentException("Payment ID cannot be null");
        }
        if (merchantId == null) {
            throw new IllegalArgumentException("Merchant ID cannot be null");
        }
        if (amount == null || !amount.isPositive()) {
            throw new IllegalArgumentException("Refund amount must be positive");
        }
    }

    public void startProcessing(String gatewayRefundId) {
        if (status != RefundStatus.INITIATED) {
            throw new IllegalStateException("Only initiated refunds can be processed");
        }
        this.status = RefundStatus.PROCESSING;
        this.gatewayRefundId = gatewayRefundId;
        markAsUpdated();
    }

    public void complete() {
        if (status != RefundStatus.PROCESSING) {
            throw new IllegalStateException("Only processing refunds can be completed");
        }
        this.status = RefundStatus.COMPLETED;
        markAsUpdated();
    }

    public void fail(String errorCode, String errorDescription) {
        if (status == RefundStatus.COMPLETED) {
            throw new IllegalStateException("Completed refunds cannot be failed");
        }
        this.status = RefundStatus.FAILED;
        this.errorCode = errorCode;
        this.errorDescription = errorDescription;
        markAsUpdated();
    }

    public void cancel() {
        if (status != RefundStatus.INITIATED) {
            throw new IllegalStateException("Only initiated refunds can be cancelled");
        }
        this.status = RefundStatus.CANCELLED;
        markAsUpdated();
    }

    // Getters
    public PaymentId getPaymentId() { return paymentId; }
    public MerchantId getMerchantId() { return merchantId; }
    public Money getAmount() { return amount; }
    public RefundStatus getStatus() { return status; }
    public String getGatewayRefundId() { return gatewayRefundId; }
    public String getReason() { return reason; }
    public String getNotes() { return notes; }
    public String getErrorCode() { return errorCode; }
    public String getErrorDescription() { return errorDescription; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private RefundId id;
        private PaymentId paymentId;
        private MerchantId merchantId;
        private Money amount;
        private String reason;
        private String notes;

        public Builder id(RefundId id) {
            this.id = id;
            return this;
        }

        public Builder paymentId(PaymentId paymentId) {
            this.paymentId = paymentId;
            return this;
        }

        public Builder merchantId(MerchantId merchantId) {
            this.merchantId = merchantId;
            return this;
        }

        public Builder amount(Money amount) {
            this.amount = amount;
            return this;
        }

        public Builder reason(String reason) {
            this.reason = reason;
            return this;
        }

        public Builder notes(String notes) {
            this.notes = notes;
            return this;
        }

        public Refund build() {
            return new Refund(this);
        }
    }
}
