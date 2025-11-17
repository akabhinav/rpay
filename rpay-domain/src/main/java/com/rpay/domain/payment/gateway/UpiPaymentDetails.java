package com.rpay.domain.payment.gateway;

import com.rpay.domain.payment.PaymentMethod;

/**
 * UPI-specific payment details
 */
public record UpiPaymentDetails(
    String vpa,  // Virtual Payment Address (e.g., user@upi)
    String flow  // collect, intent, qr
) implements PaymentDetails {

    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.UPI;
    }
}
