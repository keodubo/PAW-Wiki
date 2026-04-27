---
titulo: Persistencia JDBC — Patrones y Errores
tipo: concepto
fuentes: [raw/correcciones_tp1.md, raw/apuntes.txt, raw/notas.txt]
creado: 2026-04-09
actualizado: 2026-04-27
---

# Persistencia JDBC — Patrones y Errores

La capa de persistencia opera via JDBC puro. Los detalles de `java.sql` deben quedar atrapados permanentemente en esta capa.

## Reglas de aislamiento

- `java.sql` solo en la capa de persistencia — **nunca** en models, services o webapp
- `java.sql.Timestamp` y `java.sql.Date` no deben aparecer en modelos — usar `LocalDate`/`LocalDateTime`
- El esquema compartido pertenece a la capa de persistence, no a webapp
- Cada tabla debe ser modificada por un solo DAO

## RowMappers

- Deben ser `private static final` — instanciarlos en cada ejecucion agota heap
- Definirlos como constantes estaticas al nivel de la clase DAO
- Reusar rowmappers entre metodos del mismo DAO

## JdbcInsert y JdbcTemplate

- Usar `SimpleJdbcInsert` para inserts (inyectable, thread-safe)
- Inicializar `SimpleJdbcInsert` **una sola vez** (en constructor o init), no en cada llamada a metodo — causa problemas de concurrencia
- `jdbcTemplate.update` es aceptable pero `SimpleJdbcInsert` es preferido
- Usar `ResultSetExtractor` para mappings complejos (ej: rankings) en vez de `RowCallbackHandler` con closure

## Cascadas y relaciones

- Usar `ON DELETE CASCADE` en la BD, no eliminar relaciones a mano en Java
- No guardar composicion forzada en modelos — evitar traer todas las entidades relacionadas en cada RowMapper

## StringBuilder

- No usar `StringBuilder` para queries estaticas (solo appends constantes) — usar `String.concat` o `String.format`

## Paginacion

- La capa de persistence deberia recibir una abstraccion tipo `Page` en vez de offset/limit como parametros sueltos
- Siempre paginar — no retornar colecciones sin limite

## Fundamentos de los apuntes

- **JdbcTemplate**: abstraccion de Spring que reduce boilerplate de JDBC puro
- **RowMapper**: interfaz funcional (un unico metodo) que convierte un ResultSet en una posicion a un objeto. Es lo que "sabe interpretar" los resultados de la query
- **SimpleJdbcInsert**: configurar con DataSource, inicializar UNA vez (en constructor), usar `executeAndReturnKey()` para obtener ID generado
- **Driver**: PostgreSQL driver en pom padre, dependency en pom de persistence
- **DataSource**: configurar en un properties file externo, bean en configuracion de Spring

## Schema compartido y dialectos

- Evolucion editorial:
  - En ejemplos iniciales del curso aparece un `schema.sql` unico.
  - El estado final que conviene documentar en este repo separa `schema-base.sql` del dialecto (`schema-postgres.sql` y `schema-hsqldb.sql`).
  - La regla estable es que app y tests carguen el mismo esquema final desde el modulo `persistence`.
- Separar siempre el esquema base (`schema-base.sql`) del esquema que usa comandos especificos del motor (`schema-postgres.sql` y `schema-hsqldb.sql` para testing).
- Estos deben vivir en los `resources` del modulo de **persistence**.
- En `WebConfig` y `TestConfig` levantar juntos los scripts para asegurar compatibilidad.

## Ver tambien
- [[comparacion-testing-servicios-y-daos]]
- [[comparacion-capas-web-services-persistence]]
- [[configuracion-maven]]
- [[criterios-evaluacion]]
- [[hibernate-jpa]]
- [[java-style]]
- [[manejo-imagenes]]
- [[n-plus-1-joins-java]]
- [[transactional]]
- [[xss-prevencion]]
- [[modelo-capas]]
- [[testing-unitario]]
- [[resumen-correcciones]]
- [[resumen-clases-paw-2026]]
- [[resumen-notas-sprint-1]]
- [[resumen-apuntes]]
- [[resumen-notas]]
- [[remediacion-violaciones-paw]]

- [[plan-implementacion-reservas]]
- [[resumen-spec-reservas]]
