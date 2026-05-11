# PAW TP2 Migration Rules

## Source Pages

Read current code plus:

- `PAW-Wiki/docs/wiki/resumen-clases-paw-2026.md`
- `PAW-Wiki/docs/wiki/tp1-vs-tpe2-final.md`
- `PAW-Wiki/docs/wiki/paw-unidad-09-hibernate-jpa.md`
- `PAW-Wiki/docs/wiki/paw-unidad-08-aop-transacciones.md`
- `PAW-Wiki/docs/wiki/hibernate-jpa.md`
- `PAW-Wiki/docs/wiki/persistencia-jdbc.md`
- `PAW-Wiki/docs/wiki/modelo-capas.md`
- `PAW-Wiki/docs/wiki/transactional.md`
- `PAW-Wiki/docs/wiki/testing-unitario.md`
- `PAW-Wiki/docs/wiki/comparacion-testing-servicios-y-daos.md`

## Stage Contract

- TP2 changes persistence mechanics, not the whole product surface by default.
- The migration path is JDBC DAO behavior -> JPA entity mapping -> `EntityManager` DAO implementation -> service/test adaptation.
- Do not introduce SPA, REST-only architecture, or frontend build tooling as part of TP2 unless the user explicitly asks.
- Dependency versions in class PDFs are historical. Use the checkout, enunciado, and current compatibility for actual versions.

## Entity Mapping Checklist

- Table and column names match the current schema.
- Id strategy matches existing sequences/identity behavior.
- Nullability and boxed vs primitive fields match real data.
- Enums use a deliberate mapping, usually string for readability.
- Relationships declare required vs optional explicitly.
- Fetch choices are reviewed for list/detail use cases.
- Cascades and orphan removal match ownership semantics.
- Default constructor exists only for Hibernate when needed.

## DAO And Contract Rules

- Keep DAO interfaces free of `EntityManager`, Hibernate `Session`, JPQL strings, lazy proxy types, and fetch details.
- Preserve service-facing behavior unless the user approved a contract change.
- Do not rely on generated lazy loading to replace explicit query design for list pages.
- If JDBC and JPA implementations coexist during migration, make bean selection explicit.
- Keep pagination, filtering, and sorting efficient; verify generated SQL for heavy flows.

## Service And Transaction Rules

- Service methods remain the transaction boundary.
- `@Transactional(readOnly = true)` matters more once JPA entities and persistence context are involved.
- Avoid self-invocation when a method expects transactional/proxy behavior.
- Do not pass detached entities around and expect lazy properties to work later.
- Do not expose merge/reattach mechanics to webapp or service contracts unless intentionally designed.

## Test Rules

- Add persistence tests that verify actual database state and mapping behavior.
- Add tests for relationships, nullable data, enum values, pagination/sorting, and update behavior.
- Dirty checking must be proven by observable persisted state when relied on.
- Lazy/fetch behavior needs a test or explicit SQL/log review for risky flows.
- Service unit tests do not prove Spring ORM wiring; use context/integration coverage when config/proxy behavior matters.

## Rollback Strategy

- Migrate one DAO/aggregate per slice.
- Keep the old JDBC implementation available until the JPA slice is verified, unless the user asks for a clean cut.
- Avoid destructive SQL. If schema changes are required, make them additive first and document rollback.
