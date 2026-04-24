# PAW Wiki Compliance Remediation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** corregir las brechas reales de cumplimiento contra `PAW-Wiki` sin tocar decisiones ya validadas por el usuario ni cambiar el stack Java/Spring MVC/JSP/JDBC.

**Architecture:** la remediacion se ejecuta en fases independientes y reversibles: primero cambios funcionales con tests claros, despues deuda estructural y CSS. Cada fase debe cerrar con la suite mas chica que pruebe el cambio y, cuando toque varias capas, con `mvn clean test`.

**Tech Stack:** Java 21, Spring Web MVC clasico, Spring Security, JSP/JSTL, JDBC, Maven multi-modulo, PostgreSQL/HSQLDB tests.

---

## Decisiones de alcance

### Confirmadas por el usuario y fuera de remediacion

- `webapp/src/main/resources/{database,mail,security}.properties` reales dentro del WAR: aceptado porque el servidor de la catedra lo requiere para que la webapp funcione con la configuracion actual.
- `/PAW-Wiki` ignorado por `.gitignore`: aceptado porque el wiki se trackea en otro git privado del equipo.

### Hallazgos que si se corrigen

1. `GET /lang/{language}` persiste locale en DB.
2. N+1 en mesas reasignables de reservas owner.
3. Cambio de contrasena autenticado no pide contrasena actual.
4. Referencia documental rota en `LegacySchemaGuard`.
5. Estilos inline masivos en JSP/tag files.
6. Contexto Spring web cargado en root context, sujeto a fase de migracion controlada o excepcion documentada si el server de catedra lo exige.

## Orden recomendado

| Fase | Riesgo | Motivo del orden |
| --- | --- | --- |
| 0 - Baseline | Bajo | Congela estado actual y evita discutir fallas preexistentes. |
| 1 - Locale GET sin DB write | Bajo | Cambio acotado, regla wiki clara, tests directos. |
| 2 - Batch de reassignable tables | Medio | Toca contrato persistence/service/web, pero cierra N+1 real. |
| 3 - Cambio de contrasena con password actual | Medio | Toca auth, service, form, JSP, i18n y tests. |
| 4 - LegacySchemaGuard doc link | Bajo | Barato y sin impacto funcional. |
| 5 - Inline styles | Alto por volumen | Mucho JSP/CSS; hacerlo por clusters verificables. |
| 6 - Spring context split | Alto | Puede impactar seguridad por `MvcRequestMatcher`; hacerlo al final. |
| 7 - Verificacion final | Medio | Maven + WAR + Jetty smoke manual. |

## Criterio de cierre final

- `mvn clean test` pasa.
- `mvn clean package` pasa.
- `jar tf webapp/target/webapp.war` sigue incluyendo los `.properties` reales por decision del usuario.
- `rg -n "System\\.out|printStackTrace" models service-contracts persistence-contracts persistence services webapp/src/main` sin resultados.
- `rg -n "style=|<style" webapp/src/main/webapp/WEB-INF --glob '*.jsp' --glob '*.jspf' --glob '*.tag'` sin resultados, salvo que el equipo documente una allowlist puntual y justificada.
- Smoke manual en Jetty cubre login reviewer, perfil, cambio de idioma, cambio de contrasena, owner reservas, confirmacion/reasignacion, admin basico.

---

## File Map

### Locale GET

- Modify: `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/LanguageController.java`
- Modify: `webapp/src/test/java/ar/edu/itba/paw/webapp/LanguageControllerMvcTest.java`

### Reservas owner N+1

- Modify: `persistence-contracts/src/main/java/ar/edu/itba/paw/persistence/RestaurantTableDao.java`
- Modify: `persistence/src/main/java/ar/edu/itba/paw/persistence/RestaurantTableJdbcDao.java`
- Modify: `service-contracts/src/main/java/ar/edu/itba/paw/services/ReservationService.java`
- Modify: `services/src/main/java/ar/edu/itba/paw/services/ReservationServiceImpl.java`
- Modify: `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/OwnerReservationPageSupport.java`
- Modify tests: `persistence/src/test/java/ar/edu/itba/paw/persistence/RestaurantTableJdbcDaoTest.java`
- Modify tests: `services/src/test/java/ar/edu/itba/paw/services/ReservationServiceImplTest.java`
- Modify tests: `services/src/test/java/ar/edu/itba/paw/services/ReservationSchedulerImplTest.java`
- Modify tests: `webapp/src/test/java/ar/edu/itba/paw/webapp/OwnerReservationControllerMvcTest.java`
- Modify tests: `webapp/src/test/java/ar/edu/itba/paw/webapp/SecurityMvcTest.java`

### Cambio de contrasena

- Create: `service-contracts/src/main/java/ar/edu/itba/paw/services/ChangePasswordData.java`
- Create: `service-contracts/src/main/java/ar/edu/itba/paw/services/InvalidCurrentPasswordException.java`
- Modify: `service-contracts/src/main/java/ar/edu/itba/paw/services/UserService.java`
- Modify: `services/src/main/java/ar/edu/itba/paw/services/UserServiceImpl.java`
- Create: `webapp/src/main/java/ar/edu/itba/paw/webapp/form/ChangePasswordForm.java`
- Modify: `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/ProfileController.java`
- Create: `webapp/src/main/webapp/WEB-INF/views/profile/change-password.jsp`
- Modify: `webapp/src/main/webapp/WEB-INF/views/profile/profile.jsp`
- Modify: `webapp/src/main/resources/i18n/messages.properties`
- Modify: `webapp/src/main/resources/i18n/messages_es.properties`
- Modify: `webapp/src/main/resources/i18n/messages_en.properties`
- Modify tests: `services/src/test/java/ar/edu/itba/paw/services/UserServiceImplTest.java`
- Create tests: `webapp/src/test/java/ar/edu/itba/paw/webapp/ProfileChangePasswordMvcTest.java`
- Modify tests: `webapp/src/test/java/ar/edu/itba/paw/webapp/FormValidationTest.java`
- Modify tests: `webapp/src/test/java/ar/edu/itba/paw/webapp/ViewTemplateSecurityTest.java`

