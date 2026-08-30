CREATE TABLE vehicles (
    id BIGSERIAL PRIMARY KEY,
    plate VARCHAR(20) NOT NULL UNIQUE,
    model VARCHAR(100) NOT NULL,
    vehicle_type VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    max_capacity INTEGER NOT NULL CHECK (max_capacity > 0),
    current_odometer_km NUMERIC(12,1) NOT NULL DEFAULT 0,
    fuel_type VARCHAR(30) NOT NULL,
    next_maintenance_km NUMERIC(12,1),
    driver_id BIGINT REFERENCES drivers(id) ON DELETE SET NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_vehicles_status ON vehicles(status);
CREATE INDEX idx_vehicles_driver ON vehicles(driver_id);

INSERT INTO vehicles (plate, model, vehicle_type, status, max_capacity, current_odometer_km, fuel_type, next_maintenance_km, driver_id)
SELECT
    d.vehicle_plate,
    d.vehicle_model,
    CASE
        WHEN LOWER(d.vehicle_model) LIKE '%delivery%' THEN 'TRUCK'
        WHEN LOWER(d.vehicle_model) LIKE '%kangoo%' OR LOWER(d.vehicle_model) LIKE '%fiorino%' THEN 'VAN'
        ELSE 'UTILITY'
    END,
    CASE WHEN d.available THEN 'AVAILABLE' ELSE 'IN_OPERATION' END,
    d.max_capacity,
    0,
    'FLEX',
    10000,
    d.id
FROM drivers d
ON CONFLICT (plate) DO NOTHING;

CREATE TABLE incidents (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT REFERENCES orders(id) ON DELETE SET NULL,
    driver_id BIGINT REFERENCES drivers(id) ON DELETE SET NULL,
    vehicle_id BIGINT REFERENCES vehicles(id) ON DELETE SET NULL,
    severity VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    category VARCHAR(40) NOT NULL,
    title VARCHAR(140) NOT NULL,
    description TEXT NOT NULL,
    location VARCHAR(180),
    resolution TEXT,
    opened_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    resolved_at TIMESTAMPTZ
);

CREATE INDEX idx_incidents_status ON incidents(status);
CREATE INDEX idx_incidents_severity ON incidents(severity);
CREATE INDEX idx_incidents_order ON incidents(order_id);

INSERT INTO incidents (order_id, driver_id, vehicle_id, severity, status, category, title, description, location)
VALUES
    (NULL, NULL, NULL, 'MEDIUM', 'OPEN', 'OPERATIONAL', 'Checklist operacional pendente', 'Ocorrência demonstrativa para validação do novo centro de ocorrências.', 'Centro Operacional RotaCerta'),
    (NULL, NULL, NULL, 'LOW', 'RESOLVED', 'CUSTOMER', 'Contato de entrega atualizado', 'Registro demonstrativo encerrado após atualização das instruções de entrega.', 'São Paulo/SP');

UPDATE incidents
SET resolved_at = NOW(), resolution = 'Instruções revisadas e ocorrência encerrada.'
WHERE status = 'RESOLVED';

CREATE TABLE system_settings (
    id BIGSERIAL PRIMARY KEY,
    setting_key VARCHAR(100) NOT NULL UNIQUE,
    category VARCHAR(60) NOT NULL,
    label VARCHAR(120) NOT NULL,
    setting_value VARCHAR(500) NOT NULL,
    value_type VARCHAR(20) NOT NULL,
    description VARCHAR(300),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

INSERT INTO system_settings (setting_key, category, label, setting_value, value_type, description) VALUES
    ('operations.auto_dispatch', 'Operações', 'Despacho automático', 'true', 'BOOLEAN', 'Permite executar auto-planejamento para pedidos elegíveis.'),
    ('operations.default_sla_minutes', 'Operações', 'SLA padrão (min)', '120', 'INTEGER', 'SLA usado quando não existe janela específica no pedido.'),
    ('tracking.public_live_enabled', 'Rastreamento', 'RotaCerta Live habilitado', 'true', 'BOOLEAN', 'Habilita a geração de acompanhamento público temporário.'),
    ('drones.simulation_enabled', 'Drones', 'Simulação de drones', 'true', 'BOOLEAN', 'Mantém o módulo aéreo disponível somente em SIMULATION_ONLY.'),
    ('notifications.default_channel', 'Notificações', 'Canal padrão', 'EMAIL', 'STRING', 'Canal preferencial de notificação para novos cadastros.'),
    ('maintenance.warning_km', 'Frota', 'Alerta de manutenção (km)', '1000', 'INTEGER', 'Antecedência para destacar manutenção programada.'),
    ('reports.currency', 'Relatórios', 'Moeda de referência', 'BRL', 'STRING', 'Moeda usada nos indicadores financeiros do painel.')
ON CONFLICT (setting_key) DO NOTHING;
