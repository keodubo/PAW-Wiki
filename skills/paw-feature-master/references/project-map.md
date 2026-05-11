# PAW Forkd Project Map

## Checkout

- Default app repo: current working directory if it is a PAW/Forkd checkout; otherwise the path in `PAW_APP_REPO`.
- Wiki: `PAW-Wiki/docs/` when vendored beside the app checkout, or this repository's `docs/` when using the wiki as a standalone clone.
- Wiki raw sources are immutable; read them but do not edit.
- Wiki updates, when requested, must update `PAW-Wiki/docs/index.md` and `PAW-Wiki/docs/log.md`.

## Stack And Modules

For TP1, the repo is Java 21, Spring Web MVC classic, Spring Security, JSP/JSTL, JDBC, PostgreSQL, Maven, WAR packaging. Do not migrate to Spring Boot, React/Next/Vue/Thymeleaf for web views, JPA/Hibernate for TP1 persistence, or a new frontend build pipeline unless the user explicitly asks.

## Stage Map

| Stage | Allowed default stack | Migration skill | Main risk |
| --- | --- | --- | --- |
| `TP1` | Spring MVC + JSP/JSTL + JDBC + HSQLDB + Spring Security + Logback | none by default | Accidentally introducing later-stage tools |
| `TP2` / `TPE2` | JPA/Hibernate persistence with Spring ORM over the existing service/web shape, preserving behavior and server data | `$paw-tp2-migration` | Bad mappings, hidden N+1, unsafe schema generation, dirty checking surprises, data loss, unresolved TPE1 feedback |
| `TP final` | REST API + SPA/frontend build served by the WAR | `$paw-tp-final-migration` | Leaky API contracts, duplicated business logic, auth storage risk, broken packaging/cache |

Ask or confirm stage before stack-changing work. Class PDFs in `resumen-clases-paw-2026.md` are important stage guidance, but their dependency versions are historical and not an implementation recommendation.

Real modules:

| Module | Role | Notes |
| --- | --- | --- |
| `models` | Domain POJOs, enums, value objects, pagination/filter/sort types | No Spring/JDBC/servlet infrastructure. |
| `persistence-contracts` | DAO interfaces | Repo split of the catedra's `interfaces` module. |
| `persistence` | JDBC DAOs, schema, HSQLDB test config, migrations | SQL and `java.sql` stay here. |
| `service-contracts` | Service APIs, command DTOs, business exceptions | Repo split of the catedra's `interfaces` module. |
| `services` | Business logic, transactions, mail, schedulers | Uses DAO contracts; concrete persistence is runtime. |
| `webapp` | Controllers, forms, validators, Spring Security, JSP/tags/CSS/JS, WAR | Controllers compile against models and service contracts. |

Canonical catedra split is `webapp`, `services`, `interfaces`, `persistence`, `models`. In this repo, document `service-contracts` and `persistence-contracts` as implementation detail, not a violation.

## Canonical Wiki Pages

Always start from `PAW-Wiki/docs/index.md`, then load only relevant pages:

- Architecture: `modelo-capas.md`, `comparacion-capas-web-services-persistence.md`, `configuracion-maven.md`, `inyeccion-dependencias.md`.
- Web/MVC: `spring-web-mvc.md`, `jsp-jstl.md`, `validacion-formularios.md`, `comparacion-jsp-formularios-e-i18n.md`, `internacionalizacion.md`, `ux-flows.md`.
- Security: `spring-security.md`, `auth-flows.md`, `comparacion-seguridad-web.md`, `xss-prevencion.md`.
- Persistence: `persistencia-jdbc.md`, `n-plus-1-joins-java.md`, `comparacion-testing-servicios-y-daos.md`.
- Services: `transactional.md`, `spring-aop.md`, `mailing.md`, `manejo-excepciones.md`, `logging.md`.
- Quality: `testing-unitario.md`, `java-style.md`, `buenas-practicas.md`, `criterios-evaluacion.md`.
- Stage source: `resumen-clases-paw-2026.md`, `tp1-vs-tpe2-final.md`.
- TP2 migration: `resumen-enunciado-tpe2.md`, `hibernate-jpa.md`, plus `persistencia-jdbc.md`, `transactional.md`, `testing-unitario.md`, `criterios-evaluacion.md`, `calendario-entregas.md`.
- TP final migration: `api-rest.md`, `single-page-applications.md`, `internacionalizacion.md`, `ux-flows.md`, `spring-security.md`.
- Current large features: `plan-implementacion-reservas.md`, `resumen-spec-reservas.md`, `PAW-Wiki/docs/superpowers/plans/2026-04-26_social-forkd-plan_v1.md`.
- Current compliance findings: `2026-04-24_auditoria-implementacion-contra-wiki_v1.md`, `PAW-Wiki/docs/superpowers/plans/2026-04-24_paw-remediacion-cumplimiento-wiki_v1.md`.

