# Delta for Catalog

## MODIFIED Requirements

En el detalle de perfil y listado de catálogo, se modifica el comportamiento del peso y se añade material.

### Requirement: Cálculo de peso automático

El sistema MUST exponer el peso calculado (`calculatedWeightKgM`) como campo separado del peso almacenado (`weightKgM`) en `CatalogProfileResponse`. El cálculo MUST ser `areaCm2 × material.density / 10000` cuando `areaCm2` esté presente. El `materialType` MUST ser configurable por perfil (nullable, default `STEEL`).
(Previously: `weightKgM` era manual, sin material asociado)

#### Scenario: Perfil con areaCm2 y material acero

- GIVEN un perfil HEB 200 con `areaCm2 = 78.1` y `materialType = STEEL`
- WHEN se consulta `GET /api/catalog/profiles/{id}`
- THEN `calculatedWeightKgM` = `78.1 × 7850 / 10000 = 61.3 kg/m`
- AND `weightKgM` almacenado (si existe) no se modifica

#### Scenario: Perfil sin areaCm2

- GIVEN un perfil sin `areaCm2`
- WHEN se consulta el detalle
- THEN `calculatedWeightKgM` devuelve `null`

#### Scenario: Perfil con materialType aluminio

- GIVEN un perfil con `areaCm2 = 78.1` y `materialType = ALUMINIUM`
- WHEN se consulta el detalle
- THEN `calculatedWeightKgM` = `78.1 × 2700 / 10000 = 21.1 kg/m`

### MODIFIED API: CatalogProfileResponse

```json
{ "id": "uuid", "designation": "HEB 200", "weightKgM": 61.3, "calculatedWeightKgM": 61.3,
  "materialType": "STEEL", "areaCm2": 78.1,
  "h": 200.0, "b": 200.0, "tw": 9.0, "tf": 15.0, "r": 18.0,
  "imagePath": null, "familyId": "uuid", "familyStandard": "UNE-EN 10034",
  "familyCode": "HEB", "familyShapeType": "H", "familyDescription": "HEB - IPB",
  "createdAt": "2026-01-15T10:00:00" }
```
