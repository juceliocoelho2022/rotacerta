package com.rotacerta.api.repository;

import com.rotacerta.api.model.OrderDeliveryDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderDeliveryDetailsRepository extends JpaRepository<OrderDeliveryDetails, Long> {
    Optional<OrderDeliveryDetails> findByOrderId(Long orderId);
}
