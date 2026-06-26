-- ============================================================================
-- Metal Store — RLS Policies for Supabase
-- Run this in the Supabase SQL Editor AFTER running migration_tenant_to_organization.sql.
-- Idempotent: safe to run multiple times.
-- ============================================================================

-- ── Helper: organization isolation function ───────────────────────────────
-- Returns the set of organization IDs the authenticated user has active
-- membership in. Used to scope all business data queries.
CREATE OR REPLACE FUNCTION public.user_organization_ids()
RETURNS SETOF uuid
LANGUAGE sql STABLE
AS $$
  SELECT m.organization_id FROM public.memberships m WHERE m.user_id = auth.uid() AND m.status = 'ACTIVE'
$$;

-- ── organizations table ──────────────────────────────────────────────────
ALTER TABLE public.organizations ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Organizations are viewable by members" ON public.organizations;
CREATE POLICY "Organizations are viewable by members"
  ON public.organizations FOR SELECT
  USING (id IN (SELECT public.user_organization_ids()));

DROP POLICY IF EXISTS "Organizations are insertable by service role only" ON public.organizations;
CREATE POLICY "Organizations are insertable by service role only"
  ON public.organizations FOR INSERT
  WITH CHECK (false);

-- ── memberships table ────────────────────────────────────────────────────
ALTER TABLE public.memberships ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Users see their own memberships" ON public.memberships;
CREATE POLICY "Users see their own memberships"
  ON public.memberships FOR SELECT
  USING (user_id = auth.uid());

-- ── invitations table ────────────────────────────────────────────────────
ALTER TABLE public.invitations ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Users see invitations for their orgs" ON public.invitations;
CREATE POLICY "Users see invitations for their orgs"
  ON public.invitations FOR SELECT
  USING (organization_id IN (SELECT public.user_organization_ids()));

-- ── users table ─────────────────────────────────────────────────────────
ALTER TABLE public.users ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Users see their own row" ON public.users;
CREATE POLICY "Users see their own row"
  ON public.users FOR SELECT
  USING (id = auth.uid());

DROP POLICY IF EXISTS "Users can update their own row" ON public.users;
CREATE POLICY "Users can update their own row"
  ON public.users FOR UPDATE
  USING (id = auth.uid());

-- ── catalog (globally shared, read-only) ─────────────────────────────────
ALTER TABLE public.catalog_families ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.catalog_profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.aisc_profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.euro_profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.catalog_items ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Catalog is globally readable" ON public.catalog_families;
CREATE POLICY "Catalog is globally readable"
  ON public.catalog_families FOR SELECT USING (true);

DROP POLICY IF EXISTS "Catalog is globally readable" ON public.catalog_profiles;
CREATE POLICY "Catalog is globally readable"
  ON public.catalog_profiles FOR SELECT USING (true);

DROP POLICY IF EXISTS "Catalog is globally readable" ON public.aisc_profiles;
CREATE POLICY "Catalog is globally readable"
  ON public.aisc_profiles FOR SELECT USING (true);

DROP POLICY IF EXISTS "Catalog is globally readable" ON public.euro_profiles;
CREATE POLICY "Catalog is globally readable"
  ON public.euro_profiles FOR SELECT USING (true);

DROP POLICY IF EXISTS "Catalog is globally readable" ON public.catalog_items;
CREATE POLICY "Catalog is globally readable"
  ON public.catalog_items FOR SELECT USING (true);

-- ── catalog_item_types (org-scoped) ──────────────────────────────────────
ALTER TABLE public.catalog_item_types ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Users see their own item types" ON public.catalog_item_types;
CREATE POLICY "Users see their own item types"
  ON public.catalog_item_types FOR SELECT
  USING (organization_id IN (SELECT public.user_organization_ids()));

DROP POLICY IF EXISTS "Users insert their own item types" ON public.catalog_item_types;
CREATE POLICY "Users insert their own item types"
  ON public.catalog_item_types FOR INSERT
  WITH CHECK (organization_id IN (SELECT public.user_organization_ids()));

DROP POLICY IF EXISTS "Users update their own item types" ON public.catalog_item_types;
CREATE POLICY "Users update their own item types"
  ON public.catalog_item_types FOR UPDATE
  USING (organization_id IN (SELECT public.user_organization_ids()));

