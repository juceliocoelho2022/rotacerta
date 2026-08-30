package com.rotacerta.api.repository;

import com.rotacerta.api.model.DroneAuthorizationDecision;
import com.rotacerta.api.model.DroneMissionAuthorization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DroneMissionAuthorizationRepository extends JpaRepository<DroneMissionAuthorization, Long> {
    List<DroneMissionAuthorization> findByMissionIdOrderByAuthorizedAtDesc(Long missionId);

    Optional<DroneMissionAuthorization> findFirstByMissionIdAndDecisionOrderByAuthorizedAtDesc(
            Long missionId,
            DroneAuthorizationDecision decision
    );
}
