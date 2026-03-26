-- =====================================================================================================================
-- V7: CREATE AUDIT RECORD TABLE
-- Phase: B3-03 (Forensic Audit)
-- =====================================================================================================================
-- Purpose: Create append-only forensic audit trail table for critical operations
-- Scope:   Sales (confirm/void), Purchase Orders (receive/void), Inventory (adjustments)
-- =====================================================================================================================

-- ─────────────────────────────────────────────────────────────────────────────────────────────────────────────────
-- TABLE: audit_record
-- ─────────────────────────────────────────────────────────────────────────────────────────────────────────────────
-- This table is append-only (no updates, no soft-deletes).
-- Captures before/after JSON snapshots of critical operations for compliance and debugging.
-- Username retrieved from SecurityContextHolder, IP address from HttpServletRequest.
-- ─────────────────────────────────────────────────────────────────────────────────────────────────────────────────

CREATE TABLE audit_record (
    id              BIGSERIAL PRIMARY KEY,
    entity_type     VARCHAR(50) NOT NULL,       -- SALE, PURCHASE_ORDER, INVENTORY
    entity_id       BIGINT NOT NULL,            -- ID of the affected entity
    action          VARCHAR(50) NOT NULL,       -- CONFIRM, VOID, RECEIVE, ADJUST
    previous_data   TEXT,                       -- JSON snapshot before operation (NULL for CREATE)
    new_data        TEXT,                       -- JSON snapshot after operation (NULL for DELETE)
    username        VARCHAR(100) NOT NULL,      -- Actor username from SecurityContextHolder
    ip_address      VARCHAR(45),                -- Client IP (IPv4 or IPv6), NULL for non-HTTP ops
    created_at      TIMESTAMP NOT NULL          -- Auto-populated by @CreatedDate
);

-- ─────────────────────────────────────────────────────────────────────────────────────────────────────────────────
-- INDEXES
-- ─────────────────────────────────────────────────────────────────────────────────────────────────────────────────
-- Common query patterns:
--   1. Find all audits for a specific entity instance (entity_type + entity_id)
--   2. Find recent audits (created_at DESC)
--   3. Find audits by actor (username)
--   4. Find audits with filters (entity_type + action + date range)
-- ─────────────────────────────────────────────────────────────────────────────────────────────────────────────────

-- Index for entity-specific audit trail (e.g., all audits for Sale #1042)
CREATE INDEX idx_audit_record_entity ON audit_record(entity_type, entity_id);

-- Index for recent audits (sorted by time)
CREATE INDEX idx_audit_record_created_at ON audit_record(created_at DESC);

-- Index for audits by actor
CREATE INDEX idx_audit_record_username ON audit_record(username);

-- Composite index for filtered queries (entity_type + action + date range)
CREATE INDEX idx_audit_record_filter ON audit_record(entity_type, action, created_at DESC);

-- =====================================================================================================================
-- END V7
-- =====================================================================================================================
