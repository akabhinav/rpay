package com.rpay.api.controller;

import com.rpay.application.dto.CreateOrderRequest;
import com.rpay.application.service.OrderService;
import com.rpay.domain.order.Order;
import com.rpay.domain.order.OrderId;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for order management.
 *
 * Endpoints:
 * - POST /api/v1/orders - Create new order
 * - GET /api/v1/orders/{id} - Get order details
 */
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Create a new order
     */
    @PostMapping
    public ResponseEntity<Order> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        var order = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    /**
     * Get order by ID
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrder(@PathVariable String orderId) {
        var order = orderService.getOrder(OrderId.of(orderId));
        return ResponseEntity.ok(order);
    }
}
