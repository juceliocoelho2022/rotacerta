package com.rotacerta.api.controller;

import com.rotacerta.api.dto.DroneAuthorizationRequest;
import com.rotacerta.api.dto.DroneAuthorizationResponse;
import com.rotacerta.api.dto.DroneEligibilityResponse;
import com.rotacerta.api.dto.DroneFlightSimulationResponse;
import com.rotacerta.api.dto.DroneMissionEventResponse;
import com.rotacerta.api.dto.DroneMissionResponse;
import com.rotacerta.api.dto.DroneMissionStatusUpdateRequest;
import com.rotacerta.api.dto.DroneResponse;
import com.rotacerta.api.service.DroneDeliveryService;
import com.rotacerta.api.service.DroneFlightSimulationService;
import com.rotacerta.api.service.DroneMissionOrchestrationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drone-delivery")
public class DroneDeliveryController {

    private final DroneDeliveryService service;
    private final DroneMissionOrchestrationService orchestrationService;
    private final DroneFlightSimulationService flightSimulationService;

    public DroneDeliveryController(
            DroneDeliveryService service,
            DroneMissionOrchestrationService orchestrationService,
            DroneFlightSimulationService flightSimulationService
    ) {
        this.service = service;
        this.orchestrationService = orchestrationService;
        this.flightSimulationService = flightSimulationService;
    }

    @GetMapping("/drones")
    public List<DroneResponse> drones() {
        return service.findAllDrones();
    }

    @GetMapping("/missions")
    public List<DroneMissionResponse> missions() {
        return service.findAllMissions();
    }

    @GetMapping("/orders/{orderId}/eligibility")
    public DroneEligibilityResponse eligibility(@PathVariable Long orderId) {
        return service.eligibility(orderId);
    }

    @PostMapping("/orders/{orderId}/missions")
    @ResponseStatus(HttpStatus.CREATED)
    public DroneMissionResponse createMission(@PathVariable Long orderId) {
        return orchestrationService.createMission(orderId);
    }

    @GetMapping("/missions/{missionId}/authorizations")
    public List<DroneAuthorizationResponse> authorizations(@PathVariable Long missionId) {
        return service.findAuthorizations(missionId);
    }

    @GetMapping("/missions/{missionId}/timeline")
    public List<DroneMissionEventResponse> timeline(@PathVariable Long missionId) {
        return orchestrationService.findTimeline(missionId);
    }

    @GetMapping("/missions/{missionId}/flight-simulation")
    public DroneFlightSimulationResponse flightSimulation(@PathVariable Long missionId) {
        return flightSimulationService.getSimulation(missionId);
    }

    @PostMapping("/missions/{missionId}/authorizations")
    @ResponseStatus(HttpStatus.CREATED)
    public DroneAuthorizationResponse authorizeMission(
            @PathVariable Long missionId,
            @Valid @RequestBody DroneAuthorizationRequest request
    ) {
        return orchestrationService.createAuthorization(missionId, request);
    }

    @PatchMapping("/missions/{missionId}/status")
    public DroneMissionResponse updateMissionStatus(
            @PathVariable Long missionId,
            @Valid @RequestBody DroneMissionStatusUpdateRequest request
    ) {
        return orchestrationService.updateMissionStatus(missionId, request.status());
    }
}
