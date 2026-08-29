package com.rotacerta.api.service;

import com.rotacerta.api.dto.DroneEligibilityResponse;
import com.rotacerta.api.dto.DroneMissionResponse;
import com.rotacerta.api.dto.DroneResponse;
import com.rotacerta.api.model.*;
import com.rotacerta.api.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class DroneDeliveryService {

    private static final double DRONE_CRUISE_SPEED_KMH = 45.0;
    private static final int HANDLING_MINUTES = 2;
    private static final String SIMULATION_MODE = "SIMULATION_ONLY";

    private final DroneRepository droneRepository;
    private final DroneMissionRepository missionRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository itemRepository;
    private final DeliveryLocationRepository locationRepository;
    private final TrackingEventRepository trackingEventRepository;

    public DroneDeliveryService(
            DroneRepository droneRepository,
            DroneMissionRepository missionRepository,
            OrderRepository orderRepository,
            OrderItemRepository itemRepository,
            DeliveryLocationRepository locationRepository,
            TrackingEventRepository trackingEventRepository
    ) {
        this.droneRepository = droneRepository;
        this.missionRepository = missionRepository;
        this.orderRepository = orderRepository;
        this.itemRepository = itemRepository;
        this.locationRepository = locationRepository;
        this.trackingEventRepository = trackingEventRepository;
    }

    @Transactional(readOnly = true)
    public List<DroneResponse> findAllDrones() {
        return droneRepository.findAll().stream()
                .sorted(Comparator.comparing(Drone::getCode))
                .map(this::toDroneResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DroneMissionResponse> findAllMissions() {
        return missionRepository.findAll().stream()
                .sorted(Comparator.comparing(DroneMission::getCreatedAt).reversed())
                .map(this::toMissionResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DroneEligibilityResponse eligibility(Long orderId) {
        OrderEntity order = getOrder(orderId);
        DeliveryLocation destination = locationRepository.findByOrderId(orderId).orElse(null);
        BigDecimal payload = payloadKg(orderId);
        List<String> blockers = new ArrayList<>();

        if (order.getStatus() != DeliveryStatus.READY_FOR_SHIPMENT) {
            blockers.add("Pedido precisa estar em READY_FOR_SHIPMENT.");
        }
        if (destination == null) {
            blockers.add("Destino sem latitude/longitude para planejamento aéreo.");
        }
        if (payload.compareTo(BigDecimal.ZERO) <= 0) {
            blockers.add("Peso do pedido não está disponível nos itens.");
        }
        if (missionRepository.findByOrderId(orderId).isPresent()) {
            blockers.add("Pedido já possui uma missão de drone.");
        }

        DroneCandidate candidate = destination == null || payload.compareTo(BigDecimal.ZERO) <= 0
                ? null
                : bestCandidate(payload, destination);

        if (candidate == null && destination != null && payload.compareTo(BigDecimal.ZERO) > 0) {
            blockers.add("Nenhum drone disponível atende peso, alcance e reserva de bateria.");
        }

        List<String> externalChecks = List.of(
                "Autorização/regra operacional aplicável da ANAC",
                "Autorização de uso do espaço aéreo pelo DECEA",
                "Homologação/comunicação aplicável da ANATEL",
                "Meteorologia e vento em tempo real",
                "Geofencing, obstáculos e zona segura de entrega"
        );

        return new DroneEligibilityResponse(
                order.getId(),
                order.getOrderNumber(),
                blockers.isEmpty() && candidate != null,
                payload,
                destination == null ? 0.0 : destination.getLatitude(),
                destination == null ? 0.0 : destination.getLongitude(),
                candidate == null ? null : candidate.drone().getId(),
                candidate == null ? null : candidate.drone().getCode(),
                candidate == null ? null : decimal(candidate.distanceKm(), 2),
                candidate == null ? null : etaMinutes(candidate.distanceKm()),
                blockers,
                externalChecks,
                SIMULATION_MODE
        );
    }

    @Transactional
    public DroneMissionResponse createMission(Long orderId) {
        if (missionRepository.findByOrderId(orderId).isPresent()) {
            throw new IllegalArgumentException("Pedido já possui missão de drone.");
        }

        DroneEligibilityResponse eligibility = eligibility(orderId);
        if (!eligibility.eligible() || eligibility.recommendedDroneId() == null) {
            throw new IllegalArgumentException("Pedido não elegível para drone: " + String.join(" ", eligibility.blockers()));
        }

        OrderEntity order = getOrder(orderId);
        DeliveryLocation destination = locationRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Destino sem coordenadas."));
        Drone drone = droneRepository.findById(eligibility.recommendedDroneId())
                .orElseThrow(() -> new IllegalArgumentException("Drone recomendado não encontrado."));

        drone.reserve();
        droneRepository.save(drone);

        DroneMission mission = missionRepository.save(new DroneMission(
                order,
                drone,
                eligibility.payloadKg(),
                eligibility.estimatedDistanceKm(),
                eligibility.estimatedEtaMinutes(),
                drone.getLatitude(),
                drone.getLongitude(),
                destination.getLatitude(),
                destination.getLongitude()
        ));

        return toMissionResponse(mission);
    }

    @Transactional
    public DroneMissionResponse updateMissionStatus(Long missionId, DroneMissionStatus status) {
        DroneMission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new IllegalArgumentException("Missão de drone não encontrada. ID: " + missionId));

        validateTransition(mission.getStatus(), status);
        mission.setStatus(status);

        if (status == DroneMissionStatus.IN_FLIGHT) {
            mission.getDrone().setStatus(DroneStatus.IN_FLIGHT);
            OrderEntity order = mission.getOrder();
            if (order.getStatus() == DeliveryStatus.READY_FOR_SHIPMENT) {
                order.setStatus(DeliveryStatus.SHIPPED);
                orderRepository.save(order);
                trackingEventRepository.save(new TrackingEvent(
                        order,
                        DeliveryStatus.SHIPPED,
                        "Drone Delivery • " + mission.getDrone().getCode(),
                        OffsetDateTime.now()
                ));
            }
        } else if (status == DroneMissionStatus.RETURNING) {
            mission.getDrone().setStatus(DroneStatus.RETURNING);
        } else if (status == DroneMissionStatus.COMPLETED || status == DroneMissionStatus.ABORTED) {
            mission.getDrone().setStatus(DroneStatus.AVAILABLE);
        }

        droneRepository.save(mission.getDrone());
        return toMissionResponse(missionRepository.save(mission));
    }

    private void validateTransition(DroneMissionStatus current, DroneMissionStatus next) {
        if (next == DroneMissionStatus.ABORTED) return;
        List<DroneMissionStatus> flow = List.of(
                DroneMissionStatus.PLANNED,
                DroneMissionStatus.AUTHORIZED,
                DroneMissionStatus.LOADING,
                DroneMissionStatus.READY_FOR_TAKEOFF,
                DroneMissionStatus.IN_FLIGHT,
                DroneMissionStatus.APPROACHING,
                DroneMissionStatus.LOWERING_PACKAGE,
                DroneMissionStatus.DELIVERED,
                DroneMissionStatus.RETURNING,
                DroneMissionStatus.COMPLETED
        );
        int currentIndex = flow.indexOf(current);
        int nextIndex = flow.indexOf(next);
        if (currentIndex < 0 || nextIndex != currentIndex + 1) {
            throw new IllegalArgumentException("Transição inválida de " + current + " para " + next + ".");
        }
    }

    private DroneCandidate bestCandidate(BigDecimal payload, DeliveryLocation destination) {
        return droneRepository.findByAvailableTrue().stream()
                .filter(Drone::operationallyAvailable)
                .filter(drone -> payload.compareTo(drone.getMaxPayloadKg()) <= 0)
                .map(drone -> {
                    double distance = distanceKm(drone.getLatitude(), drone.getLongitude(), destination.getLatitude(), destination.getLongitude());
                    double roundTrip = distance * 2.0;
                    double range = drone.getMaxRangeKm().doubleValue();
                    int requiredBattery = (int) Math.ceil((roundTrip / range) * 70.0 + 20.0);
                    return new DroneCandidate(drone, distance, roundTrip, requiredBattery);
                })
                .filter(candidate -> candidate.roundTripKm() <= candidate.drone().getMaxRangeKm().doubleValue())
                .filter(candidate -> candidate.requiredBatteryPercent() <= candidate.drone().getBatteryPercent())
                .min(Comparator.comparingDouble(candidate ->
                        candidate.distanceKm() * 5.0
                                + (100 - candidate.drone().getBatteryPercent()) * 0.15
                                + candidate.drone().getMaxPayloadKg().subtract(payload).doubleValue() * 0.2
                ))
                .orElse(null);
    }

    private BigDecimal payloadKg(Long orderId) {
        return itemRepository.findByOrderIdOrderByIdAsc(orderId).stream()
                .map(item -> item.getWeightKg().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(3, RoundingMode.HALF_UP);
    }

    private OrderEntity getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado. ID: " + orderId));
    }

    private int etaMinutes(double distanceKm) {
        return Math.max(1, (int) Math.ceil((distanceKm / DRONE_CRUISE_SPEED_KMH) * 60.0) + HANDLING_MINUTES);
    }

    private double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        double earthRadius = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return earthRadius * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private BigDecimal decimal(double value, int scale) {
        return BigDecimal.valueOf(value).setScale(scale, RoundingMode.HALF_UP);
    }

    private DroneResponse toDroneResponse(Drone drone) {
        return new DroneResponse(
                drone.getId(), drone.getCode(), drone.getModel(), drone.getStatus(),
                drone.getLatitude(), drone.getLongitude(), drone.getBatteryPercent(),
                drone.getMaxPayloadKg(), drone.getMaxRangeKm(), drone.isAvailable()
        );
    }

    private DroneMissionResponse toMissionResponse(DroneMission mission) {
        return new DroneMissionResponse(
                mission.getId(),
                mission.getOrder().getId(),
                mission.getOrder().getOrderNumber(),
                mission.getDrone().getId(),
                mission.getDrone().getCode(),
                mission.getDrone().getModel(),
                mission.getStatus(),
                mission.getPayloadKg(),
                mission.getDistanceKm(),
                mission.getEtaMinutes(),
                mission.getOriginLatitude(),
                mission.getOriginLongitude(),
                mission.getDestinationLatitude(),
                mission.getDestinationLongitude(),
                mission.getCreatedAt(),
                mission.getUpdatedAt()
        );
    }

    private record DroneCandidate(Drone drone, double distanceKm, double roundTripKm, int requiredBatteryPercent) {}
}
