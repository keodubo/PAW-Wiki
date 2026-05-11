# PAW Testing Rules

## Precedence

When old wiki text and recent professor feedback disagree, use the stricter rule below. The current PAW testing contract is blackbox: prove behavior and state, never implementation calls.

## Wiki Sources

Read current code plus:

- `PAW-Wiki/docs/wiki/testing-unitario.md`
- `PAW-Wiki/docs/wiki/resumen-clases-paw-2026.md`
- `PAW-Wiki/docs/wiki/comparacion-testing-servicios-y-daos.md`
- `PAW-Wiki/docs/wiki/transactional.md`
- `PAW-Wiki/docs/wiki/spring-aop.md`
- `PAW-Wiki/docs/wiki/persistencia-jdbc.md`
- `PAW-Wiki/docs/wiki/criterios-evaluacion.md`
- `PAW-Wiki/docs/wiki/resumen-enunciado-tpe2.md` when stage is TP2.
- `PAW-Wiki/docs/wiki/hibernate-jpa.md` when stage is TP2.
- `PAW-Wiki/docs/wiki/api-rest.md` and `PAW-Wiki/docs/wiki/single-page-applications.md` when stage is TP final.

## PAW-Wiki Rules

- Testing quality is an evaluation criterion; insufficient or low-quality tests are penalized.
- For TPE2/TP2, tests must also protect the migration contract: JDBC -> JPA/Hibernate without functional regression or data loss.
- Tests are blackbox. Do not inspect private methods, internal call order, or collaborator calls as proof.
- Each test covers one scenario with one main action. A test name with "and" is a split signal.
- Do not leave tests without assertions. `assertDoesNotThrow` is only valid when "no exception" is the explicit public behavior.
- Service tests validate business behavior and final state.
- DAO tests validate real database state, query behavior, mappings, constraints, ordering, and pagination.
- Persistence tests use HSQLDB as a lightweight test database.
- DAO test preconditions come from SQL fixtures/direct DB setup, not from the DAO under test.
- Use `@Rollback` or explicit cleanup so tests are independent.
- `verify()`, `never()`, `verifyNoInteractions()`, `spy()`, `lenient()`-driven overmocking, and hand-made call counters are forbidden unless the user explicitly asks for a temporary diagnostic spike. Remove them from committed tests.
- Never test private methods. Do not use reflection, `Class.forName`, or private-access helpers to reach internals.
- Do not call more than one public method on the same subject under test. For example, if testing `create`, validate with direct SQL/result/fake state, not `findById` on the same DAO/service.
- Tests must cover happy and unhappy paths.
- Parameterized tests are preferred when the same behavior must hold for several inputs.

## Test Type Map

