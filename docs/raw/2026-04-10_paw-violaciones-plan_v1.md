# Plan: Corregir violaciones PAW — v3 Final

## Contexto

Auditoría del proyecto Forkd (PAW TPE1). Se corrigen errores que afectan Arquitectura (-4 pts) y Funcionalidad (-2 pts). Se skipea seguridad de usuarios y README.

## Estado actual de `day_of_week` — el bug central

Hay tres fuentes de datos con convenciones distintas:

| Fuente | Convención | Evidencia |
| --- | --- | --- |
| `schema.sql` primera def (línea 36) | `1..7` | `CHECK (day_of_week >= 1 AND day_of_week <= 7)` |
| `seed.sql` (línea 596+) | `1..7` | `VALUES` usan `1..7` (comments dicen `0-indexed` pero datos son `1-indexed`) |
| `RestaurantForm.java` (línea 66) | `0..6` | `for (int i = 0; i <= 6; i++) { dh.setDayOfWeek(i); }` |
| `EXTRACT(ISODOW)` en queries SQL | `1..7` | ISO estándar |
| `DayOfWeek.getValue()` en Java | `1..7` | ISO estándar |
| `restaurant.jsp` iteración (línea 125) | `1..7` | `<c:forEach begin="1" end="7">` |
| `restaurantForm.jsp` iteración (línea 357) | `0..6` | `<c:forEach begin="0" end="6">` |

### Consecuencias en producción

- En deploy fresco: el `CHECK constraint` impide insertar `day_of_week=0`, por lo tanto Lunes nunca se puede setear desde admin.
- El filtro "abierto ahora" (`ISODOW 1..7`) no matchea con datos `0..6` del form.
- `Restaurant.isOpenNow()` (`getValue() 1..7`) no matchea con datos `0..6` del form.
- `restaurant.jsp` itera `1..7` y compara `h.dayOfWeek == day` → no matchea datos `0..6`.

### Decisión

Estandarizar a `1..7`: coincide con el `CHECK` existente, seed data, Java `DayOfWeek`, PostgreSQL `ISODOW`, e ISO 8601.

---

## Fase 1: Normalizar `day_of_week` — Diagnóstico, Migración y Fix

### Paso 1.0: Diagnóstico pre-deploy

Antes de deployar código nuevo, correr esta query en producción para entender el estado de los datos:

```sql
SELECT
    MIN(day_of_week) AS min_dow,
    MAX(day_of_week) AS max_dow,
    COUNT(*) AS total,
    COUNT(*) FILTER (WHERE day_of_week = 0) AS zeros,
    COUNT(*) FILTER (WHERE day_of_week = 7) AS sevens,
    COUNT(*) FILTER (WHERE day_of_week < 1 OR day_of_week > 7) AS out_of_range
FROM restaurant_hours;
```

#### Interpretación

- `zeros = 0` y `max_dow = 7`: todos los datos son `1..7` (probablemente solo seed). Migración no toca datos.
- `zeros > 0`: hay datos `0-indexed` del admin form. Migración los normaliza a `1..7`.
- `sevens = 0` y `max_dow = 6`: todo es `0-indexed`. Migración shift `+1`.

### Paso 1.1: Migración Java para datos existentes

**Archivo nuevo:** `webapp/src/main/java/ar/edu/itba/paw/webapp/config/DayOfWeekMigration.java`

Sigue el patrón de `ReviewRatingSchemaMigration.java` (solo corre en PostgreSQL, idempotente):

