# 013 — Organización, Miembros e Invitaciones

## Pantallas

### `/organization` — Configuración de la organización
- Solo ADMIN puede acceder. STAFF/VIEWER ven versión solo lectura con un mensaje "Solo el administrador puede modificar la organización".
- Formulario: nombre de la organización
- Botón "Guardar cambios" → `PUT /api/organizations/{id}`

### `/organization/members` — Miembros
- Tabla: Email/Usuario | Rol | Estado | Acciones
- Roles: ADMIN (badge rojo), STAFF (badge azul), VIEWER (badge gris)
- Acciones por miembro:
  - Cambiar rol (ADMIN only): dropdown con roles disponibles → `PUT /api/organizations/{orgId}/members/{userId}/role`
  - Eliminar miembro (ADMIN only, confirmación) → `DELETE /api/organizations/{orgId}/members/{userId}`
- No se puede cambiar rol ni eliminar al propio usuario ADMIN (mostrar disabled)

### `/organization/invitations` — Invitaciones
- Tabla: Email | Rol | Estado (PENDING/ACCEPTED/DECLINED/EXPIRED) | Creada | Expira | Acciones
- Botón "Invitar miembros" (ADMIN) → modal: lista de emails (textarea o input múltiple), selector de rol
  - Enviar: `POST /api/organizations/{orgId}/invitations` con `{ emails: [...] }`
- Acciones: Revocar invitación (solo PENDING) → `DELETE /api/organizations/{orgId}/invitations/{id}`

### Selector de organización (global)
- En el header, dropdown con todas las organizaciones del usuario
- Al cambiar: actualiza `currentOrganization` signal, recarga datos de la ruta actual
- Si el usuario solo tiene una organización, no mostrar el selector (o mostrarlo deshabilitado)

## API Reference

| Método | Endpoint | Request | Response |
|--------|----------|---------|----------|
| GET | `/api/organizations` | — | `List<OrganizationResponse>` |
| POST | `/api/organizations` | `{ name }` | `OrganizationResponse` |
| GET | `/api/organizations/{id}` | — | `OrganizationResponse` |
| PUT | `/api/organizations/{id}` | `{ name }` | `OrganizationResponse` (ADMIN) |
| GET | `/api/organizations/{orgId}/members` | — | `List<MembershipResponse>` |
| GET | `/api/organizations/{orgId}/members/me` | — | `MembershipResponse` |
| PUT | `/api/organizations/{orgId}/members/{userId}/role` | `{ role }` | `200` (ADMIN) |
| DELETE | `/api/organizations/{orgId}/members/{userId}` | — | `204` (ADMIN) |
| POST | `/api/organizations/{orgId}/invitations` | `{ emails: ["a@b.com"] }` | `List<InvitationResponse>` (ADMIN) |
| GET | `/api/organizations/{orgId}/invitations?page=` | — | `Page<InvitationResponse>` (ADMIN) |
| DELETE | `/api/organizations/{orgId}/invitations/{id}` | — | `204` (ADMIN) |
| POST | `/api/invitations/accept` | `{ token }` | `MembershipResponse` |
| POST | `/api/invitations/decline` | `{ token }` | `200` |

### OrganizationResponse
```json
{ "id": "uuid", "name": "Talleres López SL", "slug": "talleres-lopez-sl", "memberCount": 5 }
```

### MembershipResponse
```json
{ "id": "uuid", "userId": "uuid", "organizationId": "uuid", "role": "admin", "status": "active" }
```

### InvitationResponse
```json
{ "id": "uuid", "organizationId": "uuid", "organizationName": "Talleres López SL",
  "email": "nuevo@example.com", "role": "staff", "status": "PENDING",
  "token": "abc123", "link": "http://.../accept-invitation?token=abc123",
  "expiresAt": "2026-07-26T21:00:00", "createdAt": "..." }
```
