---
titulo: PAW Unidad 11 - API REST
tipo: fuente
fuentes: [raw/apuntes.txt, raw/pdfs/PAW - Apuntes.pdf]
creado: 2026-05-11
actualizado: 2026-05-11
---

# PAW Unidad 11 - API REST

Esta unidad define el contrato HTTP para el TP final. REST no es "devolver JSON": es disenar recursos, representaciones, links, status codes, headers y autenticacion stateless para que una SPA u otro cliente puedan consumir la aplicacion.

## Idea central

- REST significa Representational State Transfer.
- El servidor transmite representaciones de recursos, no pantallas.
- La SPA decide como mostrar la informacion.
- El diseno se apoya en HTTP: statelessness, verbos, headers, content negotiation y status codes.
- La API debe ser agnostica del cliente especifico.

## Recursos y URNs

- Los recursos se nombran con sustantivos.
- Colecciones fuertes viven en paths como `/users`, `/posts`, `/issues`.
- Un recurso individual vive en `/users/{id}`.
- Entidades debiles pueden vivir bajo la entidad fuerte: `/users/{id}/addresses`.
- Paths contextuales como `/me` son sospechosos porque dependen del solicitante y no son una URN universal.
- El path debe mapear recursos, no acciones.

## Verbos HTTP

- `GET /users` lista usuarios.
- `POST /users` crea un usuario.
- `PUT /users/{id}` reemplaza o actualiza segun contrato definido.
- `DELETE /users/{id}` elimina ese recurso si corresponde.
- `/users/{id}/validate` huele mal porque `validate` es accion, no recurso.
- La accion viaja en el verbo HTTP; el path identifica el recurso.

## Status codes

| Caso | Codigo esperado | Nota |
| --- | --- | --- |
| Lectura exitosa con cuerpo | `200 OK` | Devuelve representacion. |
| Operacion exitosa sin cuerpo | `204 No Content` | Comun para delete. |
| Creacion | `201 Created` | Debe incluir `Location`. |
| No autenticado | `401` | No se sabe quien solicita. |
| Sin permiso | `403` | Se conoce al usuario, pero no puede operar. |
| No existe | `404` | Recurso ausente. |
| Metodo incorrecto | `405 Method Not Allowed` | La URN existe, pero ese verbo no aplica. |

## Paginacion

- El body deberia contener la data principal.
- Metadata de navegacion puede viajar en headers.
- El apunte privilegia header `Link`.
- Relaciones como `first`, `prev`, `next` y `last` describen navegacion.
- Esto evita mezclar presentacion o metadata arbitraria dentro del payload.

## Hipervinculos

- Los recursos deberian enlazar recursos relacionados.
- En vez de embeber issues completos dentro de usuario, se linkea la coleccion relacionada.
- `self` identifica la URL del propio recurso.
- Links evitan que el cliente conozca detalles internos como FKs o nombres de parametros.
- Tambien reducen caminos duplicados para modificar la misma entidad.

## Evitar embedding excesivo

- Si se embeben direcciones dentro de usuario y tambien existen endpoints propios para direcciones, aparecen dos formas de modificar lo mismo.
- Eso complica autorizacion, validacion y consistencia.
- Una API REST limpia prefiere una URN clara por recurso y links entre recursos.

## Negociacion de contenido

- Versionar como `/v1/users` duplica la identidad del recurso.
- El usuario es el mismo; lo que cambia es la representacion.
- HTTP resuelve representaciones con `Accept` y `Content-Type`.
- MIME types propios pueden expresar version y formato, por ejemplo `application/vnd.userlist.v1+json`.
- `Accept` indica lo que el cliente espera recibir.
- `Content-Type` indica lo que el cliente esta enviando.
- Este enfoque permite evolucionar representaciones sin partir el mapa de recursos.

## Descriptor de API

- `GET /` puede devolver un descriptor de recursos y parametros disponibles.
- No es obligatorio para el final, pero ayuda a una API navegable.
- Encaja con la idea de que el cliente conoce HTTP, verbos y links, no implementacion interna.

## Autenticacion stateless

- No hay sesiones server-side tradicionales.
- Las credenciales viajan como metadata HTTP.
- Un request inicial puede usar `Authorization: Basic ...` para obtener tokens.
- Luego se usa `Authorization: Bearer {token}`.
- JWT aparece como token autocontenido con informacion de identidad y validez.
- La autenticacion deberia poder ocurrir contra endpoints sin crear un verbo `/login` como accion fuera del modelo de recursos.

## Password reset y content-types

- El recupero de password puede modelarse como operacion sobre recursos.
- Si cambia la representacion o intencion, puede distinguirse con `Content-Type` propio.
- La clase enfatiza usar content-types especificos cuando el contrato lo necesita.
- Lo importante es evitar endpoints accion y sostener un contrato HTTP coherente.

## Imagenes

- La API no tiene que devolver siempre el original.
- Puede devolver variantes segun ancho/alto solicitado.
- Esto evita transferir imagenes grandes cuando el cliente necesita miniaturas.
- El recurso sigue siendo imagen; la representacion cambia segun parametros/headers.

## Riesgos a auditar en TP final

- Paths con verbos.
- Versionado por URL sin justificacion.
- Status codes genericos con errores escondidos en strings.
- Sesiones server-side disfrazadas de API REST.
- Embedding que duplica caminos de mutacion.
- Paginacion acoplada a una UI concreta en vez de headers/links.

## Ver tambien

- [[resumen-apuntes]]
- [[api-rest]]
- [[single-page-applications]]
- [[http-y-sesiones]]
- [[manejo-imagenes]]
- [[paw-unidad-10-single-page-applications]]
