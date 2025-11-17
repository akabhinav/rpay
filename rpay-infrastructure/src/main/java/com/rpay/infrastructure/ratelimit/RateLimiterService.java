package com.rpay.infrastructure.ratelimit;

import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Distributed rate limiting service using Redis.
 *
 * Prevents abuse and ensures fair usage across all API endpoints.
 * Critical for handling India-scale traffic without service degradation.
 *
 * Rate limits enforced:
 * - Per merchant API key
 * - Per IP address
 * - Per endpoint
 *
 * Uses Token Bucket algorithm for smooth rate limiting.
 */
@Service
public class RateLimiterService {
    private final RedissonClient redissonClient;

    // Rate limit configurations
    private static final long MERCHANT_RATE_LIMIT = 1000; // requests per minute
    private static final long IP_RATE_LIMIT = 100; // requests per minute
    private static final long PAYMENT_CREATE_LIMIT = 10; // requests per minute per merchant

    public RateLimiterService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * Check if merchant is within rate limit
     */
    public boolean checkMerchantRateLimit(String merchantId) {
        String key = "ratelimit:merchant:" + merchantId;
        return checkRateLimit(key, MERCHANT_RATE_LIMIT, 1, RateIntervalUnit.MINUTES);
    }

    /**
     * Check if IP is within rate limit
     */
    public boolean checkIpRateLimit(String ipAddress) {
        String key = "ratelimit:ip:" + ipAddress;
        return checkRateLimit(key, IP_RATE_LIMIT, 1, RateIntervalUnit.MINUTES);
    }

    /**
     * Check payment creation rate limit (stricter)
     */
    public boolean checkPaymentCreationLimit(String merchantId) {
        String key = "ratelimit:payment:create:" + merchantId;
        return checkRateLimit(key, PAYMENT_CREATE_LIMIT, 1, RateIntervalUnit.MINUTES);
    }

    /**
     * Generic rate limit check using token bucket algorithm
     */
    private boolean checkRateLimit(String key, long rate, long rateInterval, RateIntervalUnit unit) {
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);

        // Initialize rate limiter if not exists
        if (!rateLimiter.isExists()) {
            rateLimiter.trySetRate(RateType.OVERALL, rate, rateInterval, unit);
        }

        // Try to acquire permit
        return rateLimiter.tryAcquire(1);
    }

    /**
     * Get remaining quota for merchant
     */
    public long getRemainingQuota(String merchantId) {
        String key = "ratelimit:merchant:" + merchantId;
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);

        if (!rateLimiter.isExists()) {
            return MERCHANT_RATE_LIMIT;
        }

        return rateLimiter.availablePermits();
    }
}
