---
titulo: Plan de Implementación del Sistema de Reservas
tipo: sintesis
fuentes: [raw/reservas/spec-funcional-reservas.md, raw/diagrama_bd.md]
creado: 2026-04-15
actualizado: 2026-04-15
---

# Plan de Implementación del Sistema de Reservas

> **Para agentes**: este plan usa convenciones de `superpowers:writing-plans`. Ejecutar con `superpowers:executing-plans` o `superpowers:subagent-driven-development` una fase por vez.

**Goal**: implementar el sistema de reservas definido en [[resumen-spec-reservas]] sobre el repo actual, sin romper el flujo existente de restaurantes ni reviews.

**Arquitectura**: capa de persistencia JDBC (joins y filtros en BD), capa de servicios `@Transactional` con lógica de disponibilidad y asignación de mesa, capa web con controllers finos validando con JSR380, vistas JSP con `<c:out>`, i18n y Spring Security. Dos jobs `@Scheduled` para transiciones temporales y emails de recordatorio.

**Tech stack**: Java 21, Spring Web MVC (sin Boot), JDBC, PostgreSQL, HSQLDB en memoria para tests, JSP/JSTL, Spring Security, Bean Validation JSR380, SLF4J/Logback, Thymeleaf para emails, JUnit + Mockito + `@Rollback`.

---

## Decisiones lockeadas (confirmadas con el usuario)

| Tema | Decisión |
|------|----------|
| `accepts_reservations = null` | restaurante legacy; UI muestra "no definido"; no forzamos migración |
| Pending cuyo slot venció | **se borra la reserva** (DELETE físico) — no hay estado `expired`, no hay transición a `cancelled` |
| Scheduler de borrado de pendings vencidos | corre cada **5 minutos**; idempotente |
| Slot duration | enum `SLOT_60` / `SLOT_90` / `SLOT_120` (minutos), se usa también para armar slots |
| Generación de slots | arrancan en `open_time`, se concatenan; si el último excede `close_time`, **se acorta** |
| Zona horaria | **GMT-3** (Argentina, sin DST activo); todo scheduler usa `ZoneId.of("America/Argentina/Buenos_Aires")` |
| Teléfono de contacto alternativo | **misma validación que `restaurantForm.phone`**: `@NotEmpty @Size(8-50) @Pattern("^(?=.*[0-9])[0-9+ -]+$")` |
| Contador `no_show` | **materializado** en columna `users.no_show_count`; se incrementa al marcar, decrementa al desmarcar; **no se muestra en UI** (uso futuro anti-fraude) |
| Obligatoriedad de `reservation_disabled_mode` cuando `accepts_reservations=false` | **obligatorio**; si no se elige opción, el save falla con error in-line |
| Transición `confirmed → completed` | por scheduler cada 5 min |
| Reviews legacy sin `reservation_id` | **preservar como read-only**; `reservation_id` nullable con UNIQUE (múltiples NULLs permitidos en Postgres y HSQLDB); las nuevas reviews exigen `reservation_id NOT NULL` validado en servicio; la UI no expone edición ni borrado sobre reviews legacy |

---

## Mapa de archivos afectados

### `models/`

- **Crear** `models/src/main/java/ar/edu/itba/paw/models/Reservation.java`
- **Crear** `models/src/main/java/ar/edu/itba/paw/models/ReservationStatus.java` (enum)
- **Crear** `models/src/main/java/ar/edu/itba/paw/models/ReservationConfirmationMode.java` (enum)
- **Crear** `models/src/main/java/ar/edu/itba/paw/models/ReservationDisabledMode.java` (enum: `NONE_MESSAGE`, `WHATSAPP`, `PHONE_CALL`, `EXTERNAL_URL`)
- **Crear** `models/src/main/java/ar/edu/itba/paw/models/ReservationSlotDuration.java` (enum `SLOT_60=60`, `SLOT_90=90`, `SLOT_120=120`, con `int toMinutes()`)
- **Crear** `models/src/main/java/ar/edu/itba/paw/models/RestaurantTable.java`
- **Crear** `models/src/main/java/ar/edu/itba/paw/models/TimeSlot.java` (value object con `slotStart`, `slotEnd`)
- **Modificar** `models/src/main/java/ar/edu/itba/paw/models/Restaurant.java` (nuevos campos)
- **Modificar** `models/src/main/java/ar/edu/itba/paw/models/User.java` (campo `noShowCount`)
- **Modificar** `models/src/main/java/ar/edu/itba/paw/models/Review.java` (campo `reservationId`)

### `persistence-contracts/`

- **Crear** `persistence-contracts/src/main/java/ar/edu/itba/paw/persistence/ReservationDao.java`
- **Crear** `persistence-contracts/src/main/java/ar/edu/itba/paw/persistence/RestaurantTableDao.java`
- **Modificar** `persistence-contracts/src/main/java/ar/edu/itba/paw/persistence/RestaurantDao.java` (CRUD extendido con campos de reservas)
- **Modificar** `persistence-contracts/src/main/java/ar/edu/itba/paw/persistence/UserDao.java` (increment/decrement `no_show_count`)

### `persistence/`

- **Crear** `persistence/src/main/java/ar/edu/itba/paw/persistence/ReservationJdbcDao.java`
- **Crear** `persistence/src/main/java/ar/edu/itba/paw/persistence/RestaurantTableJdbcDao.java`
- **Modificar** `persistence/src/main/java/ar/edu/itba/paw/persistence/RestaurantJdbcDao.java`
- **Modificar** `persistence/src/main/java/ar/edu/itba/paw/persistence/UserJdbcDao.java`
- **Modificar** `persistence/src/main/resources/schema-base.sql`
- **Modificar** `persistence/src/main/resources/schema-postgres.sql`
- **Modificar** `persistence/src/test/resources/schema-hsqldb.sql`

