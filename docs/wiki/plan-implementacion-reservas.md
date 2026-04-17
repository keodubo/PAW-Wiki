---
titulo: Plan de Implementación del Sistema de Reservas
tipo: sintesis
fuentes: [raw/reservas/spec-funcional-reservas.md, raw/diagrama_bd.md]
creado: 2026-04-15
actualizado: 2026-04-17
---

# Plan de Implementación del Sistema de Reservas

> **Para agentes**: este plan usa convenciones de `superpowers:writing-plans`. Ejecutar con `superpowers:executing-plans` o `superpowers:subagent-driven-development` una fase por vez.

**Goal**: implementar el sistema de reservas definido en [[resumen-spec-reservas]] sobre el repo actual, sin romper el flujo existente de restaurantes ni reviews.

**Arquitectura**: capa de persistencia JDBC (joins y filtros en BD), capa de servicios `@Transactional` con lógica de disponibilidad y asignación de mesa, capa web con controllers finos validando con JSR380, vistas JSP con `<c:out>`, i18n y Spring Security. Dos jobs `@Scheduled` para transiciones temporales y emails de recordatorio.

**Tech stack**: Java 21, Spring Web MVC (sin Boot), JDBC, PostgreSQL, HSQLDB en memoria para tests, JSP/JSTL, Spring Security, Bean Validation JSR380, SLF4J/Logback, Thymeleaf para emails, JUnit + Mockito + `@Rollback`.

---

## Reglas de implementación no-negociables

Cualquier task del plan se implementa respetando estas reglas. Son la extensión obligatoria del wiki ([[criterios-evaluacion]], [[logica-en-controllers]], [[persistencia-jdbc]], [[spring-security]], [[testing-unitario]], [[validacion-formularios]], [[xss-prevencion]], [[manejo-excepciones]], [[internacionalizacion]], [[logging]], [[jsp-jstl]], [[transactional]], [[mailing]]).

### Persistencia
- `java.sql` **solo** dentro del módulo `persistence`. En models y services usar `LocalDate`, `LocalTime`, `LocalDateTime`. `reservation_date → LocalDate`; `slot_start`/`slot_end → LocalTime`; `created_at`/`updated_at`/`sent_at → LocalDateTime`.
- `RowMapper`: **siempre** `private static final` al tope de la clase DAO. Nunca `new RowMapper` por método.
- `SimpleJdbcInsert`: inicializar **una sola vez** en constructor (no en cada método). Usar `executeAndReturnKey()` para IDs generados.
- Una tabla la modifica **un solo DAO**. Cascadas con `ON DELETE CASCADE` en SQL, no en Java.
- Joins, filtros y orden en SQL. Prohibido hacer joins o filtrado en Java (`.stream().filter(...)` sobre datos de BD = violación). Paginación con query + `LIMIT/OFFSET`, nunca `subList`.
- La capa `persistence` recibe un `Page`/`PageRequest`, no `offset/limit` sueltos.

### Autorización (Spring Security, declarativa)
- **No mezclar** `@PreAuthorize` con reglas de URL en `WebAuthConfig`. En este plan se usa **exclusivamente** `WebAuthConfig` + SpEL (`.access("@AccessHelper...")`).
- Ownership de reservas y restaurantes: resolver con un bean `AccessHelper` invocado desde `WebAuthConfig` via SpEL, **no** con ifs en controller ni en servicio.
  - Ej: `.access("@AccessHelper.canCancelReservation(authentication, #reservationId)")`.
- Los servicios reciben el `User` autenticado como parámetro pero **no hacen** `if (!owner) throw`. Si el flujo llega al servicio, ya pasó la expression.
- Taglib `<sec:authorize access="...">` para mostrar/ocultar UI por rol o expression. Nunca condicionales Java en JSP sobre roles.
- HTTP: **403 cuando el recurso existe sin permisos**; **404 solo cuando no existe**. No mezclar (`returns403Or404` está prohibido).

### Controllers y validación
- Controllers finos: reciben `@Valid FormDTO + BindingResult`, arman un DTO inmutable (`CreateReservationData`, `SaveRestaurantData`), llaman **un** método del servicio, retornan vista.
- Validación cross-field: **class-level `@Constraint`** con su `ConstraintValidator`. Nunca invocar un `Validator` manualmente desde el controller.
- Excepciones de dominio: `SlotUnavailableException`, `LayoutFrozenException`, `ReservationNotFoundException` viven en `service-contracts` y se mapean a HTTP status en `@ControllerAdvice` (`ErrorHandlingAdvice`).
- `@ModelAttribute` en un `@ControllerAdvice` para el usuario logueado; no llamar `SecurityContextHolder` en cada método.

### Transaccionalidad
- Todos los métodos públicos de servicio con `@Transactional`; selectores con `@Transactional(readOnly = true)`.
- `MailService` y `ReservationScheduler` **no** llevan `@Transactional` a nivel clase. El scheduler delega en métodos del servicio que sí son `@Transactional`.
- Nada de `this.otro()` interno esperando proxy — siempre entrar por bean externo.

### Testing
- `verify()` de Mockito **prohibido**. Toda validación es sobre estado final (retorno, BD, tabla `reservation_notifications`, contadores).
- Para asertar "se mandó el email X": el servicio inserta en `reservation_notifications` **antes** de delegar al `MailService`. El test assertea rowcount + tipo en esa tabla con `JdbcTestUtils.countRowsInTable` o query directa. El doble de `MailService` puede ser un stub que falla si se invoca dos veces con la misma clave, sin `verify()`.
- Tests JDBC: precondiciones **siempre** con scripts SQL (`@Sql("/sql/...")` o `JdbcTestUtils.execute`), nunca usando el DAO bajo test. `@Rollback` por defecto. HSQLDB vía `TestConfiguration` existente.
- Tests de servicio: `@Mock` + `@InjectMocks`, asserts sobre retorno; cubrir camino feliz y no felices.
- Tests MVC (controllers): verifican status, redirects y bindings. Son de integración liviana; no suplantan a los tests de servicio.

### Vistas
- Todo valor dinámico con `<c:out>`; URLs con `<c:url>`; textos con `<spring:message>`.
- Renderizado condicional por rol o autenticación con `<sec:authorize>`, no con lógica Java.
- Mensajes i18n completos con placeholders (`arguments="..."`); prohibido concatenar partes traducidas.
- La auditoría i18n cubre **labels, errores, titles (`<title>`) y alt texts** de imágenes.

### Logging
- `private static final Logger LOGGER = LoggerFactory.getLogger(ClaseActual.class);` por clase.
- Interpolación con `{}`: `LOGGER.info("reserva {} cancelada por user {}", id, userId);`. Prohibido `System.out`, `printStackTrace`, concatenación con `+`.

### Mailing
- `MailService` vive en `services`, recibe `Locale` del destinatario (no `LocaleContextHolder`). `@Async` en los métodos; `@EnableAsync` en `ServicesConfig`.
- Idempotencia vía `reservation_notifications`: `INSERT` condicionado por `UNIQUE(reservation_id, notification_type)`. Si el insert pincha por UNIQUE, el email no se reenvía.

---

## Decisiones lockeadas (confirmadas con el usuario)

