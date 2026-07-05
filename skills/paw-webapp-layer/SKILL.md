---
name: paw-webapp-layer
description: Use when creating, changing, auditing, or reviewing PAW Forkd webapp controllers, forms, validators, JSP/JSTL views, i18n bundles, Spring Security routes, webapp CSS/JS, uploads, redirects, MVC tests, REST resources, API error mapping, CORS, WAR asset integration, or SPA static-hosting work across TP1 and TP final.
---

# Paw Webapp Layer

## Overview

Use this for `webapp/`: TP1 controllers, forms, validation, JSP/JSTL, i18n, Spring Security, filters, webapp CSS/JS, and MVC tests. In TP final, use it for REST resources, API error mapping, auth/web security, CORS, SPA static hosting, and WAR asset integration after `$paw-tp-final-migration` sets the plan. Use `$paw-frontend-layer` for SPA source under `frontend/`.

Read `references/layer-rules.md` before editing webapp.

## Workflow

1. Inspect `CLAUDE.md`, affected controller/form/JSP/tests, and `DESIGN.md` for UI changes; for TP final also read `resumen-final-paw-2026` and `checklist-tp-final-rest-spa`.
2. Resolve stage: TP1 uses MVC/JSP; TP final may use REST resources plus SPA assets.
3. Keep controllers/resources thin: bind/deserialize, validate, delegate once, return view/redirect or HTTP response/status/header/body.
4. Put validation in form annotations/custom JSR380 validators for MVC; keep REST input validation and API errors explicit for final.
5. Keep authorization declarative in `WebAuthConfig` plus `AccessHelper` expressions; update JSP visibility with Spring Security taglib when JSP remains.
6. Render JSP safely with `<c:out>`, `<c:url>`, `<spring:message>`, and private views under `WEB-INF`.
7. Preserve GET state and inline validation behavior already expected by MVC flows.
8. For TP final migration, work only inside the current vertical slice from `$paw-tp-final-migration`: REST contract, SPA route/static hosting, tests, and rollback state must match that slice.
9. Route SPA source code, API client, router, stores/composables, frontend i18n, and frontend tests to `$paw-frontend-layer`.
10. Test routes, bindings, security, redirects/status codes, i18n, template safety, or API contracts as applicable.

## Web Rules

- No SQL, `java.sql`, business orchestration, or domain ownership decisions in controllers.
- No scriptlets or Java code in JSPs.
- No raw `${...}` for dynamic user content; use `<c:out>` or escaping helpers.
- No free redirects; use `SafeRedirectPathValidator`.
- Do not mix `@PreAuthorize` with URL rules unless the existing flow already chose that pattern.
- Use shared JSP fragments/tags and `forkd.css` before adding local markup or inline styles.
- For visual work, read `DESIGN.md` first.
- For TP final SPA/API work, use `$paw-tp-final-migration` first; do not graft a frontend build or JWT flow into TP1 by accident.
- For SPA source work under `frontend/`, use `$paw-frontend-layer`; webapp owns hosting/integration, not component/store/router implementation.
- TP final JAX-RS resources live under `/api/*`, expose DTOs/forms only, and build `Location`, relation links, `Link`, and `ETag` with request-aware URI helpers.
- API errors are JSON Problem Details (`type`, `title`, `status`, `detail`, `instance`); `/api/*` 404 stays JSON, while SPA fallback serves `index.html` only for non-API deep links.
- If the SPA reads `Location`, `Link`, `ETag`, `X-Access-Token`, or `X-Refresh-Token`, expose them explicitly through CORS.
- Cache dynamic API responses with validators (`ETag`/`If-None-Match`/`304`) only when semantically valid. Cache only hashed static assets as long-lived `immutable`; keep `index.html`/root revalidated or short-lived.
- TP final packaging must keep one WAR with API, `index.html`, hashed JS/CSS/assets, and backend classes; Maven must build frontend before `webapp`.
- Do not remove the old JSP route for a migrated flow until the slice has green API/resource tests, SPA route verification, and package/static-hosting checks or an explicit rollback decision.
- Admin and owner restaurant forms share `WEB-INF/views/shared/restaurant-form-body.jspf`; keep them aligned.
- Upload errors go through `MultipartRequestSizeFilter` and `ErrorHandlingAdvice`.

## Verification

- Focused MVC tests: `mvn -pl webapp -am -Dtest=<MvcTestName> -Dsurefire.failIfNoSpecifiedTests=false test`.
- Webapp suite: `mvn -pl webapp -am test`.
- Cross-layer feature: `mvn clean test`.
- TP final package gate: `mvn clean package`, then inspect the WAR for `index.html`, JS/CSS/assets, and `WEB-INF/classes`.
- WAR inspection example: `jar tf webapp/target/*.war | rg '(^|/)index.html$|assets/|static/|WEB-INF/classes'`.
- Local smoke when needed: `mvn -pl webapp -am jetty:run -Dsecurity.rememberme.key="$(openssl rand -base64 48)"`.
