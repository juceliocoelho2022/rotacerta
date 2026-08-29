package com.rotacerta.api.service;

import com.rotacerta.api.dto.DispatchAssignmentResponse;
import com.rotacerta.api.dto.DriverLocationUpdateRequest;
import com.rotacerta.api.dto.DriverRouteResponse;
import com.rotacerta.api.dto.DriverRouteStopResponse;
import com.rotacerta.api.model.DeliveryAssignment;
import com.rotacerta.api.model.DeliveryLocation;
import com.rotacerta.api.model.Driver;
import com.rotacerta.api.model.OrderEntity;
import com.rotacerta.api.repository.DeliveryAssignmentRepository;
import com.rotacerta.api.repository.DeliveryLocationRepository;
import com.rotacerta.api.repository.DriverRepository;
import com.rotacerta.api.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class SmartDispatchService {

    private static final double URBAN_AVERAGE_SPEED_KMH = 28.0;
    private static final int SERVICE_MINUTES_PER_STOP = 4;

    private final OrderRepository orderRepository;
    private final DriverRepository driverRepository;
    private final DeliveryLocationRepository locationRepository;
    private final DeliveryAssignmentRepository assignmentRepository;

    public SmartDispatchService(
            OrderRepository orderRepository,
            DriverRepository driverRepository,
            DeliveryLocationRepository locationRepository,
            DeliveryAssignmentRepository assignmentRepository
    ) {
        this.orderRepository = orderRepository;
        this.driverRepository = driverRepository;
        this.locationRepository = locationRepository;
        this.assignmentRepository = assignmentRepository;
    }

    /**
     * Seleciona automaticamente o motorista com menor score logístico.
     *
     * O score combina distância até o destino, ocupação atual do motorista,
     * pressão de SLA e prioridade do pedido. Quanto menor, melhor.
     */
    @Transactional
    public DispatchAssignmentResponse assignBestDriver(Long orderId) {
        DeliveryAssignment existing = assignmentRepository.findByOrderId(orderId).orElse(null);
        if (existing != null) {
            return toResponse(existing);
        }

        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado. ID: " + orderId));

        DeliveryLocation destination = locationRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Pedido sem coordenadas de entrega. ID: " + orderId
                ));

        Candidate winner = driverRepository.findByAvailableTrue().stream()
                .filter(Driver::hasCapacity)
                .map(driver -> candidate(driver, destination))
                .min(Comparator.comparingDouble(Candidate::score))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Nenhum motorista disponível com capacidade para a entrega."
                ));

        Driver driver = winner.driver();
        driver.incrementLoad();
        driverRepository.save(driver);

        DeliveryAssignment assignment = assignmentRepository.save(
                new DeliveryAssignment(
                        order,
                        driver,
                        decimal(winner.distanceKm(), 2),
                        decimal(winner.score(), 4),
                        winner.etaMinutes()
                )
        );

        return toResponse(assignment);
    }

    @Transactional(readOnly = true)
    public DispatchAssignmentResponse getAssignment(Long orderId) {
        return assignmentRepository.findByOrderId(orderId)
                .map(this::toResponse)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Pedido ainda não possui motorista atribuído. ID: " + orderId
                ));
    }

    /** Atualiza a posição do motorista; a próxima consulta de rota será recalculada. */
    @Transactional
    public DriverRouteResponse updateDriverLocation(
            Long driverId,
            DriverLocationUpdateRequest request
    ) {
        Driver driver = getDriver(driverId);
        driver.updateLocation(request.latitude(), request.longitude());
        driverRepository.save(driver);
        return buildRoute(driver);
    }

    /**
     * Recalcula dinamicamente a sequência das entregas atribuídas usando
     * nearest-neighbor como heurística inicial de VRP.
     */
    @Transactional(readOnly = true)
    public DriverRouteResponse getOptimizedRoute(Long driverId) {
        return buildRoute(getDriver(driverId));
    }

    private DriverRouteResponse buildRoute(Driver driver) {
        List<DeliveryAssignment> remaining = new ArrayList<>(
                assignmentRepository.findByDriverIdAndStatusOrderByAssignedAtAsc(
                        driver.getId(),
                        "ASSIGNED"
                )
        );

        List<DriverRouteStopResponse> stops = new ArrayList<>();
        double currentLat = driver.getLatitude();
        double currentLon = driver.getLongitude();
        double totalDistance = 0.0;
        int elapsedMinutes = 0;
        int position = 1;

        while (!remaining.isEmpty()) {
            DeliveryAssignment next = remaining.stream()
                    .min(Comparator.comparingDouble(assignment -> {
                        DeliveryLocation location = getLocation(assignment.getOrder().getId());
                        return distanceKm(
                                currentLat,
                                currentLon,
                                location.getLatitude(),
                                location.getLongitude()
                        );
                    }))
                    .orElseThrow();

            DeliveryLocation location = getLocation(next.getOrder().getId());
            double legDistance = distanceKm(
                    currentLat,
                    currentLon,
                    location.getLatitude(),
                    location.getLongitude()
            );

            elapsedMinutes += travelMinutes(legDistance);
            totalDistance += legDistance;

            stops.add(new DriverRouteStopResponse(
                    position++,
                    next.getOrder().getId(),
                    next.getOrder().getOrderNumber(),
                    location.getLatitude(),
                    location.getLongitude(),
                    location.getPriority(),
                    decimal(legDistance, 2),
                    elapsedMinutes
            ));

            elapsedMinutes += SERVICE_MINUTES_PER_STOP;
            currentLat = location.getLatitude();
            currentLon = location.getLongitude();
            remaining.remove(next);
        }

        return new DriverRouteResponse(
                driver.getId(),
                driver.getName(),
                stops.size(),
                decimal(totalDistance, 2),
                stops.isEmpty() ? 0 : Math.max(0, elapsedMinutes - SERVICE_MINUTES_PER_STOP),
                stops
        );
    }

    private Candidate candidate(Driver driver, DeliveryLocation destination) {
        double distance = distanceKm(
                driver.getLatitude(),
                driver.getLongitude(),
                destination.getLatitude(),
                destination.getLongitude()
        );

        int etaMinutes = travelMinutes(distance)
                + driver.getCurrentLoad() * SERVICE_MINUTES_PER_STOP;

        double slaRatio = (double) etaMinutes / destination.getSlaMinutes();

        double score =
                (distance * 8.0)
                        + (driver.loadRatio() * 20.0)
                        + (slaRatio * 15.0)
                        - (destination.getPriority() * 2.0);

        return new Candidate(driver, distance, Math.max(0.0, score), etaMinutes);
    }

    private Driver getDriver(Long driverId) {
        return driverRepository.findById(driverId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Motorista não encontrado. ID: " + driverId
                ));
    }

    private DeliveryLocation getLocation(Long orderId) {
        return locationRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Pedido sem coordenadas de entrega. ID: " + orderId
                ));
    }

    private DispatchAssignmentResponse toResponse(DeliveryAssignment assignment) {
        return new DispatchAssignmentResponse(
                assignment.getId(),
                assignment.getOrder().getId(),
                assignment.getOrder().getOrderNumber(),
                assignment.getDriver().getId(),
                assignment.getDriver().getName(),
                assignment.getDistanceKm(),
                assignment.getScore(),
                assignment.getEtaMinutes(),
                assignment.getStatus(),
                assignment.getAssignedAt()
        );
    }

    private int travelMinutes(double distanceKm) {
        return Math.max(1, (int) Math.ceil((distanceKm / URBAN_AVERAGE_SPEED_KMH) * 60.0));
    }

    /** Distância geodésica pela fórmula de Haversine. */
    private double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        double earthRadiusKm = 6371.0088;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadiusKm * c;
    }

    private BigDecimal decimal(double value, int scale) {
        return BigDecimal.valueOf(value).setScale(scale, RoundingMode.HALF_UP);
    }

    private record Candidate(
            Driver driver,
            double distanceKm,
            double score,
            int etaMinutes
    ) {}
}
