package com.rotacerta.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rotacerta.api.dto.*;
import com.rotacerta.api.model.*;
import com.rotacerta.api.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DroneDeliveryService {

    private static final double DRONE_CRUISE_SPEED_KMH = 45.0;
    private static final int HANDLING_MINUTES = 2;
    private static final String SIMULATION_MODE = "SIMULATION_ONLY";
    private static final String AUTHORIZATION_POLICY_VERSION = "DRONE-SIM-AUTH-1.0";

    private final DroneRepository droneRepository;
    private final DroneMissionRepository missionRepository;
    private final DroneMissionAuthorizationRepository authorizationRepository;
    private final DroneAuthorizationEvidenceRepository evidenceRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository itemRepository;
    private final DeliveryLocationRepository locationRepository;
    private final TrackingEventRepository trackingEventRepository;
    private final ObjectMapper objectMapper;

    public DroneDeliveryService(
            DroneRepository droneRepository,
            DroneMissionRepository missionRepository,
            DroneMissionAuthorizationRepository authorizationRepository,
            DroneAuthorizationEvidenceRepository evidenceRepository,
            OrderRepository orderRepository,
            OrderItemRepository itemRepository,
            DeliveryLocationRepository locationRepository,
            TrackingEventRepository trackingEventRepository,
            ObjectMapper objectMapper
    ) {
        this.droneRepository = droneRepository;
        this.missionRepository = missionRepository;
        this.authorizationRepository = authorizationRepository;
        this.evidenceRepository = evidenceRepository;
        this.orderRepository = orderRepository;
        this.itemRepository = itemRepository;
        this.locationRepository = locationRepository;
        this.trackingEventRepository = trackingEventRepository;
        this.objectMapper = objectMapper;
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
    public List<DroneAuthorizationResponse> findAuthorizations(Long missionId) {
        getMission(missionId);
        return authorizationRepository.findByMissionIdOrderByAuthorizedAtDesc(missionId).stream()
                .map(this::toAuthorizationResponse)
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
    public DroneAuthorizationResponse createAuthorization(Long missionId, DroneAuthorizationRequest request) {
        DroneMission mission = getMission(missionId);

        if (mission.getStatus() != DroneMissionStatus.PLANNED
                && mission.getStatus() != DroneMissionStatus.AUTHORIZED) {
            throw new IllegalArgumentException(
                    "Autorização só pode ser registrada antes do carregamento. Status atual: " + mission.getStatus()
            );
        }
        if (mission.getStatus() == DroneMissionStatus.AUTHORIZED
                && request.decision() == DroneAuthorizationDecision.REJECTED) {
            throw new IllegalArgumentException("Uma missão já autorizada só pode receber renovação de autorização.");
        }

        AuditChecks checks = auditChecks(mission);
        if (request.decision() == DroneAuthorizationDecision.APPROVED_SIMULATION && !checks.internalPassed()) {
            throw new IllegalArgumentException(
                    "Autorização simulada bloqueada: peso, bateria e rota precisam estar válidos no momento da decisão."
            );
        }

        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime validUntil = now.plusMinutes(request.validMinutes());
        String snapshot = buildContextSnapshot(mission, checks);
        String fingerprint = fingerprint(snapshot);

        DroneMissionAuthorization authorization = authorizationRepository.save(
                new DroneMissionAuthorization(
                        mission,
                        request.decision(),
                        request.authorizedBy().trim(),
                        now,
                        now,
                        validUntil,
                        request.reason().trim(),
                        AUTHORIZATION_POLICY_VERSION,
                        SIMULATION_MODE,
                        DroneAuditCheck.PENDING_EXTERNAL,
                        DroneAuditCheck.PENDING_EXTERNAL,
                        DroneAuditCheck.PENDING_EXTERNAL,
                        checks.payloadCheck(),
                        checks.batteryCheck(),
                        checks.routeCheck(),
                        snapshot,
                        fingerprint
                )
        );

        if (request.evidence() != null) {
            request.evidence().stream()
                    .filter(item -> item != null)
                    .forEach(item -> evidenceRepository.save(new DroneAuthorizationEvidence(
                            authorization,
                            item.evidenceType().trim(),
                            item.reference().trim(),
                            item.description() == null || item.description().isBlank() ? null : item.description().trim()
                    )));
        }

        if (request.decision() == DroneAuthorizationDecision.APPROVED_SIMULATION
                && mission.getStatus() == DroneMissionStatus.PLANNED) {
            mission.setStatus(DroneMissionStatus.AUTHORIZED);
            missionRepository.save(mission);
        }

        return toAuthorizationResponse(authorization);
    }

    @Transactional
    public DroneMissionResponse updateMissionStatus(Long missionId, DroneMissionStatus status) {
        DroneMission mission = getMission(missionId);

        validateTransition(mission, status);
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

    private void validateTransition(DroneMission mission, DroneMissionStatus next) {
        DroneMissionStatus current = mission.getStatus();
        if (next == DroneMissionStatus.ABORTED) return;

        if (current == DroneMissionStatus.PLANNED && next == DroneMissionStatus.AUTHORIZED) {
            throw new IllegalArgumentException(
                    "Use o endpoint de autorização auditável para registrar responsável, validade, contexto e evidências."
            );
        }

        if (current == DroneMissionStatus.AUTHORIZED && next == DroneMissionStatus.LOADING) {
            ensureActiveAuthorization(mission);
        }

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

    private void ensureActiveAuthorization(DroneMission mission) {
        DroneMissionAuthorization authorization = authorizationRepository
                .findFirstByMissionIdAndDecisionOrderByAuthorizedAtDesc(
                        mission.getId(),
                        DroneAuthorizationDecision.APPROVED_SIMULATION
                )
                .orElseThrow(() -> new IllegalArgumentException(
                        "Missão sem autorização auditável aprovada."
                ));

        OffsetDateTime now = OffsetDateTime.now();
        if (!authorization.isActiveAt(now)) {
            throw new IllegalArgumentException(
                    "A autorização da missão expirou. Registre uma nova decisão antes do carregamento."
            );
        }

        AuditChecks currentChecks = auditChecks(mission);
        if (!currentChecks.internalPassed()) {
            throw new IllegalArgumentException(
                    "O contexto técnico deixou de ser válido após a autorização. Reavalie a missão."
            );
        }

        String currentFingerprint = fingerprint(buildContextSnapshot(mission, currentChecks));
        if (!authorization.getContextFingerprint().equals(currentFingerprint)) {
            throw new IllegalArgumentException(
                    "O contexto operacional mudou após a autorização. Registre uma nova autorização auditável."
            );
        }
    }

    private AuditChecks auditChecks(DroneMission mission) {
        Drone drone = mission.getDrone();
        boolean payloadOk = mission.getPayloadKg().compareTo(drone.getMaxPayloadKg()) <= 0;
        double roundTrip = mission.getDistanceKm().doubleValue() * 2.0;
        boolean routeOk = roundTrip <= drone.getMaxRangeKm().doubleValue();
        int requiredBattery = requiredBatteryPercent(roundTrip, drone.getMaxRangeKm().doubleValue());
        boolean batteryOk = requiredBattery <= drone.getBatteryPercent();

        return new AuditChecks(
                payloadOk ? DroneAuditCheck.PASSED : DroneAuditCheck.FAILED,
                batteryOk ? DroneAuditCheck.PASSED : DroneAuditCheck.FAILED,
                routeOk ? DroneAuditCheck.PASSED : DroneAuditCheck.FAILED,
                roundTrip,
                requiredBattery
        );
    }

    private String buildContextSnapshot(DroneMission mission, AuditChecks checks) {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("missionId", mission.getId());
        context.put("orderId", mission.getOrder().getId());
        context.put("orderNumber", mission.getOrder().getOrderNumber());
        context.put("droneId", mission.getDrone().getId());
        context.put("droneCode", mission.getDrone().getCode());
        context.put("droneModel", mission.getDrone().getModel());
        context.put("droneStatus", mission.getDrone().getStatus().name());
        context.put("batteryPercent", mission.getDrone().getBatteryPercent());
        context.put("maxPayloadKg", mission.getDrone().getMaxPayloadKg());
        context.put("payloadKg", mission.getPayloadKg());
        context.put("maxRangeKm", mission.getDrone().getMaxRangeKm());
        context.put("oneWayDistanceKm", mission.getDistanceKm());
        context.put("roundTripDistanceKm", decimal(checks.roundTripKm(), 2));
        context.put("requiredBatteryPercent", checks.requiredBatteryPercent());
        context.put("etaMinutes", mission.getEtaMinutes());
        context.put("originLatitude", mission.getOriginLatitude());
        context.put("originLongitude", mission.getOriginLongitude());
        context.put("destinationLatitude", mission.getDestinationLatitude());
        context.put("destinationLongitude", mission.getDestinationLongitude());
        context.put("payloadCheck", checks.payloadCheck().name());
        context.put("batteryCheck", checks.batteryCheck().name());
        context.put("routeCheck", checks.routeCheck().name());
        context.put("airspaceCheck", DroneAuditCheck.PENDING_EXTERNAL.name());
        context.put("weatherCheck", DroneAuditCheck.PENDING_EXTERNAL.name());
        context.put("geofenceCheck", DroneAuditCheck.PENDING_EXTERNAL.name());
        context.put("policyVersion", AUTHORIZATION_POLICY_VERSION);
        context.put("mode", SIMULATION_MODE);

        try {
            return objectMapper.writeValueAsString(context);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Não foi possível registrar o snapshot de autorização.", exception);
        }
    }

    private String fingerprint(String snapshot) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(snapshot.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 indisponível para auditoria.", exception);
        }
    }

    private int requiredBatteryPercent(double roundTripKm, double maxRangeKm) {
        return (int) Math.ceil((roundTripKm / maxRangeKm) * 70.0 + 20.0);
    }

    private DroneCandidate bestCandidate(BigDecimal payload, DeliveryLocation destination) {
        return droneRepository.findByAvailableTrue().stream()
                .filter(Drone::operationallyAvailable)
                .filter(drone -> payload.compareTo(drone.getMaxPayloadKg()) <= 0)
                .map(drone -> {
                    double distance = distanceKm(
                            drone.getLatitude(),
                            drone.getLongitude(),
                            destination.getLatitude(),
                            destination.getLongitude()
                    );
                    double roundTrip = distance * 2.0;
                    double range = drone.getMaxRangeKm().doubleValue();
                    int requiredBattery = requiredBatteryPercent(roundTrip, range);
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

    private DroneMission getMission(Long missionId) {
        return missionRepository.findById(missionId)
                .orElseThrow(() -> new IllegalArgumentException("Missão de drone não encontrada. ID: " + missionId));
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
                drone.getId(),
                drone.getCode(),
                drone.getModel(),
                drone.getStatus(),
                drone.getLatitude(),
                drone.getLongitude(),
                drone.getBatteryPercent(),
                drone.getMaxPayloadKg(),
                drone.getMaxRangeKm(),
                drone.isAvailable()
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

    private DroneAuthorizationResponse toAuthorizationResponse(DroneMissionAuthorization authorization) {
        List<DroneAuthorizationEvidenceResponse> evidence = evidenceRepository
                .findByAuthorizationIdOrderByCreatedAtAsc(authorization.getId()).stream()
                .map(item -> new DroneAuthorizationEvidenceResponse(
                        item.getId(),
                        item.getEvidenceType(),
                        item.getReference(),
                        item.getDescription(),
                        item.getCreatedAt()
                ))
                .toList();

        AuditChecks checks = auditChecks(authorization.getMission());
        boolean contextMatches = authorization.getContextFingerprint()
                .equals(fingerprint(buildContextSnapshot(authorization.getMission(), checks)));
        boolean active = authorization.isActiveAt(OffsetDateTime.now()) && contextMatches && checks.internalPassed();

        return new DroneAuthorizationResponse(
                authorization.getId(),
                authorization.getMission().getId(),
                authorization.getDecision(),
                authorization.getAuthorizedBy(),
                authorization.getAuthorizedAt(),
                authorization.getValidFrom(),
                authorization.getValidUntil(),
                authorization.getReason(),
                authorization.getPolicyVersion(),
                authorization.getSimulationMode(),
                authorization.getAirspaceCheck(),
                authorization.getWeatherCheck(),
                authorization.getGeofenceCheck(),
                authorization.getPayloadCheck(),
                authorization.getBatteryCheck(),
                authorization.getRouteCheck(),
                authorization.getContextSnapshot(),
                authorization.getContextFingerprint(),
                active,
                evidence
        );
    }

    private record DroneCandidate(
            Drone drone,
            double distanceKm,
            double roundTripKm,
            int requiredBatteryPercent
    ) {}

    private record AuditChecks(
            DroneAuditCheck payloadCheck,
            DroneAuditCheck batteryCheck,
            DroneAuditCheck routeCheck,
            double roundTripKm,
            int requiredBatteryPercent
    ) {
        boolean internalPassed() {
            return payloadCheck == DroneAuditCheck.PASSED
                    && batteryCheck == DroneAuditCheck.PASSED
                    && routeCheck == DroneAuditCheck.PASSED;
        }
    }
}
