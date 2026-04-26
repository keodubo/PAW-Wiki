# Forkd Social Area Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** agregar modalidad social para reviewers: seguir/dejar de seguir usuarios, ver seguidores/seguidos en perfiles reviewer y consultar un feed paginado de reviews recientes de usuarios seguidos.

**Architecture:** la feature se implementa de forma aditiva: una nueva tabla `user_follows`, un contrato `SocialService`, un DAO JDBC dedicado, controladores finos en `webapp`, JSP/JSTL con i18n completa y mail Thymeleaf para notificar nuevos seguidores. No se cambia el modelo de roles ni se toca `users` salvo consultas.

**Tech Stack:** Java 21, Spring Web MVC clasico, Spring Security, JSP/JSTL, JDBC, PostgreSQL, HSQLDB para tests, Maven multi-modulo, Thymeleaf para mails.

---

## Decision Recomendada

Implementar v1 con las rutas canonicas reales del repo:

- `GET /profile/{profileKey}/followers`
- `GET /profile/{profileKey}/following`
- `POST /profile/{profileKey}/follow`
- `POST /profile/{profileKey}/unfollow`
- `GET /feed`

El borrador original menciona `/perfil/{slug}/followers` y `/perfil/{slug}/following`, pero el checkout actual usa `/profile/{publicProfileKey}` como ruta publica canonica. No agregar alias `/perfil/**` en v1: hacerlo obliga a duplicar reglas de seguridad, links y tests. Si producto exige URLs en castellano, tratarlo como una fase separada de routing.

## Alcance

### Incluido

- Reviewer autenticado puede seguir a otro reviewer.
- Reviewer autenticado puede dejar de seguir a otro reviewer.
- Owner y admin no pueden seguir usuarios ni acceder a `/feed`.
- Un reviewer no puede seguirse a si mismo.
- Seguir dos veces al mismo usuario es idempotente y no duplica mails.
- Dejar de seguir a alguien no seguido es idempotente.
- Perfil reviewer muestra:
  - contador de reviews ya existente,
  - contador de seguidores,
  - contador de seguidos,
  - boton `Seguir` o `Dejar de seguir` cuando corresponde.
- Paginas publicas de seguidores y seguidos para perfiles reviewer.
- `/feed` privado para pure reviewers con reviews de usuarios seguidos.
- Feed con filtros por barrio, rating minimo y restaurante.
- Feed con orden, paginacion y preservacion de estado GET.
- Mail al usuario seguido cuando recibe un nuevo seguidor.

### Fuera de alcance v1

- Notificaciones dentro de la app.
- Bloqueos, perfiles privados o aprobacion manual de seguidores.
- Alias `/perfil/**`.
- Feed infinito o AJAX.
- Actividad distinta de reviews, por ejemplo listas creadas o restaurantes guardados.
- Recomendaciones de usuarios a seguir.

## Supuestos Operativos

- "Usuario" en esta feature significa reviewer puro: `ROLE_REVIEWER` y no owner.
- Los perfiles reviewer siguen siendo visibles publicamente como hoy.
- Las paginas de seguidores/seguidos tambien son publicas, igual que el perfil.
- `/feed` requiere login reviewer porque depende de la relacion social del usuario actual.
- El mail de nuevo follower usa el locale preferido del usuario seguido.
- Si el mail falla, el follow queda persistido y se loguea el error sin romper la accion.
- El feed muestra reviews de restaurantes publicos/aprobados; no debe exponer restaurantes no aprobados.
- El feed no tiene ventana temporal dura: "reciente" se define por orden default `created_at DESC`.

## Modelo de Datos

Agregar una tabla relacional aditiva:

```sql
CREATE TABLE IF NOT EXISTS user_follows (
    follower_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    followed_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (follower_id, followed_id),
    CHECK (follower_id <> followed_id)
);

CREATE INDEX IF NOT EXISTS user_follows_followed_created_at_idx
    ON user_follows (followed_id, created_at DESC, follower_id);

CREATE INDEX IF NOT EXISTS user_follows_follower_created_at_idx
    ON user_follows (follower_id, created_at DESC, followed_id);
```

