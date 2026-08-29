package com.rotacerta.api.controller;

import com.rotacerta.api.dto.DriverAvailabilityUpdateRequest;
import com.rotacerta.api.dto.DriverCreateRequest;
import com.rotacerta.api.dto.DriverResponse;
import com.rotacerta.api.service.DriverService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/drivers")
public class DriverController {

    private final DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    @GetMapping
    public List<DriverResponse> findAll() {
        return driverService.findAll();
    }

    @GetMapping("/{id}")
    public DriverResponse findById(@PathVariable Long id) {
        return driverService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DriverResponse create(@Valid @RequestBody DriverCreateRequest request) {
        return driverService.create(request);
    }

    @PatchMapping("/{id}/availability")
    public DriverResponse updateAvailability(
            @PathVariable Long id,
            @Valid @RequestBody DriverAvailabilityUpdateRequest request
    ) {
        return driverService.updateAvailability(id, request);
    }
}
