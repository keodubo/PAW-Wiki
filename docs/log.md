# Log del Wiki PAW

Registro cronologico de actividad del wiki.

---

## [2026-05-11] ingest | apuntes unidades 8 en adelante

- Reextraidas del PDF `docs/raw/pdfs/PAW - Apuntes.pdf` las paginas 68-96 para ampliar la cobertura de unidades 8-11.
- Actualizado `wiki/resumen-apuntes.md` con una reingesta detallada de AOP/proxies/transacciones, Hibernate/JPA, SPA y API REST.
- Creadas 11 paginas dedicadas por unidad: `wiki/paw-unidad-01-introduccion-scrum-mvp.md` a `wiki/paw-unidad-11-api-rest.md`; las unidades 8-11 quedan especialmente desarrolladas.
- Actualizado `docs/index.md` para dejar asentado que las unidades 9-11 ya no quedan solo como fuera de foco, sino como base TP2/final navegable desde la fuente principal.
- Sin cambios en `docs/raw/`; el PDF y las fuentes crudas quedan inmutables.

## [2026-04-29] docs | tono público y guías actualizadas

- Pulidos `README.md`, `docs/CLAUDE.md`, `docs/examples/README.md` y
  `skills/README.md` para mejorar el tono público del repositorio y reducir
  lenguaje coloquial.
- Conservada la estructura stage-aware por `TP1`, `TP2` y `TP final`, con
  `$paw-feature-master` como entrada recomendada para agentes.
- Sin cambios en `docs/raw/`, `docs/wiki/` ni `docs/private/`.

## [2026-04-27] docs | decisión de copy en reseña verificada

- Marcado como resuelto en `docs/wiki/2026-04-24_correcciones-sprint-2_v1.md` el item de reemplazar "verificado" en reviews.
- Decisión de producto registrada: se conserva "Reseña verificada" / "Verified review" para reviews vinculadas a reserva, en lugar de "Comió en este lugar".
- Actualizado `docs/index.md` para reflejar que el tracker también conserva decisiones de producto.

## [2026-04-27] docs | onboarding con actualizacion automatizable de wiki

- Agregada `docs/examples/actualizar-wiki.md` para que un agente revise `origin/main`, compare `HEAD..origin/main` y haga `git pull --ff-only` solo si `PAW-Wiki` esta limpia.
- Actualizados `README.md`, `docs/CLAUDE.md`, `docs/index.md`, `docs/examples/README.md`, `setup-local`, `wiki-dentro-repo-paw`, `uso-con-agente`, `instalar-skills`, `checklist-publicacion`, `troubleshooting` y `skills/README.md` para hacer explicito el flujo recomendado: clonar dentro del repo PAW, ignorar `/PAW-Wiki/`, revisar actualizaciones remotas antes de usar la wiki y reinstalar skills si `skills/` cambio.
- Regla operativa nueva: un agente puede actualizar automaticamente con `git pull --ff-only` solo si no hay cambios locales; si hay cambios, debe mostrar `git status`/`git diff` y pedir decision.

## [2026-04-27] docs | README y CLAUDE alineados por etapa

- Actualizados `README.md`, `docs/CLAUDE.md`, `docs/examples/*.md` y `skills/README.md` para reflejar que la wiki trabaja por etapa: TP1, TP2 y TP final.
- Reforzado que `[[resumen-clases-paw-2026]]` y `[[tp1-vs-tpe2-final]]` son lecturas base para stack, migraciones y uso de agentes.
- Documentado en guias publicas que las versiones de dependencias en PDFs viejos son historicas y no deben tratarse como recomendacion vigente sin contrastar el checkout/enunciado actual.
- Sin cambios en `docs/raw/`; esta fue una sincronizacion documental posterior a la ingesta de clases.

## [2026-04-27] ingest | clases PAW nuevas por etapa

- Ingeridos 11 PDFs nuevos de `docs/raw/` que comienzan con `PAW`, separados en TP1, TP2 y TP final.
- Creada `wiki/resumen-clases-paw-2026.md` como fuente detallada clase por clase: Spring MVC/Maven, JSP/JSTL, JDBC/testing, forms/i18n, security/logging, AOP, JPA/Hibernate, REST/Jersey y SPA/frontend.
- Actualizado `docs/CLAUDE.md` para que futuros agentes traten PAW por etapa: TP1, TP2 y TP final.
- Actualizadas paginas canonicas relacionadas (`tp1-vs-tpe2-final`, `hibernate-jpa`, `api-rest`, `single-page-applications` y backlinks de conceptos TP1) para que apunten a la nueva fuente.
- Actualizados ejemplos de uso/instalacion de skills para pedir etapa y enrutar migraciones por `$paw-tp2-migration` o `$paw-tp-final-migration`.
- Normalizadas referencias editoriales a fuentes raw historicas que ya no estan en el arbol publico (`notas_sprint_1`, `notas_para_sprint_2`, `specs_reserva_v2`, `diagrama_bd.puml`) como `fuente historica no incluida`.
- Nota editorial: las versiones de dependencias presentes en PDFs viejos quedan tratadas como historicas, no como recomendacion vigente.

## [2026-04-27] docs | wiki dentro del repo PAW

- Documentado el flujo recomendado para clonar o forkear PAW-Wiki dentro del repo de la app PAW.
- Aclarado que el repo entregable debe ignorar `/PAW-Wiki/` en `.gitignore` o `.git/info/exclude`.
- Agregado `docs/examples/wiki-dentro-repo-paw.md` y enlaces desde README, index, setup, checklist y troubleshooting.

## [2026-04-27] docs | uso de skills y orquestador

- Documentado que `$paw-feature-master` es la skill orquestadora y debe usarse como entrada para features, bugs, auditorias y planes de una app PAW.
- Explicado el ruteo desde `$paw-feature-master` hacia subskills de capa: models, persistence, services, webapp y testing.
- Actualizados `skills/README.md`, `docs/examples/instalar-skills.md`, `docs/examples/uso-con-agente.md`, `README.md` y `docs/CLAUDE.md` con prompts de uso.

## [2026-04-27] docs | onboarding a prueba de errores

- Ampliado `README.md` con quickstart de cero: clonar, abrir en Obsidian, crear `docs/private/`, instalar skills opcionales y verificar antes de commitear.
- Agregados examples de setup local, instalacion de skills, checklist de publicacion y troubleshooting.
- Actualizados `docs/CLAUDE.md`, `docs/index.md`, `skills/README.md` y `docs/tree.txt` para enlazar los nuevos flujos de onboarding.

## [2026-04-27] docs | README, CLAUDE y examples actualizados

- Actualizados `README.md`, `docs/CLAUDE.md` y `skills/README.md` para reflejar la estructura actual del wiki.
- Agregada `docs/examples/` con ejemplos de ingesta publica, consulta, uso con agentes y second brain privado.
- Enlazados los examples desde `docs/index.md` como guias operativas no canonicas.
- Eliminados emojis de los README/CLAUDE publicos y actualizado el arbol de `docs/tree.txt`.

## [2026-04-27] docs | imagen del grafo de Obsidian

- Reemplazada la captura `image-1.png` por `obsidian-graph-view.png` en la raiz del wiki.
- Actualizado `README.md` para mostrar la vista del grafo de Obsidian con un nombre de archivo descriptivo.
- Agregada la regla `docs/private/` a `.gitignore` para mantener el second brain privado fuera del repo publico.

## [2026-04-24] move | correcciones sprint 2 sin ingesta

