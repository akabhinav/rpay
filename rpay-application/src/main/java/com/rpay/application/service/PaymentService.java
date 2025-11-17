package com.rpay.application.service;

import com.rpay.application.dto.CreatePaymentRequest;
import com.rpay.domain.order.Order;
import com.rpay.domain.order.OrderId;
import com.rpay.domain.payment.Payment;
import com.rpay.domain.payment.PaymentId;
import com.rpay.domain.payment.PaymentMethod;
import com.rpay.domain.payment.PaymentRepository;
import com.rpay.domain.payment.gateway.PaymentGateway;
import com.rpay.domain.payment.gateway.PaymentGatewayResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Application service for payment processing.
 * Orchestrates payment lifecycle and gateway interactions.
 *
 * Design Patterns:
 * - Strategy Pattern: Different payment gateways for different methods
 * - Template Method: Common payment flow with method-specific steps
 */
@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderService orderService;
    private final Map<PaymentMethod, PaymentGateway> gatewayRegistry;

    public PaymentService(
        PaymentRepository paymentRepository,
        OrderService orderService,
        List<PaymentGateway> paymentGateways
    ) {
        this.paymentRepository = paymentRepository;
        this.orderService = orderService;

        // Build registry of payment gateways (Strategy Pattern)
        this.gatewayRegistry = paymentGateways.stream()
            .collect(Collectors.toMap(
                PaymentGateway::getSupportedMethod,
                Function.identity()
            ));
    }

    /**
     * Create and initiate a payment
     */
    @Transactional
    public PaymentGatewayResponse createPayment(CreatePaymentRequest request) {
        // Get and validate order
        var orderId = OrderId.of(request.orderId());
        var order = orderService.getOrder(orderId);

        if (!order.canAcceptPayment()) {
            throw new IllegalStateException("Order cannot accept payment");
        }

        // Mark order as attempted
        orderService.markOrderAsAttempted(orderId);

        // Create payment entity
        var paymentMethod = PaymentMethod.fromCode(request.paymentMethod());
        var payment = Payment.builder()
            .merchantId(order.getMerchantId())
            .orderId(orderId)
            .customerId(order.getCustomerId())
            .amount(order.getAmount())
            .method(paymentMethod)
            .build();

        payment = paymentRepository.save(payment);

        // Get appropriate gateway and initiate payment
        var gateway = getGateway(paymentMethod);
        var response = gateway.initiatePayment(payment, request.paymentDetails());

        // Update payment based on gateway response
        updatePaymentFromGatewayResponse(payment, response);
        paymentRepository.save(payment);

        return response;
    }

    /**
     * Capture an authorized payment
     */
    @Transactional
    public void capturePayment(PaymentId paymentId) {
        var payment = getPayment(paymentId);
        var gateway = getGateway(payment.getMethod());

        var response = gateway.capturePayment(payment);

        if (response.success()) {
            payment.capture();
            orderService.markOrderAsPaid(payment.getOrderId());
        } else {
            payment.fail(response.errorCode(), response.message());
        }

        paymentRepository.save(payment);
    }

    /**
     * Handle payment callback from gateway
     */
    @Transactional
    public void handlePaymentCallback(String gatewayTransactionId, PaymentGatewayResponse response) {
        var payment = paymentRepository.findByGatewayTransactionId(gatewayTransactionId)
            .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        updatePaymentFromGatewayResponse(payment, response);
        paymentRepository.save(payment);

        // If payment successful, mark order as paid
        if (response.success() && response.status() == PaymentGatewayResponse.PaymentGatewayStatus.SUCCESS) {
            orderService.markOrderAsPaid(payment.getOrderId());
        }
    }

    /**
     * Get payment by ID
     */
    @Transactional(readOnly = true)
    public Payment getPayment(PaymentId paymentId) {
        return paymentRepository.findById(paymentId)
            .orElseThrow(() -> new IllegalArgumentException("Payment not found"));
    }

    /**
     * Verify payment status with gateway
     */
    @Transactional
    public Payment verifyPayment(PaymentId paymentId) {
        var payment = getPayment(paymentId);

        if (payment.getStatus().isTerminal()) {
            return payment; // Already in final state
        }

        var gateway = getGateway(payment.getMethod());
        var response = gateway.verifyPayment(payment.getGatewayTransactionId());

        updatePaymentFromGatewayResponse(payment, response);
        paymentRepository.save(payment);

        return payment;
    }

    private PaymentGateway getGateway(PaymentMethod method) {
        var gateway = gatewayRegistry.get(method);
        if (gateway == null) {
            throw new UnsupportedOperationException(
                "Payment method not supported: " + method);
        }
        return gateway;
    }

    private void updatePaymentFromGatewayResponse(Payment payment, PaymentGatewayResponse response) {
        switch (response.status()) {
            case SUCCESS -> payment.capture();
            case PENDING -> {
                if (response.redirectUrl() != null) {
                    payment.requireAuthorization();
                } else {
                    payment.startProcessing(response.gatewayTransactionId());
                }
            }
            case FAILED -> payment.fail(response.errorCode(), response.message());
            case CANCELLED -> payment.cancel();
        }
    }
}
