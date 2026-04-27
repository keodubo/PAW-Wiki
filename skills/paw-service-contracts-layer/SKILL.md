---
name: paw-service-contracts-layer
description: Use when creating, changing, auditing, or reviewing PAW Forkd service interfaces, command DTOs, public service contracts, business exceptions, mail service contracts, or API-facing service boundaries across TP1, TP2, and TP final.
---

# Paw Service Contracts Layer

## Overview

Use this for `service-contracts/`, the public API between webapp and services.

Read `references/layer-rules.md` before changing service contracts.

## Workflow

1. Inspect existing service interfaces, DTOs, and exceptions in `service-contracts/src/main/java/ar/edu/itba/paw/services`.
2. Resolve stage: TP1 contracts serve MVC/JSP flows; TP2 should preserve business use cases while persistence changes; TP final may need DTO/API boundaries.
3. Design the service method as a use-case boundary, not as a direct DAO wrapper.
4. Create command/data objects when passing many related values from webapp to services.
5. Put domain exceptions here when webapp advice or REST resources must map them to HTTP status or inline/API errors.
6. Keep contracts independent from JSP paths, servlet types, Spring MVC forms, JDBC/JPA classes, frontend state, and implementation details.
7. Migrate implementations, tests, and callers in the same slice when changing existing contracts.

## Contract Rules

- Services own business use cases; DAOs own storage mechanics.
- DTOs such as `SaveRestaurantData`, `CreateReservationData`, and `ChangePasswordData` make controllers thinner.
- Exceptions should be specific enough for `ErrorHandlingAdvice` and tests to assert behavior without string parsing.
- Do not split interface contracts around async implementation details such as `MailService` vs `AsyncMailService`.
- Keep locale-sensitive mail contracts explicit about recipient locale.

## Verification

- Contract-only compile: `mvn -pl service-contracts -am test`.
- Contract plus implementation: `mvn -pl services -am test`.
- Contract used from web forms/controllers: `mvn -pl webapp -am test`.