- Movido `docs/raw/2026-04-24_correcciones-sprint-2_v1.md` a `docs/wiki/2026-04-24_correcciones-sprint-2_v1.md` para que funcione como tracker operativo editable
- Agregado frontmatter minimo de wiki y nota editorial explicita: no se hizo ingesta ni resumen, se conserva el detalle completo del plan/checklist
- Actualizados backlinks manuales desde `index.md`, `wiki/resumen-notas-sprint-2.md`, `wiki/2026-04-20_bugs-deploy-lote-0-y-plan-remediacion_v1.md` y `wiki/2026-04-24_auditoria-implementacion-contra-wiki_v1.md`

## [2026-04-24] plan | remediacion secuencial de cumplimiento contra wiki

- Creado `superpowers/plans/2026-04-24_paw-remediacion-cumplimiento-wiki_v1.md` como plan ejecutable por fases para cerrar los hallazgos reales de la auditoria del `2026-04-24`
- Incorporadas las dos decisiones confirmadas por el usuario: properties reales dentro del WAR quedan aceptadas por requisito del servidor de catedra y `/PAW-Wiki` ignorado queda aceptado porque se trackea en git privado separado
- El plan ordena la remediacion en fases reversibles: locale sin side effect en GET, batch para mesas reasignables, cambio de contrasena con password actual, link documental de `LegacySchemaGuard`, limpieza de estilos inline, decision/migracion de contextos Spring y verificacion final
- No se modifico codigo productivo; solo se agrego el plan y se actualizo el snapshot de `tree.txt`

## [2026-04-24] audit | implementacion actual contrastada contra PAW-Wiki

- Creada `wiki/2026-04-24_auditoria-implementacion-contra-wiki_v1.md` como auditoria exhaustiva del repo actual contra el canon del wiki, el enunciado y las convenciones de cursada
- Verificado `mvn clean test` y `mvn clean package` con BUILD SUCCESS; el reporte distingue el build verde de las brechas de cumplimiento que los tests actuales no cubren
- Hallazgos priorizados: `GET /lang/{language}` persiste locale en DB, N+1 en mesas reasignables de reservas owner, properties reales empaquetadas en el WAR, cambio de contrasena sin contrasena actual, `WebConfig` en root context, estilos inline masivos, link documental roto y riesgo de `PAW-Wiki/` ignorado por git
- Actualizados `index.md` y `tree.txt`; no se modifico `raw/` ni codigo productivo

## [2026-04-22] ingest | PDF crudo del informe tecnico absorbido en la pagina canonica

- Reescrita `wiki/2026-04-19_informe-implementaciones-desde-f81eb49_v1.md` para convertirla de nodo de navegacion a ingesta estructurada del informe: ahora deja explicitos alcance, bloques funcionales, QA manual, matriz de stories y limites de atribucion del documento
- Actualizado `index.md` para registrar `raw/2026-04-19_informe-implementaciones-desde-f81eb49_v1.pdf` como fuente absorbida y aclarar su relacion con `reports/2026-04-19_informe-implementaciones-desde-f81eb49_v1.{tex,pdf}`
- Verificado que el PDF en `raw/` y el PDF en `reports/` coinciden byte a byte; la fuente `raw/` sigue inmutable y `reports/assets/manual-qa/` queda como evidencia visual de soporte del QA documentado
- Sin cambios en `tree.txt`: la ingesta fue editorial sobre paginas ya existentes, no implico altas ni bajas de archivos dentro de `docs/`

## [2026-04-20] close | plan de remediacion de auditoria actual marcado como resuelto

- Actualizada `docs/superpowers/plans/2026-04-20_paw-plan-remediacion-auditoria-actual_v1.md` para dejarla en estado `resuelto/cerrado` y convertirla en registro de cierre, no en backlog abierto
- Agregado resumen de cierre por hallazgo con una aclaracion explicita sobre el punto de bootstrap: se considera resuelto por alineacion operativa/documental, sin seedear reviewer/owner/restaurantes en `schema-postgres.sql`
- Registrada la verificacion final usada para darlo por cerrado: `mvn clean test`, `mvn -pl webapp -am package -DskipTests` y smoke manual en `localhost:8080`

## [2026-04-20] ingest | absorcion canonica del cluster de bugs de deploy

- Creada `wiki/2026-04-20_bugs-deploy-lote-0-y-plan-remediacion_v1.md` para absorber `raw/todo.md`, `raw/2026-04-20_deploy-bugs-lote-0-audit_v1.md` y `raw/2026-04-20_deploy-bugs-remediation-plan_v1.md` en un unico nodo historico navegable
- Reescrito `index.md` para registrar el nodo nuevo dentro del bloque de auditorias/remediacion historica y marcar esas tres fuentes raw como absorbidas, no solo preservadas
- Enlazado el nodo nuevo con `wiki/resumen-notas-sprint-2.md` y `wiki/2026-04-19_informe-implementaciones-desde-f81eb49_v1.md` para dejar visible el puente entre cierre de sprint, informe tecnico y backlog postcierre
- Actualizado `tree.txt` para reflejar la nueva pagina wiki

## [2026-04-20] archive | cierre documental del plan de bugs deploy

- Copiadas al repo separado del wiki las fuentes `raw/2026-04-20_deploy-bugs-remediation-plan_v1.md`, `raw/2026-04-20_deploy-bugs-lote-0-audit_v1.md` y `raw/todo.md` para conservar el rastro documental del cierre de remediacion de deploy
- Eliminados del repo principal `2026-04-20_deploy-bugs-remediation-plan_v1.md`, `2026-04-20_deploy-bugs-lote-0-audit_v1.md` y `todo.md` para que dejen de quedar trackeados fuera del wiki
- Actualizados `index.md` y `tree.txt` para registrar las nuevas fuentes raw historicas

## [2026-04-20] lint | absorcion historica de fuentes y consolidacion de reportes

- Cerrada la orfandad de `wiki/resumen-notas-sprint-2.md` y normalizados sus backlinks recíprocos con `auth-flows`, `mailing`, `ux-flows`, `testing-unitario`, `resumen-correcciones`, `resumen-notas-sprint-1` y `2026-04-17_cierre-implementacion-reservas_v1`
- `raw/2026-04-10_paw-violaciones-plan_v1.md` queda absorbido explícitamente por `wiki/remediacion-violaciones-paw.md`
- Creada `wiki/2026-04-12_auditoria-y-plan-remediacion_v1.md` para absorber el par `raw/2026-04-12_paw-remediacion-plan-audit_v1.md` + `raw/2026-04-12_paw-remediacion-plan-secuencial_v1.md`
- Registrado `raw/diagrama_bd.puml` como fuente editable del diagrama de reservas dentro de `wiki/resumen-spec-reservas.md`
- Creada `wiki/2026-04-19_informe-implementaciones-desde-f81eb49_v1.md` para conectar `docs/reports/` al wiki y distinguir `tex/pdf` canónicos de sus artefactos `.aux/.log/.out/.toc`
- Eliminados el `.DS_Store` residual de la raiz de `PAW-Wiki` y los derivados LaTeX `.aux/.log/.out/.toc` del informe tecnico por ser artefactos no canónicos
- Reordenado `index.md` para separar el cluster de estudio/patrones del cluster historico de auditorías/remediación y del bloque de reservas/informe tecnico
- Actualizados `index.md` y `tree.txt` para reflejar las nuevas absorciones, el cleanup físico y los nodos de navegación

## [2026-04-19] ingest | notas de sprint 2 y cierre documental del checklist

