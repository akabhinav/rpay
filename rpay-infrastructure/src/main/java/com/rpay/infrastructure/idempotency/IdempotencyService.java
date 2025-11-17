package com.rpay.infrastructure.idempotency;

import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Idempotency service using Redis for distributed idempotency keys.
 *
 * Ensures that duplicate requests with same idempotency key are handled correctly.
 * Critical for payment processing to prevent duplicate charges.
 *
 * Design: Uses Redis distributed cache with TTL for idempotency tracking
 */
@Service
public class IdempotencyService {
    private final RMapCache<String, IdempotencyRecord> idempotencyCache;
    private static final long IDEMPOTENCY_TTL_HOURS = 24;

    public IdempotencyService(RedissonClient redissonClient) {
        this.idempotencyCache = redissonClient.getMapCache("idempotency");
    }

    /**
     * Check if request is duplicate and get previous response if exists
     */
    public IdempotencyCheck checkIdempotency(String idempotencyKey, String merchantId) {
        String key = buildKey(idempotencyKey, merchantId);
        IdempotencyRecord record = idempotencyCache.get(key);

        if (record != null) {
            return new IdempotencyCheck(true, record.response());
        }

        return new IdempotencyCheck(false, null);
    }

    /**
     * Store response for idempotency key
     */
    public void storeResponse(String idempotencyKey, String merchantId, Object response) {
        String key = buildKey(idempotencyKey, merchantId);
        IdempotencyRecord record = new IdempotencyRecord(
            idempotencyKey,
            merchantId,
            response,
            System.currentTimeMillis()
        );

        idempotencyCache.put(key, record, IDEMPOTENCY_TTL_HOURS, TimeUnit.HOURS);
    }

    /**
     * Clear idempotency key (for testing or explicit retry)
     */
    public void clearIdempotency(String idempotencyKey, String merchantId) {
        String key = buildKey(idempotencyKey, merchantId);
        idempotencyCache.remove(key);
    }

    private String buildKey(String idempotencyKey, String merchantId) {
        return String.format("idempotency:%s:%s", merchantId, idempotencyKey);
    }

    public record IdempotencyCheck(boolean isDuplicate, Object previousResponse) {}

    public record IdempotencyRecord(
        String idempotencyKey,
        String merchantId,
        Object response,
        long timestamp
    ) {}
}
