package com.rotacerta.api.model;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "drone_mission_authorizations")
public class DroneMissionAuthorization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id", nullable = false)
    private DroneMission mission;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private DroneAuthorizationDecision decision;

    @Column(name = "authorized_by", nullable = false, length = 160)
    private String authorizedBy;

    @Column(name = "authorized_at", nullable = false)
    private OffsetDateTime authorizedAt;

    @Column(name = "valid_from", nullable = false)
    private OffsetDateTime validFrom;

    @Column(name = "valid_until", nullable = false)
    private OffsetDateTime validUntil;

    @Column(nullable = false, length = 1000)
    private String reason;

    @Column(name = "policy_version", nullable = false, length = 60)
    private String policyVersion;

    @Column(name = "simulation_mode", nullable = false, length = 40)
    private String simulationMode;

    @Enumerated(EnumType.STRING)
    @Column(name = "airspace_check", nullable = false, length = 40)
    private DroneAuditCheck airspaceCheck;

    @Enumerated(EnumType.STRING)
    @Column(name = "weather_check", nullable = false, length = 40)
    private DroneAuditCheck weatherCheck;

    @Enumerated(EnumType.STRING)
    @Column(name = "geofence_check", nullable = false, length = 40)
    private DroneAuditCheck geofenceCheck;

    @Enumerated(EnumType.STRING)
    @Column(name = "payload_check", nullable = false, length = 40)
    private DroneAuditCheck payloadCheck;

    @Enumerated(EnumType.STRING)
    @Column(name = "battery_check", nullable = false, length = 40)
    private DroneAuditCheck batteryCheck;

    @Enumerated(EnumType.STRING)
    @Column(name = "route_check", nullable = false, length = 40)
    private DroneAuditCheck routeCheck;

    @Column(name = "context_snapshot", nullable = false, columnDefinition = "TEXT")
    private String contextSnapshot;

    @Column(name = "context_fingerprint", nullable = false, length = 64)
    private String contextFingerprint;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected DroneMissionAuthorization() {}

    public DroneMissionAuthorization(
            DroneMission mission,
            DroneAuthorizationDecision decision,
            String authorizedBy,
            OffsetDateTime authorizedAt,
            OffsetDateTime validFrom,
            OffsetDateTime validUntil,
            String reason,
            String policyVersion,
            String simulationMode,
            DroneAuditCheck airspaceCheck,
            DroneAuditCheck weatherCheck,
            DroneAuditCheck geofenceCheck,
            DroneAuditCheck payloadCheck,
            DroneAuditCheck batteryCheck,
            DroneAuditCheck routeCheck,
            String contextSnapshot,
            String contextFingerprint
    ) {
        this.mission = mission;
        this.decision = decision;
        this.authorizedBy = authorizedBy;
        this.authorizedAt = authorizedAt;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
        this.reason = reason;
        this.policyVersion = policyVersion;
        this.simulationMode = simulationMode;
        this.airspaceCheck = airspaceCheck;
        this.weatherCheck = weatherCheck;
        this.geofenceCheck = geofenceCheck;
        this.payloadCheck = payloadCheck;
        this.batteryCheck = batteryCheck;
        this.routeCheck = routeCheck;
        this.contextSnapshot = contextSnapshot;
        this.contextFingerprint = contextFingerprint;
        this.createdAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public DroneMission getMission() { return mission; }
    public DroneAuthorizationDecision getDecision() { return decision; }
    public String getAuthorizedBy() { return authorizedBy; }
    public OffsetDateTime getAuthorizedAt() { return authorizedAt; }
    public OffsetDateTime getValidFrom() { return validFrom; }
    public OffsetDateTime getValidUntil() { return validUntil; }
    public String getReason() { return reason; }
    public String getPolicyVersion() { return policyVersion; }
    public String getSimulationMode() { return simulationMode; }
    public DroneAuditCheck getAirspaceCheck() { return airspaceCheck; }
    public DroneAuditCheck getWeatherCheck() { return weatherCheck; }
    public DroneAuditCheck getGeofenceCheck() { return geofenceCheck; }
    public DroneAuditCheck getPayloadCheck() { return payloadCheck; }
    public DroneAuditCheck getBatteryCheck() { return batteryCheck; }
    public DroneAuditCheck getRouteCheck() { return routeCheck; }
    public String getContextSnapshot() { return contextSnapshot; }
    public String getContextFingerprint() { return contextFingerprint; }
    public OffsetDateTime getCreatedAt() { return createdAt; }

    public boolean isActiveAt(OffsetDateTime instant) {
        return decision == DroneAuthorizationDecision.APPROVED_SIMULATION
                && !instant.isBefore(validFrom)
                && instant.isBefore(validUntil);
    }
}
