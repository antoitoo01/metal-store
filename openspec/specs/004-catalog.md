# 004 — Catálogo

## Pantallas

### `/catalog` — Listado de perfiles e items
- Tabs o pestañas: "Perfiles" | "Ítems generales" | "Tipos de ítem"
- Por defecto: "Perfiles"

#### Pestaña "Perfiles"
- Tabla paginada (20 por página) con columnas: Designación | Peso (kg/m) | Familia | Estándar | Shape | Acciones
- Filtros: búsqueda (`q`), familia (`familyCode`), estándar (`standard`), tipo de perfil (`shapeType`)
- Cada fila → clic abre detalle o drawer lateral
- Botón "Añadir imagen" → upload multipart

#### Pestaña "Ítems generales"
- Tabla: SKU | Designación | Tipo | Dimensiones | Peso | Precio estimado
- Filtro: búsqueda (`q`), tipo (`itemType`)

#### Pestaña "Tipos de ítem"
- Solo visible para STAFF/ADMIN
- Tabla + botón "Nuevo tipo" (modal con name + description + schemaDefinition como JSON textarea)

### `/catalog/profiles/:id` — Detalle de perfil
- Información completa del perfil (dimensiones, peso, familia)
- Imagen del perfil (si tiene)
- Galería de imágenes relacionadas (subir/eliminar)
- Precios asociados (tabla desde `/api/billing/prices`)

### `/catalog/items/:id` — Detalle de ítem
- Información completa del ítem
- Imagen (subir/eliminar)

## API Reference

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/catalog/profiles?q=&standard=&shapeType=&familyCode=&page=` | Listar perfiles |
| GET | `/api/catalog/profiles/{id}` | Detalle perfil |
| GET | `/api/catalog/families?standard=` | Listar familias |
| GET | `/api/catalog/items?q=&itemType=&page=` | Listar items |
| GET | `/api/catalog/items/{id}` | Detalle item |
| GET | `/api/catalog/item-types?page=` | Listar tipos |
| POST | `/api/catalog/item-types` | Crear tipo (STAFF) |
| PUT | `/api/catalog/item-types/{id}` | Actualizar tipo (STAFF) |
| DELETE | `/api/catalog/item-types/{id}` | Eliminar tipo (ADMIN) |
| POST | `/api/catalog/profiles/{id}/image` | Subir imagen perfil (multipart) |
| DELETE | `/api/catalog/profiles/{id}/image` | Eliminar imagen perfil |
| POST | `/api/catalog/items/{id}/image` | Subir imagen item (multipart) |
| DELETE | `/api/catalog/items/{id}/image` | Eliminar imagen item |
| GET | `/api/images/{namespace}/{filename}` | Servir imagen |

### CatalogProfileResponse
```json
{ "id": "uuid", "designation": "HEB 200", "weightKgM": 61.3, "areaCm2": 78.1,
  "h": 200.0, "b": 200.0, "tw": 9.0, "tf": 15.0, "r": 18.0,
  "imagePath": null, "familyId": "uuid", "familyStandard": "UNE-EN 10034",
  "familyCode": "HEB", "familyShapeType": "H", "familyDescription": "HEB - IPB",
  "createdAt": "2026-01-15T10:00:00" }
```

### CatalogItemResponse
```json
{ "id": "uuid", "itemType": "SERVICE", "sku": "SERV-001", "designation": "Corte plasma",
  "dimensions": null, "weightKgM": null, "material": null,
  "estimatedPriceKg": 0, "imagePath": null, "createdAt": "..." }
```