| Tema | Decisión |
|------|----------|
| `accepts_reservations = null` | restaurante legacy; owner dashboard muestra badge "no definido"; página pública oculta el bloque de reservas y el canal alternativo; no forzamos migración |
| Pending cuyo slot venció | **se borra la reserva** (DELETE físico) — no hay estado `expired`, no hay transición a `cancelled` |
| Scheduler de borrado de pendings vencidos | corre cada **5 minutos**; idempotente |
| Slot duration | enum `SLOT_60` / `SLOT_90` / `SLOT_120` (minutos), se usa también para armar slots |
| Generación de slots | arrancan en `open_time`, se concatenan; si el último excede `close_time`, **se acorta** |
| Zona horaria | **GMT-3** (Argentina, sin DST activo); todo scheduler usa `ZoneId.of("America/Argentina/Buenos_Aires")` |
| Teléfono de contacto alternativo | **misma validación que `restaurantForm.phone`**: `@NotEmpty @Size(8-50) @Pattern("^(?=.*[0-9])[0-9+ -]+$")` |
| Obligatoriedad de `reservation_disabled_mode` cuando `accepts_reservations=false` | **obligatorio**; si no se elige opción, el save falla con error in-line |
| Transición `confirmed → completed` | por scheduler cada 5 min |
| Reviews legacy y reviews sin reserva | preservar como read-only las legacy; `reservation_id` nullable con UNIQUE (múltiples NULLs permitidos); las nuevas reviews admiten `reservation_id` opcional (si se provee y está completada, la review aparece como "verificada" con un tic); la UI no expone edición ni borrado sobre reviews legacy |
| Admin bypass en gestión de reservas | **ADMIN** tiene acceso a ambas vistas. `/perfil/{slug}/reservas` la ven el dueño del slug y cualquier `ADMIN`. `/mis-restaurantes/{id}/reservas` la ven el owner del restaurante y cualquier `ADMIN`. Cancelación de reservas: el dueño de la reserva o un `ADMIN`. Todo resuelto por `AccessHelper`, nunca con ifs en controller o servicio. |
| `accepts_reservations` como enum (v2-Q4) | se modela como enum Java `AcceptsReservationsStatus { NOT_DEFINED, DISABLED, ENABLED }` persistido en una columna `VARCHAR(16) NULL` en `restaurants` (NULL mapea a `NOT_DEFINED`). Motivo: el usuario planea sumar más estados a futuro; un enum mantiene el upgrade path abierto sin migraciones disruptivas. |
| Gap grande que degrada a pending (v2-Q1) | `ratio = assigned_table.type / people_count`; si `ratio >= 2`, la reserva entra **siempre** como `pending` aunque la modalidad sea `automatic`. Ejemplo locked: 2 personas en mesa de 4 → ratio 2 → pending (requiere revisión del owner). La vista del owner debe mostrar esa diferencia explícitamente (ej: "mesa de 4 asignada a 2 comensales"). |
| Email "pending para el owner" (v2-Q2) | se envía **a todo `pending` recién creado**, sea por modalidad `manual` o por gap grande en `automatic`. Template único `reservation-pending-owner.html`. El CTA **redirige a la vista de gestión de reservas del restaurante** (`/mis-restaurantes/{id}/reservas?focus={reservationId}`) — no dispara confirmación ni rechazo desde el email. El owner decide ahí (confirmar, confirmar con reassignación, cancelar). Idempotente vía `reservation_notifications(type='PENDING_FOR_OWNER')`. |
| Reassignación de mesa por el owner (v2-Q3) | **cualquier** reserva `pending` admite reassignación a otra mesa antes (o como parte) de confirmar. En modalidad `manual` el owner maneja todo el flujo (confirmar, reassignar, cancelar). La reassignación es **opcional**: el owner puede confirmar sin cambiar la mesa. |
| `no_show` fuera de alcance (v2-Q5 / v2-Q6) | **se descarta la feature completa** por decisión del usuario (no se implementará). No hay columna `no_show` en `reservations`, ni acción "marcar no-show" en la UI, ni badge ni efecto sobre `completed` o reviews. |
| Cambio de modalidad con pendings vivos | si el owner cambia `reservation_confirmation_mode` de `manual` a `automatic` (o viceversa) con pendings existentes, **esas pendings quedan intactas**. La regla de gap/modalidad aplica solo a reservas nuevas. Los pendings heredados siguen requiriendo confirmación manual del owner. |
| Concurrencia en la reassignación del owner | alcanza con re-chequeo de disponibilidad dentro del mismo `@Transactional` de `confirmAsOwner` (SELECT + NOT EXISTS sobre `reservations pending|confirmed` en ese `table_id`+`date`+`slot_start`, luego UPDATE). **No** se agrega columna `version` ni optimistic locking. Si el re-check detecta colisión se lanza `TableNotAvailableForSlotException`. |
| Gap para grupos de 1 persona | la regla ratio >= 2 aplica **sin excepciones**: un grupo de 1 asignado a mesa de 2 (la más chica posible) da ratio 2 → `pending`. Confirmado por el usuario; es el comportamiento esperado (single diners requieren revisión del owner si el restaurante no tiene mesas de 1). |
| Tipo SQL de `accepts_reservations` | `VARCHAR(16) NULL` con `CHECK` sobre los 3 valores del enum; **no** se usa tipo `ENUM` nativo de Postgres para mantener compatibilidad con HSQLDB en tests. |
| Labels de botones en `/mis-restaurantes/{id}/reservas` | dependen del estado: `pending` → botones **"Confirmar"** + **"Rechazar"**; `confirmed` → botón **"Cancelar"**; `completed`/`cancelled` → sin botones de acción. "Rechazar" y "Cancelar" usan el **mismo endpoint** (`POST /mis-restaurantes/{id}/reservas/{reservationId}/cancel`) y disparan el mismo email de cancelación al usuario; la diferencia es puramente de i18n/UX según el estado previo. |
| Resolución de paginación para `?focus={reservationId}` | el controller **computa qué página contiene la reserva** y redirige/renderiza esa página, resaltando la fila. Si la reserva no existe o pertenece a otro restaurante, el query param se ignora silenciosamente y se muestra la página 1. Servicio expone `findPageContainingReservation(restaurantId, reservationId, pageSize)` que devuelve un `int page` (0 si no se encuentra). |
| Filtros en `/mis-restaurantes/{id}/reservas` | página admite 2 filtros propagados como query params: (1) **orden por fecha** `?sort=recent` (default, desc por `reservation_date` + `slot_start`) o `?sort=oldest` (asc); (2) **selector de restaurante**: dropdown que lista los restaurantes administrados por el owner autenticado y cambia la URL al restaurante elegido (`/mis-restaurantes/{otherId}/reservas`). El selector usa `RestaurantService.getRestaurantsByOwner(userId)` existente (o el equivalente vigente). El `sort` se persiste junto a `page` y `focus` en los links de paginación. |
| "Dato sensible" del link externo (§4.2) | se interpreta estrictamente como **escapado XSS**: `reservation_external_link` se renderiza siempre con `<c:out>`, el validador rechaza schemes `javascript:` / `data:` y sólo admite `http(s)://`. No hay requisito adicional de logging especial, cifrado en reposo, ni ocultamiento en endpoints — es dato público mostrado en la página del restaurante. Confirmado con el usuario. |
| Reserva para hoy con `slot_start` ya pasado | **se rechaza en la creación** con error in-line. Validación en servicio (`ReservationServiceImpl.createReservation`) que lanza `SlotAlreadyPastException` si `reservation_date == hoy && slot_start <= now()` en `America/Argentina/Buenos_Aires`. El advice la mapea a **400** y redibuja el form con mensaje i18n (`reservation.error.slotAlreadyPast`). No se confía sólo en `@Future` del form (que opera sobre `reservation_date` y no distingue slots del día mismo). |
| Destinatarios del recordatorio 11:00 AM | **sólo reservas `confirmed`** reciben el mail `REMINDER`. Las `pending` no — si al owner se le pasa confirmar, la reserva muere sin recordatorio; las `cancelled`/`completed` tampoco. Motivo: una `pending` puede terminar cancelada y el recordatorio induciría al usuario a presentarse sin reserva real. |
| Mesas elegibles en la reassignación del owner (v2-§5.2) | el owner puede reassignar a **cualquier** mesa con `capacity >= people_count` (y libre en ese `date`+`slot_start`), sin restricción de dirección. Puede ir a una mesa **más chica** que la originalmente asignada por el algoritmo (siempre que aún fitee al grupo) o a una **más grande**. **Nunca** a una mesa con `capacity < people_count` — 3 comensales jamás caen en mesa de 1 o 2. El spec §5.2 ("de menos comensales a una para más") se interpreta como prosa ambigua, no regla de dirección. Confirmado con el usuario. |

---

## Mapa de archivos afectados

### `models/`

- **Crear** `models/src/main/java/ar/edu/itba/paw/models/Reservation.java`
- **Crear** `models/src/main/java/ar/edu/itba/paw/models/ReservationStatus.java` (enum)
- **Crear** `models/src/main/java/ar/edu/itba/paw/models/ReservationConfirmationMode.java` (enum)
- **Crear** `models/src/main/java/ar/edu/itba/paw/models/ReservationDisabledMode.java` (enum: `NONE_MESSAGE`, `WHATSAPP`, `PHONE_CALL`, `EXTERNAL_URL`)
- **Crear** `models/src/main/java/ar/edu/itba/paw/models/ReservationSlotDuration.java` (enum `SLOT_60=60`, `SLOT_90=90`, `SLOT_120=120`, con `int toMinutes()`)
- **Crear** `models/src/main/java/ar/edu/itba/paw/models/AcceptsReservationsStatus.java` (enum `NOT_DEFINED`, `DISABLED`, `ENABLED`; con helper `fromDbValue(String)` que mapea `NULL → NOT_DEFINED`)
- **Crear** `models/src/main/java/ar/edu/itba/paw/models/RestaurantTable.java`
- **Crear** `models/src/main/java/ar/edu/itba/paw/models/TimeSlot.java` (value object con `LocalTime slotStart`, `LocalTime slotEnd`)
- **Crear** `models/src/main/java/ar/edu/itba/paw/models/ReservationSort.java` (enum `DATE_DESC` (default) / `DATE_ASC`; helper `fromQueryParam("recent"|"oldest")` con fallback a `DATE_DESC` si viene ausente o inválido)
- **Modificar** `models/src/main/java/ar/edu/itba/paw/models/Restaurant.java` (nuevos campos)
- **Modificar** `models/src/main/java/ar/edu/itba/paw/models/Review.java` (campo `reservationId`)

### `persistence-contracts/`

- **Crear** `persistence-contracts/src/main/java/ar/edu/itba/paw/persistence/ReservationDao.java`
- **Crear** `persistence-contracts/src/main/java/ar/edu/itba/paw/persistence/RestaurantTableDao.java`
- **Modificar** `persistence-contracts/src/main/java/ar/edu/itba/paw/persistence/RestaurantDao.java` (CRUD extendido con campos de reservas)

### `persistence/`

- **Crear** `persistence/src/main/java/ar/edu/itba/paw/persistence/ReservationJdbcDao.java`
- **Crear** `persistence/src/main/java/ar/edu/itba/paw/persistence/RestaurantTableJdbcDao.java`
- **Modificar** `persistence/src/main/java/ar/edu/itba/paw/persistence/RestaurantJdbcDao.java`
- **Modificar** `persistence/src/main/resources/schema-base.sql`
- **Modificar** `persistence/src/main/resources/schema-postgres.sql`
- **Modificar** `persistence/src/test/resources/schema-hsqldb.sql`

### `service-contracts/`

- **Crear** `service-contracts/src/main/java/ar/edu/itba/paw/services/ReservationService.java` — incluye `listReservationsByRestaurant(restaurantId, ReservationSort, PageRequest)` con sort propagado al SQL y `findPageContainingReservation(restaurantId, reservationId, pageSize, ReservationSort)` → `int` (0 si la reserva no existe, no pertenece al restaurante, o el sort no la alcanza)
- **Crear** `service-contracts/src/main/java/ar/edu/itba/paw/services/CreateReservationData.java`
- **Crear** `service-contracts/src/main/java/ar/edu/itba/paw/services/SlotUnavailableException.java`
- **Crear** `service-contracts/src/main/java/ar/edu/itba/paw/services/LayoutFrozenException.java`
- **Crear** `service-contracts/src/main/java/ar/edu/itba/paw/services/ReservationNotFoundException.java`
- **Crear** `service-contracts/src/main/java/ar/edu/itba/paw/services/TableNotAvailableForSlotException.java` (reassignación del owner a una mesa ocupada/inexistente/incompatible)
- **Crear** `service-contracts/src/main/java/ar/edu/itba/paw/services/SlotAlreadyPastException.java` (creación para hoy con `slot_start` ya vencido; ver decisiones lockeadas)
- **Crear** `service-contracts/src/main/java/ar/edu/itba/paw/services/InvalidReservationSlotException.java` (slot que no pertenece al catálogo del restaurante; mapeado a 400 en `ErrorHandlingAdvice`)
- **Crear** `service-contracts/src/main/java/ar/edu/itba/paw/services/ReservationsDisabledException.java` (intento de crear reserva en restaurante con `accepts_reservations ≠ ENABLED`; mapeado a 400 en `ErrorHandlingAdvice`)
- **Modificar** `service-contracts/src/main/java/ar/edu/itba/paw/services/RestaurantService.java` (config de reservas + layout; además reutiliza `getRestaurantsByOwner(userId)` para poblar el selector del owner — si el nombre actual en el repo difiere, se conserva el nombre vigente)
- **Modificar** `service-contracts/src/main/java/ar/edu/itba/paw/services/SaveRestaurantData.java` (nuevos campos)
- **Modificar** `service-contracts/src/main/java/ar/edu/itba/paw/services/ReviewService.java` (soporte para `reservation_id` opcional y lógica de verificación)

### `services/`

- **Crear** `services/src/main/java/ar/edu/itba/paw/services/ReservationServiceImpl.java`
- **Crear** `services/src/main/java/ar/edu/itba/paw/services/ReservationSchedulerImpl.java` (`@Scheduled`)
- **Modificar** `services/src/main/java/ar/edu/itba/paw/services/RestaurantServiceImpl.java`
- **Modificar** `services/src/main/java/ar/edu/itba/paw/services/ReviewServiceImpl.java`
- **Modificar** `services/src/main/java/ar/edu/itba/paw/services/ServicesConfig.java` (`@EnableScheduling`, `@EnableAsync`)

