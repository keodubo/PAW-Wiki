# PAW Testing Rules

## Wiki Sources

Read current code plus:

- `PAW-Wiki/docs/wiki/testing-unitario.md`
- `PAW-Wiki/docs/wiki/resumen-clases-paw-2026.md`
- `PAW-Wiki/docs/wiki/comparacion-testing-servicios-y-daos.md`
- `PAW-Wiki/docs/wiki/transactional.md`
- `PAW-Wiki/docs/wiki/spring-aop.md`
- `PAW-Wiki/docs/wiki/persistencia-jdbc.md`
- `PAW-Wiki/docs/wiki/criterios-evaluacion.md`
- `PAW-Wiki/docs/wiki/hibernate-jpa.md` when stage is TP2.
- `PAW-Wiki/docs/wiki/api-rest.md` and `PAW-Wiki/docs/wiki/single-page-applications.md` when stage is TP final.

## PAW-Wiki Rules

- Testing quality is an evaluation criterion; insufficient or low-quality tests are penalized.
- Service tests validate business behavior and final state.
- DAO tests validate real database state, query behavior, mappings, constraints, ordering, and pagination.
- Persistence tests use HSQLDB as a lightweight test database.
- DAO test preconditions come from SQL fixtures/direct DB setup, not from the DAO under test.
- Use `@Rollback` or explicit cleanup so tests are independent.
- `verify()` / `never()` validates implementation coupling and is discouraged by the catedra.
- Tests must cover happy and unhappy paths.
- Tests that only prove "it does not crash" are acceptable only when that is the explicit behavior under test.

## Test Type Map

| Test type | Object under test | Dependencies | Good assertions | Avoid |
| --- | --- | --- | --- | --- |
| Service unit | Use case/service facade | Mock DAOs, fakes for mail/side effects | return value, exception, state transition, recorded fake data | `verify()` as primary proof |
| DAO integration | JDBC DAO/query/schema | HSQLDB + schema + SQL fixtures | row count, selected values, ordering, constraints | using DAOs to seed same DAO test |
| MVC/security | Controller route and web contract | Mock services/security context | status, redirect, model, binding errors, access | business logic assertions |
| Template/i18n | JSP/message bundle safety | file scan or view test | no scriptlets, escaping, keys present | treating bundle symmetry as wording audit |
| Context/runtime | Spring wiring/AOP/startup | Spring context or Jetty | proxy/wiring/startup behavior | service unit test as proxy proof |
| TP2 JPA persistence | Entity mapping/fetch/cascade/dirty checking | JPA context + test DB | persisted state, generated SQL shape, lazy/fetch behavior | assuming compile proves mapping safety |
| TP final API | REST resource contract | MockMvc/Jersey test + service fakes | status, headers, DTO body, auth errors | returning domain objects directly |
| TP final frontend | SPA state/component/forms | frontend test runner when present | state transitions, validation, render contract | relying only on backend tests |

## Repo-Specific Maven Notes

Use targeted tests first, then widen:

```bash
mvn -pl persistence -am -Dtest=<DaoTestName> test
mvn -pl services -am -Dtest=<ServiceTestName> test
mvn -pl webapp -am -Dtest=<MvcTestName> -Dsurefire.failIfNoSpecifiedTests=false test
mvn -pl webapp -am test
mvn clean test
```

Why:

- `-am` rebuilds upstream modules so stale signatures do not produce false failures.
- `-Dsurefire.failIfNoSpecifiedTests=false` prevents upstream modules from failing before the target webapp test runs.
- Full `mvn clean test` is the final arbiter for multi-layer changes.

## Service Test Patterns

Prefer:

- `when(...).thenReturn(...)` to shape DAO answers.
- `thenAnswer(...)` to capture values when the returned state is what matters.
- Recording fakes such as `RecordingMailService` for side effects.
- Assertions on returned object, thrown exception, rows in notification tables, or fake contents.

Avoid:

- `verify(dao).method(...)` as the only proof.
- `verifyNoInteractions(...)` as a proxy for behavior.
- Asserting internal call order unless the product behavior is ordering itself.

## DAO Test Patterns

Prefer:

- `@Sql("/sql/fixture.sql")` or direct JDBC setup.
- `JdbcTestUtils.countRowsInTable(...)`.
- Direct `SELECT` assertions for mapped fields and ordering.
- Edge cases: empty results, invalid constraints, duplicate keys, nullable legacy data.

Avoid:

- Calling the DAO under test to create preconditions.
- Tests that execute a method but never inspect DB state.
- Assuming Postgres-only DDL works in HSQLDB.

## AOP And Transactions

- Unit tests that instantiate service implementations do not prove `@Transactional`, `@Async`, or proxy behavior.
- Proxy-sensitive behavior needs Spring context/integration coverage or explicit startup smoke.
- Self-invocation risks are not caught by simple unit tests; inspect the code path and add context tests when the proxy boundary matters.

## Commit-Readiness Scan

For work touching tests or service behavior, consider:

```bash
rg -n "verify\\(|verifyNoInteractions\\(|ArgumentCaptor" services/src/test webapp/src/test persistence/src/test
git diff --check
```

Do not automatically delete every `verify()` found; decide whether each one is implementation coupling or a justified interaction assertion.
