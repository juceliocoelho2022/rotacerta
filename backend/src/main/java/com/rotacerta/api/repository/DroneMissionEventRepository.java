package com.rotacerta.api.repository;

import com.rotacerta.api.model.DroneMissionEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DroneMissionEventRepository extends JpaRepository<DroneMissionEvent, Long> {
    List<DroneMissionEvent> findByMissionIdOrderByCreatedAtAscIdAsc(Long missionId);
}
