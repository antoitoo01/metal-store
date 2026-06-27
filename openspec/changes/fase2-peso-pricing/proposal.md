# Proposal: Cálculo de peso automático y pricing inteligente

## Intent

El catálogo de perfiles almacena `weightKgM` manualmente, sin relación con el material. El `PriceListItem.unitPrice` es plano, sin markup configurable ni relación con el peso. Necesitamos calcular peso teórico desde área × densidad del material, y precios sugeridos desde peso × markup.

## Scope

### In Scope
- `MaterialType` enum con densidades (STEEL, ALUMINIUM, STAINLESS_STEEL, GALVANIZED)
- `materialType` en `CatalogProfile` (nullable, default STEEL)
- Método `getCalculatedWeightKgM()`: `areaCm2 × material.density / 10000`
- `calculatedWeightKgM` en `CatalogProfileResponse`
- Entidad `PricingRule`: profileId?, familyId?, markupPercent, basePricePerKg?, priority
- CRUD de `PricingRule` (controlador + servicio + DTOs)
- `PricingCalculator` que resuelve regla por perfil → familia → global
- Endpoint `GET /api/billing/prices/suggested?profileId=X&quantity=Y`
- Tests de unidad + integración

### Out of Scope
- MaterialType en `CatalogItem` (ya tiene `material` libre)
- Precios por volumen (descuentos escalonados)
- Integración con ERP de proveedor para precio base automático
- Portal de cliente

## Capabilities

### New Capabilities
- `pricing-rules`: Gestión de reglas de precio (markup por perfil/familia) + cálculo de precio sugerido

### Modified Capabilities
- `catalog`: Añadir `materialType` a perfil, exponer `calculatedWeightKgM` en response
- `billing`: Añadir endpoint `GET /prices/suggested`

## Approach

1. `MaterialType` enum con densidad (kg/m³) en `shared/domain/`
2. `@Column materialType` en `CatalogProfile` + migración datos existentes a STEEL
3. `CatalogProfile.getCalculatedWeightKgM()`: `areaCm2?.let { it * material.density / 10000 }`
4. Exponer como `calculatedWeightKgM` en response DTO
5. `PricingRule` entidad en nuevo contexto `pricing/`
6. `PricingCalculator` que busca mejor regla y computa: `weightKgM × lengthM × basePricePerKg × (1 + markupPercent/100)`
7. CRUD + suggested endpoint

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `shared/domain/MaterialType.kt` | New | Enum con densidades |
| `catalog/domain/entity/CatalogProfile.kt` | Modified | +materialType, +getCalculatedWeightKgM() |
| `catalog/domain/dto/response/CatalogProfileResponse.kt` | Modified | +calculatedWeightKgM |
| `catalog/api/CatalogProfileController.kt` | Modified | Aceptar materialType en queries |
| `pricing/` | New | Entidad, servicio, controlador, DTOs, repo |
| `billing/controller/BillingController.kt` | Modified | +GET /prices/suggested |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| `areaCm2` null en perfiles existentes | Medium | getCalculatedWeightKgM() devuelve null, no rompe |
| Precios existentes no coinciden con nuevo cálculo | High | suggestedPrice es solo sugerencia, no reemplaza |
| Regla de pricing ambigua (perfil vs familia) | Low | priority resuelve, perfil > familia > global |

## Rollback Plan

Eliminar columna `material_type` de `catalog_profiles`, eliminar tablas `pricing_rules`, revertir cambios en DTOs y controladores.

## Success Criteria

- [ ] `GET /api/catalog/profiles/{id}` devuelve `calculatedWeightKgM` correcto para acero (ej: HEB 200 area=78.1cm² → 61.3 kg/m)
- [ ] `GET /api/catalog/profiles/{id}` cambia calculatedWeightKgM al cambiar materialType
- [ ] CRUD de pricing rules funciona con org-scoped isolation
- [ ] `GET /api/billing/prices/suggested?profileId=X&quantity=5` devuelve precio correcto
- [ ] Tests unitarios + integración pasan
