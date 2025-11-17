package com.rpay.domain.payment.gateway;

import com.rpay.domain.payment.PaymentMethod;

/**
 * Wallet payment details
 */
public record WalletPaymentDetails(
    String walletProvider,  // paytm, phonepe, amazonpay, etc.
    String phone
) implements PaymentDetails {

    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.WALLET;
    }
}