DROP POLICY IF EXISTS "Users delete their own item types" ON public.catalog_item_types;
CREATE POLICY "Users delete their own item types"
  ON public.catalog_item_types FOR DELETE
  USING (organization_id IN (SELECT public.user_organization_ids()));

-- ── inventory_items (org-scoped) ─────────────────────────────────────────
ALTER TABLE public.inventory_items ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Users see their own inventory" ON public.inventory_items;
CREATE POLICY "Users see their own inventory"
  ON public.inventory_items FOR SELECT
  USING (organization_id IN (SELECT public.user_organization_ids()));

DROP POLICY IF EXISTS "Users insert their own inventory" ON public.inventory_items;
CREATE POLICY "Users insert their own inventory"
  ON public.inventory_items FOR INSERT
  WITH CHECK (organization_id IN (SELECT public.user_organization_ids()));

DROP POLICY IF EXISTS "Users update their own inventory" ON public.inventory_items;
CREATE POLICY "Users update their own inventory"
  ON public.inventory_items FOR UPDATE
  USING (organization_id IN (SELECT public.user_organization_ids()));

DROP POLICY IF EXISTS "Users delete their own inventory" ON public.inventory_items;
CREATE POLICY "Users delete their own inventory"
  ON public.inventory_items FOR DELETE
  USING (organization_id IN (SELECT public.user_organization_ids()));

-- ── stock_movements (org-scoped) ─────────────────────────────────────────
ALTER TABLE public.stock_movements ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Users see their own stock movements" ON public.stock_movements;
CREATE POLICY "Users see their own stock movements"
  ON public.stock_movements FOR SELECT
  USING (organization_id IN (SELECT public.user_organization_ids()));

DROP POLICY IF EXISTS "Users manage their own stock movements" ON public.stock_movements;
CREATE POLICY "Users manage their own stock movements"
  ON public.stock_movements FOR INSERT
  WITH CHECK (organization_id IN (SELECT public.user_organization_ids()));

DROP POLICY IF EXISTS "Users manage their own stock movements" ON public.stock_movements;
CREATE POLICY "Users manage their own stock movements"
  ON public.stock_movements FOR UPDATE
  USING (organization_id IN (SELECT public.user_organization_ids()));

DROP POLICY IF EXISTS "Users manage their own stock movements" ON public.stock_movements;
CREATE POLICY "Users manage their own stock movements"
  ON public.stock_movements FOR DELETE
  USING (organization_id IN (SELECT public.user_organization_ids()));

-- ── price_list (org-scoped) ──────────────────────────────────────────────
ALTER TABLE public.price_list ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Users see their own prices" ON public.price_list;
CREATE POLICY "Users see their own prices"
  ON public.price_list FOR SELECT
  USING (organization_id IN (SELECT public.user_organization_ids()));

DROP POLICY IF EXISTS "Users manage their own prices" ON public.price_list;
CREATE POLICY "Users manage their own prices"
  ON public.price_list FOR INSERT
  WITH CHECK (organization_id IN (SELECT public.user_organization_ids()));

DROP POLICY IF EXISTS "Users manage their own prices" ON public.price_list;
CREATE POLICY "Users manage their own prices"
  ON public.price_list FOR UPDATE
  USING (organization_id IN (SELECT public.user_organization_ids()));

DROP POLICY IF EXISTS "Users manage their own prices" ON public.price_list;
CREATE POLICY "Users manage their own prices"
  ON public.price_list FOR DELETE
  USING (organization_id IN (SELECT public.user_organization_ids()));

-- ── invoices (org-scoped) ────────────────────────────────────────────────
ALTER TABLE public.invoices ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Users see their own invoices" ON public.invoices;
CREATE POLICY "Users see their own invoices"
  ON public.invoices FOR SELECT
  USING (organization_id IN (SELECT public.user_organization_ids()));

DROP POLICY IF EXISTS "Users manage their own invoices" ON public.invoices;
CREATE POLICY "Users manage their own invoices"
  ON public.invoices FOR INSERT
  WITH CHECK (organization_id IN (SELECT public.user_organization_ids()));