### LegacySchemaGuard

- Modify: `persistence/src/main/java/ar/edu/itba/paw/persistence/LegacySchemaGuard.java`
- Modify: `persistence/src/test/java/ar/edu/itba/paw/persistence/LegacySchemaGuardTest.java`

### JSP/CSS cleanup

- Modify: `DESIGN.md` only if a reusable CSS convention needs documentation.
- Modify: `webapp/src/main/webapp/css/forkd.css`
- Modify: high-count JSP/tag files, in this order:
  - `webapp/src/main/webapp/WEB-INF/views/restaurant.jsp` (79 inline styles)
  - `webapp/src/main/webapp/WEB-INF/views/admin/pending-restaurants.jsp` (40)
  - `webapp/src/main/webapp/WEB-INF/views/owner/dashboard.jsp` (36)
  - `webapp/src/main/webapp/WEB-INF/views/profile/reservations.jsp` (31)
  - `webapp/src/main/webapp/WEB-INF/views/profile/profile.jsp` (31)
  - `webapp/src/main/webapp/WEB-INF/views/admin/unassigned-restaurants.jsp` (24)
  - `webapp/src/main/webapp/WEB-INF/views/profile/saved-restaurants.jsp` (20)
  - `webapp/src/main/webapp/WEB-INF/views/home.jsp` (18)
  - `webapp/src/main/webapp/WEB-INF/views/error/*.jsp`
  - `webapp/src/main/webapp/WEB-INF/tags/*.tag`
  - auth/admin low-count pages
- Modify tests: `webapp/src/test/java/ar/edu/itba/paw/webapp/ViewTemplateSecurityTest.java`

### Spring context split

- Modify: `webapp/src/main/webapp/WEB-INF/web.xml`
- Possibly modify: `webapp/src/test/java/ar/edu/itba/paw/webapp/config/WebAuthConfigPropertyTest.java`
- Possibly modify/add a minimal startup smoke test only if the repo already has a pattern; otherwise use Jetty smoke.

---

## Task 0: Baseline y preparar rama

**Files:**
- Read only at this stage.

- [ ] **Step 1: Confirmar que el working tree no tiene cambios ajenos relevantes**

Run:

```bash
git status --short --branch
```

Expected:

```text
## dev...origin/dev [ahead 1]
```

If there are unrelated changes, do not revert them. Note them in the implementation log and work around them.

- [ ] **Step 2: Ejecutar baseline completo**

Run:

```bash
mvn clean test
mvn clean package
```

Expected:

```text
BUILD SUCCESS
```

- [ ] **Step 3: Registrar baseline de los hallazgos**

Run:

```bash
rg -n "updatePreferredLocale" webapp/src/main/java/ar/edu/itba/paw/webapp/controller/LanguageController.java webapp/src/test/java/ar/edu/itba/paw/webapp/LanguageControllerMvcTest.java
rg -n "listReassignableTables|findReassignableForReservation" webapp/src/main services/src/main service-contracts/src/main persistence-contracts/src/main persistence/src/main
rg -n "style=|<style" webapp/src/main/webapp/WEB-INF --glob '*.jsp' --glob '*.jspf' --glob '*.tag' | wc -l
```

Expected current baseline:

```text
LanguageController and LanguageControllerMvcTest still mention updatePreferredLocale
OwnerReservationPageSupport still calls listReassignableTables per reservation
inline style count is non-zero
```

---

## Task 1: Hacer que `GET /lang/{language}` no escriba en DB

**Files:**
- Modify: `webapp/src/test/java/ar/edu/itba/paw/webapp/LanguageControllerMvcTest.java`
- Modify: `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/LanguageController.java`

- [ ] **Step 1: Cambiar tests para capturar la regla correcta**

Replace the authenticated persistence tests with no-write assertions:

```java
@Test
void changeLanguage_authenticatedUserStoresEnglishLocaleInSessionWithoutPersistingIt() throws Exception {
    mockMvc.perform(get("/lang/en")
                    .with(authenticationFor(7L, "reviewer", "Reviewer", "ROLE_REVIEWER"))
                    .param("redirect", "/explore"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/explore"))
            .andExpect(request().sessionAttribute(
                    SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME,
                    Locale.ENGLISH));

    Mockito.verify(userService, Mockito.never()).updatePreferredLocale(anyLong(), anyString());
}

@Test
void changeLanguage_rejectsUnsafeRedirectAndDoesNotPersistDefaultLocaleForAuthenticatedUser() throws Exception {
    mockMvc.perform(get("/lang/es")
                    .with(authenticationFor(7L, "reviewer", "Reviewer", "ROLE_REVIEWER"))
                    .param("redirect", "//evil.com"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"))
            .andExpect(request().sessionAttribute(
                    SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME,
                    DEFAULT_LOCALE));

    Mockito.verify(userService, Mockito.never()).updatePreferredLocale(anyLong(), anyString());
}
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```bash
mvn -pl webapp -am test -Dtest=LanguageControllerMvcTest
```

Expected:

```text
FAILURE: Wanted but not invoked / NeverWantedButInvoked for updatePreferredLocale
```

- [ ] **Step 3: Remove DB write from controller**

Replace `LanguageController` with this shape:

```java
package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.webapp.util.SafeRedirectPathValidator;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

@Controller
public class LanguageController {

    private static final Locale DEFAULT_LOCALE = Locale.of("es", "AR");

    @RequestMapping(value = "/lang/{language}", method = RequestMethod.GET)
    public String changeLanguage(@PathVariable("language") final String language,
                                 final String redirect,
                                 final HttpServletRequest request,
                                 final HttpServletResponse response) {
        final Locale locale = "en".equalsIgnoreCase(language)
                ? Locale.ENGLISH
                : DEFAULT_LOCALE;

        final var localeResolver = RequestContextUtils.getLocaleResolver(request);
        if (localeResolver != null) {
            localeResolver.setLocale(request, response, locale);
        }

        if (SafeRedirectPathValidator.isSafeRedirectPath(redirect)) {
            return "redirect:" + redirect;
        }

        return "redirect:/";
    }
}
```

- [ ] **Step 4: Verify webapp tests for language**

Run:

```bash
mvn -pl webapp -am test -Dtest=LanguageControllerMvcTest
```

Expected:

```text
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

