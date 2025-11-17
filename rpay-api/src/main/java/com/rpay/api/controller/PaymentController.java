package com.rpay.api.controller;

import com.rpay.application.dto.CreatePaymentRequest;
import com.rpay.application.service.PaymentService;
import com.rpay.domain.payment.Payment;
import com.rpay.domain.payment.PaymentId;
import com.rpay.domain.payment.gateway.PaymentGatewayResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for payment operations.
 *
 * Endpoints:
 * - POST /api/v1/payments - Create and initiate payment
 * - GET /api/v1/payments/{id} - Get payment details
 * - POST /api/v1/payments/{id}/capture - Capture authorized payment
 * - POST /api/v1/payments/{id}/verify - Verify payment status
 */
@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Create and initiate payment
     */
    @PostMapping
    public ResponseEntity<PaymentGatewayResponse> createPayment(
        @Valid @RequestBody CreatePaymentRequest request
    ) {
        var response = paymentService.createPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get payment details
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<Payment> getPayment(@PathVariable String paymentId) {
        var payment = paymentService.getPayment(PaymentId.of(paymentId));
        return ResponseEntity.ok(payment);
    }

    /**
     * Capture authorized payment (for two-step payments)
     */
    @PostMapping("/{paymentId}/capture")
    public ResponseEntity<Void> capturePayment(@PathVariable String paymentId) {
        paymentService.capturePayment(PaymentId.of(paymentId));
        return ResponseEntity.ok().build();
    }

    /**
     * Verify payment status with gateway
     */
    @PostMapping("/{paymentId}/verify")
    public ResponseEntity<Payment> verifyPayment(@PathVariable String paymentId) {
        var payment = paymentService.verifyPayment(PaymentId.of(paymentId));
        return ResponseEntity.ok(payment);
    }
}
