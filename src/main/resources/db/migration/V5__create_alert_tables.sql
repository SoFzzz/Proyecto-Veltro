-- B2-03 Proactive Alerts

CREATE TABLE alert_configuration (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL UNIQUE REFERENCES product (id),
    critical_stock INTEGER NOT NULL,
    min_stock INTEGER NOT NULL,
    overstock_threshold INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP,
    updated_by VARCHAR(100),
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE alert (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES product (id),
    type VARCHAR(32) NOT NULL,
    severity VARCHAR(16) NOT NULL,
    message VARCHAR(500) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    resolved BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP,
    updated_by VARCHAR(100),
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_alert_product_resolved ON alert (product_id, resolved);
CREATE INDEX idx_alert_severity_created ON alert (severity DESC, created_at ASC);
