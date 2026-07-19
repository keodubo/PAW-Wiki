---
name: paw-tp-final-migration
description: Use when planning, implementing, auditing, reviewing, or closing a PAW TP final migration to REST API plus SPA/frontend, especially when the user says TP final, final delivery, pass the app to final, replace MVC/JSP with API+SPA, add Jersey/JAX-RS resources, stateless auth, frontend build, routing/state/forms/i18n/testing, Maven/WAR integration, PostgreSQL data preservation, prior-feedback closure, performance/cache, deployment, or delivery readiness.
---

# Paw TP Final Migration

## Overview

Use this when the user says the PAW project is in TP final, asks for REST/API work, SPA/frontend migration, Jersey/JAX-RS, stateless auth, frontend build tooling, routing/state, asset caching, final compliance, or says "pasalo al final". The goal is to deliver the official REST + SPA contract without losing functionality, TPE2 server data, or backend business authority.

Read `references/migration-rules.md` before editing or judging final-stage work.

## Required First Pass

1. Confirm the stage is `TP final` or the user explicitly asked for REST/SPA/frontend migration.
2. Treat `PAW-Wiki/docs/raw/Enunciado_TPE_Final.pdf` as the official delivery contract when present. Read the app `CLAUDE.md`, `PAW-Wiki/docs/CLAUDE.md`, `PAW-Wiki/docs/index.md`, `PAW-Wiki/docs/wiki/resumen-final-paw-2026.md`, and `PAW-Wiki/docs/wiki/checklist-tp-final-rest-spa.md` as implementation guidance. If they conflict, the official enunciado wins for final compliance.
3. Read `PAW-Wiki/docs/wiki/api-rest.md`, `single-page-applications.md`, `spring-security.md`, `internacionalizacion.md`, `ux-flows.md`, and `resumen-clases-paw-2026.md` only for the affected topics.
4. Find the actual TP1/TPE2 feedback for the project. Use `resumen-correcciones-tp1-2026-c1.md`, `resumen-correcciones-tp2.md`, and `criterios-evaluacion.md` as pattern catalogs, not as substitutes for the team's own feedback.
5. Capture baseline MVC routes, services, auth rules, views, tests, packaging, PostgreSQL schema/data assumptions, deployed environment, and current delivery hash when relevant.
6. State whether the work is one migration slice, an audit/plan, or final-delivery closeout. Build a traceability matrix from official requirement and prior correction to code/test/runtime evidence and status.
7. If the user says only "quiero migrar" without TP final/REST/SPA evidence, ask whether this is TP2/JPA or TP final/REST+SPA before queuing implementation.
8. For implementation requests, produce an execution queue of vertical slices before editing. Each slice names the current MVC route/view, target REST contract, SPA route, tests to write first, frontend runner/smoke command, verification command, data-preservation/feedback impact, auth/storage decision when touched, package output/base-path check when touched, and rollback/parallel-routing state.

## Migration Workflow

1. Choose one user-visible MVC flow as the next slice unless the user asked for an audit/plan or final closeout only. Preserve functional parity and close recorded feedback before adding unrequested features.
2. Inventory the current route, JSP/view, service calls, auth rule, i18n keys, tests, data dependencies, and deploy/package assumptions for that flow.
3. Define noun-based API resources, DTOs/forms, links/URIs, status codes, error shape, pagination headers, media types, auth model, and cache behavior before frontend implementation.
4. Keep services as business authority; Jersey/JAX-RS resources deserialize/validate/delegate and translate results with `Response`, `Location`, `Link`, and DTO links built from `UriInfo`/builders.
5. Use vendor media types for representation/version negotiation only; do not hide unrelated operations behind `PATCH` + media-type switches.
6. Make auth stateless: initial `Authorization: Basic ...` may mint tokens, normal requests use `Authorization: Bearer <access-token>`, `401` is missing/invalid/expired credentials, `403` is authenticated-but-forbidden, and refresh tokens rotate on login/use instead of traveling in every response.
7. Follow TDD per slice: write the narrow failing API/resource tests first, verify the failure, implement only enough backend to pass, then widen to service/frontend/package tests as the slice grows.
8. Use `$paw-frontend-layer` for SPA source: routes, state, forms, API client, auth state, errors, i18n, frontend tests, and base-path source config for the same slice. If no frontend runner exists, define and run a repeatable browser/static-hosting smoke instead of leaving a prose-only gap.
9. Integrate the frontend build into Maven/WAR packaging only after the API/client boundary is clear. Verify lockfile/package-manager choice, reproducible install command, Node/npm source, output directory, `webapp` dependency on `frontend`, static hosting, base path, lazy loading/code splitting, minification, and cache/file revving so hashed assets can be immutable while `index.html`/root is revalidated.
10. When persistence/schema changes, prove both paths: upgrading a representative TPE2 PostgreSQL dataset without losing information and bootstrapping every required table in an empty PostgreSQL schema. Never use destructive regeneration as the upgrade strategy.
11. Keep the old JSP flow only as a temporary, reversible migration aid. Before final delivery, remove it with user approval or record a blocking enunciado deviation; do not call a deliverable compliant while Spring MVC views or user-facing controllers remain.

## Slice Definition Of Done

Before claiming a migrated slice is done:

