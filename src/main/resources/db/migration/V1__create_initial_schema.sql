-- =============================================================================
-- V1 | Create initial schema: users, categories, products
-- =============================================================================
-- ADR-004: created_at / created_by are NOT NULL (immutable after insert).
-- ADR-005: monetary fields use NUMERIC(19,4).
-- CA-05:   soft-delete via active column (DEFAULT TRUE).
-- =============================================================================

-- ---------------------------------------------------------------------------
-- USERS
-- ---------------------------------------------------------------------------
CREATE TABLE users (
    id             BIGSERIAL       PRIMARY KEY,
    username       VARCHAR(50)     NOT NULL,
    email          VARCHAR(150)    NOT NULL,
    password_hash  TEXT            NOT NULL,
    role           VARCHAR(20)     NOT NULL,           -- ADMIN | CAJERO | ALMACENERO
    active         BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMPTZ     NOT NULL,
    created_by     VARCHAR(100)    NOT NULL,
    updated_at     TIMESTAMPTZ,
    updated_by     VARCHAR(100),

    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_email    UNIQUE (email),
    CONSTRAINT ck_users_role     CHECK  (role IN ('ADMIN', 'CAJERO', 'ALMACENERO'))
);

-- ---------------------------------------------------------------------------
-- CATEGORIES  (Composite Pattern — self-referencing hierarchy)
-- ---------------------------------------------------------------------------
CREATE TABLE categories (
    id                  BIGSERIAL       PRIMARY KEY,
    name                VARCHAR(100)    NOT NULL,
    description         TEXT,
    parent_category_id  BIGINT          REFERENCES categories (id) ON DELETE RESTRICT,
    active              BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ     NOT NULL,
    created_by          VARCHAR(100)    NOT NULL,
    updated_at          TIMESTAMPTZ,
    updated_by          VARCHAR(100)
);

CREATE INDEX idx_categories_parent ON categories (parent_category_id);

-- ---------------------------------------------------------------------------
-- PRODUCTS
-- ---------------------------------------------------------------------------
CREATE TABLE products (
    id           BIGSERIAL        PRIMARY KEY,
    name         VARCHAR(200)     NOT NULL,
    barcode      VARCHAR(100),
    sku          VARCHAR(100),
    description  TEXT,
    cost_price   NUMERIC(19, 4)   NOT NULL,            -- ADR-005
    sale_price   NUMERIC(19, 4)   NOT NULL,            -- ADR-005
    category_id  BIGINT           REFERENCES categories (id) ON DELETE RESTRICT,
    active       BOOLEAN          NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ      NOT NULL,
    created_by   VARCHAR(100)     NOT NULL,
    updated_at   TIMESTAMPTZ,
    updated_by   VARCHAR(100),

    CONSTRAINT uq_products_barcode UNIQUE (barcode)
);

-- B-Tree index for fast barcode lookups (ADR-001 / B1-03 spec)
CREATE INDEX idx_products_barcode ON products (barcode);

-- Partial index: only active products need fast lookup
CREATE INDEX idx_products_active ON products (active) WHERE active = TRUE;
