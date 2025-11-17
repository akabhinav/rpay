package com.rpay.infrastructure.gateway;

import com.rpay.domain.common.Money;
import com.rpay.domain.payment.Payment;
import com.rpay.domain.payment.PaymentMethod;
import com.rpay.domain.payment.gateway.*;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

/**
 * Net Banking Payment Gateway Implementation.
 *
 * Supports major Indian banks through redirect-based flow.
 */
@Component
public class NetbankingGateway implements PaymentGateway {
    private static final Set<String> SUPPORTED_BANKS = Set.of(
        "HDFC", "ICICI", "SBI", "AXIS", "KOTAK", "YES", "PNB", "BOB", "CANARA"
    );

    @Override
    public PaymentMethod getSupportedMethod() {
        return PaymentMethod.NETBANKING;
    }

    @Override
    @CircuitBreaker(name = "netbankingGateway")
    @Retry(name = "netbankingGateway")
    public PaymentGatewayResponse initiatePayment(Payment payment, PaymentDetails paymentDetails) {
        if (!(paymentDetails instanceof NetbankingPaymentDetails nbDetails)) {
            throw new IllegalArgumentException("Invalid payment details for netbanking");
        }

        if (!SUPPORTED_BANKS.contains(nbDetails.bankCode())) {
            return PaymentGatewayResponse.failure(
                "UNSUPPORTED_BANK",
                "Bank not supported: " + nbDetails.bankCode()
            );
        }

        String gatewayTxnId = "nb_" + UUID.randomUUID().toString();
        String redirectUrl = generateBankRedirectUrl(payment, nbDetails);

        return PaymentGatewayResponse.pending(gatewayTxnId, redirectUrl);
    }

    @Override
    public PaymentGatewayResponse capturePayment(Payment payment) {
        // Net banking payments are auto-captured
        return PaymentGatewayResponse.success(
            payment.getGatewayTransactionId(),
            "Payment auto-captured"
        );
    }

    @Override
    @CircuitBreaker(name = "netbankingGateway")
    public RefundGatewayResponse processRefund(Payment payment, Money refundAmount) {
        String refundId = "rfnd_nb_" + UUID.randomUUID().toString();
        return RefundGatewayResponse.success(
            refundId,
            "Refund will be processed in 5-7 business days"
        );
    }

    @Override
    public PaymentGatewayResponse verifyPayment(String gatewayTransactionId) {
        return PaymentGatewayResponse.success(
            gatewayTransactionId,
            "Payment verified"
        );
    }

    @Override
    public PaymentGatewayResponse cancelPayment(Payment payment) {
        return PaymentGatewayResponse.success(
            payment.getGatewayTransactionId(),
            "Payment cancelled"
        );
    }

    private String generateBankRedirectUrl(Payment payment, NetbankingPaymentDetails details) {
        return String.format(
            "https://netbanking.%s.com/pay?merchant=rpay&txn=%s&amount=%s",
            details.bankCode().toLowerCase(),
            payment.getId().getValue(),
            payment.getAmount().amount()
        );
    }
}
