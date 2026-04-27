---
name: paw-persistence-layer
description: "Use when creating, changing, auditing, or reviewing PAW Forkd persistence: TP1 SQL schemas, migrations, JDBC DAO implementations, HSQLDB fixtures, DAO tests, or TP2 JPA/Hibernate persistence implementation after migration routing."
---

# Paw Persistence Layer

## Overview

Use this for the `persistence/` module. In TP1 it means schema, dialect scripts, JDBC DAOs, migrations, and persistence tests. In TP2 it may mean JPA/Hibernate DAOs and ORM config after `$paw-tp2-migration` has set the migration plan.

Read `references/layer-rules.md` before editing persistence.

## Workflow

1. Inspect `PersistenceConfig`, schema scripts, DAO contracts, and existing DAO/test style.
2. Resolve stage: TP1 uses JDBC; TP2 uses JPA/Hibernate only when the user is in that stage or explicitly asks for migration.
3. Keep schema and migrations in `persistence`, not `webapp`.
4. Update `schema-base.sql`, `schema-postgres.sql`, and `schema-hsqldb.sql` together when the TP1 schema changes.
5. Write or adjust DAO/persistence tests before relying on service/web behavior.
6. In TP1, implement SQL with explicit columns, joins, filters, ordering, and pagination in the database. In TP2, inspect generated SQL/fetches and avoid hidden N+1.
7. Run targeted persistence tests, then a wider Maven check if contracts changed.

## JDBC Rules

- `java.sql` belongs only in this layer.
- RowMappers are `private static final`, not created per method call.
- `SimpleJdbcInsert` is initialized once, usually in the constructor.
- Avoid `SELECT *`.
- Avoid N+1: use `JOIN`, `WHERE IN`, and batch methods.
- Escape LIKE metacharacters `%` and `_` before queries that use user input.
- Use database cascades (`ON DELETE CASCADE`) instead of manual Java delete chains when appropriate.
- Do not filter, sort, join, or paginate in Java after loading unbounded rows.

## TP2 JPA Rules

- Use `$paw-tp2-migration` before introducing `EntityManager`, JPA annotations, or Spring ORM config.
- Do not expose `EntityManager`, Hibernate `Session`, lazy proxies, or JPQL strings outside persistence.
- Review fetch type, cascade, orphan removal, optionality, and enum mapping for each relation.
- Treat Hibernate schema auto-update as a migration risk; do not enable destructive or production-affecting behavior without explicit approval.
- Inspect generated SQL/logs for list pages and relationship-heavy flows.

## Test Rules

- DAO tests validate real DB state with HSQLDB, `JdbcTestUtils`, direct queries, and `@Rollback`.
- Precondition data comes from SQL fixtures or direct setup, not from the DAO under test.
- Tests must cover happy paths, empty cases, constraints, and relevant ordering/pagination.

## Verification

- Focused DAO test: `mvn -pl persistence -am -Dtest=<DaoTestName> test`.
- Persistence module: `mvn -pl persistence -am test`.
- Schema used by web startup: `mvn -pl webapp -am test` or `mvn clean test`.
