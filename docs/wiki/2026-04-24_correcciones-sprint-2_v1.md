---
titulo: Correcciones Sprint 2 - Plan secuencial
tipo: sintesis
fuentes: [CorreccionesSprint2.txt]
creado: 2026-04-24
actualizado: 2026-04-27
---

# Correcciones Sprint 2 - Plan secuencial

> **For agentic workers:** REQUIRED SUB-SKILL: use `superpowers:subagent-driven-development` to execute this plan.

> Nota editorial: esta pagina fue movida desde `raw/` a `wiki/` como plan operativo editable. No es una ingesta resumida de `CorreccionesSprint2.txt`; conserva el detalle completo para evitar perdida de informacion.

## Objetivo

Convertir las correcciones de la demo del Sprint 2 en una lista ejecutable, verificable y fácil de tildar. El foco es arreglar cada problema de forma separada, salvo cuando conviene agrupar cambios porque comparten archivos, validaciones o tests.

## Alcance y supuestos

- No se modifica `CorreccionesSprint2.txt`; este archivo es la versión Markdown/checklist.
- El plan está basado en el estado actual del repo al 2026-04-24.
- La recomendación por defecto es hacer cambios pequeños, reversibles y con tests por bloque.
- No hay migración de base necesaria para estas correcciones, salvo que durante implementación aparezca evidencia contraria.
- Para reservas concretadas, el supuesto recomendado es permitir marcar como `COMPLETED` solo reservas `CONFIRMED` ya vencidas. Si producto quiere permitir marcar antes del horario, decidirlo explícitamente antes de implementar ese bloque.

## Estrategia de agrupación

| Grupo | Correcciones incluidas | Motivo |
| --- | --- | --- |
| Copy/i18n y labels | Wording de reviews, username, imagen opcional, títulos, "verificado" | Comparten `messages*.properties` y JSPs; conviene hacer una pasada de consistencia. |
| Menú y precio | Símbolo pesos + precio numérico | Es el mismo flujo de formulario, validación, JS dinámico y modelo. |
| Exploración/búsqueda | Tag clickeable, cocinas como tags, búsqueda duplicada | Comparten `/explore`, header search y estado de filtros. |
| Reviews pagination UX | Paginación de reseñas ubicada/ancorada abajo | Cambio localizado en `restaurant.jsp` y comportamiento de paginación. |
| Seguridad owner | Editar restaurante ajeno debe dar 403 | Conviene primero testear si ya está cubierto y solo corregir si falla. |
| Reservas owner | Botones separados por barra + marcar reservas concretadas | Comparten pantalla owner, controller y service de reservas. |
| Reservas reviewer | Futuras/pasadas + paginación múltiplos de 3 | Comparten pantalla perfil, controller, DAO/service y resolver de page size. |
| Mails | Estandarizar mails | Cambio transversal pero aislable a templates/tests de mail. |

## Plan secuencial

### 0. Baseline antes de tocar código

- [ ] Revisar estado local:

```bash
git status --short --untracked-files=all
```

- [ ] Confirmar que `CorreccionesSprint2.txt` queda intacto.
- [ ] Ejecutar tests de referencia solo si el árbol está en condiciones:

```bash
mvn -pl webapp -am test
```

**Riesgo:** si ya hay cambios locales ajenos, no mezclarlos.
**Rollback:** no aplica; bloque de lectura/verificación.

### 1. Copy, i18n y labels visibles

**Correcciones cubiertas**

- [ ] Poner wording de review todo en castellano.
- [ ] Cuando te registras, que diga cuáles son las condiciones del username.
- [ ] Cuando creo el restaurant, en la imagen poner entre paréntesis que es opcional.
- [ ] Arreglar título pestaña de reservas del lado del restaurant.
- [x] Decisión de producto: conservar "Reseña verificada" / "Verified review"; no reemplazar por "Comió en este lugar".

**Archivos probables**

