package com.rotacerta.api.repository;

import com.rotacerta.api.model.InventoryReservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventoryReservationRepository extends JpaRepository<InventoryReservation, Long> {
    Optional<InventoryReservation> findByOrderIdAndProductId(Long orderId, Long productId);
    List<InventoryReservation> findByOrderIdOrderByCreatedAtAsc(Long orderId);
}