### `service-contracts/`

- **Crear** `service-contracts/src/main/java/ar/edu/itba/paw/services/ReservationService.java`
- **Crear** `service-contracts/src/main/java/ar/edu/itba/paw/services/CreateReservationData.java`
- **Crear** `service-contracts/src/main/java/ar/edu/itba/paw/services/SlotUnavailableException.java`
- **Crear** `service-contracts/src/main/java/ar/edu/itba/paw/services/LayoutFrozenException.java`
- **Crear** `service-contracts/src/main/java/ar/edu/itba/paw/services/ReservationNotFoundException.java`
- **Modificar** `service-contracts/src/main/java/ar/edu/itba/paw/services/RestaurantService.java` (config de reservas + layout)
- **Modificar** `service-contracts/src/main/java/ar/edu/itba/paw/services/SaveRestaurantData.java` (nuevos campos)
- **Modificar** `service-contracts/src/main/java/ar/edu/itba/paw/services/ReviewService.java` (exigir reservation_id en creación)

### `services/`

- **Crear** `services/src/main/java/ar/edu/itba/paw/services/ReservationServiceImpl.java`
- **Crear** `services/src/main/java/ar/edu/itba/paw/services/ReservationSchedulerImpl.java` (`@Scheduled`)
- **Modificar** `services/src/main/java/ar/edu/itba/paw/services/RestaurantServiceImpl.java`
- **Modificar** `services/src/main/java/ar/edu/itba/paw/services/ReviewServiceImpl.java`
- **Modificar** `services/src/main/java/ar/edu/itba/paw/services/ServicesConfig.java` (`@EnableScheduling`, `@EnableAsync`)

### `webapp/` — controllers, forms, views

- **Crear** `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/ReservationController.java` (crear + cancelar user)
- **Crear** `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/OwnerReservationController.java` (admin de restaurante)
- **Crear** `webapp/src/main/java/ar/edu/itba/paw/webapp/form/CreateReservationForm.java`
- **Crear** `webapp/src/main/java/ar/edu/itba/paw/webapp/form/ReservationConfigForm.java` (subform de reservas dentro de RestaurantForm)
- **Crear** `webapp/src/main/java/ar/edu/itba/paw/webapp/form/RestaurantTableGroupForm.java` (tipo + cantidad)
- **Crear** `webapp/src/main/java/ar/edu/itba/paw/webapp/validation/ReservationConfigValidator.java` (validador condicional cross-field)
- **Modificar** `webapp/src/main/java/ar/edu/itba/paw/webapp/form/RestaurantForm.java` (composición con ReservationConfigForm)
- **Modificar** `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/OwnerDashboardController.java` (crear/editar con reservas)
- **Modificar** `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/RestaurantController.java` (render de form de reserva en la página pública)
- **Modificar** `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/ProfileController.java` (página `/perfil/{slug}/reservas`)
- **Modificar** `webapp/src/main/java/ar/edu/itba/paw/webapp/exceptions/` + `ErrorHandlingAdvice.java` (exceptions nuevas)
- **Modificar** `webapp/src/main/java/ar/edu/itba/paw/webapp/config/WebAuthConfig.java` (rutas protegidas)
- **Crear** `webapp/src/main/webapp/WEB-INF/views/owner/reservations.jsp`
- **Crear** `webapp/src/main/webapp/WEB-INF/views/profile/reservations.jsp`
- **Modificar** `webapp/src/main/webapp/WEB-INF/views/owner/restaurant-form.jsp` (subform de reservas + layout)
- **Modificar** `webapp/src/main/webapp/WEB-INF/views/restaurant.jsp` (form de reserva para usuario)
- **Modificar** `webapp/src/main/resources/i18n/messages.properties` y `messages_es.properties`

### `webapp/` — mailing

- **Crear** `webapp/src/main/resources/mail-templates/reservation-confirmed.html` (+ `reservation-cancelled.html`, `reservation-reminder.html`, `reservation-completed.html`)
- **Modificar** `services/.../SmtpMailServiceImpl.java` para soportar los 4 templates nuevos (o agregar métodos helper)

### Tests

- **Crear** `persistence/src/test/java/.../ReservationJdbcDaoTest.java`
- **Crear** `persistence/src/test/java/.../RestaurantTableJdbcDaoTest.java`
- **Crear** `services/src/test/java/.../ReservationServiceImplTest.java`
- **Crear** `services/src/test/java/.../ReservationSchedulerImplTest.java`
- **Crear** `webapp/src/test/java/.../ReservationControllerMvcTest.java`
- **Crear** `webapp/src/test/java/.../OwnerReservationControllerMvcTest.java`
- **Modificar** `webapp/src/test/java/.../SecurityMvcTest.java` (rutas nuevas)
- **Modificar** tests existentes de `RestaurantForm` cuando se agreguen campos

---

## Fase 1 — Modelo de datos y migraciones

**Objetivo**: esquema listo en Postgres e HSQLDB, DAOs mínimos compilando, sin tocar flujo usuario.