DROP POLICY IF EXISTS "Users manage their own invoices" ON public.invoices;
CREATE POLICY "Users manage their own invoices"
  ON public.invoices FOR UPDATE
  USING (organization_id IN (SELECT public.user_organization_ids()));

DROP POLICY IF EXISTS "Users manage their own invoices" ON public.invoices;
CREATE POLICY "Users manage their own invoices"
  ON public.invoices FOR DELETE
  USING (organization_id IN (SELECT public.user_organization_ids()));

-- ── suppliers (org-scoped) ──────────────────────────────────────────────
ALTER TABLE public.suppliers ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Users see their own suppliers" ON public.suppliers;
CREATE POLICY "Users see their own suppliers"
  ON public.suppliers FOR SELECT
  USING (organization_id IN (SELECT public.user_organization_ids()));

DROP POLICY IF EXISTS "Users manage their own suppliers" ON public.suppliers;
CREATE POLICY "Users manage their own suppliers"
  ON public.suppliers FOR INSERT
  WITH CHECK (organization_id IN (SELECT public.user_organization_ids()));

DROP POLICY IF EXISTS "Users manage their own suppliers" ON public.suppliers;
CREATE POLICY "Users manage their own suppliers"
  ON public.suppliers FOR UPDATE
  USING (organization_id IN (SELECT public.user_organization_ids()));

DROP POLICY IF EXISTS "Users manage their own suppliers" ON public.suppliers;
CREATE POLICY "Users manage their own suppliers"
  ON public.suppliers FOR DELETE
  USING (organization_id IN (SELECT public.user_organization_ids()));

-- ── purchase_orders (org-scoped) ────────────────────────────────────────
ALTER TABLE public.purchase_orders ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Users see their own purchase orders" ON public.purchase_orders;
CREATE POLICY "Users see their own purchase orders"
  ON public.purchase_orders FOR SELECT
  USING (organization_id IN (SELECT public.user_organization_ids()));

DROP POLICY IF EXISTS "Users manage their own purchase orders" ON public.purchase_orders;
CREATE POLICY "Users manage their own purchase orders"
  ON public.purchase_orders FOR INSERT
  WITH CHECK (organization_id IN (SELECT public.user_organization_ids()));

DROP POLICY IF EXISTS "Users manage their own purchase orders" ON public.purchase_orders;
CREATE POLICY "Users manage their own purchase orders"
  ON public.purchase_orders FOR UPDATE
  USING (organization_id IN (SELECT public.user_organization_ids()));

DROP POLICY IF EXISTS "Users manage their own purchase orders" ON public.purchase_orders;
CREATE POLICY "Users manage their own purchase orders"
  ON public.purchase_orders FOR DELETE
  USING (organization_id IN (SELECT public.user_organization_ids()));

-- ── purchase_order_lines (org-scoped via purchase_orders.organization_id) ─
ALTER TABLE public.purchase_order_lines ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Users see their own purchase order lines" ON public.purchase_order_lines;
CREATE POLICY "Users see their own purchase order lines"
  ON public.purchase_order_lines FOR SELECT
  USING (
    EXISTS (
      SELECT 1 FROM public.purchase_orders po
      WHERE po.id = purchase_order_id AND po.organization_id IN (SELECT public.user_organization_ids())
    )
  );

DROP POLICY IF EXISTS "Users manage their own purchase order lines" ON public.purchase_order_lines;
CREATE POLICY "Users manage their own purchase order lines"
  ON public.purchase_order_lines FOR INSERT
  WITH CHECK (
    EXISTS (
      SELECT 1 FROM public.purchase_orders po
      WHERE po.id = purchase_order_id AND po.organization_id IN (SELECT public.user_organization_ids())
    )
  );

DROP POLICY IF EXISTS "Users manage their own purchase order lines" ON public.purchase_order_lines;
CREATE POLICY "Users manage their own purchase order lines"
  ON public.purchase_order_lines FOR UPDATE
  USING (
    EXISTS (
      SELECT 1 FROM public.purchase_orders po
      WHERE po.id = purchase_order_id AND po.organization_id IN (SELECT public.user_organization_ids())
    )
  );

DROP POLICY IF EXISTS "Users manage their own purchase order lines" ON public.purchase_order_lines;
CREATE POLICY "Users manage their own purchase order lines"
  ON public.purchase_order_lines FOR DELETE
  USING (
    EXISTS (
      SELECT 1 FROM public.purchase_orders po
      WHERE po.id = purchase_order_id AND po.organization_id IN (SELECT public.user_organization_ids())
    )
  );

