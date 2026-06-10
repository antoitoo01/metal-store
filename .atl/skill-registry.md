# Skill Registry — metal-store

Generated: 2026-06-05

## Convention Files

- `AGENTS.md` — stack, architecture, auth flow, conventions

## User Skills

> User-level skills from `~/.config/opencode/skills/` — non-SDD, non-shared, non-registry.

### branch-pr
- **Path**: `~/.config/opencode/skills/branch-pr/SKILL.md`
- **Description**: Create Gentle AI pull requests with issue-first checks.
- **Compact Rules**:
  - Check for existing issue/PR before creating a new one
  - Use `gh` CLI for PR operations
  - Include issue references in PR description
  - Verify branch targets correct base

### chained-pr
- **Path**: `~/.config/opencode/skills/chained-pr/SKILL.md`
- **Description**: Split oversized changes (>400 lines) into chained PRs.
- **Compact Rules**:
  - Each PR must be independently reviewable (<400 lines)
  - Track dependency ordering between PRs
  - Each PR must pass CI independently
  - Use stacked-to-main or feature-branch-chain strategy

### cognitive-doc-design
- **Path**: `~/.config/opencode/skills/cognitive-doc-design/SKILL.md`
- **Description**: Design docs that reduce cognitive load for readers.
- **Compact Rules**:
  - One concept per section
  - Use progressive disclosure (overview → detail)
  - Include concrete examples before abstract rules
  - Keep paragraphs under 6 lines

### comment-writer
- **Path**: `~/.config/opencode/skills/comment-writer/SKILL.md`
- **Description**: Write warm, direct collaboration comments.
- **Compact Rules**:
  - Match user's language (Rioplatense voseo for Spanish, natural English otherwise)
  - Validate before agreeing — never assume
  - Lead with evidence when correcting
  - Be direct but caring

### customize-opencode
- **Path**: `~/.config/opencode/skills/customize-opencode/SKILL.md`
- **Description**: Edit opencode's own config (opencode.json, .opencode/, skills/).
- **Compact Rules**:
  - Only use for opencode configuration, NOT user application code
  - Validate JSON/YAML schema before writing
  - Back up existing config before changes

### find-skills
- **Path**: `~/.config/opencode/skills/find-skills/SKILL.md`
- **Description**: Help discover and install agent skills.
- **Compact Rules**:
  - Search registry and skill directories
  - Match task description to skill triggers
  - Provide install path and activation instructions

### issue-creation
- **Path**: `~/.config/opencode/skills/issue-creation/SKILL.md`
- **Description**: Create GitHub issues with issue-first checks.
- **Compact Rules**:
  - Check for duplicate issues before creating
  - Use `gh issue create` with structured templates
  - Include reproduction steps for bugs

### judgment-day
- **Path**: `~/.config/opencode/skills/judgment-day/SKILL.md`
- **Description**: Blind dual review, fix confirmed issues, re-judge.
- **Compact Rules**:
  - First pass: review without reading own implementation
  - Flag issues with evidence only
  - Fix only confirmed issues
  - Second pass: re-verify fixes

### pdf-extraction
- **Path**: `~/.config/opencode/skills/pdf-extraction/SKILL.md`
- **Description**: Extract text, tables, metadata from PDFs via pdfplumber.
- **Compact Rules**:
  - Use pdfplumber for extraction
  - Handle table detection and text extraction separately
  - Extract metadata (author, date, page count)

### skill-creator
- **Path**: `~/.config/opencode/skills/skill-creator/SKILL.md`
- **Description**: Create LLM-first skills with valid frontmatter.
- **Compact Rules**:
  - Required structure: frontmatter, Activation Contract, Hard Rules, Decision Gates, Execution Steps, Output Contract
  - Keep description ≤250 chars, one physical line
  - Body target: 180–450 tokens; move depth to references/
  - Hard rules must be observable; decision gates cover real forks
  - References locally resolved relative to skill directory

### go-testing
- **Path**: `~/.config/opencode/skills/go-testing/SKILL.md`
- **Description**: Apply focused Go testing patterns — table-driven tests, Bubbletea teatest, golden files.
- **Compact Rules**:
  - Prefer table-driven tests with `t.Run(tt.name, ...)`
  - Test behavior and state transitions, not implementation trivia
  - Use `t.TempDir()` for filesystem tests; never rely on real home dir
  - Skip integration tests with `testing.Short()` when they run external commands
  - For Bubbletea: test `Model.Update()` directly; use `teatest` only for interactive flows
  - Golden files must be deterministic; update only through `-update` path
  - Use small mocks/interfaces around system or command boundaries

