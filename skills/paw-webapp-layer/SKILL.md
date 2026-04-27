---
name: paw-webapp-layer
description: Use when creating, changing, auditing, or reviewing PAW Forkd webapp controllers, forms, validators, JSP/JSTL views, i18n bundles, Spring Security routes, CSS/JS, uploads, redirects, MVC tests, REST resources, or SPA static-hosting work across TP1 and TP final.
---

# Paw Webapp Layer

## Overview

Use this for `webapp/`: TP1 controllers, forms, validation, JSP/JSTL, i18n, Spring Security, filters, CSS, JS, and MVC tests. In TP final, use it for REST resources, API error mapping, auth/web security, SPA static hosting, and frontend asset integration after `$paw-tp-final-migration` sets the plan.

Read `references/layer-rules.md` before editing webapp.

## Workflow

1. Inspect `CLAUDE.md`, affected controller/form/JSP/tests, and `DESIGN.md` for UI changes.
2. Resolve stage: TP1 uses MVC/JSP; TP final may use REST resources plus SPA assets.
3. Keep controllers/resources thin: bind/deserialize, validate, delegate once, return view/redirect or HTTP response.
4. Put validation in form annotations/custom JSR380 validators for MVC; keep REST input validation and API errors explicit for final.
5. Keep authorization declarative in `WebAuthConfig` plus `AccessHelper` expressions; update JSP visibility with Spring Security taglib when JSP remains.
6. Render JSP safely with `<c:out>`, `<c:url>`, `<spring:message>`, and private views under `WEB-INF`.
7. Preserve GET state and inline validation behavior already expected by MVC flows.
8. Test routes, bindings, security, redirects/status codes, i18n, template safety, or API contracts as applicable.

## Web Rules

- No SQL, `java.sql`, business orchestration, or domain ownership decisions in controllers.
- No scriptlets or Java code in JSPs.
- No raw `${...}` for dynamic user content; use `<c:out>` or escaping helpers.
- No free redirects; use `SafeRedirectPathValidator`.
- Do not mix `@PreAuthorize` with URL rules unless the existing flow already chose that pattern.
- Use shared JSP fragments/tags and `forkd.css` before adding local markup or inline styles.
- For visual work, read `DESIGN.md` first.
- For TP final SPA/API work, use `$paw-tp-final-migration` first; do not graft a frontend build or JWT flow into TP1 by accident.
- Admin and owner restaurant forms share `WEB-INF/views/shared/restaurant-form-body.jspf`; keep them aligned.
- Upload errors go through `MultipartRequestSizeFilter` and `ErrorHandlingAdvice`.

## Verification

- Focused MVC tests: `mvn -pl webapp -am -Dtest=<MvcTestName> -Dsurefire.failIfNoSpecifiedTests=false test`.
- Webapp suite: `mvn -pl webapp -am test`.
- Cross-layer feature: `mvn clean test`.
- Local smoke when needed: `mvn -pl webapp -am jetty:run -Dsecurity.rememberme.key="$(openssl rand -base64 48)"`.
