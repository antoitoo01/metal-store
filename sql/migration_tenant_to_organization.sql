-- ============================================================
-- Migration: tenant → organization
-- Ejecutar UNA sola vez en Supabase SQL Editor.
-- Crea tablas si no existen (Hibernate nunca las crea en prod
-- porque usa ddl-auto: validate) + migra datos existentes.
-- ============================================================

BEGIN;

-- ============================================================
-- 1. Tablas nuevas (schema exacto que espera Hibernate)
-- ============================================================

CREATE TABLE IF NOT EXISTS organizations (
    id UUID NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL,
    settings TEXT,
    PRIMARY KEY (id),
    CONSTRAINT uq_organizations_slug UNIQUE (slug)
);

CREATE TABLE IF NOT EXISTS memberships (
    id UUID NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    user_id UUID NOT NULL,
    organization_id UUID NOT NULL,
    role VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL DEFAULT 'ACTIVE',
    invited_by UUID,
    PRIMARY KEY (id),
    CONSTRAINT uq_memberships_user_org UNIQUE (user_id, organization_id),
    CONSTRAINT fk_memberships_organization FOREIGN KEY (organization_id) REFERENCES organizations(id)
);

CREATE INDEX IF NOT EXISTS idx_membership_org ON memberships(organization_id);
CREATE INDEX IF NOT EXISTS idx_membership_user ON memberships(user_id);

CREATE TABLE IF NOT EXISTS invitations (
    id UUID NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    token UUID NOT NULL,
    organization_id UUID NOT NULL,
    role VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL DEFAULT 'PENDING',
    created_by UUID NOT NULL,
    expires_at TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uq_invitations_token UNIQUE (token),
    CONSTRAINT fk_invitations_organization FOREIGN KEY (organization_id) REFERENCES organizations(id)
);

-- ============================================================
-- 2. Migrar datos desde old tenants → organizations
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

        INSERT INTO organizations (id, name, slug, created_at, updated_at, settings)
        SELECT id, name, slug, created_at, COALESCE(updated_at, created_at), settings
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

DO $$
DECLARE
    org_col TEXT;
BEGIN
    SELECT column_name INTO org_col
    FROM information_schema.columns
    WHERE table_schema = 'public' AND table_name = 'users'
      AND column_name IN ('tenant_id', 'organization_id')
    ORDER BY column_name DESC
    LIMIT 1;

    IF org_col IS NOT NULL THEN
        EXECUTE format(
            'INSERT INTO memberships (id, created_at, updated_at, user_id, organization_id, role, status)
             SELECT gen_random_uuid(), NOW(), NOW(), u.id, u.%I, ''OWNER'', ''ACTIVE''
             FROM users u
             WHERE u.%I IS NOT NULL
               AND NOT EXISTS (
                 SELECT 1 FROM memberships m
                 WHERE m.user_id = u.id AND m.organization_id = u.%I
               )',
            org_col, org_col, org_col
        );
        RAISE NOTICE 'Created memberships using users.%', org_col;
    ELSE
        RAISE NOTICE 'users table has neither tenant_id nor organization_id — skipping memberships';
    END IF;
END $$;

-- ============================================================
-- 4. Renombrar tenant_id → organization_id
-- ============================================================

DO $$
DECLARE
    tables_to_fix TEXT[] := ARRAY['users', 'clients', 'catalog_item_types', 'inventory_items', 'quotes', 'price_list', 'invoices'];
    tbl TEXT;
    has_old_col BOOLEAN;
BEGIN
    FOREACH tbl IN ARRAY tables_to_fix
    LOOP
        EXECUTE format(
            'SELECT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = ''public'' AND table_name = %L AND column_name = ''tenant_id'')',
            tbl
        ) INTO has_old_col;

        IF has_old_col THEN
            EXECUTE format('ALTER TABLE %I DROP COLUMN IF EXISTS organization_id', tbl);
            EXECUTE format('ALTER TABLE %I RENAME COLUMN tenant_id TO organization_id', tbl);
            RAISE NOTICE 'Renamed tenant_id → organization_id in %', tbl;
        ELSE
            RAISE NOTICE 'Table % already has organization_id — skipping', tbl;
        END IF;
    END LOOP;
END $$;

-- ============================================================
-- 5. FK constraints → organizations
-- ============================================================

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
-- 6. Actualizar CHECK constraints + datos
-- ============================================================

ALTER TABLE IF EXISTS users DROP CONSTRAINT IF EXISTS users_role_check;

UPDATE users SET role = 'ORGANIZATION_OWNER' WHERE role = 'TENANT_OWNER';

ALTER TABLE IF EXISTS users ADD CONSTRAINT users_role_check
    CHECK (role::text = ANY (ARRAY['ORGANIZATION_OWNER', 'ADMIN', 'STAFF', 'USER']));

-- ============================================================
-- 7. Dropear tabla old tenants
-- ============================================================

DROP TABLE IF EXISTS tenants CASCADE;

-- ============================================================
-- 8. RLS policies
-- ============================================================

ALTER TABLE organizations ENABLE ROW LEVEL SECURITY;
ALTER TABLE memberships ENABLE ROW LEVEL SECURITY;
ALTER TABLE invitations ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS organizations_select_policy ON organizations;
CREATE POLICY organizations_select_policy ON organizations
    FOR SELECT USING (
        id IN (
            SELECT organization_id FROM memberships
            WHERE user_id = (current_setting('request.jwt.claims')::json ->> 'sub')::uuid
        )
    );

DROP POLICY IF EXISTS memberships_select_policy ON memberships;
CREATE POLICY memberships_select_policy ON memberships
    FOR SELECT USING (
        user_id = (current_setting('request.jwt.claims')::json ->> 'sub')::uuid
    );

COMMIT;
