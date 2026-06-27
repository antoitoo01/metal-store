# 010 — Inventario

## Pantallas

### `/inventory` — Listado de stock
- Tabla paginada: Perfil/Ítem | Ubicación | Cantidad | Precio coste | Proveedor | Recibido | Acciones
- Filtros: búsqueda (`q`), columna de búsqueda visible
- Cantidad con formato: si es negativa o cero, texto en rojo
- Botón "+ Nueva entrada" (modal): seleccionar perfil o ítem, cantidad, ubicación, precio coste, proveedor, notas → `POST /api/inventory`
- Acciones por fila:
  - Ver movimientos → navega o abre drawer con historial
  - Añadir stock (modal) → `POST /api/inventory/{id}/add-stock`
  - Retirar stock (modal) → `POST /api/inventory/{id}/remove-stock`
  - Editar (modal) → `PUT /api/inventory/{id}`
  - Eliminar (ADMIN, confirmación) → `DELETE /api/inventory/{id}`

### `/inventory/:id` — Detalle de item de inventario
- Cabecera: perfil/ítem, cantidad actual, ubicación
- Totales: coste total estimado (cantidad × costPriceEur)
- Botones: Añadir stock, Retirar stock, Editar
- Historial de movimientos (tabla): Fecha | Tipo (ENTRADA/SALIDA) | Cantidad | Cantidad anterior | Nueva cantidad | Referencia | Notas
  - Tipo con badge: INBOUND=verde, OUTBOUND=rojo, ADJUSTMENT=azul
  - La referencia enlaza al albarán correspondiente si aplica

### Modal "Añadir/Retirar stock"
- Campo cantidad (solo positivo)
- Campo notas (opcional)
- Botón confirmar → loading, luego refrescar
- En retirar: validar que la cantidad no supere el stock actual

## API Reference

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/inventory?page=` | Listar inventario |
| GET | `/api/inventory/{id}` | Detalle item |
| POST | `/api/inventory` | Crear item (STAFF) |
| PUT | `/api/inventory/{id}` | Actualizar item (STAFF) |
| DELETE | `/api/inventory/{id}` | Eliminar item (ADMIN) |
| GET | `/api/inventory/{id}/movements?page=` | Historial movimientos |
| POST | `/api/inventory/{id}/add-stock` | Añadir stock (STAFF) |
| POST | `/api/inventory/{id}/remove-stock` | Retirar stock (STAFF) |

### ItemResponse
```json
{ "id": "uuid", "organizationId": "uuid",
  "profileId": "uuid", "itemId": null,
  "quantity": 50.0000, "location": "Almacén A - Estantería 3",
  "costPriceEur": 125.00, "supplier": "Aceros del Sur SL",
  "receivedAt": "2026-06-10T10:00:00", "notes": null }
```

### MovementResponse
```json
{ "id": "uuid", "inventoryItemId": "uuid",
  "movementType": "INBOUND", "quantity": 50.0000,
  "referenceType": "DELIVERY_NOTE", "referenceId": "uuid",
  "previousQuantity": 0.0000, "newQuantity": 50.0000,
  "notes": "Entrada por albarán AL-...", "performedAt": "..." }
```
