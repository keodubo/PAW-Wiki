---
titulo: API REST
tipo: concepto
fuentes: [raw/apuntes.txt]
creado: 2026-04-13
actualizado: 2026-04-13
---

# API REST

Esta pagina resume la **unidad 11**. Es material de TPE2/final y **fuera del foco de TP1**.

## REST no es "CRUD en JSON"

- REST significa **Representational State Transfer**.
- La API ya no entrega presentacion; entrega informacion y el cliente decide como mostrarla.
- El diseno se apoya fuerte en HTTP: statelessness, verbs, headers, content negotiation y status codes.

## Recursos y URNs

- Los recursos deberian modelarse como sustantivos, no acciones.
- Una estructura sana se apoya en paths predecibles como:
  - `/users`
  - `/users/{id}`
  - `/users/{id}/addresses`
- El ejemplo de clase marca `/me` como mala senal si rompe la idea de URN estable e independiente del contexto.
- Las entidades debiles deberian vivir debajo del recurso fuerte del que dependen.

## Operaciones via verbos HTTP

- Las acciones se expresan con verbos HTTP, no metiendolas en el path.
- Ejemplos:
  - `GET /users`
  - `POST /users`
  - `PUT /users/{id}`
  - `DELETE /users/{id}`
- Un path como `/users/{id}/validate` ya huele a no-REST porque `validate` no es un recurso.

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

## Negociacion de contenido

- Versionar recursos en el path (`/v1/users`) se presenta como mala practica.
- El cambio de version es un problema de **representacion**, no de identidad del recurso.
- La alternativa recomendada es negociar por headers `Accept` y `Content-Type`.
- Se mencionan MIME types propios del estilo:
  - `application/vnd.userlist.v1+json`
  - `application/vnd.userlist.v2+json`

## Autenticacion en APIs

- En este modelo desaparecen sesiones y cookies tradicionales del lado servidor.
- La autenticacion viaja por metadata HTTP, por ejemplo `Authorization`.
- Los apuntes introducen JWT en headers para requests posteriores.
- La idea central es mantener el modelo stateless tambien en la autenticacion.

## Casos practicos de clase

- Recupero de password puede modelarse con distintos `Content-Type` segun la operacion.
- Se recomienda usar content types propios cuando cambia el contrato del recurso.
- Para imagenes, se marca como buena practica devolver variantes adaptadas al tamano necesario y no siempre el original.

## Ver tambien

- [[http-y-sesiones]]
- [[single-page-applications]]
- [[tp1-vs-tpe2-final]]
- [[manejo-imagenes]]
- [[resumen-apuntes]]