- Movida la copia viva de `notas_para_sprint_2.txt` desde la raiz del repo a `docs/raw/notas_para_sprint_2.txt` para dejar una unica fuente operativa en el wiki
- Creada `wiki/resumen-notas-sprint-2.md` como pagina `fuente` para registrar el checklist del sprint 2
- La pagina nueva no solo resume pedidos originales: tambien deja asentadas las decisiones posteriores que redefinen el cierre (roles separados, filtro de explore no reabierto, form de registro fuera de esta pasada y tags reales/seed como follow-up tecnico)
- Actualizado `index.md` para registrar la nueva fuente y marcar `raw/notas_para_sprint_2.txt` como procesado
- Quedan visibles como seguimiento operativo los checks manuales de auth/reservas; la deuda tecnica de `verify`/`spy` quedo cerrada en el repo en esta misma pasada

## [2026-04-19] fix | seed de tags alineado al catalogo vigente

- Cerrado el follow-up tecnico que habia quedado abierto en `[[resumen-notas-sprint-2]]`: `persistence/src/main/resources/seed.sql` ahora backfillea tags para los restaurantes seed existentes
- La cobertura se alinea al conjunto vigente expuesto por `RestaurantCatalog.getSuggestedTags()` y garantiza que todos los tags hoy validos aparezcan al menos una vez en la base sembrada
- La decision de producto queda asentada como estable: los tags historicos pasaron a tratarse como tags reales editables y ya no quedan pendientes exclusivos de seed para la promocion a prod

## [2026-04-17] lint | normalizacion estructural del wiki

- Corregido el contrato editorial de `wiki/2026-04-17_cierre-implementacion-reservas_v1.md`: `tipo` valido, `fuentes` explicitas y link al `README` sin `file://` absoluto
- Cerrados los backlinks recíprocos faltantes detectados por el lint en páginas de auditoría, remediación, reservas y conceptos transversales (`spring-security`, `mailing`, `persistencia-jdbc`, `validacion-formularios`, entre otras)
- La página de cierre de reservas dejó de quedar huérfana al enlazarse desde `[[plan-implementacion-reservas]]` y `[[resumen-correcciones]]`
- Residuales no destructivos detectados: `.DS_Store` locales dentro de `docs/`; se reportan pero no se eliminan en esta pasada

## [2026-04-17] synth | hardening, estabilización de tests y cierre de reservas

- **Seguridad**: Se completó la migración a un modelo de seguridad declarativo en `WebAuthConfig`. Se eliminó el uso de `AccessDeniedHandler` manual que puenteaba el `DispatcherServlet`, reemplazándolo por `.accessDeniedPage("/error/403")`.
- **Manejo de Errores**: Implementación de `ErrorController.java` para resolver las vistas de error (`/error/403`, `/error/404`, `/error/500`) asegurando compatibilidad con redirecciones de Spring Security y `MockMvc`.
- **Estabilización de Tests**: Corrección de regresiones en `SecurityMvcTest` causadas por el cambio en el ruteo de errores. Auditoría y validación de `InlineValidationMvcTest`, `OwnerDashboardControllerMvcTest` y `OwnerReservationControllerMvcTest` contra el modelo de datos actualizado y las nuevas reglas de negocio de la Fase 10.
- **Cierre de Implementación**: Marcado de todas las fases del `plan-implementacion-reservas.md` como completado. Actualizada la wiki con el reporte de resolución de hallazgos en `resumen-correcciones.md`.
- **Nuevo Log**: Creado `wiki/2026-04-17_cierre-implementacion-reservas_v1.md` como documento de cierre operativo.
- **Lint del Wiki**: Auditoría de salud del wiki, corrección de enlaces huérfanos y broken links. Actualización de `index.md`, `log.md` y `tree.txt`.


## [2026-04-16] lint | auditoria del plan de reservas contra reglas del wiki

- Auditoria cruzada del plan `wiki/plan-implementacion-reservas.md` contra las ~24 paginas de reglas del wiki y el estado actual del repo. Resultado: dos hallazgos criticos, cuatro ambiguedades del spec resueltas por el usuario y seis mejoras menores aplicadas
- **Critico C1 (violacion de [[modelo-capas]] y [[mailing]])**: el plan ubicaba los templates de mail en `webapp/src/main/resources/mail-templates/`. El repo los tiene en `services/src/main/resources/mail-templates/` (`ServicesConfig.java:52` define el prefijo) y la regla es explicita: `MailService` vive en `services` y no puede conocer rutas de `webapp`. Corregido en el mapa de archivos (seccion `services/ — mailing`), en los archivos de Fase 8 y en Fase 8.1
- **Critico C2 (N+1 latente)**: las queries de `listUserReservations` y `listReservationsByRestaurant` no declaraban `JOIN` explicito para traer `restaurant.name`/`slug` y `user.display_name`/`slug` y `restaurant_tables.capacity`/`table_number`. Cualquier implementacion que cargue ids y resuelva nombres fila por fila en Java viola [[n-plus-1-joins-java]]. Agregadas notas explicitas en Fase 5.5 (JOIN a `restaurants`) y Fase 6.3d (JOIN a `users` + `restaurant_tables`) con el contrato de `RowMapper` enriquecido y un test `rowMapperResolvesUserNameAndTableTypeViaJoinInSql_notViaExtraQueriesInJava` que cuenta queries con `StatementCountingDataSource`. `SELECT` columnas explicitas (sin `SELECT *`)
- **S1 (link externo "dato sensible", §4.2)**: resuelto como **escapado XSS estricto** (`<c:out>` + validador rechaza `javascript:`/`data:`, solo `http(s)://`). No hay requisito adicional de logging, cifrado u ocultamiento. Es dato publico en la pagina del restaurante. Nueva fila en Decisiones lockeadas y bullet en `resumen-spec-reservas`
- **S2 (reserva para hoy con `slot_start` vencido)**: **se rechaza en la creacion** con 400 + error in-line. Nueva excepcion `SlotAlreadyPastException` en `service-contracts/`, chequeo temporal como paso 1 del algoritmo en Fase 4.2, tres tests nuevos en Fase 4.1 (`throwsSlotAlreadyPastWhenReservationIsTodayAndSlotStartIsInThePast`, `createsReservationWhenReservationIsTodayAndSlotStartIsInTheFuture`, `createsReservationWhenReservationIsTomorrow_regardlessOfClockTime`), un test MVC en 4.5 y mapeo en `ErrorHandlingAdvice` (Fase 10.2). Se prefiere validacion en servicio sobre `@Future` del form porque `@Future` opera sobre `reservation_date` y no distingue slots del dia mismo
- **S3 (recordatorio 11:00 AM)**: **solo reservas `confirmed`** reciben el mail `REMINDER`. Las `pending` no — si el owner no confirma, la reserva muere sin recordatorio. Tests de Fase 8.5 reescritos: renombrado a `sendsReminderForConfirmedReservationsOfTodayCreatedBefore11AM`, agregados `skipsPendingReservations`, `skipsCancelledReservations`, `skipsCompletedReservations`, `skipsReservationsForOtherDays`. Fase 8.6 actualizada con el filtro SQL `status = 'confirmed'` y `LEFT JOIN reservation_notifications` para idempotencia
- **S4 (direccion de reassignacion, §5.2)**: el owner puede reassignar a **cualquier** mesa con `capacity >= people_count` y libre en el slot, sin restriccion de direccion. Puede ir hacia abajo (mesa mas chica) o hacia arriba. Nunca a mesa con `capacity < people_count` (3 comensales jamas caen en mesa de 1 o 2). La prosa "de menos comensales a una para mas" se interpreta como ambigua. Nuevos tests en Fase 6.3b (`excludesTablesSmallerThanPartySize`, `includesTablesSmallerThanOriginallyAssigned_whenStillFitsPartySize`) y en Fase 6.3c (`confirmsAndReassignsToSmallerTable_whenStillFitsPartySize`)
- **Mejoras menores aplicadas**:
  - M1 pluralizacion: Fase 10.1 ahora exige sintaxis `ChoiceFormat`/`{0,plural,...}` para contadores; prohibido concatenar numero con texto en Java/JSP. Las tres ramas `0/1/>1` deben existir en ES y EN
  - M2 scheduler en tests: Fase 7.4 concretada con `@Profile("!test")` sobre `@EnableScheduling`/`@EnableAsync` en `ServicesConfig`; los tests de scheduler invocan `tick()`/`sendDailyReminders()` como metodos comunes sin depender del cron de Spring
  - M3 `Review.reservationId` nullable: Fase 1.8 explicita que el campo es `Long` boxed (nunca `long` primitivo) para representar reviews legacy con `reservation_id = NULL`
  - M4 auditoria de endpoints de review: Fase 9.5 reemplazada con tres `grep` concretos (`@RequestMapping`, `@PostMapping`/`@DeleteMapping`, `reviewService\.update|delete`) y requisito de documentar hallazgos en el PR; prohibido asumir sin evidencia
  - M5 fixture de Fase 1.9: horario fijado en `19:00–23:00` todos los dias para que sirva directo al `SlotCatalogTest`; fechas relativas (`CURRENT_DATE + INTERVAL '7 days'`) para que la fixture nunca vencie
  - M6 `SlotAlreadyPastException` mapeada en el advice (10.2, ya contemplado en S2)
