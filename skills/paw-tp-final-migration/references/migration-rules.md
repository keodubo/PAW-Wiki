# PAW TP Final Migration Rules

## Source Pages

Read current code plus:

- `PAW-Wiki/docs/wiki/resumen-clases-paw-2026.md`
- `PAW-Wiki/docs/wiki/tp1-vs-tpe2-final.md`
- `PAW-Wiki/docs/wiki/api-rest.md`
- `PAW-Wiki/docs/wiki/single-page-applications.md`
- `PAW-Wiki/docs/wiki/spring-security.md`
- `PAW-Wiki/docs/wiki/internacionalizacion.md`
- `PAW-Wiki/docs/wiki/ux-flows.md`
- `PAW-Wiki/docs/wiki/manejo-excepciones.md`
- `PAW-Wiki/docs/wiki/testing-unitario.md`

## Stage Contract

- TP final changes the presentation/API architecture: REST resources plus SPA/frontend.
- Do not silently remove working JSP flows unless the user asked for replacement and rollback/parallel routing is clear.
- Backend services remain the business source of truth.
- Dependency/tool versions in class PDFs are historical. Use current repo docs and compatibility for actual tooling choices.

## API Rules

- Model resources as nouns and stable URIs.
- Use HTTP verbs for actions where possible.
- Return correct status codes: `201` + `Location` for creation, `204` for successful delete/no body, `400` for validation, `401/403/404` deliberately.
- Use DTOs for API representations; do not leak domain entities, persistence entities, lazy proxies, or internal fields.
- Keep validation and error payloads consistent enough for the SPA to consume.
- Document pagination and links/headers when a list endpoint needs navigation.
- Version representations through media/content negotiation when required; avoid path versioning unless the project chose it explicitly.

## Auth And Security Rules

- API auth should be stateless by design.
- Compare token/header and cookie approaches before implementing.
- LocalStorage has XSS risk; cookies have automatic-send and overhead/CSRF trade-offs.
- Spring Security still owns authorization rules; do not recreate route access logic inside every resource.
- Frontend visibility checks are UX only, not authorization.

## SPA Rules

- Keep a clear API client boundary; components should not scatter raw fetch/HTTP details everywhere.
- Treat state as a single source of truth. Choose local state/store complexity based on real need.
- Client-side validation gives immediate feedback, but backend validation remains authoritative.
- Handle 401/403/404 and global API errors intentionally.
- Keep i18n keys and placeholders complete; do not concatenate translated fragments.
- Routes must survive refresh/share/bookmark where the final hosting strategy supports it.

## Build, Hosting, And Cache Rules

- `mvn package` should still produce the final deployable artifact unless the user changes the delivery contract.
- If a frontend module exists, ensure Maven runs it before WAR packaging.
- Static resources need correct Spring resource handling.
- Only hashed/versioned assets should get long immutable cache.
- Root HTML or SPA entrypoint should be revalidated so users can receive new asset names.
- File revving/cache busting is part of correctness, not optional polish, once long cache headers are used.

## Test Rules

- API tests cover status, headers, body, auth, validation, and error mapping.
- Service tests still prove business behavior behind resources.
- Frontend tests cover component/state/form behavior when a runner exists.
- Package/build verification must include the frontend build and WAR output when packaging changes.

## Rollback Strategy

- Prefer API-first slices with old JSP routes intact until replacement is ready.
- Keep frontend build integration reversible: separate module/config changes from feature logic.
- Cache changes should be easy to disable if stale asset behavior appears during smoke testing.
