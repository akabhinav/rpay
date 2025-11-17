package com.rpay.domain.merchant;

import java.util.Optional;

/**
 * Repository interface for Merchant aggregate.
 */
public interface MerchantRepository {
    /**
     * Save a merchant entity
     */
    Merchant save(Merchant merchant);

    /**
     * Find merchant by ID
     */
    Optional<Merchant> findById(MerchantId merchantId);

    /**
     * Find merchant by email
     */
    Optional<Merchant> findByEmail(String email);

    /**
     * Find merchant by API key
     */
    Optional<Merchant> findByApiKey(String apiKey);

    /**
     * Check if merchant exists
     */
    boolean existsById(MerchantId merchantId);
}
