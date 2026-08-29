ALTER TABLE drivers
    ADD COLUMN vehicle_plate VARCHAR(20),
    ADD COLUMN vehicle_model VARCHAR(80);

UPDATE drivers SET vehicle_plate = 'ABC1D23', vehicle_model = 'Fiat Fiorino' WHERE id = 1;
UPDATE drivers SET vehicle_plate = 'DEF4G56', vehicle_model = 'Renault Kangoo' WHERE id = 2;
UPDATE drivers SET vehicle_plate = 'GHI7J89', vehicle_model = 'VW Delivery' WHERE id = 3;

ALTER TABLE drivers
    ALTER COLUMN vehicle_plate SET NOT NULL,
    ALTER COLUMN vehicle_model SET NOT NULL;
