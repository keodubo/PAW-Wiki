# PAW Persistence Contracts Layer Rules

## Source Pages

Read current DAO interfaces plus:

- `PAW-Wiki/docs/wiki/modelo-capas.md`
- `PAW-Wiki/docs/wiki/comparacion-capas-web-services-persistence.md`
- `PAW-Wiki/docs/wiki/persistencia-jdbc.md`
- `PAW-Wiki/docs/wiki/n-plus-1-joins-java.md`
- `PAW-Wiki/docs/wiki/configuracion-maven.md`

## Allowed Responsibilities

- DAO interfaces under `ar.edu.itba.paw.persistence`.
- Storage-oriented operations expressed in domain terms.
- Query contracts for paging, sorting, filters, counts, and batch reads.

## Forbidden In Contracts

- `JdbcTemplate`, `DataSource`, `ResultSet`, `RowMapper`, SQL strings, `java.sql.*`, concrete DAO classes.
- Web/session/security/JSP types.
- Service use-case orchestration.

## Contract Design

- A DAO method should answer one persistence question.
- Prefer `PageRequest` plus filter/sort model types over `offset`, `limit`, and raw strings.
- For list pages, expose count and page queries where the UI needs pagination metadata.
- Add batch methods when an existing flow would query inside a loop.
- Keep writes owned by the DAO responsible for that table.

## Existing Examples To Inspect

- `ReservationDao`
- `RestaurantTableDao`
- `SocialDao`
- `ReviewDao`
- `RestaurantDao`

## Review Checklist

- Does the contract avoid leaking JDBC details?
- Can services implement the use case without N+1 calls?
- Is pagination/sorting/filtering expressible in SQL from the contract?
- Are write responsibilities unambiguous?
