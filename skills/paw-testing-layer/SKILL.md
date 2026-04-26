---
name: paw-testing-layer
description: Use when creating, changing, auditing, or reviewing PAW Forkd tests, Maven verification gates, test fixtures, HSQLDB persistence tests, service tests, MVC/security tests, template tests, or test failures.
---

# Paw Testing Layer

## Overview

Use this as the transversal testing skill for PAW/Forkd. It encodes the PAW-Wiki testing rules and the repo-specific Maven/test habits needed to avoid false confidence.

Read `references/testing-rules.md` before writing or judging tests.

## Test Selection

Choose the test type from the behavior under test:

- Business rule or state transition: service test.
- SQL, row mapping, constraints, ordering, pagination, or schema behavior: DAO/persistence test against HSQLDB.
- Route, binding, validation error, security rule, redirect, or JSP model contract: webapp MVC/security test.
- JSP escaping, scriptlets, i18n bundle symmetry, or rendered-template contract: template/i18n test.
- Runtime wiring, AOP proxy, scheduler, or app startup behavior: context/integration test or Jetty smoke.

## Core Rules

- Prefer behavior and state assertions over Mockito interaction assertions.
- Treat `verify()` / `never()` as a smell; use only when there is no cleaner observable state and explain why.
- DAO tests use HSQLDB, shared schema bootstrap, SQL fixtures/direct setup, `@Rollback`, and DB state assertions.
- Service tests mock DAOs but assert returned state, thrown exceptions, state transitions, or recorded side effects.
- MVC tests verify status, redirects, model/binding errors, security, and preserved GET state.
- Do not use the object under test to set its own preconditions.
- Cover happy path and unhappy/edge paths.
- A green build is not proof of wiki compliance; inspect test style and coverage against the rules.

## TDD Flow

When implementing a feature or bugfix, follow Superpowers `test-driven-development`:

1. Write one failing test for the next behavior.
2. Run the narrow command and verify the failure is for the expected reason.
3. Implement the smallest production change.
4. Re-run the narrow command until green.
5. Refactor only after green.
6. Run the broader gate for the changed modules.

## Maven Gates

Use the narrowest meaningful command first:

```bash
mvn -pl persistence -am -Dtest=<DaoTestName> test
mvn -pl services -am -Dtest=<ServiceTestName> test
mvn -pl webapp -am -Dtest=<MvcTestName> -Dsurefire.failIfNoSpecifiedTests=false test
mvn -pl webapp -am test
mvn clean test
mvn clean package
```

Use `-am` for tests that depend on upstream modules. For targeted webapp tests, include `-Dsurefire.failIfNoSpecifiedTests=false` so upstream modules without that test pattern do not fail before the target module runs.

## Review Checklist

- Does each test name describe a behavior, not an implementation detail?
- Does setup avoid using the subject under test?
- Does validation assert observable behavior or final state?
- Are DAO tests checking real DB state?
- Are service tests not pretending to prove Spring proxy/AOP behavior when they instantiate implementations directly?
- Does the final gate match the blast radius of the change?
