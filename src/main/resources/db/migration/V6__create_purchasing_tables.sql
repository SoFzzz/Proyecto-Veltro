-- V6__create_purchasing_tables.sql
-- B2-04 | Purchasing Module with State Pattern (PENDING → PARTIAL → RECEIVED → VOIDED)
-- Creates supplier, purchase_order, and purchase_order_detail tables

-- Create sequence for purchase order number generation (PO-YYYY-NNNNNN)
CREATE SEQUENCE IF NOT EXISTS purchase_order_number_seq START 1 INCREMENT 1;

-- Create supplier table
CREATE TABLE supplier (
    id              BIGSERIAL PRIMARY KEY,
    tax_id          VARCHAR(50) UNIQUE NOT NULL,  -- Simple unique string, no format validation
    company_name    VARCHAR(200) NOT NULL,
    email           VARCHAR(100),
    phone           VARCHAR(50),
    address         TEXT,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL,
    created_by      VARCHAR(100) NOT NULL,
    updated_at      TIMESTAMPTZ,
    updated_by      VARCHAR(100)
);

-- Create purchase_order table
CREATE TABLE purchase_order (
    id              BIGSERIAL PRIMARY KEY,
    order_number    VARCHAR(20) UNIQUE NOT NULL,  -- PO-YYYY-NNNNNN format
    supplier_id     BIGINT NOT NULL,
    requested_by    BIGINT NOT NULL,
    status          VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'PARTIAL', 'RECEIVED', 'VOIDED')),
    total           NUMERIC(19, 4) NOT NULL DEFAULT 0,  -- ADR-005: monetary precision
    notes           TEXT,
    version         BIGINT NOT NULL DEFAULT 0,  -- ADR-002: optimistic locking
    active          BOOLEAN NOT NULL DEFAULT TRUE,  -- AC-05: soft delete
    created_at      TIMESTAMPTZ NOT NULL,  -- ADR-004: immutable audit
    created_by      VARCHAR(100) NOT NULL,  -- ADR-004: immutable audit
    updated_at      TIMESTAMPTZ,
    updated_by      VARCHAR(100),
    CONSTRAINT fk_po_supplier FOREIGN KEY (supplier_id) REFERENCES supplier(id),
    CONSTRAINT fk_po_requested_by FOREIGN KEY (requested_by) REFERENCES users(id)
);

-- Create purchase_order_detail table
CREATE TABLE purchase_order_detail (
    id                  BIGSERIAL PRIMARY KEY,
    purchase_order_id   BIGINT NOT NULL,
    product_id          BIGINT NOT NULL,
    requested_quantity  INTEGER NOT NULL CHECK (requested_quantity > 0),
    received_quantity   INTEGER NOT NULL DEFAULT 0 CHECK (received_quantity >= 0),
    unit_cost           NUMERIC(19, 4) NOT NULL,  -- Historical snapshot, does NOT update ProductEntity
    active              BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ NOT NULL,
    created_by          VARCHAR(100) NOT NULL,
    updated_at          TIMESTAMPTZ,
    updated_by          VARCHAR(100),
    CONSTRAINT fk_pod_order FOREIGN KEY (purchase_order_id) REFERENCES purchase_order(id),
    CONSTRAINT fk_pod_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT chk_received_not_exceeds_requested CHECK (received_quantity <= requested_quantity)
);

-- Performance indexes
CREATE INDEX idx_supplier_tax_id ON supplier(tax_id);
CREATE INDEX idx_po_order_number ON purchase_order(order_number);
CREATE INDEX idx_po_supplier_id ON purchase_order(supplier_id);
CREATE INDEX idx_po_requested_by ON purchase_order(requested_by);
CREATE INDEX idx_po_status ON purchase_order(status);
CREATE INDEX idx_po_created_at ON purchase_order(created_at);
CREATE INDEX idx_pod_order_id ON purchase_order_detail(purchase_order_id);
CREATE INDEX idx_pod_product_id ON purchase_order_detail(product_id);