package com.rpay.application.service;

import com.rpay.application.dto.CreateOrderRequest;
import com.rpay.domain.common.Money;
import com.rpay.domain.customer.Customer;
import com.rpay.domain.customer.CustomerId;
import com.rpay.domain.customer.CustomerRepository;
import com.rpay.domain.merchant.MerchantId;
import com.rpay.domain.merchant.MerchantRepository;
import com.rpay.domain.order.Order;
import com.rpay.domain.order.OrderId;
import com.rpay.domain.order.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Currency;

/**
 * Application service for order management.
 * Orchestrates order-related use cases.
 *
 * Design: Single Responsibility - handles only order-related operations
 */
@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final MerchantRepository merchantRepository;
    private final CustomerRepository customerRepository;

    public OrderService(
        OrderRepository orderRepository,
        MerchantRepository merchantRepository,
        CustomerRepository customerRepository
    ) {
        this.orderRepository = orderRepository;
        this.merchantRepository = merchantRepository;
        this.customerRepository = customerRepository;
    }

    /**
     * Create a new order
     */
    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        // Validate merchant exists and is active
        var merchantId = MerchantId.of(request.merchantId());
        var merchant = merchantRepository.findById(merchantId)
            .orElseThrow(() -> new IllegalArgumentException("Merchant not found"));

        // Get or create customer
        CustomerId customerId = null;
        if (request.customerId() != null) {
            customerId = CustomerId.of(request.customerId());
        } else if (request.customerEmail() != null || request.customerPhone() != null) {
            var customer = getOrCreateCustomer(
                request.customerEmail(),
                request.customerPhone()
            );
            customerId = customer.getId();
        }

        // Create order
        var amount = Money.of(request.amount(), request.currency());
        var order = Order.builder()
            .merchantId(merchantId)
            .customerId(customerId)
            .amount(amount)
            .receipt(request.receipt())
            .notes(request.notes())
            .metadata(request.metadata())
            .build();

        return orderRepository.save(order);
    }

    /**
     * Get order by ID
     */
    @Transactional(readOnly = true)
    public Order getOrder(OrderId orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Order not found"));
    }

    /**
     * Mark order as attempted
     */
    @Transactional
    public void markOrderAsAttempted(OrderId orderId) {
        var order = getOrder(orderId);
        order.markAsAttempted();
        orderRepository.save(order);
    }

    /**
     * Mark order as paid
     */
    @Transactional
    public void markOrderAsPaid(OrderId orderId) {
        var order = getOrder(orderId);
        order.markAsPaid();
        orderRepository.save(order);
    }

    /**
     * Process expired orders (scheduled job)
     */
    @Transactional
    public void processExpiredOrders() {
        var expiredOrders = orderRepository.findExpiredOrders();
        expiredOrders.forEach(order -> {
            order.expire();
            orderRepository.save(order);
        });
    }

    private Customer getOrCreateCustomer(String email, String phone) {
        // Try to find existing customer
        if (email != null) {
            var existing = customerRepository.findByEmail(email);
            if (existing.isPresent()) {
                return existing.get();
            }
        }

        if (phone != null) {
            var existing = customerRepository.findByPhone(phone);
            if (existing.isPresent()) {
                return existing.get();
            }
        }

        // Create new customer
        var customer = Customer.builder()
            .email(email)
            .phone(phone)
            .build();

        return customerRepository.save(customer);
    }
}
