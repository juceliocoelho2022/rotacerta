package com.rotacerta.api.repository;

import com.rotacerta.api.model.DeliveryTrackingSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeliveryTrackingSessionRepository extends JpaRepository<DeliveryTrackingSession, Long> {
    Optional<DeliveryTrackingSession> findByOrderId(Long orderId);
    Optional<DeliveryTrackingSession> findByPublicToken(String publicToken);
}