```java
class DayOfWeekMigration {
    void migrateIfNeeded() {
        if (!shouldRun(dbUrl) || !hasTable("restaurant_hours")) return;

        // 1. Contar filas con day_of_week = 0 (definitivamente 0-indexed)
        int zeros = count("SELECT COUNT(*) FROM restaurant_hours WHERE day_of_week = 0");

        if (zeros > 0) {
            // Hay datos 0-indexed -> shift +1 SOLO a los que están en rango 0..6
            // (no tocar los que ya son 1..7)
            jdbcTemplate.update(
                "UPDATE restaurant_hours SET day_of_week = day_of_week + 1 WHERE day_of_week = 0"
            );
            // Los valores 1..6 podrían ser de cualquier convención.
            // NO los tocamos porque:
            //   - Si eran 0-indexed (1=Tue..6=Sun), están "desplazados"
            //     pero no podemos distinguirlos de 1-indexed
            //   - Si eran 1-indexed (1=Mon..6=Sat), ya están bien
            // SOLUCIÓN: el admin form re-escribe las horas completas en cada update
            //   -> la próxima vez que se edite un restaurante, los datos quedan correctos (1..7)
        }

        // 2. Asegurar CHECK constraint
        ensureCheckConstraint();

        // 3. Asegurar UNIQUE constraint
        ensureUniqueConstraint();
    }
}
```

#### Estrategia para datos ambiguos (`1..6`)

No se pueden distinguir entre convenciones. Sin embargo, el admin form hace `DELETE + INSERT` de todas las horas cada vez que se edita un restaurante (`RestaurantJdbcDao.update()` línea 470). Por lo tanto, la próxima vez que cualquier restaurante se edite, sus horas se reescriben correctamente en `1..7`. Los restaurantes que solo tienen seed data ya están en `1..7`.

**Archivo:** `webapp/src/main/java/ar/edu/itba/paw/webapp/config/WebConfig.java`

Agregar bean después de `reviewRatingSchemaMigration`:

```java
@Bean
public DayOfWeekMigration dayOfWeekMigration(final JdbcTemplate jdbcTemplate,
        final ReviewRatingSchemaMigration reviewRatingSchemaMigration) {
    final DayOfWeekMigration migration = new DayOfWeekMigration(
            jdbcTemplate, env.getProperty("db.url"));
    migration.migrateIfNeeded();
    return migration;
}
```

### Paso 1.2: Corregir `RestaurantForm` para emitir `1..7`

**Archivo:** `webapp/src/main/java/ar/edu/itba/paw/webapp/form/RestaurantForm.java`

Línea 68:

```java
dh.setDayOfWeek(i);
```

Pasa a:

```java
dh.setDayOfWeek(i + 1);
```

El array sigue `0-indexed` (7 elementos), pero el valor de `dayOfWeek` pasa a `1..7`. Las claves i18n no cambian porque:

- `restaurantForm.jsp:372` usa `weekday.short.${i}` con `i=0..6` → `weekday.short.0=Lun`
- `restaurant.jsp:129` usa `weekday.${day - 1}` con `day=1..7` → `weekday.0=Lunes`

### Paso 1.3: Corregir `fromRestaurant()` para matchear `1..7`

**Archivo:** `RestaurantForm.java:262-275`

El método `fromRestaurant()` compara `rh.getDayOfWeek() == dh.getDayOfWeek()`. Con el cambio del Paso 1.2:

- Form crea `DayHourForms` con `dayOfWeek 1..7` (array index `0..6`)
- DB ahora tiene `1..7`
- Comparación: `rh.getDayOfWeek() (1..7) == dh.getDayOfWeek() (1..7)` ✓

No requiere cambio adicional en `fromRestaurant()`, solo funciona porque ambos lados ahora usan `1..7`.

### Paso 1.4: Corregir comments en `seed.sql`

**Archivo:** `persistence/src/main/resources/seed.sql:596`

Cambiar:

```sql
-- 0=Monday, 1=Tuesday, ...6=Sunday
```

Por:

```sql
-- 1=Monday, 2=Tuesday, ...7=Sunday (ISO 8601)
```

Los `VALUES` ya usan `1..7`, solo los comments mienten.

### Paso 1.5: Deduplicar schema + agregar constraints

**Archivo:** `persistence/src/main/resources/schema.sql`

1. Eliminar primera definición (líneas 33-40): tiene `CHECK` pero le falta `UNIQUE` y usa `INTEGER`.
2. Eliminar segunda definición duplicada (líneas 53-61): tiene `UNIQUE` pero le falta `CHECK`.
3. Poner una sola definición con todo:

```sql
CREATE TABLE IF NOT EXISTS restaurant_hours (
    id SERIAL PRIMARY KEY,
    restaurant_id BIGINT NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
    day_of_week SMALLINT NOT NULL CHECK (day_of_week >= 1 AND day_of_week <= 7),
    shift SMALLINT NOT NULL DEFAULT 1,
    open_time TIME NOT NULL,
    close_time TIME NOT NULL,
    UNIQUE (restaurant_id, day_of_week, shift)
);
```

Los constraints para deployments existentes se agregan vía la migración Java (Paso 1.1).

### Paso 1.6: Actualizar schema de test

**Archivo:** `persistence/src/test/resources/schema-hsqldb.sql:37`

Agregar:

```sql
day_of_week SMALLINT NOT NULL CHECK (day_of_week >= 1 AND day_of_week <= 7),
```

### Paso 1.7: Corregir tests

**Archivo:** `persistence/src/test/java/.../RestaurantHoursJdbcDaoTest.java`

- Línea 53: comment `Mon(0)` → `Mon(1)`
- Línea 54: `for (int day = 0; day < 5; day++)` → `for (int day = 1; day <= 5; day++)`
- Línea 66: `assertEquals(i, ...)` → `assertEquals(i + 1, ...)`
- Líneas 80-87: `day_of_week = 0` → `day_of_week = 1` (ambas inserciones)

### Paso 1.8: Test de la migración

**Archivo nuevo:** `webapp/src/test/java/.../config/DayOfWeekMigrationTest.java`

- Insertar datos con `day_of_week=0` (caso `0-indexed`)
- Correr migración
- Verificar que `0` se convirtió en `1`
- Verificar que valores `1..7` no se modificaron

### Plan de deployment para producción

#### Antes del deploy

1. Correr query diagnóstica (Paso 1.0) en la DB de producción.
2. Anotar resultado: ¿hay `zeros`? ¿hay `sevens`? ¿cuántas filas?
3. Hacer backup de la tabla:

```sql
CREATE TABLE restaurant_hours_backup AS SELECT * FROM restaurant_hours;
```

#### Deploy

4. Deploy del nuevo WAR.
5. La app arranca → `DataSourceInitializer` ejecuta `schema.sql` → `DayOfWeekMigration.migrateIfNeeded()` corre automáticamente.
6. La migración es idempotente: si se re-deploya, no rompe nada.

#### Post-deploy

7. Verificar datos:

```sql
SELECT day_of_week, COUNT(*)
FROM restaurant_hours
GROUP BY day_of_week
ORDER BY 1;
```

Debe mostrar solo valores `1..7`.

8. Verificar en la UI:
   - Admin: crear restaurante con horario Lunes → debe funcionar
   - Admin: editar restaurante existente → horarios se muestran correctos
   - Detalle: horarios se muestran en los días correctos
   - Filtro "abierto ahora": funciona
9. Si todo OK, borrar backup:

```sql
DROP TABLE restaurant_hours_backup;
```

#### Rollback

10. Restaurar datos:

```sql
DELETE FROM restaurant_hours;
INSERT INTO restaurant_hours SELECT * FROM restaurant_hours_backup;
```

11. Re-deploy versión anterior.

---

## Fase 2: `ON DELETE CASCADE` para reviews

### Paso 2.1: Migración Java

Agregar a `DayOfWeekMigration` o crear `ReviewCascadeMigration` separada:

```java
// Verificar si FK tiene CASCADE via pg_constraint
// Si no tiene:
jdbcTemplate.execute("ALTER TABLE reviews DROP CONSTRAINT IF EXISTS reviews_restaurant_id_fkey");
jdbcTemplate.execute("ALTER TABLE reviews ADD CONSTRAINT reviews_restaurant_id_fkey " +
    "FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE");
```

### Paso 2.2: Actualizar `schema-hsqldb.sql`

**Archivo:** `persistence/src/test/resources/schema-hsqldb.sql:25`

Agregar `ON DELETE CASCADE`:

```sql
restaurant_id BIGINT NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
```

### Paso 2.3: Eliminar borrado manual

**Archivo:** `services/.../RestaurantServiceImpl.java:212`

Eliminar:

```java
reviewDao.deleteByRestaurantId(id);
```

El método queda:

```java
@Transactional
@Override
public void delete(final long id) {
    restaurantDao.delete(id);
}
```

### Paso 2.4: Test

Actualizar `RestaurantServiceImplTest` para verificar que `delete()` solo llama `restaurantDao.delete()`.

---

## Fase 3: N+1 Queries — listados y lectura individual de restaurantes

### Paso 3.1: Agregar métodos hidratados al `RestaurantDao`

**Archivo:** `persistence-contracts/.../RestaurantDao.java`

Agregar cuatro métodos nuevos:

```java
List<Restaurant> findPopularWithHours(int limit);
List<Restaurant> searchWithHours(RestaurantSearchArguments searchArguments);
Optional<Restaurant> findByIdWithHours(long id);
Optional<Restaurant> findBySlugWithHours(String slug);
```

La idea es que el contrato exponga lecturas ya hidratadas con horarios para los casos donde el caller realmente los necesita.

### Paso 3.2: Hidratar horarios en `RestaurantJdbcDao`

**Archivo:** `persistence/.../RestaurantJdbcDao.java`

Implementación final:

- Reusar `findPopular()`, `search(...)`, `findById()` y `findBySlug()` para obtener la lista base de restaurantes.
- Agregar un `RowMapper<RestaurantHour>` privado para `restaurant_hours`.
- Agregar helper privado `hydrateRestaurantsWithHours(List<Restaurant>)`.
- Agregar helper privado `hydrateRestaurantWithHours(Optional<Restaurant>)`.
- Resolver los horarios con una sola query batch:

```sql
SELECT id, restaurant_id, day_of_week, shift, open_time, close_time
FROM restaurant_hours
WHERE restaurant_id IN (?, ?, ...)
ORDER BY restaurant_id, day_of_week, shift
```

- Agrupar por `restaurant_id` dentro de persistence y reconstruir allí los `Restaurant` con `hours`.

Esto evita el N+1 y mantiene la instanciación de entidades en la capa de persistencia, no en services.

### Paso 3.3: Simplificar `RestaurantServiceImpl`

**Archivo:** `services/.../RestaurantServiceImpl.java`

Cambio final:

- `findPopular()` delega a `restaurantDao.findPopularWithHours(limit)`
- `searchPage()` delega a `restaurantDao.searchWithHours(...)`
- `findById()` delega a `restaurantDao.findByIdWithHours(id)`
- `findBySlug()` delega a `restaurantDao.findBySlugWithHours(slug)`
- Se elimina `loadHours()` del service
- `findHoursByRestaurantId()` se mantiene como passthrough a `RestaurantHoursDao` para callers que solo necesitan horarios

### Paso 3.4: Tests

#### Persistence — `RestaurantJdbcDaoTest`

Agregar cobertura para:

- `testFindPopularWithHoursReturnsHydratedRestaurantsInRatingOrder()`
- `testSearchWithHoursReturnsPagedResultsWithHydratedHours()`
- `testSearchWithHoursReturnsEmptyWhenNothingMatches()`
- `testFindByIdWithHours_whenExists_returnsHydratedRestaurant()`
- `testFindBySlugWithHours_whenExists_returnsHydratedRestaurant()`

Validar estado final:

- orden por rating en resultados
- horarios cargados correctamente
- restaurante sin horarios
- lista vacía

#### Services — `RestaurantServiceImplTest`

Agregar/ajustar tests para:

- `findPopular()` usando `findPopularWithHours()`
- `searchPage()` preservando `currentPage`, `pageSize` y `totalResults`
- `findById()` usando `findByIdWithHours()`
- `findBySlug()` usando `findBySlugWithHours()`

Sin `verify()` ni conteo de llamadas: solo asserts sobre el resultado final.

---

## Fase 4: XSS — escape en `restaurant.jsp` + test

### Paso 4.1: Escapar `title attributes`

**Archivo:** `webapp/.../views/restaurant.jsp`

- Línea 77: `title="${comingSoon}"` → `title="${fn:escapeXml(comingSoon)}"`
- Línea 80: `title="${comingSoon}"` → `title="${fn:escapeXml(comingSoon)}"`