- Actualizado `wiki/resumen-spec-reservas.md` con las 4 resoluciones nuevas en la seccion "Ambiguedades v2 — resoluciones aplicadas" y el frontmatter `actualizado` reflejando el scope del cambio
- No se toco `raw/` (inmutable); no se toco codigo del repo (solo wiki)

## [2026-04-16] synth | filtros y focus en la vista del owner de reservas

- Tres decisiones nuevas del usuario propagadas a `wiki/plan-implementacion-reservas.md`
- **Botones contextuales por estado** en `/mis-restaurantes/{id}/reservas`: `pending` muestra "Confirmar" + "Rechazar"; `confirmed` muestra "Cancelar"; `completed`/`cancelled` no muestran acciones. "Rechazar" y "Cancelar" comparten endpoint y side-effect (reserva a `cancelled` + `reservation_notifications(type='CANCELLED')`); el label es UX puro segun el estado previo
- **Paginacion con `?focus={reservationId}`**: el controller computa via servicio en que pagina cae la reserva bajo el sort vigente y hace `redirect` a esa pagina resaltando la fila. Reserva ajena o inexistente silencia el param. Nuevo metodo `findPageContainingReservation(restaurantId, reservationId, pageSize, sort)` con una sola query SQL (COUNT filas precedentes, sin traer la lista a Java). Tests en Fase 6.3e
- **Filtros nuevos en la vista del owner**: (1) sort por fecha `?sort=recent|oldest` con `recent` default, propagado al `ORDER BY` en SQL — tests en Fase 6.3d; (2) selector de restaurante poblado con `RestaurantService.getRestaurantsByOwner(userId)`. El bypass ADMIN **no** expande el selector a restaurantes ajenos: el ADMIN ve sus propios restaurantes owned, pero igual puede acceder a la URL de cualquier restaurante
- Nuevo enum `ReservationSort {DATE_DESC, DATE_ASC}` en `models/` con helper `fromQueryParam` (fallback a `DATE_DESC` si el valor es invalido o ausente — evita 400 ante query params malformados)
- Agregadas 3 filas a la tabla "Decisiones lockeadas" (labels de botones, resolucion de `?focus`, filtros de sort + restaurant selector)
- Fase 6 ampliada: Fase 6.3d y 6.3e (tests de sort y `findPageContainingReservation`), Fase 6.5 con 11 MVC tests nuevos (sort, focus en pagina 2, focus ajeno, selector en owner single/multi, admin sin expansion del selector, redirect post-accion preservando contexto), Fase 6.6 con flujo explicito de resolucion de `focus` y preservacion de `sort`/`page` tras POST, Fase 6.7 con zonas UI detalladas (botones por estado, toggle de sort que resetea a `page=0`, dropdown de restaurantes, highlight con scroll, zero state que preserva selectores)
- Mapa de archivos: agregado `ReservationSort.java` en `models/` y los dos metodos nuevos documentados en la entrada de `ReservationService.java`

## [2026-04-16] synth | cierre de dudas residuales v2

- Dudas residuales resueltas por el usuario y propagadas a `wiki/plan-implementacion-reservas.md`
- **Regla de gap sin excepciones**: un solo comensal en mesa de 2 (ratio 2) cae en `pending`. Se agrega fila en Decisiones lockeadas documentandolo
- **CTA del email pending-owner**: redirige a la vista de gestion (`/mis-restaurantes/{id}/reservas?focus={reservationId}`), no ofrece acciones directas desde el email. El owner decide ahi. Clarificado en Decisiones lockeadas y Fase 8.1
- **Concurrencia en reassignacion**: se resuelve con re-chequeo dentro del mismo `@Transactional` de `confirmAsOwner`; no se agrega optimistic locking ni columna `version`. Nuevo test `reChecksTableAvailabilityBeforeUpdate_concurrencyGuard` en Fase 6.3c
- **Cambio de modalidad con pendings vivos**: las pendings preexistentes no se re-evaluan; la regla aplica solo a reservas nuevas. Nuevo test `changingConfirmationMode_doesNotReEvaluateExistingPendings` en Fase 2.5b
- **Persistencia de `accepts_reservations`**: confirmado `VARCHAR(16) NULL` + `CHECK`; se descarta ENUM nativo de Postgres por compatibilidad con HSQLDB. Documentado en Decisiones lockeadas

## [2026-04-16] synth | cierre de dudas v2 y descarte de no_show

