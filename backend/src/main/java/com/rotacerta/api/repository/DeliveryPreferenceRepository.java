package com.rotacerta.api.repository;

import com.rotacerta.api.model.DeliveryPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeliveryPreferenceRepository extends JpaRepository<DeliveryPreference, Long> {
    Optional<DeliveryPreference> findByCustomerId(Long customerId);
}
