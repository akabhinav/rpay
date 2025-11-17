package com.rpay.infrastructure.persistence.repository;

import com.rpay.infrastructure.persistence.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for PaymentEntity
 */
@Repository
public interface JpaPaymentRepository extends JpaRepository<PaymentEntity, String> {

    @Query("SELECT p FROM PaymentEntity p WHERE p.orderId = :orderId ORDER BY p.createdAt DESC")
    List<PaymentEntity> findByOrderId(@Param("orderId") String orderId);

    @Query("SELECT p FROM PaymentEntity p WHERE p.merchantId = :merchantId ORDER BY p.createdAt DESC")
    List<PaymentEntity> findByMerchantId(@Param("merchantId") String merchantId);

    Optional<PaymentEntity> findByGatewayTransactionId(String gatewayTransactionId);
}
