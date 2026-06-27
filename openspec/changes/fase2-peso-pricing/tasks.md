# Tasks: Cálculo de peso automático y pricing inteligente

## Review Workload Forecast

| Field | Value |
|-------|-------|
| Estimated changed lines | ~600 |
| 400-line budget risk | Medium |
| Chained PRs recommended | Yes |
| Suggested split | PR 1 (auto-weight) → PR 2a (PricingRule CRUD) → PR 2b (calculator + suggested) |
| Decision needed before apply | No (auto-weight independiente, pricing divisible) |

```text
Decision needed before apply: No
Chained PRs recommended: Yes
Chain strategy: stacked-to-main
400-line budget risk: Medium
```

## Phase 1: Auto-weight (PR 1)

- [x] 1.1 Crear `shared/domain/MaterialType.kt` con enum STEEL(7850), ALUMINIUM(2700), STAINLESS_STEEL(7930), GALVANIZED(7850)
- [x] 1.2 Modificar `CatalogProfile.kt`: añadir columna `materialType: MaterialType?` + método `getCalculatedWeightKgM()`
- [x] 1.3 Modificar `CatalogProfileResponse.kt`: añadir `calculatedWeightKgM` + `materialType`
- [x] 1.4 Tests: `getCalculatedWeightKgM()` para cada material, sin areaCm2, sin materialType (default STEEL) — 6 tests, todos verdes
- [x] 4.1 `gradlew test --tests *CatalogProfile*` — 14/14 tests passing

## Phase 2: PricingRule infrastructure (PR 2a)

- [ ] 2.1 Crear `pricing/domain/entity/PricingRule.kt` con id, organizationId, profileId?, familyId?, markupPercent, basePricePerKg?, priority
- [ ] 2.2 Crear DTOs: `CreatePricingRuleRequest`, `UpdatePricingRuleRequest`, `PricingRuleResponse`
- [ ] 2.3 Crear `PricingRuleRepository` (JPA, org-scoped queries)
- [ ] 2.4 Crear `PricingRuleService` con CRUD completo + AuditLogger
- [ ] 2.5 Crear `PricingRuleController` con endpoints GET/POST/PUT/DELETE + @RequiresRole
- [ ] 2.6 Tests: CRUD integration + org-scoped isolation + role security

## Phase 3: PricingCalculator + suggested endpoint (PR 2b)

- [ ] 3.1 Crear `PricingCalculator` con lógica: buscar regla por profileId → familyId → global (priority), computar `unitPrice = basePricePerKg × (1 + markupPercent/100)`, `totalPrice = unitPrice × lengthM`. Fallback a `CatalogItem.estimatedPriceKg` si basePricePerKg es null.
- [ ] 3.2 Añadir `GET /api/billing/prices/suggested` en `BillingController` con query params profileId, lengthM
- [ ] 3.3 Tests: resolución de reglas (perfil > familia > global), suggested endpoint, sin regla configurada

## Phase 4: Verify

- [ ] 4.1 Ejecutar `gradlew build` y verificar que todos los tests pasan
