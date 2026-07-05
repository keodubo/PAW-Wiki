# PAW Webapp Layer Rules

## Source Pages

Read current webapp code plus:

- `PAW-Wiki/docs/wiki/spring-web-mvc.md`
- `PAW-Wiki/docs/wiki/resumen-clases-paw-2026.md`
- `PAW-Wiki/docs/wiki/jsp-jstl.md`
- `PAW-Wiki/docs/wiki/validacion-formularios.md`
- `PAW-Wiki/docs/wiki/comparacion-jsp-formularios-e-i18n.md`
- `PAW-Wiki/docs/wiki/internacionalizacion.md`
- `PAW-Wiki/docs/wiki/spring-security.md`
- `PAW-Wiki/docs/wiki/auth-flows.md`
- `PAW-Wiki/docs/wiki/xss-prevencion.md`
- `PAW-Wiki/docs/wiki/manejo-excepciones.md`
- `PAW-Wiki/docs/wiki/api-rest.md`, `PAW-Wiki/docs/wiki/single-page-applications.md`, `PAW-Wiki/docs/wiki/resumen-final-paw-2026.md`, and `PAW-Wiki/docs/wiki/checklist-tp-final-rest-spa.md` when stage is TP final.
- `DESIGN.md` for visual changes.

## Controllers

- Controllers bind request data, validate with `@Valid`, inspect `BindingResult`, call one service use case, and return view/redirect.
- Do not put SQL, joins, business state transitions, ownership checks, or multi-service orchestration in controllers.
- Use DTOs from `service-contracts` instead of building domain entities in controllers.
- Use `@ControllerAdvice` for repeated model attributes and exception handling.

## TP Final REST/SPA Notes

- Use `$paw-tp-final-migration` before replacing JSP flows with API/SPA work.
- JAX-RS resources use resource paths and HTTP verbs (`@Path`, `@GET`, `@POST`, `@PUT`, `@PATCH`, `@DELETE`) under `/api/*`; no action endpoints or `/v1` path versioning.
- REST resources return deliberate status codes, DTO bodies, and headers; never expose domain entities or lazy proxies by accident.
- `201 Created` includes valid `Location`; collection navigation uses `Link` headers and optional `X-Total-Count`; cacheable API responses use `ETag`/`If-None-Match`/`304`.
- Error mappers return JSON Problem Details (`type`, `title`, `status`, `detail`, `instance`) for validation, auth, negotiation, and 404 API errors.
- Expose `Location`, `Link`, `ETag`, `X-Access-Token`, and `X-Refresh-Token` in CORS when the browser client must read them.
- API auth should be stateless by design. Document token/cookie storage trade-offs before implementing.
- Frontend validation improves UX, but backend validation remains authoritative.
- Routing stays separated: `/api/*` returns API/Problem JSON, `/assets/*` or `/static/*` serves static files, and SPA fallback serves `index.html` only for non-API deep links.
- Static asset hosting must preserve `mvn package`; cache immutable only for hashed/versioned assets, never for `index.html` or the root HTML entry.
- Maven packaging must build the frontend before `webapp` and produce one WAR containing API classes plus `index.html`, JS/CSS, and assets.

## Forms And Validation

- Use form beans with getters/setters and Bean Validation annotations.
- Use custom `@Constraint` validators for cross-field or service-backed validation.
- Do not manually invoke validators from controllers.
- Preserve user input and inline errors on validation failure.
- Validation messages use i18n keys, not hardcoded text.

## Security

- Keep route access in `WebAuthConfig` plus `AccessHelper` expressions.
- Avoid manual `if (loggedUser == null)` and role magic strings in controllers.
- Use Spring Security taglib in JSP for role/visibility decisions.
- Login POST and logout are Spring Security responsibilities, not custom controllers.
- Safe redirects go through `SafeRedirectPathValidator`.
- Use 403 for existing resource without permission and 404 for missing resource unless endpoint semantics explicitly require otherwise.

## JSP/JSTL/i18n

- Views live under `WEB-INF`; this repo uses `WEB-INF/views`.
- Use `<c:url>` for internal URLs.
- Use `<c:out>` or equivalent escaping for dynamic content.
- Use `<spring:message>` for visible text, titles, alt text, labels, buttons, errors, and helper copy.
- Do not concatenate translated fragments; use complete messages with placeholders.
- No scriptlets.
- Avoid inline `style`; extract repeated styles into `forkd.css`.

## Design System

- Start visual work at `DESIGN.md`, then `webapp/src/main/webapp/css/forkd.css`, tags under `WEB-INF/tags`, and `webapp/src/main/webapp/js/forkd.js`.
- Public/reviewer pages are restaurant-first and image-led.
- Owner/admin pages are dense, compact, and operational.
- Reuse shared tags/fragments before adding new page-local structures.

## Repo-Specific Hot Spots

- Owner/admin restaurant forms share `WEB-INF/views/shared/restaurant-form-body.jspf`.
- Upload UX flows through `MultipartRequestSizeFilter` and `ErrorHandlingAdvice`.
- Current public profile route is `/profile/{publicProfileKey}`.
- Owner route prefix is `/my-restaurants/**`.
- Preserve full GET state when changing filters, page size, sort, language redirects, and paginated links.

## Common Verification Commands

```bash
mvn -pl webapp -am -Dtest=<MvcTestName> -Dsurefire.failIfNoSpecifiedTests=false test
mvn -pl webapp -am test
mvn clean test
mvn -pl webapp -am jetty:run -Dsecurity.rememberme.key="$(openssl rand -base64 48)"
```