- API contract tests cover status, body, media type, `Location`/`Link`/`ETag` headers where applicable, auth, validation, and Problem Details errors.
- Service tests cover business behavior only when behavior moved or changed.
- SPA route/client/state/form/i18n behavior is tested when a runner exists; otherwise a repeatable smoke covers the user-visible behavior and the test gap is documented.
- `/api/*` failures stay JSON/status API, while non-API deep links return `index.html`.
- Packaging changes pass through Maven, and the WAR contains backend classes plus current `index.html`, JS/CSS, and assets from the declared frontend output directory.
- Affected TPE2 data and functionality are preserved, and any prior correction touched by the slice has evidence of closure.
- The old MVC route may remain only as a named temporary rollback path; final-delivery readiness additionally requires the gates below.

## Final Delivery Gates

Do not claim the TP final is ready while any mandatory gate is `fail`, `not run`, or supported only by assumption:

- All prior user-visible functionality is available through the SPA, all project-specific TP1/TPE2 corrections are closed or explicitly blocking, and no speculative feature displaced parity/feedback work.
- No Spring MVC view or user-facing Spring MVC controller remains in the deliverable. A compatibility redirect retained by local project policy is an explicit enunciado deviation until accepted by the user/catedra.
- Jersey REST resources follow REST/HATEOAS; absence of REST/HATEOAS compliance is a hard delivery failure, not a polish item.
- Backend and frontend have meaningful unit-level, blackbox, behavior-only tests. A missing frontend runner is a blocker for final readiness even if a browser smoke exists.
- The production frontend demonstrates lazy loading/code splitting, minification, cache-busted hashed assets with long unconditional cache, and conditional cache/revalidation for mutable representations or entry HTML.
- A clean checkout can run only `mvn clean package` and produce the complete usable WAR without a manual frontend build or stale generated assets.
- An empty PostgreSQL schema can initialize all required tables, and an upgrade rehearsal preserves a representative copy/snapshot of TPE2 server data.
- The exact WAR is smoke-tested in the target Application Container and, when access exists, on the catedra server: root SPA, deep link, static asset, authenticated flow, valid API request, API error, and one core flow per role.
- `README.md` documents deployment prerequisites and non-sensitive demo credentials for at least one seeded user at every access level.
- The repository excludes IDE metadata, dependencies, Maven/frontend generated output, secrets, and other unnecessary files. The recorded delivery commit hash is the exact deployed commit, the deadline constraint is respected, and the online demo is prepared.

## Coordination

- Use `$paw-service-contracts-layer` for service/API boundary DTOs and exceptions.
- Use `$paw-services-layer` for backend business behavior used by REST resources.
- Use `$paw-webapp-layer` for REST resources, security config, API error mapping, SPA static hosting, and asset integration.
- Use `$paw-frontend-layer` for frontend source, API clients, stores/composables, router, forms, i18n catalogs, frontend tests, and build config.
- Use `$paw-testing-layer` for API contract tests, frontend test strategy, and Maven/package gates.
- Use `$paw-models-layer` only for domain concepts, not as a dumping ground for API payloads.

## Stop Conditions

Ask before proceeding when:

- The user has not confirmed TP final and the task would introduce SPA, REST-only architecture, JWT/token auth, or frontend build tooling.
- Auth storage trade-offs are unresolved.
- Existing JSP routes must remain live in parallel but routing ownership is unclear.
- Removing the remaining Spring MVC views/controllers requires user approval or would break an unverified flow.
- The frontend build would change deploy/package commands.
- Basic/Bearer token storage, refresh rotation, or exposed auth headers are unresolved.
- Cache settings could make stale HTML or stale assets persist for users.
- A schema/data change lacks a representative TPE2 snapshot, rollback, or non-destructive migration path.
- Final closeout lacks a frontend unit-test runner, target-container/server access, per-role demo users, the actual prior feedback, or a trustworthy deployed-commit hash.
- The reference implementation contains tempting local `.env`, `.properties`, keys, credentials, or generated assets; do not copy them.

## Verification

Use repo-specific commands where available:

```bash
mvn -pl webapp -am test
mvn clean test
mvn clean package
```

For targeted API/MVC tests:

```bash
mvn -pl webapp -am -Dtest=<ApiOrMvcTestName> -Dsurefire.failIfNoSpecifiedTests=false test
```

If a frontend module exists, run its declared package/test/build commands from the repo documentation before claiming the final migration is ready.

When packaging/static hosting changed, also run `mvn clean package` and inspect or smoke the WAR for `index.html`, hashed JS/CSS/assets, `/api/*` JSON errors, SPA deep-link fallback, and correct context-path asset URLs:

```bash
jar tf webapp/target/*.war | rg '(^|/)index.html$|assets/|static/|WEB-INF/classes'
```

For final closeout, also capture evidence for repository hygiene and the exact delivery identity:

```bash
git status --short --branch
git rev-parse HEAD
git ls-files | rg '(^|/)(target|node_modules|dist|coverage|\.idea)(/|$)|(^|/)(\.classpath|\.project)$'
```

Run PostgreSQL bootstrap/upgrade and target-container/catedra-server smokes with repo-specific, non-destructive commands. Never reset or overwrite the real server database to manufacture evidence.
