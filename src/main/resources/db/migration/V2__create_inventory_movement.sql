-- =============================================================================
-- V2 | Create inventory and inventory_movements tables
-- =============================================================================
-- ADR-002: inventory.version BIGINT NOT NULL — optimistic locking field.
-- CA-04:   CHECK (current_stock >= 0) enforced at DB level as a safety net.
--          The service layer validates before DML, but the constraint is the
--          final guard against bugs slipping through.
-- inventory_movements is append-only: no updated_at / updated_by / active.
-- =============================================================================

-- ---------------------------------------------------------------------------
-- INVENTORY  (one row per product — 1-to-1 relationship)
-- ---------------------------------------------------------------------------
CREATE TABLE inventory (
    id             BIGSERIAL   PRIMARY KEY,
    product_id     BIGINT      NOT NULL REFERENCES products (id) ON DELETE RESTRICT,
    current_stock  INTEGER     NOT NULL DEFAULT 0,
    min_stock      INTEGER     NOT NULL DEFAULT 0,
    max_stock      INTEGER     NOT NULL DEFAULT 0,
    version        BIGINT      NOT NULL DEFAULT 0,     -- ADR-002: optimistic locking
    active         BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMPTZ NOT NULL,
    created_by     VARCHAR(100) NOT NULL,
    updated_at     TIMESTAMPTZ,
    updated_by     VARCHAR(100),

    CONSTRAINT uq_inventory_product  UNIQUE  (product_id),
    CONSTRAINT ck_inventory_stock_nn CHECK   (current_stock >= 0),    -- CA-04
    CONSTRAINT ck_inventory_min_nn   CHECK   (min_stock     >= 0),
    CONSTRAINT ck_inventory_max_nn   CHECK   (max_stock     >= 0)
);

-- ---------------------------------------------------------------------------
-- INVENTORY_MOVEMENTS  (append-only audit trail)
-- ---------------------------------------------------------------------------
CREATE TABLE inventory_movements (
    id              BIGSERIAL   PRIMARY KEY,
    inventory_id    BIGINT      NOT NULL REFERENCES inventory (id) ON DELETE RESTRICT,
    movement_type   VARCHAR(20) NOT NULL,              -- ENTRY | EXIT | ADJUSTMENT
    quantity        INTEGER     NOT NULL,
    previous_stock  INTEGER     NOT NULL,
    new_stock       INTEGER     NOT NULL,
    reason          TEXT,
    created_at      TIMESTAMPTZ NOT NULL,
    created_by      VARCHAR(100) NOT NULL,

    CONSTRAINT ck_movement_type CHECK (movement_type IN ('ENTRY', 'EXIT', 'ADJUSTMENT')),
    CONSTRAINT ck_movement_qty  CHECK (quantity > 0)
);

CREATE INDEX idx_movements_inventory ON inventory_movements (inventory_id);
