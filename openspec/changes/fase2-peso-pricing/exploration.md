# Exploration: Cálculo de peso automático y pricing inteligente

## Current State

**Peso:**
- `CatalogProfile`: tiene `weightKgM` (stored, nullable, precision 10 scale 4) y `areaCm2` (stored, nullable)
- `CatalogItem`: tiene `weightKgM` y `estimatedPriceKg`
- `EuroProfile` y `AiscProfile` heredan de `CatalogProfile` con dimensiones (h, b, tw, tf, r)
- **weightKgM se introduce manualmente, nunca se calcula**

**Pricing:**
- `PriceListItem.unitPrice` es precio plano (precision 12 scale 4)
- No hay markup ni relación con peso del perfil
- Los precios se crean manualmente vía `POST /api/billing/prices`
- `CatalogItem.estimatedPriceKg` existe pero no se usa en pricing

**Uso en líneas:**
- `QuoteLine`, `PurchaseOrderLine`, `InboundDeliveryNoteLine`, `OutboundDeliveryNoteLine`
- Todas tienen `unitPrice` plano, `profileId`, `itemId`
- Ninguna transporta peso calculado ni referencia a precio sugerido

## Approaches

### Feature 1: Auto-weight

1. **Domain method + service** — Añadir `getCalculatedWeightKgM(): BigDecimal?` en `CatalogProfile` que calcule desde `areaCm2 × 0.785`. Si no hay `areaCm2`, devolver `null`.
   - Pros: Sencillo, sin nueva entidad, testable
   - Cons: Solo funciona si `areaCm2` está poblado
   - Effort: Bajo

2. **Geometric calculation** — Calcular área desde h,b,tw,tf usando fórmula de área de perfil
   - Pros: No depende de `areaCm2`
   - Cons: Complejo por cada forma (I, C, L, U, T, circular), frágil
   - Effort: Alto

### Feature 2: Smart Pricing

1. **PricingRule entity** — Nueva entidad `PricingRule` con `profileId?`, `familyId?`, `markupPercent`, `basePricePerKg?`. Servicio `PricingCalculator` que compute: `suggestedPrice = weightKgM × basePricePerKg × (1 + markupPercent/100)`.
   - Pros: Flexible, configurable por perfil/familia/global, desacoplado
   - Cons: Nueva entidad + CRUD + migración
   - Effort: Medio

2. **Extend PriceListItem** — Añadir `markupPercent`, `basePricePerKg` a `PriceListItem` y calcular en getter
   - Pros: Sin nueva tabla
   - Cons: Mezcla responsabilidades, viola SRP, lógica de negocio en entidad
   - Effort: Bajo pero mala práctica

3. **Inline calculator** — Endpoint GET `/api/billing/prices/suggested?profileId=X&quantity=Y` que calcule sobre la marcha sin persistencia
   - Pros: Simple, sin entidades nuevas
   - Cons: No persiste reglas, cada vez hay que pasar parámetros
   - Effort: Bajo

## Recommendation

**Feature 1**: Approach 1 (domain method). Añadir `getCalculatedWeightKgM()` en `CatalogProfile` que calcule desde `areaCm2 × STEEL_DENSITY_FACTOR(0.785)`. Como fallback, si no hay `areaCm2` ni `weightKgM`, devolver `null`. Exponer en `CatalogProfileResponse` como `calculatedWeightKgM`.

**Feature 2**: Approach 1 (PricingRule entity). Es la solución correcta a largo plazo: permite configurar markup por perfil/familia y calcular precios sugeridos. Incluir:
- Entidad `PricingRule`: `id`, `organizationId`, `profileId?`, `familyId?`, `markupPercent`, `basePricePerKg?`, `priority`
- Servicio `PricingCalculator`: busca regla por perfil → familia → global, calcula precio sugerido
- Endpoints CRUD para `PricingRule` (STAFF/ADMIN)
- Endpoint GET `/api/billing/prices/suggested?profileId=X&quantity=Y&lengthM=Z` para UI

## Risks
- `areaCm2` puede no estar poblado en todos los perfiles existentes → necesitamos migración de datos
- La regla de pricing puede ser demasiado simple para casos reales (descuentos por volumen, cliente VIP)
- Los datos existentes de `weightKgM` pueden no coincidir con el cálculo → posible discrepancia

## Ready for Proposal
Sí. Pasos claros y effort bajo/medio.
