ALTER TABLE orders
    ADD COLUMN priority VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    ADD COLUMN delivery_type VARCHAR(20) NOT NULL DEFAULT 'STANDARD';

CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_name VARCHAR(180) NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_price NUMERIC(12,2) NOT NULL CHECK (unit_price >= 0),
    weight_kg NUMERIC(10,3) NOT NULL DEFAULT 0 CHECK (weight_kg >= 0),
    volume_m3 NUMERIC(10,4) NOT NULL DEFAULT 0 CHECK (volume_m3 >= 0)
);

CREATE TABLE order_delivery_details (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE REFERENCES orders(id) ON DELETE CASCADE,
    customer_address_id BIGINT REFERENCES customer_addresses(id) ON DELETE SET NULL,
    address_label VARCHAR(40) NOT NULL,
    street VARCHAR(160) NOT NULL,
    number VARCHAR(30) NOT NULL,
    complement VARCHAR(120),
    district VARCHAR(100),
    city VARCHAR(100) NOT NULL,
    state VARCHAR(2) NOT NULL,
    zip_code VARCHAR(12),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    delivery_date DATE NOT NULL,
    window_start TIME,
    window_end TIME,
    instructions VARCHAR(300),
    CONSTRAINT chk_order_delivery_window CHECK (
        (window_start IS NULL AND window_end IS NULL)
        OR (window_start IS NOT NULL AND window_end IS NOT NULL AND window_end > window_start)
    )
);

CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_orders_priority ON orders(priority);
CREATE INDEX idx_orders_delivery_type ON orders(delivery_type);
CREATE INDEX idx_order_delivery_date ON order_delivery_details(delivery_date);

INSERT INTO order_delivery_details (
    order_id,
    customer_address_id,
    address_label,
    street,
    number,
    complement,
    district,
    city,
    state,
    zip_code,
    latitude,
    longitude,
    delivery_date,
    window_start,
    window_end,
    instructions
)
SELECT
    o.id,
    a.id,
    COALESCE(a.label, 'Legado'),
    COALESCE(a.street, 'Endereço não informado'),
    COALESCE(a.number, 'S/N'),
    a.complement,
    a.district,
    COALESCE(a.city, 'Não informado'),
    COALESCE(a.state, 'SP'),
    a.zip_code,
    a.latitude,
    a.longitude,
    CAST(o.created_at AS DATE),
    p.preferred_start_time,
    p.preferred_end_time,
    p.delivery_instructions
FROM orders o
LEFT JOIN LATERAL (
    SELECT ca.*
    FROM customer_addresses ca
    WHERE ca.customer_id = o.customer_id
    ORDER BY ca.primary_address DESC, ca.created_at ASC
    LIMIT 1
) a ON TRUE
LEFT JOIN delivery_preferences p ON p.customer_id = o.customer_id
WHERE NOT EXISTS (
    SELECT 1 FROM order_delivery_details odd WHERE odd.order_id = o.id
);