### Paso 4.2: Agregar assertion al test

**Archivo:** `webapp/src/test/.../ViewTemplateSecurityTest.java`

En `restaurantTemplate_escapesUserSuppliedAttributeValues()`, agregar:

```java
() -> assertTrue(restaurant.contains("title=\"${fn:escapeXml(comingSoon)}\"")),
```

---

## Fase 5: i18n — mensajes hardcodeados

**Archivo:** `webapp/.../form/RestaurantSearchForm.java`

- Línea 10: `message = "{Size.restaurantSearchForm.query}"`
- Línea 15: `message = "{Size.restaurantSearchForm.neighborhood}"`

**Archivos:** `messages_es.properties` y `messages_en.properties`

Agregar claves.

> Nota: este form no se usa con `@Valid` en ningún controller actualmente.

---

## Verificación end-to-end

```bash
mvn clean test
mvn clean package
mvn -pl webapp -am jetty:run
```

### Verificación manual

- Admin: crear restaurante con Lunes activo → funciona (antes fallaba `CHECK`)
- Admin: editar restaurante existente → horarios se mapean correctamente
- Detalle restaurante: grilla de horarios muestra días correctos
- Filtro "abierto ahora": funciona (`ISODOW` matchea con datos `1..7`)
- Home: populares cargan con horas (batch, 2 queries en vez de N+1)
- Explorar: búsqueda con horas (batch)
- Eliminar restaurante: reviews se borran por `CASCADE`
- Tooltips: `title attributes` escapados

---

## Resumen de archivos por fase

| Fase | Archivo | Operación |
| --- | --- | --- |
| 1.1 | `webapp/.../config/DayOfWeekMigration.java` | NUEVO |
| 1.1 | `webapp/.../config/WebConfig.java` | Agregar bean |
| 1.2 | `webapp/.../form/RestaurantForm.java` | `dayOfWeek i -> i + 1` |
| 1.4 | `persistence/.../seed.sql` | Fix comments |
| 1.5 | `persistence/.../schema.sql` | Dedup + `CHECK` + `UNIQUE` |
| 1.6 | `persistence/test/.../schema-hsqldb.sql` | `CHECK constraint` |
| 1.7 | `persistence/test/.../RestaurantHoursJdbcDaoTest.java` | `0..6 -> 1..7` |
| 1.8 | `webapp/test/.../DayOfWeekMigrationTest.java` | NUEVO |
| 2.1 | `webapp/.../config/DayOfWeekMigration.java` (o separada) | `CASCADE migration` |
| 2.2 | `persistence/test/.../schema-hsqldb.sql` | `CASCADE` |
| 2.3 | `services/.../RestaurantServiceImpl.java` | Borrar `deleteByRestaurantId` |
| 2.4 | `services/test/.../RestaurantServiceImplTest.java` | Verificar `delete` |
| 3.1 | `persistence-contracts/.../RestaurantDao.java` | Agregar métodos `*WithHours` |
| 3.2 | `persistence/.../RestaurantJdbcDao.java` | Hidratar horarios con batch `WHERE IN` |
| 3.3 | `services/.../RestaurantServiceImpl.java` | Delegar lecturas hidratadas al DAO |
| 3.4 | `persistence/test/.../RestaurantJdbcDaoTest.java` | Tests de hydration batch e individual |
| 3.4 | `services/test/.../RestaurantServiceImplTest.java` | Tests de resultados hidratados y paginación |
| 4.1 | `webapp/.../views/restaurant.jsp` | `fn:escapeXml` |
| 4.2 | `webapp/test/.../ViewTemplateSecurityTest.java` | Assert escape |
| 5 | `webapp/.../form/RestaurantSearchForm.java` + properties | i18n keys |

---

## Next actions

- [ ] Revisar el archivo guardado y ajustar el nombre si querés que refleje otro topic.
- [ ] Si querés, lo convierto en una versión más ejecutiva para entregar o compartir.
- [ ] Si querés, también puedo transformar este plan en un checklist de implementación por commits.