Por que esta forma:

- `PRIMARY KEY (follower_id, followed_id)` evita duplicados.
- `ON DELETE CASCADE` limpia relaciones si se borra una cuenta.
- `CHECK (follower_id <> followed_id)` protege contra self-follow aunque falle la capa service.
- Indice por `followed_id` acelera seguidores.
- Indice por `follower_id` acelera seguidos y feed.

## Contrato de Rutas

| Ruta | Metodo | Auth | Responsable | Resultado |
| --- | --- | --- | --- | --- |
| `/profile/{profileKey}` | GET | Publica | `ProfileController` | Perfil con contadores y boton social si aplica |
| `/profile/{profileKey}/followers` | GET | Publica | `SocialController` | Pagina paginada de seguidores |
| `/profile/{profileKey}/following` | GET | Publica | `SocialController` | Pagina paginada de seguidos |
| `/profile/{profileKey}/follow` | POST | Reviewer puro | `SocialController` | Sigue al target y redirige |
| `/profile/{profileKey}/unfollow` | POST | Reviewer puro | `SocialController` | Deja de seguir y redirige |
| `/feed` | GET | Reviewer puro | `FeedController` | Feed paginado y filtrable |

Parametros de `/feed`:

| Parametro | Tipo | Default | Uso |
| --- | --- | --- | --- |
| `page` | int | `1` | pagina actual |
| `pageSize` | string | resolver standard | opciones existentes de `PageSizeResolver` |
| `sort` | string | `DATE_DESC` | `DATE_DESC`, `DATE_ASC`, `RATING_DESC`, `RATING_ASC` |
| `neighborhood` | string | vacio | barrio canonico de `RestaurantCatalog` |
| `rating` | decimal | vacio | rating minimo, valores `0.5` a `5.0` |
| `restaurant` | string | vacio | busqueda parcial por nombre de restaurante |

## File Map

### Models

- Create: `models/src/main/java/ar/edu/itba/paw/models/SocialFeedFilters.java`

### Persistence Contracts

- Create: `persistence-contracts/src/main/java/ar/edu/itba/paw/persistence/SocialDao.java`

### Persistence

- Create: `persistence/src/main/java/ar/edu/itba/paw/persistence/SocialJdbcDao.java`
- Modify: `persistence/src/main/resources/schema-base.sql`
- Modify: `persistence/src/main/resources/schema-postgres.sql`
- Modify: `persistence/src/test/resources/schema-hsqldb.sql`
- Create tests: `persistence/src/test/java/ar/edu/itba/paw/persistence/SocialJdbcDaoTest.java`

### Service Contracts

- Create: `service-contracts/src/main/java/ar/edu/itba/paw/services/SocialService.java`
- Create: `service-contracts/src/main/java/ar/edu/itba/paw/services/InvalidSocialFollowException.java`
- Modify: `service-contracts/src/main/java/ar/edu/itba/paw/services/MailService.java`

### Services

- Create: `services/src/main/java/ar/edu/itba/paw/services/SocialServiceImpl.java`
- Modify: `services/src/main/java/ar/edu/itba/paw/services/SmtpMailServiceImpl.java`
- Create: `services/src/main/resources/mail-templates/new-follower.html`
- Modify tests: `services/src/test/java/ar/edu/itba/paw/services/SmtpMailServiceImplContextTest.java`
- Create tests: `services/src/test/java/ar/edu/itba/paw/services/SocialServiceImplTest.java`
- Create tests: `services/src/test/java/ar/edu/itba/paw/services/NewFollowerMailTemplateEscapingTest.java`

### Webapp