### work-unit-commits
- **Path**: `~/.config/opencode/skills/work-unit-commits/SKILL.md`
- **Description**: Plan commits as reviewable work units.
- **Compact Rules**:
  - One logical change per commit
  - Tests and docs travel with code in same commit
  - Commit message explains WHY, not WHAT
  - Each commit must pass CI independently

### angular-component
- **Path**: `~/.agents/skills/angular-component/SKILL.md`
- **Description**: Create modern Angular standalone components with signal-based inputs/outputs, OnPush CD, host bindings.
- **Compact Rules**:
  - Use `input.required()` / `input()` with signals, NOT `@Input()` decorator
  - Use `output()` for events, NOT `@Output()` + EventEmitter
  - Use `host` object in `@Component`, NOT `@HostBinding` or `@HostListener`
  - Use `ChangeDetectionStrategy.OnPush` always
  - Use native control flow (`@if`, `@for`, `@switch`), NOT `*ngIf`, `*ngFor`, `*ngSwitch`
  - Do NOT use `ngClass` or `ngStyle` — use direct bindings (`[class.active]`, `[style.color]`)
  - Components are standalone by default — do NOT set `standalone: true`
  - Pass AXE accessibility checks, WCAG AA, proper ARIA attributes

### angular-forms
- **Path**: `~/.agents/skills/angular-forms/SKILL.md`
- **Description**: Build signal-based forms in Angular v21+ using the new Signal Forms API.
- **Compact Rules**:
  - Signal Forms API is experimental in Angular v21
  - Form models are writable signals (`signal<T>()`) — single source of truth
  - Use `form()` with schema for validation: `required`, `email`, `min`, `max`, `minLength`, `maxLength`, `pattern`
  - Cross-field validation: use `validate()` with `valueOf()` access
  - Async validation: use `validateHttp()` for server-side checks
  - Use `submit()` helper to mark all fields touched before running handler
  - Use `@if (form.field().touched() && form.field().invalid())` for error display
  - Field state signals: `valid()`, `invalid()`, `errors()`, `touched()`, `dirty()`, `pending()`, `disabled()`, `hidden()`

### angular-http
- **Path**: `~/.agents/skills/angular-http/SKILL.md`
- **Description**: Implement HTTP data fetching in Angular v20+ using resource(), httpResource(), and HttpClient.
- **Compact Rules**:
  - Prefer `httpResource<T>()` for signal-based HTTP — wraps HttpClient with isLoading/error/value state
  - Use `resource()` for non-HTTP async or custom fetch logic
  - Use `HttpClient` directly (via `inject()`) for Observable patterns when needed
  - Functional interceptors (not class-based): `HttpInterceptorFn`
  - Register interceptors with `withInterceptors()` in `provideHttpClient()`
  - `httpResource` refetches when request params change
  - Return `undefined` from request fn to skip loading (status becomes 'idle')

### angular-routing
- **Path**: `~/.agents/skills/angular-routing/SKILL.md`
- **Description**: Implement routing in Angular v20+ with lazy loading, functional guards, and signal-based params.
- **Compact Rules**:
  - Use `provideRouter(routes, withComponentInputBinding())` for signal-based route params as `input()`
  - Lazy load with `loadComponent` or `loadChildren`, NOT NgModule-based
  - Use functional guards (`CanActivateFn`, `CanDeactivateFn`), NOT class-based guards
  - Use `ResolveFn` for resolvers (functional pattern)
  - Use `routerLink`, `routerLinkActive`, `routerOutlet` directives in templates
  - Navigate programmatically with `Router.navigate()` or `Router.navigateByUrl()`

### angular-signals
- **Path**: `~/.agents/skills/angular-signals/SKILL.md`
- **Description**: Implement signal-based reactive state management in Angular v20+.
- **Compact Rules**:
  - `signal()` for writable state, `computed()` for derived/dependent state
  - `linkedSignal()` for dependent state that auto-resets when source changes
  - `effect()` for side effects — runs in injection context, auto-cleaned on destroy
  - Convert Observable ↔ Signal: `toSignal()` (Observable→Signal), `toObservable()` (Signal→Observable)
  - Use `asReadonly()` to expose public read-only state from services
  - Use `untracked()` to read signal without creating a dependency
  - Custom equality via `{ equal: (a, b) => ... }` in `signal()` options

### tailwind-v4-shadcn
- **Path**: `~/.agents/skills/tailwind-v4-shadcn/SKILL.md`
- **Description**: Production-tested Tailwind CSS v4 with shadcn/ui, Vite, React, dark mode.
- **Compact Rules**:
  - Use `@tailwindcss/vite` plugin (NOT PostCSS); delete `tailwind.config.ts`
  - Set `"config": ""` in `components.json` for Tailwind v4
  - Define CSS variables at `:root` level (NOT in `@layer base`)
  - Wrap color values with `hsl()` in `:root`/`.dark`; reference as `var(--name)` in `@layer base`
  - Use `@theme inline` to map CSS variables to Tailwind utilities
  - Use `.dark` class for dark mode (NOT nested `@theme`)
  - Do NOT double-wrap: `body { color: var(--foreground) }` NOT `hsl(var(--foreground))`
  - Do NOT use `dark:` variants for semantic colors
  - Do NOT use `@apply` (deprecated in v4); do NOT install `tailwindcss-animate` (deprecated)

