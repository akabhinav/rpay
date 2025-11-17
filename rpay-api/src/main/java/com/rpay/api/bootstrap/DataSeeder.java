package com.rpay.api.bootstrap;

import com.rpay.domain.merchant.Merchant;
import com.rpay.domain.merchant.MerchantRepository;
import com.rpay.domain.merchant.MerchantStatus;
import com.rpay.infrastructure.security.ApiKeyAuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Seeds initial test data for local development.
 * Creates a test merchant with API key for testing.
 */
@Component
public class DataSeeder implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final MerchantRepository merchantRepository;
    private final ApiKeyAuthenticationService authService;

    public DataSeeder(
        MerchantRepository merchantRepository,
        ApiKeyAuthenticationService authService
    ) {
        this.merchantRepository = merchantRepository;
        this.authService = authService;
    }

    @Override
    public void run(String... args) {
        seedTestMerchant();
    }

    private void seedTestMerchant() {
        String testEmail = "test@merchant.com";

        // Check if test merchant already exists
        var existing = merchantRepository.findByEmail(testEmail);
        if (existing.isPresent()) {
            log.info("Test merchant already exists");
            printMerchantInfo(existing.get());
            return;
        }

        // Create test merchant
        var merchant = Merchant.builder()
            .businessName("Test Merchant")
            .email(testEmail)
            .phone("+919876543210")
            .webhookUrl("http://localhost:3000/webhook")
            .webhookSecret("test_webhook_secret_123")
            .build();

        // Generate and add API key
        String apiKey = authService.generateApiKey();
        String hashedKey = hashApiKey(apiKey);
        merchant.addApiKey(hashedKey);

        // Activate merchant
        merchant.activate();

        // Save to database
        merchantRepository.save(merchant);

        log.info("=".repeat(80));
        log.info("TEST MERCHANT CREATED SUCCESSFULLY!");
        log.info("=".repeat(80));
        log.info("Merchant ID:    {}", merchant.getId().getValue());
        log.info("Business Name:  {}", merchant.getBusinessName());
        log.info("Email:          {}", merchant.getEmail());
        log.info("Status:         {}", merchant.getStatus());
        log.info("");
        log.info("API KEY (save this for testing):");
        log.info("  {}", apiKey);
        log.info("");
        log.info("Use this API key in the X-Api-Key header for all requests");
        log.info("=".repeat(80));
    }

    private void printMerchantInfo(Merchant merchant) {
        log.info("=".repeat(80));
        log.info("EXISTING TEST MERCHANT");
        log.info("=".repeat(80));
        log.info("Merchant ID:    {}", merchant.getId().getValue());
        log.info("Business Name:  {}", merchant.getBusinessName());
        log.info("Email:          {}", merchant.getEmail());
        log.info("Status:         {}", merchant.getStatus());
        log.info("");
        log.info("Note: API key was already generated. Check previous logs or regenerate.");
        log.info("=".repeat(80));
    }

    private String hashApiKey(String apiKey) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(apiKey.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return java.util.Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash API key", e);
        }
    }
}
