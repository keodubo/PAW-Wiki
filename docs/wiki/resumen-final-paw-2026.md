---
titulo: Ingesta Final PAW 2026
tipo: fuente
fuentes: ["raw/final paw/Clase 8 - FINAL.pdf", "raw/final paw/Correcciones viejas_.docx", "raw/final paw/Optimización REST_ Caching Frontend y Backend.pdf", "raw/final paw/PAW - Apuntes.pdf", "raw/final paw/Ultima clase de PAW - v2 - Apuntes.pdf", "raw/final paw/clases grabadas.docx", "raw/final paw/como esta implementado el tp de ejemplo.docx", "raw/final paw/presentacion-api.pdf", "raw/final paw/presentacion-spring-frontend.pdf", "raw/final paw/resumen clases.docx", "raw/final paw/tp-final nota_10/"]
creado: 2026-07-05
actualizado: 2026-07-05
---

# Ingesta Final PAW 2026

Esta pagina ingiere el lote `raw/final paw/` agregado el 2026-07-05. Refuerza el canon de **TP final**: API REST con hipermedia, autenticacion stateless, SPA como cliente con estado, build frontend integrado al WAR, cache correcto y verificacion de empaquetado.

## Posicion editorial

- El lote confirma que TP final no es "sumar JSON": la entrega se corrige como arquitectura REST + SPA + packaging.
- Las versiones concretas de dependencias y Node que aparezcan en ejemplos son historicas o de muestra. Para implementar, contrastar contra el checkout actual.
- El proyecto `tp-final nota_10/` se usa como ejemplo de patrones, no como canon ciego. Contiene configuracion local sensible; esos archivos no deben publicarse ni copiarse.
- Si una fuente contradice el wiki anterior, priorizar la fuente mas cercana a la ultima clase/correccion de final y dejar explicita la transicion.

## Mapa de fuentes

| Fuente | Foco util |
| --- | --- |
| `Clase 8 - FINAL.pdf` | SPA, modulos JS, bundlers, routing, i18n frontend, testing, cache, file revving, auth y Spring static hosting. |
| `presentacion-api.pdf` | Redisenio REST/HATEOAS: recursos como sustantivos, verbos HTTP, status codes, links, paginacion y auth por headers. |
| `presentacion-spring-frontend.pdf` | Build frontend integrado con Maven, WAR unico, static assets, fallback SPA y base path. |
| `Optimización REST_ Caching Frontend y Backend.pdf` | Cache condicional en API, cache no condicional de assets versionados, identity map/request coalescing en el cliente. |
| `Ultima clase de PAW - v2 - Apuntes.pdf` | Advertencias de correccion: HATEOAS extremo, no `/login`, no `/v1`, 401 vs 403, paginacion en `Link`, endpoints con recursos fuertes. |
| `como esta implementado el tp de ejemplo.docx` | Guia practica Jersey/Spring: recursos `@Path`, DTOs con URIs, vendor media types, Problem Details, JWT, ETag y multipart. |
| `Correcciones viejas_.docx` | Errores graves observados: JSESSIONID, cache incorrecto, URLs invalidas en `Location`, API no RESTful, datos sensibles expuestos, refresh token mal manejado. |
| `resumen clases.docx` | Resumen corto de la clase REST/API: stateless, URNs, headers, media types, JWT y errores comunes. |
| `tp-final nota_10/` | Ejemplo de app completa con frontend, Jersey/Spring, DTOs, filtros, Pinia/Vue, tests y Maven. Usar con cautela por datos locales sensibles. |

## Contrato REST reforzado

- Las URLs identifican recursos, no acciones. Usar sustantivos en plural, jerarquia clara y filtros como query params sobre colecciones.
- El metodo HTTP comunica la operacion: `GET` lee, `POST` crea, `PUT` reemplaza, `PATCH` modifica parcialmente y `DELETE` elimina.
- El codigo HTTP debe alcanzar para distinguir exito/error: `201 Created` con `Location`, `204 No Content` sin body, `400` para validacion, `401` para credenciales ausentes/vencidas, `403` para falta de permisos y `404` para recurso inexistente.
- No envolver listas con objetos artificiales de paginacion. La coleccion devuelve una lista limpia y la navegacion va en headers `Link` (`first`, `prev`, `next`, `last`) y, si hace falta, `X-Total-Count`.
- HATEOAS exige que el cliente siga links que devuelve el servidor: `_links`, URIs en DTOs o headers `Link`. El cliente no deberia construir URLs internas a mano.
- Versionar representaciones con `Accept`/`Content-Type` y vendor media types (`application/vnd.<app>.<recurso>.v1+json`), no con `/v1` en el path.

