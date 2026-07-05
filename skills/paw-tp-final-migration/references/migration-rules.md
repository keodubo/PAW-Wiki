# PAW TP Final Migration Rules

## Source Pages

Read current code plus:

- `PAW-Wiki/docs/wiki/resumen-clases-paw-2026.md`
- `PAW-Wiki/docs/wiki/resumen-final-paw-2026.md`
- `PAW-Wiki/docs/wiki/checklist-tp-final-rest-spa.md`
- `PAW-Wiki/docs/wiki/tp1-vs-tpe2-final.md`
- `PAW-Wiki/docs/wiki/api-rest.md`
- `PAW-Wiki/docs/wiki/single-page-applications.md`
- `PAW-Wiki/docs/wiki/spring-security.md`
- `PAW-Wiki/docs/wiki/internacionalizacion.md`
- `PAW-Wiki/docs/wiki/ux-flows.md`
- `PAW-Wiki/docs/wiki/manejo-excepciones.md`
- `PAW-Wiki/docs/wiki/testing-unitario.md`
- `skills/paw-frontend-layer/references/frontend-rules.md` from `$paw-frontend-layer` when frontend source changes.

## Stage Contract

- TP final changes the presentation/API architecture: REST resources plus SPA/frontend.
- Do not silently remove working JSP flows unless the user asked for replacement and rollback/parallel routing is clear.
- Backend services remain the business source of truth.
- Dependency/tool versions in class PDFs are historical. Use current repo docs and compatibility for actual tooling choices.
- A request like "pasalo a final", "TP final", or "migrar a REST/SPA" is an implementation request unless the user explicitly says audit/plan only. A bare "quiero migrar" is ambiguous between TP2/JPA and TP final/REST+SPA; ask before editing.

## API Rules

- Model resources as nouns and stable URIs.
- Use HTTP verbs for actions where possible.
- Return correct status codes: `201` + `Location` for creation, `204` for successful delete/no body, `400` for validation, `401/403/404` deliberately.
- Jersey/JAX-RS resources use `@Path`, HTTP method annotations, DTOs/forms, `Response`, and `UriInfo`/builders for `self`, related links, `Location`, and context-path-safe URIs.
- Use DTOs for API representations; include public links/URIs and safe fields, but do not leak domain entities, persistence entities, lazy proxies, emails, tokens, admin flags, or internal ids without product need.
- Keep validation and error payloads consistent enough for the SPA to consume.
- Put pagination navigation in `Link` headers and, when useful, `X-Total-Count`; avoid artificial list envelopes unless the contract has a clear reason.
- Version representations through `Accept`/`Content-Type` vendor media types when required; avoid `/v1` path versioning and never use media types to select hidden operations unrelated to a real partial update.

## Slice Execution Rules

- Do not migrate the whole app as one undifferentiated refactor. Work one vertical user-visible flow at a time.
- Each slice starts with an inventory: old MVC route/view, current service calls, auth rule, data dependencies, i18n keys, existing tests, and deploy/package assumptions.
- Each slice plan must name the new REST resource(s), DTO/form objects, status/header/error contracts, SPA route/client/store/form work, auth storage/refresh/logout decision when touched, first failing tests, frontend runner or smoke command, verification commands, package output/base-path gate, and rollback/parallel-routing state.
- API-first does not mean API-only. A slice is complete when backend contract, SPA usage, tests, and packaging/smoke implications are handled or explicitly deferred.
- Keep the old JSP route live until the migrated slice has passed API + SPA verification or the user approves removal.
- If frontend framework/tooling is absent, propose the minimal compatible module shape and ask only when the stack choice cannot be inferred from the repo/user.
- Treat `tp-final nota_10/` as reference patterns with caveats, not code to clone. Never copy local `.env`, `.properties`, private keys, demo credentials, generated `dist`, or machine-specific config.

## Auth And Security Rules

- API auth should be stateless by design.
- Compare token/header and cookie approaches before implementing.
- Initial credentials may use `Authorization: Basic ...`; normal API calls should use `Authorization: Bearer <access-token>`.
- `401` means no valid credential, invalid credential, or expired credential; `403` means the user is known but lacks permission. Do not refresh tokens on `403`.
- Refresh tokens should rotate on login/use and should not be returned in every response or exposed as normal public data.
- LocalStorage has XSS risk; cookies have automatic-send and overhead/CSRF trade-offs.
- Make token storage, refresh rotation/revocation, logout clearing, and exposed auth headers explicit. Do not normalize exposing `X-Refresh-Token` unless the trade-off is documented.
- Spring Security still owns authorization rules; do not recreate route access logic inside every resource.
- Frontend visibility checks are UX only, not authorization.
- If the SPA must read auth or navigation headers, expose only the required headers explicitly (`Location`, `Link`, `ETag`, `X-Access-Token`, `X-Refresh-Token`).

## SPA Rules

- Keep a clear API client boundary; components should not scatter raw fetch/HTTP details everywhere.
- Treat state as a single source of truth. Choose local state/store complexity based on real need.
- For HATEOAS-heavy flows, use cache by URI, identity map behavior, request coalescing, and `ETag` revalidation when supported.
- Client-side validation gives immediate feedback, but backend validation remains authoritative.
- Handle 401/403/404 and global API errors intentionally.
- Keep i18n keys and placeholders complete; do not concatenate translated fragments.
- Routes must survive refresh/share/bookmark where the final hosting strategy supports it.

## Build, Hosting, And Cache Rules

- `mvn package` should still produce the final deployable artifact unless the user changes the delivery contract.
- If a frontend module exists, declare `frontend/` in the parent POM and make `webapp` depend on it so Maven builds it before WAR packaging.
- `frontend-maven-plugin` should install reproducible Node/npm, run install/test/build as declared, and leave the real output directory ready for packaging.
- Require package-manager evidence: lockfile present when applicable, reproducible install command (`npm ci` for package-lock unless the repo says otherwise), no manual/stale asset copy, and no tracked local env/secrets.
- `maven-war-plugin` must include the directory that contains `index.html` (`dist`, `build`, or framework-specific browser output).
- Static resource routing must keep `/api/*` as API, static assets under `/assets/*` or `/static/*`, and SPA deep links falling back to `index.html`.
- The bundler base path (`base`, `homepage`, `baseHref`) must match the WAR context path.
- Only hashed/versioned assets should get long immutable cache.
- Root HTML or SPA entrypoint should be revalidated so users can receive new asset names.
- File revving/cache busting is part of correctness, not optional polish, once long cache headers are used.

## Test Rules

- Use test-driven development for implementation slices: failing API/resource test first, then minimal backend, then SPA/frontend tests where available.
- API tests cover status, headers, body, auth, validation, and error mapping.
- Include `Location`, `Link`, media type, `401` vs `403`, refresh, API 404 JSON, and `ETag`/`304` coverage when those features exist.
- Service tests still prove business behavior behind resources.
- Frontend tests cover route/state/form/API client/error/i18n behavior when a runner exists.
- If no frontend runner exists, require a repeatable browser/static-hosting smoke and document the test gap.
- Package/build verification must include the frontend build, WAR output, hashed assets, context-path root, API smoke, API JSON 404, and SPA deep-link smoke when packaging changes.

## Rollback Strategy

- Prefer API-first slices with old JSP routes intact until replacement is ready.
- Keep frontend build integration reversible: separate module/config changes from feature logic.
- Cache changes should be easy to disable if stale asset behavior appears during smoke testing.
