# 006 — Proveedores

## Pantallas

### `/suppliers` — Listado
- Tabla paginada: Nombre | Email | Teléfono | VAT | Estado | Acciones
- Filtro: búsqueda (`q`), estado (`status: ACTIVE/INACTIVE`)
- Estados con badge: ACTIVE=verde, INACTIVE=rojo
- Botón "+ Nuevo proveedor" (STAFF/ADMIN) → modal con: nombre (obligatorio), email, teléfono, dirección, VAT, notas
- Acciones: Editar (modal), Activar/Desactivar (toggle), Eliminar (ADMIN, con confirmación)

### `/suppliers/:id` — Detalle (opcional, puede ser drawer)
- Toda la info del proveedor
- Purchase orders asociadas: desde `/api/purchase-orders?supplierId=`
- Albaranes de entrada asociados: desde `/api/inbound-delivery-notes?q=`

## API Reference

| Método | Endpoint | Request | Response |
|--------|----------|---------|----------|
| GET | `/api/suppliers?q=&status=&page=` | — | `Page<SupplierResponse>` |
| GET | `/api/suppliers/{id}` | — | `SupplierResponse` |
| POST | `/api/suppliers` | `{ name, email?, phone?, address?, vatNumber?, notes? }` | `SupplierResponse` |
| PUT | `/api/suppliers/{id}` | `{ name?, email?, phone?, address?, vatNumber?, notes?, status? }` | `SupplierResponse` |
| DELETE | `/api/suppliers/{id}` | — | `204` |
| POST | `/api/suppliers/{id}/activate` | — | `SupplierResponse` |
| POST | `/api/suppliers/{id}/deactivate` | — | `SupplierResponse` |

### SupplierResponse
```json
{ "id": "uuid", "organizationId": "uuid", "name": "Aceros del Sur SL",
  "email": "ventas@acerosur.com", "phone": "955001122",
  "address": "Pol. Ind. Store, nave 7, Sevilla", "vatNumber": "B87654321",
  "notes": null, "status": "ACTIVE", "createdAt": "...", "updatedAt": "..." }
```
