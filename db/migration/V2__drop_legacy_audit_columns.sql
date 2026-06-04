-- Clean up legacy audit columns from a previous JPA auditing configuration.
-- These columns (created_date, last_modified_date) were created by an earlier
-- version of the app that used @CreatedDate/@LastModifiedDate on differently-named
-- fields. The current BaseEntity uses created_at/updated_at (added in V1).
-- Safe to re-run (all DROP COLUMN use IF EXISTS).

ALTER TABLE tenants DROP COLUMN IF EXISTS created_date;
ALTER TABLE tenants DROP COLUMN IF EXISTS last_modified_date;

ALTER TABLE users DROP COLUMN IF EXISTS created_date;
ALTER TABLE users DROP COLUMN IF EXISTS last_modified_date;

ALTER TABLE clients DROP COLUMN IF EXISTS created_date;
ALTER TABLE clients DROP COLUMN IF EXISTS last_modified_date;