- [ ] **Step 5: Verify no DB write remains in language flow**

Run:

```bash
rg -n "updatePreferredLocale" webapp/src/main/java/ar/edu/itba/paw/webapp/controller/LanguageController.java webapp/src/test/java/ar/edu/itba/paw/webapp/LanguageControllerMvcTest.java
```

Expected:

```text
No output
```

Rollback:

```bash
git restore webapp/src/main/java/ar/edu/itba/paw/webapp/controller/LanguageController.java webapp/src/test/java/ar/edu/itba/paw/webapp/LanguageControllerMvcTest.java
```

---

## Task 2: Eliminar N+1 en mesas reasignables de reservas owner

**Files:**
- Modify: `persistence-contracts/src/main/java/ar/edu/itba/paw/persistence/RestaurantTableDao.java`
- Modify: `persistence/src/main/java/ar/edu/itba/paw/persistence/RestaurantTableJdbcDao.java`
- Modify: `service-contracts/src/main/java/ar/edu/itba/paw/services/ReservationService.java`
- Modify: `services/src/main/java/ar/edu/itba/paw/services/ReservationServiceImpl.java`
- Modify: `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/OwnerReservationPageSupport.java`
- Modify related tests/fakes.

- [ ] **Step 1: Add DAO contract for batch lookup**

In `RestaurantTableDao`, add:

```java
import java.util.Map;
```

and:

```java
Map<Long, List<RestaurantTable>> findReassignableForReservations(List<Long> reservationIds);
```

- [ ] **Step 2: Run compile to verify implementors fail**

Run:

```bash
mvn -pl persistence -am test -DskipTests
```

Expected:

```text
COMPILATION ERROR: RestaurantTableJdbcDao is not abstract and does not override findReassignableForReservations
```

- [ ] **Step 3: Implement batch DAO**

In `RestaurantTableJdbcDao`, add imports:

```java
import java.util.LinkedHashMap;
import java.util.stream.Collectors;
```

Implement:

```java
@Override
public List<RestaurantTable> findReassignableForReservation(final long reservationId) {
    return findReassignableForReservations(List.of(reservationId))
            .getOrDefault(reservationId, List.of());
}

@Override
public Map<Long, List<RestaurantTable>> findReassignableForReservations(final List<Long> reservationIds) {
    if (reservationIds == null || reservationIds.isEmpty()) {
        return Map.of();
    }

    final String placeholders = reservationIds.stream()
            .map(id -> "?")
            .collect(Collectors.joining(", "));
    final Map<Long, List<RestaurantTable>> result = new LinkedHashMap<>();

    jdbcTemplate.query(
            "SELECT res.id AS reservation_id, rt.id, rt.restaurant_id, rt.table_number, rt.capacity, rt.created_at " +
                    "FROM reservations res " +
                    "JOIN restaurant_tables rt ON rt.restaurant_id = res.restaurant_id " +
                    "WHERE res.id IN (" + placeholders + ") " +
                    "AND rt.capacity >= res.party_size " +
                    "AND (rt.id = res.table_id OR NOT EXISTS (" +
                    "    SELECT 1 " +
                    "    FROM reservations overlap " +
                    "    WHERE overlap.table_id = rt.id " +
                    "      AND overlap.id <> res.id " +
                    "      AND overlap.reservation_date = res.reservation_date " +
                    "      AND overlap.status IN ('PENDING', 'CONFIRMED') " +
                    "      AND overlap.slot_start < res.slot_end " +
                    "      AND overlap.slot_end > res.slot_start" +
                    ")) " +
                    "ORDER BY res.id ASC, rt.capacity ASC, rt.id ASC",
            rs -> {
                final long reservationId = rs.getLong("reservation_id");
                result.computeIfAbsent(reservationId, ignored -> new ArrayList<>())
                        .add(ROW_MAPPER.mapRow(rs, 0));
            },
            reservationIds.toArray());

    for (final Long reservationId : reservationIds) {
        result.putIfAbsent(reservationId, List.of());
    }
    return result;
}
```

- [ ] **Step 4: Add persistence regression test**

In `RestaurantTableJdbcDaoTest`, add a test that creates or reuses two pending reservations for the same restaurant and asserts:

```java
final Map<Long, List<RestaurantTable>> result = restaurantTableDao.findReassignableForReservations(
        List.of(firstReservationId, secondReservationId));

assertTrue(result.containsKey(firstReservationId));
assertTrue(result.containsKey(secondReservationId));
assertEquals(
        restaurantTableDao.findReassignableForReservation(firstReservationId),
        result.get(firstReservationId));
assertEquals(
        restaurantTableDao.findReassignableForReservation(secondReservationId),
        result.get(secondReservationId));
```

Use existing reservation fixtures in that test file. Do not introduce `SELECT *`.

- [ ] **Step 5: Add service batch contract**

In `ReservationService`, add:

```java
import java.util.Map;
```

and:

```java
Map<Long, List<RestaurantTable>> listReassignableTablesByReservation(List<Long> reservationIds);
```

In `ReservationServiceImpl`, add:

```java
@Transactional(readOnly = true)
@Override
public Map<Long, List<RestaurantTable>> listReassignableTablesByReservation(final List<Long> reservationIds) {
    return restaurantTableDao.findReassignableForReservations(reservationIds);
}
```

- [ ] **Step 6: Update test fakes**

Any fake `RestaurantTableDao` in service tests must implement:

```java
@Override
public Map<Long, List<RestaurantTable>> findReassignableForReservations(final List<Long> reservationIds) {
    return reservationIds.stream()
            .collect(java.util.stream.Collectors.toMap(
                    id -> id,
                    id -> reassignableTables,
                    (left, right) -> left,
                    java.util.LinkedHashMap::new));
}
```