| Test type | Object under test | Dependencies | Good assertions | Avoid |
| --- | --- | --- | --- | --- |
| Service unit | Use case/service facade | Mock DAOs, fakes for mail/side effects | return value, custom exception, state transition, recorded fake data | pass-through tests, `verify`, `spy`, call counters |
| DAO integration | JDBC DAO/query/schema | HSQLDB + schema + SQL fixtures | row count, selected values, ordering, constraints, expected actor/user | using DAOs to seed or validate the same DAO test |
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
mvn clean package
```

Why:

- `-am` rebuilds upstream modules so stale signatures do not produce false failures.
- `-Dsurefire.failIfNoSpecifiedTests=false` prevents upstream modules from failing before the target webapp test runs.
- Full `mvn clean test` is the final arbiter for multi-layer changes.
- `mvn clean package` is required when TP2 ORM config, schema bootstrap, or webapp packaging/deploy behavior changed.

## Service Test Patterns

Prefer:

- `when(...).thenReturn(...)` to shape DAO answers.
- `thenAnswer(...)` to capture values when the returned state is what matters.
- Recording fakes only when the fake exposes a real product-side effect, not a collaborator call count.
- Assertions on returned object, thrown exception, rows in notification tables, or fake contents.
- Custom domain exceptions for business failures. Do not assert generic Java exceptions when the production contract should be domain-specific.
- `assertDoesNotThrow` for void behavior only when the public contract is "succeeds without exception" and there is no meaningful state to inspect.

Avoid:

- `verify(dao).method(...)`, `verifyNoInteractions(...)`, `spy(...)`, or any equivalent fake/counter that only proves a method was called.
- Testing services that just delegate to a DAO. If there is no business rule, the useful test is usually DAO/web contract coverage or no test.
- Multiple public calls on the same service in one test.
- Tests for schedulers as scheduling mechanics. If testing a scheduler entrypoint is unavoidable, treat it as a normal public method and assert no exception or observable state, not the cron trigger.

## DAO Test Patterns

Prefer:

- `@Sql("/sql/fixture.sql")` or direct JDBC setup.
- `JdbcTestUtils.countRowsInTable(...)`.
- Direct `SELECT` assertions for mapped fields, ordering, and DB state after writes.
- Assert ownership/actor columns when the behavior saves data "for a user" or "by a user".
- Edge cases: empty results, invalid constraints, duplicate keys, nullable legacy data.
- One DAO method under test per test. For setup use SQL; for assertions use SQL/JdbcTestUtils.

Avoid:

- Calling the DAO under test to create preconditions.
- Calling another method on the same DAO to validate the result of the method under test.
- Double create: do not set up a create test by calling create first. Seed directly with SQL.
- Tests that execute a method but never inspect DB state.
- Assuming Postgres-only DDL works in HSQLDB.

## Controller, Mail, And Architecture Signals

- Controllers should stay simple: bind, validate, delegate once to a service/use case, and return view/redirect. If a controller performs several service calls for one process, flag the production design before writing more controller tests.
- Business logic, logging for business events, mail orchestration, and multi-step processes belong in services. Tests should expose misplaced logic instead of encoding it as acceptable controller behavior.
- Pagination/filtering/sorting that comes from DB data should be validated at DAO/query level when there is meaningful persistence logic.
- Mail tests should prefer public outcome: no exception for direct mail rendering/sending contracts, inserted notification rows for transactional mail workflows, or generated message content if the renderer exposes it. Do not prove mail behavior with `verify(mailService).send...`.
- Async `try/catch` blocks that only log and swallow are production smells; do not add tests that bless useless catches.

## Repairing Existing Tests

Use this scan before a PAW test-quality cleanup:

```bash
rg -n "verify\\(|verifyNoInteractions\\(|spy\\(|lenient\\(|ArgumentCaptor|ReflectionTestUtils|setAccessible|Class\\.forName|find.*Calls|Calls\\)|assertEquals\\(0, .*Calls|assertDoesNotThrow|assertAll" \
  persistence/src/test services/src/test webapp/src/test
```

For each hit:

1. Identify the public behavior the test should prove.
2. Delete tests that only prove a collaborator call, scheduler trigger, or pass-through wrapper.
3. Replace service interaction checks with returned value, custom exception, domain state, DB row, or meaningful fake state.
4. Replace DAO setup/validation through the same DAO with SQL fixtures and direct SQL/JdbcTestUtils assertions.
5. Split tests with multiple actions, multiple scenarios, or "and" in the name.
6. Remove reflection/private-method tests and cover the same rule through public methods.
7. Re-run the narrow Maven command, then the broader gate for the touched modules.

## AOP And Transactions

- Unit tests that instantiate service implementations do not prove `@Transactional`, `@Async`, or proxy behavior.
- Proxy-sensitive behavior needs Spring context/integration coverage or explicit startup smoke.
- Self-invocation risks are not caught by simple unit tests; inspect the code path and add context tests when the proxy boundary matters.

## Commit-Readiness Scan

For work touching tests or service behavior, consider:

```bash
rg -n "verify\\(|verifyNoInteractions\\(|spy\\(|lenient\\(|ReflectionTestUtils|setAccessible|Class\\.forName|find.*Calls|assertEquals\\(0, .*Calls" services/src/test webapp/src/test persistence/src/test
git diff --check
```

Committed PAW tests should not keep `verify`, `spy`, reflection, or hand-made interaction counters. If a behavior cannot be observed, document the gap and prefer deleting the low-value test over locking in whitebox testing.
