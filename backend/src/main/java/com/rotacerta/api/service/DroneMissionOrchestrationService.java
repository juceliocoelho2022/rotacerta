package com.rotacerta.api.service;

import com.rotacerta.api.dto.DroneAuthorizationRequest;
import com.rotacerta.api.dto.DroneAuthorizationResponse;
import com.rotacerta.api.dto.DroneMissionEventResponse;
import com.rotacerta.api.dto.DroneMissionResponse;
import com.rotacerta.api.model.DroneMissionStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DroneMissionOrchestrationService {

    private final DroneDeliveryService droneDeliveryService;
    private final OrderService orderService;
    private final DroneMissionTimelineService timelineService;

    public DroneMissionOrchestrationService(
            DroneDeliveryService droneDeliveryService,
            OrderService orderService,
            DroneMissionTimelineService timelineService
    ) {
        this.droneDeliveryService = droneDeliveryService;
        this.orderService = orderService;
        this.timelineService = timelineService;
    }

    @Transactional
    public DroneMissionResponse createMission(Long orderId) {
        DroneMissionResponse mission = droneDeliveryService.createMission(orderId);
        timelineService.recordMissionCreated(mission);
        return mission;
    }

    @Transactional
    public DroneAuthorizationResponse createAuthorization(Long missionId, DroneAuthorizationRequest request) {
        DroneAuthorizationResponse authorization = droneDeliveryService.createAuthorization(missionId, request);
        timelineService.recordAuthorization(authorization);
        return authorization;
    }

    @Transactional(readOnly = true)
    public List<DroneMissionEventResponse> findTimeline(Long missionId) {
        return timelineService.findTimeline(missionId);
    }

    @Transactional
    public DroneMissionResponse updateMissionStatus(Long missionId, DroneMissionStatus status) {
        DroneMissionResponse mission = droneDeliveryService.updateMissionStatus(missionId, status);

        if (status == DroneMissionStatus.DELIVERED) {
            orderService.confirmDelivery(mission.orderId());
        }

        timelineService.recordStatusChanged(mission);
        return mission;
    }
}