Any fake `ReservationService` in scheduler or web tests must implement:

```java
@Override
public Map<Long, List<RestaurantTable>> listReassignableTablesByReservation(final List<Long> reservationIds) {
    return java.util.Collections.emptyMap();
}
```

- [ ] **Step 7: Change owner page support to one service call**

Replace `resolveReassignableTables` in `OwnerReservationPageSupport`:

```java
private Map<Long, List<RestaurantTable>> resolveReassignableTables(final List<Reservation> reservations) {
    final List<Long> pendingReservationIds = reservations.stream()
            .filter(reservation -> reservation.getStatus() == ReservationStatus.PENDING)
            .map(Reservation::getId)
            .toList();
    if (pendingReservationIds.isEmpty()) {
        return Map.of();
    }
    return reservationService.listReassignableTablesByReservation(pendingReservationIds);
}
```

- [ ] **Step 8: Update MVC tests**

In `OwnerReservationControllerMvcTest` and `SecurityMvcTest`, replace stubs like:

```java
when(reservationService.listReassignableTables(10L)).thenReturn(List.of(table));
```

with:

```java
when(reservationService.listReassignableTablesByReservation(List.of(10L)))
        .thenReturn(Map.of(10L, List.of(table)));
```

If multiple pending reservations are present, use `List.of(id1, id2)` in the same order as the page content.

- [ ] **Step 9: Verify targeted suites**

Run:

```bash
mvn -pl persistence -am test -Dtest=RestaurantTableJdbcDaoTest
mvn -pl services -am test -Dtest=ReservationServiceImplTest
mvn -pl webapp -am test -Dtest=OwnerReservationControllerMvcTest,SecurityMvcTest
```

Expected:

```text
BUILD SUCCESS
```

Rollback:

```bash
git restore persistence-contracts/src/main/java/ar/edu/itba/paw/persistence/RestaurantTableDao.java \
  persistence/src/main/java/ar/edu/itba/paw/persistence/RestaurantTableJdbcDao.java \
  service-contracts/src/main/java/ar/edu/itba/paw/services/ReservationService.java \
  services/src/main/java/ar/edu/itba/paw/services/ReservationServiceImpl.java \
  webapp/src/main/java/ar/edu/itba/paw/webapp/controller/OwnerReservationPageSupport.java
```

---

## Task 3: Implementar cambio de contrasena autenticado con contrasena actual

**Files:**
- Create/modify service contracts, service impl, profile controller, form, JSP, i18n and tests listed in File Map.

- [ ] **Step 1: Create service DTO**

Create `service-contracts/src/main/java/ar/edu/itba/paw/services/ChangePasswordData.java`:

```java
package ar.edu.itba.paw.services;

public class ChangePasswordData {
    private final String currentPassword;
    private final String newPassword;

    public ChangePasswordData(final String currentPassword, final String newPassword) {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }
}
```

- [ ] **Step 2: Create service exception**

Create `service-contracts/src/main/java/ar/edu/itba/paw/services/InvalidCurrentPasswordException.java`:

```java
package ar.edu.itba.paw.services;

public class InvalidCurrentPasswordException extends RuntimeException {
}
```

- [ ] **Step 3: Extend UserService**

In `UserService`, add:

```java
User changePassword(long userId, ChangePasswordData data);
```

- [ ] **Step 4: Write service tests first**

In `UserServiceImplTest`, add:

```java
@Test
void changePassword_updatesPasswordWhenCurrentPasswordMatches() {
    final User user = verifiedReviewer(5L, "reviewer", "Reviewer", "encoded-current");
    when(userDao.findById(5L)).thenReturn(Optional.of(user), Optional.of(user));
    when(passwordEncoder.matches("current-secret", "encoded-current")).thenReturn(true);
    when(passwordEncoder.encode("new-secret-123")).thenReturn("encoded-new");

    final User result = userService.changePassword(5L,
            new ChangePasswordData("current-secret", "new-secret-123"));

    verify(userDao).updatePassword(5L, "encoded-new");
    assertEquals(5L, result.getId());
}

@Test
void changePassword_throwsWhenCurrentPasswordDoesNotMatch() {
    final User user = verifiedReviewer(5L, "reviewer", "Reviewer", "encoded-current");
    when(userDao.findById(5L)).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("bad-secret", "encoded-current")).thenReturn(false);

    assertThrows(InvalidCurrentPasswordException.class,
            () -> userService.changePassword(5L,
                    new ChangePasswordData("bad-secret", "new-secret-123")));

    verify(userDao, never()).updatePassword(anyLong(), anyString());
}
```

Use the existing helper style in the file. If there is no `verifiedReviewer(...)` helper with this signature, create a private helper matching the existing `User` constructor.

- [ ] **Step 5: Run service tests to verify fail**

Run:

```bash
mvn -pl services -am test -Dtest=UserServiceImplTest
```

Expected:

```text
Compilation failure or failing tests because changePassword is not implemented
```

- [ ] **Step 6: Implement service method**

In `UserServiceImpl`, add:

```java
@Transactional
@Override
public User changePassword(final long userId, final ChangePasswordData data) {
    final User user = userDao.findById(userId)
            .filter(User::canAuthenticate)
            .orElseThrow(InvalidCurrentPasswordException::new);

    if (!passwordEncoder.matches(data.getCurrentPassword(), user.getPasswordHash())) {
        throw new InvalidCurrentPasswordException();
    }

    userDao.updatePassword(user.getId(), passwordEncoder.encode(data.getNewPassword()));
    LOGGER.info("Password changed by authenticated userId={}", user.getId());
    return userDao.findById(user.getId()).orElseThrow(InvalidCurrentPasswordException::new);
}
```

- [ ] **Step 7: Create web form**

Create `webapp/src/main/java/ar/edu/itba/paw/webapp/form/ChangePasswordForm.java`:

