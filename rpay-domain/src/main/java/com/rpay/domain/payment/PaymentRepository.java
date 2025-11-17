package com.rpay.domain.payment;

import com.rpay.domain.merchant.MerchantId;
import com.rpay.domain.order.OrderId;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Payment aggregate.
 * This is a port in hexagonal architecture - implementation is in infrastructure layer.
 */
public interface PaymentRepository {
    /**
     * Save a payment entity
     */
    Payment save(Payment payment);

    /**
     * Find payment by ID
     */
    Optional<Payment> findById(PaymentId paymentId);

    /**
     * Find all payments for an order
     */
    List<Payment> findByOrderId(OrderId orderId);

    /**
     * Find all payments for a merchant
     */
    List<Payment> findByMerchantId(MerchantId merchantId);

    /**
     * Find payment by gateway transaction ID
     */
    Optional<Payment> findByGatewayTransactionId(String gatewayTransactionId);

    /**
     * Check if payment exists
     */
    boolean existsById(PaymentId paymentId);
}