-- ── invoice_lines (org-scoped via invoices.organization_id) ──────────────
ALTER TABLE public.invoice_lines ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Users see their own invoice lines" ON public.invoice_lines;
CREATE POLICY "Users see their own invoice lines"
  ON public.invoice_lines FOR SELECT
  USING (
    EXISTS (
      SELECT 1 FROM public.invoices i
      WHERE i.id = invoice_id AND i.organization_id IN (SELECT public.user_organization_ids())
    )
  );

DROP POLICY IF EXISTS "Users manage their own invoice lines" ON public.invoice_lines;
CREATE POLICY "Users manage their own invoice lines"
  ON public.invoice_lines FOR INSERT
  WITH CHECK (
    EXISTS (
      SELECT 1 FROM public.invoices i
      WHERE i.id = invoice_id AND i.organization_id IN (SELECT public.user_organization_ids())
    )
  );

DROP POLICY IF EXISTS "Users manage their own invoice lines" ON public.invoice_lines;
CREATE POLICY "Users manage their own invoice lines"
  ON public.invoice_lines FOR UPDATE
  USING (
    EXISTS (
      SELECT 1 FROM public.invoices i
      WHERE i.id = invoice_id AND i.organization_id IN (SELECT public.user_organization_ids())
    )
  );

DROP POLICY IF EXISTS "Users manage their own invoice lines" ON public.invoice_lines;
CREATE POLICY "Users manage their own invoice lines"
  ON public.invoice_lines FOR DELETE
  USING (
    EXISTS (
      SELECT 1 FROM public.invoices i
      WHERE i.id = invoice_id AND i.organization_id IN (SELECT public.user_organization_ids())
    )
  );

-- ── inbound_delivery_notes (org-scoped) ────────────────────────────────
ALTER TABLE public.inbound_delivery_notes ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Users see their own delivery notes" ON public.inbound_delivery_notes;
CREATE POLICY "Users see their own delivery notes"
  ON public.inbound_delivery_notes FOR SELECT
  USING (organization_id IN (SELECT public.user_organization_ids()));

DROP POLICY IF EXISTS "Users manage their own delivery notes" ON public.inbound_delivery_notes;
CREATE POLICY "Users manage their own delivery notes"
  ON public.inbound_delivery_notes FOR INSERT
  WITH CHECK (organization_id IN (SELECT public.user_organization_ids()));

DROP POLICY IF EXISTS "Users manage their own delivery notes" ON public.inbound_delivery_notes;
CREATE POLICY "Users manage their own delivery notes"
  ON public.inbound_delivery_notes FOR UPDATE
  USING (organization_id IN (SELECT public.user_organization_ids()));

DROP POLICY IF EXISTS "Users manage their own delivery notes" ON public.inbound_delivery_notes;
CREATE POLICY "Users manage their own delivery notes"
  ON public.inbound_delivery_notes FOR DELETE
  USING (organization_id IN (SELECT public.user_organization_ids()));

-- ── inbound_delivery_note_lines (org-scoped via inbound_delivery_notes.organization_id) ─
ALTER TABLE public.inbound_delivery_note_lines ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Users see their own delivery note lines" ON public.inbound_delivery_note_lines;
CREATE POLICY "Users see their own delivery note lines"
  ON public.inbound_delivery_note_lines FOR SELECT
  USING (
    EXISTS (
      SELECT 1 FROM public.inbound_delivery_notes idn
      WHERE idn.id = delivery_note_id AND idn.organization_id IN (SELECT public.user_organization_ids())
    )
  );

DROP POLICY IF EXISTS "Users manage their own delivery note lines" ON public.inbound_delivery_note_lines;
CREATE POLICY "Users manage their own delivery note lines"
  ON public.inbound_delivery_note_lines FOR INSERT
  WITH CHECK (
    EXISTS (
      SELECT 1 FROM public.inbound_delivery_notes idn
      WHERE idn.id = delivery_note_id AND idn.organization_id IN (SELECT public.user_organization_ids())
    )
  );

