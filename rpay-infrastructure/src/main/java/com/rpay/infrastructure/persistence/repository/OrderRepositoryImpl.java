package com.rpay.infrastructure.persistence.repository;

import com.rpay.domain.common.Money;
import com.rpay.domain.customer.CustomerId;
import com.rpay.domain.merchant.MerchantId;
import com.rpay.domain.order.Order;
import com.rpay.domain.order.OrderId;
import com.rpay.domain.order.OrderRepository;
import com.rpay.infrastructure.persistence.entity.OrderEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class OrderRepositoryImpl implements OrderRepository {
    private final JpaOrderRepository jpaRepository;

    public OrderRepositoryImpl(JpaOrderRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Order save(Order order) {
        var entity = toEntity(order);
        jpaRepository.save(entity);
        return order;
    }

    @Override
    public Optional<Order> findById(OrderId orderId) {
        return jpaRepository.findById(orderId.getValue())
            .map(this::toDomain);
    }

    @Override
    public List<Order> findByMerchantId(MerchantId merchantId) {
        return jpaRepository.findByMerchantId(merchantId.getValue()).stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public Optional<Order> findByReceipt(String receipt) {
        return jpaRepository.findByReceipt(receipt)
            .map(this::toDomain);
    }

    @Override
    public List<Order> findExpiredOrders() {
        return jpaRepository.findExpiredOrders(Instant.now()).stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(OrderId orderId) {
        return jpaRepository.existsById(orderId.getValue());
    }

    private OrderEntity toEntity(Order order) {
        var entity = new OrderEntity();
        entity.setId(order.getId().getValue());
        entity.setMerchantId(order.getMerchantId().getValue());
        entity.setCustomerId(order.getCustomerId() != null ? order.getCustomerId().getValue() : null);
        entity.setAmount(order.getAmount().amount());
        entity.setCurrency(order.getAmount().currency().getCurrencyCode());
        entity.setReceipt(order.getReceipt());
        entity.setNotes(order.getNotes());
        entity.setStatus(order.getStatus());
        entity.setExpiresAt(order.getExpiresAt());
        entity.setPaymentAttempts(order.getPaymentAttempts());
        return entity;
    }

    private Order toDomain(OrderEntity entity) {
        return Order.builder()
            .id(OrderId.of(entity.getId()))
            .merchantId(MerchantId.of(entity.getMerchantId()))
            .customerId(entity.getCustomerId() != null ? CustomerId.of(entity.getCustomerId()) : null)
            .amount(Money.of(entity.getAmount(), entity.getCurrency()))
            .receipt(entity.getReceipt())
            .notes(entity.getNotes())
            .expiresAt(entity.getExpiresAt())
            .build();
    }
}
