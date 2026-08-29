package com.rotacerta.api.service;

import com.rotacerta.api.dto.DispatchAssignmentResponse;
import com.rotacerta.api.dto.DriverLocationUpdateRequest;
import com.rotacerta.api.dto.DriverRouteResponse;
import com.rotacerta.api.dto.DriverRouteStopResponse;
import com.rotacerta.api.dto.MonitoringDriverResponse;
import com.rotacerta.api.dto.MonitoringOrderResponse;
import com.rotacerta.api.dto.OperationsMonitoringResponse;
import com.rotacerta.api.model.DeliveryAssignment;
import com.rotacerta.api.model.DeliveryLocation;
import com.rotacerta.api.model.DeliveryStatus;
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
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SmartDispatchService {

    private static final double URBAN_AVERAGE_SPEED_KMH = 28.0;
    private static final int SERVICE_MINUTES_PER_STOP = 4;
    private static final Set<DeliveryStatus> IN_PROGRESS_STATUSES = Set.of(
            DeliveryStatus.PICKING,
            DeliveryStatus.PACKING,
            DeliveryStatus.READY_FOR_SHIPMENT,
            DeliveryStatus.SHIPPED,
            DeliveryStatus.IN_TRANSIT,
            DeliveryStatus.OUT_FOR_DELIVERY
    );

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

    @Transactional(readOnly = true)
    public OperationsMonitoringResponse monitoring() {
        List<OrderEntity> orders = orderRepository.findAll();
        List<Driver> drivers = driverRepository.findAll();
        List<DeliveryLocation> locations = locationRepository.findAll();
        List<DeliveryAssignment> assignments = assignmentRepository.findAll();

        Map<Long, DeliveryLocation> locationByOrder = locations.stream()
                .collect(Collectors.toMap(
                        location -> location.getOrder().getId(),
                        Function.identity()
                ));

        Map<Long, DeliveryAssignment> assignmentByOrder = assignments.stream()
                .collect(Collectors.toMap(
                        assignment -> assignment.getOrder().getId(),
                        Function.identity()
                ));

        long delivered = orders.stream()
                .filter(order -> order.getStatus() == DeliveryStatus.DELIVERED)
                .count();

        long failed = orders.stream()
                .filter(order -> order.getStatus() == DeliveryStatus.DELIVERY_FAILED)
                .count();

        long inProgress = orders.stream()
                .filter(order -> IN_PROGRESS_STATUSES.contains(order.getStatus()))
                .count();

        long delayed = assignments.stream()
                .filter(assignment -> {
                    DeliveryLocation location = locationByOrder.get(assignment.getOrder().getId());
                    return location != null && assignment.getEtaMinutes() > location.getSlaMinutes();
                })
                .count();

        long completed = delivered + failed;
        double successRate = completed == 0
                ? 0.0
                : BigDecimal.valueOf((delivered * 100.0) / completed)
                        .setScale(1, RoundingMode.HALF_UP)
                        .doubleValue();

        List<MonitoringDriverResponse> driverResponses = drivers.stream()
                .map(driver -> new MonitoringDriverResponse(
                        driver.getId(),
                        driver.getName(),
                        driver.getLatitude(),
                        driver.getLongitude(),
                        driver.isAvailable(),
                        driver.getCurrentLoad(),
                        driver.getMaxCapacity()
                ))
                .toList();

        List<MonitoringOrderResponse> orderResponses = orders.stream()
                .filter(order -> locationByOrder.containsKey(order.getId()))
                .map(order -> toMonitoringOrder(
                        order,
                        locationByOrder.get(order.getId()),
                        assignmentByOrder.get(order.getId())
                ))
                .toList();

        return new OperationsMonitoringResponse(
                orders.size(),
                inProgress,
                delivered,
                delayed,
                drivers.stream().filter(Driver::isAvailable).count(),
                successRate,
                driverResponses,
                orderResponses
        );
    }

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
            final double originLat = currentLat;
            final double originLon = currentLon;

            DeliveryAssignment next = remaining.stream()
                    .min(Comparator.comparingDouble(assignment -> {
                        DeliveryLocation location = getLocation(assignment.getOrder().getId());
                        return distanceKm(
                                originLat,
                                originLon,
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

    private MonitoringOrderResponse toMonitoringOrder(
            OrderEntity order,
            DeliveryLocation location,
            DeliveryAssignment assignment
    ) {
        return new MonitoringOrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getCustomer().getName(),
                order.getStatus(),
                location.getLatitude(),
                location.getLongitude(),
                location.getPriority(),
                location.getSlaMinutes(),
                assignment == null ? null : assignment.getDriver().getId(),
                assignment == null ? null : assignment.getDriver().getName(),
                assignment == null ? null : assignment.getEtaMinutes(),
                assignment == null ? null : assignment.getDistanceKm(),
                assignment == null ? null : assignment.getScore()
        );
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
