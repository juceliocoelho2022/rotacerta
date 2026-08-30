package com.rotacerta.api.service;

import com.rotacerta.api.dto.DroneAuthorizationResponse;
import com.rotacerta.api.dto.DroneMissionEventResponse;
import com.rotacerta.api.dto.DroneMissionResponse;
import com.rotacerta.api.model.DroneAuthorizationDecision;
import com.rotacerta.api.model.DroneMission;
import com.rotacerta.api.model.DroneMissionEvent;
import com.rotacerta.api.model.DroneMissionStatus;
import com.rotacerta.api.repository.DroneMissionEventRepository;
import com.rotacerta.api.repository.DroneMissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class DroneMissionTimelineService {

    private static final String SYSTEM_ACTOR = "Sistema RotaCerta";

    private final DroneMissionRepository missionRepository;
    private final DroneMissionEventRepository eventRepository;

    public DroneMissionTimelineService(
            DroneMissionRepository missionRepository,
            DroneMissionEventRepository eventRepository
    ) {
        this.missionRepository = missionRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional(readOnly = true)
    public List<DroneMissionEventResponse> findTimeline(Long missionId) {
        getMission(missionId);
        return eventRepository.findByMissionIdOrderByCreatedAtAscIdAsc(missionId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void recordMissionCreated(DroneMissionResponse response) {
        DroneMission mission = getMission(response.id());
        save(
                mission,
                "MISSION_CREATED",
                DroneMissionStatus.PLANNED,
                "Missão criada",
                "Pedido " + response.orderNumber() + " reservado para o drone " + response.droneCode() + ".",
                SYSTEM_ACTOR,
                response.createdAt()
        );
    }

    @Transactional
    public void recordAuthorization(DroneAuthorizationResponse response) {
        DroneMission mission = getMission(response.missionId());
        boolean approved = response.decision() == DroneAuthorizationDecision.APPROVED_SIMULATION;
        save(
                mission,
                approved ? "AUTHORIZATION_APPROVED" : "AUTHORIZATION_REJECTED",
                approved ? DroneMissionStatus.AUTHORIZED : DroneMissionStatus.PLANNED,
                approved ? "Autorização simulada aprovada" : "Autorização rejeitada",
                response.reason(),
                response.authorizedBy(),
                response.authorizedAt()
        );
    }

    @Transactional
    public void recordStatusChanged(DroneMissionResponse response) {
        DroneMission mission = getMission(response.id());
        DroneMissionStatus status = response.status();
        save(
                mission,
                status == DroneMissionStatus.ABORTED ? "MISSION_ABORTED" : "STATUS_CHANGED",
                status,
                statusTitle(status),
                "Pedido " + response.orderNumber() + " • Drone " + response.droneCode() + ".",
                SYSTEM_ACTOR,
                response.updatedAt()
        );
    }

    private void save(
            DroneMission mission,
            String eventType,
            DroneMissionStatus status,
            String title,
            String description,
            String actor,
            OffsetDateTime createdAt
    ) {
        eventRepository.save(new DroneMissionEvent(
                mission,
                eventType,
                status,
                title,
                description,
                actor,
                createdAt
        ));
    }

    private DroneMission getMission(Long missionId) {
        if (missionId == null) {
            throw new IllegalArgumentException("O ID da missão é obrigatório.");
        }
        return missionRepository.findById(missionId)
                .orElseThrow(() -> new IllegalArgumentException("Missão de drone não encontrada. ID: " + missionId));
    }

    private String statusTitle(DroneMissionStatus status) {
        return switch (status) {
            case PLANNED -> "Missão planejada";
            case AUTHORIZED -> "Missão autorizada";
            case LOADING -> "Carregamento iniciado";
            case READY_FOR_TAKEOFF -> "Pronta para decolagem";
            case IN_FLIGHT -> "Drone em voo";
            case APPROACHING -> "Aproximação do destino";
            case LOWERING_PACKAGE -> "Descida do pacote";
            case DELIVERED -> "Entrega confirmada";
            case RETURNING -> "Retorno à base";
            case COMPLETED -> "Missão concluída";
            case ABORTED -> "Missão abortada";
        };
    }

    private DroneMissionEventResponse toResponse(DroneMissionEvent event) {
        return new DroneMissionEventResponse(
                event.getId(),
                event.getEventType(),
                event.getMissionStatus(),
                event.getTitle(),
                event.getDescription(),
                event.getActor(),
                event.getCreatedAt()
        );
    }
}
