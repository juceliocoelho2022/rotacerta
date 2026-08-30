CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    sku VARCHAR(80) NOT NULL UNIQUE,
    name VARCHAR(180) NOT NULL,
    description VARCHAR(500),
    unit_price NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (unit_price >= 0),
    weight_kg NUMERIC(10,3) NOT NULL DEFAULT 0 CHECK (weight_kg >= 0),
    volume_m3 NUMERIC(10,4) NOT NULL DEFAULT 0 CHECK (volume_m3 >= 0),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE inventory (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL UNIQUE REFERENCES products(id) ON DELETE CASCADE,
    total_quantity INTEGER NOT NULL DEFAULT 0 CHECK (total_quantity >= 0),
    reserved_quantity INTEGER NOT NULL DEFAULT 0 CHECK (reserved_quantity >= 0),
    minimum_quantity INTEGER NOT NULL DEFAULT 0 CHECK (minimum_quantity >= 0),
    warehouse_location VARCHAR(120),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_inventory_reserved_not_above_total CHECK (reserved_quantity <= total_quantity)
);

CREATE TABLE inventory_reservations (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    released_at TIMESTAMPTZ,
    CONSTRAINT uq_inventory_reservation_order_product UNIQUE (order_id, product_id)
);

CREATE TABLE inventory_movements (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
    order_id BIGINT REFERENCES orders(id) ON DELETE SET NULL,
    movement_type VARCHAR(40) NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    previous_total INTEGER NOT NULL,
    new_total INTEGER NOT NULL,
    previous_reserved INTEGER NOT NULL,
    new_reserved INTEGER NOT NULL,
    reason VARCHAR(300),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_products_name ON products(name);
CREATE INDEX idx_inventory_low_stock ON inventory(total_quantity, reserved_quantity, minimum_quantity);
CREATE INDEX idx_inventory_reservations_order ON inventory_reservations(order_id);
CREATE INDEX idx_inventory_reservations_status ON inventory_reservations(status);
CREATE INDEX idx_inventory_movements_product ON inventory_movements(product_id, created_at DESC);
CREATE INDEX idx_inventory_movements_order ON inventory_movements(order_id, created_at DESC);

INSERT INTO products (sku, name, description, unit_price, weight_kg, volume_m3)
VALUES
    ('NOTEBOOK-DELL-001', 'Notebook Dell', 'Notebook para demonstração do estoque', 4299.90, 1.800, 0.0080),
    ('MOUSE-LOGI-001', 'Mouse Logitech', 'Mouse sem fio para demonstração', 149.90, 0.120, 0.0008),
    ('MONITOR-LG-001', 'Monitor LG 24', 'Monitor para demonstração do estoque', 899.90, 3.200, 0.0200),
    ('HEADSET-JBL-001', 'Headset JBL', 'Headset para demonstração do estoque', 329.90, 0.350, 0.0020);

INSERT INTO inventory (product_id, total_quantity, reserved_quantity, minimum_quantity, warehouse_location)
SELECT id,
       CASE sku
           WHEN 'NOTEBOOK-DELL-001' THEN 20
           WHEN 'MOUSE-LOGI-001' THEN 10
           WHEN 'MONITOR-LG-001' THEN 4
           ELSE 18
       END,
       0,
       CASE sku
           WHEN 'NOTEBOOK-DELL-001' THEN 5
           WHEN 'MOUSE-LOGI-001' THEN 5
           WHEN 'MONITOR-LG-001' THEN 2
           ELSE 4
       END,
       CASE sku
           WHEN 'NOTEBOOK-DELL-001' THEN 'A-01-01'
           WHEN 'MOUSE-LOGI-001' THEN 'A-02-03'
           WHEN 'MONITOR-LG-001' THEN 'B-01-02'
           ELSE 'B-03-04'
       END
FROM products
WHERE sku IN ('NOTEBOOK-DELL-001', 'MOUSE-LOGI-001', 'MONITOR-LG-001', 'HEADSET-JBL-001');
