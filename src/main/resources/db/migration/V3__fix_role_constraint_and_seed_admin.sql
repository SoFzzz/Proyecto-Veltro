-- =============================================================================
-- V3 | Fix role constraint to match Java Role enum + seed default admin user
-- =============================================================================
-- The V1 migration used Spanish role names (CAJERO, ALMACENERO).
-- This migration aligns the CHECK constraint with the Java Role enum values
-- (ADMIN, CASHIER, WAREHOUSE) and inserts the default development admin user.
--
-- Default credentials (LOCAL DEV ONLY):
--   username : admin
--   password : admin123
--   hash     : BCrypt cost=12
-- =============================================================================

-- Drop the old constraint and recreate with the correct enum values
ALTER TABLE users
    DROP CONSTRAINT IF EXISTS ck_users_role;

ALTER TABLE users
    ADD CONSTRAINT ck_users_role
        CHECK (role IN ('ADMIN', 'CASHIER', 'WAREHOUSE'));

-- Insert the default ADMIN user for local development.
-- Password hash is BCrypt(cost=12) of 'admin123'.
-- ON CONFLICT DO NOTHING so re-running migrations is idempotent.
INSERT INTO users (username, email, password_hash, role, active, created_at, created_by)
VALUES (
    'admin',
    'admin@veltro.dev',
    '$2a$12$eFQHUoGpC5lxzHT3NZRLNuqkniC5Y5kRW8RGRGBqX1hSdIk6BrPDe',
    'ADMIN',
    TRUE,
    NOW(),
    'SYSTEM'
)
ON CONFLICT (username) DO NOTHING;