- `webapp/src/main/resources/i18n/messages.properties`
- `webapp/src/main/resources/i18n/messages_es.properties`
- `webapp/src/main/resources/i18n/messages_en.properties`
- `webapp/src/main/webapp/WEB-INF/views/auth/register.jsp`
- `webapp/src/main/webapp/WEB-INF/views/shared/restaurant-form-body.jspf`
- `webapp/src/main/webapp/WEB-INF/views/restaurant.jsp`
- `webapp/src/main/webapp/WEB-INF/views/profile/profile.jsp`
- `webapp/src/main/webapp/WEB-INF/views/profile/reservations.jsp`
- `webapp/src/main/webapp/WEB-INF/views/owner/dashboard.jsp`

**Pasos**

- [ ] Agregar ayuda debajo del username con la regla real de `RegisterForm`: 3 a 30 caracteres, minúsculas, números y guion bajo.
- [ ] Cambiar el label/mensaje de imagen para que diga que es opcional, manteniendo el shared form de owner/admin.
- [ ] Revisar todos los textos de reviews que todavía estén en inglés o suenen inconsistentes en castellano.
- [x] Mantener el label de reseña verificada con traducción ES/EN; queda resuelto por decisión de producto.
- [ ] Corregir el título literal de `profile.reservations.title` si aparece como key en la pestaña o en acciones owner.
- [ ] Mantener simetría de keys entre `messages`, `messages_es` y `messages_en`.

**Verificación**

```bash
mvn -pl webapp -am test -Dtest=MessageBundleConsistencyTest,I18nSymmetryTest,ViewTemplateSecurityTest
```

**Riesgo:** romper keys compartidas o dejar inglés/español mezclado.
**Rollback:** revertir solo cambios en JSPs y `messages*.properties`.

### 2. Menú: símbolo pesos y precio numérico

**Correcciones cubiertas**

- [ ] Agregar símbolo pesos al menú y que en el precio solo se pueda poner un número.

**Archivos probables**

- `webapp/src/main/webapp/WEB-INF/views/shared/menu-form-body.jspf`
- `webapp/src/main/webapp/resources/js/forkd.js`
- `webapp/src/main/java/ar/edu/itba/paw/webapp/form/MenuForm.java`
- `models/src/main/java/ar/edu/itba/paw/models/RestaurantMenuItem.java`
- `webapp/src/main/resources/i18n/messages*.properties`
- Tests de `MenuForm` y `RestaurantMenuItem`

**Pasos**

- [ ] Mostrar `$` como prefijo visual del input, no como parte del valor guardado.
- [ ] Cambiar el input a modo numérico/decimal (`inputmode`, patrón HTML y ayuda visual).
- [ ] Endurecer `MenuForm` para aceptar solo números válidos, con decimal si corresponde.
- [ ] Normalizar valores viejos que ya tengan `$` o espacios para no romper menús existentes.
- [ ] Actualizar el JS que agrega filas dinámicas del menú para usar el mismo input/precio.
- [ ] Actualizar mensajes de validación y tests.

**Verificación**

```bash
mvn -pl models test -Dtest=RestaurantMenuItemTest
mvn -pl webapp -am test -Dtest=MenuFormTest
```

**Riesgo:** rechazar datos históricos que hoy se muestran bien.
**Rollback:** volver a la validación anterior y al input simple; no requiere migración.

### 3. Explorar, tags, cocinas y búsqueda duplicada

**Correcciones cubiertas**

- [ ] Si toco un tag dentro de un restaurant, que haga la búsqueda de ese tag.
- [ ] En explorar, en cocina, que las opciones aparezcan como en tags.
- [ ] Se repite la búsqueda de la barra de búsqueda general con la barra de búsqueda de los propios restaurantes.

**Archivos probables**

- `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/ExploreController.java`
- `webapp/src/main/webapp/WEB-INF/views/restaurant.jsp`
- `webapp/src/main/webapp/WEB-INF/views/explore.jsp`
- `webapp/src/main/webapp/WEB-INF/tags/header.tag`
- `webapp/src/main/webapp/WEB-INF/views/owner/dashboard.jsp`
- CSS de chips/filtros si hace falta reutilizar clases existentes.

**Pasos**