```java
package ar.edu.itba.paw.webapp.form;

import ar.edu.itba.paw.webapp.util.InputMaxLengths;
import ar.edu.itba.paw.webapp.validation.PasswordsMatch;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@PasswordsMatch
public class ChangePasswordForm {

    @NotBlank(message = "{profile.password.current.required}")
    @Size(max = InputMaxLengths.PASSWORD, message = "{auth.validation.password.size}")
    private String currentPassword;

    @NotBlank(message = "{auth.validation.password.required}")
    @Size(min = 8, max = 72, message = "{auth.validation.password.size}")
    private String password;

    @NotBlank(message = "{auth.validation.passwordConfirm.required}")
    @Size(max = InputMaxLengths.PASSWORD, message = "{auth.validation.password.size}")
    private String passwordConfirm;

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(final String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public String getPasswordConfirm() {
        return passwordConfirm;
    }

    public void setPasswordConfirm(final String passwordConfirm) {
        this.passwordConfirm = passwordConfirm;
    }
}
```

- [ ] **Step 8: Add i18n keys in all three bundles**

Add equivalent keys to `messages.properties`, `messages_es.properties`, and `messages_en.properties`.

Spanish/default:

```properties
profile.password.title=Cambiar contrasena
profile.password.heading=Cambiar contrasena
profile.password.body=Ingresá tu contrasena actual y elegí una nueva.
profile.password.current.label=Contrasena actual
profile.password.new.label=Nueva contrasena
profile.password.confirm.label=Repetir nueva contrasena
profile.password.submit=Guardar contrasena
profile.password.current.required=Ingresá tu contrasena actual.
profile.password.current.invalid=La contrasena actual no es correcta.
profile.password.cancel=Volver al perfil
```

English:

```properties
profile.password.title=Change password
profile.password.heading=Change password
profile.password.body=Enter your current password and choose a new one.
profile.password.current.label=Current password
profile.password.new.label=New password
profile.password.confirm.label=Repeat new password
profile.password.submit=Save password
profile.password.current.required=Enter your current password.
profile.password.current.invalid=The current password is not correct.
profile.password.cancel=Back to profile
```

- [ ] **Step 9: Add controller GET/POST**

In `ProfileController`, import:

```java
import ar.edu.itba.paw.services.ChangePasswordData;
import ar.edu.itba.paw.services.InvalidCurrentPasswordException;
import ar.edu.itba.paw.webapp.form.ChangePasswordForm;
import org.springframework.validation.FieldError;
```

Add model attribute:

```java
@ModelAttribute("changePasswordForm")
public ChangePasswordForm changePasswordForm() {
    return new ChangePasswordForm();
}
```

Replace current POST-only token redirect with:

```java
@RequestMapping(value = "/profile/{username}/change-password", method = RequestMethod.GET)
public ModelAndView changePasswordForm(@PathVariable("username") final String username) {
    final User user = userService.findByUsername(username)
            .filter(User::isReviewer)
            .orElseThrow(() -> new ResourceNotFoundException("Reviewer profile not found"));
    return new ModelAndView("profile/change-password")
            .addObject("profileUser", user);
}

@RequestMapping(value = "/profile/{username}/change-password", method = RequestMethod.POST)
public ModelAndView changePassword(@PathVariable("username") final String username,
        @Valid @ModelAttribute("changePasswordForm") final ChangePasswordForm form,
        final BindingResult errors) {
    final User user = userService.findByUsername(username)
            .filter(User::isReviewer)
            .orElseThrow(() -> new ResourceNotFoundException("Reviewer profile not found"));

    if (errors.hasErrors()) {
        return new ModelAndView("profile/change-password")
                .addObject("profileUser", user);
    }

    try {
        final User updatedUser = userService.changePassword(user.getId(),
                new ChangePasswordData(form.getCurrentPassword(), form.getPassword()));
        refreshCurrentAuthenticationIfNeeded(updatedUser);
        return new ModelAndView("redirect:/profile/" + updatedUser.getPublicProfileKey())
                .addObject("successMessage", "profile.password.changed");
    } catch (final InvalidCurrentPasswordException ex) {
        errors.addError(new FieldError(
                "changePasswordForm",
                "currentPassword",
                form.getCurrentPassword(),
                false,
                new String[] { "profile.password.current.invalid" },
                null,
                "profile.password.current.invalid"));
        return new ModelAndView("profile/change-password")
                .addObject("profileUser", user);
    }
}
```

If flash success is required instead of query/model object, replace `.addObject(...)` with a `RedirectAttributes` parameter and `redirectAttributes.addFlashAttribute("successMessage", "profile.password.changed")`.

- [ ] **Step 10: Create JSP**

Create `WEB-INF/views/profile/change-password.jsp` using existing auth/profile form conventions. Required fields:

```jsp
<form:form modelAttribute="changePasswordForm" method="POST" action="${fn:escapeXml(changePasswordUrl)}" class="form-stack">
    <input type="hidden" name="${fn:escapeXml(_csrf.parameterName)}" value="${fn:escapeXml(_csrf.token)}" />

    <label for="currentPassword"><spring:message code="profile.password.current.label" /></label>
    <form:password path="currentPassword" id="currentPassword" cssClass="input" autocomplete="current-password" />
    <form:errors path="currentPassword" cssClass="form-error" />

    <label for="password"><spring:message code="profile.password.new.label" /></label>
    <form:password path="password" id="password" cssClass="input" autocomplete="new-password" />
    <form:errors path="password" cssClass="form-error" />

    <label for="passwordConfirm"><spring:message code="profile.password.confirm.label" /></label>
    <form:password path="passwordConfirm" id="passwordConfirm" cssClass="input" autocomplete="new-password" />
    <form:errors path="passwordConfirm" cssClass="form-error" />

    <button type="submit" class="btn btn--primary">
        <spring:message code="profile.password.submit" />
    </button>
</form:form>
```

Use `<c:url value="/profile/${profileUser.username}/change-password" var="changePasswordUrl" />` before the form.