## Feature Categories

### Data/schema feature

Order: models -> persistence contracts -> persistence -> service contracts -> services -> webapp.

Checks: HSQLDB schema parity, DAO tests, no data-loss migration, no `java.sql` outside persistence.

Testing skill: use `$paw-testing-layer` for DAO fixtures, HSQLDB assertions, and Maven gates.

### Business flow feature

Order: service contract DTO/exceptions -> service tests -> service implementation -> controller/forms/views.

Checks: controller calls one service use case, transactional boundaries, exception mapping, route/security coverage.

Testing skill: use `$paw-testing-layer` for service tests, fake side effects, and avoiding `verify()` coupling.

### Auth/security feature

Order: WebAuthConfig/AccessHelper decision -> service/domain invariants -> JSP visibility -> MVC/security tests.

Checks: no manual auth `if` in controllers, no free redirects, 403 vs 404 semantics, Spring Security taglib in JSP.

Testing skill: use `$paw-testing-layer` for MVC/security tests and route-access verification.

### UI/form feature

Order: shared form/body/tag contract -> form bean validators -> controller binding -> JSP/i18n/CSS -> MVC/template tests.

Checks: input preserved on validation error, inline errors, i18n keys in default/ES/EN bundles, no raw dynamic output.

Testing skill: use `$paw-testing-layer` for validation, template, and i18n consistency tests.

### Mail/scheduler feature

Order: service contract -> service implementation -> template in `services/src/main/resources/mail-templates` -> idempotence/storage -> tests.

Checks: recipient locale, async where appropriate, scheduler delegates to transactional service method, mail templates do not live in webapp.

Testing skill: use `$paw-testing-layer` for recording fakes, scheduler state assertions, and context/proxy-sensitive checks.

### Docs/wiki task

Order: read `PAW-Wiki/docs/CLAUDE.md` -> read index -> edit/create wiki page -> update index/log/tree if required.

Checks: raw sources untouched, bidirectional links, uncertainty marked when sources are weak.

### TP2 persistence migration

Use `$paw-tp2-migration`.

Order: official TPE2 contract -> baseline DAO/schema/tests/data assumptions -> TPE1 feedback inventory -> entity mapping plan -> Spring ORM config -> DAO implementation -> service transaction audit -> tests -> generated SQL/fetch review -> package/deploy check when wiring changed.

Checks: no destructive schema generation without approval, no server data loss, no `EntityManager` leaking to services/webapp, fetch/cascade choices reviewed, current JDBC behavior preserved unless intentionally changed, prior feedback not left broken, `mvn clean package` still produces a usable WAR when relevant.

### TP final REST + SPA migration

Use `$paw-tp-final-migration`.

Order: API contract/DTOs -> service boundary review -> REST resources/security/errors -> frontend app/routing/state/forms -> Maven packaging/static hosting -> cache/file revving.

Checks: API is stateless, backend remains source of truth, frontend validates for UX only, `mvn package` still produces a usable WAR, root HTML is not cached as immutable.

## Verification Gates

Use the smallest meaningful gate first:

```bash
mvn -pl models test
mvn -pl persistence-contracts -am test
mvn -pl persistence -am test
mvn -pl service-contracts -am test
mvn -pl services -am test
mvn -pl webapp -am test
mvn clean test
mvn clean package
```

For targeted webapp tests that sit behind upstream modules:

```bash
mvn -pl webapp -am -Dtest=<TestName> -Dsurefire.failIfNoSpecifiedTests=false test
```

Local runtime:

```bash
mvn -pl webapp -am jetty:run -Dsecurity.rememberme.key="$(openssl rand -base64 48)"
```

Jetty is pinned to port `8080` in `webapp/pom.xml`; if occupied, diagnose the process conflict rather than assuming `-Djetty.http.port` works.

## High-Risk Guardrails

- Do not change local config/secret handling unless the user asks.
- Do not delete data or reset DB without explicit confirmation.
- Do not do commits, merges, PRs, or branch rewrites unless explicitly requested.
- Do not treat a green Maven build as proof of wiki compliance; still inspect layer rules.
- Do not assume older wiki plans reflect current code; current checkout wins.
