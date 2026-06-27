# 008 — Albaranes de Entrada (Inbound Delivery Notes)

## Pantallas

### `/inbound` — Listado
- Tabla paginada: Número | Proveedor | Fecha | Estado | Total | Acciones
- Filtros: búsqueda (`q`), estado (`status: DRAFT/CONFIRMED/CANCELLED`)
- Estados con badge: DRAFT=gris, CONFIRMED=verde, CANCELLED=rojo
- Botón "+ Nuevo albarán" (STAFF/ADMIN) → formulario completo

### `/inbound/new` — Crear albarán de entrada
- Paso 1: datos generales → proveedor (selector o manual), OC asociada (selector opcional desde `/api/purchase-orders?status=ISSUED`), fecha, notas
- Paso 2: líneas. Tabla editable con:
  - Línea | Perfil/Ítem (autocomplete desde catálogo) | Descripción | Cantidad | Precio | IVA (%) | Notas
  - Botón "+ Añadir línea"
- Botón "Guardar borrador" → `POST /api/inbound-delivery-notes` + líneas
- Botón "Confirmar entrada" → guarda + confirma (confirmar añade stock automáticamente)

### `/inbound/:id` — Detalle albarán
- Cabecera: número, proveedor, OC asociada, fechas, estado, total
- Tabla de líneas
- Botones según estado:
  - DRAFT: Editar, Confirmar, Cancelar
  - CONFIRMED: (solo lectura — ya afectó al stock)
  - CANCELLED: (solo lectura)

### Estados y transiciones
```
DRAFT ──confirm──→ CONFIRMED
  │
  └──cancel──→ CANCELLED
```

## API Reference

| Método | Endpoint | Request | Response |
|--------|----------|---------|----------|
| GET | `/api/inbound-delivery-notes?page=` | — | `Page<InboundResponse>` |
| GET | `/api/inbound-delivery-notes/{id}` | — | `InboundResponse` |
| GET | `/api/inbound-delivery-notes/{id}/lines` | — | `List<InboundLineResponse>` |
| POST | `/api/inbound-delivery-notes` | `{ supplierId?, supplierName?, supplierVat?, supplierAddress?, poId?, poNumber?, issueDate?, notes? }` | `InboundResponse` |
| POST | `/api/inbound-delivery-notes/{id}/lines` | `{ lineNumber, profileId?, itemId?, description, quantity, unitPrice, vatRate, notes? }` | `InboundLineResponse` |
| DELETE | `/api/inbound-delivery-notes/{id}/lines/{lineId}` | — | `204` |
| POST | `/api/inbound-delivery-notes/{id}/confirm` | — | `InboundResponse` |
| POST | `/api/inbound-delivery-notes/{id}/cancel` | — | `InboundResponse` |

### InboundResponse
```json
{ "id": "uuid", "number": "AL-2026-A1B2C3D4-001",
  "supplierId": "uuid", "supplierName": "Aceros del Sur SL",
  "supplierVat": "B87654321", "supplierAddress": "...",
  "poId": "uuid", "poNumber": "OC-2026-A1B2C3D4-001",
  "issueDate": "2026-06-10", "status": "CONFIRMED",
  "totalAmount": 1512.50, "notes": null, "createdAt": "...", "updatedAt": "..." }
```

### InboundLineResponse
```json
{ "id": "uuid", "deliveryNoteId": "uuid", "lineNumber": 10,
  "profileId": null, "itemId": null, "description": "HEB 200, 6m",
  "quantity": 10, "unitPrice": 125.00, "vatRate": 21.00, "notes": null }
```