## Autenticacion stateless

- Una API REST no debe depender de `JSESSIONID` ni estado de sesion del servidor.
- No hace falta un endpoint `/login`: las credenciales pueden viajar como `Authorization: Basic ...` en un request comun y el servidor devuelve tokens en headers.
- Los requests posteriores usan `Authorization: Bearer <access-token>`.
- Si el access token vence, el cliente reintenta con refresh token y recibe un par renovado. Un `403` no se refresca: el usuario esta autenticado pero no autorizado.
- No enviar refresh token en cada response. Regenerarlo cuando se usa o en login reduce superficie de replay.
- No exponer emails, roles administrativos ni flags internos en listados publicos salvo necesidad explicita del producto.

## Jersey, DTOs y errores

- Jersey/JAX-RS puede registrarse como filter para `/api/*` y convivir con Spring Security y JPA. El fallback SPA no debe transformar errores reales de API en HTML.
- Los recursos REST deserializan, validan, delegan a servicios y traducen a HTTP. Los servicios siguen siendo autoridad de negocio.
- No devolver entidades JPA ni proxies lazy. El contrato publico son DTOs/forms con URIs, links y campos seguros.
- Usar `UriInfo`/builders para construir `self`, relaciones, `Location` y links absolutos o correctos bajo el contexto real.
- Las excepciones se mapean a payloads JSON consistentes, idealmente con forma tipo Problem Details: `type`, `title`, `status`, `detail`, `instance`.

## SPA, build y WAR

- En desarrollo pueden existir dos procesos: Vite/React/Angular y backend Maven/Tomcat, conectados por proxy `/api` o CORS.
- En produccion debe haber un unico deployable: el WAR sirve la API, `index.html` y assets estaticos.
- Agregar `frontend/` como modulo Maven, declararlo en el POM padre y hacer que `webapp` dependa de `frontend` (`type=pom`, scope adecuado) para garantizar orden de build.
- `frontend-maven-plugin` instala Node/npm reproducibles, ejecuta install/test/build y deja `dist/` listo.
- `maven-war-plugin` debe incluir el directorio que contiene `index.html`: Vite suele usar `../frontend/dist`, React `../frontend/build`, Angular `../frontend/dist/<app>/browser`.
- El routing recomendado separa responsabilidades: `/api/*` para API, `/assets/*` o `/static/*` al servlet default, y las rutas SPA profundas a `index.html`.
- Configurar el base path del bundler (`base`, `homepage`, `baseHref`) para coincidir con el context path real del WAR.

## Cache y performance

- API dinamica: usar cache condicional (`ETag`, `If-None-Match`, `304 Not Modified`) para reducir payload sin mentir sobre datos cambiantes.
- Assets versionados: usar cache no condicional largo (`public`, `max-age` alto, `immutable`) solo si el nombre incluye hash/file revving.
- `index.html` o la entrada de la SPA no debe quedar cacheada como immutable; debe revalidarse para apuntar a los nuevos nombres hasheados.
- En el cliente, resolver HATEOAS sin N+1 excesivo requiere cache por URI, identity map y deduplicacion de requests en vuelo.
- Lighthouse sirve como chequeo operativo de headers, minificacion, carga inicial y accesibilidad.

## Errores graves a evitar

- Seguir generando `JSESSIONID` en una API que se declara REST stateless.
- Crear endpoints con verbos (`/validate`, `/login`, `/uploadImage`) o rutas que duplican identidad del mismo recurso.
- Devolver siempre `200 OK` y esconder error/status dentro del body.
- Poner paginacion en el body en vez de headers `Link`.
- Devolver `Location` invalido en `201 Created`.
- Usar `PATCH` con mime types para operaciones que no representan una modificacion parcial del recurso. Los vendor media types sirven para representaciones/versiones, no para elegir operaciones escondidas.
- Exponer datos sensibles de usuarios o tokens.
- Hacer que la historia del browser, filtros y back/forward de la SPA queden desconectados de la URL.
- Empaquetar el WAR sin dependencia explicita al frontend y terminar con assets viejos o ausentes.

## Ver tambien

- [[checklist-tp-final-rest-spa]]
- [[api-rest]]
- [[single-page-applications]]
- [[spring-security]]
- [[internacionalizacion]]
- [[ux-flows]]
- [[tp1-vs-tpe2-final]]
- [[manejo-excepciones]]
- [[testing-unitario]]