### `webapp/` — controllers, forms, views

- **Crear** `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/ReservationController.java` (crear + cancelar user)
- **Crear** `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/OwnerReservationController.java` (admin de restaurante: ver, confirmar con reassignación opcional de mesa, cancelar)
- **Crear** `webapp/src/main/java/ar/edu/itba/paw/webapp/form/CreateReservationForm.java`
- **Crear** `webapp/src/main/java/ar/edu/itba/paw/webapp/form/ReservationConfigForm.java` (subform de reservas dentro de RestaurantForm)
- **Crear** `webapp/src/main/java/ar/edu/itba/paw/webapp/form/RestaurantTableGroupForm.java` (tipo + cantidad)
- **Crear** `webapp/src/main/java/ar/edu/itba/paw/webapp/validation/ValidReservationConfig.java` (anotación `@Constraint` class-level) + `webapp/src/main/java/ar/edu/itba/paw/webapp/validation/ReservationConfigValidator.java` (`ConstraintValidator` que implementa la regla; **no** se invoca manualmente)
- **Crear** `webapp/src/main/java/ar/edu/itba/paw/webapp/auth/AccessHelper.java` (bean `@Component("AccessHelper")` con métodos `canCancelReservation(Authentication, Long)`, `canManageRestaurantReservations(Authentication, Long)`, `canViewUserReservations(Authentication, String)`, etc., invocados desde `WebAuthConfig` vía SpEL. Todos los métodos contemplan el bypass de `ROLE_ADMIN` explícitamente.)
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

### `services/` — mailing

- **Crear** `services/src/main/resources/mail-templates/_layout.html` (header + body slot + footer compartido). Los templates de mail viven en `services/` porque `MailService` es servicio y no puede conocer rutas de `webapp` ([[modelo-capas]], [[mailing]]); el repo ya tiene el prefijo `mail-templates/` cableado en `ServicesConfig` del módulo `services`.
- **Crear** `services/src/main/resources/mail-templates/reservation-confirmed.html` (+ `reservation-cancelled.html`, `reservation-reminder.html`, `reservation-completed.html`, `reservation-pending-owner.html`) — todos extienden `_layout.html` con fragmento Thymeleaf y renderizan CTA como botón estilizado
- **Modificar** `services/.../SmtpMailServiceImpl.java` para soportar los 5 templates nuevos (o agregar métodos helper)

### Tests

- **Crear** `persistence/src/test/java/.../ReservationJdbcDaoTest.java`
- **Crear** `persistence/src/test/java/.../RestaurantTableJdbcDaoTest.java`
- **Crear** `services/src/test/java/.../ReservationServiceImplTest.java`
- **Crear** `services/src/test/java/.../ReservationSchedulerImplTest.java`
- **Crear** `webapp/src/test/java/.../ReservationControllerMvcTest.java`
- **Crear** `webapp/src/test/java/.../OwnerReservationControllerMvcTest.java`
- **Crear** `webapp/src/test/java/.../AccessHelperTest.java`
- **Crear** `persistence/src/test/resources/sql/reservations-fixture.sql` (precondiciones para DAO tests, cargado vía `@Sql`)
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

- [x] **1.1 SQL**: agregar columnas a `restaurants`:
  - `accepts_reservations VARCHAR(16) NULL` con `CHECK (accepts_reservations IS NULL OR accepts_reservations IN ('NOT_DEFINED','DISABLED','ENABLED'))` (v2-Q4: enum para admitir más estados a futuro; `NULL` en BD se interpreta como `NOT_DEFINED` en el modelo)
  - `reservation_disabled_mode VARCHAR(32) NULL`
  - `reservation_external_link VARCHAR(512) NULL`
  - `reservation_contact_phone VARCHAR(50) NULL`
  - `reservation_confirmation_mode VARCHAR(16) NULL`
  - `reservation_slot_duration_minutes SMALLINT NULL`
- [x] **1.2 SQL**: crear tabla `restaurant_tables`:
  - `id BIGSERIAL PK`
  - `restaurant_id BIGINT NOT NULL FK`
  - `table_number INTEGER NOT NULL`
  - `capacity SMALLINT NOT NULL`
  - `created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP`
  - UNIQUE `(restaurant_id, table_number)`
  - CHECK `capacity BETWEEN 1 AND 12` (12 representa "12+")
- [x] **1.3 SQL**: crear tabla `reservations`:
  - `id BIGSERIAL PK`
  - `restaurant_id BIGINT NOT NULL FK`
  - `user_id BIGINT NOT NULL FK`
  - `table_id BIGINT NOT NULL FK`
  - `reservation_date DATE NOT NULL`
  - `slot_start TIME NOT NULL`
  - `slot_end TIME NOT NULL`
  - `party_size SMALLINT NOT NULL` (se corresponde con `people_count` del spec v2 §21)
  - `comment TEXT NULL`
  - `status VARCHAR(16) NOT NULL`
  - `created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP`
  - `updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP`
  - índices: `(restaurant_id, reservation_date, status)` y `(table_id, reservation_date, slot_start)`
- [x] **1.4 SQL**: crear tabla `reservation_notifications`:
  - `id BIGSERIAL PK`
  - `reservation_id BIGINT NOT NULL FK ON DELETE CASCADE`
  - `notification_type VARCHAR(32) NOT NULL`
  - `sent_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP`
  - UNIQUE `(reservation_id, notification_type)` para idempotencia
- [x] **1.5 SQL**: agregar columna a `reviews`:
  - `reservation_id BIGINT NULL` (nullable en esta fase; UNIQUE se decide en Fase 9)
- [x] **1.6**: ajustar HSQLDB mirror (`schema-hsqldb.sql`) con sintaxis equivalente
- [x] **1.7**: crear enums y POJOs del modelo (`Reservation`, `RestaurantTable`, `TimeSlot`, 5 enums: `ReservationStatus`, `ReservationConfirmationMode`, `ReservationDisabledMode`, `ReservationSlotDuration`, `AcceptsReservationsStatus`). Tipos de tiempo en Java: `reservation_date → LocalDate`, `slot_start`/`slot_end → LocalTime`, `created_at`/`updated_at` → `LocalDateTime`. `Restaurant.acceptsReservations` como `AcceptsReservationsStatus` (nunca null en Java — el RowMapper traduce `NULL → NOT_DEFINED`). Ningún import de `java.sql.*` en el módulo `models`.
- [x] **1.8**: extender `Restaurant.java` y `Review.java` con los nuevos campos + getters/setters (respetando estilo existente). **`Review.reservationId` es `Long` boxed (nullable)**, nunca `long` primitivo: las reviews legacy tienen `reservation_id = NULL` y el modelo debe representarlo explícitamente. Getter `getReservationId()` devuelve `Long` (posiblemente `null`); la presencia se chequea con `!= null`, no con centinelas tipo `0`/`-1`.
- [x] **1.9 Fixture**: crear `persistence/src/test/resources/sql/reservations-fixture.sql` con inserts de 1 restaurante, 3 mesas (capacidades 2/4/6), 2 usuarios, `restaurant_hours` (horario fijo **19:00–23:00** todos los días de la semana para que sirva directo al `SlotCatalogTest` de Fase 3 sin configuración extra) y 2 reservas de ejemplo (1 `pending` para hoy+7 días a las 19:00, 1 `confirmed` para hoy+7 días a las 20:30). Será la precondición reutilizable para tests de DAO via `@Sql("/sql/reservations-fixture.sql")`. Mantener la fecha relativa (`CURRENT_DATE + INTERVAL '7 days'` o equivalente HSQLDB) para que la fixture nunca vencie y los tests no sean flaky.
- [x] **1.10 TDD**: `RestaurantTableJdbcDaoTest` — todas las precondiciones vía `@Sql` (nunca vía el DAO bajo test); validar estado post-operación con `JdbcTestUtils.countRowsInTable`.
  - test `create_generatesTablesWithUniqueTableNumberPerRestaurant`
  - test `findByRestaurantId_returnsAllTablesOrderedByCapacityAsc`
  - test `deleteByRestaurantId_removesAllTables`
- [x] **1.11**: implementar `RestaurantTableJdbcDao` hasta que pasen los tests. `RowMapper` como `private static final ROW_MAPPER = ...`; `SimpleJdbcInsert` inicializado en constructor una sola vez. Joins/orden en SQL, no en Java.
- [x] **1.12 TDD**: `ReservationJdbcDaoTest` — precondiciones vía `@Sql`; asserts con `JdbcTestUtils`/queries directas; **sin** `verify()`.
  - test `create_persistsAndAssignsId`
  - test `findByUserId_returnsOrderedByDateDesc`
  - test `findByRestaurantId_returnsOrderedByDateAsc`
  - test `findOverlappingForTable_excludesCancelledAndCompleted`
  - test `deleteExpiredPending_removesOnlyPendingWithSlotEndBeforeNow`
- [x] **1.13**: implementar `ReservationJdbcDao` con misma disciplina (RowMapper estático, SimpleJdbcInsert en constructor). Ninguna query con `SELECT *`; columnas explícitas.
- [x] **1.14**: correr `mvn clean test` completo; todo verde
- [x] **1.15 Commit**: `feat(reservations): schema and DAOs for reservations and tables`

**Supuesto del modelo horario**: los slots se derivan directamente de `restaurant_hours` (open/close del día) y siempre quedan dentro del mismo día calendario. No hay soporte para turnos que crucen medianoche; si el schema actual admite `close < open`, ese caso queda fuera del alcance de esta feature.

**Criterio de cierre**: suite entera verde; migración corre limpia en Postgres local (`dropdb && createdb && jetty:run`); tests de DAO cubren inserción, lectura y eliminación de pendings vencidos.

---

## Fase 2 — Configuración de reservas en crear/editar restaurante

**Objetivo**: `RestaurantForm` admite los tres casos (null/false/true) con validaciones condicionales y persiste el layout.

**Archivos**:
- Modificar `webapp/.../form/RestaurantForm.java`
- Crear `webapp/.../form/ReservationConfigForm.java`
- Crear `webapp/.../form/RestaurantTableGroupForm.java`
- Crear `webapp/.../validation/ValidReservationConfig.java` + `webapp/.../validation/ReservationConfigValidator.java` (`@Constraint` **class-level** sobre `ReservationConfigForm`; el controller solo recibe `@Valid` y `BindingResult`, **nunca** invoca el validator manualmente)
- Modificar `webapp/.../controller/OwnerDashboardController.java`
- Modificar `webapp/src/main/webapp/WEB-INF/views/owner/restaurant-form.jsp`
- Modificar `service-contracts/.../SaveRestaurantData.java`
- Modificar `services/.../RestaurantServiceImpl.java`
- Modificar `webapp/src/main/resources/i18n/messages.properties` y `messages_es.properties`

**Tasks**:

- [x] **2.1 TDD**: test `ReservationConfigFormValidationTest` cubriendo:
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
- [x] **2.2**: crear `ReservationConfigForm`, `RestaurantTableGroupForm`, la anotación `@ValidReservationConfig` (class-level, activada por JSR380) y el `ReservationConfigValidator` que implementa `ConstraintValidator<ValidReservationConfig, ReservationConfigForm>`. Reutilizar anotaciones de `RestaurantForm.phone` para el teléfono (`@NotEmpty @Size(8,50) @Pattern("^(?=.*[0-9])[0-9+ -]+$")`). Validar URL con `@org.hibernate.validator.constraints.URL` y con regex que excluya `javascript:` / `data:` schemes. Los mensajes de error usan claves i18n (`{reservation.disabledMode.required}`) sin concatenar strings.
- [x] **2.3**: anotar `ReservationConfigForm` con `@ValidReservationConfig` a nivel clase y componer `RestaurantForm` con `@Valid ReservationConfigForm reservationConfig`. El controller recibe `@Valid RestaurantForm + BindingResult`; si hay errores, redibuja el form sin perder valores; **prohibido** invocar `validator.validate(...)` manualmente.
- [x] **2.4**: extender `SaveRestaurantData` + `RestaurantServiceImpl.createRestaurant/updateRestaurant` para persistir la config y generar las mesas del layout (`restaurant_tables` se reemplaza en update si no hay reservas bloqueantes)
- [x] **2.5 TDD**: test `RestaurantServiceImplTest.updateLayout_failsWhenPendingOrConfirmedExist` (lanza `LayoutFrozenException`)
- [x] **2.5b TDD**: test `RestaurantServiceImplTest.changingConfirmationMode_doesNotReEvaluateExistingPendings` — precondición `@Sql`: 2 reservas `pending` con ratio chico (<2), restaurante en modalidad `manual`. Acción: update a modalidad `automatic`. Assert: las 2 reservas siguen `pending` (ningún auto-confirm retroactivo); solo reservas **nuevas** creadas después del cambio aplican la regla
- [x] **2.6**: chequear en `RestaurantServiceImpl.updateRestaurant` si hay reservas `pending|confirmed` activas para el restaurante; si sí y el layout cambia, lanzar `LayoutFrozenException`
- [x] **2.7 MVC test**: `OwnerDashboardControllerMvcTest.createRestaurantWithReservationsEnabled_persistsConfigAndLayout`
- [x] **2.8 MVC test**: `OwnerDashboardControllerMvcTest.editRestaurantWithActiveReservations_returnsErrorWhenChangingLayout`
- [x] **2.9**: editar `owner/restaurant-form.jsp`:
  - radio group null/false/true
  - subform false (visible si false): select con 4 opciones de `reservation_disabled_mode`, inputs condicionales para link/teléfono, todo con `<c:out>`
  - subform true (visible si true): radio `AUTOMATIC`/`MANUAL`, select slot_duration, UI de layout (agregar/quitar grupos tipo+cantidad)
  - JavaScript mínimo para mostrar/ocultar subforms según valor elegido (progressive enhancement; el server valida igual)
- [x] **2.10**: i18n keys nuevas en ambos `messages*.properties` (labels, errores, ayudas)
- [x] **2.11**: correr `mvn -pl webapp -am jetty:run` y crear/editar un restaurante en cada modalidad manualmente
- [x] **2.12 Commit**: `feat(reservations): restaurant form with reservation config and table layout`

**Criterio de cierre**: form respeta las 3 ramas; tests MVC verdes; editar layout falla si hay reservas activas; todos los textos en ES/EN.

---

## Fase 3 — Disponibilidad y asignación de mesa

**Objetivo**: servicio puro que calcule slots disponibles y asigne la mesa libre más chica compatible. Queries resueltos en BD.

**Archivos**:
- Crear `service-contracts/.../SlotUnavailableException.java`
- Crear `services/.../SlotCatalog.java` (helper estático o `@Component` sin estado; deriva slots a partir de `RestaurantHour[]` + `ReservationSlotDuration`)
- Crear sección nueva en `ReservationServiceImpl` con `findAvailableTable(...)` y `listAvailableSlots(...)`
- Crear tests en `services/src/test/java/.../SlotCatalogTest.java` y `ReservationServiceImplTest.java`

**Regla de slot catalog (locked — reconciliada 2026-04-17)**:

- Arranca en `open_time`.
- Concatenado: `[open, open+d), [open+d, open+2d), ...` con `d ∈ {60, 90, 120}` minutos según `reservation_slot_duration_minutes`.
- Si el siguiente slot `[open+k*d, open+(k+1)*d)` excede `close`, **se acorta** a `[open+k*d, close)` y se incluye en el catálogo mientras tenga duración positiva.
- Ejemplo: 19:00–23:00 con `d=90` → 19:00–20:30, 20:30–22:00 y 22:00–23:00 (último acortado de 90 a 60 minutos).
- Esta regla es coherente con la decisión lockeada (plan:83) que dice "se acorta". La versión anterior de esta sección decía "se descarta" por error de redacción; el código y tests implementan "acorta" correctamente.

**Tasks**:

- [x] **3.1 TDD**: `SlotCatalogTest`
  - `generateSlots_startsAtOpenTime`
  - `generateSlots_90MinDurationChainsCorrectly` (19:00–23:30 con 90 → 19:00, 20:30, 22:00; el último dura 90 exactos)
  - `generateSlots_lastSlotTruncatedWhenExceedsClose` (19:00–23:00 con 90 → 19:00–20:30, 20:30–22:00, 22:00–23:00)
  - `generateSlots_dropsPartialSlotShorterThan60Min` (o ajustar según decisión final)
  - `generateSlots_respectsShiftBoundaries` (un restaurante con dos turnos, ej mediodía y noche)
- [x] **3.2**: implementar `SlotCatalog.generate(RestaurantHour, ReservationSlotDuration)` devolviendo `List<TimeSlot>`
- [x] **3.3 TDD**: `ReservationServiceImplTest.findAvailableTable`
  - `returnsSmallestCapacityTableThatFitsPartySize` (5 personas con mesas libres de 6 y 8 → mesa de 6)
  - `roundsUpToNextCapacityWhenExactSizeUnavailable` (7 personas, mesas libres de 4 y 8 → mesa de 8; caso borde explícito del spec)
  - `returnsEmptyWhenNoTableMatchesPartySize` (7 personas, solo hay mesas de 2 y 4)
  - `skipsTablesOccupiedByPendingReservation`
  - `skipsTablesOccupiedByConfirmedReservation`
  - `ignoresCancelledReservations`
  - `ignoresCompletedReservations`
  - `considersSameSlotOverlapOnly` (una reserva en otra franja del mismo día no bloquea)
  - `allowsMultipleSimultaneousReservationsForSameUser` (un usuario puede tener 2 reservas en slots solapados de restaurantes distintos, o incluso del mismo restaurante en mesas distintas — caso borde explícito del spec, no hay constraint que lo impida)
- [x] **3.4**: escribir query SQL: devuelve `table_id` de mesas del restaurante con `capacity >= :partySize` que NO tengan reserva `pending|confirmed` para esa fecha+slot, ordenado por `capacity ASC, id ASC`, LIMIT 1
- [x] **3.5 TDD**: `ReservationServiceImplTest.listAvailableSlots`
  - `returnsSlotsFilteredByPartySize`
  - `skipsSlotsWithNoAvailableTable`
  - `returnsEmptyWhenDateIsOutsideOperatingDays`
  - `issuesExactlyOneQueryRegardlessOfSlotCount` (usar `DataSourceProxy` o un `StatementCountingDataSource` de test; si se ejecutan N queries para N slots, el test falla)
- [x] **3.6**: implementar `listAvailableSlots(restaurantId, date, partySize)` con **una sola query SQL** que devuelva `(slot_start, table_id)` para **todo el día**. Patrón: el servicio computa los slots del día con `SlotCatalog` en Java (cálculo puro sobre `restaurant_hours`, sin BD) y los pasa como tabla virtual (`VALUES (?, ?), (?, ?), ...`) a una única query con `CROSS JOIN restaurant_tables` + `NOT EXISTS` sobre `reservations` y `GROUP BY slot_start` para quedarse con la mesa más chica compatible. Compatible con Postgres y HSQLDB. **Prohibido** iterar los slots en Java invocando `findAvailableTable(...)` por slot — eso es N+1 y viola [[n-plus-1-joins-java]]. La query final es una por request, no una por slot.
- [x] **3.7**: correr suite completa; verificar queries con EXPLAIN en Postgres local; confirmar que la query única usa los índices `(table_id, reservation_date, slot_start)` y `(restaurant_id, reservation_date, status)`
- [x] **3.8 Commit**: `feat(reservations): slot catalog and availability service`

**Criterio de cierre**: tests verdes; no hay joins ni filtrado en Java sobre datos de BD; `listAvailableSlots` ejecuta exactamente **una** query sin importar la cantidad de slots del día; la query usa los índices esperados.

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

- [x] **4.1 TDD**: `ReservationServiceImplTest.createReservation`
  - `autoConfirmsWhenAutomaticAndGapRatioBelow2_returnsConfirmed` (ej: 3 personas → mesa de 4, ratio 1.33)
  - `autoConfirmsWhenAutomaticAndGapRatioExactlyEqualTableMatchesPartySize` (ej: 4 personas → mesa de 4, ratio 1.0)
  - `createsAsPendingWhenAutomaticAndGapRatioIsExactly2_requiresOwnerReview` (locked: 2 personas → mesa de 4, ratio 2.0 → pending)
  - `createsAsPendingWhenAutomaticAndGapRatioAbove2` (ej: 2 personas → mesa de 6, ratio 3.0 → pending)
  - `insertsPendingForOwnerNotificationWhenGapTriggersPending` (assert: fila en `reservation_notifications(type='PENDING_FOR_OWNER')`)
  - `insertsPendingForOwnerNotificationForEveryManualPending` (asserta que en modalidad `manual` **toda** pending dispara el email al owner, no solo las por gap)
  - `assignsSmallestFittingTable`
  - `throwsSlotUnavailableWhenNoTable`
  - `throwsIllegalStateWhenRestaurantDoesNotAcceptReservations`
  - `throwsIllegalStateWhenSlotOutsideRestaurantHours`
  - `throwsSlotAlreadyPastWhenReservationIsTodayAndSlotStartIsInThePast` (hoy 19:00 en `America/Argentina/Buenos_Aires` + reserva para hoy 18:00 → `SlotAlreadyPastException`)
  - `createsReservationWhenReservationIsTodayAndSlotStartIsInTheFuture` (hoy 19:00 + reserva para hoy 20:00 → OK, no rechaza)
  - `createsReservationWhenReservationIsTomorrow_regardlessOfClockTime` (fecha futura nunca dispara `SlotAlreadyPastException`)
