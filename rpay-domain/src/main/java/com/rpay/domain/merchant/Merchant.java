package com.rpay.domain.merchant;

import com.rpay.domain.common.BaseEntity;

import java.util.HashSet;
import java.util.Set;

/**
 * Merchant entity represents a business using the payment gateway.
 * Merchants have API keys, webhook configurations, and settlement preferences.
 */
public class Merchant extends BaseEntity<MerchantId> {
    private String businessName;
    private String email;
    private String phone;
    private MerchantStatus status;
    private final Set<String> apiKeys;
    private String webhookUrl;
    private String webhookSecret;
    private SettlementConfig settlementConfig;

    private Merchant(Builder builder) {
        super(builder.id != null ? builder.id : MerchantId.generate());
        this.businessName = builder.businessName;
        this.email = builder.email;
        this.phone = builder.phone;
        this.status = MerchantStatus.PENDING_VERIFICATION;
        this.apiKeys = new HashSet<>();
        this.webhookUrl = builder.webhookUrl;
        this.webhookSecret = builder.webhookSecret;
        this.settlementConfig = builder.settlementConfig != null ?
                                builder.settlementConfig : SettlementConfig.defaultConfig();

        validate();
    }

    private void validate() {
        if (businessName == null || businessName.isBlank()) {
            throw new IllegalArgumentException("Business name cannot be null or blank");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or blank");
        }
    }

    public void activate() {
        if (status != MerchantStatus.PENDING_VERIFICATION) {
            throw new IllegalStateException("Only pending merchants can be activated");
        }
        this.status = MerchantStatus.ACTIVE;
        markAsUpdated();
    }

    public void suspend(String reason) {
        this.status = MerchantStatus.SUSPENDED;
        markAsUpdated();
    }

    public void addApiKey(String apiKey) {
        this.apiKeys.add(apiKey);
        markAsUpdated();
    }

    public void revokeApiKey(String apiKey) {
        this.apiKeys.remove(apiKey);
        markAsUpdated();
    }

    public boolean isValidApiKey(String apiKey) {
        return apiKeys.contains(apiKey);
    }

    public void configureWebhook(String url, String secret) {
        this.webhookUrl = url;
        this.webhookSecret = secret;
        markAsUpdated();
    }

    // Getters
    public String getBusinessName() { return businessName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public MerchantStatus getStatus() { return status; }
    public Set<String> getApiKeys() { return new HashSet<>(apiKeys); }
    public String getWebhookUrl() { return webhookUrl; }
    public String getWebhookSecret() { return webhookSecret; }
    public SettlementConfig getSettlementConfig() { return settlementConfig; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private MerchantId id;
        private String businessName;
        private String email;
        private String phone;
        private String webhookUrl;
        private String webhookSecret;
        private SettlementConfig settlementConfig;

        public Builder id(MerchantId id) {
            this.id = id;
            return this;
        }

        public Builder businessName(String businessName) {
            this.businessName = businessName;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public Builder webhookUrl(String webhookUrl) {
            this.webhookUrl = webhookUrl;
            return this;
        }

        public Builder webhookSecret(String webhookSecret) {
            this.webhookSecret = webhookSecret;
            return this;
        }

        public Builder settlementConfig(SettlementConfig settlementConfig) {
            this.settlementConfig = settlementConfig;
            return this;
        }

        public Merchant build() {
            return new Merchant(this);
        }
    }
}
