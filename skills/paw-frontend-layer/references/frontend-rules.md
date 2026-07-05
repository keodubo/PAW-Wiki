# PAW Frontend Layer Rules

## Source Pages

Read current frontend code plus:

- `PAW-Wiki/docs/wiki/resumen-final-paw-2026.md`
- `PAW-Wiki/docs/wiki/checklist-tp-final-rest-spa.md`
- `PAW-Wiki/docs/wiki/single-page-applications.md`
- `PAW-Wiki/docs/wiki/api-rest.md`
- `PAW-Wiki/docs/wiki/internacionalizacion.md`
- `PAW-Wiki/docs/wiki/ux-flows.md`
- `PAW-Wiki/docs/wiki/testing-unitario.md`

## Ownership

- `frontend/` owns SPA routes, pages/components, API client wrappers, stores/composables, frontend i18n catalogs, frontend tests, build config, and static assets.
- `webapp/` owns JAX-RS resources, Spring Security, API error mapping, CORS, SPA fallback/static hosting, and WAR asset integration.
- `services/` owns business use cases. The frontend never becomes business authority.

## API Client Boundary

- Centralize base URL, Accept/Content-Type, `Accept-Language`, auth headers, token refresh, Problem Details parsing, `Location`, `Link`, `ETag`, and CORS-exposed headers.
- Components/pages should call services/composables, not raw HTTP.
- Follow server-provided links/URIs when available; do not rebuild internal API paths from memory except for documented base collection entrypoints.
- Keep `401` and `403` behavior distinct. Refresh on expired credentials; route/notify forbidden access without refresh loops.

## State, Cache, And HATEOAS

- Use store/composable state as the single source of truth for shared async data.
- Cache by URI when DTOs expose related resource URIs.
- Deduplicate in-flight requests for the same URI.
- Use `ETag`/`If-None-Match` when the API provides validators; preserve old data on `304`.
- Reset or invalidate cache when filters, auth identity, language, or write operations change the visible result.

## Routing And URL State

- SPA routes must survive refresh, deep links, bookmarks, and back/forward.
- Keep filters, search, sort, and pagination in query params when they define visible page state.
- Production base path must match the WAR context path. Vite `base`, React `homepage`/router basename, or Angular `baseHref` cannot be guessed.
- Non-API deep links fall back to `index.html`; `/api/*` failures stay API JSON/status.

## Forms, Errors, And I18n

- Client validation is for immediate feedback; backend validation is authoritative.
- Map Problem Details and field errors into user-visible messages.
- Preserve entered values on recoverable validation failures.
- Keep catalogs symmetric across locales and use complete messages with placeholders.
- Do not hardcode visible copy in components when the project uses i18n.

## Build And Packaging

- Use the package manager and lockfile already present. Prefer reproducible install commands (`npm ci` when a package-lock exists) unless the repo declares otherwise.
- `frontend-maven-plugin` should install reproducible Node/npm and run install/test/build from Maven when the final artifact is a WAR.
- `webapp` must depend on the `frontend` module so Maven builds current assets before packaging.
- `maven-war-plugin` must include the actual output directory containing `index.html`.
- Do not rely on manual `dist/` copies or stale generated assets.

## Tests And Smoke

- Behavior-only frontend tests assert route/query state, visible results, form errors, auth redirects, store/composable state, API client return values, and cache behavior.
- Avoid assertions on component internals, exact CSS classes, exact DOM structure, source snippets, call order, or framework internals.
- When no runner exists, document the gap and run a repeatable smoke: context-path root, hashed asset, valid API call, API JSON 404, SPA deep link, and one route/form behavior from the slice.

