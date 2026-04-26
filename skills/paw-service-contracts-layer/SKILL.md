---
name: paw-service-contracts-layer
description: Use when creating, changing, auditing, or reviewing PAW Forkd service interfaces, command DTOs, public service contracts, business exceptions, or mail service contracts.
---

# Paw Service Contracts Layer

## Overview

Use this for `service-contracts/`, the public API between webapp and services.

Read `references/layer-rules.md` before changing service contracts.

## Workflow

1. Inspect existing service interfaces, DTOs, and exceptions in `service-contracts/src/main/java/ar/edu/itba/paw/services`.
2. Design the service method as a use-case boundary, not as a direct DAO wrapper.
3. Create command/data objects when passing many related values from webapp to services.
4. Put domain exceptions here when webapp advice must map them to HTTP status or inline errors.
5. Keep contracts independent from JSP paths, servlet types, Spring MVC forms, JDBC classes, and implementation details.
6. Migrate implementations, tests, and callers in the same slice when changing existing contracts.

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
