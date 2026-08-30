package com.rotacerta.api.service;

import com.rotacerta.api.dto.IncidentDtos;
import com.rotacerta.api.model.Driver;
import com.rotacerta.api.model.Incident;
import com.rotacerta.api.model.OrderEntity;
import com.rotacerta.api.model.Vehicle;
import com.rotacerta.api.repository.DriverRepository;
import com.rotacerta.api.repository.IncidentRepository;
import com.rotacerta.api.repository.OrderRepository;
import com.rotacerta.api.repository.VehicleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class IncidentService {

    private static final Set<String> SEVERITIES = Set.of("LOW", "MEDIUM", "HIGH", "CRITICAL");
    private static final Set<String> CATEGORIES = Set.of("OPERATIONAL", "VEHICLE", "DELIVERY", "CUSTOMER", "SAFETY", "SYSTEM");
    private static final Set<String> STATUSES = Set.of("OPEN", "IN_PROGRESS", "RESOLVED", "CLOSED");

    private final IncidentRepository incidentRepository;
    private final OrderRepository orderRepository;
    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;

    public IncidentService(IncidentRepository incidentRepository, OrderRepository orderRepository,
                           DriverRepository driverRepository, VehicleRepository vehicleRepository) {
        this.incidentRepository = incidentRepository;
        this.orderRepository = orderRepository;
        this.driverRepository = driverRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @Transactional(readOnly = true)
    public List<IncidentDtos.Response> findAll() {
        return incidentRepository.findAllByOrderByOpenedAtDesc().stream().map(this::toResponse).toList();
    }

    @Transactional
    public IncidentDtos.Response create(IncidentDtos.CreateRequest request) {
        validateReferences(request.orderId(), request.driverId(), request.vehicleId());
        String severity = normalize(request.severity());
        String category = normalize(request.category());
        if (!SEVERITIES.contains(severity)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Severidade inválida.");
        }
        if (!CATEGORIES.contains(category)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Categoria inválida.");
        }
        Incident incident = new Incident(
                request.orderId(), request.driverId(), request.vehicleId(), severity, category,
                request.title().trim(), request.description().trim(), trimToNull(request.location())
        );
        return toResponse(incidentRepository.save(incident));
    }

    @Transactional
    public IncidentDtos.Response updateStatus(Long id, IncidentDtos.StatusUpdateRequest request) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ocorrência não encontrada."));
        String status = normalize(request.status());
        if (!STATUSES.contains(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status de ocorrência inválido.");
        }
        if (("RESOLVED".equals(status) || "CLOSED".equals(status)) && trimToNull(request.resolution()) == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe a resolução para encerrar a ocorrência.");
        }
        incident.updateStatus(status, trimToNull(request.resolution()));
        return toResponse(incidentRepository.save(incident));
    }

    private void validateReferences(Long orderId, Long driverId, Long vehicleId) {
        if (orderId != null && !orderRepository.existsById(orderId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pedido informado não existe.");
        }
        if (driverId != null && !driverRepository.existsById(driverId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Motorista informado não existe.");
        }
        if (vehicleId != null && !vehicleRepository.existsById(vehicleId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Veículo informado não existe.");
        }
    }

    private IncidentDtos.Response toResponse(Incident incident) {
        String orderNumber = incident.getOrderId() == null ? null : orderRepository.findById(incident.getOrderId()).map(OrderEntity::getOrderNumber).orElse(null);
        String driverName = incident.getDriverId() == null ? null : driverRepository.findById(incident.getDriverId()).map(Driver::getName).orElse(null);
        String vehiclePlate = incident.getVehicleId() == null ? null : vehicleRepository.findById(incident.getVehicleId()).map(Vehicle::getPlate).orElse(null);
        return new IncidentDtos.Response(
                incident.getId(), incident.getOrderId(), orderNumber, incident.getDriverId(), driverName,
                incident.getVehicleId(), vehiclePlate, incident.getSeverity(), incident.getStatus(), incident.getCategory(),
                incident.getTitle(), incident.getDescription(), incident.getLocation(), incident.getResolution(),
                incident.getOpenedAt(), incident.getResolvedAt()
        );
    }

    private String normalize(String value) {
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
