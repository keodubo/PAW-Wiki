---
name: paw-persistence-contracts-layer
description: Use when creating, changing, auditing, or reviewing PAW Forkd DAO interfaces and persistence-facing contracts in the persistence-contracts Maven module.
---

# Paw Persistence Contracts Layer

## Overview

Use this for `persistence-contracts/`, the module that lets services depend on DAO abstractions instead of JDBC implementations.

Read `references/layer-rules.md` before changing DAO contracts.

## Workflow

1. Inspect existing DAO interfaces in `persistence-contracts/src/main/java/ar/edu/itba/paw/persistence`.
2. Define the contract from the service use case, not from a planned SQL implementation.
3. Keep signatures free of `java.sql`, `JdbcTemplate`, row mappers, servlet/web types, and implementation classes.
4. Prefer `PageRequest`, sort/filter domain objects, and domain return types over loose offset/limit or raw maps.
5. Add batch contracts when a web/service flow would otherwise call a DAO once per row.
6. Update persistence implementation and tests in the same feature slice unless the user explicitly asks for contracts only.

## Contract Rules

- One table should have one owning DAO for writes.
- DAO contracts can expose query intent but not SQL plumbing.
- Return empty lists/pages instead of `null`.
- Use `Optional` only if the existing DAO style supports it for that absence case.
- Keep names behavior-oriented: `findByRestaurantId`, `countFeedReviews`, `findReassignableForReservations`.

## Verification

- Contract-only compile: `mvn -pl persistence-contracts -am test`.
- Contract plus JDBC implementation: `mvn -pl persistence -am test`.
- Contract used by services/webapp: `mvn -pl webapp -am test` or broader.
