---
name: paw-services-layer
description: Use when creating, changing, auditing, or reviewing PAW Forkd service implementations, business rules, transactions, scheduler jobs, mail rendering/sending, or services-layer tests.
---

# Paw Services Layer

## Overview

Use this for `services/`: business use cases, transactions, mail, schedulers, and service tests.

Read `references/layer-rules.md` before changing services.

## Workflow

1. Inspect the service contract, implementation, DAO contracts, and existing tests for the use case.
2. Put business decisions in one service method that controllers can call once.
3. Annotate public service methods with `@Transactional`; selectors use `@Transactional(readOnly = true)`.
4. Let services create/update domain models and orchestrate DAOs.
5. Keep JSP paths, request objects, controller redirects, and SQL out of services.
6. Add tests that assert returned state, database-observable side effects, or fake/recorded mail behavior.

## Business Rules

- Use DAOs for persistence; do not join/filter persisted datasets in Java.
- Do not depend on `LocaleContextHolder` for recipient mail language; use the recipient preference/locale.
- Scheduler jobs delegate into transactional service methods.
- Avoid transactional self-invocation (`this.otherMethod()` expecting proxy behavior).
- Do not put `@Transactional` on mail-only async classes unless they use DB state deliberately.
- Authorization and ownership should be declarative in web security when it is route access; services can enforce domain invariants that are not route permissions.

## Test Rules

- Avoid Mockito `verify()`/`never()`/ `spy()` for implementation coupling.
- Assert final return values, state transitions, DAO-visible changes, notification rows, or recording fakes.
- Cover happy paths and edge cases.
- Keep service tests about business logic; DAO SQL belongs in persistence tests.

## Verification

- Focused service tests: `mvn -pl services -am -Dtest=<ServiceTestName> test`.
- Services module: `mvn -pl services -am test`.
- If contracts/web callers changed: `mvn -pl webapp -am test` or `mvn clean test`.
