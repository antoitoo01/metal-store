-- ============================================================
-- Migration: tenant → organization
-- Ejecutar UNA sola vez en: PostgreSQL local + Supabase prod
--
-- Orden:
--   1. Crear tablas nuevas (organizations, memberships, invitations)
--   2. Migrar datos desde old tenants (si existe)
--   3. Crear memberships para users existentes
--   4. Renombrar tenant_id → organization_id en tablas de negocio
--   5. Agregar FK constraints
--   6. Dropear tabla old tenants
-- ============================================================

BEGIN;

-- ============================================================
-- 1. Tablas nuevas
-- ============================================================

CREATE TABLE IF NOT EXISTS organizations (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS organization_role (
    id UUID PRIMARY KEY,
    name VARCHAR(20) NOT NULL UNIQUE
);

INSERT INTO organization_role (id, name) VALUES
    (gen_random_uuid(), 'OWNER'),
    (gen_random_uuid(), 'SUPER_ADMIN'),
    (gen_random_uuid(), 'ADMIN'),
    (gen_random_uuid(), 'WORKER')
ON CONFLICT (name) DO NOTHING;

CREATE TYPE IF NOT EXISTS membership_status AS ENUM ('ACTIVE', 'INVITED');

CREATE TABLE IF NOT EXISTS memberships (
    id UUID PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    organization_id UUID NOT NULL REFERENCES organizations(id),
    role VARCHAR(20) NOT NULL,
    status membership_status NOT NULL DEFAULT 'ACTIVE',
    invited_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(user_id, organization_id)
);

CREATE INDEX IF NOT EXISTS idx_memberships_org ON memberships(organization_id);
CREATE INDEX IF NOT EXISTS idx_memberships_user ON memberships(user_id);

CREATE TYPE IF NOT EXISTS invitation_status AS ENUM ('PENDING', 'ACCEPTED', 'EXPIRED');

CREATE TABLE IF NOT EXISTS invitations (
    id UUID PRIMARY KEY,
    token VARCHAR(36) UNIQUE NOT NULL,
    organization_id UUID NOT NULL REFERENCES organizations(id),
    role VARCHAR(20) NOT NULL,
    email VARCHAR(255) NOT NULL,
    status invitation_status NOT NULL DEFAULT 'PENDING',
    created_by VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_invitations_token ON invitations(token);

-- ============================================================
-- 2. Migrar organizations desde old tenants (si existe)
-- ============================================================

DO $$
DECLARE
    tenant_exists BOOLEAN;
    tenant_count INTEGER;
BEGIN
    SELECT EXISTS (
        SELECT FROM information_schema.tables
        WHERE table_schema = 'public' AND table_name = 'tenants'
    ) INTO tenant_exists;

    IF tenant_exists THEN
        SELECT COUNT(*) INTO tenant_count FROM tenants;

        INSERT INTO organizations (id, name, slug, created_at)
        SELECT id, name, slug, created_at
        FROM tenants
        ON CONFLICT (id) DO NOTHING;

        RAISE NOTICE 'Migrated % tenants to organizations', tenant_count;
    ELSE
        RAISE NOTICE 'No old tenants table found — skipping data migration';
    END IF;
END $$;

-- ============================================================
-- 3. Memberships para users existentes
-- ============================================================

-- Solo si no existen ya (idempotente)
INSERT INTO memberships (id, user_id, organization_id, role, status)
SELECT
    gen_random_uuid(),
    u.id,
    u.organization_id,
    'OWNER',
    'ACTIVE'
FROM users u
WHERE u.organization_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM memberships m
    WHERE m.user_id = u.id AND m.organization_id = u.organization_id
  );

-- ============================================================
-- 4. Renombrar tenant_id → organization_id
-- ============================================================

ALTER TABLE IF EXISTS users
    RENAME COLUMN tenant_id TO organization_id;

ALTER TABLE IF EXISTS clients
    RENAME COLUMN tenant_id TO organization_id;

ALTER TABLE IF EXISTS catalog_item_types
    RENAME COLUMN tenant_id TO organization_id;

ALTER TABLE IF EXISTS inventory_items
    RENAME COLUMN tenant_id TO organization_id;

ALTER TABLE IF EXISTS quotes
    RENAME COLUMN tenant_id TO organization_id;

ALTER TABLE IF EXISTS price_list
    RENAME COLUMN tenant_id TO organization_id;

ALTER TABLE IF EXISTS invoices
    RENAME COLUMN tenant_id TO organization_id;

-- ============================================================
-- 5. FK constraints desde tablas de negocio → organizations
-- ============================================================

-- Solo si no existen ya
DO $$
DECLARE
    fk_name TEXT;
    tables_with_org_id TEXT[] := ARRAY['users', 'clients', 'catalog_item_types', 'inventory_items', 'quotes', 'price_list', 'invoices'];
    tbl TEXT;
BEGIN
    FOREACH tbl IN ARRAY tables_with_org_id
    LOOP
        fk_name := 'fk_' || tbl || '_organization';

        IF NOT EXISTS (
            SELECT 1 FROM information_schema.table_constraints
            WHERE constraint_schema = 'public'
              AND table_name = tbl
              AND constraint_name = fk_name
              AND constraint_type = 'FOREIGN KEY'
        ) THEN
            EXECUTE format(
                'ALTER TABLE %I ADD CONSTRAINT %I FOREIGN KEY (organization_id) REFERENCES organizations(id)',
                tbl, fk_name
            );
        END IF;
    END LOOP;
END $$;

-- ============================================================
-- 6. Dropear tabla old tenants
-- ============================================================

DROP TABLE IF EXISTS tenants CASCADE;

-- ============================================================
-- 7. Actualizar RLS (si aplica en Supabase)
-- ============================================================

ALTER TABLE organizations ENABLE ROW LEVEL SECURITY;
ALTER TABLE memberships ENABLE ROW LEVEL SECURITY;
ALTER TABLE invitations ENABLE ROW LEVEL SECURITY;

-- Policy básica: cada quien ve sus propias organizaciones via membership
CREATE POLICY IF NOT EXISTS organizations_select_policy ON organizations
    FOR SELECT USING (
        id IN (SELECT organization_id FROM memberships WHERE user_id = current_setting('request.jwt.claims')::json ->> 'sub')
    );

CREATE POLICY IF NOT EXISTS memberships_select_policy ON memberships
    FOR SELECT USING (
        user_id = current_setting('request.jwt.claims')::json ->> 'sub'
    );

COMMIT;
