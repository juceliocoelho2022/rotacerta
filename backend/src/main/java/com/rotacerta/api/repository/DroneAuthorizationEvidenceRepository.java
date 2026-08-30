package com.rotacerta.api.repository;

import com.rotacerta.api.model.DroneAuthorizationEvidence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DroneAuthorizationEvidenceRepository extends JpaRepository<DroneAuthorizationEvidence, Long> {
    List<DroneAuthorizationEvidence> findByAuthorizationIdOrderByCreatedAtAsc(Long authorizationId);
}
