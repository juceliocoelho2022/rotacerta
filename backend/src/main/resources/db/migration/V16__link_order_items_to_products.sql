ALTER TABLE order_items
    ADD COLUMN product_id BIGINT REFERENCES products(id) ON DELETE RESTRICT,
    ADD COLUMN sku VARCHAR(80);

UPDATE order_items oi
SET product_id = p.id,
    sku = p.sku
FROM products p
WHERE LOWER(TRIM(oi.product_name)) = LOWER(TRIM(p.name));

CREATE INDEX idx_order_items_product_id ON order_items(product_id);
CREATE INDEX idx_order_items_sku ON order_items(sku);

COMMENT ON COLUMN order_items.product_id IS 'Produto de catálogo vinculado ao item. Pode ser nulo apenas para itens legados.';
COMMENT ON COLUMN order_items.sku IS 'Snapshot do SKU usado no pedido para rastreabilidade histórica.';
