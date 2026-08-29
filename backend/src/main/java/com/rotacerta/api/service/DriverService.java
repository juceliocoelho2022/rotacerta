package com.rotacerta.api.service;

import com.rotacerta.api.dto.DriverAvailabilityUpdateRequest;
import com.rotacerta.api.dto.DriverCreateRequest;
import com.rotacerta.api.dto.DriverResponse;
import com.rotacerta.api.model.Driver;
import com.rotacerta.api.repository.DriverRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class DriverService {

    private final DriverRepository driverRepository;

    public DriverService(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    @Transactional(readOnly = true)
    public List<DriverResponse> findAll() {
        return driverRepository.findAll().stream()
                .sorted(Comparator.comparing(Driver::getName, String.CASE_INSENSITIVE_ORDER))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DriverResponse findById(Long id) {
        return toResponse(getDriver(id));
    }

    @Transactional
    public DriverResponse create(DriverCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Os dados do motorista são obrigatórios.");
        }

        String plate = request.vehiclePlate().trim().toUpperCase();
        if (driverRepository.existsByVehiclePlateIgnoreCase(plate)) {
            throw new IllegalArgumentException("Já existe motorista vinculado à placa " + plate + ".");
        }

        Driver driver = driverRepository.save(new Driver(
                request.name().trim(),
                request.latitude(),
                request.longitude(),
                request.available(),
                request.maxCapacity(),
                plate,
                request.vehicleModel().trim()
        ));

        return toResponse(driver);
    }

    @Transactional
    public DriverResponse updateAvailability(Long id, DriverAvailabilityUpdateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("A disponibilidade do motorista é obrigatória.");
        }

        Driver driver = getDriver(id);
        driver.setAvailable(request.available());
        return toResponse(driverRepository.save(driver));
    }

    private Driver getDriver(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("O ID do motorista é obrigatório.");
        }

        return driverRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Motorista não encontrado. ID: " + id));
    }

    private DriverResponse toResponse(Driver driver) {
        return new DriverResponse(
                driver.getId(),
                driver.getName(),
                driver.getLatitude(),
                driver.getLongitude(),
                driver.isAvailable(),
                driver.getCurrentLoad(),
                driver.getMaxCapacity(),
                driver.getVehiclePlate(),
                driver.getVehicleModel(),
                driver.getUpdatedAt()
        );
    }
}
