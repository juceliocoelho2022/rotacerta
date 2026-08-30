package com.rotacerta.api.service;

import com.rotacerta.api.dto.DroneMissionResponse;
import com.rotacerta.api.model.DroneMissionStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DroneMissionOrchestrationService {

    private final DroneDeliveryService droneDeliveryService;
    private final OrderService orderService;

    public DroneMissionOrchestrationService(
            DroneDeliveryService droneDeliveryService,
            OrderService orderService
    ) {
        this.droneDeliveryService = droneDeliveryService;
        this.orderService = orderService;
    }

    @Transactional
    public DroneMissionResponse updateMissionStatus(Long missionId, DroneMissionStatus status) {
        DroneMissionResponse mission = droneDeliveryService.updateMissionStatus(missionId, status);

        if (status == DroneMissionStatus.DELIVERED) {
            orderService.confirmDelivery(mission.orderId());
        }

        return mission;
    }
}
