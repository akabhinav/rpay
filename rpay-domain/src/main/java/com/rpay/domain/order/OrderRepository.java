package com.rpay.domain.order;

import com.rpay.domain.merchant.MerchantId;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Order aggregate.
 */
public interface OrderRepository {
    /**
     * Save an order entity
     */
    Order save(Order order);

    /**
     * Find order by ID
     */
    Optional<Order> findById(OrderId orderId);

    /**
     * Find orders by merchant ID
     */
    List<Order> findByMerchantId(MerchantId merchantId);

    /**
     * Find order by receipt number
     */
    Optional<Order> findByReceipt(String receipt);

    /**
     * Find all expired but not yet marked orders
     */
    List<Order> findExpiredOrders();

    /**
     * Check if order exists
     */
    boolean existsById(OrderId orderId);
}
