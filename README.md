# Metal Store ERP

ERP para talleres metalúrgicos — multi-tenant SaaS. Backend en Kotlin + Spring Boot 4 con arquitectura de monolito modular.

## Stack

| Capa | Tecnología |
|------|------------|
| Lenguaje | Kotlin 2.2 + JVM 25 |
| Framework | Spring Boot 4.0.1 |
| Base de datos | PostgreSQL (prod) / H2 (dev/test) |
| ORM | JPA / Hibernate |
| Auth | Supabase Auth + OAuth2 Resource Server (JWT) |
| API Docs | Springdoc OpenAPI 3.1 (Swagger UI) |
| Rate limiting | Bucket4j |
| Build | Gradle 9.2 |
| CI | GitHub Actions + PostgreSQL service container |

## Arquitectura

Monolito modular con bounded contexts por paquete:

```
com.blacksmith.metalstore/
├── auth/          — Autenticación, usuarios, rate limiting
├── client/        — Gestión de clientes
├── catalog/       — Perfiles, items, familias, tipos, imágenes
├── quote/         — Presupuestos con máquina de estados
├── billing/       — Facturación, precios, líneas
├── inventory/     — Control de stock
└── config/        — Configuración global (OpenAPI, etc.)
```

Cada contexto tiene su propio modelo, repositorio, servicio y controlador. No hay dependencias circulares entre contextos.

## Quick Start

```bash
# 1. Clonar
git clone https://github.com/tu-usuario/metal-store.git
cd metal-store

# 2. Configurar variables de entorno (opcional para dev)
cp .env.example .env
# Editar SUPABASE_URL, SUPABASE_PUBLISHABLE_KEY, SUPABASE_SECRET_KEY

# 3. Iniciar (dev profile — H2 + permit-all security)
./gradlew bootRun

# 4. Abrir
curl http://localhost:8080/api/health
```

### Perfiles

| Profile | DB | Security | Uso |
|---------|----|----------|-----|
| `dev` (default) | H2 in-memory | Permit-all | Desarrollo local |
| `test` | H2 in-memory | Permit-all | Tests |
| `ci` | PostgreSQL | Permit-all | CI local con PostgreSQL |
| `prod` | PostgreSQL | OAuth2 JWT | Producción |

Para usar PostgreSQL local: `--spring.profiles.active=ci`

## API Documentation

Con la app corriendo:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

Endpoints agrupados por tag:

| Tag | Contexto |
|-----|----------|
| Auth | AuthController, UserController, HealthController |
| Catalog | Perfiles, items, familias, tipos, imágenes |
| Clients | CRUD clientes con activate/deactivate |
| Quotes | Presupuestos (DRAFT → ISSUED → ACCEPTED/REJECTED → CANCELLED) |
| Billing | Precios + facturas (DRAFT → ISSUED → PAID/CANCELLED) |
| Inventory | Stock, ubicaciones, proveedores |

### Autenticación

La API usa Bearer JWT (Supabase Auth). Todos los endpoints excepto `/api/auth/**`, `/swagger-ui/**` y `/v3/api-docs/**` requieren token en producción.

### HTTP Requests

En la carpeta `http/` hay ejemplos listos para usar con el HTTP Client de IntelliJ:

```
http/auth.http       — Auth + users
http/catalog.http    — Familias, perfiles, items, tipos, imágenes
http/client.http     — CRUD clientes
http/billing.http    — Precios + facturas
http/inventory.http  — Stock
http/quote.http      — Presupuestos
```

Variables de entorno (`http/http-client.env.json`): `baseUrl`, `email`, `password`.

## Variables de Entorno

| Variable | Requerida | Descripción |
|----------|-----------|-------------|
| `SUPABASE_URL` | Prod | URL del proyecto Supabase |
| `SUPABASE_PUBLISHABLE_KEY` | Prod | Anon key de Supabase |
| `SUPABASE_SECRET_KEY` | Prod | Service role key |
| `SUPABASE_DB_URL` | Prod | JDBC URL a Supabase PostgreSQL |
| `SUPABASE_DB_USER` | Prod | Usuario de base de datos |
| `SUPABASE_DB_PASSWORD` | Prod | Contraseña |

Se cargan desde `.env` en `main()` via `DotenvLoader`. En CI se setean como env vars del workflow.

## Testing

```bash
# Todos los tests (unit + integración)
./gradlew test
```

### Tipos de test

| Tipo | Cantidad | Qué prueban |
|------|----------|-------------|
| Unit / Service | ~40 | Lógica de negocio con mocks |
| HTTP (MockMvc) | ~80 | Endpoints, validación, autorización, ciclo de vida |
| Integración real | 8 | Auth y Storage contra Supabase real (opt-in) |

### Integración real contra Supabase

Hay tests que llaman a Supabase Auth y Storage **reales** (no mocks):

```
SupabaseAuthIntegrationTest — register, login, refresh, logout, getUser
SupabaseStorageIntegrationTest — upload, load, delete files
```

Se ejecutan **solo cuando hay credenciales**. Caso contrario se skipean automáticamente:

```bash
# Con Supabase configurado (usa .env):
./gradlew test    # corre 131 tests (123 + 8 de integración real)

# Sin Supabase (CI, local sin .env):
./gradlew test    # corre 123 tests, 8 skipeados
```

Para correr SOLO los de integración:

```bash
./gradlew test --tests "*Supabase*Integration*"
```

### CI

En GitHub Actions, `SUPABASE_URL=""`, así que los tests de integración real se skipean. Los 123 tests unitarios + HTTP corren contra PostgreSQL vía service container.

## CI/CD

El workflow `.github/workflows/ci.yml` se ejecuta en PRs a `main` y pushes a `main`:

1. Checkout + JDK 25 (temurin)
2. Inicia PostgreSQL 16 service container
3. Corre `./gradlew build` con `SPRING_DATASOURCE_*` apuntando al service container
4. `SUPABASE_URL=""` previene llamadas externas

## Project Structure

```
metal-store/
├── .github/workflows/ci.yml     — CI pipeline
├── http/                         — HTTP requests de ejemplo
├── docs/data/raw/                — Fuentes de datos originales (AISC, Eurocódigos)
├── src/
│   ├── main/
│   │   ├── kotlin/com/blacksmith/metalstore/
│   │   │   ├── auth/             — Auth context
│   │   │   ├── client/           — Client context
│   │   │   ├── catalog/          — Catalog context
│   │   │   ├── quote/            — Quote context
│   │   │   ├── billing/          — Billing context
│   │   │   ├── inventory/        — Inventory context
│   │   │   └── config/           — Global config
│   │   └── resources/
│   │       ├── data/             — Seed data (JSON, CSV)
│   │       └── application*.yml  — Config por perfil
│   └── test/                     — Tests espejando estructura main
└── build.gradle.kts              — Dependencias y plugins
```

## Licencia

Código abierto — sin licencia definida aún.