- Modify: `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/ProfileController.java`
- Create: `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/SocialController.java`
- Create: `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/FeedController.java`
- Modify: `webapp/src/main/java/ar/edu/itba/paw/webapp/config/WebAuthConfig.java`
- Modify: `webapp/src/main/java/ar/edu/itba/paw/webapp/auth/AccessHelper.java`
- Create: `webapp/src/main/webapp/WEB-INF/views/profile/followers.jsp`
- Create: `webapp/src/main/webapp/WEB-INF/views/profile/following.jsp`
- Create: `webapp/src/main/webapp/WEB-INF/views/feed.jsp`
- Create: `webapp/src/main/webapp/WEB-INF/tags/userProfileCard.tag`
- Modify: `webapp/src/main/webapp/WEB-INF/views/profile/profile.jsp`
- Modify: `webapp/src/main/webapp/css/forkd.css`
- Modify: `webapp/src/main/resources/i18n/messages.properties`
- Modify: `webapp/src/main/resources/i18n/messages_es.properties`
- Modify: `webapp/src/main/resources/i18n/messages_en.properties`
- Create tests: `webapp/src/test/java/ar/edu/itba/paw/webapp/SocialControllerMvcTest.java`
- Create tests: `webapp/src/test/java/ar/edu/itba/paw/webapp/FeedControllerMvcTest.java`
- Modify tests: `webapp/src/test/java/ar/edu/itba/paw/webapp/SecurityMvcTest.java`
- Modify tests: `webapp/src/test/java/ar/edu/itba/paw/webapp/ViewTemplateSecurityTest.java`

## Contratos Java Propuestos

### `SocialFeedFilters`

```java
package ar.edu.itba.paw.models;

public class SocialFeedFilters {

    private final String neighborhood;
    private final Double minimumRating;
    private final String restaurantQuery;

    public SocialFeedFilters(final String neighborhood,
            final Double minimumRating,
            final String restaurantQuery) {
        this.neighborhood = neighborhood;
        this.minimumRating = minimumRating;
        this.restaurantQuery = restaurantQuery;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public Double getMinimumRating() {
        return minimumRating;
    }

    public String getRestaurantQuery() {
        return restaurantQuery;
    }
}
```

### `SocialDao`

```java
package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.models.PageRequest;
import ar.edu.itba.paw.models.Review;
import ar.edu.itba.paw.models.ReviewSortOrder;
import ar.edu.itba.paw.models.SocialFeedFilters;
import ar.edu.itba.paw.models.User;

import java.util.List;

public interface SocialDao {

    boolean follow(long followerId, long followedId);

    void unfollow(long followerId, long followedId);

    boolean isFollowing(long followerId, long followedId);

    long countFollowers(long userId);

    long countFollowing(long userId);

    List<User> findFollowers(long userId, PageRequest pageRequest);

    List<User> findFollowing(long userId, PageRequest pageRequest);

    List<Review> findFeedReviews(long followerId, SocialFeedFilters filters,
            ReviewSortOrder sortOrder, PageRequest pageRequest);

    long countFeedReviews(long followerId, SocialFeedFilters filters);
}
```

### `SocialService`

```java
package ar.edu.itba.paw.services;

import ar.edu.itba.paw.models.Page;
import ar.edu.itba.paw.models.Review;
import ar.edu.itba.paw.models.ReviewSortOrder;
import ar.edu.itba.paw.models.SocialFeedFilters;
import ar.edu.itba.paw.models.User;

public interface SocialService {

    void follow(long followerId, long followedId);

    void unfollow(long followerId, long followedId);

    boolean isFollowing(long followerId, long followedId);

    long countFollowers(long userId);

    long countFollowing(long userId);

    Page<User> findFollowers(long userId, int page, int pageSize);

    Page<User> findFollowing(long userId, int page, int pageSize);

    Page<Review> findFeed(long followerId, SocialFeedFilters filters,
            ReviewSortOrder sortOrder, int page, int pageSize);
}
```

## Plan de Implementacion

### Task 0: Baseline y pre-check

**Files:** read-only.

- [ ] Run:

```bash
git status --short --branch
git -C PAW-Wiki status --short --branch
mvn -pl persistence -am test
mvn -pl services -am test
mvn -pl webapp -am test
```

Expected:

```text
BUILD SUCCESS
```

- [ ] Si hay cambios ajenos, registrarlos en el log de implementacion y no revertirlos.
- [ ] Confirmar que el repo sigue usando `/profile/{profileKey}` leyendo `ProfileController` y `WebAuthConfig`.

### Task 1: Schema social aditivo

**Files:**

- Modify: `persistence/src/main/resources/schema-base.sql`
- Modify: `persistence/src/main/resources/schema-postgres.sql`
- Modify: `persistence/src/test/resources/schema-hsqldb.sql`
- Create tests: `persistence/src/test/java/ar/edu/itba/paw/persistence/SocialJdbcDaoTest.java`

- [ ] Agregar `user_follows` e indices en los tres scripts SQL.
- [ ] En `schema-postgres.sql`, usar solo sentencias compatibles con `ResourceDatabasePopulator`; no usar `DO $$`.
- [ ] Escribir test HSQLDB que verifique:
  - se puede crear follow reviewer -> reviewer,
  - duplicate follow no duplica filas,
  - self-follow falla por constraint o no llega desde service,
  - borrar follower o followed borra filas por cascade.
- [ ] Run:

```bash
mvn -pl persistence -am test -Dtest=SocialJdbcDaoTest
```

Expected:

```text
BUILD SUCCESS
```

### Task 2: DAO social y queries de feed

**Files:**

- Create: `models/src/main/java/ar/edu/itba/paw/models/SocialFeedFilters.java`
- Create: `persistence-contracts/src/main/java/ar/edu/itba/paw/persistence/SocialDao.java`
- Create: `persistence/src/main/java/ar/edu/itba/paw/persistence/SocialJdbcDao.java`
- Modify tests: `persistence/src/test/java/ar/edu/itba/paw/persistence/SocialJdbcDaoTest.java`

- [ ] Implementar `follow` con `boolean` de retorno:
  - `true` si inserto fila nueva,
  - `false` si ya existia.
- [ ] Implementar `unfollow` idempotente con `DELETE`.
- [ ] Implementar `findFollowers`:
  - join `user_follows uf`
  - join `users u ON u.id = uf.follower_id`
  - order `uf.created_at DESC, u.id DESC`
  - `LIMIT/OFFSET`.
- [ ] Implementar `findFollowing`:
  - join `users u ON u.id = uf.followed_id`
  - order `uf.created_at DESC, u.id DESC`.
- [ ] Implementar `findFeedReviews` con un solo query paginado:

```sql
SELECT r.id, r.restaurant_id, r.user_id, r.rating, r.comment, r.created_at, r.reservation_id,
       u.name AS author_name,
       u.username AS author_username,
       u.profile_image_id AS author_profile_image_id,
       rest.name AS restaurant_name,
       rest.slug AS restaurant_slug
FROM user_follows uf
JOIN reviews r ON r.user_id = uf.followed_id
JOIN users u ON u.id = r.user_id
JOIN restaurants rest ON rest.id = r.restaurant_id
WHERE uf.follower_id = ?
  AND rest.status = 'APPROVED'
```

- [ ] Agregar filtros opcionales:
  - `rest.neighborhood = ?` para barrio canonico.
  - `r.rating >= ?` para rating minimo.
  - `LOWER(rest.name) LIKE ? ESCAPE '!'` para restaurante, reutilizando el patron de escape de `RestaurantJdbcDao`.
- [ ] Agregar `ORDER BY` desde `ReviewSortOrder`; no concatenar input crudo de request.
- [ ] Agregar tests de:
  - feed solo trae reviews de seguidos,
  - no trae reviews de no seguidos,
  - filtra por barrio,
  - filtra por rating minimo,
  - filtra por restaurante con LIKE escapado,
  - pagina y cuenta correctamente.
