---
name: paw-models-layer
description: Use when creating, changing, auditing, or reviewing PAW Forkd domain models, enums, value objects, pagination types, shared immutable filter objects, or TP2 JPA entity annotations in the models Maven module.
---

# Paw Models Layer

## Overview

Use this for `models/`, the infrastructure-free domain module shared by web, services, and persistence.

Read `references/layer-rules.md` before editing models.

## Workflow

1. Inspect the existing model style in `models/src/main/java/ar/edu/itba/paw/models`.
2. Resolve stage from `$paw-feature-master`: TP1 keeps infrastructure-free POJOs; TP2 may allow JPA annotations only as part of a planned migration; TP final may add API-facing DTO pressure but should not turn models into transport objects.
3. Decide whether the concept is a domain entity, enum, value object, filter object, sort enum, or pagination type.
4. Keep the type free of Spring, JDBC, servlet, JSP, validation annotations tied to web forms, and implementation package dependencies.
5. Preserve nullability deliberately: use boxed values only when the database/domain allows absence.
6. Use Java time types (`LocalDate`, `LocalTime`, `LocalDateTime`) instead of `java.sql.*`.
7. Re-run compile/tests for the smallest module set that imports the changed model.

## Design Rules

- Put finite states in enums, not magic strings.
- Keep entities in singular names.
- Do not expose persistence implementation decisions through model APIs.
- Prefer explicit constructors/getters/setters matching the repo style.
- If overriding `equals`, also override `hashCode`.
- Avoid `Optional` as a field; use it only as a return type when the surrounding API already does.

## Verification

- Models-only change: `mvn -pl models test`.
- Shared model used by contracts or persistence: `mvn -pl persistence -am test`.
- Cross-layer model change: `mvn -pl webapp -am test` or `mvn clean test` when behavior spans modules.
