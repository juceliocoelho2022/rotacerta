package com.rotacerta.api.dto;

import com.rotacerta.api.model.DroneAuditCheck;
import com.rotacerta.api.model.DroneAuthorizationDecision;

import java.time.OffsetDateTime;
import java.util.List;

public record DroneAuthorizationResponse(
        Long id,
        Long missionId,
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
        String contextFingerprint,
        boolean active,
        List<DroneAuthorizationEvidenceResponse> evidence
) {}
