# 011 — Presupuestos (Quotes)

## Pantallas

### `/quotes` — Listado
- Tabla paginada: Número | Cliente | Fecha | Válido hasta | Estado | Total | Acciones
- Filtros: búsqueda (`q`), estado (`status: DRAFT/ISSUED/ACCEPTED/REJECTED/CANCELLED`), cliente (`clientId`)
- Estados con badge: DRAFT=gris, ISSUED=azul, ACCEPTED=verde, REJECTED=rojo, CANCELLED=gris oscuro
- Botón "+ Nuevo presupuesto" (STAFF/ADMIN) → formulario completo

### `/quotes/new` — Crear presupuesto
- Paso 1: datos generales → cliente (selector desde `/api/clients` o manual), válido hasta, notas
- Paso 2: líneas. Tabla editable igual que purchase orders:
  - Línea | Perfil/Ítem | Descripción | Cantidad | Precio unitario | IVA (%) | Total
  - Al seleccionar perfil, sugerir precio desde `/api/billing/prices`
- Botones: "Guardar borrador", "Guardar y emitir" (issue)

### `/quotes/:id` — Detalle presupuesto
- Cabecera: número, cliente, fechas, estado, totales
- Tabla de líneas
- Botones según estado:
  - DRAFT: Editar, Emitir, Cancelar
  - ISSUED: Aceptar, Rechazar, Cancelar
  - ACCEPTED: (solo lectura — posible botón "Crear albarán de salida" en futuro)
  - REJECTED / CANCELLED: (solo lectura)

### Estados y transiciones
```
DRAFT ──issue──→ ISSUED ──accept──→ ACCEPTED
                  │   │
                  │   └──reject──→ REJECTED
                  │
                  └──cancel──→ CANCELLED
```

## API Reference

| Método | Endpoint | Request | Response |
|--------|----------|---------|----------|
| GET | `/api/quotes?q=&status=&clientId=&page=` | — | `Page<QuoteResponse>` |
| GET | `/api/quotes/{id}` | — | `QuoteResponse` |
| POST | `/api/quotes` | `{ clientId?, customerName?, customerVat?, customerAddress?, validUntil?, notes? }` | `QuoteResponse` |
| PUT | `/api/quotes/{id}` | `{ customerName?, customerVat?, customerAddress?, validUntil?, notes? }` | `QuoteResponse` |
| GET | `/api/quotes/{id}/lines` | — | `List<QuoteLineResponse>` |
| POST | `/api/quotes/{id}/lines` | `{ lineNumber, profileId?, itemId?, description, quantity, unitPrice, vatRate }` | `QuoteLineResponse` |
| DELETE | `/api/quotes/{quoteId}/lines/{lineId}` | — | `204` |
| POST | `/api/quotes/{id}/issue` | — | `QuoteResponse` |
| POST | `/api/quotes/{id}/accept` | — | `QuoteResponse` |
| POST | `/api/quotes/{id}/reject` | — | `QuoteResponse` |
| POST | `/api/quotes/{id}/cancel` | — | `QuoteResponse` |

### QuoteResponse
```json
{ "id": "uuid", "organizationId": "uuid", "quoteNumber": "P-2026-0001",
  "clientId": "uuid", "customerName": "Construcciones López SL",
  "customerVat": "B12345678", "customerAddress": "Calle Acero 5, Sevilla",
  "issueDate": "2026-06-01", "validUntil": "2026-07-01",
  "status": "ISSUED", "subtotal": 3500.00, "vatTotal": 735.00,
  "total": 4235.00, "notes": null }
```

### QuoteLineResponse
```json
{ "id": "uuid", "quoteId": "uuid", "lineNumber": 10,
  "profileId": "uuid", "itemId": null,
  "description": "Fabricación e instalación de estructura metálica HEB 200, 12 unidades",
  "quantity": 12, "unitPrice": 291.67, "vatRate": 21.00, "totalPrice": 3500.00 }
```