- v2-Q1 resuelto (gap grande): ratio `assigned_table.type / people_count >= 2` degrada a `pending` aun en modalidad `automatic`. Ejemplo locked: 2 personas + mesa de 4 = ratio 2 -> pending. Aplicado como decision lockeada, tests de Fase 4.1 reformulados, algoritmo del servicio reescrito en 4.2
- v2-Q2 resuelto (email pending-para-owner): template unico `reservation-pending-owner.html` disparado sobre **todo** `pending` recien creado (sea por `manual` o por gap). Agregado al mapa de archivos (Fase 8.1/8.2) y al contrato `MailService.sendReservationPendingToOwner`. Idempotencia via `reservation_notifications(type='PENDING_FOR_OWNER')`. Tests MVC de Fase 8.7 actualizados con los 4 contratos de notificacion (automatic small gap, automatic big gap, manual, owner confirm)
- v2-Q3 resuelto (reassignacion por el owner): aplica a cualquier `pending`; es opcional; mesas elegibles son libres en el slot con `capacity >= people_count`. Agregados `TableNotAvailableForSlotException`, service methods `listReassignableTables` y `confirmAsOwner(reservationId, Optional<Long>)`, endpoint con `tableId` opcional en el POST confirm, select server-side en `owner/reservations.jsp`, mapeo 409 en `ErrorHandlingAdvice` (10.2), y tests en 6.3b, 6.3c y 6.5
- v2-Q4 resuelto (enum vs boolean): se elige **enum** `AcceptsReservationsStatus {NOT_DEFINED, DISABLED, ENABLED}` persistido como `VARCHAR(16) NULL`. Agregado al `models/`, Fase 1.1 SQL actualizado, 1.7 actualizado con los 5 enums. Motivo explicito: el usuario planea sumar mas estados sin migracion disruptiva
- v2-Q5 y v2-Q6 descartados: el usuario decide **no implementar** `no_show`. Removidas las menciones que v2 habia mandado reintroducir (columna en `reservations`, test `markNoShow`, endpoint `/no-show`, badge en owner JSP, filtro en scheduler, test de review). Queda `no_show` como feature ausente del plan, documentada como descartada en Decisiones lockeadas y en el resumen de v2
- Nueva fila en Decisiones lockeadas del plan: aviso visual de gap en la vista del owner (`owner.reservations.gap.warning` con argumentos `tableType` y `peopleCount`) por pedido explicito del spec v2 §7.2
- Actualizado `wiki/resumen-spec-reservas.md` reemplazando la seccion de ambiguedades pendientes por una de "Ambiguedades v2 resueltas"

## [2026-04-16] ingest | spec funcional de reservas v2

- Nueva fuente raw en `raw/specs_reserva_v2.txt` provista por el usuario; supera a `raw/reservas/spec-funcional-reservas.md` (v1 queda como antecedente historico)
- Actualizado `wiki/resumen-spec-reservas.md`: fuentes ahora incluyen v2, nueva seccion "Cambios v2 vs v1" y seccion de ambiguedades v2 pendientes de decidir
- Cambios principales detectados respecto a v1:
  - `no_show` vuelve a ser obligatorio (campo en Reserva, accion "marcar no_show" del owner, regla consolidada §22.7)
  - Nueva regla §7.2: en modalidad `automatic`, si la mesa asignada difiere "por mucho" del `people_count`, la reserva **no se aprueba automaticamente** y se notifica al restaurante
  - Nueva regla §5.2: el restaurant admin puede elegir que mesa asignar al confirmar una `pending` cuando la mesa es mas grande que lo solicitado
  - §3.1 pide que `accepts_reservations` sea un enum (manteniendo valores null/false/true)
- Ingesta dispara re-apertura de dudas: umbral del gap, forma de la notificacion al restaurante, alcance de la reassignacion, enum vs boolean para `accepts_reservations`, visibilidad de `no_show` en perfil del usuario, efecto de `no_show` sobre transicion a `completed`
- Actualizado `index.md` con el nuevo archivo raw
- Plan `wiki/plan-implementacion-reservas.md` readaptado: restaurados los puntos de `no_show` que v2 mandata; agregada seccion "Dudas v2 pendientes" al tope con las preguntas bloqueantes para las reglas de gap/reassignacion/enum antes de codear

## [2026-04-16] synth | cierre de 3 dudas abiertas en el plan de reservas

- Dudas resueltas tras consulta con el usuario y aplicadas a `wiki/plan-implementacion-reservas.md`
- **1. Horarios de slots**: los slots se derivan directamente de `restaurant_hours`; no se soportan turnos que crucen medianoche. Reemplazado el chequeo condicional previo por un supuesto explicito en la Fase 1
- **2. Duracion de slots (locked)**: los slots duran **exactamente** `d` minutos (`d in {60, 90, 120}`); si el siguiente slot no entra completo antes de `close_time`, se descarta (no se acorta). Reescrita la "Regla de slot catalog" en Fase 3 con ejemplo `19:00-23:00 / d=90 -> 19:00-20:30 + 20:30-22:00`; tambien ajustada la fila "Generacion de slots" de la tabla de decisiones lockeadas
- **3. Admin bypass para gestion de reservas**: `ROLE_ADMIN` puede ver y operar sobre cualquier vista de gestion (`/perfil/{slug}/reservas` y `/mis-restaurantes/{id}/reservas`) y cancelar reservas ajenas. Implementado declarativamente en `AccessHelper` (nuevos metodos con bypass explicito de admin, nunca replicado en controllers/services)
- Fase 5 reestructurada: agregado `canViewUserReservations(auth, slug)`, cambio de `authenticated()` a `.access(...)` sobre GET `/perfil/*/reservas`, nuevos tests de acceso (admin ve slug ajeno, slug inexistente -> 404, admin cancela reserva ajena)
- Fase 6 reforzada: MVC tests explicitos para admin en GET/confirm/cancel; `canManageRestaurantReservations` documenta bypass admin
- Fase 10.5 actualizada con las rutas nuevas y la verificacion de admin bypass en `SecurityMvcTest`

## [2026-04-16] synth | eliminacion del feature no_show del plan de reservas

- Eliminadas todas las referencias a `no_show` del plan `wiki/plan-implementacion-reservas.md` (17 ocurrencias en 13 secciones): tabla de decisiones, mapa de archivos, tasks 1.2/1.15, test de Fase 4, Fase 6 entera (servicio, controller, JSP, criterio de cierre), scheduler y Fase 10.4
- Renumeradas las tasks de Fase 1 (ahora 1.1 a 1.15) y Fase 6 (ahora 6.1 a 6.9) para mantener consistencia
- Se conserva el cambio del campo `reservation_id` en `reviews`, la tabla `reservation_notifications` y el resto del flujo sin alteraciones
- El contador `users.no_show_count` y los endpoints `/no-show` / `/unmark-no-show` quedan fuera del alcance del sistema de reservas

## [2026-04-16] synth | ajuste del plan de reservas contra reglas del wiki

- Auditoria del plan `wiki/plan-implementacion-reservas.md` contra las paginas canonicas de reglas y correcciones; cerrados 7 gaps que chocaban con reglas del wiki
- Fase 3: `listAvailableSlots` pasa de iterar slots (N+1) a una sola query SQL con tabla virtual `VALUES` + `CROSS JOIN` + `NOT EXISTS`; agregado test `issuesExactlyOneQueryRegardlessOfSlotCount` para prevenir regresion; alineacion con [[n-plus-1-joins-java]]
- Fase 3.3: sumados tests de caso borde del spec (`roundsUpToNextCapacityWhenExactSizeUnavailable` para 7 -> 8; `allowsMultipleSimultaneousReservationsForSameUser`)
- Fase 5.9 / 6.8: zero states i18n con CTA para listados vacios (elogio explicito de [[buenas-practicas]])
- Fase 8.1: layout HTML comun `_layout.html` con header/footer y CTA como boton estilizado (alineacion con [[mailing]])
- Fase 10.2: mapeo explicito de `DataIntegrityViolationException` a 400 en `ErrorHandlingAdvice` (cierra regresion documentada en `resumen-correcciones`)
- Fase 10.5: paso explicito de verificacion del orden de `antMatchers` para evitar que el catch-all `/**.authenticated()` anule reglas por rol/expression
- Clarificada contradiccion interna sobre `accepts_reservations = null`: owner dashboard muestra badge "no definido"; pagina publica oculta el bloque
- Actualizado frontmatter `actualizado: 2026-04-16`

## [2026-04-15] ingest | spec funcional de reservas + plan de implementacion