**Archivos**:
- Modificar `persistence/src/main/resources/schema-base.sql`
- Modificar `persistence/src/main/resources/schema-postgres.sql`
- Modificar `persistence/src/test/resources/schema-hsqldb.sql`
- Crear `models/.../Reservation.java`, `ReservationStatus.java`, `ReservationConfirmationMode.java`, `ReservationDisabledMode.java`, `ReservationSlotDuration.java`, `RestaurantTable.java`, `TimeSlot.java`
- Modificar `models/.../Restaurant.java`, `User.java`, `Review.java`
- Crear `persistence-contracts/.../ReservationDao.java`, `RestaurantTableDao.java`
- Crear `persistence/.../ReservationJdbcDao.java`, `RestaurantTableJdbcDao.java`
- Crear `persistence/src/test/java/.../ReservationJdbcDaoTest.java`, `RestaurantTableJdbcDaoTest.java`

**Tasks**:

- [ ] **1.1 SQL**: agregar columnas a `restaurants`:
  - `accepts_reservations BOOLEAN NULL`
  - `reservation_disabled_mode VARCHAR(32) NULL`
  - `reservation_external_link VARCHAR(512) NULL`
  - `reservation_contact_phone VARCHAR(50) NULL`
  - `reservation_confirmation_mode VARCHAR(16) NULL`
  - `reservation_slot_duration_minutes SMALLINT NULL`
- [ ] **1.2 SQL**: agregar columna a `users`:
  - `no_show_count INTEGER NOT NULL DEFAULT 0`
- [ ] **1.3 SQL**: crear tabla `restaurant_tables`:
  - `id BIGSERIAL PK`
  - `restaurant_id BIGINT NOT NULL FK`
  - `table_number INTEGER NOT NULL`
  - `capacity SMALLINT NOT NULL`
  - `created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP`
  - UNIQUE `(restaurant_id, table_number)`
  - CHECK `capacity BETWEEN 1 AND 12` (12 representa "12+")
- [ ] **1.4 SQL**: crear tabla `reservations`:
  - `id BIGSERIAL PK`
  - `restaurant_id BIGINT NOT NULL FK`
  - `user_id BIGINT NOT NULL FK`
  - `table_id BIGINT NOT NULL FK`
  - `reservation_date DATE NOT NULL`
  - `slot_start TIME NOT NULL`
  - `slot_end TIME NOT NULL`
  - `party_size SMALLINT NOT NULL`
  - `comment TEXT NULL`
  - `status VARCHAR(16) NOT NULL`
  - `no_show BOOLEAN NOT NULL DEFAULT FALSE`
  - `created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP`
  - `updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP`
  - índices: `(restaurant_id, reservation_date, status)` y `(table_id, reservation_date, slot_start)`
- [ ] **1.5 SQL**: crear tabla `reservation_notifications`:
  - `id BIGSERIAL PK`
  - `reservation_id BIGINT NOT NULL FK ON DELETE CASCADE`
  - `notification_type VARCHAR(32) NOT NULL`
  - `sent_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP`
  - UNIQUE `(reservation_id, notification_type)` para idempotencia
- [ ] **1.6 SQL**: agregar columna a `reviews`:
  - `reservation_id BIGINT NULL` (nullable en esta fase; UNIQUE se decide en Fase 9)
- [ ] **1.7**: ajustar HSQLDB mirror (`schema-hsqldb.sql`) con sintaxis equivalente
- [ ] **1.8**: crear enums y POJOs del modelo (`Reservation`, `RestaurantTable`, `TimeSlot`, 4 enums)
- [ ] **1.9**: extender `Restaurant.java`, `User.java`, `Review.java` con los nuevos campos + getters/setters (respetando estilo existente)
- [ ] **1.10 TDD**: `RestaurantTableJdbcDaoTest`
  - test `create_generatesTablesWithUniqueTableNumberPerRestaurant`
  - test `findByRestaurantId_returnsAllTablesOrderedByCapacityAsc`
  - test `deleteByRestaurantId_removesAllTables`
- [ ] **1.11**: implementar `RestaurantTableJdbcDao` hasta que pasen los tests
- [ ] **1.12 TDD**: `ReservationJdbcDaoTest`
  - test `create_persistsAndAssignsId`
  - test `findByUserId_returnsOrderedByDateDesc`
  - test `findByRestaurantId_returnsOrderedByDateAsc`
  - test `findOverlappingForTable_excludesCancelledAndCompleted`
  - test `deleteExpiredPending_removesOnlyPendingWithSlotEndBeforeNow`
- [ ] **1.13**: implementar `ReservationJdbcDao` hasta que pasen los tests
- [ ] **1.14**: extender `UserJdbcDao` con `incrementNoShowCount(userId)` / `decrementNoShowCount(userId)` + tests
- [ ] **1.15**: correr `mvn clean test` completo; todo verde
- [ ] **1.16 Commit**: `feat(reservations): schema and DAOs for reservations and tables`

**Validación pre-fase**: revisar si `restaurant_hours` admite turnos que crucen medianoche (`close < open`). Si sí, documentar ese edge case en Fase 3 y testearlo. Si no, notar que los slots siempre quedan dentro del día.

**Criterio de cierre**: suite entera verde; migración corre limpia en Postgres local (`dropdb && createdb && jetty:run`); tests de DAO cubren inserción, lectura y eliminación de pendings vencidos.

---

## Fase 2 — Configuración de reservas en crear/editar restaurante

**Objetivo**: `RestaurantForm` admite los tres casos (null/false/true) con validaciones condicionales y persiste el layout.

