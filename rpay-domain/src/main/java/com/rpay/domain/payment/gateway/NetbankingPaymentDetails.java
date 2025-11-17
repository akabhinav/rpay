package com.rpay.domain.payment.gateway;

import com.rpay.domain.payment.PaymentMethod;

/**
 * Netbanking payment details
 */
public record NetbankingPaymentDetails(
    String bankCode  // Bank identifier (e.g., HDFC, ICICI, SBI)
) implements PaymentDetails {

    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.NETBANKING;
    }
}
