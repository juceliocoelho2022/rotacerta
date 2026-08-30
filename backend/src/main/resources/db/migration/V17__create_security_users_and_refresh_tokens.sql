CREATE TABLE app_users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(180) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(160) NOT NULL,
    role VARCHAR(30) NOT NULL,
    customer_id BIGINT REFERENCES customers(id) ON DELETE SET NULL,
    driver_id BIGINT REFERENCES drivers(id) ON DELETE SET NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_app_user_role CHECK (role IN ('ADMIN', 'CUSTOMER', 'DRIVER')),
    CONSTRAINT chk_app_user_link CHECK (
        (role = 'ADMIN' AND customer_id IS NULL AND driver_id IS NULL)
        OR (role = 'CUSTOMER' AND driver_id IS NULL)
        OR (role = 'DRIVER' AND customer_id IS NULL)
    )
);

CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    revoked_at TIMESTAMPTZ
);

CREATE INDEX idx_app_users_role ON app_users(role);
CREATE INDEX idx_app_users_customer ON app_users(customer_id);
CREATE INDEX idx_app_users_driver ON app_users(driver_id);
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_expiry ON refresh_tokens(expires_at);
