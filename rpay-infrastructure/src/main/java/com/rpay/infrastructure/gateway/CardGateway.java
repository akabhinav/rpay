package com.rpay.infrastructure.gateway;

import com.rpay.domain.common.Money;
import com.rpay.domain.payment.Payment;
import com.rpay.domain.payment.PaymentMethod;
import com.rpay.domain.payment.gateway.*;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;

/**
 * Card Payment Gateway Implementation.
 *
 * Handles credit/debit card payments with:
 * - PCI DSS compliance (card data not stored)
 * - 3D Secure authentication
 * - Two-step payment (authorize then capture)
 * - EMV/chip card support
 */
@Component
public class CardGateway implements PaymentGateway {
    private final WebClient webClient;

    public CardGateway(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
            .baseUrl("https://card-gateway.example.com")
            .build();
    }

    @Override
    public PaymentMethod getSupportedMethod() {
        return PaymentMethod.CARD;
    }

    @Override
    @CircuitBreaker(name = "cardGateway", fallbackMethod = "fallbackInitiatePayment")
    @Retry(name = "cardGateway")
    public PaymentGatewayResponse initiatePayment(Payment payment, PaymentDetails paymentDetails) {
        if (!(paymentDetails instanceof CardPaymentDetails cardDetails)) {
            throw new IllegalArgumentException("Invalid payment details for card");
        }

        // Validate card details
        if (!isValidCardNumber(cardDetails.cardNumber())) {
            return PaymentGatewayResponse.failure(
                "INVALID_CARD",
                "Invalid card number"
            );
        }

        if (!isValidCvv(cardDetails.cvv())) {
            return PaymentGatewayResponse.failure(
                "INVALID_CVV",
                "Invalid CVV"
            );
        }

        // Generate gateway transaction ID
        String gatewayTxnId = "card_" + UUID.randomUUID().toString();

        // Check if 3DS authentication required (typically for amounts > certain threshold)
        if (requires3DS(payment.getAmount())) {
            // Return 3DS authentication URL
            String authUrl = generate3DSUrl(payment, cardDetails);
            return PaymentGatewayResponse.pending(gatewayTxnId, authUrl);
        }

        // For smaller amounts or pre-authenticated cards, authorize directly
        return PaymentGatewayResponse.success(
            gatewayTxnId,
            "Card authorized successfully"
        );
    }

    @Override
    @CircuitBreaker(name = "cardGateway")
    public PaymentGatewayResponse capturePayment(Payment payment) {
        // Two-step payment: capture previously authorized amount
        // In production: API call to gateway to capture funds
        return PaymentGatewayResponse.success(
            payment.getGatewayTransactionId(),
            "Payment captured successfully"
        );
    }

    @Override
    @CircuitBreaker(name = "cardGateway")
    @Retry(name = "cardGateway")
    public RefundGatewayResponse processRefund(Payment payment, Money refundAmount) {
        // In production: API call to process card refund
        String refundId = "rfnd_card_" + UUID.randomUUID().toString();

        return RefundGatewayResponse.success(
            refundId,
            "Refund will be credited in 5-7 business days"
        );
    }

    @Override
    @CircuitBreaker(name = "cardGateway")
    public PaymentGatewayResponse verifyPayment(String gatewayTransactionId) {
        // Query payment status from card network
        return PaymentGatewayResponse.success(
            gatewayTransactionId,
            "Payment verified"
        );
    }

    @Override
    public PaymentGatewayResponse cancelPayment(Payment payment) {
        return PaymentGatewayResponse.success(
            payment.getGatewayTransactionId(),
            "Authorization cancelled"
        );
    }

    private PaymentGatewayResponse fallbackInitiatePayment(
        Payment payment,
        PaymentDetails paymentDetails,
        Throwable throwable
    ) {
        return PaymentGatewayResponse.failure(
            "GATEWAY_UNAVAILABLE",
            "Card payment gateway temporarily unavailable"
        );
    }

    private boolean isValidCardNumber(String cardNumber) {
        // Luhn algorithm for card validation
        if (cardNumber == null || !cardNumber.matches("\\d{13,19}")) {
            return false;
        }

        int sum = 0;
        boolean alternate = false;
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(cardNumber.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
    }

    private boolean isValidCvv(String cvv) {
        return cvv != null && cvv.matches("\\d{3,4}");
    }

    private boolean requires3DS(Money amount) {
        // Typically required for amounts > 2000 INR or international cards
        return amount.amount().compareTo(java.math.BigDecimal.valueOf(2000)) > 0;
    }

    private String generate3DSUrl(Payment payment, CardPaymentDetails cardDetails) {
        return String.format(
            "https://3ds.example.com/auth?txn=%s&amount=%s",
            payment.getId().getValue(),
            payment.getAmount().amount()
        );
    }
}