- [ ] Convertir tags del detalle de restaurant en links a `/explore?tags=<tag>`.
- [ ] Mantener encoding correcto del tag y preservar el contrato GET de filtros.
- [ ] Cambiar cocina en `explore.jsp` para usar chips visuales como tags, sin cambiar el parámetro `cuisines`.
- [ ] Revisar por qué el header usa `currentQuery` también en pantallas owner/admin.
- [ ] Separar el estado de la búsqueda global del estado de búsquedas locales. Recomendado: que `header.tag` solo pinte `currentQuery` en `/explore`, o renombrar el atributo de owner para que no colisione.
- [ ] Agregar/ajustar tests de vista para asegurar que los filtros conservan `query`, `tags`, `cuisines`, `pageSize` y demás estado GET.

**Verificación**

```bash
mvn -pl webapp -am test -Dtest=ViewTemplateSecurityTest,ExploreControllerMvcTest
```

**Riesgo:** perder filtros actuales al navegar o cambiar idioma.
**Rollback:** revertir JSP/tag changes; no afecta persistencia.

### 4. Reviews: paginación anclada abajo

**Correcciones cubiertas**

- [ ] Estoy leyendo las reseñas, paso de página y quiero que me aparezca ubicado en la parte de abajo, no arriba.

**Archivos probables**

- `webapp/src/main/webapp/WEB-INF/views/restaurant.jsp`
- `webapp/src/main/webapp/resources/js/pagination.js`
- `webapp/src/main/webapp/WEB-INF/tags/paging.tag`

**Pasos**

- [ ] Identificar si el paging de reviews usa form GET, links o JS.
- [ ] Agregar un anchor estable al bloque de reseñas, por ejemplo `#reviews`.
- [ ] Hacer que la paginación de reviews conserve el fragment o haga scroll al bloque de reseñas después de cargar.
- [ ] Evitar impactar paginaciones de explorar, reservas o admin.

**Verificación**

```bash
mvn -pl webapp -am test -Dtest=ViewTemplateSecurityTest
```

Manual:

- [ ] Abrir un restaurante con varias reseñas.
- [ ] Pasar de página en reseñas.
- [ ] Confirmar que la pantalla queda en la zona de reseñas, no en el inicio del restaurante.

**Riesgo:** cambiar comportamiento global del tag de paginación.
**Rollback:** revertir solo el anchor/JS del bloque reviews.

### 5. Seguridad: editar restaurante ajeno debe devolver 403

**Correcciones cubiertas**

- [ ] Que sea un 403 cuando quiero hacer un edit a un restaurant que no estimo. Ahora es 404.

**Archivos probables**

- `webapp/src/main/java/ar/edu/itba/paw/webapp/config/WebAuthConfig.java`
- `webapp/src/main/java/ar/edu/itba/paw/webapp/auth/AccessHelper.java`
- `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/owner/OwnerDashboardController.java`
- `webapp/src/test/java/ar/edu/itba/paw/webapp/controller/SecurityMvcTest.java`

**Pasos**

- [ ] Primero escribir o ubicar un test explícito para `GET /my-restaurants/{id}/edit` con owner incorrecto.
- [ ] Si el test ya da 403, dejar solo el test como cobertura.
- [ ] Si da 404, corregir el matcher/security guard para diferenciar:
  - restaurante inexistente: 404;
  - restaurante existente de otro owner: 403.
- [ ] Repetir criterio para rutas owner equivalentes solo si comparten el mismo bug.

**Verificación**

```bash
mvn -pl webapp -am test -Dtest=SecurityMvcTest
```

**Riesgo:** exponer existencia de restaurantes si se cambia mal el orden 403/404.
**Rollback:** revertir test y cambio en security/controller.

### 6. Reservas owner: acciones y reservas concretadas

**Correcciones cubiertas**

- [ ] Los botones de qué hacer con la reserva están separados por una barra.
- [ ] En reservas estaría bueno agregar quiénes fueron al restaurant. Poder ir tickeando las reservas que se concretaron.

**Archivos probables**

