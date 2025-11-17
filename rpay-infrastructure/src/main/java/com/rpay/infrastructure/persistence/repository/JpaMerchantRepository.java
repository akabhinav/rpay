package com.rpay.infrastructure.persistence.repository;

import com.rpay.infrastructure.persistence.entity.MerchantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaMerchantRepository extends JpaRepository<MerchantEntity, String> {
    Optional<MerchantEntity> findByEmail(String email);
    Optional<MerchantEntity> findByApiKey(String apiKey);
}
