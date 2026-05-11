---
name: paw-tp2-migration
description: Use when planning, implementing, auditing, or reviewing PAW TP2 migration from TP1 JDBC persistence to JPA/Hibernate, including entity mapping, Spring ORM config, EntityManager DAOs, transaction manager changes, fetch/cascade reviews, generated SQL/performance audits, and persistence tests.
---

# Paw TP2 Migration

## Overview

Use this when the user says the PAW project is in TP2, asks for Hibernate/JPA/ORM work, or wants to migrate persistence away from TP1 JDBC. The goal is a controlled persistence migration, not a broad rewrite of webapp or product behavior.

Read `references/migration-rules.md` before editing or judging migration work.

## Required First Pass

1. Confirm the stage is `TP2` or the user explicitly asked for Hibernate/JPA.
2. Read the app `CLAUDE.md`, `PAW-Wiki/docs/CLAUDE.md`, `PAW-Wiki/docs/index.md`, and `PAW-Wiki/docs/wiki/resumen-clases-paw-2026.md`.
3. Read `PAW-Wiki/docs/wiki/paw-unidad-09-hibernate-jpa.md`, `PAW-Wiki/docs/wiki/hibernate-jpa.md`, `persistencia-jdbc.md`, `transactional.md`, and `testing-unitario.md`.
4. If the migration depends on transaction/proxy behavior, also read `PAW-Wiki/docs/wiki/paw-unidad-08-aop-transacciones.md`.
5. Capture baseline code and tests for the DAOs/entities being migrated.
6. State which behavior must stay identical and which persistence mechanics are allowed to change.

## First TP2 Session Checklist

When the user says TP1 is finished and they are starting TP2, do this before editing:

1. Inventory current JDBC DAOs, DAO contracts, schema scripts, row mappers, service callers, and persistence tests.
2. Identify the smallest safe first aggregate to migrate.
3. Decide whether JDBC and JPA implementations will coexist temporarily, and how Spring bean selection will be explicit.
4. Plan entity mappings against the existing schema before adding Hibernate config.
5. Define verification gates for the slice: focused persistence test, affected service tests, then wider Maven gate.

## Migration Workflow

1. Audit existing JDBC DAO contracts, SQL, schema, row mappings, service callers, and tests.
2. Decide whether contracts stay stable or need explicit service-facing changes.
3. Map entities deliberately: ids/sequences, table/column names, nullability, enum storage, and relationships.
4. Configure Spring ORM and `JpaTransactionManager` only after the entity/DAO slice is defined.
5. Implement one DAO or aggregate at a time with `EntityManager`.
6. Review generated SQL, fetch behavior, cascades, orphan removal, and lazy loading boundaries.
7. Update tests to prove persisted state and service behavior, not just compilation.
8. Run the smallest Maven gate first, then widen when contracts or wiring changed.

## Coordination

- Use `$paw-models-layer` for entity/model annotations and enum/nullability choices.
- Use `$paw-persistence-contracts-layer` when DAO interfaces change.
- Use `$paw-persistence-layer` for JPA DAO implementation, Spring ORM config, schema, and persistence tests.
- Use `$paw-services-layer` when transaction boundaries or managed entity behavior affects service logic.
- Use `$paw-testing-layer` for JPA persistence tests, generated SQL checks, and verification gates.

## Stop Conditions

Ask before proceeding when:

- The user has not confirmed TP2 and the task would introduce JPA/Hibernate.
- Schema generation or migration can rewrite/drop production data.
- A lazy-loading strategy would require OpenEntityManagerInView or longer-lived sessions.
- A contract change would force broad webapp or product behavior changes.
- Dependency versions are unclear; do not copy old PDF versions as current guidance.

## Verification

Use repo-specific commands where available:

```bash
mvn -pl models test
mvn -pl persistence-contracts -am test
mvn -pl persistence -am test
mvn -pl services -am test
mvn -pl webapp -am test
mvn clean test
```

For a narrow DAO test:

```bash
mvn -pl persistence -am -Dtest=<PersistenceTestName> test
```
