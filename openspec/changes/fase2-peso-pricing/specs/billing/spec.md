# Delta for Billing

## ADDED Requirements

### Requirement: Endpoint de precio sugerido

El sistema MUST exponer `GET /api/billing/prices/suggested` que calcule un precio sugerido para un perfil basado en las reglas de pricing activas, sin persistir nada.

#### Scenario: Consultar precio sugerido con regla de perfil

- GIVEN una `PricingRule` para el perfil HEB 200 con `markupPercent = 20` y `basePricePerKg = 1.50`
- WHEN `GET /api/billing/prices/suggested?profileId=X&quantity=5`
- THEN se devuelve `{ profileId, calculatedWeightKgM: 61.3, basePricePerKg: 1.50, markupPercent: 20, unitPrice: 1.80, totalPrice: 551.70 }`

#### Scenario: Consultar sin regla configurada

- GIVEN no hay `PricingRule` para el perfil ni su familia
- WHEN se consulta `GET /api/billing/prices/suggested?profileId=X`
- THEN respuesta con `unitPrice = null` y mensaje indicando que no hay regla
