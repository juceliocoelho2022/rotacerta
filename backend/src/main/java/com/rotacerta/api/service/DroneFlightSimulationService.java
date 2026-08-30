package com.rotacerta.api.service;

import com.rotacerta.api.dto.DroneFlightSimulationResponse;
import com.rotacerta.api.model.DroneMission;
import com.rotacerta.api.model.DroneMissionStatus;
import com.rotacerta.api.repository.DroneMissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.OffsetDateTime;

@Service
public class DroneFlightSimulationService {

    private static final String MODE = "SIMULATION_ONLY";
    private static final String POSITION_SOURCE = "SIMULATED_INTERPOLATION";

    private final DroneMissionRepository missionRepository;

    public DroneFlightSimulationService(DroneMissionRepository missionRepository) {
        this.missionRepository = missionRepository;
    }

    @Transactional(readOnly = true)
    public DroneFlightSimulationResponse getSimulation(Long missionId) {
        DroneMission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new IllegalArgumentException("Missão de drone não encontrada. ID: " + missionId));

        OffsetDateTime now = OffsetDateTime.now();
        DroneMissionStatus status = mission.getStatus();
        FlightState state = calculateState(mission, status, now);

        return new DroneFlightSimulationResponse(
                mission.getId(),
                mission.getOrder().getId(),
                mission.getOrder().getOrderNumber(),
                mission.getDrone().getCode(),
                status,
                MODE,
                state.phase(),
                decimal(state.progressPercent(), 1),
                decimal(state.legProgress() * 100.0, 1),
                state.latitude(),
                state.longitude(),
                mission.getOriginLatitude(),
                mission.getOriginLongitude(),
                mission.getDestinationLatitude(),
                mission.getDestinationLongitude(),
                mission.getDistanceKm(),
                state.remainingDistanceKm() == null ? null : decimal(state.remainingDistanceKm(), 2),
                mission.getEtaMinutes(),
                state.remainingEtaMinutes(),
                state.moving(),
                state.positionSource(),
                now
        );
    }

    private FlightState calculateState(DroneMission mission, DroneMissionStatus status, OffsetDateTime now) {
        double totalDistance = mission.getDistanceKm().doubleValue();
        int etaMinutes = Math.max(1, mission.getEtaMinutes());

        return switch (status) {
            case PLANNED -> atOrigin(mission, "PRE_FLIGHT", 2.0, totalDistance, etaMinutes, false);
            case AUTHORIZED -> atOrigin(mission, "PRE_FLIGHT", 8.0, totalDistance, etaMinutes, false);
            case LOADING -> atOrigin(mission, "PRE_FLIGHT", 14.0, totalDistance, etaMinutes, false);
            case READY_FOR_TAKEOFF -> atOrigin(mission, "PRE_FLIGHT", 20.0, totalDistance, etaMinutes, false);
            case IN_FLIGHT -> outbound(mission, now, 0.0, 0.75, 20.0, 45.0, "OUTBOUND", true);
            case APPROACHING -> fixedOutbound(mission, 0.85, 72.0, "OUTBOUND", true);
            case LOWERING_PACKAGE -> fixedOutbound(mission, 0.97, 82.0, "DELIVERY", false);
            case DELIVERED -> atDestination(mission, "DELIVERY", 88.0, false);
            case RETURNING -> returning(mission, now);
            case COMPLETED -> atOrigin(mission, "COMPLETED", 100.0, 0.0, 0, false);
            case ABORTED -> new FlightState(
                    "ABORTED",
                    0.0,
                    0.0,
                    null,
                    null,
                    null,
                    0,
                    false,
                    "UNAVAILABLE_AFTER_ABORT"
            );
        };
    }

    private FlightState outbound(
            DroneMission mission,
            OffsetDateTime now,
            double minLegProgress,
            double maxLegProgress,
            double baseProgress,
            double progressSpan,
            String phase,
            boolean moving
    ) {
        double elapsedFraction = elapsedFraction(mission, now);
        double legProgress = clamp(minLegProgress + elapsedFraction * (maxLegProgress - minLegProgress), minLegProgress, maxLegProgress);
        double missionProgress = baseProgress + (legProgress / maxLegProgress) * progressSpan;
        return interpolateOutbound(mission, legProgress, missionProgress, phase, moving);
    }

    private FlightState fixedOutbound(DroneMission mission, double legProgress, double missionProgress, String phase, boolean moving) {
        return interpolateOutbound(mission, legProgress, missionProgress, phase, moving);
    }

    private FlightState interpolateOutbound(DroneMission mission, double legProgress, double missionProgress, String phase, boolean moving) {
        double latitude = interpolate(mission.getOriginLatitude(), mission.getDestinationLatitude(), legProgress);
        double longitude = interpolate(mission.getOriginLongitude(), mission.getDestinationLongitude(), legProgress);
        double remainingDistance = mission.getDistanceKm().doubleValue() * (1.0 - legProgress);
        int remainingEta = remainingEta(mission.getEtaMinutes(), legProgress);

        return new FlightState(
                phase,
                clamp(missionProgress, 0.0, 100.0),
                legProgress,
                latitude,
                longitude,
                remainingDistance,
                remainingEta,
                moving,
                POSITION_SOURCE
        );
    }

    private FlightState returning(DroneMission mission, OffsetDateTime now) {
        double returnProgress = clamp(elapsedFraction(mission, now), 0.0, 1.0);
        double latitude = interpolate(mission.getDestinationLatitude(), mission.getOriginLatitude(), returnProgress);
        double longitude = interpolate(mission.getDestinationLongitude(), mission.getOriginLongitude(), returnProgress);
        double remainingDistance = mission.getDistanceKm().doubleValue() * (1.0 - returnProgress);
        int remainingEta = remainingEta(mission.getEtaMinutes(), returnProgress);
        double missionProgress = 88.0 + returnProgress * 11.0;

        return new FlightState(
                "RETURN",
                clamp(missionProgress, 88.0, 99.0),
                returnProgress,
                latitude,
                longitude,
                remainingDistance,
                remainingEta,
                true,
                POSITION_SOURCE
        );
    }

    private FlightState atOrigin(
            DroneMission mission,
            String phase,
            double progress,
            double remainingDistance,
            int remainingEta,
            boolean moving
    ) {
        return new FlightState(
                phase,
                progress,
                0.0,
                mission.getOriginLatitude(),
                mission.getOriginLongitude(),
                remainingDistance,
                remainingEta,
                moving,
                "MISSION_ORIGIN"
        );
    }

    private FlightState atDestination(DroneMission mission, String phase, double progress, boolean moving) {
        return new FlightState(
                phase,
                progress,
                1.0,
                mission.getDestinationLatitude(),
                mission.getDestinationLongitude(),
                0.0,
                0,
                moving,
                "MISSION_DESTINATION"
        );
    }

    private double elapsedFraction(DroneMission mission, OffsetDateTime now) {
        long elapsedSeconds = Math.max(0, Duration.between(mission.getUpdatedAt(), now).toSeconds());
        long etaSeconds = Math.max(60, mission.getEtaMinutes() * 60L);
        return clamp((double) elapsedSeconds / etaSeconds, 0.0, 1.0);
    }

    private int remainingEta(int etaMinutes, double legProgress) {
        if (legProgress >= 1.0) return 0;
        return Math.max(1, (int) Math.ceil(Math.max(1, etaMinutes) * (1.0 - legProgress)));
    }

    private double interpolate(double start, double end, double progress) {
        return start + (end - start) * progress;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private BigDecimal decimal(double value, int scale) {
        return BigDecimal.valueOf(value).setScale(scale, RoundingMode.HALF_UP);
    }

    private record FlightState(
            String phase,
            double progressPercent,
            double legProgress,
            Double latitude,
            Double longitude,
            Double remainingDistanceKm,
            int remainingEtaMinutes,
            boolean moving,
            String positionSource
    ) {
    }
}
