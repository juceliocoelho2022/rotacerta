package com.rotacerta.api.repository;

import com.rotacerta.api.model.DroneMission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DroneMissionRepository extends JpaRepository<DroneMission, Long> {
    Optional<DroneMission> findByOrderId(Long orderId);
}
