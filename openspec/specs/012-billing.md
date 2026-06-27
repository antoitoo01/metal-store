# 012 — Facturación (Billing)

## Pantallas

### `/billing` — Listado de facturas
- Tabla paginada: Número | Cliente | Fecha | Vencimiento | Estado | Total | Acciones
- Filtros: búsqueda (`q`), estado (`status: DRAFT/ISSUED/PAID/CANCELLED`)
- Estados con badge: DRAFT=gris, ISSUED=azul, PAID=verde, CANCELLED=rojo
- Botón "+ Nueva factura" (STAFF/ADMIN)

### `/billing/new` — Crear factura
- Paso 1: datos generales: nombre cliente, VAT, dirección (manual, no selector — las facturas pueden ser a clientes no registrados)
- Paso 2: líneas. Tabla editable:
  - Línea | Perfil/Ítem (autocomplete) | Descripción | Cantidad | Precio | IVA | Total
- Botones: "Guardar borrador", "Guardar y emitir"

### `/billing/invoices/:id` — Detalle factura
- Cabecera: número factura, cliente, fechas, estado, totales
- Tabla de líneas
- Botones según estado:
  - DRAFT: Editar, Emitir, Cancelar
  - ISSUED: Marcar como pagada, Cancelar
  - PAID / CANCELLED: (solo lectura)
- PDF descargable (a implementar en backend futuro)

### Precios (pestaña adicional en `/billing`)
### `/billing/prices` — Lista de precios
- Tabla: Perfil/Ítem | Precio unitario | Válido desde | Válido hasta | Notas | Acciones
- Botón "+ Nuevo precio" (STAFF) → modal: seleccionar perfil o ítem, precio, vigencia, notas
- Acciones: Editar, Eliminar (ADMIN)

## API Reference

### Invoices
| Método | Endpoint | Request | Response |
|--------|----------|---------|----------|
| GET | `/api/billing/invoices?q=&status=&page=` | — | `Page<InvoiceResponse>` |
| GET | `/api/billing/invoices/{id}` | — | `InvoiceResponse` |
| POST | `/api/billing/invoices` | `{ customerName?, customerVat? }` | `InvoiceResponse` |
| PUT | `/api/billing/invoices/{id}` | `{ customerName?, customerVat?, customerAddress?, notes? }` | `InvoiceResponse` |
| GET | `/api/billing/invoices/{id}/lines` | — | `List<LineResponse>` |
| POST | `/api/billing/invoices/{id}/lines` | `{ lineNumber, profileId?, itemId?, description, quantity, unitPrice, vatRate }` | `LineResponse` |
| DELETE | `/api/billing/invoices/{invoiceId}/lines/{lineId}` | — | `204` |
| POST | `/api/billing/invoices/{id}/issue` | — | `InvoiceResponse` |
| POST | `/api/billing/invoices/{id}/pay` | — | `InvoiceResponse` |
| POST | `/api/billing/invoices/{id}/cancel` | — | `InvoiceResponse` |

### Prices
| Método | Endpoint | Request | Response |
|--------|----------|---------|----------|
| GET | `/api/billing/prices?page=` | — | `Page<PriceResponse>` |
| POST | `/api/billing/prices` | `{ profileId?, itemId?, unitPrice, validFrom?, validTo?, notes? }` | `PriceResponse` |
| PUT | `/api/billing/prices/{id}` | `{ unitPrice?, validFrom?, validTo?, notes? }` | `PriceResponse` |
| DELETE | `/api/billing/prices/{id}` | — | `204` |

### InvoiceResponse
```json
{ "id": "uuid", "organizationId": "uuid", "invoiceNumber": "F-2026-0001",
  "customerName": "Construcciones López SL", "customerVat": "B12345678",
  "customerAddress": "Calle Acero 5, Sevilla",
  "issueDate": "2026-06-15", "dueDate": "2026-07-15",
  "status": "ISSUED", "subtotal": 4235.00, "vatTotal": 889.35,
  "total": 5124.35, "notes": null }
```

### PriceResponse
```json
{ "id": "uuid", "organizationId": "uuid",
  "profileId": "uuid", "itemId": null,
  "unitPrice": 125.00, "validFrom": "2026-01-01", "validTo": null, "notes": null }
```