- [x] **4.2**: implementar `ReservationServiceImpl.createReservation(CreateReservationData data, User authenticatedUser)` en una sola transacción. Algoritmo:
  1. **Chequeo temporal** (antes de tocar BD para asignar mesa): si `data.reservationDate == LocalDate.now(ZONE_AR) && data.slotStart <= LocalTime.now(ZONE_AR)` → `throw new SlotAlreadyPastException()`. La constante `ZONE_AR = ZoneId.of("America/Argentina/Buenos_Aires")` vive en `ReservationServiceImpl` (o helper común junto al scheduler). Este chequeo complementa el `@Future` del form — el form sólo asegura `reservation_date` futuro o de hoy; este paso filtra slots del día ya vencidos.
  2. Busca la mesa libre más chica con `capacity >= people_count` vía `findAvailableTable` (Fase 3)
  3. Calcula `ratio = assignedTable.type / people_count` (aritmética entera basta porque `type` y `people_count` son enteros; usar comparación `assignedTable.type >= 2 * people_count` para evitar división)
  4. Decide status inicial:
     - Modalidad `manual` → siempre `pending`
     - Modalidad `automatic` + `ratio < 2` → `confirmed`
     - Modalidad `automatic` + `ratio >= 2` → `pending` (gap grande)
  5. Inserta la reserva; inserta la fila correspondiente en `reservation_notifications`:
     - `CONFIRMED` si quedó `confirmed`
     - `PENDING_FOR_OWNER` si quedó `pending` (sea por `manual` o por gap)
  6. Dispara el email `@Async` correspondiente después del insert en `reservation_notifications` (idempotencia: si el insert pincha por UNIQUE, no se envía).
  El servicio **no** loguea el motivo del pending; el motivo lo infiere la UI desde la modalidad del restaurante y la capacidad de la mesa asignada vs `people_count`.
- [x] **4.3 TDD**: `CreateReservationFormValidationTest` para:
  - partySize fuera de rango (< 1 o > 12) → inválido
  - fecha en el pasado → inválido
  - slot con formato incorrecto → inválido
  - comment > límite → inválido
- [x] **4.4**: implementar `CreateReservationForm` con JSR380 (`@NotNull`, `@NotBlank`, `@Min(1) @Max(12)`, `@Size(max=500)` en comment). **Nota (enmienda 2026-04-17)**: `reservationDate` usa `@NotBlank String` con validación class-level en zona AR en lugar de `@Future LocalDate`, porque `@Future` opera en UTC y no distingue slots del día actual ya vencidos en `America/Argentina/Buenos_Aires`. El validator class-level (`ValidReservationDate`) parsea la fecha y la compara con `LocalDate.now(ZONE_AR)`. El chequeo de slot-ya-pasado lo realiza el servicio (`SlotAlreadyPastException`).
- [x] **4.5 MVC test**: `ReservationControllerMvcTest`
  - `post_reservation_anonymous_redirectsToLogin`
  - `post_reservation_authenticated_createsAndRedirectsToProfile`
  - `post_reservation_toRestaurantWithReservationsDisabled_returns400OrForbidden`
  - `post_reservation_unavailableSlot_returnsFormWithError`
  - `post_reservation_todayWithSlotStartAlreadyPast_returns400_rendersFormWithInlineError` (mensaje i18n `reservation.error.slotAlreadyPast`; el form conserva los demás valores cargados por el usuario)
- [x] **4.6**: implementar `ReservationController`:
  - **sin** `@PreAuthorize`; la autenticación se exige en `WebAuthConfig` con `.antMatchers(HttpMethod.POST, "/restaurantes/*/reservas").authenticated()`
  - el user logueado llega vía `@ModelAttribute` definido en un `@ControllerAdvice` global (no se invoca `SecurityContextHolder` dentro del método)
  - arma `CreateReservationData` y llama a `reservationService.createReservation(data)` una sola vez
  - no catchea excepciones: `SlotUnavailableException` la resuelve `ErrorHandlingAdvice` (400 + form redibujado con error)
- [x] **4.7**: editar `restaurant.jsp`:
  - si `accepts_reservations = true`: render del form (date picker, cantidad de personas, dropdown de slots disponibles cargado server-side en primer render y refrescable vía JS simple)
  - si `accepts_reservations = false`: render del canal alternativo según `reservation_disabled_mode` (string i18n + `<c:out>` sobre link/teléfono)
  - si `accepts_reservations = null`: no render de nada relacionado a reservas
- [x] **4.8**: i18n para labels, errores y mensajes de éxito
- [x] **4.9 Commit**: `feat(reservations): create reservation flow from restaurant page`

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

- [x] **5.1 TDD**: `ReservationServiceImplTest.cancelAsUser`
  - `cancelsPendingReservation_setsStatusCancelled` (asume pre-autorizado por Security)
  - `cancelsConfirmedReservation_setsStatusCancelled`
  - `throwsIllegalStateWhenReservationIsCompleted`
  - `throwsIllegalStateWhenReservationIsAlreadyCancelled`
  - `insertsCancelledNotificationRow` (valida idempotencia de email por estado de BD, no con `verify()`)
- [x] **5.2**: implementar `cancelAsUser(reservationId)` con transición de estado. **No** chequea ownership (lo resuelve `AccessHelper.canCancelReservation` vía Security). Inserta fila en `reservation_notifications(type='CANCELLED')` dentro de la misma transacción; `@Async` dispara el mail leyendo esa fila.
- [x] **5.3 TDD**: `AccessHelperTest.canCancelReservation`
  - `returnsTrueWhenAuthenticatedUserOwnsReservation`
  - `returnsTrueWhenUserIsAdmin` (bypass explícito aunque no sea el dueño de la reserva)
  - `returnsFalseWhenUserDoesNotOwnReservationAndIsNotAdmin`
  - `returnsFalseWhenReservationNotFound`
- [x] **5.3b TDD**: `AccessHelperTest.canViewUserReservations`
  - `returnsTrueWhenAuthenticatedUserSlugMatches`
  - `returnsTrueWhenUserIsAdmin` (cualquier slug)
  - `returnsFalseWhenSlugBelongsToOtherUserAndIsNotAdmin`
  - `returnsFalseWhenUserSlugDoesNotExist`
- [x] **5.4 TDD**: `ReservationServiceImplTest.listUserReservations_paginated` (el servicio recibe el `userId` del target — quien figura en el slug — ya resuelto por el controller; no consulta `SecurityContextHolder`)
  - `sortRecent_returnsByDateDescSlotDesc` (más recientes primero; default)
  - `sortOldest_returnsByDateAscSlotAsc` (más antiguas primero)
  - `paginationPreservesSortOrderAcrossPages`
- [x] **5.5**: implementar `listUserReservations(userId, ReservationSort, PageRequest)` usando el patrón de `Page<T>` existente; la query lleva `ORDER BY` dinámico según `ReservationSort` + `LIMIT/OFFSET`, nunca `subList` ni re-orden en Java. El controller obtiene el `userId` a partir del `{slug}` de la URL, **no** del usuario autenticado (para que ADMIN pueda ver reservas ajenas). El `sort` llega como query param opcional (`?sort=recent|oldest`), parseado por `ReservationSort.fromQueryParam` con fallback a `DATE_DESC`.
  - **JOIN explícito en SQL**: la query `SELECT` incluye `JOIN restaurants r ON r.id = reservations.restaurant_id` y trae `r.name` / `r.slug` / `r.accepts_reservations` junto con las columnas de `reservations`. El `RowMapper` construye una `Reservation` enriquecida (o un view-model `UserReservationRow`) con `restaurantName`/`restaurantSlug` ya resueltos. **Prohibido** devolver `List<Reservation>` con sólo ids y luego iterar en Java resolviendo `restaurantDao.findById(id)` por fila — eso es N+1 y viola [[n-plus-1-joins-java]]. Columnas `SELECT` explícitas (sin `SELECT *`).
- [x] **5.6 MVC test**: `UserReservationsControllerMvcTest`
  - `get_page_anonymous_redirectsToLogin`
  - `get_page_ownSlug_authenticated_showsReservations`
  - `get_page_otherUserSlug_asRegularUser_returns403` (el perfil existe → 403, **no** 404)
  - `get_page_otherUserSlug_asAdmin_showsReservations` (bypass admin resuelto por `canViewUserReservations`)
  - `get_page_unknownSlug_returns404` (el perfil no existe → 404)
  - `post_cancel_valid_updatesState`
  - `post_cancel_notOwner_asRegularUser_returns403`
  - `post_cancel_notOwner_asAdmin_updatesState` (bypass admin resuelto por `canCancelReservation`)
- [x] **5.7**: extender `WebAuthConfig` con reglas declarativas (SpEL contra `AccessHelper`, sin `@PreAuthorize`):
  - `.antMatchers(HttpMethod.GET, "/perfil/*/reservas").access("@AccessHelper.canViewUserReservations(authentication, #userSlug)")`
  - `.antMatchers(HttpMethod.POST, "/perfil/*/reservas/*/cancelar").access("@AccessHelper.canCancelReservation(authentication, #reservationId)")`
  - Usar `@EnableWebSecurity` con expression handler que soporte path variables. Los nombres de variable de path (`userSlug`, `reservationId`) deben coincidir con los `@PathVariable` del controller.
- [x] **5.8**: implementar `UserReservationsController` o extender `ProfileController` con GET y POST cancel (sin `@PreAuthorize`, sin ifs de ownership). El GET resuelve `userId` a partir del `{slug}` usando `UserService`; si el slug no existe, lanza `UserNotFoundException` (→ 404 vía advice). El POST cancel solo invoca `reservationService.cancelAsUser(reservationId)`; ownership la bloquea Security antes de entrar al método.
- [x] **5.9**: crear `profile/reservations.jsp` con tabla paginada usando tag `<paw:paging>`. Campos: restaurante (link con `<c:url>`), fecha, slot, personas, estado (`<spring:message>`). Botón Cancelar envuelto en `<sec:authorize access="@AccessHelper.canCancelReservation(authentication, #reservation.id)">` + CSRF token; todos los campos de texto con `<c:out>`. **Zero state**: si la lista está vacía, renderizar mensaje i18n (`profile.reservations.empty.title` + `profile.reservations.empty.body`) con CTA `<c:url value="/explorar"/>` para descubrir restaurantes (elogio [[buenas-practicas]]: zero states amigables).
- [x] **5.10 Commit**: `feat(reservations): user reservation listing and cancellation`

**Criterio de cierre**: usuario regular ve solo sus reservas y cancela las suyas; `ROLE_ADMIN` puede ver y cancelar reservas de cualquier slug por bypass explícito en `AccessHelper`; acceso no autorizado devuelve 403 (no 404); slug inexistente devuelve 404; paginación usa tag existente; ownership y admin bypass resueltos íntegramente por `AccessHelper` desde `WebAuthConfig`.

---

