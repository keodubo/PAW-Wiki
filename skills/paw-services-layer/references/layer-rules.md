# PAW Services Layer Rules

## Source Pages

Read current services/tests plus:

- `PAW-Wiki/docs/wiki/transactional.md`
- `PAW-Wiki/docs/wiki/spring-aop.md`
- `PAW-Wiki/docs/wiki/resumen-clases-paw-2026.md`
- `PAW-Wiki/docs/wiki/hibernate-jpa.md` when stage is TP2.
- `PAW-Wiki/docs/wiki/api-rest.md` when stage is TP final.
- `PAW-Wiki/docs/wiki/logica-en-controllers.md`
- `PAW-Wiki/docs/wiki/mailing.md`
- `PAW-Wiki/docs/wiki/manejo-excepciones.md`
- `PAW-Wiki/docs/wiki/testing-unitario.md`
- `PAW-Wiki/docs/wiki/comparacion-testing-servicios-y-daos.md`

## Responsibilities

- Business use cases and orchestration.
- Transaction boundaries.
- Domain state transitions.
- Mail rendering/sending as service behavior.
- Scheduler jobs that delegate into services.
- Business tests with mocked DAO dependencies or controlled fakes.

## Transaction Rules

- Public service methods should be annotated with `@Transactional`.
- Read methods use `@Transactional(readOnly = true)`.
- Do not expect `@Transactional` to work on private methods or self-invocation.
- In TP2, transaction boundaries also define JPA persistence context lifetime. Be explicit about where managed entities are loaded and changed.
- Scheduler entrypoints should delegate to transactional services.
- Mail-only async classes do not get class-level transactions unless they intentionally use DB state.

## Layer Rules

- Services can create/modify domain models and call DAOs.
- Services must not know JSP templates as web paths, controllers, request/response, redirects, or SQL mechanics.
- Services must not know `EntityManager`, Hibernate sessions, lazy proxy mechanics, REST serialization annotations, frontend stores, or router state.
- Services should not perform route authorization that belongs in Spring Security/AccessHelper, but they should enforce product invariants such as self-follow rejection or invalid state transition.
- Mail uses recipient locale/preferred language, not sender locale or `LocaleContextHolder`.

## Test Rules

- Avoid Mockito `verify()` as the primary assertion.
- Assert return value, state, row counts, notification records, or effects captured by fakes.
- Use `RecordingMailService` or similar recording fakes where already established.
- Cover edge cases and unhappy paths, not only happy path.

## Existing Examples To Inspect

- `ReservationServiceImpl`
- `ReservationSchedulerImpl`
- `SocialServiceImpl`
- `RestaurantServiceImpl`
- `SmtpMailServiceImpl`

## Common Verification Commands

```bash
mvn -pl services -am -Dtest=<ServiceTestName> test
mvn -pl services -am test
mvn -pl webapp -am test
mvn clean test
```
