package com.rotacerta.api.repository;

import com.rotacerta.api.model.OrderEntity;
import com.rotacerta.api.model.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    Optional<OrderEntity> findByTrackingCode(String trackingCode);
    long countByStatus(DeliveryStatus status);
}
