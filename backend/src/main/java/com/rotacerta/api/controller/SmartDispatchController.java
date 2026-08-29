package com.rotacerta.api.controller;

import com.rotacerta.api.dto.DispatchAssignmentResponse;
import com.rotacerta.api.dto.DriverLocationUpdateRequest;
import com.rotacerta.api.dto.DriverRouteResponse;
import com.rotacerta.api.service.SmartDispatchService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dispatch")
public class SmartDispatchController {

    private final SmartDispatchService smartDispatchService;

    public SmartDispatchController(SmartDispatchService smartDispatchService) {
        this.smartDispatchService = smartDispatchService;
    }

    @PostMapping("/orders/{orderId}/assign")
    public DispatchAssignmentResponse assign(@PathVariable Long orderId) {
        return smartDispatchService.assignBestDriver(orderId);
    }

    @GetMapping("/orders/{orderId}")
    public DispatchAssignmentResponse assignment(@PathVariable Long orderId) {
        return smartDispatchService.getAssignment(orderId);
    }

    @GetMapping("/drivers/{driverId}/route")
    public DriverRouteResponse route(@PathVariable Long driverId) {
        return smartDispatchService.getOptimizedRoute(driverId);
    }

    @PatchMapping("/drivers/{driverId}/location")
    public DriverRouteResponse updateLocation(
            @PathVariable Long driverId,
            @Valid @RequestBody DriverLocationUpdateRequest request
    ) {
        return smartDispatchService.updateDriverLocation(driverId, request);
    }
}
