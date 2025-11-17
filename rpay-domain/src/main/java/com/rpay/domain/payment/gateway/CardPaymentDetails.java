package com.rpay.domain.payment.gateway;

import com.rpay.domain.payment.PaymentMethod;

/**
 * Card payment details
 */
public record CardPaymentDetails(
    String cardNumber,
    String cardholderName,
    String expiryMonth,
    String expiryYear,
    String cvv,
    CardType cardType
) implements PaymentDetails {

    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.CARD;
    }

    public enum CardType {
        CREDIT,
        DEBIT
    }

    // Mask sensitive data
    public String getMaskedCardNumber() {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        return "****-****-****-" + cardNumber.substring(cardNumber.length() - 4);
    }
}
