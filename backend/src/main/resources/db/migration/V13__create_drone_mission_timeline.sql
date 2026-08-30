CREATE TABLE drone_mission_events (
    id BIGSERIAL PRIMARY KEY,
    mission_id BIGINT NOT NULL REFERENCES drone_missions(id) ON DELETE CASCADE,
    event_type VARCHAR(50) NOT NULL,
    mission_status VARCHAR(30),
    title VARCHAR(160) NOT NULL,
    description VARCHAR(1000),
    actor VARCHAR(160),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_drone_mission_events_mission_time
    ON drone_mission_events(mission_id, created_at, id);

CREATE INDEX idx_drone_mission_events_type
    ON drone_mission_events(event_type, created_at DESC);

-- Backfill only facts that can be reconstructed safely for existing missions.
INSERT INTO drone_mission_events (
    mission_id, event_type, mission_status, title, description, actor, created_at
)
SELECT
    m.id,
    'MISSION_CREATED',
    'PLANNED',
    'Missão criada',
    'Registro histórico criado a partir da data original da missão.',
    'Sistema RotaCerta',
    m.created_at
FROM drone_missions m;

INSERT INTO drone_mission_events (
    mission_id, event_type, mission_status, title, description, actor, created_at
)
SELECT
    a.mission_id,
    CASE WHEN a.decision = 'APPROVED_SIMULATION' THEN 'AUTHORIZATION_APPROVED' ELSE 'AUTHORIZATION_REJECTED' END,
    CASE WHEN a.decision = 'APPROVED_SIMULATION' THEN 'AUTHORIZED' ELSE 'PLANNED' END,
    CASE WHEN a.decision = 'APPROVED_SIMULATION' THEN 'Autorização simulada aprovada' ELSE 'Autorização rejeitada' END,
    a.reason,
    a.authorized_by,
    a.authorized_at
FROM drone_mission_authorizations a;

INSERT INTO drone_mission_events (
    mission_id, event_type, mission_status, title, description, actor, created_at
)
SELECT
    m.id,
    'STATUS_SNAPSHOT',
    m.status,
    'Estado histórico importado',
    'Snapshot do estado final/atual existente antes da ativação da timeline detalhada.',
    'Sistema RotaCerta',
    m.updated_at
FROM drone_missions m
WHERE m.status <> 'PLANNED';
