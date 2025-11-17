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
 * Digital Wallet Payment Gateway Implementation.
 *
 * Supports popular Indian wallets like Paytm, PhonePe, Amazon Pay, etc.
 */
@Component
public class WalletGateway implements PaymentGateway {
    private static final Set<String> SUPPORTED_WALLETS = Set.of(
        "paytm", "phonepe", "amazonpay", "mobikwik", "freecharge", "airtel"
    );

    @Override
    public PaymentMethod getSupportedMethod() {
        return PaymentMethod.WALLET;
    }

    @Override
    @CircuitBreaker(name = "walletGateway")
    @Retry(name = "walletGateway")
    public PaymentGatewayResponse initiatePayment(Payment payment, PaymentDetails paymentDetails) {
        if (!(paymentDetails instanceof WalletPaymentDetails walletDetails)) {
            throw new IllegalArgumentException("Invalid payment details for wallet");
        }

        if (!SUPPORTED_WALLETS.contains(walletDetails.walletProvider().toLowerCase())) {
            return PaymentGatewayResponse.failure(
                "UNSUPPORTED_WALLET",
                "Wallet not supported: " + walletDetails.walletProvider()
            );
        }

        String gatewayTxnId = "wallet_" + UUID.randomUUID().toString();
        String redirectUrl = generateWalletRedirectUrl(payment, walletDetails);

        return PaymentGatewayResponse.pending(gatewayTxnId, redirectUrl);
    }

    @Override
    public PaymentGatewayResponse capturePayment(Payment payment) {
        // Wallet payments are auto-captured
        return PaymentGatewayResponse.success(
            payment.getGatewayTransactionId(),
            "Payment auto-captured"
        );
    }

    @Override
    @CircuitBreaker(name = "walletGateway")
    public RefundGatewayResponse processRefund(Payment payment, Money refundAmount) {
        String refundId = "rfnd_wallet_" + UUID.randomUUID().toString();
        return RefundGatewayResponse.success(
            refundId,
            "Refund will be credited to wallet instantly"
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

    private String generateWalletRedirectUrl(Payment payment, WalletPaymentDetails details) {
        return String.format(
            "https://%s.com/pay?merchant=rpay&txn=%s&amount=%s&phone=%s",
            details.walletProvider().toLowerCase(),
            payment.getId().getValue(),
            payment.getAmount().amount(),
            details.phone()
        );
    }
}
