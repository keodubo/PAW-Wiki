---
titulo: API REST
tipo: concepto
fuentes: [raw/apuntes.txt, raw/PAW - clases 9 y 10 (TP final).pdf, "raw/final paw/presentacion-api.pdf", "raw/final paw/Ultima clase de PAW - v2 - Apuntes.pdf", "raw/final paw/resumen clases.docx", "raw/final paw/como esta implementado el tp de ejemplo.docx", "raw/final paw/Correcciones viejas_.docx", "raw/final paw/Optimización REST_ Caching Frontend y Backend.pdf"]
creado: 2026-04-13
actualizado: 2026-07-05
---

# API REST

Esta pagina resume la API REST para **TP final**. Es material fuera del foco de TP1 y distinto de la migracion TP2 a JPA.

Nota: la clase menciona dependencias Jersey/JAX-RS historicas. El criterio vigente es el diseno REST y la integracion con el stack actual, no copiar versiones viejas.

## REST no es "CRUD en JSON"

- REST significa **Representational State Transfer**.
- La API ya no entrega presentacion; entrega informacion y el cliente decide como mostrarla.
- El diseno se apoya fuerte en HTTP: statelessness, verbs, headers, content negotiation y status codes.
- Para la entrega final, la API aparece porque una SPA necesita un contrato HTTP consumible por un cliente separado.
- La API tambien abre integraciones con terceros o mobile; por eso no debe depender de detalles de JSP/server-side views.

## Recursos y URNs

- Los recursos deberian modelarse como sustantivos, no acciones.
- Una estructura sana se apoya en paths predecibles como:
  - `/users`
  - `/users/{id}`
  - `/users/{id}/addresses`
- El ejemplo de clase marca `/me` como mala senal si rompe la idea de URN estable e independiente del contexto.
- Las entidades debiles deberian vivir debajo del recurso fuerte del que dependen.
- Si un recurso es fuerte, no debe aparecer anidado bajo otro recurso fuerte solo por conveniencia de UI. Usar filtros o links desde colecciones.
- Mezclar singular/plural, camelCase/snake_case y endpoints con verbos obliga al cliente a memorizar casos especiales y baja madurez REST.

## Operaciones via verbos HTTP

- Las acciones se expresan con verbos HTTP, no metiendolas en el path.
- Ejemplos:
  - `GET /users`
  - `POST /users`
  - `PUT /users/{id}`
  - `DELETE /users/{id}`
- Un path como `/users/{id}/validate` ya huele a no-REST porque `validate` no es un recurso.
- No reducir todo a `GET`/`POST`: `PUT` debe ser idempotente y no conviene cuando dispara mails, crea filas auxiliares o produce efectos no repetibles.
- `PATCH` debe modificar campos reales del recurso. No usar vendor media types como selector de operaciones inconexas sobre el mismo path.

## Semantica de status codes

- `200 OK`: operacion exitosa con representacion
- `201 Created`: creacion exitosa, idealmente con `Location`
- `204 No Content`: operacion exitosa sin payload
- `401`: no se sabe quien es el solicitante
- `403`: se sabe quien es, pero no tiene permisos
- `404`: recurso no encontrado
- `405`: el metodo no aplica a ese recurso

## Paginacion e hipervinculos

- El payload deberia concentrarse en los datos, no en metadata arbitraria de presentacion.
- Para paginacion, la clase privilegia headers `Link` con relaciones como `first`, `prev`, `next`, `last`.
- Las respuestas deberian incluir `links` hacia recursos relacionados en vez de embeber todo el grafo.
- Eso mantiene URNs unicas y evita exponer multiples maneras inconsistentes de manipular lo mismo.
- Un envelope `{ items, pagination }` para listas se considera peor que una coleccion limpia mas headers `Link`.
- El cliente deberia descubrir `next`, `prev`, `first` y `last` por headers o links, no reconstruir URLs con conocimiento interno.
- Si HATEOAS genera N+1 HTTP en el cliente, resolverlo con cache por URI, request coalescing y cache condicional, no embebiendo todo el grafo sin criterio.

## Negociacion de contenido

- Versionar recursos en el path (`/v1/users`) se presenta como mala practica.
- El cambio de version es un problema de **representacion**, no de identidad del recurso.
- La alternativa recomendada es negociar por headers `Accept` y `Content-Type`.
- Se mencionan MIME types propios del estilo:
  - `application/vnd.userlist.v1+json`
  - `application/vnd.userlist.v2+json`
- Los media types versionan representaciones. No deben convertir un unico `PATCH /users/{id}` en multiples operaciones secretas que el cliente solo conoce por documentacion externa.

## Autenticacion en APIs

- En este modelo desaparecen sesiones y cookies tradicionales del lado servidor.
- La autenticacion viaja por metadata HTTP, por ejemplo `Authorization`.
- Los apuntes introducen JWT en headers para requests posteriores.
- La idea central es mantener el modelo stateless tambien en la autenticacion.
- La clase contrasta tokens por header con cookies: cookies se mandan automaticamente y pueden agregar overhead/riesgo; localStorage tambien tiene riesgo ante XSS. La decision final debe explicitar trade-offs.
- La ultima clase refuerza que no hace falta `/login`: `Authorization: Basic ...` puede autenticar un request y devolver tokens por headers.
- `401` es credencial ausente, invalida o vencida; `403` es usuario identificado sin permiso. Refrescar token ante `403` es conceptualmente incorrecto.
- El refresh token no deberia viajar en cada response; se rota al usarse o al iniciar sesion para reducir replay.

## Jersey/JAX-RS en la clase

- Jersey reemplaza controllers Spring MVC para los recursos REST, pero Spring puede seguir resolviendo seguridad, servicios y persistencia.
- El filtro Jersey debe ordenarse detras de Spring Security y, si aplica JPA, del filtro que mantiene contexto de persistencia.
- Recursos usan `@Path`, verbos como `@GET`/`@POST`/`@DELETE`, DTOs y `Response`.
- Creaciones deberian devolver `201 Created` con header `Location`.
- Deletes exitosos sin body deberian devolver `204 No Content`.
- Si Jersey se registra como filter, el fallback SPA no debe tragarse errores reales de `/api/*`: una API 404 debe seguir siendo JSON/status API, no `index.html`.
- DTOs y forms son el contrato publico; no exponer entidades JPA, proxies lazy, emails masivos ni flags internos innecesarios.
- `UriInfo`/builders ayudan a generar `self`, links y `Location` validos bajo el context path real.

## Cache y semantica HTTP

- La semantica REST impacta cache: un `200 OK` falso para errores o una URL inestable impide que clientes, proxies y browser cacheen correctamente.
- Para JSON dinamico, usar cache condicional cuando corresponda: `ETag`, `If-None-Match` y `304 Not Modified`.
- Para imagenes o uploads de usuario, cache inmutable solo es seguro si la URL cambia cuando cambia el contenido.
- Para assets estaticos de SPA, ver [[single-page-applications#Cache y file revving]] y [[checklist-tp-final-rest-spa]].

## Casos practicos de clase

- Recupero de password puede modelarse con distintos `Content-Type` segun la operacion.
- Se recomienda usar content types propios cuando cambia el contrato del recurso.
- Para imagenes, se marca como buena practica devolver variantes adaptadas al tamano necesario y no siempre el original.

## Ver tambien

- [[http-y-sesiones]]
- [[resumen-final-paw-2026]]
- [[checklist-tp-final-rest-spa]]
- [[resumen-clases-paw-2026]]
- [[single-page-applications]]
- [[tp1-vs-tpe2-final]]
- [[manejo-imagenes]]
- [[resumen-apuntes]]