- [ ] **Step 11: Change profile menu from POST to link**

In `profile/profile.jsp`, replace the current POST form for `profile.password.change` with a GET link:

```jsp
<a href="${fn:escapeXml(changePasswordUrl)}" class="profile-menu__item">
    <spring:message code="profile.password.change" />
</a>
```

Move the visual style to CSS in Task 5 if the class does not exist yet.

- [ ] **Step 12: Add MVC tests**

Create `ProfileChangePasswordMvcTest` with these cases:

```java
@Test
void changePasswordForm_ownerProfile_returns200() throws Exception {
    when(userService.findByUsername("reviewer")).thenReturn(Optional.of(verifiedUser(7L, "reviewer", "Reviewer")));

    mockMvc.perform(get("/profile/reviewer/change-password")
                    .with(authenticationFor(7L, "reviewer", "Reviewer", "ROLE_REVIEWER")))
            .andExpect(status().isOk())
            .andExpect(view().name("profile/change-password"))
            .andExpect(model().attributeExists("changePasswordForm"));
}

@Test
void changePassword_submitMismatchedPasswords_returnsInlineError() throws Exception {
    when(userService.findByUsername("reviewer")).thenReturn(Optional.of(verifiedUser(7L, "reviewer", "Reviewer")));

    mockMvc.perform(post("/profile/reviewer/change-password")
                    .with(authenticationFor(7L, "reviewer", "Reviewer", "ROLE_REVIEWER"))
                    .with(csrf())
                    .param("currentPassword", "current-secret")
                    .param("password", "new-secret-123")
                    .param("passwordConfirm", "other-secret-123"))
            .andExpect(status().isOk())
            .andExpect(view().name("profile/change-password"))
            .andExpect(model().attributeHasErrors("changePasswordForm"));
}

@Test
void changePassword_submitWrongCurrentPassword_returnsInlineCurrentPasswordError() throws Exception {
    when(userService.findByUsername("reviewer")).thenReturn(Optional.of(verifiedUser(7L, "reviewer", "Reviewer")));
    when(userService.changePassword(Mockito.eq(7L), Mockito.any(ChangePasswordData.class)))
            .thenThrow(new InvalidCurrentPasswordException());

    mockMvc.perform(post("/profile/reviewer/change-password")
                    .with(authenticationFor(7L, "reviewer", "Reviewer", "ROLE_REVIEWER"))
                    .with(csrf())
                    .param("currentPassword", "bad-secret")
                    .param("password", "new-secret-123")
                    .param("passwordConfirm", "new-secret-123"))
            .andExpect(status().isOk())
            .andExpect(view().name("profile/change-password"))
            .andExpect(model().attributeHasFieldErrors("changePasswordForm", "currentPassword"));
}

@Test
void changePassword_submitValidPassword_redirectsToProfile() throws Exception {
    when(userService.findByUsername("reviewer")).thenReturn(Optional.of(verifiedUser(7L, "reviewer", "Reviewer")));
    when(userService.changePassword(Mockito.eq(7L), Mockito.argThat(data ->
            data != null
                    && "current-secret".equals(data.getCurrentPassword())
                    && "new-secret-123".equals(data.getNewPassword()))))
            .thenReturn(verifiedUser(7L, "reviewer", "Reviewer"));

    mockMvc.perform(post("/profile/reviewer/change-password")
                    .with(authenticationFor(7L, "reviewer", "Reviewer", "ROLE_REVIEWER"))
                    .with(csrf())
                    .param("currentPassword", "current-secret")
                    .param("password", "new-secret-123")
                    .param("passwordConfirm", "new-secret-123"))
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/profile/reviewer"));
}
```

- [ ] **Step 13: Verify targeted suites**

Run:

```bash
mvn -pl services -am test -Dtest=UserServiceImplTest
mvn -pl webapp -am test -Dtest=ProfileChangePasswordMvcTest,PasswordResetMvcTest,FormValidationTest,ViewTemplateSecurityTest
```

Expected:

```text
BUILD SUCCESS
```

Rollback: revert files listed in this task. Do not change password reset token flow while rolling back.

---

## Task 4: Corregir referencia rota de `LegacySchemaGuard`

**Files:**
- Modify: `persistence/src/main/java/ar/edu/itba/paw/persistence/LegacySchemaGuard.java`
- Modify: `persistence/src/test/java/ar/edu/itba/paw/persistence/LegacySchemaGuardTest.java`

- [ ] **Step 1: Replace dead docs path**

Replace:

```java
"See docs/2026-04-02-reviewer-reset_v1.md for details."
```

with:

```java
"See CLAUDE.md section 'Base de datos y bootstrap' or README.md deployment notes for details."
```

- [ ] **Step 2: Update test assertion**

Replace:

```java
assertTrue(exception.getMessage().contains("docs/2026-04-02-reviewer-reset_v1.md"));
```

with:

```java
assertTrue(exception.getMessage().contains("CLAUDE.md"));
assertTrue(exception.getMessage().contains("Base de datos y bootstrap"));
```

- [ ] **Step 3: Verify persistence test**

Run:

```bash
mvn -pl persistence -am test -Dtest=LegacySchemaGuardTest
```

Expected:

```text
Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Rollback:

```bash
git restore persistence/src/main/java/ar/edu/itba/paw/persistence/LegacySchemaGuard.java \
  persistence/src/test/java/ar/edu/itba/paw/persistence/LegacySchemaGuardTest.java
