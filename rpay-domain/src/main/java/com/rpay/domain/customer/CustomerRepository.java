package com.rpay.domain.customer;

import java.util.Optional;

/**
 * Repository interface for Customer aggregate.
 */
public interface CustomerRepository {
    /**
     * Save a customer entity
     */
    Customer save(Customer customer);

    /**
     * Find customer by ID
     */
    Optional<Customer> findById(CustomerId customerId);

    /**
     * Find customer by email
     */
    Optional<Customer> findByEmail(String email);

    /**
     * Find customer by phone
     */
    Optional<Customer> findByPhone(String phone);

    /**
     * Check if customer exists
     */
    boolean existsById(CustomerId customerId);
}