## Fase 6 — Gestión de reservas por el restaurant admin

**Objetivo**: `/mis-restaurantes/{restaurant_id}/reservas` con confirmar y cancelar.

**Archivos**:
- Crear `webapp/.../controller/OwnerReservationController.java`
- Crear `webapp/src/main/webapp/WEB-INF/views/owner/reservations.jsp`
- Extender `ReservationServiceImpl` con `confirmAsOwner`, `cancelAsOwner`
- Modificar `webapp/.../config/WebAuthConfig.java`

**Tasks**:

- [x] **6.1 TDD**: `ReservationServiceImplTest.confirmAsOwner`
  - `confirmsPending_setsStatusConfirmedAndInsertsConfirmationNotification`
  - `throwsWhenReservationIsNotPending`
- [x] **6.2 TDD**: `ReservationServiceImplTest.cancelAsOwner`
  - `cancelsPendingOrConfirmed_insertsCancelledNotification`
- [x] **6.3 TDD**: `AccessHelperTest.canManageRestaurantReservations`
  - `returnsTrueWhenOwnerOwnsRestaurant`
  - `returnsTrueWhenUserIsAdmin`
  - `returnsFalseWhenOwnerDoesNotOwnRestaurant`
- [x] **6.4**: implementar los 2 métodos en el servicio **sin** chequeos de ownership (la expression de Security los bloquea antes). Validaciones de estado sí (ej: no confirmar algo que ya está cancelado). Inserts en `reservation_notifications` antes de disparar mails `@Async`.
- [x] **6.3b TDD**: `ReservationServiceImplTest.listReassignableTables(reservationId)` (feature v2-Q3)
  - `returnsOnlyTablesWithCapacityAtLeastPartySize` (regla lockeada: `capacity >= people_count`. 3 comensales jamás caen en mesa de 1 o 2; 3 comensales sí pueden caer en mesa de 3, 4, 5, ...)
  - `excludesTablesSmallerThanPartySize` — precondición `@Sql`: reserva para 3 comensales en restaurante con mesas de 2, 3, 4, 6. Assert: la lista sólo contiene mesas de 3, 4 y 6; mesa de 2 **no** aparece
  - `includesTablesSmallerThanOriginallyAssigned_whenStillFitsPartySize` — precondición `@Sql`: reserva para 3 comensales con mesa de 6 asignada por el algoritmo (gap grande). Assert: la lista incluye mesas de 3, 4 y 6; el owner puede reassignar **hacia abajo** (mesa de 3 o 4) mientras se mantenga `capacity >= people_count`. La regla spec §5.2 "de menos a más" se interpreta como prosa, no restricción de dirección
  - `excludesTablesAlreadyOccupiedBySameSlot` (no listar mesas ocupadas por otra reserva `pending|confirmed` en ese `reservation_date`+`slot_start`)
  - `includesCurrentlyAssignedTable` (la mesa actual de la reserva siempre aparece como opción: permite "no cambiar")
  - `ordersTablesByCapacityAscIdAsc`
- [x] **6.3d TDD**: `ReservationServiceImplTest.listReservationsByRestaurant_respectsSort`
  - `sortRecent_returnsByDateDescSlotDesc` (más recientes primero; default)
  - `sortOldest_returnsByDateAscSlotAsc` (más antiguas primero)
  - `paginationPreservesSortOrderAcrossPages` (página 2 continúa donde terminó página 1 con el mismo orden)
  - `rowMapperResolvesUserNameAndTableTypeViaJoinInSql_notViaExtraQueriesInJava` — assert: la invocación dispara **una** query SQL (usar `StatementCountingDataSource` o similar) y el `Reservation`/view-model retornado trae `userName`, `userSlug`, `assignedTableType` y `assignedTableNumber` ya poblados
  - la query debe llevar el `ORDER BY` en SQL, nunca re-ordenar en Java sobre la lista paginada
  - **JOIN explícito en SQL**: la query incluye `JOIN users u ON u.id = reservations.user_id JOIN restaurant_tables t ON t.id = reservations.table_id` y trae `u.display_name` / `u.slug` / `t.table_number` / `t.capacity` en el mismo `SELECT`. El `RowMapper` construye la fila completa; **prohibido** volver a consultar `userDao`/`restaurantTableDao` por reserva desde Java ([[n-plus-1-joins-java]]). Columnas `SELECT` explícitas (sin `SELECT *`).
- [x] **6.3e TDD**: `ReservationServiceImplTest.findPageContainingReservation`
  - `returnsPageIndexContainingReservation_forDefaultSort` (ej: 25 reservas, pageSize 10, target en posición 17 en orden `DATE_DESC` → devuelve página 1 con índice base 0, es decir la segunda página)
  - `returnsPageIndexContainingReservation_forOldestSort` (mismo cómputo pero con orden inverso → página distinta para la misma reserva)
  - `returnsZeroWhenReservationDoesNotExist`
  - `returnsZeroWhenReservationBelongsToOtherRestaurant`
  - implementación: el servicio hace **una** query SQL que cuenta cuántas filas del listado ordenado preceden a `reservationId` y divide por `pageSize` (`COUNT(*) FROM reservations WHERE restaurant_id=? AND (orden < orden_target)`, comparando `reservation_date, slot_start` según el sort). Prohibido traer la lista completa y buscar el índice en Java
- [x] **6.3c TDD**: `ReservationServiceImplTest.confirmAsOwnerWithReassignment`
  - `confirmsWithoutChangingTable_whenTableIdNull_keepsOriginalAssignment` (reassignación es opcional)
  - `confirmsAndReassignsToRequestedTable_whenTableIdProvided`
  - `confirmsAndReassignsToSmallerTable_whenStillFitsPartySize` (mesa original de 6 para 3 comensales → owner reassigna a mesa de 3; válido porque `3 >= 3`)
  - `throwsTableNotAvailableForSlot_whenChosenTableIsOccupied`
  - `throwsTableNotAvailableForSlot_whenChosenTableBelongsToOtherRestaurant`
  - `throwsTableNotAvailableForSlot_whenChosenTableCapacityBelowPartySize` (3 comensales + mesa de 2 → rechazo, aun si la mesa está libre)
  - `reassignmentIsAtomic_onSameTransactionAsConfirmation` (un fallo de reassignación deja status `pending` sin side-effects)
  - `reChecksTableAvailabilityBeforeUpdate_concurrencyGuard` — precondición `@Sql`: reserva A `pending` en mesa 1; reserva B `confirmed` en mesa 2 (misma slot). Acción: owner intenta reassignar A a mesa 2 (que el re-check debe detectar ocupada dentro del mismo `@Transactional`). Assert: lanza `TableNotAvailableForSlotException`; A permanece `pending` en mesa 1
- [x] **6.5 MVC test**: `OwnerReservationControllerMvcTest`
  - `get_page_ownerOfRestaurant_showsReservations`
  - `get_page_otherOwner_returns403`
  - `get_page_asAdmin_showsReservations` (bypass admin para cualquier restaurante)
  - `get_page_sortRecent_returnsDescOrder` (default; `?sort=recent` o sin param)
  - `get_page_sortOldest_returnsAscOrder` (`?sort=oldest`)
  - `get_page_sortUnknownValue_fallsBackToRecent` (`?sort=whatever` se trata como default; no 400)
  - `get_page_withFocusOnPage2_redirectsToPage2` (`?focus={reservationId}` donde esa reserva cae en la segunda página bajo el sort actual → 302 a `?page=2&sort=...&focus=...` o equivalente, resaltando la fila)
  - `get_page_withFocusOnCurrentPage_rendersHighlightedRow` (si ya estamos en la página correcta, no redirige, solo marca la fila)
  - `get_page_withFocusForeignReservation_ignoresParam` (reserva existe pero pertenece a otro restaurante → página 1 sin redirect, sin revelar 403/404 — el param se silencia)
  - `get_page_withFocusNonExistentReservation_ignoresParam` (id inventado → página 1 sin redirect)
  - `get_page_paginationLinks_preserveSortAndFocus` (los links de paginación construidos server-side llevan `sort` y NO llevan `focus` residual para evitar re-redirects en cada click)
  - `get_page_ownerOfMultipleRestaurants_showsSelectorWithAllOwned` (el dropdown lista solo restaurantes del owner autenticado; si hay uno solo, el selector aún se muestra pero sin opciones alternativas — UI libre de decidir si ocultarlo en ese caso)
  - `get_page_ownerOfSingleRestaurant_selectorStillRenders`
  - `get_page_asAdmin_selectorShowsAllRestaurantsOwnedByAdmin` (el bypass admin **no** expande el selector a todos los restaurantes del sistema; el selector siempre refleja los restaurantes administrados por el usuario autenticado — si el ADMIN no es owner de ninguno, el selector queda vacío pero la página del restaurante solicitado se muestra igual)
  - `post_confirm_noTableId_keepsOriginalTable_updatesState`
  - `post_confirm_withTableId_reassignsAndUpdatesState`
  - `post_confirm_withTableIdOccupied_returns409_keepsPending` (conflict, no 400 — la mesa existe pero colisiona)
  - `post_confirm_otherOwner_returns403`
  - `post_confirm_asAdmin_updatesState`
  - `post_cancel_ownerOfRestaurant_updatesState` (botón con label "Rechazar" si estado previo era `pending`, "Cancelar" si era `confirmed`; ambos llegan al mismo endpoint)
  - `post_cancel_asAdmin_updatesState`
  - `post_cancel_afterAction_redirectsToSamePageWithSortAndFilters` (tras confirmar/cancelar el redirect preserva `page` y `sort` originales para que el owner no pierda su contexto de navegación)
  - transiciones de estado + paginación
- [x] **6.6**: implementar `OwnerReservationController` con endpoints `GET /mis-restaurantes/{id}/reservas`, `POST /mis-restaurantes/{id}/reservas/{reservationId}/confirm` (recibe `tableId` opcional como form param), `POST .../cancel`. Sin `@PreAuthorize`, sin ifs de ownership; el controller solo orquesta form → DTO → servicio. El servicio expone `confirmAsOwner(reservationId, Optional<Long> newTableId)`: si presente, reassigna antes de cambiar status a `confirmed`; si ausente, mantiene la mesa original.
  - El `GET` recibe query params opcionales `page` (default 0), `sort` (default `recent` vía `ReservationSort.fromQueryParam`) y `focus` (`Long` opcional).
  - Flujo de resolución de `focus`: si presente, el controller llama `reservationService.findPageContainingReservation(restaurantId, focusId, pageSize, sort)`. Si devuelve un índice ≠ página actual, hace `redirect:` a la misma URL con `page` corregido (y `sort` preservado; el `focus` se mantiene **solo** en el redirect inicial para que la JSP pueda resaltar la fila en esa nueva carga). Si devuelve 0 y la reserva no existe/no pertenece, el param se silencia (no log, no error al usuario) y se renderiza `page` tal como venía.
  - Los links de paginación que la JSP recibe en el model **no** propagan `focus` para que tras el primer redirect el parámetro no siga re-disparando re-cálculos en cada click.
  - Tras `confirm` o `cancel`, el `POST` hace redirect a `GET` preservando `page` y `sort` (pero no `focus`), para que el owner regrese a su contexto exacto.
  - El controller carga el model con `restaurants = restaurantService.getRestaurantsByOwner(authenticatedUserId)` (o el nombre vigente en el contract) para el selector; ADMIN recibe sus propios restaurantes owned (ver decisión lockeada — no se expanden los de otros owners).
