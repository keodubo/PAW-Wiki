---
name: paw-webapp-layer
description: Use when creating, changing, auditing, or reviewing PAW Forkd webapp controllers, forms, validators, JSP/JSTL views, i18n bundles, Spring Security routes, CSS/JS, uploads, redirects, or MVC tests.
---

# Paw Webapp Layer

## Overview

Use this for `webapp/`: controllers, forms, validation, JSP/JSTL, i18n, Spring Security, filters, CSS, JS, and MVC tests.

Read `references/layer-rules.md` before editing webapp.

## Workflow

1. Inspect `CLAUDE.md`, affected controller/form/JSP/tests, and `DESIGN.md` for UI changes.
2. Keep controllers thin: bind, validate, delegate, return view/redirect.
3. Put validation in form annotations and custom JSR380 validators, not controller `if` blocks.
4. Keep authorization declarative in `WebAuthConfig` plus `AccessHelper` expressions; update JSP visibility with Spring Security taglib.
5. Render JSP safely with `<c:out>`, `<c:url>`, `<spring:message>`, and private views under `WEB-INF`.
6. Preserve GET state and inline validation behavior already expected by the flow.
7. Test routes, bindings, security, redirects, i18n, and template safety.

## Web Rules

- No SQL, `java.sql`, business orchestration, or domain ownership decisions in controllers.
- No scriptlets or Java code in JSPs.
- No raw `${...}` for dynamic user content; use `<c:out>` or escaping helpers.
- No free redirects; use `SafeRedirectPathValidator`.
- Do not mix `@PreAuthorize` with URL rules unless the existing flow already chose that pattern.
- Use shared JSP fragments/tags and `forkd.css` before adding local markup or inline styles.
- For visual work, read `DESIGN.md` first.
- Admin and owner restaurant forms share `WEB-INF/views/shared/restaurant-form-body.jspf`; keep them aligned.
- Upload errors go through `MultipartRequestSizeFilter` and `ErrorHandlingAdvice`.

## Verification

- Focused MVC tests: `mvn -pl webapp -am -Dtest=<MvcTestName> -Dsurefire.failIfNoSpecifiedTests=false test`.
- Webapp suite: `mvn -pl webapp -am test`.
- Cross-layer feature: `mvn clean test`.
- Local smoke when needed: `mvn -pl webapp -am jetty:run -Dsecurity.rememberme.key="$(openssl rand -base64 48)"`.
