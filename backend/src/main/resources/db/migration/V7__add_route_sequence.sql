ALTER TABLE delivery_assignments
    ADD COLUMN sequence_position INTEGER NOT NULL DEFAULT 999;

WITH ranked AS (
    SELECT id,
           ROW_NUMBER() OVER (PARTITION BY driver_id ORDER BY assigned_at, id) AS position
    FROM delivery_assignments
)
UPDATE delivery_assignments da
SET sequence_position = ranked.position
FROM ranked
WHERE da.id = ranked.id;

CREATE INDEX idx_assignments_route_sequence
    ON delivery_assignments(driver_id, status, sequence_position);
