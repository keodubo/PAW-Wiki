---
name: paw-feature-master
description: Use when planning, implementing, auditing, or reviewing PAW Forkd features in a PAW app checkout across TP1, TP2, or TP final; routes by project stage, Maven modules, PAW-Wiki rules, Superpowers workflows, layer-specific skills, and migration skills.
---

# Paw Feature Master

## Overview

Use this as the entrypoint for PAW/Forkd work. It keeps implementation aligned with the checkout, PAW-Wiki, Maven layer boundaries, and the user's preference for safe, reversible changes.

## Required First Pass

1. Resolve the PAW app checkout. Prefer the current working directory; otherwise use `PAW_APP_REPO` if set, or ask for the path.
2. Read `<PAW_APP_REPO>/CLAUDE.md`.
3. Read `<PAW_APP_REPO>/PAW-Wiki/docs/CLAUDE.md` when the wiki is vendored beside the app; otherwise read the cloned PAW-Wiki repo's `docs/CLAUDE.md`.
4. Read the corresponding `docs/index.md`.
5. Resolve the project stage: `TP1`, `TP2`, or `TP final`. Use the user's explicit stage if provided; otherwise infer only when the task is unambiguous. Ask before changing stack assumptions.
6. Read the current code and tests for the affected flow before trusting older plans.
7. For substantial work, show a short plan before editing.

Use `references/project-map.md` for the current module map, canonical wiki pages, feature categories, and verification gates.

## Stage Routing

- **TP1**: Keep the classic stack: Spring Web MVC, JSP/JSTL, JDBC, HSQLDB, Spring Security, Logback, Maven WAR. Do not introduce JPA/Hibernate, SPA, REST-only architecture, or frontend build tooling unless the user explicitly asks.
- **TP2/TPE2**: Treat work as persistence migration or Hibernate/JPA adaptation when it touches entities, `EntityManager`, ORM config, fetch/cascade behavior, data/schema migration, TPE1 feedback corrections, or JDBC-to-JPA DAO changes. Use `$paw-tp2-migration` before layer skills for migration planning or implementation.
- **TP final**: Treat work as REST + SPA migration when it touches Jersey/JAX-RS/resources, DTO API contracts, stateless auth, frontend build, routing, client state, cache/file revving, or static hosting. Use `$paw-tp-final-migration` before layer skills for migration planning or implementation.

If the user says only "PAW" and the task is normal feature/debug work in the current Forkd checkout, default to TP1 constraints. If the user mentions Hibernate/JPA, REST, API, SPA, frontend module, Vite/React/Angular, JWT, or final delivery, stop and resolve stage before editing.

## Superpowers Routing

- New feature or behavior design: use `superpowers:brainstorming`, then this skill.
- Written multi-step implementation plan: use `superpowers:writing-plans`.
- Executing an existing checklist plan: use `superpowers:executing-plans`.
- Bug, failing test, or unexpected runtime behavior: use `superpowers:systematic-debugging`.
- Feature or bugfix implementation: use `superpowers:test-driven-development` unless the user explicitly asks for audit/plan only.
- Before claiming done: use `superpowers:verification-before-completion`.
- Before merge/PR or major handoff: use `superpowers:requesting-code-review`.

## Layer Skill Routing

- Domain POJOs, enums, value objects, pagination models: use `$paw-models-layer`.
- DAO interfaces and persistence contracts: use `$paw-persistence-contracts-layer`.
- SQL, schema, JDBC DAO implementations, HSQLDB tests: use `$paw-persistence-layer`.
- Service interfaces, command DTOs, domain exceptions: use `$paw-service-contracts-layer`.
- Business logic, transactions, mail, schedulers, service tests: use `$paw-services-layer`.
- Controllers, forms, validators, JSP/JSTL, i18n, Spring Security, CSS: use `$paw-webapp-layer`.
- Any new/changed tests, test failures, Maven verification strategy, fixtures, or test-quality review: use `$paw-testing-layer`.
- TP2 JDBC -> JPA/Hibernate migration: use `$paw-tp2-migration`, then the affected layer skills.
- TP final REST + SPA migration: use `$paw-tp-final-migration`, then the affected layer skills.

Load only the layer skills that match the task. For cross-layer features, move in dependency order: models -> persistence contracts -> persistence -> service contracts -> services -> webapp -> verification.

## Default Implementation Shape

Use the smallest reversible slice that proves the behavior:

1. Capture baseline: `git status --short --branch`, relevant tests, and affected code paths.
2. Decide the feature category: data/schema, business flow, auth/security, UI/form, mail/scheduler, docs/wiki, or mixed.
3. State the project stage and the allowed stack for that stage.
4. Define contracts before implementations when a layer boundary changes.
5. Keep controllers/resources thin and service methods transactional.
6. In TP1, keep SQL, joins, sorting, filtering, and pagination in JDBC persistence. In TP2, audit generated JPA SQL/fetches instead of assuming ORM performance.
7. Preserve GET state (`page`, `pageSize`, `sort`, filters, redirects) when editing navigation or filters.
8. Finish with the narrowest meaningful Maven check, then broader checks when contracts or multiple layers changed.

## Stop Conditions

Ask the user before proceeding when:

- The request changes stack choices: Spring Boot, React/SPA, Tailwind build tooling, module collapse, ORM/JPA for TP1.
- The project stage is ambiguous and the request mentions JPA/Hibernate, REST/API, SPA/frontend build, JWT/stateless auth, or migration.
- A data migration can lose user data or requires destructive local DB operations.
- Owner/admin/shared-form parity is ambiguous.
- A route, role, or product rule has competing plausible interpretations.
- The user asked for a plan/doc only.

## Sources

Canonical source order for PAW work:

1. Current checkout code, tests, and Maven config.
2. `CLAUDE.md`.
3. `PAW-Wiki/docs/index.md` and linked wiki pages.
4. Current durable plans under `PAW-Wiki/docs/superpowers/plans/`.
5. `PAW-Wiki/docs/wiki/resumen-enunciado-tpe2.md` for the official TP2 delivery contract when the stage is TP2/TPE2.
6. `PAW-Wiki/docs/wiki/resumen-clases-paw-2026.md` for stage-specific TP1/TP2/final class guidance.
6. Historical `PAW-Wiki/docs/raw/` sources, read-only.
