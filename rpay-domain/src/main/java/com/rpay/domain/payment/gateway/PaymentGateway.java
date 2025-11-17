package com.rpay.domain.payment.gateway;

import com.rpay.domain.common.Money;
import com.rpay.domain.payment.Payment;
import com.rpay.domain.payment.PaymentMethod;

/**
 * Port interface for payment gateway integration.
 * Different payment methods will have their own implementations (Strategy Pattern).
 *
 * This follows SOLID:
 * - Single Responsibility: Only handles payment gateway communication
 * - Open/Closed: Open for extension (new payment methods), closed for modification
 * - Liskov Substitution: All implementations must be interchangeable
 * - Interface Segregation: Focused interface for payment processing
 * - Dependency Inversion: Domain depends on abstraction, not concrete implementations
 */
public interface PaymentGateway {
    /**
     * Get the payment method this gateway handles
     */
    PaymentMethod getSupportedMethod();

    /**
     * Initiate a payment transaction
     *
     * @param payment The payment to process
     * @param paymentDetails Method-specific payment details
     * @return Result of payment initiation
     */
    PaymentGatewayResponse initiatePayment(Payment payment, PaymentDetails paymentDetails);

    /**
     * Capture an authorized payment
     */
    PaymentGatewayResponse capturePayment(Payment payment);

    /**
     * Process refund for a payment
     */
    RefundGatewayResponse processRefund(Payment payment, Money refundAmount);

    /**
     * Verify payment status with gateway
     */
    PaymentGatewayResponse verifyPayment(String gatewayTransactionId);

    /**
     * Cancel a pending payment
     */
    PaymentGatewayResponse cancelPayment(Payment payment);
}
