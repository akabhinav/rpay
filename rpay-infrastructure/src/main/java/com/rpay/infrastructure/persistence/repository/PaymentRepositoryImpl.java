package com.rpay.infrastructure.persistence.repository;

import com.rpay.domain.common.Money;
import com.rpay.domain.merchant.MerchantId;
import com.rpay.domain.order.OrderId;
import com.rpay.domain.payment.Payment;
import com.rpay.domain.payment.PaymentId;
import com.rpay.domain.payment.PaymentRepository;
import com.rpay.infrastructure.persistence.entity.PaymentEntity;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of PaymentRepository using JPA.
 * Includes caching for improved performance.
 */
@Component
public class PaymentRepositoryImpl implements PaymentRepository {
    private final JpaPaymentRepository jpaRepository;

    public PaymentRepositoryImpl(JpaPaymentRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @CacheEvict(value = "payments", key = "#payment.id.value")
    public Payment save(Payment payment) {
        var entity = toEntity(payment);
        jpaRepository.save(entity);
        return payment;
    }

    @Override
    @Cacheable(value = "payments", key = "#paymentId.value")
    public Optional<Payment> findById(PaymentId paymentId) {
        return jpaRepository.findById(paymentId.getValue())
            .map(this::toDomain);
    }

    @Override
    public List<Payment> findByOrderId(OrderId orderId) {
        return jpaRepository.findByOrderId(orderId.getValue()).stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<Payment> findByMerchantId(MerchantId merchantId) {
        return jpaRepository.findByMerchantId(merchantId.getValue()).stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public Optional<Payment> findByGatewayTransactionId(String gatewayTransactionId) {
        return jpaRepository.findByGatewayTransactionId(gatewayTransactionId)
            .map(this::toDomain);
    }

    @Override
    public boolean existsById(PaymentId paymentId) {
        return jpaRepository.existsById(paymentId.getValue());
    }

    private PaymentEntity toEntity(Payment payment) {
        var entity = new PaymentEntity();
        entity.setId(payment.getId().getValue());
        entity.setMerchantId(payment.getMerchantId().getValue());
        entity.setOrderId(payment.getOrderId().getValue());
        entity.setCustomerId(payment.getCustomerId() != null ? payment.getCustomerId().getValue() : null);
        entity.setAmount(payment.getAmount().amount());
        entity.setRefundedAmount(payment.getRefundedAmount().amount());
        entity.setCurrency(payment.getAmount().currency().getCurrencyCode());
        entity.setStatus(payment.getStatus());
        entity.setMethod(payment.getMethod());
        entity.setGatewayTransactionId(payment.getGatewayTransactionId());
        entity.setErrorCode(payment.getErrorCode());
        entity.setErrorDescription(payment.getErrorDescription());
        entity.setCapturedAt(payment.getCapturedAt());
        entity.setFailedAt(payment.getFailedAt());
        entity.setVersion(payment.getVersion());
        return entity;
    }

    private Payment toDomain(PaymentEntity entity) {
        // Reconstruct payment from entity
        // This is a simplified version - in production, you'd use MapStruct or similar
        var payment = Payment.builder()
            .id(PaymentId.of(entity.getId()))
            .merchantId(com.rpay.domain.merchant.MerchantId.of(entity.getMerchantId()))
            .orderId(OrderId.of(entity.getOrderId()))
            .customerId(entity.getCustomerId() != null ?
                       com.rpay.domain.customer.CustomerId.of(entity.getCustomerId()) : null)
            .amount(Money.of(entity.getAmount(), entity.getCurrency()))
            .method(entity.getMethod())
            .build();

        return payment;
    }
}
