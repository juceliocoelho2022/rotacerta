package com.rotacerta.api.repository;

import com.rotacerta.api.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    boolean existsByPlateIgnoreCase(String plate);
    boolean existsByDriverId(Long driverId);
    long countByStatus(String status);
    List<Vehicle> findAllByOrderByUpdatedAtDesc();
}
