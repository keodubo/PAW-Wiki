---
name: paw-persistence-layer
description: Use when creating, changing, auditing, or reviewing PAW Forkd SQL schemas, migrations, JDBC DAO implementations, persistence config, HSQLDB fixtures, or DAO tests.
---

# Paw Persistence Layer

## Overview

Use this for the `persistence/` module: schema, dialect scripts, JDBC DAOs, migrations, and persistence tests.

Read `references/layer-rules.md` before editing persistence.

## Workflow

1. Inspect `PersistenceConfig`, schema scripts, DAO contracts, and existing DAO/test style.
2. Keep schema and migrations in `persistence`, not `webapp`.
3. Update `schema-base.sql`, `schema-postgres.sql`, and `schema-hsqldb.sql` together when the schema changes.
4. Write or adjust DAO tests with HSQLDB before relying on service/web behavior.
5. Implement SQL with explicit columns, joins, filters, ordering, and pagination in the database.
6. Run targeted persistence tests, then a wider Maven check if contracts changed.

## JDBC Rules

- `java.sql` belongs only in this layer.
- RowMappers are `private static final`, not created per method call.
- `SimpleJdbcInsert` is initialized once, usually in the constructor.
- Avoid `SELECT *`.
- Avoid N+1: use `JOIN`, `WHERE IN`, and batch methods.
- Escape LIKE metacharacters `%` and `_` before queries that use user input.
- Use database cascades (`ON DELETE CASCADE`) instead of manual Java delete chains when appropriate.
- Do not filter, sort, join, or paginate in Java after loading unbounded rows.

## Test Rules

- DAO tests validate real DB state with HSQLDB, `JdbcTestUtils`, direct queries, and `@Rollback`.
- Precondition data comes from SQL fixtures or direct setup, not from the DAO under test.
- Tests must cover happy paths, empty cases, constraints, and relevant ordering/pagination.

## Verification

- Focused DAO test: `mvn -pl persistence -am -Dtest=<DaoTestName> test`.
- Persistence module: `mvn -pl persistence -am test`.
- Schema used by web startup: `mvn -pl webapp -am test` or `mvn clean test`.