- [ ] Run:

```bash
mvn -pl persistence -am test -Dtest=SocialJdbcDaoTest
```

Expected:

```text
BUILD SUCCESS
```

### Task 3: Service social y reglas de negocio

**Files:**

- Create: `service-contracts/src/main/java/ar/edu/itba/paw/services/SocialService.java`
- Create: `service-contracts/src/main/java/ar/edu/itba/paw/services/InvalidSocialFollowException.java`
- Create: `services/src/main/java/ar/edu/itba/paw/services/SocialServiceImpl.java`
- Create tests: `services/src/test/java/ar/edu/itba/paw/services/SocialServiceImplTest.java`

- [ ] `SocialServiceImpl.follow(followerId, followedId)` debe:
  - cargar ambos usuarios con `UserDao.findById`,
  - exigir que ambos sean reviewers puros con `User::isReviewer`,
  - retornar sin cambios si `followerId == followedId`,
  - llamar `socialDao.follow`,
  - enviar mail solo si `socialDao.follow` devuelve `true`.
- [ ] `unfollow` debe validar que el actor sea reviewer y despues llamar DAO.
- [ ] `findFeed` debe:
  - validar reviewer,
  - normalizar pagina con el mismo calculo de safe page usado por `ReviewServiceImpl`,
  - devolver `Page<Review>`.
- [ ] Tests de service:
  - reviewer puede seguir reviewer,
  - owner no puede seguir,
  - admin no puede seguir,
  - reviewer no puede seguir owner/admin,
  - self-follow no inserta ni manda mail,
  - duplicate follow no manda segundo mail,
  - unfollow es idempotente,
  - feed rechaza actor no reviewer.
- [ ] Run:

```bash
mvn -pl services -am test -Dtest=SocialServiceImplTest
```

Expected:

```text
BUILD SUCCESS
```

### Task 4: Mail de nuevo follower

**Files:**

- Modify: `service-contracts/src/main/java/ar/edu/itba/paw/services/MailService.java`
- Modify: `services/src/main/java/ar/edu/itba/paw/services/SmtpMailServiceImpl.java`
- Create: `services/src/main/resources/mail-templates/new-follower.html`
- Modify: `webapp/src/main/resources/i18n/messages.properties`
- Modify: `webapp/src/main/resources/i18n/messages_es.properties`
- Modify: `webapp/src/main/resources/i18n/messages_en.properties`
- Modify tests: `services/src/test/java/ar/edu/itba/paw/services/SmtpMailServiceImplContextTest.java`
- Create tests: `services/src/test/java/ar/edu/itba/paw/services/NewFollowerMailTemplateEscapingTest.java`

- [ ] Agregar contrato:

```java
void sendNewFollowerNotification(String to, String followerName, String followerProfileKey, Locale locale);
```

- [ ] En `SmtpMailServiceImpl`, construir CTA con:

```text
{app.public.base-url}/profile/{followerProfileKey}
```

- [ ] Template `new-follower.html` debe usar `th:text` para `followerName`, nunca `th:utext`.
- [ ] Agregar keys:
  - `mail.follower.subject`
  - `mail.follower.greeting`
  - `mail.follower.hello`
  - `mail.follower.body.prefix`
  - `mail.follower.body.suffix`
  - `mail.follower.cta`
  - `mail.follower.footer`
- [ ] Tests:
  - render ES y EN sin excepciones,
  - CTA contiene `/profile/{followerProfileKey}`,
  - nombre con HTML se escapa.
- [ ] Run:

```bash
mvn -pl services -am test -Dtest=SmtpMailServiceImplContextTest,NewFollowerMailTemplateEscapingTest,SocialServiceImplTest
```

Expected:

```text
BUILD SUCCESS
```

### Task 5: Seguridad web

**Files:**