- [x] **6.7**: crear `owner/reservations.jsp`. Campos: usuario, fecha, slot, personas, mesa asignada (número + `type`), estado, comentario (`<c:out>`).
  - **Botones de acción por estado**: render condicional según `reservation.status` usando `<c:choose>` (la autorización ya fue resuelta por `WebAuthConfig`; el switch de labels es UX puro, no seguridad):
    - `pending` → dos `<form method="POST">`: uno apunta a `.../confirm` con label `<spring:message code="owner.reservations.action.confirm"/>` ("Confirmar") e incluye el `<select>` de reassignación como hidden/visible según corresponda; otro apunta a `.../cancel` con label `<spring:message code="owner.reservations.action.reject"/>` ("Rechazar").
    - `confirmed` → un `<form method="POST">` a `.../cancel` con label `<spring:message code="owner.reservations.action.cancel"/>` ("Cancelar").
    - `completed` / `cancelled` → sin botones; solo visualización.
    - Ambos endpoints cancel/reject son el mismo — el label distingue UX pero el side-effect es idéntico (reserva pasa a `cancelled`, se inserta `reservation_notifications(type='CANCELLED')`).
  - **Badge de gap**: si la reserva está `pending` y `assigned_table.type >= 2 * people_count`, mostrar aviso i18n explícito (`owner.reservations.gap.warning` con `arguments="{tableType}, {peopleCount}"` — elogio explícito del spec v2 §7.2: "al restaurante le tiene que aparecer que están asignando una mesa de 4 para solo 2").
  - **Select de reassignación**: en reservas `pending`, mostrar un `<select name="tableId">` poblado server-side con `listReassignableTables(reservationId)`; la opción seleccionada por default es la mesa actual (`selected` con `<c:if>` sobre `table.id`). El botón "Confirmar" envía ese `tableId` como form param opcional.
  - **Filtro de orden (sort)**: toggle UI con 2 opciones visibles (`<spring:message code="owner.reservations.sort.recent"/>` / `...sort.oldest"/>`), implementado como dos links `<c:url>` que reescriben el query param `sort` preservando `page=0` (cambiar el orden resetea a la primera página, para no terminar en una página inexistente del nuevo orden). La opción activa se resalta con clase CSS leída del model (`activeSort`).
  - **Selector de restaurante**: `<select>` populado con `restaurants` del model (owner del autenticado). Cada `<option value="<c:url value='/mis-restaurantes/${r.id}/reservas'/>">` con `<c:out value="${r.name}"/>`. El cambio dispara navegación vía JS mínimo (`onchange=\"location.href=this.value\"`) o se envuelve en un `<form method=\"GET\">` con `action=\"\"` y botón explícito si se prefiere no-JS. La URL destino **no** propaga `sort`/`page`/`focus` (contexto de un restaurante ≠ contexto de otro). El selector se oculta vía `<c:if test=\"${fn:length(restaurants) > 1}\">` si el owner administra uno solo — elimina ruido UI sin perder la funcionalidad.
  - **Focus highlight**: si el model trae `focusReservationId` (inyectado por el controller solo tras el redirect inicial), renderizar la `<tr>` de esa reserva con clase CSS `owner-reservations__row--focused` y ancla `<a name="reservation-{id}"></a>` para scroll automático. En el JS `DOMContentLoaded` mínimo, `document.getElementById(...).scrollIntoView()` sobre esa fila.
  - Todo texto via `<spring:message>`. Usar `<paw:paging>`; los links de paginación construidos por la JSP deben incluir `sort` como query param adicional (la tag `<paw:paging>` debe permitirlo; si no, usar una versión local que concatene los params correctos con `<c:url>`).
  - **Zero state**: si el restaurante aún no tiene reservas, mensaje i18n (`owner.reservations.empty.title` + `owner.reservations.empty.body`) sin CTA adicional. El selector de restaurante y el toggle de sort siguen visibles para que el owner pueda saltar a otro restaurante o cambiar el orden sin salir de la página.
- [x] **6.8**: proteger rutas en `WebAuthConfig`:
  - `.antMatchers("/mis-restaurantes/*/reservas/**").access("@AccessHelper.canManageRestaurantReservations(authentication, #restaurantId)")` cubre GET, confirm, cancel y no-show con la misma expression
  - `AccessHelper.canManageRestaurantReservations` devuelve `true` para el owner del restaurante **o** cualquier usuario con `ROLE_ADMIN` (bypass explícito verificado en `AccessHelperTest.returnsTrueWhenUserIsAdmin` de la 6.3)
- [x] **6.9 Commit**: `feat(reservations): owner reservation management`

**Criterio de cierre**: restaurant owner confirma y cancela sobre sus restaurantes; `ROLE_ADMIN` puede hacer lo mismo sobre cualquier restaurante por bypass en `AccessHelper`; ownership y admin bypass resueltos por `AccessHelper`; lista paginada; tests MVC en verde cubriendo casos owner, ajeno (403) y admin.

---

## Fase 7 — Transiciones temporales y schedulers

**Objetivo**: scheduler que borra pendings vencidos y completa confirmados vencidos.

**Archivos**:
- Crear `services/.../ReservationSchedulerImpl.java`
- Modificar `services/.../ServicesConfig.java` con `@EnableScheduling` y `@EnableAsync`
- Crear `services/src/test/java/.../ReservationSchedulerImplTest.java`

**Tasks**:

- [x] **7.1 TDD**: `ReservationSchedulerImplTest` — **sin `verify()`**; todo lo assert es estado: count de filas por status y filas en `reservation_notifications`.
  - `tick_deletesAllPendingWithSlotEndBeforeNow` (assert: `countRowsInTableWhere(reservations, "status='PENDING'")` = 0)
  - `tick_transitionsConfirmedWithSlotEndBeforeNow_toCompleted`
  - `tick_leavesFutureReservationsUntouched`
  - `tick_leavesCancelledUntouched`
  - `tick_leavesCompletedUntouched`
  - `tick_insertsCompletedNotificationRowExactlyOncePerReservation` (assert: una sola fila en `reservation_notifications(type='COMPLETED')` por reserva, aun corriendo `tick()` dos veces)
- [x] **7.2**: implementar `ReservationSchedulerImpl.tick()` en una transacción (o dos: una por cada query), usando `ZoneId.of("America/Argentina/Buenos_Aires")` para construir `now`
- [x] **7.3**: anotar con `@Scheduled(fixedDelay = 300000)` (5 min)
- [x] **7.4**: agregar `@EnableScheduling` y `@EnableAsync` en `ServicesConfig`, anotados con `@Profile("!test")` para que el contenedor de tests **no** arranque los jobs reales. Los tests de scheduler (`ReservationSchedulerImplTest`) instancian `ReservationSchedulerImpl` directamente e invocan `tick()` / `sendDailyReminders()` como métodos comunes — no dependen de que Spring dispare el cron. Los tests MVC/servicio que levantan contexto (`@ContextConfiguration`) deben activar perfil `test` (ya debe existir el patrón; si no, agregar `@ActiveProfiles("test")` al base test class). Motivo: evita side-effects de threads de fondo en suites que corren en paralelo y elimina timing flakiness.
- [x] **7.5**: validar en local que el scheduler arranca con Jetty y no bloquea el servlet
- [x] **7.6 Commit**: `feat(reservations): scheduler for expired pendings and completions`

**Criterio de cierre**: tests verdes; scheduler corre en dev sin errores; logs muestran transiciones.

---

## Fase 8 — Emails transaccionales

**Objetivo**: 5 emails (confirmación, cancelación, recordatorio 11:00 AM, post-completion y pending-para-owner) con idempotencia.

**Archivos**:
- Crear `services/src/main/resources/mail-templates/_layout.html` y `services/src/main/resources/mail-templates/reservation-{confirmed,cancelled,reminder,completed,pending-owner}.html` (5 templates + layout compartido, todos en `services/` para no violar [[modelo-capas]])
- Modificar `services/.../SmtpMailServiceImpl.java` (o `MailService` contract) con métodos para los 5 tipos
- Extender `ReservationSchedulerImpl` con `sendDailyReminders()` con cron 11:00 AM GMT-3

**Tasks**:

- [x] **8.1**: crear un **layout HTML común** `services/src/main/resources/mail-templates/_layout.html` con header (logo/marca), slot de body y footer (datos legales/unsuscribe opcional), más los 5 templates Thymeleaf en la misma carpeta (`reservation-confirmed`, `reservation-cancelled`, `reservation-reminder`, `reservation-completed`, `reservation-pending-owner`) que extiendan ese layout con `th:replace`/`th:fragment`. **Todos viven en `services/`, nunca en `webapp/`** — `MailService` es capa de servicio y no puede resolver classpath de `webapp` ([[modelo-capas]]). CTA renderizado como **botón estilizado** (inline CSS permitido en email; no link suelto) — elogio explícito de la cátedra (ver [[mailing]] y [[buenas-practicas]]). Textos siempre en locale del destinatario. CTAs:
  - Confirmation → `/perfil/{slug}/reservas`
  - Cancelled → `/explorar`
  - Reminder → `/perfil/{slug}/reservas`
  - Completed → `/restaurantes/{slug}?review={reservationId}`
  - Pending-owner → `/mis-restaurantes/{id}/reservas?focus={reservationId}` (redirige a la vista de gestión; el owner elige confirmar/cancelar/reassignar desde ahí — el email **no** ofrece acciones directas). Copy: "Tenés una reserva pendiente que requiere tu revisión". El query param `focus` debe resaltar/scrollear a esa reserva dentro de la lista paginada.
