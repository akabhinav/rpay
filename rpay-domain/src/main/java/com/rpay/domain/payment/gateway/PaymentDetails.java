package com.rpay.domain.payment.gateway;

import com.rpay.domain.payment.PaymentMethod;

/**
 * Base interface for payment method-specific details.
 * Each payment method will have its own implementation.
 */
public sealed interface PaymentDetails permits
    UpiPaymentDetails,
    CardPaymentDetails,
    NetbankingPaymentDetails,
    WalletPaymentDetails {

    PaymentMethod getPaymentMethod();
}
