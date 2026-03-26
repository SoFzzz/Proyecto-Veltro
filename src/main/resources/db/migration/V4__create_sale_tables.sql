-- V4__create_sale_tables.sql
-- B2-01 | Sale Module (POS) with State Pattern
-- Creates sale and sale_detail tables with optimistic locking and audit fields

-- Create sequence for sale number generation
CREATE SEQUENCE IF NOT EXISTS sale_number_seq START 1 INCREMENT 1;

-- Create sale table
CREATE TABLE sale (
    id                  BIGSERIAL PRIMARY KEY,
    sale_number         VARCHAR(20) UNIQUE NOT NULL,
    status              VARCHAR(20) NOT NULL CHECK (status IN ('IN_PROGRESS', 'COMPLETED', 'VOIDED')),
    cashier_id          BIGINT NOT NULL,
    payment_method      VARCHAR(20) CHECK (payment_method IN ('CASH', 'CARD', 'YAPE', 'PLIN')),
    amount_received     NUMERIC(19, 4),
    change              NUMERIC(19, 4),
    subtotal            NUMERIC(19, 4) NOT NULL DEFAULT 0,
    total               NUMERIC(19, 4) NOT NULL DEFAULT 0,
    completed_at        TIMESTAMPTZ,
    version             BIGINT NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ NOT NULL,
    created_by          VARCHAR(100) NOT NULL,
    updated_at          TIMESTAMPTZ,
    updated_by          VARCHAR(100),
    active              BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_sale_cashier FOREIGN KEY (cashier_id) REFERENCES users(id)
);

-- Create sale_detail table
CREATE TABLE sale_detail (
    id                  BIGSERIAL PRIMARY KEY,
    sale_id             BIGINT NOT NULL,
    product_id          BIGINT NOT NULL,
    quantity            INTEGER NOT NULL CHECK (quantity > 0),
    unit_price          NUMERIC(19, 4) NOT NULL,
    subtotal            NUMERIC(19, 4) NOT NULL,
    version             BIGINT NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ NOT NULL,
    created_by          VARCHAR(100) NOT NULL,
    updated_at          TIMESTAMPTZ,
    updated_by          VARCHAR(100),
    active              BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_sale_detail_sale FOREIGN KEY (sale_id) REFERENCES sale(id),
    CONSTRAINT fk_sale_detail_product FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Create indexes for performance
CREATE INDEX idx_sale_sale_number ON sale(sale_number);
CREATE INDEX idx_sale_detail_sale_id ON sale_detail(sale_id);
CREATE INDEX idx_sale_cashier_id ON sale(cashier_id);
CREATE INDEX idx_sale_status ON sale(status);
