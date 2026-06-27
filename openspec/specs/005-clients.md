# 005 — Clientes

## Pantallas

### `/clients` — Listado
- Tabla paginada: Nombre | Email | Teléfono | VAT | Estado | Acciones
- Filtro: búsqueda textual (`q`), filtro por estado (`status: ACTIVE/INACTIVE`)
- Estados con badge de color: ACTIVE=verde, INACTIVE=rojo
- Botón "+ Nuevo cliente" (STAFF/ADMIN) → modal con: nombre (obligatorio), email, teléfono, dirección, VAT, notas
- Acciones por fila: Editar (modal), Activar/Desactivar (toggle), Eliminar (ADMIN, confirmación)
- Al eliminar: `DELETE /api/clients/{id}` → 204, quitar de la lista

### `/clients/:id` — Detalle (opcional, puede ser drawer)
- Toda la info del cliente
- Historial de presupuestos asociados (desde `/api/quotes?clientId=`)
- Historial de albaranes de salida asociados (desde `/api/outbound-delivery-notes?q=`)
- Botones: Editar, Activar/Desactivar, Eliminar

## API Reference

| Método | Endpoint | Request | Response |
|--------|----------|---------|----------|
| GET | `/api/clients?q=&status=&page=` | — | `Page<ClientResponse>` |
| GET | `/api/clients/{id}` | — | `ClientResponse` |
| POST | `/api/clients` | `{ name, email?, phone?, address?, vatNumber?, notes? }` | `ClientResponse` |
| PUT | `/api/clients/{id}` | `{ name?, email?, phone?, address?, vatNumber?, notes?, status? }` | `ClientResponse` |
| DELETE | `/api/clients/{id}` | — | `204` |
| POST | `/api/clients/{id}/activate` | — | `ClientResponse` |
| POST | `/api/clients/{id}/deactivate` | — | `ClientResponse` |

### ClientResponse
```json
{ "id": "uuid", "organizationId": "uuid", "name": "Construcciones López SL",
  "email": "info@lopez.com", "phone": "954001122", "address": "Calle Acero 5, Sevilla",
  "vatNumber": "B12345678", "notes": null, "status": "ACTIVE",
  "createdAt": "...", "updatedAt": "..." }
```