**Archivos**:
- Modificar `webapp/.../form/RestaurantForm.java`
- Crear `webapp/.../form/ReservationConfigForm.java`
- Crear `webapp/.../form/RestaurantTableGroupForm.java`
- Crear `webapp/.../validation/ReservationConfigValidator.java` (cross-field, invocado desde controller o via `@Valid` clase-level)
- Modificar `webapp/.../controller/OwnerDashboardController.java`
- Modificar `webapp/src/main/webapp/WEB-INF/views/owner/restaurant-form.jsp`
- Modificar `service-contracts/.../SaveRestaurantData.java`
- Modificar `services/.../RestaurantServiceImpl.java`
- Modificar `webapp/src/main/resources/i18n/messages.properties` y `messages_es.properties`

**Tasks**:

- [ ] **2.1 TDD**: test `ReservationConfigFormValidationTest` cubriendo:
  - null → válido sin subforms
  - false sin `reservation_disabled_mode` → inválido (error in-line)
  - false + `EXTERNAL_URL` sin link → inválido
  - false + `EXTERNAL_URL` con link XSS-friendly (ej `javascript:alert(1)`) → inválido
  - false + `WHATSAPP` sin teléfono → inválido
  - false + `WHATSAPP` con teléfono válido → válido
  - false + `PHONE_CALL` con teléfono válido → válido
  - false + `NONE_MESSAGE` → válido sin requerir link/teléfono
  - true sin `reservation_confirmation_mode` → inválido
  - true sin `reservation_slot_duration_minutes` ∈ {60,90,120} → inválido
  - true + 0 grupos de mesas → inválido (debe haber al menos un tipo)
  - true + grupo con cantidad 0 o 31 → inválido
- [ ] **2.2**: crear `ReservationConfigForm`, `RestaurantTableGroupForm` y `ReservationConfigValidator`; reutilizar anotaciones de `RestaurantForm.phone` para el teléfono (`@NotEmpty @Size(8,50) @Pattern("^(?=.*[0-9])[0-9+ -]+$")`). Validar URL con `@org.hibernate.validator.constraints.URL` y con regex que excluya `javascript:` / `data:` schemes
- [ ] **2.3**: componer `RestaurantForm` con `@Valid ReservationConfigForm reservationConfig`
- [ ] **2.4**: extender `SaveRestaurantData` + `RestaurantServiceImpl.createRestaurant/updateRestaurant` para persistir la config y generar las mesas del layout (`restaurant_tables` se reemplaza en update si no hay reservas bloqueantes)
- [ ] **2.5 TDD**: test `RestaurantServiceImplTest.updateLayout_failsWhenPendingOrConfirmedExist` (lanza `LayoutFrozenException`)
- [ ] **2.6**: chequear en `RestaurantServiceImpl.updateRestaurant` si hay reservas `pending|confirmed` activas para el restaurante; si sí y el layout cambia, lanzar `LayoutFrozenException`
- [ ] **2.7 MVC test**: `OwnerDashboardControllerMvcTest.createRestaurantWithReservationsEnabled_persistsConfigAndLayout`
- [ ] **2.8 MVC test**: `OwnerDashboardControllerMvcTest.editRestaurantWithActiveReservations_returnsErrorWhenChangingLayout`
- [ ] **2.9**: editar `owner/restaurant-form.jsp`:
  - radio group null/false/true
  - subform false (visible si false): select con 4 opciones de `reservation_disabled_mode`, inputs condicionales para link/teléfono, todo con `<c:out>`
  - subform true (visible si true): radio `AUTOMATIC`/`MANUAL`, select slot_duration, UI de layout (agregar/quitar grupos tipo+cantidad)
  - JavaScript mínimo para mostrar/ocultar subforms según valor elegido (progressive enhancement; el server valida igual)
- [ ] **2.10**: i18n keys nuevas en ambos `messages*.properties` (labels, errores, ayudas)
- [ ] **2.11**: correr `mvn -pl webapp -am jetty:run` y crear/editar un restaurante en cada modalidad manualmente
- [ ] **2.12 Commit**: `feat(reservations): restaurant form with reservation config and table layout`

**Criterio de cierre**: form respeta las 3 ramas; tests MVC verdes; editar layout falla si hay reservas activas; todos los textos en ES/EN.

---

## Fase 3 — Disponibilidad y asignación de mesa

**Objetivo**: servicio puro que calcule slots disponibles y asigne la mesa libre más chica compatible. Queries resueltos en BD.

**Archivos**:
- Crear `service-contracts/.../SlotUnavailableException.java`
- Crear `services/.../SlotCatalog.java` (helper estático o `@Component` sin estado; deriva slots a partir de `RestaurantHour[]` + `ReservationSlotDuration`)
- Crear sección nueva en `ReservationServiceImpl` con `findAvailableTable(...)` y `listAvailableSlots(...)`
- Crear tests en `services/src/test/java/.../SlotCatalogTest.java` y `ReservationServiceImplTest.java`

**Regla de slot catalog (locked)**:

- Arranca en `open_time`.
- Concatenado: `[open, open+d), [open+d, open+2d), ...`
- Si `open + k*d > close`, el último slot se acorta a `[open+(k-1)*d, close)` y esa duración parcial es válida solo si `close - (open+(k-1)*d) >= 60 min` (para no generar slots de 5 min; si el remanente es menor a 60, se descarta).
  - Nota: esta regla del mínimo de 60 min para el slot parcial no está en el spec; si el usuario prefiere otra cosa, ajustar antes de implementar.

**Tasks**:

- [ ] **3.1 TDD**: `SlotCatalogTest`
  - `generateSlots_startsAtOpenTime`
  - `generateSlots_90MinDurationChainsCorrectly` (19:00–23:30 con 90 → 19:00, 20:30, 22:00; el último dura 90 exactos)
  - `generateSlots_lastSlotTruncatedWhenExceedsClose` (19:00–23:00 con 90 → 19:00–20:30, 20:30–22:00, 22:00–23:00)
  - `generateSlots_dropsPartialSlotShorterThan60Min` (o ajustar según decisión final)
  - `generateSlots_respectsShiftBoundaries` (un restaurante con dos turnos, ej mediodía y noche)
