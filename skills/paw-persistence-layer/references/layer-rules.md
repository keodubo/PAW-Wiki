# PAW Persistence Layer Rules

## Source Pages

Read current DAO/schema code plus:

- `PAW-Wiki/docs/wiki/persistencia-jdbc.md`
- `PAW-Wiki/docs/wiki/n-plus-1-joins-java.md`
- `PAW-Wiki/docs/wiki/comparacion-testing-servicios-y-daos.md`
- `PAW-Wiki/docs/wiki/modelo-capas.md`
- `PAW-Wiki/docs/wiki/configuracion-maven.md`
- `PAW-Wiki/docs/wiki/testing-unitario.md`

## Files To Check

- `persistence/src/main/java/ar/edu/itba/paw/persistence/PersistenceConfig.java`
- `persistence/src/main/java/ar/edu/itba/paw/persistence/LegacySchemaGuard.java`
- `persistence/src/main/resources/schema-base.sql`
- `persistence/src/main/resources/schema-postgres.sql`
- `persistence/src/test/resources/schema-hsqldb.sql`
- Existing DAO and DAO test for the nearest feature.

## Schema Rules

- Shared schema belongs in `persistence`, not `webapp`.
- Keep Postgres and HSQLDB scripts equivalent.
- Avoid data-loss changes without explicit user confirmation.
- Use `ON DELETE CASCADE` where the data relation owns cleanup and the wiki/feature plan supports it.
- Keep generated/derived local config out of schema changes.

## DAO Rules

- `java.sql` is allowed here and should not escape elsewhere.
- RowMappers are `private static final`.
- `SimpleJdbcInsert` is initialized once.
- Use explicit column lists, not `SELECT *`.
- Use `JOIN`, `WHERE IN`, `ORDER BY`, `LIMIT/OFFSET`, and aggregate/count SQL instead of Java-side filtering/sorting/joining.
- Escape LIKE input for `%` and `_`.
- Use one DAO as owner for writes to a table.

## DAO Test Rules

- Use HSQLDB with the existing test bootstrap.
- Use SQL fixtures/direct setup for preconditions.
- Use `JdbcTestUtils` or direct queries for postconditions.
- Do not use the DAO under test to prepare its own test state.
- Cover constraints, ordering, pagination, empty results, and update/delete effects.

## Existing Examples To Inspect

- `ReservationJdbcDao` and `ReservationJdbcDaoTest`
- `RestaurantTableJdbcDao` and tests
- `SocialJdbcDao` and tests
- `ReviewCascadeMigration` and runner pattern

## Common Verification Commands

```bash
mvn -pl persistence -am -Dtest=<DaoTestName> test
mvn -pl persistence -am test
mvn -pl webapp -am test
```
