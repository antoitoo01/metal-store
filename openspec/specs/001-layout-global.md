# 001 — Layout global y navegación

## Estructura

```
/login             → AuthComponent (página completa, sin sidebar)
/register          → AuthComponent
/accept-invitation → AuthComponent
/*                 → LayoutComponent (sidebar + header + router-outlet)
  /dashboard       → DashboardComponent
  /catalog         → CatalogModule (lazy)
  /clients         → ClientModule (lazy)
  /suppliers       → SupplierModule (lazy)
  /purchase-orders → PurchaseOrderModule (lazy)
  /inbound         → InboundModule (lazy)
  /outbound        → OutboundModule (lazy)
  /inventory       → InventoryModule (lazy)
  /quotes          → QuoteModule (lazy)
  /billing         → BillingModule (lazy)
  /organization    → OrganizationModule (lazy)
```

## LayoutComponent

- **Sidebar** fijo a la izquierda (~240px), colapsable a iconos (~64px)
  - Icono/logo de la empresa arriba
  - Navegación con iconos + texto: Dashboard, Catálogo, Clientes, Proveedores, Compras, Entradas, Salidas, Inventario, Presupuestos, Facturación
  - Al final: Administración (solo ADMIN/STAFF) → Organización, Miembros, Invitaciones
- **Header** superior: selector de organización (dropdown), nombre de usuario, botón cerrar sesión
- **router-outlet** en el área de contenido

## Responsive

- <768px: sidebar oculta, menú hamburguesa, layout vertical
- 768-1024px: sidebar colapsada (solo iconos), hover expande
- >1024px: sidebar expandida

## Estados globales

- `currentOrganization` signal con el UUID + nombre de la org activa
- `currentUser` signal con los datos del usuario autenticado
- Todas las llamadas API envían `X-Organization-Id: currentOrganization().id`

## Organización activa

- Al iniciar sesión, si el usuario pertenece a una sola organización, se selecciona automáticamente
- Si pertenece a varias, se muestra un selector obligatorio antes de acceder al dashboard
- El selector de organización en el header permite cambiar de organización activa (recarga los datos del contexto actual)