- [ ] **3.2**: implementar `SlotCatalog.generate(RestaurantHour, ReservationSlotDuration)` devolviendo `List<TimeSlot>`
- [ ] **3.3 TDD**: `ReservationServiceImplTest.findAvailableTable`
  - `returnsSmallestCapacityTableThatFitsPartySize` (5 personas con mesas libres de 6 y 8 → mesa de 6)
  - `returnsEmptyWhenNoTableMatchesPartySize` (7 personas, solo hay mesas de 2 y 4)
  - `skipsTablesOccupiedByPendingReservation`
  - `skipsTablesOccupiedByConfirmedReservation`
  - `ignoresCancelledReservations`
  - `ignoresCompletedReservations`
  - `considersSameSlotOverlapOnly` (una reserva en otra franja del mismo día no bloquea)
- [ ] **3.4**: escribir query SQL: devuelve `table_id` de mesas del restaurante con `capacity >= :partySize` que NO tengan reserva `pending|confirmed` para esa fecha+slot, ordenado por `capacity ASC, id ASC`, LIMIT 1
- [ ] **3.5 TDD**: `ReservationServiceImplTest.listAvailableSlots`
  - `returnsSlotsFilteredByPartySize`
  - `skipsSlotsWithNoAvailableTable`
  - `returnsEmptyWhenDateIsOutsideOperatingDays`
- [ ] **3.6**: implementar `listAvailableSlots(restaurantId, date, partySize)` combinando `SlotCatalog` + `findAvailableTable` por cada slot
- [ ] **3.7**: correr suite completa; verificar queries con EXPLAIN en Postgres local
- [ ] **3.8 Commit**: `feat(reservations): slot catalog and availability service`

**Criterio de cierre**: tests verdes; no hay joins en Java; query usa los índices `(table_id, reservation_date, slot_start)` y `(restaurant_id, reservation_date, status)`.

---

## Fase 4 — Crear reserva desde la página del restaurante

**Objetivo**: usuario autenticado crea reservas desde `restaurant.jsp`. La UI solo aparece si `accepts_reservations = true`.

**Archivos**:
- Crear `webapp/.../controller/ReservationController.java` (POST /restaurantes/{id}/reservas)
- Crear `webapp/.../form/CreateReservationForm.java`
- Crear `service-contracts/.../CreateReservationData.java`
- Extender `ReservationServiceImpl` con `createReservation(...)`
- Modificar `webapp/src/main/webapp/WEB-INF/views/restaurant.jsp`
- Modificar `webapp/src/main/webapp/WEB-INF/views/restaurant.jsp` para renderizar canal alternativo si `false` y ocultar todo si `null`
- Modificar `webapp/.../config/WebAuthConfig.java` (ruta nueva: autenticado)

**Tasks**:

- [ ] **4.1 TDD**: `ReservationServiceImplTest.createReservation`
  - `autoConfirmsWhenRestaurantIsAutomatic_returnsConfirmed`
  - `createsAsPendingWhenRestaurantIsManual`
  - `assignsSmallestFittingTable`
  - `throwsSlotUnavailableWhenNoTable`
  - `throwsIllegalStateWhenRestaurantDoesNotAcceptReservations`
  - `throwsIllegalStateWhenSlotOutsideRestaurantHours`
  - `incrementsNoShowCount_isNotInvoked` (solo chequear que no dispara ningún side effect no esperado)
- [ ] **4.2**: implementar `ReservationServiceImpl.createReservation(CreateReservationData data, User authenticatedUser)` en una sola transacción: busca mesa, inserta reserva, dispara email si queda `confirmed` (vía `@Async`)
- [ ] **4.3 TDD**: `CreateReservationFormValidationTest` para:
  - partySize fuera de rango (< 1 o > 12) → inválido
  - fecha en el pasado → inválido
  - slot con formato incorrecto → inválido
  - comment > límite → inválido
- [ ] **4.4**: implementar `CreateReservationForm` con JSR380 (`@NotNull`, `@Future`, `@Min(1) @Max(12)`, `@Size(max=500)` en comment)
- [ ] **4.5 MVC test**: `ReservationControllerMvcTest`
  - `post_reservation_anonymous_redirectsToLogin`
  - `post_reservation_authenticated_createsAndRedirectsToProfile`
  - `post_reservation_toRestaurantWithReservationsDisabled_returns400OrForbidden`
  - `post_reservation_unavailableSlot_returnsFormWithError`
- [ ] **4.6**: implementar `ReservationController`:
  - `@PreAuthorize("isAuthenticated()")` en el método de creación
  - toma el user del `SecurityContext` (no del form)
  - delega al servicio; captura `SlotUnavailableException` con ControllerAdvice
- [ ] **4.7**: editar `restaurant.jsp`:
  - si `accepts_reservations = true`: render del form (date picker, cantidad de personas, dropdown de slots disponibles cargado server-side en primer render y refrescable vía JS simple)
  - si `accepts_reservations = false`: render del canal alternativo según `reservation_disabled_mode` (string i18n + `<c:out>` sobre link/teléfono)
  - si `accepts_reservations = null`: no render de nada relacionado a reservas
- [ ] **4.8**: i18n para labels, errores y mensajes de éxito
- [ ] **4.9 Commit**: `feat(reservations): create reservation flow from restaurant page`

