# PAW Models Layer Rules

## Source Pages

Read current code plus:

- `PAW-Wiki/docs/wiki/modelo-capas.md`
- `PAW-Wiki/docs/wiki/java-style.md`
- `PAW-Wiki/docs/wiki/persistencia-jdbc.md`
- `PAW-Wiki/docs/wiki/resumen-clases-paw-2026.md`
- `PAW-Wiki/docs/wiki/hibernate-jpa.md` when stage is TP2 or TP final.
- `PAW-Wiki/docs/wiki/resumen-enunciado.md`
- Feature-specific plan/spec pages when changing an existing flow.

## Allowed Responsibilities

- Domain entities and value objects.
- Enums for finite states, sort orders, filter states, and product modes.
- Shared pagination/filter/query data such as `Page`, `PageRequest`, and feature filter objects.
- Plain data with behavior that is domain-local and infrastructure-free.

## Forbidden In Models

- `java.sql.*`, JDBC, RowMapper, Spring annotations, servlet types, controller forms, JSP concepts, mail templates, or DAO/service implementations.
- Web validation rules that belong to form beans.
- Magic strings for finite product states when an enum fits.
- Sentinel IDs (`-1`, `0`) to represent absence when the schema/domain is nullable.

## Stage Rules

- TP1: keep models as plain domain types without JPA annotations; persistence mapping belongs to JDBC DAOs.
- TP2: JPA annotations may live on entities only after `$paw-tp2-migration` establishes the migration shape. Do not add them opportunistically during a TP1 feature.
- TP final: do not reuse domain models as REST payload DTOs by default; keep API representation decisions in service/web contracts unless the repo already chose otherwise.

## Type Conventions

- Use `LocalDate`, `LocalTime`, and `LocalDateTime` for date/time concepts.
- Use boxed types (`Long`, `Integer`, `Boolean`) only when null is a real domain state, such as legacy nullable columns.
- Keep model names singular.
- Prefer clear helper methods on enums when mapping query params or database values, but keep JDBC conversion itself in persistence.

## Existing Examples To Inspect

- `AcceptsReservationsStatus`
- `ReservationStatus`
- `ReservationSort`
- `ReviewSortOrder`
- `Page` and `PageRequest`
- `SocialFeedFilters`

## Review Checklist

- Does this type compile without webapp/services/persistence implementation dependencies?
- Is nullability explicit and testable?
- Are database-specific values translated at the layer boundary?
- Would a controller or DAO need less logic because this model/enums now express the concept?
