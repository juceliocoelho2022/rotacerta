CREATE TABLE drones (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(40) NOT NULL UNIQUE,
    model VARCHAR(100) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE',
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    battery_percent INTEGER NOT NULL DEFAULT 100 CHECK (battery_percent BETWEEN 0 AND 100),
    max_payload_kg NUMERIC(8,3) NOT NULL CHECK (max_payload_kg > 0),
    max_range_km NUMERIC(8,2) NOT NULL CHECK (max_range_km > 0),
    available BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE drone_missions (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE REFERENCES orders(id),
    drone_id BIGINT NOT NULL REFERENCES drones(id),
    status VARCHAR(30) NOT NULL,
    payload_kg NUMERIC(8,3) NOT NULL,
    distance_km NUMERIC(8,2) NOT NULL,
    eta_minutes INTEGER NOT NULL,
    origin_latitude DOUBLE PRECISION NOT NULL,
    origin_longitude DOUBLE PRECISION NOT NULL,
    destination_latitude DOUBLE PRECISION NOT NULL,
    destination_longitude DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_drone_missions_drone ON drone_missions(drone_id);
CREATE INDEX idx_drone_missions_status ON drone_missions(status);

INSERT INTO drones (code, model, status, latitude, longitude, battery_percent, max_payload_kg, max_range_km, available)
VALUES
('DR-001', 'RotaCerta Air One', 'AVAILABLE', -23.550520, -46.633308, 96, 2.500, 20.00, TRUE),
('DR-002', 'RotaCerta Air Cargo', 'AVAILABLE', -23.550520, -46.633308, 88, 5.000, 16.00, TRUE),
('DR-003', 'RotaCerta Air Mini', 'CHARGING', -23.550520, -46.633308, 42, 1.500, 12.00, FALSE);
