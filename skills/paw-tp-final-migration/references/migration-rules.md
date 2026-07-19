# PAW TP Final Migration Rules

## Source Pages

Read current code plus:

- `PAW-Wiki/docs/raw/Enunciado_TPE_Final.pdf` when present (official mandatory delivery contract)
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
- `PAW-Wiki/docs/wiki/resumen-correcciones-tp1-2026-c1.md`
- `PAW-Wiki/docs/wiki/resumen-correcciones-tp2.md`
- The team's actual TP1/TPE2 feedback, when available.
- `skills/paw-frontend-layer/references/frontend-rules.md` from `$paw-frontend-layer` when frontend source changes.

The enunciado defines mandatory final compliance. Current code defines the implementation baseline; class/wiki material explains how to implement the target. If a derived wiki page, sample project, or local compatibility rule conflicts with the enunciado, record the conflict and do not silently call the result compliant.

## Stage Contract

- TP final changes the presentation/API architecture: REST resources plus SPA/frontend.
- Preserve working JSP flows only during reversible migration. The official final deliverable prohibits Spring MVC views/controllers; remove them with user approval before closeout or report a blocking deviation.
- Backend services remain the business source of truth.
- Dependency/tool versions in class PDFs are historical. Use current repo docs and compatibility for actual tooling choices.
- A request like "pasalo a final", "TP final", or "migrar a REST/SPA" is an implementation request unless the user explicitly says audit/plan only. A bare "quiero migrar" is ambiguous between TP2/JPA and TP final/REST+SPA; ask before editing.
- Preserve all existing functionality and server information from TPE2. Prioritize resolving TP1/TPE2 feedback; do not add new functionality unless feedback identifies a missing capability or the user explicitly requests it.

## Official Compliance Matrix

Maintain a live matrix with `requirement`, `implementation evidence`, `test/runtime evidence`, `status`, and `blocker/owner`. At minimum include:

- DDD, Spring dependency injection, layer separation, and Effective Java practices.
- Jersey REST/HATEOAS API and an exclusive SPA frontend using AngularJS or an equivalent framework.
- Spring Security, Hibernate as JPA provider, PostgreSQL, Maven, and Git repository hygiene.
- Backend and frontend unit testing with sufficient behavior coverage.
- Functional parity, preservation of TPE2 information, and closure of all recorded prior feedback.
- Lazy loading/code splitting, minification, cache busting plus unconditional cache, and conditional cache.
- A complete WAR reproducible with only `mvn clean package`, all tables bootstrappable on PostgreSQL, and successful target-server deployment.
- README demo credentials for every access level, exact deployed/delivered commit hash, and online-demo readiness.

Treat REST/HATEOAS non-compliance, technical-requirement failure, commit-hash inconsistency, a commit after the exam start time, or missing online demo as hard-stop final-delivery failures. Never rewrite dates or history to conceal a delivery problem.

Prioritize remediation using the official penalties: functionality/usability and architecture/implementation can each cost up to 4 points; information loss costs 2; testing and delivery form cost 1 each; every unresolved prior correction can cost another point. If a derived wiki table differs, use these official weights.

## Data And Feedback Rules

- Inventory the current PostgreSQL schema, migrations/guards, data assumptions, and the server dataset before schema work.
- Rehearse upgrades against a representative TPE2 snapshot or sanitized copy. Compare critical row counts, ownership/relations, media, auth/roles, and core business records before and after.
- Separately prove clean bootstrap against an empty PostgreSQL schema. Empty-schema creation does not prove upgrade safety; upgrade safety does not prove clean bootstrap.
- Use additive/versioned migrations or the repo's non-destructive guard path. Do not use `create`, drop/recreate, reset, or real-server mutation as a shortcut.
- Turn every actual TP1/TPE2 correction into an acceptance row tied to code and observable test/runtime evidence. Generic correction pages are risk catalogs only.

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
- Keep the old JSP route live until the migrated slice has passed API + SPA verification or the user approves removal. This is an intermediate rollback tactic, never final-delivery evidence.
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
- Final delivery must expose the product exclusively through the SPA + Jersey API. A remaining Spring MVC view/controller is a compliance blocker unless the catedra explicitly accepts the exception.