- Modify: `webapp/src/main/java/ar/edu/itba/paw/webapp/config/WebAuthConfig.java`
- Modify: `webapp/src/main/java/ar/edu/itba/paw/webapp/auth/AccessHelper.java`
- Modify tests: `webapp/src/test/java/ar/edu/itba/paw/webapp/SecurityMvcTest.java`

- [ ] Permitir publicamente:
  - `GET /profile/*/followers`
  - `GET /profile/*/following`
- [ ] Restringir a reviewer puro:
  - `GET /feed`
  - `POST /profile/{profileKey}/follow`
  - `POST /profile/{profileKey}/unfollow`
- [ ] Agregar helpers chicos en `AccessHelper` solo si mejora legibilidad; reutilizar `isPureReviewer` cuando alcance.
- [ ] Tests de seguridad:
  - anonimo puede ver followers/following de perfil existente,
  - anonimo que entra a `/feed` redirige a login,
  - owner/admin en `/feed` reciben 403,
  - owner/admin en POST follow reciben 403,
  - reviewer en POST follow pasa el filtro de seguridad.
- [ ] Run:

```bash
mvn -pl webapp -am test -Dtest=SecurityMvcTest
```

Expected:

```text
BUILD SUCCESS
```

### Task 6: Controladores sociales

**Files:**

- Create: `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/SocialController.java`
- Create tests: `webapp/src/test/java/ar/edu/itba/paw/webapp/SocialControllerMvcTest.java`

- [ ] `GET /profile/{profileKey}/followers`:
  - resolver reviewer con la misma regla que `ProfileController.resolveReviewerProfile`,
  - usar `PageNumberResolver`,
  - usar `PageSizeResolver.resolveStandardPageSize`,
  - cargar `followersPage`, `followers`, `profileUser`, `currentPageSize`, `pageSizeOptions`.
- [ ] `GET /profile/{profileKey}/following` igual, pero con seguidos.
- [ ] `POST /profile/{profileKey}/follow`:
  - resolver target reviewer,
  - obtener current user desde `PawAuthUser`,
  - llamar `socialService.follow(currentUser.id, target.id)`,
  - redirigir a `redirect` si `SafeRedirectPathValidator.isSafeRedirectPath(redirect)`, si no a `/profile/{profileKey}`.
- [ ] `POST /profile/{profileKey}/unfollow` idem.
- [ ] No duplicar logica de resolucion de perfil si se puede extraer un helper privado pequeno o un componente package-private.
- [ ] Tests MVC:
  - followers page carga model esperado,
  - following page carga model esperado,
  - follow post llama service y redirige a perfil,
  - follow post respeta redirect seguro,
  - follow post ignora redirect inseguro,
  - slug inexistente devuelve 404.
- [ ] Run:

```bash
mvn -pl webapp -am test -Dtest=SocialControllerMvcTest
```

Expected:

```text
BUILD SUCCESS
```

### Task 7: Feed controller

**Files:**

- Create: `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/FeedController.java`
- Create tests: `webapp/src/test/java/ar/edu/itba/paw/webapp/FeedControllerMvcTest.java`

- [ ] `GET /feed` debe:
  - exigir current user reviewer por seguridad,
  - resolver `sort` con `ReviewSortOrder.fromString(sort)`,
  - resolver barrio con `RestaurantCatalog.resolveNeighborhood(neighborhood).orElse(null)`,
  - resolver `rating` aceptando solo `0.5, 1.0, ..., 5.0`,
  - trim de `restaurant`, vacio -> `null`,
  - llamar `socialService.findFeed`.
- [ ] Model esperado:
  - `feedPage`
  - `reviews`
  - `currentPageSize`
  - `pageSizeOptions`
  - `currentReviewSort`
  - `currentNeighborhood`
  - `currentRating`
  - `currentRestaurant`
  - `allNeighborhoods`
  - `ratingOptions`
- [ ] Tests MVC:
  - default carga `DATE_DESC`,
  - sort invalido cae en `DATE_DESC`,
  - rating invalido cae en `null`,
  - barrio no canonico cae en `null`,
  - conserva filtros en el model,
  - pagina se normaliza a `1`.
