package com.rpay.infrastructure.persistence.repository;

import com.rpay.infrastructure.persistence.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface JpaOrderRepository extends JpaRepository<OrderEntity, String> {

    @Query("SELECT o FROM OrderEntity o WHERE o.merchantId = :merchantId ORDER BY o.createdAt DESC")
    List<OrderEntity> findByMerchantId(@Param("merchantId") String merchantId);

    Optional<OrderEntity> findByReceipt(String receipt);

    @Query("SELECT o FROM OrderEntity o WHERE o.expiresAt < :now AND o.status NOT IN ('PAID', 'CANCELLED', 'EXPIRED')")
    List<OrderEntity> findExpiredOrders(@Param("now") Instant now);
}