- `webapp/src/main/webapp/WEB-INF/views/owner/reservations.jsp`
- `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/owner/OwnerReservationController.java`
- `services/src/main/java/ar/edu/itba/paw/services/ReservationService.java`
- `services/src/main/java/ar/edu/itba/paw/services/ReservationServiceImpl.java`
- `persistence/src/main/java/ar/edu/itba/paw/persistence/ReservationJdbcDao.java`
- `webapp/src/main/resources/i18n/messages*.properties`
- Tests de controller/service relacionados a owner reservations.

**Pasos**

- [ ] Reemplazar la barra visual entre botones por un grupo de acciones claro y consistente.
- [ ] Confirmar que la tabla ya muestra nombre/contacto de quien reservó; si falta dato, agregarlo sin romper privacidad.
- [ ] Agregar acción owner para marcar reserva como concretada/completada.
- [ ] Usar estado existente `COMPLETED`; no crear columna nueva.
- [ ] Regla recomendada: solo permitir completar reservas `CONFIRMED` vencidas.
- [ ] Preservar todos los parámetros actuales al redirigir: `status`, `sort`, `page`, `pageSize`.
- [ ] Definir si al completar se envía mail. Recomendado: reutilizar el mail existente de reserva completada si ya existe y evitar duplicados.

**Verificación**

```bash
mvn -pl services test -Dtest=ReservationServiceImplTest
mvn -pl webapp -am test -Dtest=OwnerReservationControllerMvcTest,ViewTemplateSecurityTest
```

Manual:

- [ ] Como owner, ver reservas confirmadas pasadas.
- [ ] Marcar una como concretada.
- [ ] Confirmar que cambia a completada y vuelve a la misma página/filtros.

**Riesgo:** permitir completar reservas futuras o ajenas.
**Rollback:** revertir action/controller/service; no hay migración si se usa `COMPLETED`.

### 7. Mis reservas reviewer: futuras/pasadas y page size múltiplo de 3

**Correcciones cubiertas**

- [ ] En el perfil, cuando veo mis reservas, filtrar futuras y pasadas.
- [ ] Paginación en mis reservas reviewer debería ser múltiplos de 3.

**Archivos probables**

- `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/ProfileController.java`
- `webapp/src/main/webapp/WEB-INF/views/profile/reservations.jsp`
- `webapp/src/main/java/ar/edu/itba/paw/webapp/pagination/PageSizeResolver.java`
- `services/src/main/java/ar/edu/itba/paw/services/ReservationService.java`
- `services/src/main/java/ar/edu/itba/paw/services/ReservationServiceImpl.java`
- `persistence/src/main/java/ar/edu/itba/paw/persistence/ReservationJdbcDao.java`
- Tests de profile reservations/service/DAO.

**Pasos**

- [ ] Agregar filtro GET, por ejemplo `period=upcoming|past|all`.
- [ ] Definir criterio temporal con fecha + horario de la reserva, no solo fecha.
- [ ] Mantener compatibilidad: sin parámetro, usar `all` o el comportamiento actual.
- [ ] Agregar opciones de page size específicas para reviewer reservations: `3`, `6`, `9`, `12`, `15`.
- [ ] No cambiar los page sizes globales de owner/admin si no hace falta.
- [ ] Preservar `status`, `sort`, `period`, `pageSize` y `page` en filtros/paginación.

**Verificación**

```bash
mvn -pl webapp -am test -Dtest=ProfileReservationsMvcTest,ViewTemplateSecurityTest
mvn -pl services test -Dtest=ReservationServiceImplTest
```

Manual:

- [ ] Perfil > Mis reservas.
- [ ] Filtrar futuras.
- [ ] Filtrar pasadas.
- [ ] Cambiar page size a múltiplos de 3.
- [ ] Pasar de página y confirmar que se conservan filtros.

**Riesgo:** mezclar definición de "pasada" con zona horaria o slots.
**Rollback:** revertir filtro GET y volver a query actual.

### 8. Estandarizar mails

**Correcciones cubiertas**

- [ ] Estandarizar mails.