- [ ] Run:

```bash
mvn -pl webapp -am test -Dtest=FeedControllerMvcTest
```

Expected:

```text
BUILD SUCCESS
```

### Task 8: Perfil, followers/following JSP y feed JSP

**Files:**

- Modify: `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/ProfileController.java`
- Modify: `webapp/src/main/webapp/WEB-INF/views/profile/profile.jsp`
- Create: `webapp/src/main/webapp/WEB-INF/views/profile/followers.jsp`
- Create: `webapp/src/main/webapp/WEB-INF/views/profile/following.jsp`
- Create: `webapp/src/main/webapp/WEB-INF/views/feed.jsp`
- Create: `webapp/src/main/webapp/WEB-INF/tags/userProfileCard.tag`
- Modify: `webapp/src/main/webapp/css/forkd.css`
- Modify: `webapp/src/main/resources/i18n/messages.properties`
- Modify: `webapp/src/main/resources/i18n/messages_es.properties`
- Modify: `webapp/src/main/resources/i18n/messages_en.properties`
- Modify tests: `webapp/src/test/java/ar/edu/itba/paw/webapp/ViewTemplateSecurityTest.java`

- [ ] En `ProfileController.profile`, agregar al model:
  - `followerCount`
  - `followingCount`
  - `isFollowingProfile`
  - `canFollowProfile`
- [ ] `canFollowProfile` es `true` solo si:
  - hay `currentUser`,
  - `currentUser.isReviewer()`,
  - `currentUser.id != profileUser.id`.
- [ ] En `profile.jsp` agregar:
  - links a followers/following con contadores,
  - form POST follow/unfollow con CSRF,
  - hidden `redirect` con path actual si es seguro.
- [ ] `followers.jsp` y `following.jsp`:
  - usar `<c:url>` para links,
  - usar `<c:out>` para nombres y usernames,
  - usar `paw:userAvatar`,
  - usar `paw:paging`,
  - no usar scriptlets.
- [ ] `feed.jsp`:
  - form GET con filtros,
  - hidden page que vuelve a `1` cuando cambia filtro,
  - cards de reviews con autor, restaurante, fecha, rating y comentario escapado,
  - empty state cuando no hay seguidos o no hay resultados.
- [ ] CSS:
  - agregar clases especificas en `forkd.css`,
  - evitar nuevos estilos inline,
  - mantener look consistente con `profile-page`, `reviews-header`, `filter-select` y botones existentes.
- [ ] i18n minima:
  - `profile.followers.title`
  - `profile.following.title`
  - `profile.followers.button`
  - `profile.following.button`
  - `profile.follow.action`
  - `profile.unfollow.action`
  - `profile.followers.empty`
  - `profile.following.empty`
  - `feed.title`
  - `feed.empty.title`
  - `feed.empty.body`
  - `feed.filter.restaurant`
  - `feed.filter.rating`
  - `feed.filter.rating.all`
  - `feed.filter.neighborhood`
  - `feed.filter.clear`
- [ ] Run:

```bash
mvn -pl webapp -am test -Dtest=ViewTemplateSecurityTest,SocialControllerMvcTest,FeedControllerMvcTest
```

Expected:

```text
BUILD SUCCESS
```

### Task 9: Verificacion integrada

**Files:** no edits expected.

- [ ] Run:

```bash
mvn -pl persistence -am test -Dtest=SocialJdbcDaoTest
mvn -pl services -am test -Dtest=SocialServiceImplTest,SmtpMailServiceImplContextTest,NewFollowerMailTemplateEscapingTest
mvn -pl webapp -am test -Dtest=SecurityMvcTest,SocialControllerMvcTest,FeedControllerMvcTest,ViewTemplateSecurityTest
mvn clean test
```

Expected:

```text
BUILD SUCCESS
```

- [ ] Optional package check:

```bash
mvn clean package
```

Expected:

```text
BUILD SUCCESS
```