**Criterio de cierre**: usuario autenticado crea reservas en ambos modos (auto/manual); restaurantes con false muestran canal alternativo; restaurantes null ocultan el bloque.

---

## Fase 5 — Gestión de reservas por el usuario

**Objetivo**: `/perfil/{unique_user_slug}/reservas` listado + cancelación.

**Archivos**:
- Modificar `webapp/.../controller/ProfileController.java` (ruta nueva) o crear `UserReservationsController.java`
- Crear `webapp/src/main/webapp/WEB-INF/views/profile/reservations.jsp`
- Extender `ReservationServiceImpl` con `cancelAsUser(userId, reservationId)`
- Modificar `webapp/.../config/WebAuthConfig.java`

**Tasks**:

- [ ] **5.1 TDD**: `ReservationServiceImplTest.cancelAsUser`
  - `cancelsOwnPendingReservation_setsStatusCancelled`
  - `cancelsOwnConfirmedReservation_setsStatusCancelled`
  - `throwsForbiddenWhenUserIsNotOwner`
  - `throwsIllegalStateWhenReservationIsCompleted`
  - `throwsIllegalStateWhenReservationIsAlreadyCancelled`
  - `sendsCancellationEmail`
- [ ] **5.2**: implementar `cancelAsUser` con verificación de ownership y transición de estado
- [ ] **5.3 TDD**: `ReservationServiceImplTest.listUserReservations_paginated`
- [ ] **5.4**: implementar `listUserReservations(userId, PageRequest)` usando el patrón de `Page<T>` existente
- [ ] **5.5 MVC test**: `UserReservationsControllerMvcTest`
  - `get_page_anonymous_redirectsToLogin`
  - `get_page_authenticated_showsOwnReservationsOnly`
  - `get_page_otherUserSlug_returns403Or404`
  - `post_cancel_valid_updatesState`
  - `post_cancel_notOwner_returns403`
- [ ] **5.6**: implementar `UserReservationsController` o extender `ProfileController` con GET y POST cancel
- [ ] **5.7**: crear `profile/reservations.jsp` con tabla paginada usando tag `<paw:paging>`. Campos: restaurante (link), fecha, slot, personas, estado. Botón Cancelar condicional a `pending|confirmed` + CSRF token
- [ ] **5.8 Commit**: `feat(reservations): user reservation listing and cancellation`

**Criterio de cierre**: usuario ve solo sus reservas; cancela las suyas; no puede tocar las de otros; paginación usa tag existente.

---

## Fase 6 — Gestión de reservas por el restaurant admin

**Objetivo**: `/mis-restaurantes/{restaurant_id}/reservas` con confirmar, cancelar, marcar no_show.

**Archivos**:
- Crear `webapp/.../controller/OwnerReservationController.java`
- Crear `webapp/src/main/webapp/WEB-INF/views/owner/reservations.jsp`
- Extender `ReservationServiceImpl` con `confirmAsOwner`, `cancelAsOwner`, `markNoShow`, `unmarkNoShow`
- Modificar `webapp/.../config/WebAuthConfig.java`

**Tasks**:

- [ ] **6.1 TDD**: `ReservationServiceImplTest.confirmAsOwner`
  - `confirmsPending_setsStatusConfirmedAndSendsEmail`
  - `throwsWhenReservationIsNotPending`
  - `throwsWhenOwnerDoesNotOwnRestaurant`
- [ ] **6.2 TDD**: `ReservationServiceImplTest.cancelAsOwner`
  - `cancelsPendingOrConfirmed_sendsCancellationEmail`
  - `throwsWhenOwnerDoesNotOwnRestaurant`
- [ ] **6.3 TDD**: `ReservationServiceImplTest.markNoShow`
  - `incrementsNoShowCounterOnUser`
  - `allowsUnmarkingAndDecrementsCounter`
  - `throwsWhenOwnerDoesNotOwnRestaurant`
  - `noShowDoesNotChangeReservationStatus`
- [ ] **6.4**: implementar los 4 métodos en el servicio (ownership via consulta al DAO, no if-else en controller)
- [ ] **6.5 MVC test**: `OwnerReservationControllerMvcTest` cubriendo ownership, transiciones y paginación
- [ ] **6.6**: implementar `OwnerReservationController` con endpoints `GET /mis-restaurantes/{id}/reservas`, `POST /mis-restaurantes/{id}/reservas/{reservationId}/confirm`, `/cancel`, `/no-show`, `/unmark-no-show`
- [ ] **6.7**: crear `owner/reservations.jsp`. Campos: usuario, fecha, slot, personas, mesa asignada (número), estado, comentario (`<c:out>`), flag `no_show` cuando aplica. Botones condicionales por estado. Usar `<paw:paging>`
- [ ] **6.8**: proteger rutas `/mis-restaurantes/{id}/reservas/**` en `WebAuthConfig` (rol `RESTAURANT_OWNER` o `ADMIN` + ownership chequeado en servicio)
- [ ] **6.9 Commit**: `feat(reservations): owner reservation management`

**Criterio de cierre**: admin confirma, cancela y marca no_show solo sobre sus restaurantes; lista paginada; contador `users.no_show_count` se actualiza correctamente.

---

## Fase 7 — Transiciones temporales y schedulers

**Objetivo**: scheduler que borra pendings vencidos y completa confirmados vencidos.

**Archivos**:
- Crear `services/.../ReservationSchedulerImpl.java`
- Modificar `services/.../ServicesConfig.java` con `@EnableScheduling` y `@EnableAsync`
- Crear `services/src/test/java/.../ReservationSchedulerImplTest.java`