- Guardado `raw/reservas/spec-funcional-reservas.md` con el spec consolidado entregado por el usuario (sin modificaciones sobre el texto fuente)
- Creada `wiki/resumen-spec-reservas.md` como fuente estructurada: idea central, ejes, casos borde, fuera de alcance, relacion con `raw/diagrama_bd.md` y ambiguedades a confirmar
- Creada `wiki/plan-implementacion-reservas.md` como sintesis con 10 fases (schema, configuracion, disponibilidad, flujo usuario, admin, scheduler, emails, reviews, hardening, verificacion) y orden sugerido de branches
- Actualizado `index.md`: nueva entrada en Fuentes, nueva entrada en Sintesis y nuevo estado de fuente raw bajo `raw/reservas/`

## [2026-04-13] synth | AOP reclasificado como soporte de TP1

- Actualizadas `wiki/resumen-apuntes.md`, `wiki/spring-aop.md` y `wiki/tp1-vs-tpe2-final.md` para dejar explicito que la unidad 8 si entra en TP1 como soporte de implementacion
- La nota editorial ahora aclara que AOP importa por proxies y `@Transactional`, no por ampliar el alcance funcional del trabajo
- Actualizado `index.md` para mover `spring-aop` al bloque de conceptos y dejar `TPE2 / Final` reservado para unidades 9-11

## [2026-04-13] synth | paginas viejas de auditoria marcadas como historicas

- Actualizadas `wiki/auditoria-extrema-cumplimiento-paw.md`, `wiki/plan-ejecucion-remediacion-auditoria.md` y `wiki/remediacion-violaciones-paw.md` para dejar un banner explicito de `historico` y evitar que se lean como estado actual
- Las tres paginas ahora redirigen a `wiki/2026-04-13_auditoria-repo-docs_v1.md` como referencia vigente

## [2026-04-13] synth | auditoria repo contra docs

- Creada `wiki/2026-04-13_auditoria-repo-docs_v1.md` como auditoria canonica del estado real del repo frente a `docs/`
- Clasificadas las reglas documentadas en `Verified`, `Mismatch`, `Regression / unresolved known issue` y `Documentation ambiguity`
- Actualizado `index.md` para registrar la nueva sintesis y rebajar a historico el wording de las paginas de auditoria previa
- `docs/raw/` se mantiene intacto en esta pasada

## [2026-04-13] synth | normalizacion de fuentes y ruta de estudio

- Normalizado el tono editorial entre `wiki/resumen-apuntes.md`, `wiki/resumen-notas.md` y `wiki/resumen-transcripciones-clases-2-a-4.md`
- Las tres fuentes ahora explicitan mejor su posicion en el wiki: teorica principal, practica principal y auxiliar de menor confianza
- Creada `wiki/tp1-vs-tpe2-final.md` como sintesis canonica de prioridad de estudio
- Actualizado `index.md` para registrar la nueva pagina en Sintesis
- Cerrados backlinks entre la nueva ruta de estudio y las tres paginas fuente, mas paginas relacionadas de MVC/modulos/formularios

## [2026-04-13] synth | fase 3 de remediacion marcada como resuelta

- Actualizada `wiki/plan-ejecucion-remediacion-auditoria.md` para marcar la Fase 3 (`Hardening de auth`) como resuelta
- La nota de estado ahora refleja remember-me sin fallback inseguro, cambio de idioma limitado a sesion/request y setup local alineado con `security.properties.example`
- Actualizado `index.md` para dejar visible que las fases 1-3 ya quedaron resueltas y que restan las fases 4 y 5

## [2026-04-13] synth | fase 5 registrada como resuelta por packaging

- Actualizada `wiki/plan-ejecucion-remediacion-auditoria.md` para marcar la Fase 5 (`Packaging y README`) como resuelta
- La nota de estado deja explicito que el cierre registrado corresponde al cleanup de packaging Maven y a la verificacion del WAR sin librerias de test
- `README.md` queda diferido fuera de esta pasada editorial, segun decision operativa del trabajo actual
- Actualizado `index.md` para reflejar el nuevo estado visible del plan

## [2026-04-13] synth | auditoria extrema de cumplimiento

- Creada `wiki/auditoria-extrema-cumplimiento-paw.md` como nueva fuente de verdad del plan de auditoria y remediacion
- La pagina fija el baseline del repo al `2026-04-13` con `mvn clean test` y `mvn clean package` en verde
- Consolidada una matriz inicial de hallazgos confirmados por dominio, severidad y fase de cierre
- Actualizado `index.md` para registrar la nueva sintesis y reclasificar `raw/2026-04-13_paw-remediacion-hallazgos-secuencial_v1.md` como antecedente historico ya absorbido

## [2026-04-13] synth | plan de ejecucion de remediacion de auditoria

- Creada `wiki/plan-ejecucion-remediacion-auditoria.md` como pagina canonica para el orden de implementacion y seguimiento por fase
- La pagina consolida el plan secuencial completo de la auditoria y explicita dependencias entre fases para minimizar conflictos
- Registrado el estado actual: Fase 1 y Fase 2 resueltas; Fases 3, 4 y 5 pendientes
- Actualizado `index.md` para dejar visible la nueva sintesis junto a la auditoria extrema

## [2026-04-13] ingest | apuntes.txt (tercera pasada: notas + unidades 8-11)

- Hecha una pasada chica sobre `wiki/resumen-notas.md` para alinearla mejor con las paginas nuevas de MVC/AOP y con ejemplos practicos de la cursada
- Ingeridas las unidades 8-11 de `raw/apuntes.txt` en paginas separadas y explicitamente marcadas como **fuera de TP1**:
  - `wiki/spring-aop.md`
  - `wiki/hibernate-jpa.md`
  - `wiki/single-page-applications.md`
  - `wiki/api-rest.md`
- Actualizado `wiki/resumen-apuntes.md` para dejar enlazadas las unidades 8-11 sin mezclar foco con TP1
- Actualizado `index.md` con una seccion `TPE2 / Final` para que el material futuro no quede mezclado con conceptos nucleares del TP1
- Cerrados backlinks con paginas relacionadas (`transactional`, `persistencia-jdbc`, `logging`, `manejo-excepciones`, `spring-web-mvc`, `internacionalizacion`, `ux-flows`, `http-y-sesiones`, `manejo-imagenes`, `configuracion-maven`, `inyeccion-dependencias`)

## [2026-04-13] ingest | apuntes.txt (segunda pasada TP1)

- Auditada la ingesta de `raw/apuntes.txt` / `raw/pdfs/PAW - Apuntes.pdf`; se confirmo que `wiki/resumen-apuntes.md` habia quedado demasiado sintetico para unidades 1-7
- Reescrita `wiki/resumen-apuntes.md` para cubrir mejor Scrum, HTTP/sesiones, JSP/JSTL, Spring MVC, Maven, testing, formularios, i18n, seguridad y logging
- Creadas 2 paginas nuevas para cubrir huecos relevantes del TP1:
  - `wiki/http-y-sesiones.md`
  - `wiki/spring-web-mvc.md`
- Paginas ampliadas con contenido faltante de unidades 1-7:
  - `wiki/scrum-metodologia.md` — workflow, AirBnB y MVP
  - `wiki/jsp-jstl.md` — usabilidad, CSS, UI kit vs framework, directivas JSP y familias JSTL
  - `wiki/configuracion-maven.md` — lifecycle, archetypes, `groupId`/`artifactId`, `SNAPSHOT`, herencia y `dependencyManagement`
  - `wiki/inyeccion-dependencias.md` — separacion entre configuracion y uso, contexto web y reemplazo de defaults
  - `wiki/testing-unitario.md` — JUnit/TestNG, stubs/mocks/fakes, nice mocks y casos "no deberia fallar"
  - `wiki/validacion-formularios.md`, `wiki/internacionalizacion.md`, `wiki/ux-flows.md` — feedback de formularios, fallback de locales, mensajes por campo y criterio UI de la cursada
