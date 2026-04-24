---
titulo: Informe Tecnico de Implementaciones desde f81eb49
tipo: fuente
fuentes: [raw/2026-04-19_informe-implementaciones-desde-f81eb49_v1.pdf, reports/2026-04-19_informe-implementaciones-desde-f81eb49_v1.tex, reports/2026-04-19_informe-implementaciones-desde-f81eb49_v1.pdf]
creado: 2026-04-20
actualizado: 2026-04-22
---

# Informe Tecnico de Implementaciones desde f81eb49

Ingesta estructurada del informe tecnico shareable generado el `2026-04-19`. La copia inmutable en `raw/` y el PDF publicado en `reports/` son el mismo archivo byte a byte; el `.tex` conserva la fuente editable del reporte. El valor de esta pagina no es solo enlazar artefactos, sino dejar fijado que cuenta el informe y como conviene leerlo dentro del wiki.

## Alcance y criterio de lectura

- El documento tiene `47` paginas y `12` capitulos.
- Reconstruye el trabajo incorporado desde el commit base `f81eb49f7c6a90f983f67fd831cf13872ce5326c`; dentro de sus propias fuentes explicita el historial Git entre `f81eb49` y `72a5d7d8`.
- Cruza cinco insumos: historial Git, wiki local, capturas del tablero de Plane, logs locales y el codigo actual en `models`, `persistence`, `services` y `webapp`.
- No atribuye todas las tarjetas del tablero con la misma certeza: distingue entre features nacidas en el rango, features cerradas ahi y refinamientos/hardening sobre capacidades previas.

## Bloques funcionales que el informe deja ordenados

| Bloque | Que fija el informe |
| --- | --- |
| Auth y ciclo de cuenta | Spring Security, claim/reset, login por email, registro unificado, verificacion por mail, autologin, remember-me y cambio de idioma endurecidos. |
| Perfiles, reviews y listas personales | perfiles navegables y paginables, restaurantes guardados, avatar por defecto y reviews verificadas con respaldo real de persistencia. |
| Dashboard owner y moderacion admin | dashboard owner, asignacion de restaurantes huerfanos, aprobacion/rechazo con reenvio a pending, wizard owner/admin, preview y separacion de formularios de restaurante, menu y reservas. |
| Sistema de reservas | diez fases completas: modelo, configuracion, disponibilidad, creacion reviewer, gestion reviewer, gestion owner, scheduler, mails, reviews verificadas ligadas a reservas y hardening final. |
| Mailing, logs y background | mails de negocio fuera de reservas, locale del destinatario y evidencia runtime en logs, no solo en tests o diff Git. |
| Explore, paginacion y listas | preservacion mutua de filtros y query, `pageSize` configurable, paginacion mas rica y lectura cauta de stories cuyo origen puede ser anterior al rango. |
| Hardening transversal | acceso declarativo, validacion inline, limites de request/input, correcciones de persistencia y mapeo, error handling, logging y packaging. |

## Evidencia fuerte que aporta

- Deja una tabla de commits estructurantes para ubicar rapido hitos como auth base (`f497af22`), reset de password (`327aeeb7`), moderacion/admin (`03bf6161`), base de reservas (`37e7997e`), creacion de reservas (`03fd92b8`), gestion owner (`5387190a`), scheduler (`155ab1b2`), mails transaccionales (`3b6c5221`), reviews verificadas (`2e16c230`), registro unificado (`4e884b77`) y verificacion por mail (`10915c6a`).
- Explica detalles internos que no se ven en una lista plana de commits: generacion segura del token de confirmacion, calculo de slots, reasignacion de mesas owner, idempotencia de mails sobre `reservation_notifications` y la validacion backend de reviews verificadas.
- Documenta el problema real encontrado durante la implementacion de reviews verificadas en PostgreSQL y deja asentado el cierre final: `schema-postgres.sql` termina usando `CREATE UNIQUE INDEX IF NOT EXISTS reviews_reservation_id_unique ON reviews(reservation_id)` en vez de un `ALTER TABLE ... ADD CONSTRAINT IF NOT EXISTS` incompatible.
- Cierra con una matriz de stories del tablero usando confianza `Alta`, `Media` y `Baja`, para no sobreafirmar que toda capacidad visible en Sprint 2 haya nacido exactamente en este rango.

## QA manual y evidencia operativa

- El informe registra una corrida manual local sobre `http://localhost:8080` con dos cuentas nuevas (`reviewer_0419223402` y `owner_0419223402`) y dos cuentas seed (`kansas` y `admin_forkd`).
- Los checks marcados como `OK` fueron: registro reviewer, registro owner, guardado de restaurante, creacion de reserva, confirmacion owner, review verificada y moderacion admin.
- Tambien deja evidencia complementaria en datos y logs: relacion en `saved_restaurants`, reserva `id=12`, review con `reservation_id=12` y lineas de log para creacion de reserva, confirmacion owner y aprobacion admin.
- El propio informe aclara una intervencion local puntual para mover la reserva de QA a `COMPLETED` y asi destrabar la review verificada dentro de la misma sesion manual, sin esperar el scheduler natural.

## Artefactos y procedencia

- `raw/2026-04-19_informe-implementaciones-desde-f81eb49_v1.pdf` es la copia inmutable usada para esta ingesta.
- `reports/2026-04-19_informe-implementaciones-desde-f81eb49_v1.pdf` es el render shareable del mismo documento y coincide byte a byte con el PDF crudo de `raw/`.
- `reports/2026-04-19_informe-implementaciones-desde-f81eb49_v1.tex` es la fuente editable que mejor preserva la estructura del informe para futuras relecturas o regeneracion.
- `reports/assets/manual-qa/` conserva las capturas usadas como evidencia visual del QA manual documentado por el propio reporte.
- Los derivados `.aux`, `.log`, `.out` y `.toc` no eran fuentes de conocimiento independientes y quedaron eliminados en el cleanup del `2026-04-20`.

## Como conviene leer este nodo hoy

- No reemplaza el estado actual del repo: es una reconstruccion historica con corte documental en torno al `2026-04-19`.
- Tampoco invalida el backlog posterior de deploy: `[[2026-04-20_bugs-deploy-lote-0-y-plan-remediacion_v1]]` funciona como continuidad operativa del cierre documentado aqui.
- Su utilidad principal es dejar trazado, con mejor granularidad que un `git log`, que partes del producto quedaron efectivamente cerradas y cuales solo pueden atribuirse como refinamiento o consolidacion.

## Relacion con el wiki

- `[[2026-04-17_cierre-implementacion-reservas_v1]]` deja el cierre operativo del subsistema de reservas que este informe reutiliza como una de sus fuentes clave.
- `[[resumen-notas-sprint-2]]` conserva el checklist de cierre del sprint y ayuda a leer este documento como reconstruccion posterior, no como backlog vivo.
- `[[2026-04-20_bugs-deploy-lote-0-y-plan-remediacion_v1]]` registra el backlog de bugs detectado contra deploy al dia siguiente; conviene leerlo como continuidad operativa, no como desmentida retroactiva del informe.

## Ver tambien

- [[2026-04-17_cierre-implementacion-reservas_v1]]
- [[resumen-notas-sprint-2]]
- [[2026-04-20_bugs-deploy-lote-0-y-plan-remediacion_v1]]
