ALTER TABLE customers
    ADD COLUMN phone VARCHAR(30),
    ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    ADD COLUMN rating NUMERIC(2,1) NOT NULL DEFAULT 5.0;

CREATE TABLE customer_addresses (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    label VARCHAR(40) NOT NULL,
    street VARCHAR(160) NOT NULL,
    number VARCHAR(30) NOT NULL,
    complement VARCHAR(120),
    district VARCHAR(100),
    city VARCHAR(100) NOT NULL,
    state VARCHAR(2) NOT NULL,
    zip_code VARCHAR(12),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    primary_address BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX ux_customer_primary_address
    ON customer_addresses(customer_id)
    WHERE primary_address = TRUE;

CREATE INDEX idx_customer_addresses_customer_id
    ON customer_addresses(customer_id);

CREATE TABLE authorized_recipients (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    name VARCHAR(120) NOT NULL,
    relationship VARCHAR(60) NOT NULL,
    phone VARCHAR(30),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_authorized_recipients_customer_id
    ON authorized_recipients(customer_id);

CREATE TABLE delivery_preferences (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL UNIQUE REFERENCES customers(id) ON DELETE CASCADE,
    notifications_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    notification_channel VARCHAR(30) NOT NULL DEFAULT 'EMAIL',
    preferred_start_time TIME,
    preferred_end_time TIME,
    delivery_instructions VARCHAR(300),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_delivery_time_window CHECK (
        preferred_start_time IS NULL
        OR preferred_end_time IS NULL
        OR preferred_start_time < preferred_end_time
    )
);

UPDATE customers SET phone = '(11) 98888-1001', rating = 4.8 WHERE id = 1;
UPDATE customers SET phone = '(11) 98888-1002', rating = 4.9 WHERE id = 2;
UPDATE customers SET phone = '(21) 98888-1003', rating = 4.7 WHERE id = 3;
UPDATE customers SET phone = '(11) 98888-1004', rating = 4.6 WHERE id = 4;
UPDATE customers SET phone = '(31) 98888-1005', rating = 4.8 WHERE id = 5;

INSERT INTO customer_addresses (
    customer_id, label, street, number, district, city, state,
    zip_code, latitude, longitude, primary_address
) VALUES
(1, 'Casa', 'Rua das Flores', '123', 'Jardins', 'São Paulo', 'SP', '01400-000', -23.5668, -46.6558, TRUE),
(1, 'Trabalho', 'Av. Paulista', '1500', 'Bela Vista', 'São Paulo', 'SP', '01310-200', -23.5614, -46.6559, FALSE),
(2, 'Casa', 'Rua Harmonia', '245', 'Vila Madalena', 'São Paulo', 'SP', '05435-001', -23.5530, -46.6914, TRUE),
(3, 'Casa', 'Rua Voluntários da Pátria', '820', 'Botafogo', 'Rio de Janeiro', 'RJ', '22270-010', -22.9524, -43.1866, TRUE),
(4, 'Casa', 'Rua Augusta', '980', 'Consolação', 'São Paulo', 'SP', '01305-100', -23.5534, -46.6571, TRUE),
(5, 'Casa', 'Av. do Contorno', '4200', 'Funcionários', 'Belo Horizonte', 'MG', '30110-028', -19.9322, -43.9367, TRUE);

INSERT INTO authorized_recipients (customer_id, name, relationship, phone) VALUES
(1, 'Ana Souza', 'Filha', '(11) 97777-1001'),
(1, 'Paulo Souza', 'Cônjuge', '(11) 97777-1002'),
(2, 'Renata Lima', 'Irmã', '(11) 97777-2001');

INSERT INTO delivery_preferences (
    customer_id, notifications_enabled, notification_channel,
    preferred_start_time, preferred_end_time, delivery_instructions
) VALUES
(1, TRUE, 'WHATSAPP', '14:00', '18:00', 'Interfone 32. Se não houver resposta, usar recebedor autorizado.'),
(2, TRUE, 'SMS', '09:00', '13:00', 'Entregar na portaria.'),
(3, TRUE, 'EMAIL', NULL, NULL, 'Ligar ao chegar.');
