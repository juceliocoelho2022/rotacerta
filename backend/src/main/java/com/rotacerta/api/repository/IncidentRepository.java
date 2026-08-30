package com.rotacerta.api.repository;

import com.rotacerta.api.model.Incident;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncidentRepository extends JpaRepository<Incident, Long> {
    List<Incident> findAllByOrderByOpenedAtDesc();
}