DROP POLICY IF EXISTS "Users manage their own delivery note lines" ON public.inbound_delivery_note_lines;
CREATE POLICY "Users manage their own delivery note lines"
  ON public.inbound_delivery_note_lines FOR UPDATE
  USING (
    EXISTS (
      SELECT 1 FROM public.inbound_delivery_notes idn
      WHERE idn.id = delivery_note_id AND idn.organization_id IN (SELECT public.user_organization_ids())
    )
  );

DROP POLICY IF EXISTS "Users manage their own delivery note lines" ON public.inbound_delivery_note_lines;
CREATE POLICY "Users manage their own delivery note lines"
  ON public.inbound_delivery_note_lines FOR DELETE
  USING (
    EXISTS (
      SELECT 1 FROM public.inbound_delivery_notes idn
      WHERE idn.id = delivery_note_id AND idn.organization_id IN (SELECT public.user_organization_ids())
    )
  );

-- ── outbound_delivery_notes (org-scoped) ────────────────────────────────
ALTER TABLE public.outbound_delivery_notes ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Users see their own outbound delivery notes" ON public.outbound_delivery_notes;
CREATE POLICY "Users see their own outbound delivery notes"
  ON public.outbound_delivery_notes FOR SELECT
  USING (organization_id IN (SELECT public.user_organization_ids()));

DROP POLICY IF EXISTS "Users manage their own outbound delivery notes" ON public.outbound_delivery_notes;
CREATE POLICY "Users manage their own outbound delivery notes"
  ON public.outbound_delivery_notes FOR INSERT
  WITH CHECK (organization_id IN (SELECT public.user_organization_ids()));

DROP POLICY IF EXISTS "Users manage their own outbound delivery notes" ON public.outbound_delivery_notes;
CREATE POLICY "Users manage their own outbound delivery notes"
  ON public.outbound_delivery_notes FOR UPDATE
  USING (organization_id IN (SELECT public.user_organization_ids()));

DROP POLICY IF EXISTS "Users manage their own outbound delivery notes" ON public.outbound_delivery_notes;
CREATE POLICY "Users manage their own outbound delivery notes"
  ON public.outbound_delivery_notes FOR DELETE
  USING (organization_id IN (SELECT public.user_organization_ids()));

-- ── outbound_delivery_note_lines (org-scoped via outbound_delivery_notes.organization_id) ─
ALTER TABLE public.outbound_delivery_note_lines ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Users see their own outbound delivery note lines" ON public.outbound_delivery_note_lines;
CREATE POLICY "Users see their own outbound delivery note lines"
  ON public.outbound_delivery_note_lines FOR SELECT
  USING (
    EXISTS (
      SELECT 1 FROM public.outbound_delivery_notes odn
      WHERE odn.id = delivery_note_id AND odn.organization_id IN (SELECT public.user_organization_ids())
    )
  );

DROP POLICY IF EXISTS "Users manage their own outbound delivery note lines" ON public.outbound_delivery_note_lines;
CREATE POLICY "Users manage their own outbound delivery note lines"
  ON public.outbound_delivery_note_lines FOR INSERT
  WITH CHECK (
    EXISTS (
      SELECT 1 FROM public.outbound_delivery_notes odn
      WHERE odn.id = delivery_note_id AND odn.organization_id IN (SELECT public.user_organization_ids())
    )
  );

DROP POLICY IF EXISTS "Users manage their own outbound delivery note lines" ON public.outbound_delivery_note_lines;
CREATE POLICY "Users manage their own outbound delivery note lines"
  ON public.outbound_delivery_note_lines FOR UPDATE
  USING (
    EXISTS (
      SELECT 1 FROM public.outbound_delivery_notes odn
      WHERE odn.id = delivery_note_id AND odn.organization_id IN (SELECT public.user_organization_ids())
    )
  );

DROP POLICY IF EXISTS "Users manage their own outbound delivery note lines" ON public.outbound_delivery_note_lines;
CREATE POLICY "Users manage their own outbound delivery note lines"
  ON public.outbound_delivery_note_lines FOR DELETE
  USING (
    EXISTS (
      SELECT 1 FROM public.outbound_delivery_notes odn
      WHERE odn.id = delivery_note_id AND odn.organization_id IN (SELECT public.user_organization_ids())
    )
  );