- Actualizado `index.md` para registrar las nuevas paginas y dejar trazada la segunda pasada editorial sobre `PAW - Apuntes.pdf`
- Ajustados backlinks en paginas relacionadas (`spring-security`, `mailing`, `manejo-imagenes`, `modelo-capas`, `auth-flows`, `ux-flows`)

## [2026-04-13] lint | consistencia editorial del wiki

- Cerrados los backlinks faltantes detectados por el lint para volver a cumplir la regla de reciprocidad entre paginas
- `index.md` ahora registra `wiki/resumen-notas-sprint-1.md` y explicita el estado de todas las fuentes reales presentes en `docs/raw/`
- Reconciliadas las reglas editoriales de `403/404` para tratarlas como regla base con excepciones de endpoint, en vez de afirmaciones absolutas contradictorias
- Reconciliada la evolucion del esquema: ejemplo inicial con `schema.sql` versus estado final del repo con `schema-base.sql` + scripts dialectales
- Actualizadas paginas afectadas (`spring-security`, `manejo-excepciones`, `auth-flows`, `testing-unitario`, `persistencia-jdbc`, `resumen-notas`, `remediacion-violaciones-paw` y backlinks asociados)

## [2026-04-13] ingest | correcciones.md (segunda pasada exhaustiva)

- Reescrita `wiki/resumen-correcciones.md` para que la ingesta de `raw/correcciones.md` quede realmente exhaustiva y no solo resumida
- Creadas 3 paginas concepto nuevas para cubrir clusters antes difusos u omitidos:
  - `wiki/auth-flows.md`
  - `wiki/java-style.md`
  - `wiki/ux-flows.md`
- Paginas ampliadas para absorber huecos de la fuente original:
  - `wiki/spring-security.md` — flujo completo de autenticacion y backlink a `auth-flows`
  - `wiki/mailing.md` — notificaciones funcionales y backlinks a `auth-flows` / `ux-flows`
  - `wiki/jsp-jstl.md` — uso de taglib de Spring Security para UI condicionada
  - `wiki/buenas-practicas.md` — elogios faltantes sobre imagenes y calidad de interfaz
  - `wiki/internacionalizacion.md`, `wiki/validacion-formularios.md`, `wiki/configuracion-maven.md`, `wiki/logica-en-controllers.md`, `wiki/persistencia-jdbc.md`, `wiki/manejo-excepciones.md` — backlinks y fecha de actualizacion
- Actualizado `index.md` para registrar las 3 paginas nuevas y reclasificar `resumen-correcciones.md` como ingesta exhaustiva
- Objetivo editorial: que los hallazgos de `raw/correcciones.md` queden cubiertos sin perder detalle entre resumen y paginas derivadas

## [2026-04-10] synth | remediacion-violaciones-paw

- Creada `wiki/remediacion-violaciones-paw.md` como sintesis canonica del plan de remediacion
- Reclasificado el plan original segun el estado real del repo: fase 1 parcial; fases 2, 3 y 4 resueltas; fase 5 pendiente
- Actualizada la sintesis tras cerrar fase 4 en codigo: escape de `comingSoon` en `restaurant.jsp` y cobertura reforzada en `ViewTemplateSecurityTest`
- Actualizada la sintesis tras cerrar fase 5 en codigo: `RestaurantSearchForm` ahora usa claves i18n y la validacion tiene cobertura dedicada
- Actualizado `index.md` para registrar la nueva pagina en Sintesis
- Agregados backlinks desde `wiki/resumen-correcciones.md`, `wiki/xss-prevencion.md`, `wiki/internacionalizacion.md`, `wiki/persistencia-jdbc.md` y `wiki/testing-unitario.md`

## [2026-04-09] setup | Inicializacion del wiki

- Creada estructura de directorios: `raw/`, `wiki/`, `raw/pdfs/`, `raw/assets/`
- Migradas fuentes existentes desde `docs/tmp/` a `docs/raw/`
- Creado schema (`CLAUDE.md`), indice (`index.md`) y este log
- Fuentes disponibles para ingesta:
  - `correcciones.md` (26KB) -- feedback de correcciones previas
  - `enunciado.txt` (6KB) -- especificacion del TPE1
  - `apuntes.txt` (143KB) -- apuntes de la materia
  - `notas.txt` (175KB) -- notas de clases
  - 3 PDFs originales en `raw/pdfs/`

## [2026-04-09] ingest | correcciones.md

- Fuente: `raw/correcciones.md` (26KB, ~265 lineas)
- Contenido: recopilacion integra de errores, falencias y virtudes de correcciones previas de PAW
- Paginas creadas (14 total):
  - `wiki/resumen-correcciones.md` (fuente) — resumen estructurado por severidad
  - `wiki/spring-security.md` (concepto) — control de acceso declarativo
  - `wiki/xss-prevencion.md` (concepto) — escapado con c:out, sanitizacion LIKE
  - `wiki/logica-en-controllers.md` (concepto) — anti-patron de logica en controllers
  - `wiki/transactional.md` (concepto) — uso de @Transactional
  - `wiki/n-plus-1-joins-java.md` (concepto) — anti-patron N+1 queries
  - `wiki/testing-unitario.md` (concepto) — criterios de evaluacion de tests
  - `wiki/validacion-formularios.md` (concepto) — Bean Validation y custom validators
  - `wiki/persistencia-jdbc.md` (concepto) — patrones JDBC, RowMappers
  - `wiki/manejo-excepciones.md` (concepto) — ControllerAdvice y custom exceptions
  - `wiki/mailing.md` (concepto) — arquitectura de email
  - `wiki/configuracion-maven.md` (concepto) — multi-modulo y dependencias
  - `wiki/internacionalizacion.md` (concepto) — i18n
  - `wiki/logging.md` (concepto) — SLF4J/Logback
  - `wiki/buenas-practicas.md` (sintesis) — patrones elogiados por la catedra
- Indice actualizado con 15 paginas

## [2026-04-09] ingest | enunciado.txt

- Fuente: `raw/enunciado.txt` (6KB, 122 lineas)
- Contenido: especificacion oficial del TPE1 — stack, requerimientos, calendario, evaluacion
- Paginas creadas (3):
  - `wiki/resumen-enunciado.md` (fuente) — resumen estructurado del enunciado
  - `wiki/criterios-evaluacion.md` (concepto) — penalizaciones por area y prioridades
  - `wiki/calendario-entregas.md` (concepto) — fechas de sprints y entrega final
- Paginas actualizadas (3):
  - `wiki/testing-unitario.md` — agregada penalizacion de -1 punto
  - `wiki/configuracion-maven.md` — agregados requisitos de entrega (war, README)
  - `wiki/resumen-correcciones.md` — cross-links a enunciado
- Total paginas wiki: 18

## [2026-04-09] ingest | apuntes.txt

