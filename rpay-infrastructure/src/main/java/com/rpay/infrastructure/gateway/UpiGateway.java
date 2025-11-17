package com.rpay.infrastructure.gateway;

import com.rpay.domain.common.Money;
import com.rpay.domain.payment.Payment;
import com.rpay.domain.payment.PaymentMethod;
import com.rpay.domain.payment.gateway.*;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.UUID;

/**
 * UPI Payment Gateway Implementation.
 *
 * Performance optimizations:
 * - Circuit breaker pattern for fault tolerance
 * - Retry mechanism for transient failures
 * - Non-blocking I/O using WebClient
 * - Async processing capability
 *
 * In production, this would integrate with actual UPI gateways like:
 * - NPCI UPI APIs
 * - Bank UPI gateways
 * - Third-party aggregators
 */
@Component
public class UpiGateway implements PaymentGateway {
    private final WebClient webClient;

    public UpiGateway(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
            .baseUrl("https://upi-gateway.example.com") // Mock URL
            .build();
    }

    @Override
    public PaymentMethod getSupportedMethod() {
        return PaymentMethod.UPI;
    }

    @Override
    @CircuitBreaker(name = "upiGateway", fallbackMethod = "fallbackInitiatePayment")
    @Retry(name = "upiGateway")
    public PaymentGatewayResponse initiatePayment(Payment payment, PaymentDetails paymentDetails) {
        if (!(paymentDetails instanceof UpiPaymentDetails upiDetails)) {
            throw new IllegalArgumentException("Invalid payment details for UPI");
        }

        // Validate VPA format
        if (!isValidVpa(upiDetails.vpa())) {
            return PaymentGatewayResponse.failure(
                "INVALID_VPA",
                "Invalid UPI VPA format"
            );
        }

        // In production, this would make actual API call to UPI gateway
        // For now, simulating the behavior
        String gatewayTxnId = "upi_" + UUID.randomUUID().toString();

        return switch (upiDetails.flow()) {
            case "collect" -> {
                // Collect flow: Send collect request to customer's VPA
                // Customer approves on their UPI app
                yield PaymentGatewayResponse.pending(
                    gatewayTxnId,
                    "upi://pay?pa=" + upiDetails.vpa()
                );
            }
            case "intent" -> {
                // Intent flow: Generate UPI intent URL
                String intentUrl = generateUpiIntentUrl(payment, upiDetails);
                yield PaymentGatewayResponse.pending(gatewayTxnId, intentUrl);
            }
            case "qr" -> {
                // QR flow: Generate QR code data
                String qrData = generateQrData(payment, upiDetails);
                yield PaymentGatewayResponse.pending(gatewayTxnId, qrData);
            }
            default -> PaymentGatewayResponse.failure(
                "INVALID_FLOW",
                "Unsupported UPI flow: " + upiDetails.flow()
            );
        };
    }

    @Override
    @CircuitBreaker(name = "upiGateway")
    public PaymentGatewayResponse capturePayment(Payment payment) {
        // UPI payments are auto-captured, no separate capture needed
        return PaymentGatewayResponse.success(
            payment.getGatewayTransactionId(),
            "Payment auto-captured"
        );
    }

    @Override
    @CircuitBreaker(name = "upiGateway")
    @Retry(name = "upiGateway")
    public RefundGatewayResponse processRefund(Payment payment, Money refundAmount) {
        // In production: API call to process UPI refund
        String refundId = "rfnd_upi_" + UUID.randomUUID().toString();

        return RefundGatewayResponse.success(
            refundId,
            "Refund initiated successfully"
        );
    }

    @Override
    @CircuitBreaker(name = "upiGateway")
    public PaymentGatewayResponse verifyPayment(String gatewayTransactionId) {
        // In production: Query payment status from gateway
        // Simulating successful payment
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

    // Circuit breaker fallback
    private PaymentGatewayResponse fallbackInitiatePayment(
        Payment payment,
        PaymentDetails paymentDetails,
        Throwable throwable
    ) {
        return PaymentGatewayResponse.failure(
            "GATEWAY_UNAVAILABLE",
            "UPI gateway temporarily unavailable. Please try again."
        );
    }

    private boolean isValidVpa(String vpa) {
        return vpa != null && vpa.matches("^[\\w.]+@[\\w]+$");
    }

    private String generateUpiIntentUrl(Payment payment, UpiPaymentDetails details) {
        return String.format(
            "upi://pay?pa=%s&pn=Merchant&am=%s&cu=%s&tn=%s",
            details.vpa(),
            payment.getAmount().amount().toString(),
            payment.getAmount().currency().getCurrencyCode(),
            payment.getId().getValue()
        );
    }

    private String generateQrData(Payment payment, UpiPaymentDetails details) {
        return generateUpiIntentUrl(payment, details);
    }
}
