package com.rpay.infrastructure.persistence.repository;

import com.rpay.domain.merchant.Merchant;
import com.rpay.domain.merchant.MerchantId;
import com.rpay.domain.merchant.MerchantRepository;
import com.rpay.infrastructure.persistence.entity.MerchantEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class MerchantRepositoryImpl implements MerchantRepository {
    private final JpaMerchantRepository jpaRepository;

    public MerchantRepositoryImpl(JpaMerchantRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Merchant save(Merchant merchant) {
        var entity = toEntity(merchant);
        jpaRepository.save(entity);
        return merchant;
    }

    @Override
    public Optional<Merchant> findById(MerchantId merchantId) {
        return jpaRepository.findById(merchantId.getValue())
            .map(this::toDomain);
    }

    @Override
    public Optional<Merchant> findByEmail(String email) {
        return jpaRepository.findByEmail(email)
            .map(this::toDomain);
    }

    @Override
    public Optional<Merchant> findByApiKey(String apiKey) {
        return jpaRepository.findByApiKey(apiKey)
            .map(this::toDomain);
    }

    @Override
    public boolean existsById(MerchantId merchantId) {
        return jpaRepository.existsById(merchantId.getValue());
    }

    private MerchantEntity toEntity(Merchant merchant) {
        var entity = new MerchantEntity();
        entity.setId(merchant.getId().getValue());
        entity.setBusinessName(merchant.getBusinessName());
        entity.setEmail(merchant.getEmail());
        entity.setPhone(merchant.getPhone());
        entity.setStatus(merchant.getStatus());
        // Store first API key if available
        if (!merchant.getApiKeys().isEmpty()) {
            entity.setApiKey(merchant.getApiKeys().iterator().next());
        }
        entity.setWebhookUrl(merchant.getWebhookUrl());
        entity.setWebhookSecret(merchant.getWebhookSecret());
        return entity;
    }

    private Merchant toDomain(MerchantEntity entity) {
        var merchant = Merchant.builder()
            .id(MerchantId.of(entity.getId()))
            .businessName(entity.getBusinessName())
            .email(entity.getEmail())
            .phone(entity.getPhone())
            .webhookUrl(entity.getWebhookUrl())
            .webhookSecret(entity.getWebhookSecret())
            .build();

        // Add API key
        if (entity.getApiKey() != null) {
            merchant.addApiKey(entity.getApiKey());
        }

        return merchant;
    }
}
