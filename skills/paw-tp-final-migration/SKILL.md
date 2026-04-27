---
name: paw-tp-final-migration
description: Use when planning, implementing, auditing, or reviewing PAW TP final migration to REST API plus SPA/frontend, including Jersey/JAX-RS resources, DTO contracts, status codes, stateless auth, frontend routing/state/forms/i18n/testing, Maven frontend build integration, static hosting, cache, and file revving.
---

# Paw TP Final Migration

## Overview

Use this when the user says the PAW project is in TP final, asks for REST/API work, SPA/frontend migration, Jersey/JAX-RS, stateless auth, frontend build tooling, routing/state, or asset caching. The goal is to split presentation into API + client without losing backend business authority.

Read `references/migration-rules.md` before editing or judging final-stage work.

## Required First Pass

1. Confirm the stage is `TP final` or the user explicitly asked for REST/SPA/frontend migration.
2. Read the app `CLAUDE.md`, `PAW-Wiki/docs/CLAUDE.md`, `PAW-Wiki/docs/index.md`, and `PAW-Wiki/docs/wiki/resumen-clases-paw-2026.md`.
3. Read `PAW-Wiki/docs/wiki/api-rest.md`, `single-page-applications.md`, `spring-security.md`, `internacionalizacion.md`, and `ux-flows.md`.
4. Capture baseline MVC routes, services, auth rules, views, tests, and packaging.
5. State whether the work is API-only, frontend-only, packaging-only, or end-to-end migration.

## Migration Workflow

1. Define API resources, DTOs, status codes, error shape, pagination/linking, and auth model before frontend implementation.
2. Keep services as business authority; REST resources deserialize/validate/delegate and translate results.
3. Add REST tests for status, headers, body, auth, validation, and error cases.
4. Build the SPA around routes, state, forms, error handling, i18n, and API client boundaries.
5. Add frontend tests when the repo has a runner; otherwise document manual contract checks.
6. Integrate the frontend build into Maven/WAR packaging only after the API/client boundary is clear.
7. Configure static hosting and cache/file revving so versioned assets can be immutable and root HTML is revalidated.

## Coordination

- Use `$paw-service-contracts-layer` for service/API boundary DTOs and exceptions.
- Use `$paw-services-layer` for backend business behavior used by REST resources.
- Use `$paw-webapp-layer` for REST resources, security config, API error mapping, SPA static hosting, and asset integration.
- Use `$paw-testing-layer` for API contract tests, frontend test strategy, and Maven/package gates.
- Use `$paw-models-layer` only for domain concepts, not as a dumping ground for API payloads.

## Stop Conditions

Ask before proceeding when:

- The user has not confirmed TP final and the task would introduce SPA, REST-only architecture, JWT/token auth, or frontend build tooling.
- Auth storage trade-offs are unresolved.
- Existing JSP routes must remain live in parallel but routing ownership is unclear.
- The frontend build would change deploy/package commands.
- Cache settings could make stale HTML or stale assets persist for users.

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
