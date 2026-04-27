---
name: paw-services-layer
description: Use when creating, changing, auditing, or reviewing PAW Forkd service implementations, business rules, transactions, scheduler jobs, mail rendering/sending, services-layer tests, or service behavior affected by TP2 JPA/TP final API migrations.
---

# Paw Services Layer

## Overview

Use this for `services/`: business use cases, transactions, mail, schedulers, and service tests.

Read `references/layer-rules.md` before changing services.

## Workflow

1. Inspect the service contract, implementation, DAO contracts, and existing tests for the use case.
2. Resolve stage: TP1 services orchestrate JDBC DAOs; TP2 services may receive managed JPA entities but must not depend on ORM APIs; TP final services back REST resources and SPA flows.
3. Put business decisions in one service method that controllers/resources can call once.
4. Annotate public service methods with `@Transactional`; selectors use `@Transactional(readOnly = true)`.
5. Let services create/update domain models and orchestrate DAOs.
6. Keep JSP paths, request objects, controller redirects, SQL, `EntityManager`, and frontend state out of services.
7. Add tests that assert returned state, database-observable side effects, or fake/recorded mail behavior.

## Business Rules

- Use DAOs for persistence; do not join/filter persisted datasets in Java.
- Do not depend on `LocaleContextHolder` for recipient mail language; use the recipient preference/locale.
- Scheduler jobs delegate into transactional service methods.
- Avoid transactional self-invocation (`this.otherMethod()` expecting proxy behavior).
- In TP2, avoid relying on lazy loading side effects outside clear transaction boundaries.
- Do not put `@Transactional` on mail-only async classes unless they use DB state deliberately.
- Authorization and ownership should be declarative in web security when it is route access; services can enforce domain invariants that are not route permissions.

## Test Rules

- Avoid Mockito `verify()`/`never()` for implementation coupling.
- Assert final return values, state transitions, DAO-visible changes, notification rows, or recording fakes.
- Cover happy paths and edge cases.
- Keep service tests about business logic; DAO SQL belongs in persistence tests.

## Verification

- Focused service tests: `mvn -pl services -am -Dtest=<ServiceTestName> test`.
- Services module: `mvn -pl services -am test`.
- If contracts/web callers changed: `mvn -pl webapp -am test` or `mvn clean test`.
