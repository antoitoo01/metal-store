# 002 — Autenticación (Auth)

## Pantallas

### `/login` — Iniciar sesión
- Formulario: email + contraseña
- Botón "Inicia sesión" (loading state mientras se llama a `POST /api/auth/login`)
- Enlace "¿No tienes cuenta? Regístrate" → `/register`
- Errores: "Credenciales inválidas", "Usuario no encontrado"
- Éxito: almacena accessToken + refreshToken en localStorage, redirige a `/dashboard`

### `/register` — Crear cuenta
- Formulario: nombre de empresa + email + contraseña + confirmar contraseña
- Botón "Crear cuenta" → `POST /api/auth/register`
- Enlace "¿Ya tienes cuenta? Inicia sesión" → `/login`
- Éxito: igual que login (almacena tokens + redirige a `/dashboard`)

### `/accept-invitation?token=xxx` — Aceptar invitación
- Cargar automático: `POST /api/invitations/accept` con el token
- Éxito: redirige a `/login` con mensaje "Invitación aceptada. Inicia sesión."
- Error: "Invitación inválida o expirada"

## Flujo de tokens
- Interceptor HTTP: añade `Authorization: Bearer <accessToken>` a todas las requests excepto login/register/refresh
- Si `401 Unauthorized`: intenta refrescar con `POST /api/auth/refresh` usando refreshToken
- Si el refresh falla: limpia tokens, redirige a `/login`
- Logout: `POST /api/auth/logout`, limpia tokens, redirige a `/login`

## Estados
- `isAuthenticated` signal → controla el guard de rutas
- `currentUser` signal con datos de `GET /api/auth/me`

## API Reference

| Método | Endpoint | Request | Response |
|--------|----------|---------|----------|
| POST | `/api/auth/register` | `{ email, password, username?, tenantName? }` | `LoginResponse` |
| POST | `/api/auth/login` | `{ email, password }` | `LoginResponse` |
| POST | `/api/auth/refresh` | `{ refreshToken }` | `LoginResponse` |
| POST | `/api/auth/logout` | (token from header/cookie) | `204` |
| GET | `/api/auth/me` | — | `UserResponse` |

### LoginResponse
```json
{ "accessToken": "jwt...", "tokenType": "Bearer", "refreshToken": "...", "expiresIn": 3600,
  "email": "a@b.com", "username": "user", "role": "admin",
  "organizationId": "uuid", "organizationName": "Talleres López" }
```

### UserResponse
```json
{ "id": "uuid", "username": "user", "email": "a@b.com", "role": "admin",
  "organizationId": "uuid", "organizationName": "Talleres López",
  "organizations": [{ "organizationId": "uuid", "organizationName": "...", "role": "admin" }] }
```
