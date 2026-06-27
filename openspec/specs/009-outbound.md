# 009 — Albaranes de Salida (Outbound Delivery Notes)

## Pantallas

### `/outbound` — Listado
- Tabla paginada: Número | Cliente | Fecha | Estado | Total | Acciones
- Filtros: búsqueda (`q`), estado (`status: DRAFT/CONFIRMED/CANCELLED`)
- Estados con badge: DRAFT=gris, CONFIRMED=verde, CANCELLED=rojo
- Botón "+ Nuevo albarán" (STAFF/ADMIN)

### `/outbound/new` — Crear albarán de salida
- Paso 1: datos generales → cliente (selector desde `/api/clients` o datos manuales), fecha, notas
- Paso 2: líneas. Tabla editable con:
  - Línea | Perfil/Ítem (autocomplete catálogo) | Descripción | Cantidad | Precio | IVA (%) | Notas
  - ⚠️ La cantidad no puede superar el stock disponible del perfil/item (consultar en `/api/inventory` o `/api/inventory/{id}`)
  - Si stock insuficiente: mostrar error visual en la línea "Stock insuficiente: disponible X"
- Botón "Guardar borrador"
- Botón "Confirmar salida" → guarda + confirma (elimina stock automáticamente; si falla por stock, mostrar error)

### `/outbound/:id` — Detalle albarán
- Cabecera: número, cliente, fechas, estado, total
- Tabla de líneas
- Botones según estado: igual que inbound
- En estado CONFIRMED: cada línea muestra el stock movement generado (consulta opcional a `/api/inventory/{id}/movements`)

### Estados y transiciones
```
DRAFT ──confirm──→ CONFIRMED
  │
  └──cancel──→ CANCELLED
```

### Reglas de negocio (UI)
- **No se puede confirmar** si alguna línea no tiene stock suficiente. Mostrar error específico: "No hay suficiente stock del perfil X (disponible: Y, necesario: Z)".
- La UI debe consultar el stock disponible antes de permitir confirmar.
- Al confirmar, el backend valida y puede rechazar. La UI debe mostrar el error del backend.

## API Reference

| Método | Endpoint | Request | Response |
|--------|----------|---------|----------|
| GET | `/api/outbound-delivery-notes?page=` | — | `Page<OutboundResponse>` |
| GET | `/api/outbound-delivery-notes/{id}` | — | `OutboundResponse` |
| GET | `/api/outbound-delivery-notes/{id}/lines` | — | `List<OutboundLineResponse>` |
| POST | `/api/outbound-delivery-notes` | `{ customerId?, customerName?, customerVat?, customerAddress?, issueDate?, notes? }` | `OutboundResponse` |
| POST | `/api/outbound-delivery-notes/{id}/lines` | `{ lineNumber, profileId?, itemId?, description, quantity, unitPrice, vatRate, notes? }` | `OutboundLineResponse` |
| DELETE | `/api/outbound-delivery-notes/{id}/lines/{lineId}` | — | `204` |
| POST | `/api/outbound-delivery-notes/{id}/confirm` | — | `OutboundResponse` |
| POST | `/api/outbound-delivery-notes/{id}/cancel` | — | `OutboundResponse` |

### OutboundResponse
```json
{ "id": "uuid", "number": "ALS-2026-A1B2C3D4-001",
  "customerId": "uuid", "customerName": "Construcciones López SL",
  "customerVat": "B12345678", "customerAddress": "Calle Acero 5, Sevilla",
  "issueDate": "2026-06-12", "status": "CONFIRMED",
  "totalAmount": 2500.00, "notes": null, "createdAt": "...", "updatedAt": "..." }
```

### OutboundLineResponse
```json
{ "id": "uuid", "deliveryNoteId": "uuid", "lineNumber": 10,
  "profileId": "uuid", "itemId": null, "description": "HEB 200, 6m cortado",
  "quantity": 5, "unitPrice": 250.00, "vatRate": 21.00, "notes": null }
```
