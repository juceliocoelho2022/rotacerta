ALTER TABLE delivery_locations
    ADD COLUMN destination_label VARCHAR(180),
    ADD COLUMN region VARCHAR(80);

UPDATE delivery_locations SET destination_label = 'Rua das Flores, 123', region = 'Jardins' WHERE order_id = 1;
UPDATE delivery_locations SET destination_label = 'Av. Paulista, 1578', region = 'Bela Vista' WHERE order_id = 2;
UPDATE delivery_locations SET destination_label = 'Rua Oscar Freire, 620', region = 'Pinheiros' WHERE order_id = 3;
UPDATE delivery_locations SET destination_label = 'Rua Vergueiro, 950', region = 'Liberdade' WHERE order_id = 4;
UPDATE delivery_locations SET destination_label = 'Av. Santo Amaro, 1240', region = 'Moema' WHERE order_id = 5;

ALTER TABLE delivery_locations
    ALTER COLUMN destination_label SET NOT NULL,
    ALTER COLUMN region SET NOT NULL;
