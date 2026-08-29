package com.rotacerta.api.repository;

import com.rotacerta.api.model.DeliveryStatus;
import com.rotacerta.api.model.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    Optional<OrderEntity> findByTrackingCode(String trackingCode);
    Optional<OrderEntity> findByOrderNumber(String orderNumber);
    long countByStatus(DeliveryStatus status);
}
