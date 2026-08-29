CREATE TABLE delivery_tracking_sessions (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE REFERENCES orders(id) ON DELETE CASCADE,
    public_token VARCHAR(128) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    alternate_recipient_name VARCHAR(120),
    alternate_recipient_relationship VARCHAR(60),
    delivery_instructions VARCHAR(500),
    recipient_updated_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_delivery_tracking_sessions_token
    ON delivery_tracking_sessions(public_token);

CREATE INDEX idx_delivery_tracking_sessions_active
    ON delivery_tracking_sessions(active);
