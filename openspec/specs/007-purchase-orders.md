# 007 — Órdenes de Compra (Purchase Orders)

## Pantallas

### `/purchase-orders` — Listado
- Tabla paginada: Número OC | Proveedor | Fecha | Estado | Total | Acciones
- Filtros: búsqueda (`q`), estado (`status: DRAFT/ISSUED/RECEIVED/CANCELLED`), proveedor (`supplierId`)
- Estados con badge: DRAFT=gris, ISSUED=azul, RECEIVED=verde, CANCELLED=rojo
- Botón "+ Nueva OC" (STAFF/ADMIN) → formulario completo (no modal por la complejidad)

### `/purchase-orders/new` — Crear OC
- Paso 1: datos generales: proveedor (selector con autocomplete desde `/api/suppliers` o datos manuales: nombre, VAT, dirección), fecha esperada, notas
- Paso 2: líneas de OC. Tabla editable con:
  - Línea | Perfil/Ítem (selector | `profileId`/`itemId`) | Descripción (autocompletada si se selecciona perfil/item) | Cantidad | Precio unitario | IVA (%) | Total línea
  - Botón "+ Añadir línea"
  - Cada línea: input directo en la tabla (editable inline) o botón "Editar" que abre modal para esa línea
- Botón "Guardar como borrador" → `POST /api/purchase-orders` + `POST /api/purchase-orders/{id}/lines` por cada línea
- Botón "Emitir OC" → guarda + emite (`POST /api/purchase-orders/{id}/issue`)

### `/purchase-orders/:id` — Detalle OC
- Cabecera: número OC, proveedor, fechas, estado, totales (subtotal, IVA, total)
- Tabla de líneas: línea | Descripción | Cantidad | Precio | IVA | Total
- Botones de acción según estado:
  - DRAFT: Editar, Emitir, Cancelar
  - ISSUED: Marcar como Recibida (solo si tiene albarán de entrada asociado — ver PO-0003)
  - RECEIVED: (solo lectura)
  - CANCELLED: (solo lectura)
- Al editar en estado DRAFT: se redirige a misma pantalla `/purchase-orders/:id/edit` con los datos precargados y `PUT /api/purchase-orders/{id}`

### Estados y transiciones
```
DRAFT ──issue──→ ISSUED ──receive──→ RECEIVED
  │                                      │
  └──cancel──→ CANCELLED ←──cancel──────┘
```
- Register: no necesita estar autenticado

## API Reference

| Método | Endpoint | Request | Response |
|--------|----------|---------|----------|
| GET | `/api/purchase-orders?q=&status=&supplierId=&page=` | — | `Page<PurchaseOrderResponse>` |
| GET | `/api/purchase-orders/{id}` | — | `PurchaseOrderResponse` |
| POST | `/api/purchase-orders` | `{ supplierId?, supplierName?, supplierVat?, supplierAddress?, expectedDate?, notes? }` | `PurchaseOrderResponse` |
| PUT | `/api/purchase-orders/{id}` | `{ supplierId?, supplierName?, supplierVat?, supplierAddress?, expectedDate?, notes? }` | `PurchaseOrderResponse` |
| GET | `/api/purchase-orders/{id}/lines` | — | `List<POLineResponse>` |
| POST | `/api/purchase-orders/{id}/lines` | `{ lineNumber, profileId?, itemId?, description, quantity, unitPrice, vatRate }` | `POLineResponse` |
| DELETE | `/api/purchase-orders/{poId}/lines/{lineId}` | — | `204` |
| POST | `/api/purchase-orders/{id}/issue` | — | `PurchaseOrderResponse` |
| POST | `/api/purchase-orders/{id}/receive` | — | `PurchaseOrderResponse` |
| POST | `/api/purchase-orders/{id}/cancel` | — | `PurchaseOrderResponse` |

### PurchaseOrderResponse
```json
{ "id": "uuid", "organizationId": "uuid", "poNumber": "OC-2026-A1B2C3D4-001",
  "supplierId": "uuid", "supplierName": "Aceros del Sur SL", "supplierVat": "B87654321",
  "supplierAddress": "Pol. Ind. Store, nave 7, Sevilla",
  "issueDate": "2026-06-01", "expectedDate": "2026-06-15",
  "status": "ISSUED", "subtotal": 1250.00, "vatTotal": 262.50,
  "total": 1512.50, "notes": null, "createdAt": "...", "updatedAt": "..." }
```

### PurchaseOrderLineResponse
```json
{ "id": "uuid", "poId": "uuid", "lineNumber": 10,
  "profileId": null, "itemId": null, "description": "HEB 200, 6m",
  "quantity": 10, "unitPrice": 125.00, "vatRate": 21.00, "totalPrice": 1250.00 }
```