```

---

## Task 5: Eliminar estilos inline de JSP/tag files por clusters

**Files:**
- Modify: `webapp/src/main/webapp/css/forkd.css`
- Modify: JSP/tag files listed in File Map.
- Modify: `webapp/src/test/java/ar/edu/itba/paw/webapp/ViewTemplateSecurityTest.java`

### Strategy

Do not try to rewrite all 463 inline styles in one commit. Use one commit per cluster. After each cluster, run `ViewTemplateSecurityTest` and the relevant MVC test.

- [ ] **Step 1: Add a guard test for target files**

In `ViewTemplateSecurityTest`, add helper:

```java
private static void assertNoInlineStyle(final String relativePath) throws IOException {
    final String content = readWebTemplate(relativePath);
    assertFalse(content.contains("style="), relativePath + " still contains inline style attributes");
    assertFalse(content.contains("<style"), relativePath + " still contains page-local style blocks");
}
```

Add tests incrementally per cluster. For the first cluster:

```java
@Test
void sharedHeaderTags_doNotUseInlineStyles() throws IOException {
    assertNoInlineStyle("WEB-INF/tags/header.tag");
    assertNoInlineStyle("WEB-INF/tags/adminHeader.tag");
}
```

Expected first run before cleanup:

```text
FAILURE: header.tag still contains inline style attributes
```

- [ ] **Step 2: Move shared menu/header styles to CSS**

Add to `forkd.css`:

```css
.profile-options {
    position: relative;
}

.profile-options__button {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 3rem;
    height: 3rem;
    min-width: unset;
    padding: 0.5rem;
    border-radius: var(--radius-full);
}

.profile-options__menu {
    display: none;
    position: absolute;
    left: 0;
    top: 100%;
    z-index: 10;
    min-width: max-content;
    margin-top: var(--space-2);
    padding: var(--space-2);
    border: 1px solid var(--outline-variant);
    border-radius: var(--radius-md);
    background: var(--surface-container);
    box-shadow: var(--shadow-md);
}

.profile-menu__item {
    display: block;
    width: 100%;
    padding: 0.5rem 1rem;
    border: 0;
    background: none;
    color: var(--on-surface);
    cursor: pointer;
    font: inherit;
    text-align: left;
    text-decoration: none;
}

.profile-menu__item--danger {
    color: var(--error);
}
```

Use the same pattern for header/adminHeader inline logout form styles.

- [ ] **Step 3: Cluster order and commands**

Run this after each cluster:

```bash
mvn -pl webapp -am test -Dtest=ViewTemplateSecurityTest
```

Cluster-specific verification:

```bash
mvn -pl webapp -am test -Dtest=ProfileChangePasswordMvcTest,ProfileReservationsMvcTest,SavedRestaurantMvcTest
mvn -pl webapp -am test -Dtest=OwnerDashboardControllerMvcTest,OwnerReservationControllerMvcTest
mvn -pl webapp -am test -Dtest=SecurityMvcTest
```

- [ ] **Step 4: Remove tests that pin inline style**

Replace assertions in `ViewTemplateSecurityTest` that expect exact inline hero styles:

```java
assertTrue(restaurant.contains("style=\"position:relative;height:870px;width:100%;overflow:hidden;\""));
```

with class-based assertions:

```java
assertTrue(restaurant.contains("class=\"detail-hero\""));
assertTrue(restaurant.contains("class=\"detail-hero__bg\""));
assertTrue(restaurant.contains("class=\"detail-hero__image\""));
```

Then update `restaurant.jsp` and `forkd.css` so those classes carry the removed styles.

- [ ] **Step 5: Final no-inline-style gate**

When all clusters are done, add the broad guard:

```java
@Test
void webInfTemplates_doNotUseInlineStyles() throws IOException {
    final Path webInfRoot = Path.of("src/main/webapp/WEB-INF");
    try (Stream<Path> paths = Files.walk(webInfRoot)) {
        final List<Path> templates = paths
                .filter(Files::isRegularFile)
                .filter(path -> {
                    final String fileName = path.getFileName().toString();
                    return fileName.endsWith(".jsp") || fileName.endsWith(".jspf") || fileName.endsWith(".tag");
                })
                .toList();

        for (final Path template : templates) {
            final String content = Files.readString(template, StandardCharsets.UTF_8);
            assertFalse(content.contains("style="), webInfRoot.relativize(template) + " contains inline style");
            assertFalse(content.contains("<style"), webInfRoot.relativize(template) + " contains style block");
        }
    }
}
```

Run:

```bash
rg -n "style=|<style" webapp/src/main/webapp/WEB-INF --glob '*.jsp' --glob '*.jspf' --glob '*.tag'
mvn -pl webapp -am test -Dtest=ViewTemplateSecurityTest
```

Expected:

```text
rg command has no output
BUILD SUCCESS
```

Rollback: revert one cluster commit at a time. Do not revert unrelated CSS fixes from earlier clusters.

---

## Task 6: Resolver el root/web Spring context mismatch

**Files:**
- Modify: `webapp/src/main/webapp/WEB-INF/web.xml`
- Validate: `webapp/src/main/java/ar/edu/itba/paw/webapp/config/WebAuthConfig.java`
- Validate: `webapp/src/main/java/ar/edu/itba/paw/webapp/config/WebConfig.java`

### Recommended default

Try a real root/servlet split only after Tasks 1-5 are green. This phase is isolated because Spring Security currently uses path variables from request matchers; moving `WebAuthConfig` without the right web context can break ownership checks.

- [ ] **Step 1: Document baseline before editing**

Run:

```bash
mvn -pl webapp -am test -Dtest=SecurityMvcTest,OwnerReservationControllerMvcTest,ProfileReservationsMvcTest
```

Expected:

```text
BUILD SUCCESS
```

- [ ] **Step 2: Move MVC/security config to DispatcherServlet context**

In `web.xml`, root context should keep:

```xml
<param-value>
  ar.edu.itba.paw.persistence.PersistenceConfig
  ar.edu.itba.paw.services.ServicesConfig
  ar.edu.itba.paw.services.BackgroundExecutionConfig
</param-value>
```

DispatcherServlet context should load:

```xml
<param-value>
  ar.edu.itba.paw.webapp.config.WebConfig
  ar.edu.itba.paw.webapp.config.WebAuthConfig
</param-value>
```

Because `springSecurityFilterChain` would then live in the DispatcherServlet context, add this init-param to the `springSecurityFilterChain` `DelegatingFilterProxy`:

```xml
<init-param>
  <param-name>contextAttribute</param-name>
  <param-value>org.springframework.web.servlet.FrameworkServlet.CONTEXT.dispatcher</param-value>
