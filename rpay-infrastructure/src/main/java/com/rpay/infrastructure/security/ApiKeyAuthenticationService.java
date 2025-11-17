package com.rpay.infrastructure.security;

import com.rpay.domain.merchant.Merchant;
import com.rpay.domain.merchant.MerchantRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Optional;

/**
 * Service for API key authentication and signature verification.
 *
 * Security features:
 * - API key validation
 * - HMAC-SHA256 signature verification
 * - Request integrity validation
 * - Replay attack prevention (via idempotency)
 *
 * All API requests must include:
 * 1. X-Api-Key header
 * 2. X-Signature header (HMAC of request body)
 */
@Service
public class ApiKeyAuthenticationService {
    private final MerchantRepository merchantRepository;

    public ApiKeyAuthenticationService(MerchantRepository merchantRepository) {
        this.merchantRepository = merchantRepository;
    }

    /**
     * Authenticate merchant using API key
     */
    @Cacheable(value = "apiKeys", key = "#apiKey")
    public Optional<Merchant> authenticateApiKey(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            return Optional.empty();
        }

        // Hash API key for secure lookup
        String hashedKey = hashApiKey(apiKey);

        return merchantRepository.findByApiKey(hashedKey)
            .filter(merchant -> merchant.getStatus().name().equals("ACTIVE"));
    }

    /**
     * Verify request signature using HMAC-SHA256
     */
    public boolean verifySignature(String apiSecret, String requestBody, String signature) {
        if (apiSecret == null || requestBody == null || signature == null) {
            return false;
        }

        try {
            String expectedSignature = generateSignature(apiSecret, requestBody);
            return MessageDigest.isEqual(
                signature.getBytes(StandardCharsets.UTF_8),
                expectedSignature.getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Generate HMAC-SHA256 signature
     */
    public String generateSignature(String secret, String data) {
        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
            );
            sha256Hmac.init(secretKey);

            byte[] hash = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate signature", e);
        }
    }

    /**
     * Generate new API key for merchant
     */
    public String generateApiKey() {
        // Generate secure random API key
        byte[] randomBytes = new byte[32];
        new java.security.SecureRandom().nextBytes(randomBytes);
        return "rpay_" + Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    /**
     * Hash API key for storage (never store plain API keys)
     */
    private String hashApiKey(String apiKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(apiKey.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash API key", e);
        }
    }
}
