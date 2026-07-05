---
name: paw-tp-final-migration
description: Use when planning, implementing, auditing, or reviewing PAW TP final migration to REST API plus SPA/frontend, especially when the user says TP final, final delivery, pass the app to final, replace MVC/JSP with API+SPA, add Jersey/JAX-RS resources, stateless auth, frontend build, routing/state/forms/i18n/testing, Maven/WAR integration, static hosting, cache, or file revving.
---

# Paw TP Final Migration

## Overview

Use this when the user says the PAW project is in TP final, asks for REST/API work, SPA/frontend migration, Jersey/JAX-RS, stateless auth, frontend build tooling, routing/state, asset caching, or says "pasalo al final". The goal is to split presentation into API + client without losing backend business authority.

Read `references/migration-rules.md` before editing or judging final-stage work.

## Required First Pass

1. Confirm the stage is `TP final` or the user explicitly asked for REST/SPA/frontend migration.
2. Read the app `CLAUDE.md`, `PAW-Wiki/docs/CLAUDE.md`, `PAW-Wiki/docs/index.md`, `PAW-Wiki/docs/wiki/resumen-final-paw-2026.md`, and `PAW-Wiki/docs/wiki/checklist-tp-final-rest-spa.md`.
3. Read `PAW-Wiki/docs/wiki/api-rest.md`, `single-page-applications.md`, `spring-security.md`, `internacionalizacion.md`, `ux-flows.md`, and `resumen-clases-paw-2026.md` only for the affected topics.
4. Capture baseline MVC routes, services, auth rules, views, tests, and packaging.
5. State whether the work is API-only, frontend-only, packaging-only, or end-to-end migration.
6. If the user says only "quiero migrar" without TP final/REST/SPA evidence, ask whether this is TP2/JPA or TP final/REST+SPA before queuing implementation.
7. For implementation requests, produce an execution queue of vertical slices before editing. Each slice names the current MVC route/view, target REST contract, SPA route, tests to write first, frontend runner/smoke command, verification command, auth/storage decision when touched, package output/base-path check when touched, and rollback/parallel-routing state.

## Migration Workflow

1. Choose one user-visible MVC flow as the next slice unless the user asked for an audit/plan only.
2. Inventory the current route, JSP/view, service calls, auth rule, i18n keys, tests, data dependencies, and deploy/package assumptions for that flow.
3. Define noun-based API resources, DTOs/forms, links/URIs, status codes, error shape, pagination headers, media types, auth model, and cache behavior before frontend implementation.
4. Keep services as business authority; Jersey/JAX-RS resources deserialize/validate/delegate and translate results with `Response`, `Location`, `Link`, and DTO links built from `UriInfo`/builders.
5. Use vendor media types for representation/version negotiation only; do not hide unrelated operations behind `PATCH` + media-type switches.
6. Make auth stateless: initial `Authorization: Basic ...` may mint tokens, normal requests use `Authorization: Bearer <access-token>`, `401` is missing/invalid/expired credentials, `403` is authenticated-but-forbidden, and refresh tokens rotate on login/use instead of traveling in every response.
7. Follow TDD per slice: write the narrow failing API/resource tests first, verify the failure, implement only enough backend to pass, then widen to service/frontend/package tests as the slice grows.
8. Use `$paw-frontend-layer` for SPA source: routes, state, forms, API client, auth state, errors, i18n, frontend tests, and base-path source config for the same slice. If no frontend runner exists, define and run a repeatable browser/static-hosting smoke instead of leaving a prose-only gap.
9. Integrate the frontend build into Maven/WAR packaging only after the API/client boundary is clear. Verify lockfile/package-manager choice, reproducible install command, Node/npm source, output directory, `webapp` dependency on `frontend`, static hosting, base path, and cache/file revving so hashed assets can be immutable while `index.html`/root is revalidated.
10. Keep the old JSP flow available until the slice passes API + SPA + verification and rollback/parallel routing is explicit.

## Slice Definition Of Done

Before claiming a migrated slice is done:

- API contract tests cover status, body, media type, `Location`/`Link`/`ETag` headers where applicable, auth, validation, and Problem Details errors.
- Service tests cover business behavior only when behavior moved or changed.
- SPA route/client/state/form/i18n behavior is tested when a runner exists; otherwise a repeatable smoke covers the user-visible behavior and the test gap is documented.
- `/api/*` failures stay JSON/status API, while non-API deep links return `index.html`.
- Packaging changes pass through Maven, and the WAR contains backend classes plus current `index.html`, JS/CSS, and assets from the declared frontend output directory.
- The old MVC route is either still available, intentionally redirected/replaced, or has a stated rollback.

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
- The frontend build would change deploy/package commands.
- Basic/Bearer token storage, refresh rotation, or exposed auth headers are unresolved.
- Cache settings could make stale HTML or stale assets persist for users.
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