### webapp-testing
- **Path**: `~/.agents/skills/webapp-testing/SKILL.md`
- **Description**: Toolkit for interacting with and testing local web applications using Playwright.
- **Compact Rules**:
  - Write native Python Playwright scripts for web testing
  - Always wait for `networkidle` before DOM inspection on dynamic apps
  - Use `scripts/with_server.py` helper for managed server lifecycle
  - Read static HTML directly for static pages; use reconnaissance-then-action for dynamic apps
  - Use descriptive selectors: `text=`, `role=`, CSS selectors, or IDs
  - Always close browser when done

## Project Skills

> Project-level skills from `.agents/skills/` — deduplicated (project overrides user).

### java-spring-boot
- **Path**: `.agents/skills/java-spring-boot/SKILL.md`
- **Description**: Build production Spring Boot applications — REST APIs, Security, Data, Actuator.
- **Compact Rules**:
  - Use `@RestController` + `@RequestMapping` for REST endpoints
  - Validate inputs with `@Valid` + Bean Validation annotations
  - Handle exceptions via `@RestControllerAdvice` + `ProblemDetail`
  - Configure security with `SecurityFilterChain` bean, stateless sessions
  - Use OAuth2 resource server JWT for auth where configured
  - Spring Data JPA: repository pattern, query methods, pagination
  - Keep `open-in-view: false` in production
  - Use `@ConfigurationPropertiesScan` for properties binding
  - Profile-based config: dev (H2 + permit-all), test (H2), prod (PostgreSQL + JWT)
  - Do NOT use `@EnableWebSecurity` in SB3.x+ (auto-configured)

### kotlin-specialist
- **Path**: `.agents/skills/kotlin-specialist/SKILL.md`
- **Description**: Use for Kotlin coroutines, multiplatform, Android/Compose, Ktor, DSL.
- **Compact Rules**:
  - Use null safety (`?.`, `?:`, never `!!` without justification)
  - Prefer `sealed class`/`sealed interface` for state modeling
  - Use `suspend` functions for async, `Flow` for reactive streams
  - Scope functions: `let`, `run`, `apply`, `also`, `with` — use appropriately
  - NEVER use `runBlocking` in production code
  - NEVER use `GlobalScope.launch` — use structured concurrency
  - Document public APIs with KDoc
  - Use `data class` for DTOs and value objects

### supabase
- **Path**: `.agents/skills/supabase/SKILL.md`
- **Description**: Any task involving Supabase — Auth, DB, Storage, Edge Functions, Realtime.
- **Compact Rules**:
  - Verify against current docs before implementing (Supabase changes frequently)
  - NEVER use `user_metadata` for authz decisions (user-editable)
  - Deleting user ≠ invalidating tokens — sign out first
  - Views bypass RLS by default — use `security_invoker = true`
  - UPDATE needs SELECT policy — silent fail if missing
  - `auth.role()` is deprecated — use `TO authenticated`/`TO anon` clauses
  - `SECURITY DEFINER` functions bypass RLS — prefer `SECURITY INVOKER`
  - Enable RLS on every exposed table
  - For schema iteration: use `execute_sql`/`supabase db query`, NOT `apply_migration`
  - When committing: run advisors → review checklist → `supabase db pull`

### supabase-postgres-best-practices
- **Path**: `.agents/skills/supabase-postgres-best-practices/SKILL.md`
- **Description**: Postgres optimization rules from Supabase — queries, indexes, schema, RLS, monitoring.
- **Compact Rules**:
  - Priority order: query perf → connection mgmt → security/RLS → schema → locking → data access → monitoring → advanced
  - Missing indexes are the #1 performance killer
  - Use partial indexes for filtered queries
  - Connection pooling is CRITICAL for serverless/edge
  - RLS policies: always combine `TO authenticated` with ownership predicate
  - Use `EXPLAIN ANALYZE` to verify query plans
  - Prefer `security_invoker` views over `security_definer`
  - Keep transactions short to avoid lock contention

## Referenced from AGENTS.md

- `src/main/kotlin/com/blacksmith/metalstore/auth/` — auth context (entity, repo, service, controller, config, dto, exception)
- `src/main/kotlin/com/blacksmith/metalstore/shared/` — reserved for cross-context code
- `src/main/resources/application-{profile}.yml` — profile configs (dev, test, prod)
