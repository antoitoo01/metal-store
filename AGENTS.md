# Metal Store — Agent Guide

## Commands

- `gradlew build` — full build + tests (only command used)
- Tests: `@ActiveProfiles("test")`, H2 in-memory (test profile)
- Dev: profile `dev` (default), H2 in-memory

## Stack

- Java 25, Kotlin 2.2.21, Spring Boot 4.0.1, Gradle 9.2.1 wrapper
- Unusual deps: `spring-boot-h2console`, `spring-boot-starter-webmvc` (not `-web`), `tools.jackson.module:jackson-module-kotlin`
- Kotlin compiler: `-Xjsr305=strict`, `-Xannotation-default-target=param-property`
- JPA `allOpen` on `@Entity`, `@MappedSuperclass`, `@Embeddable`

## Architecture

**Modular monolith** — single Gradle project, package-level bounded contexts:
- `com.blacksmith.metalstore/auth/` — auth context (entity, repo, service, controller, config, dto, exception)
- `com.blacksmith.metalstore/shared/` — reserved for cross-context code

## Supabase Integration (current state)

- **Auth**: Supabase Auth API via `SupabaseAuthClient` (REST, not SDK). Backend proxies signup/signin.
- **JWKs**: Spring Security OAuth2 resource server validates Supabase JWT via `jwk-set-uri`.
- **DB**: `auth/` entity maps to local `users` table. No FK to `auth.users` — `User.id` = Supabase `auth.users.id` UUID.
- **Profile separation**: `dev` = permit-all security + H2; non-dev = OAuth2 + JWT validation.
- **Env vars** (loaded via `DotenvLoader` in `main()` from `.env`): `SUPABASE_URL`, `SUPABASE_PUBLISHABLE_KEY`, `SUPABASE_SECRET_KEY`, `SUPABASE_DB_URL`, `SUPABASE_DB_USER`, `SUPABASE_DB_PASSWORD`.

## Auth Flow

| Endpoint | Method | Auth | Description |
|---|---|---|---|
| `/api/auth/register` | POST | None | Creates user in Supabase + local `users` table, returns JWT |
| `/api/auth/login` | POST | None | Authenticates via Supabase, returns JWT + user info |
| `/api/auth/me` | GET | JWT | Returns current authenticated user |
| `/api/auth/refresh` | POST | None | Refresh access token using refresh token |
| `/api/auth/logout` | POST | JWT | Revoke session |
| `/api/users/{id}` | GET | Future | Find user by ID |
| `/api/users` | PUT | Future | Update user |
| `/api/users/{id}` | DELETE | Future | Delete user |

## Conventions

- Default profile is `dev` (H2 + permit-all security)
- Password removed from `User` entity — Supabase handles auth
- `LoginResponse` includes `accessToken`, `refreshToken`, `expiresIn`, `email`, `role`
- `CreateUserRequest` no longer has `password` (use `RegisterRequest` for registration)
- `@ConfigurationPropertiesScan` on `MetalStoreApplication`
