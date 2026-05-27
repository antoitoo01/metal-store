# Skill Registry — metal-store

Generated: 2026-05-25

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

### work-unit-commits
- **Path**: `~/.config/opencode/skills/work-unit-commits/SKILL.md`
- **Description**: Plan commits as reviewable work units.
- **Compact Rules**:
  - One logical change per commit
  - Tests and docs travel with code in same commit
  - Commit message explains WHY, not WHAT
  - Each commit must pass CI independently

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