**Tasks**:

- [ ] **7.1 TDD**: `ReservationSchedulerImplTest`
  - `tick_deletesAllPendingWithSlotEndBeforeNow`
  - `tick_transitionsConfirmedWithSlotEndBeforeNow_toCompleted`
  - `tick_leavesFutureReservationsUntouched`
  - `tick_leavesCancelledUntouched`
  - `tick_leavesCompletedUntouched`
  - `tick_sendsCompletionEmailOnlyOncePerReservation`
- [ ] **7.2**: implementar `ReservationSchedulerImpl.tick()` en una transacción (o dos: una por cada query), usando `ZoneId.of("America/Argentina/Buenos_Aires")` para construir `now`
- [ ] **7.3**: anotar con `@Scheduled(fixedDelay = 300000)` (5 min)
- [ ] **7.4**: agregar `@EnableScheduling` y `@EnableAsync` en `ServicesConfig`. Verificar que no se habilite en tests (usar perfil o un `TaskScheduler` mock en tests)
- [ ] **7.5**: validar en local que el scheduler arranca con Jetty y no bloquea el servlet
- [ ] **7.6 Commit**: `feat(reservations): scheduler for expired pendings and completions`

**Criterio de cierre**: tests verdes; scheduler corre en dev sin errores; logs muestran transiciones.

---

## Fase 8 — Emails transaccionales

**Objetivo**: 4 emails (confirmación, cancelación, recordatorio 11:00 AM, post-completion) con idempotencia.

**Archivos**:
- Crear `webapp/src/main/resources/mail-templates/reservation-{confirmed,cancelled,reminder,completed}.html`
- Modificar `services/.../SmtpMailServiceImpl.java` (o `MailService` contract) con métodos para los 4 tipos
- Extender `ReservationSchedulerImpl` con `sendDailyReminders()` con cron 11:00 AM GMT-3

**Tasks**:

- [ ] **8.1**: crear los 4 templates Thymeleaf con textos en locale del destinatario (ver [[mailing]]). Confirmation con CTA a `/perfil/{slug}/reservas`; Cancelled con CTA a `/explorar`; Reminder con CTA a `/perfil/{slug}/reservas`; Completed con CTA a `/restaurantes/{slug}?review={reservationId}`
- [ ] **8.2**: agregar al contract `MailService`: `sendReservationConfirmed`, `sendReservationCancelled`, `sendReservationReminder`, `sendReservationCompleted`. Implementar en `SmtpMailServiceImpl` usando el patrón existente
- [ ] **8.3 TDD**: tests de servicio de reservas verifican que cada transición dispara el email correcto (usando Mockito sobre `MailService`)
- [ ] **8.4**: antes de disparar un email, chequear `reservation_notifications` por `(reservation_id, type)`; si existe, skip. Después de disparar, insertar la fila
- [ ] **8.5 TDD**: `ReservationSchedulerImplTest.sendDailyReminders`
  - `sendsReminderForAllReservationsOfTodayCreatedBefore11AM`
  - `skipsReservationsCreatedAfter11AM`
  - `skipsCancelledOrCompleted`
  - `isIdempotentWhenCalledTwice` (usa tabla de notifications)
- [ ] **8.6**: implementar `sendDailyReminders` con `@Scheduled(cron = "0 0 11 * * *", zone = "America/Argentina/Buenos_Aires")`
- [ ] **8.7**: tests MVC verifican que crear en modalidad automática dispara email y crear en manual NO dispara hasta confirmar
- [ ] **8.8 Commit**: `feat(reservations): transactional emails and daily reminder`

**Criterio de cierre**: en dev se reciben los 4 emails; tests verdes; `reservation_notifications` evita duplicados.

---

## Fase 9 — Reviews atadas a reservas

**Objetivo**: exigir `reservation_id` en creación de review, unicidad por reserva, CTA en página del restaurante solo cuando aplica.

**Archivos**:
- Modificar `persistence/src/main/resources/schema-base.sql` + variants (UNIQUE sobre `reviews.reservation_id`)
- Modificar `service-contracts/.../ReviewService.java`
- Modificar `services/.../ReviewServiceImpl.java`
- Modificar `webapp/.../controller/RestaurantController.java` o `ReviewController` (donde esté el POST de review)
- Modificar `webapp/src/main/webapp/WEB-INF/views/restaurant.jsp`

**Política lockeada para reviews legacy**: las reviews existentes se **preservan como read-only**. `reviews.reservation_id` se mantiene nullable con UNIQUE; Postgres y HSQLDB permiten múltiples NULLs en UNIQUE, por lo que todas las reviews legacy conviven sin romper la constraint. El servicio exige `reservation_id NOT NULL` para cualquier review nueva. La UI no expone edición ni borrado sobre reviews legacy (solo lectura).

**Tasks**:

- [ ] **9.1**: agregar UNIQUE constraint sobre `reviews.reservation_id` en `schema-base.sql`, `schema-postgres.sql` y `schema-hsqldb.sql`. Verificar con un test de persistencia que una segunda review con el mismo `reservation_id` no-null falla, y que múltiples reviews con `reservation_id = NULL` coexisten
- [ ] **9.2 TDD**: `ReviewServiceImplTest.createReview`
  - `createsReviewWhenUserHasCompletedReservationWithoutReview`
  - `throwsWhenReservationIdIsNull` (regla nueva: sin reserva no se crea)
  - `throwsWhenReservationIsNotCompleted`
  - `throwsWhenReservationBelongsToOtherUser`
  - `throwsWhenReservationAlreadyHasReview`