## Manual QA

1. Levantar Jetty:

```bash
mvn -pl webapp -am jetty:run \
  -Dsecurity.rememberme.key="$(openssl rand -base64 48)"
```

2. Como reviewer A, abrir perfil de reviewer B.
3. Ver boton `Seguir`, contador de followers y contador de following.
4. Click en `Seguir`.
5. Confirmar que el boton cambia a `Dejar de seguir`.
6. Confirmar que followers de B aumenta en 1.
7. Confirmar que following de A aumenta en 1.
8. Abrir `/profile/{profileKey}/followers` de B y ver a A.
9. Abrir `/profile/{profileKey}/following` de A y ver a B.
10. Crear una review como B.
11. Como A, abrir `/feed` y confirmar que aparece la review de B.
12. Filtrar feed por barrio.
13. Filtrar feed por rating minimo.
14. Filtrar feed por restaurante.
15. Cambiar sort y page size.
16. Click en `Dejar de seguir`.
17. Confirmar que la review de B ya no aparece en `/feed`.
18. Como owner, intentar `/feed` y confirmar 403 o login segun sesion.
19. Como admin, intentar seguir un reviewer y confirmar 403.
20. Revisar mail capturado en entorno local o logs SMTP: el usuario B recibe mail de nuevo follower.

## Riesgos y Mitigaciones

| Riesgo | Impacto | Mitigacion |
| --- | --- | --- |
| Confundir `/perfil` con `/profile` | Links rotos o reglas de seguridad duplicadas | v1 usa solo `/profile`, alias separado si se decide |
| Duplicar mails por follows repetidos | Mala UX y ruido de correo | DAO devuelve `boolean inserted`; service manda mail solo en `true` |
| Feed lento con muchos follows/reviews | Mala performance | Indices por follower/followed y query paginada con joins |
| Exponer restaurantes no aprobados | Fuga funcional | `WHERE rest.status = 'APPROVED'` en feed |
| Owner/admin acceden a social | Regresion de roles | Seguridad en `WebAuthConfig` + validacion en service |
| XSS en feed o mail | Seguridad | JSP con `<c:out>` y mails con `th:text` |
| Filtros rompen con input invalido | 400 o SQL incorrecto | Resolvers defensivos y parametros SQL |

## Rollback

Rollback recomendado si la feature ya fue commiteada:

```bash
git revert <social-feature-commit>
```

Rollback si todavia no hubo commit:

```bash
git diff --name-only
```

Revisar esa lista y restaurar solo los archivos de esta feature. No usar `git restore` sobre carpetas completas si hay cambios ajenos en el working tree.

Rollback de DB local si la tabla ya fue creada y se decide revertir la feature:

```sql
DROP TABLE IF EXISTS user_follows;
```

No ejecutar rollback en una base compartida sin confirmar primero si ya hay follows reales.

## Criterio de Aceptacion

- Reviewer puede seguir y dejar de seguir reviewers.
- Owner/admin no pueden seguir ni ver feed.
- Perfil muestra reviews, followers y following.
- Followers/following tienen paginas propias paginadas.
- `/feed` muestra solo reviews de seguidos del usuario autenticado.
- `/feed` filtra por barrio, rating minimo y restaurante.
- `/feed` ordena y pagina sin perder filtros.
- Mail de nuevo follower se envia una sola vez por relacion nueva.
- `mvn clean test` pasa.
- `mvn clean package` pasa o se documenta una falla preexistente con evidencia.

## Next Actions

- [ ] Confirmar que el equipo acepta usar rutas `/profile/**` en lugar de `/perfil/**` para v1.
- [ ] Implementar Task 0 y Task 1 primero para cerrar la base de datos.
- [ ] Seguir en orden: DAO, service, mail, seguridad, controllers, vistas.
- [ ] No mezclar esta feature con redisenos generales del perfil.
- [ ] No tocar roles ni normalizar rutas existentes fuera del alcance social.
