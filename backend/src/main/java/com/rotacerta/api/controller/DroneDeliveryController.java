package com.rotacerta.api.controller;

import com.rotacerta.api.dto.DroneAuthorizationRequest;
import com.rotacerta.api.dto.DroneAuthorizationResponse;
import com.rotacerta.api.dto.DroneEligibilityResponse;
import com.rotacerta.api.dto.DroneMissionResponse;
import com.rotacerta.api.dto.DroneMissionStatusUpdateRequest;
import com.rotacerta.api.dto.DroneResponse;
import com.rotacerta.api.service.DroneDeliveryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drone-delivery")
public class DroneDeliveryController {

    private final DroneDeliveryService service;

    public DroneDeliveryController(DroneDeliveryService service) {
        this.service = service;
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
        return service.createMission(orderId);
    }

    @GetMapping("/missions/{missionId}/authorizations")
    public List<DroneAuthorizationResponse> authorizations(@PathVariable Long missionId) {
        return service.findAuthorizations(missionId);
    }

    @PostMapping("/missions/{missionId}/authorizations")
    @ResponseStatus(HttpStatus.CREATED)
    public DroneAuthorizationResponse authorizeMission(
            @PathVariable Long missionId,
            @Valid @RequestBody DroneAuthorizationRequest request
    ) {
        return service.createAuthorization(missionId, request);
    }

    @PatchMapping("/missions/{missionId}/status")
    public DroneMissionResponse updateMissionStatus(
            @PathVariable Long missionId,
            @Valid @RequestBody DroneMissionStatusUpdateRequest request
    ) {
        return service.updateMissionStatus(missionId, request.status());
    }
}
