package com.rotacerta.api.repository;

import com.rotacerta.api.model.DeliveryAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeliveryAssignmentRepository extends JpaRepository<DeliveryAssignment, Long> {
    Optional<DeliveryAssignment> findByOrderId(Long orderId);
    List<DeliveryAssignment> findByDriverIdAndStatusOrderBySequencePositionAscAssignedAtAsc(Long driverId, String status);
}
