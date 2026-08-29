package com.rotacerta.api.repository;

import com.rotacerta.api.model.Drone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DroneRepository extends JpaRepository<Drone, Long> {
    List<Drone> findByAvailableTrue();
}
