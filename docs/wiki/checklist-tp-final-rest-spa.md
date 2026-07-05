---
titulo: Checklist TP Final REST + SPA
tipo: sintesis
fuentes: ["raw/final paw/presentacion-api.pdf", "raw/final paw/presentacion-spring-frontend.pdf", "raw/final paw/Optimización REST_ Caching Frontend y Backend.pdf", "raw/final paw/Ultima clase de PAW - v2 - Apuntes.pdf", "raw/final paw/como esta implementado el tp de ejemplo.docx", "raw/final paw/Correcciones viejas_.docx", "raw/final paw/tp-final nota_10/"]
creado: 2026-07-05
actualizado: 2026-07-05
---

# Checklist TP Final REST + SPA

Usar esta pagina como lista de entrega/auditoria para TP final. No reemplaza al checkout: cada item debe validarse contra codigo, tests y packaging reales.

## API REST

- Recursos como sustantivos en plural; acciones fuera del path.
- Jerarquia clara solo cuando hay entidad debil. Si el recurso es fuerte, tiene su propia URN.
- `GET` sin body; filtros, busqueda, sort y paginacion como query params de coleccion.
- `POST` crea y devuelve `201 Created` con `Location` valido.
- `DELETE` exitoso sin payload devuelve `204 No Content`.
- `PUT` se reserva para reemplazo idempotente; no usarlo si dispara efectos secundarios no idempotentes como mails o creacion de otras filas.
- `PATCH` modifica campos reales del recurso, no inventa operaciones escondidas por mime type.
- DTOs publicos con links/URIs; no entidades JPA, ids internos sueltos ni proxies lazy.
- HATEOAS visible en `_links`, URIs relacionadas o headers `Link`.
- Paginacion por `Link` (`first`, `prev`, `next`, `last`) y, si hace falta, `X-Total-Count`; no envelope artificial en el body.
- Content negotiation con vendor media types para versiones de representacion; evitar `/v1` en rutas.

## Auth y seguridad

- API sin sesion server-side ni dependencia de `JSESSIONID`.
- Primer request puede autenticar con `Authorization: Basic ...`; responses devuelven tokens por headers.
- Requests normales usan `Authorization: Bearer <access-token>`.
- `401` significa credencial ausente/invalida/vencida y puede disparar refresh; `403` significa usuario identificado sin permiso y no debe refrescar.
- Refresh token no viaja en cada response ni se trata como dato publico.
- Spring Security sigue siendo autoridad de autorizacion; el frontend solo oculta/expone UI por UX.
- Listados publicos no exponen emails, admin flags, tokens, keys ni detalles internos innecesarios.
- CORS y headers expuestos deben declararse explicitamente si el frontend necesita leer `Location`, `Link`, `ETag`, `X-Access-Token` o `X-Refresh-Token`.

## Errores y validacion

- Errores JSON consistentes con `status`, `title/detail` e `instance`.
- Bean Validation/Jersey validation para inputs; backend es autoridad aunque exista validacion cliente.
- `400`, `401`, `403`, `404`, `405`, `406`, `415` deben tener semantica deliberada.
- No responder HTML para errores de `/api/*`.
- No filtrar excepciones a texto tecnico, stack traces o clases Java.

## SPA y frontend

- Componentes enfocados en UI; servicios/API client concentran HTTP.
- Store o capa de estado como fuente de verdad cuando hay estado compartido.
- Formularios con validacion cliente para feedback inmediato y mensajes backend bien mapeados.
- Router sincronizado con URL: refresh, deep links, bookmarks, filtros y back/forward funcionan.
- Pantallas de 401, 403, 404, loading, empty y error global existen y son coherentes.
- i18n frontend con catalogos por idioma, placeholders, pluralizacion y formato de fecha/hora/moneda.
- Cache por URI/identity map y deduplicacion de requests en vuelo cuando HATEOAS generaria N+1 HTTP.

## Build, WAR y hosting

- `frontend/` declarado como modulo Maven.
- `frontend-maven-plugin` instala Node/npm reproducible y ejecuta install/test/build.
- El POM padre conoce el modulo frontend.
- `webapp` declara dependencia al modulo frontend para garantizar orden de build.
- `maven-war-plugin` incluye el directorio real que contiene `index.html`.
- `mvn clean package` genera un WAR unico con API + SPA + assets.
- Routing separado: `/api/*` API, `/assets/*`/`/static/*` static, rutas SPA a `index.html`.
- Fallback SPA no traga errores reales de API.
- Bundler configurado con el context path real (`base`, `homepage`, `baseHref`).

## Cache y optimizacion

- API dinamica con cache condicional cuando corresponde: `ETag`, `If-None-Match`, `304`.
- Assets hasheados con cache no condicional largo e `immutable`.
- `index.html` revalidado o con cache corto.
- Minificacion, tree shaking y file revving activos en build productivo.
- Lighthouse o verificacion equivalente revisa headers, payload inicial, accesibilidad y buenas practicas.

## Tests y verificacion

- API tests cubren status, body, headers, `Location`, `Link`, media types, auth, validacion y errores.
- Tests de auth distinguen 401 vs 403 y refresh.
- Tests/cache o smoke verifican `ETag`/`304` y headers de assets cuando se implementa cache.
- Frontend tests cubren stores/composables, formularios, rutas, errores e i18n si hay runner.
- Packaging check: inspeccionar que el WAR contiene `index.html`, JS/CSS/assets y clases backend.
- Smoke minimo: ruta raiz bajo context path, asset hasheado, API valida, API 404 JSON y ruta SPA profunda.

## Ver tambien

- [[resumen-final-paw-2026]]
- [[api-rest]]
- [[single-page-applications]]
- [[spring-security]]
- [[internacionalizacion]]
- [[ux-flows]]
- [[configuracion-maven]]
- [[manejo-excepciones]]
- [[testing-unitario]]