**Archivos probables**

- `services/src/main/resources/mail-templates/_layout.html`
- `services/src/main/resources/mail-templates/*.html`
- `services/src/test/java/ar/edu/itba/paw/services/SmtpMailServiceImplContextTest.java`
- `services/src/test/java/ar/edu/itba/paw/services/ReviewMailTemplateEscapingTest.java`
- `webapp/src/main/resources/i18n/messages*.properties`

**Pasos**

- [ ] Auditar templates que ya usan `_layout.html` y los que todavía tienen HTML standalone.
- [ ] Migrar templates standalone al layout común sin cambiar subjects ni datos enviados.
- [ ] Unificar botones, footer, espaciado y tono.
- [ ] Mantener escaping de datos de usuario en templates.
- [ ] No tocar ni copiar valores reales de `mail.properties`; cualquier config sensible debe quedar fuera del plan/código.
- [ ] Agregar tests de render para cubrir al menos un mail de auth, uno de restaurant, uno de reserva y uno de review.

**Verificación**

```bash
mvn -pl services test -Dtest=SmtpMailServiceImplContextTest,ReviewMailTemplateEscapingTest
```

**Riesgo:** romper render Thymeleaf o links de mail.
**Rollback:** revertir templates; no cambia DB.

## Verificación final recomendada

- [ ] Ejecutar suite completa:

```bash
mvn clean test
```

- [ ] Hacer QA manual de estos flujos:
  - Registro.
  - Crear/editar restaurant con imagen opcional.
  - Editar menú y validar precio.
  - Explorar por tag/cocina.
  - Ver restaurante y paginar reseñas.
  - Intentar editar restaurante ajeno.
  - Owner: gestionar reservas.
  - Reviewer: filtrar mis reservas.
  - Enviar/renderizar mails relevantes en entorno local.

## Checklist original para ir tildando

- [ ] Agregar símbolo pesos al menú y que en el precio solo se pueda poner un número.
- [ ] Poner wording de review todo en castellano.
- [ ] Si toco un tag dentro de un restaurant, que haga la búsqueda de ese tag.
- [ ] En explorar, en cocina, que las opciones aparezcan como en tags.
- [ ] Estoy leyendo las reseñas, paso de página y quiero que me aparezca ubicado en la parte de abajo, no arriba.
- [ ] Cuando te registras, que te diga cuáles son las condiciones que tiene que cumplir el username.
- [ ] Cuando creo el restaurant, en la imagen poner entre paréntesis que es opcional. Como está en lo de la URL.
- [ ] Se repite la búsqueda de la barra de búsqueda general con la barra de búsqueda de los propios restaurantes. Esto probablemente se debe a que tienen el mismo nombre de variable.
- [ ] Los botones de qué hacer con la reserva están separados por una barra.
- [ ] Que sea un 403 cuando quiero hacer un edit a un restaurant que no estimo. Ahora es 404.
- [ ] Arreglar título pestaña de reservas del lado del restaurant.
- [x] Decisión final: mantener "Reseña verificada" / "Verified review" para las reviews vinculadas a reserva.
- [ ] Estandarizar mails.
- [ ] En reservas estaría bueno agregar quiénes fueron al restaurant. Poder ir tickeando las reservas que se concretaron.
- [ ] En el perfil, cuando veo mis reservas, habría que filtrar entre futuras y las que ya pasaron.
- [ ] Paginación en mis reservas reviewer debería ser múltiplos de 3.

## Next actions

- [ ] Empezar por el bloque 1 porque reduce ruido visual y valida i18n.
- [ ] Hacer un commit manual por bloque o cada dos bloques relacionados.
- [ ] Después de cada bloque, correr la verificación indicada antes de pasar al siguiente.
- [ ] Mantener este Markdown como tracker y marcar cada item cuando esté probado.

## Ver tambien

- [[resumen-notas-sprint-2]]
- [[2026-04-20_bugs-deploy-lote-0-y-plan-remediacion_v1]]
- [[2026-04-24_auditoria-implementacion-contra-wiki_v1]]
