# Design: Cálculo de peso automático y pricing inteligente

## Technical Approach

Dos features independientes pero relacionadas: (1) cálculo de peso desde área × densidad del material en `CatalogProfile`, (2) pricing basado en peso × markup mediante nueva entidad `PricingRule` en contexto `pricing/`.

## Architecture Decisions

### Decision: MaterialType como enum en shared

**Choice**: `enum class MaterialType` en `shared/domain/` con densidad en kg/m³ como propiedad
**Alternatives**: String en base de datos con lookup table, interface polimórfica
**Rationale**: Los materiales son fijos y conocidos (acero, aluminio, inoxidable, galvanizado). Un enum con `val densityKgM3: BigDecimal` es la forma más expresiva y segura en Kotlin. No requiere tabla extra.

### Decision: Calculated weight NO se persiste

**Choice**: `getCalculatedWeightKgM()` es método transient en la entidad. Se calcula en memoria en el response DTO.
**Alternatives**: Persistir peso calculado con trigger al guardar
**Rationale**: El peso es derivado de `areaCm2 × material.density`. Persistirlo violaría DRY. Si cambia el material, el cálculo se actualiza solo.

### Decision: PricingRule como bounded context separado

**Choice**: Nuevo paquete `pricing/` con su propio controlador, servicio, entidad, repositorio, DTOs
**Alternatives**: Meter en `billing/`, meter en `catalog/`
**Rationale**: Pricing es un concepto de negocio independiente. Separarlo sigue el patrón de bounded contexts del proyecto (inbound, outbound, purchase, billing). El endpoint `/api/billing/prices/suggested` es solo una fachada que delega en `PricingCalculator`.

## Data Flow

```
1. Auto-weight:
   GET /api/catalog/profiles/{id}
   → CatalogProfileController
   → CatalogProfileResponse.from(profile)
   → profile.getCalculatedWeightKgM()  // areaCm2 × material.density / 10000
   → response.calculatedWeightKgM = result

2. Suggested price:
   GET /api/billing/prices/suggested?profileId=X&lengthM=Y
   → BillingController → PricingCalculator.calculate(profileId, lengthM)
   → 1. Busca PricingRule: profileId → familyId → global (priority)
   → 2. Obtiene weightKmM = profile.getCalculatedWeightKgM()
   → 3. Computa: unitPrice = basePricePerKg × (1 + markupPercent/100)
   → 4. totalPrice = unitPrice × lengthM
```

## File Changes

| File | Action | Description |
|------|--------|-------------|
| `shared/domain/MaterialType.kt` | Create | Enum STEEL(7850), ALUMINIUM(2700), STAINLESS_STEEL(7930), GALVANIZED(7850) |
| `catalog/domain/entity/CatalogProfile.kt` | Modify | + materialType column, + getCalculatedWeightKgM() |
| `catalog/domain/dto/response/CatalogProfileResponse.kt` | Modify | + calculatedWeightKgM, + materialType field |
| `pricing/domain/entity/PricingRule.kt` | Create | Entidad con profileId?, familyId?, markupPercent, basePricePerKg?, priority |
| `pricing/domain/dto/request/CreatePricingRuleRequest.kt` | Create | DTO de creación |
| `pricing/domain/dto/request/UpdatePricingRuleRequest.kt` | Create | DTO de actualización |
| `pricing/domain/dto/response/PricingRuleResponse.kt` | Create | DTO de respuesta |
| `pricing/domain/repository/PricingRuleRepository.kt` | Create | JPA repository |
| `pricing/application/PricingCalculator.kt` | Create | Lógica de resolución de reglas + cálculo |
| `pricing/application/PricingRuleService.kt` | Create | CRUD service |
| `pricing/controller/PricingRuleController.kt` | Create | CRUD endpoints |
| `billing/controller/BillingController.kt` | Modify | + GET /prices/suggested |

## Interfaces / Contracts

### MaterialType
```kotlin
enum class MaterialType(val densityKgM3: BigDecimal) {
    STEEL(BigDecimal("7850")),
    ALUMINIUM(BigDecimal("2700")),
    STAINLESS_STEEL(BigDecimal("7930")),
    GALVANIZED(BigDecimal("7850"))
}
```

### Weight calculation
```kotlin
fun CatalogProfile.getCalculatedWeightKgM(): BigDecimal? =
    areaCm2?.let { area ->
        val material = materialType ?: MaterialType.STEEL
        area.multiply(material.densityKgM3).divide(BigDecimal("10000"), 4, RoundingMode.HALF_UP)
    }
```

### PricingRule entity
```kotlin
@Entity @Table(name = "pricing_rules")
class PricingRule(
    @Id val id: UUID,
    @Column(nullable = false) val organizationId: UUID,
    val profileId: UUID? = null,
    val familyId: UUID? = null,
    @Column(nullable = false, precision = 6, scale = 2) val markupPercent: BigDecimal,
    @Column(precision = 12, scale = 4) val basePricePerKg: BigDecimal? = null,
    @Column(nullable = false) val priority: Int = 0
) : BaseEntity()
```

### PricingCalculator
```kotlin
fun calculate(organizationId: UUID, profileId: UUID, lengthM: BigDecimal): SuggestedPriceResult
// Resuelve: profile → family → global, computa unitPrice = basePricePerKg × (1 + markupPercent/100)
```

## Testing Strategy

| Layer | What to Test | Approach |
|-------|-------------|----------|
| Unit | `getCalculatedWeightKgM()` para cada material | JUnit 5, assert compareTo |
| Unit | `PricingCalculator` resolución de reglas (perfil > familia > global) | JUnit 5 with mocked repository |
| Integration | CRUD pricing rules + org-scoped isolation | `@WebMvcTest` + H2 (patrón inbound/outbound tests) |
| Integration | `GET /api/billing/prices/suggested` | Delegated integration test |

## Migration

No migration required. `materialType` columna nullable en `catalog_profiles` (default null = STEEL en getter). `pricing_rules` es tabla nueva.
