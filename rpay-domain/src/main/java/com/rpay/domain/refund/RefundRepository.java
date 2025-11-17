package com.rpay.domain.refund;

import com.rpay.domain.payment.PaymentId;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Refund aggregate.
 */
public interface RefundRepository {
    /**
     * Save a refund entity
     */
    Refund save(Refund refund);

    /**
     * Find refund by ID
     */
    Optional<Refund> findById(RefundId refundId);

    /**
     * Find all refunds for a payment
     */
    List<Refund> findByPaymentId(PaymentId paymentId);

    /**
     * Check if refund exists
     */
    boolean existsById(RefundId refundId);
}
