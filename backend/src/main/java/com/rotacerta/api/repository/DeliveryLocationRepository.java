package com.rotacerta.api.repository;

import com.rotacerta.api.model.DeliveryLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeliveryLocationRepository extends JpaRepository<DeliveryLocation, Long> {
    Optional<DeliveryLocation> findByOrderId(Long orderId);
}