</init-param>
```

- [ ] **Step 3: Verify security matcher variables still work**

Run:

```bash
mvn -pl webapp -am test -Dtest=SecurityMvcTest,OwnerReservationControllerMvcTest,ProfileReservationsMvcTest,SavedRestaurantMvcTest
```

Expected:

```text
BUILD SUCCESS
```

If tests fail because `context.getVariables()` is empty or security chain cannot be found, revert this phase and document the current root-context setup as an accepted server constraint.

- [ ] **Step 4: Jetty smoke**

Run:

```bash
mvn -pl webapp -am jetty:run
```

Manual smoke:

1. Open `http://localhost:8080/`.
2. Login as reviewer.
3. Open profile.
4. Login as owner.
5. Open `/my-restaurants`.
6. Open owner reservations.
7. Login as admin.
8. Open `/admin/restaurants`.

Expected:

```text
No startup error
No 403/404 regression on owned resources
No missing springSecurityFilterChain error
```

Rollback:

```bash
git restore webapp/src/main/webapp/WEB-INF/web.xml
```

If rollback is chosen, add a short note to `PAW-Wiki/docs/wiki/2026-04-24_auditoria-implementacion-contra-wiki_v1.md`:

```markdown
Actualizacion: el root-context setup se conserva como excepcion aceptada por compatibilidad con el servidor/testing actual; no queda como remediacion pendiente.
```

---

## Task 7: Verificacion final y cierre documental

**Files:**
- Modify: `PAW-Wiki/docs/wiki/2026-04-24_auditoria-implementacion-contra-wiki_v1.md`
- Modify: `PAW-Wiki/docs/log.md`
- Modify: `PAW-Wiki/docs/tree.txt` only if new docs are added.

- [ ] **Step 1: Run full verification**

Run:

```bash
mvn clean test
mvn clean package
jar tf webapp/target/webapp.war | rg 'WEB-INF/classes/(database|mail|security)\\.properties$'
rg -n "System\\.out|printStackTrace" models service-contracts persistence-contracts persistence services webapp/src/main
rg -n "style=|<style" webapp/src/main/webapp/WEB-INF --glob '*.jsp' --glob '*.jspf' --glob '*.tag'
```

Expected:

```text
mvn clean test: BUILD SUCCESS
mvn clean package: BUILD SUCCESS
properties are present in WAR by accepted server constraint
System.out/printStackTrace scan has no output
style scan has no output, unless an explicit allowlist was documented
```

- [ ] **Step 2: Run focused smoke if Jetty is available**

Run:

```bash
mvn -pl webapp -am jetty:run
```

Manual QA checklist:

- [ ] Reviewer login works.
- [ ] Reviewer changes language; session locale changes and DB preferred locale does not change.
- [ ] Reviewer opens change-password form.
- [ ] Wrong current password returns inline error.
- [ ] Valid current password changes password and redirects to profile.
- [ ] Owner opens reservations page with pending reservations.
- [ ] Owner sees reassignable tables and page loads without per-row service calls.
- [ ] Owner confirm/reassign still works.
- [ ] Admin restaurant list opens.

- [ ] **Step 3: Update audit document**

In `2026-04-24_auditoria-implementacion-contra-wiki_v1.md`, add a cierre section:

```markdown
## Cierre de remediacion

- `GET /lang/{language}` ya no persiste locale en DB.
- Reservas owner ya no resuelven mesas reasignables con N+1.
- Cambio de contrasena autenticado pide contrasena actual.
- `LegacySchemaGuard` apunta a documentacion existente.
- Estilos inline de JSP/tag files removidos o allowlist justificada.
- Properties dentro del WAR quedan como decision aceptada por requisito del servidor.
- `PAW-Wiki/` ignorado queda como decision aceptada por git privado del equipo.
```

- [ ] **Step 4: Commit sequence recommended**

Use one commit per completed phase:

```bash
git add webapp/src/main/java/ar/edu/itba/paw/webapp/controller/LanguageController.java \
  webapp/src/test/java/ar/edu/itba/paw/webapp/LanguageControllerMvcTest.java
git commit -m "fix: avoid persisting locale from language GET"

git add persistence-contracts persistence service-contracts services webapp/src/main/java/ar/edu/itba/paw/webapp/controller/OwnerReservationPageSupport.java webapp/src/test
git commit -m "fix: batch owner reservation reassignable tables"

git add service-contracts services webapp/src/main/java webapp/src/main/webapp/WEB-INF/views/profile webapp/src/main/resources/i18n webapp/src/test
git commit -m "fix: require current password for profile password changes"

git add persistence/src/main/java/ar/edu/itba/paw/persistence/LegacySchemaGuard.java persistence/src/test/java/ar/edu/itba/paw/persistence/LegacySchemaGuardTest.java
git commit -m "docs: point legacy schema guard to current reset notes"

git add webapp/src/main/webapp/css/forkd.css webapp/src/main/webapp/WEB-INF webapp/src/test/java/ar/edu/itba/paw/webapp/ViewTemplateSecurityTest.java
git commit -m "style: move JSP inline styles to shared CSS"
```

Do not commit `PAW-Wiki/` in the app repo unless the user explicitly asks, because it is intentionally tracked elsewhere.

---

## Self-Review

- Spec coverage:
  - Locale GET side effect: Task 1.
  - Owner reservations N+1: Task 2.
  - Password change current password: Task 3.
  - Legacy docs path: Task 4.
  - Inline styles: Task 5.
  - Spring context split: Task 6.
  - User-accepted non-issues: out-of-scope section and Task 7 closeout.
- Placeholder scan:
  - No unresolved placeholder markers.
  - No deferred implementation markers.
  - Each implementation task names concrete files, commands, and expected outputs.
- Risk control:
  - Properties-in-WAR untouched.
  - `PAW-Wiki/` ignore untouched.
  - Each phase has a rollback path.
  - High-risk context split is last and explicitly reversible.
