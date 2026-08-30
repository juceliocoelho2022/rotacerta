CREATE TABLE drone_mission_authorizations (
    id BIGSERIAL PRIMARY KEY,
    mission_id BIGINT NOT NULL REFERENCES drone_missions(id),
    decision VARCHAR(40) NOT NULL,
    authorized_by VARCHAR(160) NOT NULL,
    authorized_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    valid_from TIMESTAMPTZ NOT NULL,
    valid_until TIMESTAMPTZ NOT NULL,
    reason VARCHAR(1000) NOT NULL,
    policy_version VARCHAR(60) NOT NULL,
    simulation_mode VARCHAR(40) NOT NULL,
    airspace_check VARCHAR(40) NOT NULL,
    weather_check VARCHAR(40) NOT NULL,
    geofence_check VARCHAR(40) NOT NULL,
    payload_check VARCHAR(40) NOT NULL,
    battery_check VARCHAR(40) NOT NULL,
    route_check VARCHAR(40) NOT NULL,
    context_snapshot TEXT NOT NULL,
    context_fingerprint VARCHAR(64) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_drone_authorization_decision CHECK (decision IN ('APPROVED_SIMULATION', 'REJECTED')),
    CONSTRAINT chk_drone_authorization_validity CHECK (valid_until > valid_from)
);

CREATE INDEX idx_drone_authorizations_mission
    ON drone_mission_authorizations(mission_id, authorized_at DESC);

CREATE INDEX idx_drone_authorizations_decision
    ON drone_mission_authorizations(decision, valid_until);

CREATE TABLE drone_authorization_evidence (
    id BIGSERIAL PRIMARY KEY,
    authorization_id BIGINT NOT NULL REFERENCES drone_mission_authorizations(id) ON DELETE CASCADE,
    evidence_type VARCHAR(80) NOT NULL,
    reference VARCHAR(300) NOT NULL,
    description VARCHAR(1000),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_drone_authorization_evidence_authorization
    ON drone_authorization_evidence(authorization_id);
