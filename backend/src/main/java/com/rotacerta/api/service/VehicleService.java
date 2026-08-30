package com.rotacerta.api.service;

import com.rotacerta.api.dto.VehicleDtos;
import com.rotacerta.api.model.Driver;
import com.rotacerta.api.model.Vehicle;
import com.rotacerta.api.repository.DriverRepository;
import com.rotacerta.api.repository.VehicleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class VehicleService {

    private static final Set<String> ALLOWED_STATUSES = Set.of("AVAILABLE", "IN_OPERATION", "MAINTENANCE", "OUT_OF_SERVICE");

    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;

    public VehicleService(VehicleRepository vehicleRepository, DriverRepository driverRepository) {
        this.vehicleRepository = vehicleRepository;
        this.driverRepository = driverRepository;
    }

    @Transactional(readOnly = true)
    public List<VehicleDtos.Response> findAll() {
        return vehicleRepository.findAllByOrderByUpdatedAtDesc().stream().map(this::toResponse).toList();
    }

    @Transactional
    public VehicleDtos.Response create(VehicleDtos.CreateRequest request) {
        String plate = request.plate().trim().toUpperCase(Locale.ROOT);
        if (vehicleRepository.existsByPlateIgnoreCase(plate)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe veículo com esta placa.");
        }
        validateDriverAssignment(request.driverId(), null);
        Vehicle vehicle = new Vehicle(
                plate,
                request.model().trim(),
                request.vehicleType().trim(),
                request.maxCapacity(),
                request.currentOdometerKm(),
                request.fuelType().trim(),
                request.nextMaintenanceKm(),
                request.driverId()
        );
        return toResponse(vehicleRepository.save(vehicle));
    }

    @Transactional
    public VehicleDtos.Response updateStatus(Long id, VehicleDtos.StatusUpdateRequest request) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Veículo não encontrado."));
        String status = request.status().trim().toUpperCase(Locale.ROOT);
        if (!ALLOWED_STATUSES.contains(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status de veículo inválido.");
        }
        validateDriverAssignment(request.driverId(), id);
        vehicle.updateStatus(status);
        vehicle.updateAssignment(request.driverId());
        return toResponse(vehicleRepository.save(vehicle));
    }

    private void validateDriverAssignment(Long driverId, Long currentVehicleId) {
        if (driverId == null) return;
        driverRepository.findById(driverId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Motorista informado não existe."));
        boolean assignedElsewhere = vehicleRepository.findAll().stream()
                .anyMatch(v -> driverId.equals(v.getDriverId()) && !v.getId().equals(currentVehicleId));
        if (assignedElsewhere) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Motorista já está associado a outro veículo.");
        }
    }

    private VehicleDtos.Response toResponse(Vehicle vehicle) {
        String driverName = null;
        if (vehicle.getDriverId() != null) {
            driverName = driverRepository.findById(vehicle.getDriverId()).map(Driver::getName).orElse(null);
        }
        return new VehicleDtos.Response(
                vehicle.getId(), vehicle.getPlate(), vehicle.getModel(), vehicle.getVehicleType(), vehicle.getStatus(),
                vehicle.getMaxCapacity(), vehicle.getCurrentOdometerKm(), vehicle.getFuelType(), vehicle.getNextMaintenanceKm(),
                vehicle.getDriverId(), driverName, vehicle.getUpdatedAt()
        );
    }
}
