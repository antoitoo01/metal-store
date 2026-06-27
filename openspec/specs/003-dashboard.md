# 003 — Dashboard

## Pantalla: `/dashboard`

Resumen de la actividad reciente con tarjetas informativas.

### Widgets

1. **Inventario bajo mínimos**: conteo de items con cantidad < umbral (parámetro configurable). Enlace → `/inventory`
2. **OC pendientes**: número de purchase orders en estado DRAFT o ISSUED. Enlace → `/purchase-orders`
3. **Albaranes de entrada hoy**: conteo de inbound delivery notes creadas hoy. Enlace → `/inbound`
4. **Albaranes de salida hoy**: conteo de outbound delivery notes creadas hoy. Enlace → `/outbound`
5. **Presupuestos activos**: presupuestos en DRAFT o ISSUED. Enlace → `/quotes`
6. **Facturas pendientes**: facturas ISSUED (sin pagar). Enlace → `/billing`

### Datos
- No hay endpoint específico de dashboard. Cada widget hace una llamada a su listado correspondiente con parámetros concretos (status filter, date range).
- Se cargan en paralelo al montar el componente.
- Estado: loading skeleton mientras se resuelven todas las peticiones.
