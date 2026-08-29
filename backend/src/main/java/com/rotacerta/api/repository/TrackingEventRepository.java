package com.rotacerta.api.repository;

import com.rotacerta.api.model.TrackingEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TrackingEventRepository extends JpaRepository<TrackingEvent, Long> {
    List<TrackingEvent> findByOrderIdOrderByEventTimeAsc(Long orderId);
}
