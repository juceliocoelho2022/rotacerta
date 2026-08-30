package com.rotacerta.api.service;

import com.rotacerta.api.dto.OperationsReportResponse;
import com.rotacerta.api.model.DeliveryStatus;
import com.rotacerta.api.model.OrderEntity;
import com.rotacerta.api.repository.DriverRepository;
import com.rotacerta.api.repository.DroneRepository;
import com.rotacerta.api.repository.IncidentRepository;
import com.rotacerta.api.repository.OrderRepository;
import com.rotacerta.api.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Service
public class ReportService {
    private final OrderRepository orderRepository;
    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;
    private final DroneRepository droneRepository;
    private final IncidentRepository incidentRepository;

    public ReportService(OrderRepository orderRepository, DriverRepository driverRepository,
                         VehicleRepository vehicleRepository, DroneRepository droneRepository,
                         IncidentRepository incidentRepository) {
        this.orderRepository = orderRepository;
        this.driverRepository = driverRepository;
        this.vehicleRepository = vehicleRepository;
        this.droneRepository = droneRepository;
        this.incidentRepository = incidentRepository;
    }

    @Transactional(readOnly = true)
    public OperationsReportResponse operations() {
        long totalOrders = orderRepository.count();
        long delivered = orderRepository.countByStatus(DeliveryStatus.DELIVERED);
        long failed = orderRepository.countByStatus(DeliveryStatus.DELIVERY_FAILED)
                + orderRepository.countByStatus(DeliveryStatus.RETURNED)
                + orderRepository.countByStatus(DeliveryStatus.CANCELLED);
        long inProgress = Math.max(0, totalOrders - delivered - failed);
        long terminal = delivered + failed;
        double successRate = terminal == 0 ? 0 : BigDecimal.valueOf((double) delivered * 100 / terminal)
                .setScale(1, RoundingMode.HALF_UP).doubleValue();

        BigDecimal deliveredRevenue = orderRepository.findAll().stream()
                .filter(order -> order.getStatus() == DeliveryStatus.DELIVERED)
                .map(OrderEntity::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (DeliveryStatus status : DeliveryStatus.values()) {
            byStatus.put(status.name(), orderRepository.countByStatus(status));
        }

        Set<String> closedStatuses = Set.of("RESOLVED", "CLOSED");
        var incidents = incidentRepository.findAll();
        long openIncidents = incidents.stream().filter(i -> !closedStatuses.contains(i.getStatus())).count();
        long criticalIncidents = incidents.stream()
                .filter(i -> "CRITICAL".equals(i.getSeverity()) && !closedStatuses.contains(i.getStatus()))
                .count();

        return new OperationsReportResponse(
                totalOrders, delivered, inProgress, failed, successRate, deliveredRevenue,
                driverRepository.count(), driverRepository.findByAvailableTrue().size(),
                vehicleRepository.count(), vehicleRepository.countByStatus("AVAILABLE"),
                vehicleRepository.countByStatus("MAINTENANCE"),
                droneRepository.count(), droneRepository.findByAvailableTrue().size(),
                openIncidents, criticalIncidents, byStatus
        );
    }
}
