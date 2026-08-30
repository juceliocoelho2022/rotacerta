package com.rotacerta.api.controller;

import com.rotacerta.api.dto.VehicleDtos;
import com.rotacerta.api.service.VehicleService;
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
@RequestMapping("/api/vehicles")
public class VehicleController {
    private final VehicleService service;

    public VehicleController(VehicleService service) {
        this.service = service;
    }

    @GetMapping
    public List<VehicleDtos.Response> findAll() {
        return service.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VehicleDtos.Response create(@Valid @RequestBody VehicleDtos.CreateRequest request) {
        return service.create(request);
    }

    @PatchMapping("/{id}/status")
    public VehicleDtos.Response updateStatus(@PathVariable Long id,
                                             @Valid @RequestBody VehicleDtos.StatusUpdateRequest request) {
        return service.updateStatus(id, request);
    }
}