## Build, Hosting, And Cache Rules

- `mvn package` should still produce the final deployable artifact unless the user changes the delivery contract.
- If a frontend module exists, declare `frontend/` in the parent POM and make `webapp` depend on it so Maven builds it before WAR packaging.
- `frontend-maven-plugin` should install reproducible Node/npm, run install/test/build as declared, and leave the real output directory ready for packaging.
- The production build must produce observable lazy-loaded/code-split chunks and minified output; a framework capable of these features is not evidence that they are enabled.
- Require package-manager evidence: lockfile present when applicable, reproducible install command (`npm ci` for package-lock unless the repo says otherwise), no manual/stale asset copy, and no tracked local env/secrets.
- `maven-war-plugin` must include the directory that contains `index.html` (`dist`, `build`, or framework-specific browser output).
- Static resource routing must keep `/api/*` as API, static assets under `/assets/*` or `/static/*`, and SPA deep links falling back to `index.html`.
- The bundler base path (`base`, `homepage`, `baseHref`) must match the WAR context path.
- Only hashed/versioned assets should get long immutable cache.
- Root HTML or SPA entrypoint should be revalidated so users can receive new asset names.
- File revving/cache busting is part of correctness, not optional polish, once long cache headers are used.
- Verify all four performance requirements independently: lazy loading, minification, cache-busted immutable assets with long unconditional cache, and conditional revalidation (`ETag`/`If-None-Match`/`304` or an equivalent valid policy) for mutable content.

## Test Rules

- Use test-driven development for implementation slices: failing API/resource test first, then minimal backend, then SPA/frontend tests where available.
- API tests cover status, headers, body, auth, validation, and error mapping.
- Include `Location`, `Link`, media type, `401` vs `403`, refresh, API 404 JSON, and `ETag`/`304` coverage when those features exist.
- Service tests still prove business behavior behind resources.
- Frontend tests cover route/state/form/API client/error/i18n behavior when a runner exists.
- If no frontend runner exists, require a repeatable browser/static-hosting smoke and document the test gap.
- For intermediate slices, a documented missing-runner gap may permit progress. For final readiness, frontend unit tests and a working runner are mandatory; smoke testing supplements them and cannot replace them.
- Package/build verification must include the frontend build, WAR output, hashed assets, context-path root, API smoke, API JSON 404, and SPA deep-link smoke when packaging changes.

## Delivery Closeout Rules

- Run `mvn clean package` from a clean checkout without a separate manual frontend build. Inspect the resulting WAR rather than trusting Maven success alone.
- Verify an empty PostgreSQL bootstrap and a non-destructive upgrade rehearsal from TPE2 data as separate gates.
- Deploy the exact WAR to the target Application Container and, when access exists, the catedra server. Smoke root/deep link/assets, auth, API success/error, and representative role flows.
- Keep only non-sensitive, seeded demo credentials in `README.md`, with at least one user for every role/access level. Never publish real user or infrastructure secrets.
- Audit tracked files for IDE metadata, dependencies, Maven/frontend output, logs, secrets, and machine-specific configuration.
- Record `git rev-parse HEAD`, build/deploy from that commit, and verify that the hash sent to the catedra identifies the deployed artifact exactly.
- Review the commit/contribution trail so every team member can explain their work. Do not squash, rewrite, or fabricate history to disguise individual contributions or timing.
- Treat the demo as a gate: prepare the deployed URL, role credentials, core scenarios, known limitations, and team ownership/explanation of the code.

## Rollback Strategy

- Prefer API-first slices with old JSP routes intact until replacement is ready.
- Before final closeout, remove temporary JSP/MVC rollback routes only with user approval and after route/mail/security/bookmark verification; if removal is not authorized, report final compliance as blocked.
- Keep frontend build integration reversible: separate module/config changes from feature logic.
- Cache changes should be easy to disable if stale asset behavior appears during smoke testing.
