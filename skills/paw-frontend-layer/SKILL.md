---
name: paw-frontend-layer
description: Use when creating, changing, auditing, or reviewing PAW TP final SPA frontend source, routes, API clients, stores/composables, forms, auth state, i18n catalogs, frontend tests, build config, static assets, or context-path/base-path behavior.
---

# Paw Frontend Layer

## Overview

Use this for the TP final `frontend/` SPA source after `$paw-tp-final-migration` has confirmed the stage and slice. Keep the frontend as a client of the REST API: UI and state live here, business authority stays in backend services, and HTTP contracts stay explicit.

Read `references/frontend-rules.md` before editing frontend source, tests, or build config.

## Required First Pass

1. Confirm the current TP final vertical slice from `$paw-tp-final-migration`.
2. Inspect `frontend/package.json`, lockfile, build config (`vite.config.*`, Angular/React equivalent), router, API client/services, stores/composables, i18n catalogs, tests, and the webapp packaging config that consumes the build output.
3. Identify the API contract the slice consumes: resource URIs, media types, `Location`, `Link`, `ETag`, auth headers, Problem Details, and exposed CORS headers.
4. State the frontend runner/build commands from package scripts. If no runner exists, define a repeatable smoke check before claiming the slice is verified.
5. Do not choose or replace a frontend framework unless the user asked or the repo already makes the choice clear.

## Workflow

1. Stay inside the current user-visible slice: one SPA route/flow, its API calls, state, forms, errors, i18n, tests, and smoke gate.
2. Put HTTP details in an API client/service boundary. Components should not scatter raw `fetch`/`axios`, token refresh, header parsing, or URL construction.
3. Keep routes URL-driven: refresh, deep links, filters, pagination, back/forward, and bookmarks must work under the WAR context path.
4. Use stores/composables for shared state and async logic. For HATEOAS-heavy flows, use cache by URI, identity map behavior, request coalescing, and `ETag` revalidation where the API supports it.
5. Treat client validation as UX only. Map backend validation and Problem Details into user-visible errors without hiding backend authority.
6. Keep i18n catalogs complete across supported locales; use full messages with placeholders, not concatenated fragments.
7. Test public behavior: route state, rendered user outcomes, form validation, store/composable state transitions, API error handling, i18n output, and cache/refetch behavior.
8. Run frontend tests/build through declared package scripts, then the Maven/WAR gate when packaging, base path, static hosting, or assets are touched.

## Frontend Rules

- API clients parse `Location`, `Link`, `ETag`, auth/token headers, and Problem Details; pages/components consume typed results.
- Do not hardcode server URLs that should come from HATEOAS links or the API base config.
- Do not refresh tokens on `403`; reserve refresh behavior for `401`/expired credential paths.
- Token storage, logout clearing, refresh rotation, and exposed auth headers must be deliberate and documented.
- Router guards are UX. Backend authorization remains authoritative through Spring Security/API status.
- Base path must match deploy context (`base`, `homepage`, `baseHref`, or equivalent). Do not assume `/` in production.
- Hashed assets can be immutable only when the build emits content-hashed names and `index.html` is revalidated or short-lived.
- Do not commit generated `dist/`, local `.env`, secrets, node caches, coverage, or machine-specific artifacts unless the repo explicitly tracks them.

## Testing Rules

- Tests are blackbox and behavior-only.
- Do not assert component internals, implementation-specific store calls, exact CSS classes, source snippets, fragile DOM shape, or framework internals as the target.
- Prefer route/query state, visible text/errors, enabled/disabled commands, emitted user-visible events, store state, API client returned values, and cache behavior.
- If there is a package script such as `test`, `type-check`, `lint`, or `build`, use the repo-declared script instead of inventing commands.
- If no test runner exists, record the gap and run a repeatable browser/static-hosting smoke for the slice.

## Stop Conditions

Ask before proceeding when:

- The frontend framework/tooling choice is absent or conflicts with the repo/user direction.
- Auth token storage, refresh behavior, logout, or exposed headers are unresolved.
- The slice requires changing deploy context path, asset base path, package manager, lockfile, or CI/package commands.
- No frontend runner exists and a repeatable smoke check cannot be performed locally.
- The API contract needed by the frontend is not implemented, ambiguous, or returns HTML for `/api/*` failures.

## Verification

Use actual package scripts from `frontend/package.json`; common examples:

```bash
npm test
npm run type-check
npm run build
```

For package/static-hosting changes, also use the PAW Maven gate:

```bash
mvn clean package
jar tf webapp/target/*.war | rg '(^|/)index.html$|assets/|static/|WEB-INF/classes'
```

Smoke when touched: root under context path, hashed asset, valid API call, API 404 JSON/Problem Details, SPA deep link fallback, and refresh/back-forward behavior for the slice.
