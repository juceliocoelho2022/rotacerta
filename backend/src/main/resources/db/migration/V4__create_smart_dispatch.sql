CREATE TABLE drivers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    available BOOLEAN NOT NULL DEFAULT TRUE,
    current_load INTEGER NOT NULL DEFAULT 0,
    max_capacity INTEGER NOT NULL DEFAULT 8,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_driver_load CHECK (current_load >= 0),
    CONSTRAINT chk_driver_capacity CHECK (max_capacity > 0)
);

CREATE TABLE delivery_locations (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE REFERENCES orders(id) ON DELETE CASCADE,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    priority INTEGER NOT NULL DEFAULT 3,
    sla_minutes INTEGER NOT NULL DEFAULT 120,
    CONSTRAINT chk_delivery_priority CHECK (priority BETWEEN 1 AND 5),
    CONSTRAINT chk_delivery_sla CHECK (sla_minutes > 0)
);

CREATE TABLE delivery_assignments (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE REFERENCES orders(id) ON DELETE CASCADE,
    driver_id BIGINT NOT NULL REFERENCES drivers(id),
    distance_km NUMERIC(10,2) NOT NULL,
    score NUMERIC(12,4) NOT NULL,
    eta_minutes INTEGER NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ASSIGNED',
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_drivers_available ON drivers(available);
CREATE INDEX idx_assignments_driver ON delivery_assignments(driver_id);
CREATE INDEX idx_assignments_status ON delivery_assignments(status);

INSERT INTO drivers (name, latitude, longitude, available, current_load, max_capacity) VALUES
('Carlos Mendes', -23.550520, -46.633308, TRUE, 1, 8),
('Ana Ribeiro', -23.565000, -46.650000, TRUE, 0, 8),
('Marcos Silva', -23.520000, -46.600000, TRUE, 3, 8);

INSERT INTO delivery_locations (order_id, latitude, longitude, priority, sla_minutes) VALUES
(1, -23.561684, -46.655981, 5, 90),
(2, -23.548943, -46.638818, 3, 120),
(3, -23.578093, -46.661560, 2, 180),
(4, -23.536769, -46.625079, 3, 120),
(5, -23.587416, -46.657634, 4, 100);