- [x] **8.2**: agregar al contract `MailService`: `sendReservationConfirmed`, `sendReservationCancelled`, `sendReservationReminder`, `sendReservationCompleted`, `sendReservationPendingToOwner`. Implementar en `SmtpMailServiceImpl` usando el patrón existente. El destinatario de `sendReservationPendingToOwner` es el email del owner del restaurante (no el del reservante); el Locale es el del owner.
- [x] **8.3 TDD**: tests de servicio verifican que cada transición **inserta la fila correcta** en `reservation_notifications`. **Prohibido `verify()`** sobre `MailService`. El stub de `MailService` en tests puede incrementar un contador interno y el assert es sobre ese contador o directamente sobre la tabla.
- [x] **8.4**: patrón de idempotencia — el servicio hace `INSERT INTO reservation_notifications(reservation_id, type) VALUES (?, ?)` **antes** de invocar al mail; si la UNIQUE constraint pincha (`DuplicateKeyException`), se captura silenciosamente y se **no se envía** el mail. El send al `MailService` es `@Async` y ocurre solo si el insert succeeded. Este orden elimina la doble escritura condicionada.
- [x] **8.5 TDD**: `ReservationSchedulerImplTest.sendDailyReminders` — asserts sobre count de `reservation_notifications(type='REMINDER')`. Regla lockeada: el recordatorio se envía **solo** a reservas `confirmed` (pending/cancelled/completed quedan afuera; motivo: una pending puede terminar cancelada y el recordatorio induciría al usuario a presentarse sin reserva real).
  - `sendsReminderForConfirmedReservationsOfTodayCreatedBefore11AM`
  - `skipsReservationsCreatedAfter11AM` (aunque estén `confirmed`)
  - `skipsPendingReservations` (status `pending` queda sin recordatorio, aun si fue creada antes de las 11)
  - `skipsCancelledReservations`
  - `skipsCompletedReservations`
  - `skipsReservationsForOtherDays` (futuras o pasadas)
  - `isIdempotentWhenCalledTwice` (count sigue siendo N, no 2N)
- [x] **8.6**: implementar `sendDailyReminders` con `@Scheduled(cron = "0 0 11 * * *", zone = "America/Argentina/Buenos_Aires")`. La query SQL filtra `status = 'confirmed' AND reservation_date = :today AND created_at < :todayAt11AM` y hace `LEFT JOIN reservation_notifications` con `type = 'REMINDER'` donde la fila no existe (idempotencia vía UNIQUE). **No** se apoya en un `if (status == CONFIRMED)` en Java sobre la lista completa ([[n-plus-1-joins-java]])
- [x] **8.7**: tests MVC verifican el contrato de notifications:
  - crear en modalidad `automatic` con gap chico (ratio < 2) → **inserta** `reservation_notifications(type='CONFIRMED')`
  - crear en modalidad `automatic` con gap grande (ratio >= 2) → **inserta** `reservation_notifications(type='PENDING_FOR_OWNER')` y **no** inserta `CONFIRMED`
  - crear en modalidad `manual` → **inserta** `reservation_notifications(type='PENDING_FOR_OWNER')` inmediatamente
  - owner confirma pending → **inserta** `reservation_notifications(type='CONFIRMED')`
  Asserts con `JdbcTestUtils.countRowsInTableWhere`.
- [x] **8.8 Commit**: `feat(reservations): transactional emails and daily reminder`

**Criterio de cierre**: en dev se reciben los 4 emails; tests verdes; `reservation_notifications` evita duplicados.

---

## Fase 9 — Reviews y verificación por reserva

**Objetivo**: permitir reviews libres (como las legacy) pero con sistema de verificación ("tick") si se vinculan a una reserva completada propia.

**Política de reviews**:
- Las nuevas reviews admiten `reservation_id` opcional (nullable).
- Si `reservation_id` es provisto: debe pertenecer al usuario, estar en estado `COMPLETED` y no tener otra review asociada (UNIQUE constraint).
- Si se cumplen las condiciones, la review se marca como **"Verificada"** en la UI (un tic o badge al lado del nombre).
- Si no se provee `reservation_id`, la review se crea normalmente pero sin marca de verificación.
- Las reviews legacy (sin `reservation_id`) se mantienen como están, sin marca.

**Tasks**:

- [x] **9.1**: asegurar UNIQUE constraint sobre `reviews.reservation_id` en `schema-*.sql`. Permitir múltiples NULLs (comportamiento default de Postgres/HSQLDB).
- [x] **9.2 TDD**: `ReviewServiceImplTest.createReview`
  - `createsReviewWithoutReservation_isUnverified`
  - `createsReviewWithValidCompletedReservation_isVerified`
  - `throwsWhenReservationBelongsToOtherUser`
- [x] **9.7**: ajustar email post-completion para linkear a `/restaurantes/{slug}?review={reservationId}`; la vista precarga el contexto desde el query param para que la review nazca verificada.
- [x] **9.8 Commit**: `feat(reservations): flexible reviews with optional reservation verification`

**Criterio de cierre**: las reviews pueden crearse con o sin reserva; la marca de "Verificado" aparece solo cuando hay una reserva de respaldo; unicidad por reserva garantizada por BD; reviews legacy siguen visibles.

---

## Fase 10 — Hardening transversal

**Objetivo**: i18n, seguridad, logging, ControllerAdvice, README de credenciales.

**Archivos**:
- Modificar `webapp/src/main/resources/i18n/messages*.properties`
- Modificar `webapp/.../exceptions/` y `ErrorHandlingAdvice.java`
- Modificar `README.md`
- Verificar `webapp/.../auth/` y `config/WebAuthConfig.java`

**Tasks**:

- [x] **10.1**: auditoría i18n completa: toda key nueva debe existir en ES y EN, **incluyendo labels, errores, `<title>` de cada JSP nuevo y `alt` de cualquier imagen**. Escribir un test `I18nSymmetryTest` que cargue ambos `Properties` y falle si una clave está solo en uno; escanear los JSPs nuevos y verificar que no haya strings hardcodeados visibles al usuario. **Pluralización**: cualquier texto con contador (ej: "Tenés {0} reservas pendientes", "{0} restaurantes encontrados") usa sintaxis `ChoiceFormat` de `MessageFormat` (`{0,choice,0#no tenés reservas|1#tenés 1 reserva|1<tenés {0} reservas}`) o `{0,plural,...}` vía ICU si el stack lo soporta. **Prohibido** concatenar la parte numérica con el resto de la frase en Java o JSP. Ambas locales (ES, EN) deben cubrir las tres ramas `0/1/>1` con su gramática propia.
- [x] **10.2**: mapear todas las excepciones nuevas a HTTP en `ErrorHandlingAdvice`:
  - `SlotUnavailableException` → 400 + redibujar form
  - `SlotAlreadyPastException` → 400 + redibujar form con error in-line (clave i18n `reservation.error.slotAlreadyPast`); se considera error del input del usuario aunque lo dispare el servicio, no el form validator
  - `LayoutFrozenException` → 409 + mensaje i18n
  - `TableNotAvailableForSlotException` → 409 + redibujar la lista del owner con mensaje de conflicto (mesa elegida ocupada o incompatible)
  - `ReservationNotFoundException` → 404
  - `AccessDeniedException` de Spring Security → 403 (recurso existe sin permisos)
  - `org.springframework.dao.DataIntegrityViolationException` → 400 con mensaje i18n genérico (FK o UNIQUE violados por input del cliente, ej: `restaurant_id` inexistente o double-submit que viola `UNIQUE(reservation_id, notification_type)`). Explícito para **no** dejar que suba como 500 (corrección canónica de [[manejo-excepciones]] y `resumen-correcciones` §Queries).
- [x] **10.3**: grep manual: ninguna vista muestra `${...}` sin `<c:out>` para valores dinámicos (comentarios de reserva, link externo, teléfono, nombre de restaurante). Ninguna URL hardcodeada sin `<c:url>`.
- [x] **10.4**: logs con SLF4J en todos los servicios (creación, cancelación, confirmación, tick del scheduler). Patrón: `private static final Logger LOGGER = LoggerFactory.getLogger(ClaseActual.class);` y `LOGGER.info("reserva {} cancelada por user {}", id, userId)`. **Cero** `System.out`, `printStackTrace` ni concatenación con `+` dentro de logs.
- [x] **10.5**: verificar que `WebAuthConfig` resuelve toda la autorización declarativa:
  - `/restaurantes/*/reservas` POST → `authenticated()`
  - `/perfil/*/reservas` GET → `.access("@AccessHelper.canViewUserReservations(authentication, #userSlug)")` (dueño del slug **o** `ROLE_ADMIN`)
  - `/perfil/*/reservas/*/cancelar` POST → `.access("@AccessHelper.canCancelReservation(authentication, #reservationId)")` (dueño de la reserva **o** `ROLE_ADMIN`)
  - `/mis-restaurantes/*/reservas/**` → `.access("@AccessHelper.canManageRestaurantReservations(authentication, #restaurantId)")` (owner del restaurante **o** `ROLE_ADMIN`)
  - Ningún controller tiene `@PreAuthorize` ni chequeos `if (user.isOwner(...))`; el admin bypass vive exclusivamente dentro de los métodos de `AccessHelper`, nunca replicado en controllers/services
  - **Orden de antMatchers**: las rutas nuevas más específicas deben declararse **antes** de cualquier catch-all `/**.authenticated()` existente. Cualquier regla por rol o expression que quede **después** de un catch-all autenticado pierde efecto — corrección canónica de [[spring-security]] (`/** antes de rutas de ORGANIZER`) y `resumen-correcciones` §HTTP. Extender `SecurityMvcTest` con casos que prueban que un usuario autenticado pero sin ownership recibe **403** y que un `ROLE_ADMIN` pasa todas las expressions.
- [x] **10.6**: actualizar `README.md` con credenciales de dev para admin, owner y reviewer (si no están ya)
- [x] **10.7**: correr `mvn clean package` y verificar que el WAR arranca sin `database.properties` / `mail.properties` / `security.properties` faltantes
- [x] **10.8 Commit**: `chore(reservations): i18n, error handling, security, readme`

**Criterio de cierre**: suite entera verde; WAR empaquetable; `SecurityMvcTest` pasa con rutas nuevas y confirma 403 (no bypass) para usuarios autenticados sin ownership; auditoría de i18n sin huérfanos (labels + titles + alts); cero `verify()` en tests nuevos; cero `@PreAuthorize` en controllers nuevos; `DataIntegrityViolationException` mapeado a 400 en el advice; orden de antMatchers verificado con caso de prueba de bypass.

---

## Checklist de requerimientos core TP1

Cruzando con [[criterios-evaluacion]]:

- [x] Roles: reviewer vs restaurant_owner vs admin con ownership aplicado (fases 5, 6, 10)
- [x] i18n completo en UI, errores y emails (fases 2, 4, 5, 6, 8, 10)
- [x] Logging SLF4J sin System.out (fase 10 + transversal)
- [x] Mailing funcional: confirmación, cancelación, recordatorio, post-completion (fase 8)
- [x] Queries no triviales resueltas en BD: disponibilidad con joins y filtros (fase 3)
- [x] Spring Security como freno principal, sin if-else locales (fases 2-6, 10)
- [x] XSS/LIKE: `<c:out>` sobre comentarios y link externo; sin LIKE abierto sobre inputs (fases 2, 4, 10)
- [x] Testing unitario: HSQLDB + `@Rollback` + Mockito sobrio (todas las fases)
- [x] README con credenciales operativas (fase 10)

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