- Fuente: `raw/apuntes.txt` (143KB, 21521 lineas)
- Contenido: apuntes completos de PAW — 11 unidades, de las cuales 1-7 son relevantes para TPE1
- Unidades procesadas: 1 (Scrum), 2 (HTTP/mailing/imagenes), 3 (JSP/JSTL), 4 (Spring/Maven/capas/JDBC), 5 (Testing), 6 (Formularios/i18n), 7 (Spring Security/Logging)
- Unidades omitidas (TPE2): 8 (AOP), 9 (Hibernate), 10 (SPA), 11 (REST API)
- Paginas creadas (6):
  - `wiki/resumen-apuntes.md` (fuente) — resumen de las 11 unidades
  - `wiki/modelo-capas.md` (concepto) — modelo de capas DDD con scopes Maven
  - `wiki/jsp-jstl.md` (concepto) — tags JSTL y Spring form tags
  - `wiki/inyeccion-dependencias.md` (concepto) — Spring DI
  - `wiki/manejo-imagenes.md` (concepto) — almacenamiento y upload de imagenes
  - `wiki/scrum-metodologia.md` (concepto) — proceso Scrum para el curso
- Paginas actualizadas (8):
  - `wiki/spring-security.md` — UserDetailsService, PawAuthUser, BCrypt, roles como Set
  - `wiki/mailing.md` — Thymeleaf templates, @Async, regla de capa
  - `wiki/persistencia-jdbc.md` — JdbcTemplate, RowMapper como interfaz funcional
  - `wiki/logging.md` — Logback, arbol jerarquico, appenders
  - `wiki/testing-unitario.md` — HSQLDB setup, @Mock/@InjectMocks, estructura de test
  - `wiki/validacion-formularios.md` — flujo completo con form beans y Spring form tags
  - `wiki/internacionalizacion.md` — MessageSource, LocaleResolver
  - `wiki/configuracion-maven.md` — fuente agregada
- Total paginas wiki: 24

## [2026-04-09] ingest | notas.txt

- Fuente: `raw/notas.txt` (175KB, 633 lineas)
- Contenido: notas clase a clase con walkthroughs practicos, comandos reales y advertencias del profesor
- Clases procesadas: Clase 2 (Maven/Spring MVC), Clase 3 (interfaces/scopes/DI), Clase 4 (JDBC/testing/Mockito), Clase 5 (formularios/Bean Validation/i18n), Clase debida (Spring Security), Clase logging, Clase AOP/@Transactional/@ControllerAdvice
- Nota: contenido refuerza apuntes teoricos pero agrega detalles practicos unicos (analogias, limitaciones de proxy, historia SLF4J, verify() penalizado)
- Paginas creadas (1):
  - `wiki/resumen-notas.md` (fuente) — resumen estructurado por clase
- Paginas actualizadas (9):
  - `wiki/validacion-formularios.md` — @ModelAttribute como I/O, Beans vs Records, groups, i18n de mensajes
  - `wiki/spring-security.md` — filtro en web.xml, login/logout automatico, RememberMe key, Expression Language
  - `wiki/logging.md` — historia SLF4J, interpolacion con {}, config dual logback-test.xml, RollingFileAppender, additivity
  - `wiki/transactional.md` — mecanismo proxy AOP, limitaciones (self-invocation, metodos privados), @Async/@Scheduled/@Cacheable
  - `wiki/manejo-excepciones.md` — @ControllerAdvice como AOP Advice, @ExceptionHandler, @assignableTypes
  - `wiki/persistencia-jdbc.md` — schema.sql compartido entre tests y app via DatabasePopulator
  - `wiki/testing-unitario.md` — JdbcTestUtils, schema compartido, verify() explicitamente prohibido
  - `wiki/internacionalizacion.md` — regla de interpolacion completa (no concatenar), spring:message arguments
  - `wiki/logica-en-controllers.md` — fuente agregada
- Total paginas wiki: 25

## [2026-04-09] lint | saneamiento del wiki

- Corregidas inconsistencias factuales sobre `schema.sql`: se aclaro la transicion entre el ejemplo inicial de tests y el setup final compartido en `src/main/resources` del modulo `persistence`
- Corregido el modelo de modulos para alinearlo con las fuentes reales de la materia: `webapp`, `services`, `interfaces`, `persistence`, `models`
- Normalizados los links de "Ver tambien" para cumplir la regla de backlinks bidireccionales definida en `CLAUDE.md`
- Unificada la nomenclatura de Spring Security (`PawAuthUser`) para evitar divergencias entre paginas

## [2026-04-09] ingest | PDFs originales

- Fuentes validadas:
  - `raw/pdfs/Enunciado_TPE1.pdf` contra `raw/enunciado.txt`
  - `raw/pdfs/Notas clases PAW.pdf` contra `raw/notas.txt`
  - `raw/pdfs/PAW - Apuntes.pdf` contra `raw/apuntes.txt`
- Resultado: no se detectaron diferencias sustantivas tras normalizar espacios, ligaduras y puntuacion tipografica
- Decision editorial: no crear paginas fuente duplicadas; se registran los PDFs como originales canonicos de las ingestas ya existentes
- Paginas actualizadas:
  - `wiki/resumen-enunciado.md` — agregado PDF original a frontmatter y nota de validacion
  - `wiki/resumen-notas.md` — agregado PDF original a frontmatter y nota de validacion
  - `wiki/resumen-apuntes.md` — agregado PDF original a frontmatter y nota de validacion
  - `index.md` — PDFs marcados como procesados y vinculados a sus fuentes de texto

## [2026-04-09] lint | segunda pasada de afirmaciones fuertes

- Matizadas formulaciones demasiado absolutas en `wiki/transactional.md` y `wiki/spring-security.md` para alinearlas mejor con el criterio de la catedra y no presentarlas como verdades universales
- Corregida una inconsistencia menor en `wiki/logging.md`: produccion ahora queda explicitamente alineada con `WARNING` en vez de `INFO`

## [2026-04-09] synth | paginas de comparacion

- Creadas 4 paginas tipo `comparacion` para cubrir huecos del indice:
  - `wiki/comparacion-seguridad-web.md`
  - `wiki/comparacion-testing-servicios-y-daos.md`
  - `wiki/comparacion-capas-web-services-persistence.md`
  - `wiki/comparacion-jsp-formularios-e-i18n.md`
- Actualizado `index.md` para dejar de tener la seccion Comparaciones vacia
- Agregados backlinks en paginas conceptuales relacionadas para mantener reciprocidad

## [2026-04-09] ingest | transcripciones de audio (revision cautelosa)

- Fuentes revisadas:
  - `raw/audio_transcript/audio_transcript clase 2.VTT`
  - `raw/audio_transcript/audio_transcript clase 3.VTT`
  - `raw/audio_transcript/audio_transcript clase 4.VTT`
- Resultado editorial:
  - Clases 2 y 3 sirven como corroboracion parcial de ideas ya presentes en fuentes mas confiables
  - La "clase 4" corresponde a una clase mas reciente sobre prompteo, agentes y formularios; no se mezcla con la cronologia clasica de `resumen-notas`
- Decision: crear `wiki/resumen-transcripciones-clases-2-a-4.md` como fuente auxiliar de baja confianza, sin contaminar el resto del wiki con afirmaciones dudosas

## [2026-04-09] lint | reclasificacion de clase 4 VTT

- Aclarado contexto: `raw/audio_transcript/audio_transcript clase 4.VTT` no es una clase "desalineada", sino una clase mas actual centrada en agentes, prompting y formularios web
- Ajustada la interpretacion editorial: el riesgo principal pasa a ser el reconocimiento de voz (ASR), no una contradiccion con las notas historicas del curso
- Actualizados `wiki/resumen-transcripciones-clases-2-a-4.md` e `index.md` para reflejar esta reclasificacion