- [ ] **9.3**: extender `ReviewService.create(...)` recibiendo `reservation_id` obligatorio; validar en servicio (no en form) que la reserva existe, pertenece al usuario autenticado, está `completed` y no tiene review previa
- [ ] **9.4 TDD**: `ReviewServiceImplTest.legacyReviewsAreReadOnly`
  - `listByRestaurant_includesLegacyReviewsWithNullReservationId` (se siguen mostrando)
  - no existe `updateReview` ni `deleteReview` expuestos para reviews legacy
- [ ] **9.5**: asegurarse de que el repo no tenga rutas públicas de edición/borrado de reviews. Si existen, removerlas o restringirlas a ADMIN. Confirmar explícitamente el estado actual antes de tocar
- [ ] **9.6**: modificar vista del restaurante (`restaurant.jsp`) para:
  - listar todas las reviews (legacy y nuevas) en solo lectura
  - mostrar CTA "Dejar review" solo si el usuario autenticado tiene al menos una reserva `completed` sin review en ese restaurante
- [ ] **9.7 MVC test**:
  - `post_review_withoutReservationId_returns400`
  - `post_review_withForeignReservation_returns403`
  - `post_review_withCompletedOwnReservation_persistsReview`
  - `get_restaurantPage_asAnonymous_showsLegacyAndNewReviews`
- [ ] **9.8**: ajustar email post-completion para linkear a `/restaurantes/{slug}?review={reservationId}`; la vista precarga el contexto desde el query param
- [ ] **9.9 Commit**: `feat(reservations): reviews gated by completed reservation, legacy preserved read-only`

**Criterio de cierre**: no existen rutas para crear review fuera del flujo post-completion; unicidad por reserva garantizada por BD; reviews legacy siguen visibles en la página del restaurante sin posibilidad de edición.

---

## Fase 10 — Hardening transversal

**Objetivo**: i18n, seguridad, logging, ControllerAdvice, README de credenciales.

**Archivos**:
- Modificar `webapp/src/main/resources/i18n/messages*.properties`
- Modificar `webapp/.../exceptions/` y `ErrorHandlingAdvice.java`
- Modificar `README.md`
- Verificar `webapp/.../auth/` y `config/WebAuthConfig.java`

**Tasks**:

- [ ] **10.1**: auditoría de textos: toda key nueva debe existir en ES y EN. Escribir un test que cargue ambos `Properties` y verifique simetría
- [ ] **10.2**: mapear todas las excepciones nuevas (`SlotUnavailableException`, `LayoutFrozenException`, `ReservationNotFoundException`) a códigos HTTP claros en `ErrorHandlingAdvice`
- [ ] **10.3**: revisar que ninguna vista muestre inputs sin `<c:out>`: comentarios de reserva, link externo, teléfono
- [ ] **10.4**: logs con SLF4J en todos los servicios (creación, cancelación, confirmación, no_show, tick del scheduler)
- [ ] **10.5**: verificar que `WebAuthConfig` protege `/restaurantes/*/reservas` (POST, autenticado), `/perfil/{slug}/reservas` (autenticado), `/mis-restaurantes/{id}/reservas/**` (rol OWNER o ADMIN)
- [ ] **10.6**: actualizar `README.md` con credenciales de dev para admin, owner y reviewer (si no están ya)
- [ ] **10.7**: correr `mvn clean package` y verificar que el WAR arranca sin `database.properties` / `mail.properties` / `security.properties` faltantes
- [ ] **10.8 Commit**: `chore(reservations): i18n, error handling, security, readme`

**Criterio de cierre**: suite entera verde; WAR empaquetable; `SecurityMvcTest` pasa con rutas nuevas; auditoría de i18n sin huérfanos.

---

## Checklist de requerimientos core TP1

Cruzando con [[criterios-evaluacion]]:

- [ ] Roles: reviewer vs restaurant_owner vs admin con ownership aplicado (fases 5, 6, 10)
- [ ] i18n completo en UI, errores y emails (fases 2, 4, 5, 6, 8, 10)
- [ ] Logging SLF4J sin System.out (fase 10 + transversal)
- [ ] Mailing funcional: confirmación, cancelación, recordatorio, post-completion (fase 8)
- [ ] Queries no triviales resueltas en BD: disponibilidad con joins y filtros (fase 3)
- [ ] Spring Security como freno principal, sin if-else locales (fases 2-6, 10)
- [ ] XSS/LIKE: `<c:out>` sobre comentarios y link externo; sin LIKE abierto sobre inputs (fases 2, 4, 10)
- [ ] Testing unitario: HSQLDB + `@Rollback` + Mockito sobrio (todas las fases)
- [ ] README con credenciales operativas (fase 10)

## Orden de branches y PRs

1. `feat/reservas-schema` → Fase 1
2. `feat/reservas-config` → Fase 2
3. `feat/reservas-disponibilidad` → Fase 3
4. `feat/reservas-flujo-usuario` → Fases 4 + 5
5. `feat/reservas-admin` → Fase 6
6. `feat/reservas-schedulers-emails` → Fases 7 + 8
7. `feat/reservas-reviews` → Fase 9
8. `feat/reservas-hardening` → Fase 10

Cada branch entra a `dev` con PR independiente, MVC tests verdes y recorrido manual documentado.

## Ver también

- [[resumen-spec-reservas]]
- [[spring-security]]
- [[mailing]]
- [[persistencia-jdbc]]
- [[validacion-formularios]]
- [[logica-en-controllers]]
- [[internacionalizacion]]
- [[testing-unitario]]
- [[criterios-evaluacion]]
