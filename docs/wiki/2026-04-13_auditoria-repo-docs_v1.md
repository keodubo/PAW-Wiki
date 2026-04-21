---
titulo: Auditoría del Repositorio contra /docs
tipo: sintesis
fuentes: [CLAUDE.md, index.md, log.md, wiki/auditoria-extrema-cumplimiento-paw.md, wiki/plan-ejecucion-remediacion-auditoria.md, wiki/remediacion-violaciones-paw.md]
creado: 2026-04-13
actualizado: 2026-04-13
---

# Auditoría del Repositorio contra /docs

Esta auditoría usa `docs/` como canon de cumplimiento. El objetivo no es reinterpretar la intención del proyecto, sino contrastar el estado real del repo contra reglas, decisiones técnicas, remediaciones exigidas y deudas históricas ya documentadas.

## Executive summary

- Baseline verificado en esta pasada:
  - `mvn clean test` ejecutado secuencialmente en verde.
  - `mvn clean package` ejecutado secuencialmente en verde.
  - El WAR final no empaqueta librerías de test.
  - El repo estaba limpio al inicio de la auditoría.
- Resultado consolidado:
  - `11` reglas documentadas verificadas.
  - `3` mismatches activos entre documentación e implementación.
  - `1` known issue todavía abierto en modo parcial.
  - `3` ambigüedades o desactualizaciones en el canon documental.
- Conclusión:
  - La mayor parte de las remediaciones estructurales ya está implementada en código.
  - El gap real restante se concentra en un redirect parcial del cambio de idioma, en la falta de bootstrap reproducible por rol en `README.md` y en la imposibilidad práctica de trackear nuevas páginas bajo `docs/` sin intervenir `.gitignore`.

## Compliance checklist

### Views / i18n

| Regla documentada | Estado | Evidencia principal | Observación |
| --- | --- | --- | --- |
| Debe existir un bundle default junto a `messages_es.properties` y `messages_en.properties` | Verified | `webapp/src/main/resources/i18n/messages.properties` | El bundle default ya existe y acompaña a los locales explícitos. |
| No deben quedar scriptlets JSP reales en las vistas auditadas | Verified | `webapp/src/main/webapp/WEB-INF/tags/ratingStars.tag` | El tag auditado ya usa JSTL/EL puro. |
| La remediación del cambio de idioma no debe alterar rutas ni parámetros al calcular el redirect | Mismatch | `docs/wiki/plan-ejecucion-remediacion-auditoria.md:28` | La implementación pública preserva ruta, pero todavía pierde query params. |

### Layer boundaries

| Regla documentada | Estado | Evidencia principal | Observación |
| --- | --- | --- | --- |
| `webapp` no debe ser dueño del wiring JDBC, migraciones ni mail | Verified | `persistence/src/main/java/ar/edu/itba/paw/persistence/PersistenceConfig.java`, `services/src/main/java/ar/edu/itba/paw/service/ServicesConfig.java`, `webapp/src/main/java/ar/edu/itba/paw/webapp/config/WebConfig.java` | El ownership quedó distribuido según la remediación documentada. |
| Los tests de migraciones y guards deben vivir en `persistence` | Verified | `persistence/src/test/java/ar/edu/itba/paw/persistence/LegacySchemaGuardTest.java` | La cobertura del guard quedó reubicada en la capa correcta. |

### Auth / security

| Regla documentada | Estado | Evidencia principal | Observación |
| --- | --- | --- | --- |
| La app debe fallar si falta `security.rememberme.key` | Verified | `webapp/src/main/java/ar/edu/itba/paw/webapp/config/WebAuthConfig.java` | Ya no existe el fallback inseguro documentado en auditorías previas. |
| `GET /lang/{language}` no debe persistir locale en DB | Verified | `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/LanguageController.java`, `webapp/src/test/java/ar/edu/itba/paw/webapp/controller/LanguageControllerMvcTest.java` | El cambio quedó acotado a sesión/request. |
| Debe existir flujo dedicado de password reset con MVC, service y tests | Verified | `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/AuthController.java`, `services/src/main/java/ar/edu/itba/paw/service/UserServiceImpl.java`, `webapp/src/test/java/ar/edu/itba/paw/webapp/controller/PasswordResetMvcTest.java` | El flujo que antes figuraba como faltante ya existe. |

### Packaging / testing

| Regla documentada | Estado | Evidencia principal | Observación |
| --- | --- | --- | --- |
| El WAR no debe incluir librerías de test | Verified | `webapp/pom.xml`, inspección del `webapp.war` generado | El empaquetado actual no incluye JUnit, Mockito, Spring Test ni HSQLDB. |
| El harness JDBC con HSQLDB debe seguir centralizado en `persistence/src/test/.../TestConfiguration.java` | Verified | `persistence/src/test/java/ar/edu/itba/paw/persistence/TestConfiguration.java` | El bootstrap de tests sigue donde la documentación lo exige. |
| No deben quedar rastros de `System.out` ni `printStackTrace` en código de aplicación | Verified | búsqueda repo-wide sobre `src/main` | No se detectaron usos activos en código productivo. |
| `README.md` debe dejar bootstrap reproducible por rol | Mismatch | `docs/wiki/auditoria-extrema-cumplimiento-paw.md:46`, `docs/wiki/plan-ejecucion-remediacion-auditoria.md:37` | El admin está sembrado, pero reviewer y owner siguen descritos como flujos abiertos. |

### Docs workflow

| Regla documentada | Estado | Evidencia principal | Observación |
| --- | --- | --- | --- |
| `docs/raw/` es inmutable | Verified | `docs/CLAUDE.md:9-10,109-110` | Esta pasada no toca fuentes raw. |
| Toda nueva síntesis del wiki debe actualizar `docs/index.md` y `docs/log.md` | Verified | `docs/CLAUDE.md:70-71,111` | Esta auditoría queda registrada en ambos archivos. |
| El canon operativo bajo `docs/` debe poder mantenerse como parte del repo | Mismatch | `docs/CLAUDE.md:8-16,109-112`, `.gitignore:65-66` | Hoy la raíz `/docs` está ignorada y los archivos nuevos quedan fuera del flujo normal de Git. |

## Issues found

### Issue 1 — Redirect público de idioma sin preservación de parámetros

- severity: `P2`
- affected path(s): `webapp/src/main/webapp/WEB-INF/tags/header.tag`
- rule violated: La remediación documentada para reemplazar `pageContext.request.contextPath` exige recalcular redirects sin cambiar rutas ni parámetros (`docs/wiki/plan-ejecucion-remediacion-auditoria.md:28`).
- supporting evidence:
  - `webapp/src/main/webapp/WEB-INF/tags/header.tag:19-23` arma `/lang/es` y `/lang/en` con `redirect=${currentPath}` solamente.
  - `webapp/src/main/webapp/WEB-INF/tags/adminHeader.tag:20-28` sí concatena `pageContext.request.queryString` en `redirectTarget`.
  - `LanguageController` ya acepta un redirect seguro; la pérdida ocurre en el tag público, no en el controller.
- explanation: La remediación quedó parcial. En páginas públicas con filtros o paginación, el cambio de idioma conserva la ruta pero descarta el query string, lo que rompe el criterio de no alterar parámetros.
- recommended fix: Replicar en `header.tag` la construcción de `redirectTarget` ya usada en `adminHeader.tag`, preservando `queryString` antes de generar los links `/lang/{language}`.

### Issue 2 — `README.md` sigue sin bootstrap reproducible por rol

- severity: `P2`
- affected path(s): `README.md`
- rule violated: La documentación de auditoría exige que el README deje un bootstrap demo exacto por rol activo (`docs/wiki/auditoria-extrema-cumplimiento-paw.md:46`, `docs/wiki/plan-ejecucion-remediacion-auditoria.md:37`).
- supporting evidence:
  - `README.md:146-156` sólo publica credenciales seed para admin.
  - `README.md:149-150` describe reviewer y owner como “crear cuenta” o “reclamar cuenta legacy”, sin credenciales resultantes ni secuencia determinística cerrada.
  - El canon documental sigue tratando este punto como criterio de cierre.
- explanation: El repo permite llegar funcionalmente a reviewer y owner, pero no documenta un camino reproducible para que un corrector o integrante levante el entorno y entre directamente con cuentas operativas equivalentes al caso admin.
- recommended fix: Reescribir la sección de accesos del README con una secuencia reproducible por rol: credenciales seed o pasos exactos de alta/claim y el estado final esperable para reviewer y owner.

### Issue 3 — `.gitignore` bloquea el flujo normal de mantenimiento de `docs/`

- severity: `P2`
- affected path(s): `.gitignore`
- rule violated: El esquema del wiki asume que `docs/wiki/`, `docs/index.md` y `docs/log.md` son parte del canon mantenido del repo (`docs/CLAUDE.md:8-16,109-112`).
- supporting evidence:
  - `.gitignore:65-66` ignora `/docs`.
  - `git status --short --ignored docs` reporta `!! docs/`.
  - `docs/CLAUDE.md` define a `docs/` como la estructura viva del wiki y exige tocar `index.md` y `log.md` en cada síntesis.
- explanation: Los archivos ya trackeados dentro de `docs/` pueden editarse, pero cualquier nueva página queda ignorada por defecto. Eso contradice el flujo documental canonizado y vuelve frágil la incorporación de auditorías nuevas como esta.
- recommended fix: Afinar `.gitignore` para no ignorar `docs/`, o reemplazar esa regla por exclusiones más específicas. Si se mantiene temporalmente, forzar `git add -f` para nuevas páginas del wiki y documentar esa excepción.

## Regressions / unresolved known issues

No se confirmó una regresión nueva de código fuera de los mismatches anteriores, pero sí queda una deuda legacy que el propio canon sigue marcando como parcialmente resuelta.

### Known issue 1 — Compatibilidad legacy de `day_of_week`

- severity: `P3`
- affected path(s): `docs/wiki/remediacion-violaciones-paw.md`
- rule violated: La remediación previa no puede darse por cerrada si todavía importa compatibilidad con datos legacy (`docs/wiki/remediacion-violaciones-paw.md:17,25-31`).
- supporting evidence:
  - `docs/wiki/remediacion-violaciones-paw.md:17` marca la fase `day_of_week` como `Parcial`.
  - `docs/wiki/remediacion-violaciones-paw.md:25-31` deja explícito que falta una migración dedicada si existen despliegues con datos anteriores.
- explanation: El esquema actual, el form y los tests ya usan `1..7`, pero el canon todavía no puede cerrar esta fase sin una decisión explícita sobre compatibilidad de datos legacy. No hay base documental para asumir que ese escenario dejó de importar.
- recommended fix: Elegir una de dos rutas y documentarla explícitamente:
  - implementar una migración de compatibilidad si los datos legacy siguen en scope;
  - o cerrar el hallazgo indicando que no existe obligación de soportar despliegues previos con ese formato.

## Documentation ambiguities

### Ambiguity 1 — El plan de remediación mantiene estados de fase desactualizados

- severity: `P2`
- affected path(s): `docs/wiki/plan-ejecucion-remediacion-auditoria.md`
- rule violated: El workflow del wiki exige reflejar el estado final recomendado y aclarar la transición para no dejar contradicciones internas (`docs/CLAUDE.md:73-74,116`).
- supporting evidence:
  - `docs/wiki/plan-ejecucion-remediacion-auditoria.md:15-21` sigue marcando `Password reset` como `Pendiente`.
  - `docs/wiki/plan-ejecucion-remediacion-auditoria.md:35` vuelve a describir ese flujo como inexistente.
  - La misma tabla marca `Packaging y README` como `Resuelta`, pero la nota de `README` pendiente sigue viva en `README.md` y en el resto del canon.
- explanation: El documento mezcla estado histórico con estado actual sin señalizar claramente que ya quedó viejo. Eso lo vuelve poco fiable como referencia operacional directa.
- recommended fix: Reescribir la tabla de estados como historial congelado o actualizarla al estado real del repo, dejando nota explícita de qué quedó resuelto y qué sigue abierto.

### Ambiguity 2 — La auditoría extrema sigue describiendo un baseline viejo como si fuera vigente

- severity: `P2`
- affected path(s): `docs/wiki/auditoria-extrema-cumplimiento-paw.md`
- rule violated: El canon no debe presentar como “estado actual” hallazgos ya resueltos sin marcar la evolución posterior (`docs/CLAUDE.md:73-74,116`).
- supporting evidence:
  - `docs/wiki/auditoria-extrema-cumplimiento-paw.md:11` se presenta como fuente de verdad operativa.
  - `docs/wiki/auditoria-extrema-cumplimiento-paw.md:24-31` describe como riesgos vigentes problemas ya corregidos en el repo.
  - `docs/wiki/auditoria-extrema-cumplimiento-paw.md:38-47` enumera hallazgos que hoy están verificados como resueltos.
- explanation: La página sigue siendo útil como baseline histórico, pero su redacción actual la hace parecer el estado vigente del repo. Eso induce falsos positivos si se la usa sola como checklist de cumplimiento.
- recommended fix: Reetiquetar la página como baseline histórico o actualizar cada hallazgo con estado vigente y referencia a esta auditoría repo-vs-docs.

### Ambiguity 3 — Referencias operativas a una página inexistente dentro de `docs/`

- severity: `P3`
- affected path(s):
  - `webapp/src/main/resources/database.properties.example`
  - `persistence/src/main/java/ar/edu/itba/paw/persistence/LegacySchemaGuard.java`
  - `persistence/src/test/java/ar/edu/itba/paw/persistence/LegacySchemaGuardTest.java`
- rule violated: Las referencias operativas hacia `/docs` deben apuntar a documentos reales del canon; un path inexistente no puede tomarse como comportamiento documentado.
- supporting evidence:
  - `webapp/src/main/resources/database.properties.example:4-5` remite a `docs/2026-04-02-reviewer-reset_v1.md`.
  - `persistence/src/main/java/ar/edu/itba/paw/persistence/LegacySchemaGuard.java:75-83` repite la misma referencia en el mensaje de error.
  - `persistence/src/test/java/ar/edu/itba/paw/persistence/LegacySchemaGuardTest.java:75-78` fija ese path inexistente como parte del contrato del mensaje.
- explanation: La implementación y su test aluden a un documento que no existe en `docs/`. Esto no crea una nueva regla de cumplimiento, pero sí evidencia documentación operacional muerta o movida.
- recommended fix: Reemplazar la referencia por el documento canónico real que describe el reset local, o incrustar las instrucciones mínimas directamente en el mensaje sin depender de un archivo inexistente.

## Recommended remediation steps

1. Corregir `header.tag` para preservar `queryString` en los links de cambio de idioma y agregar una prueba MVC o de vista que cubra redirect con parámetros.
2. Reescribir `README.md` con bootstrap reproducible por rol, no sólo por capacidad funcional.
3. Resolver el conflicto de `.gitignore` con `docs/` antes de depender de nuevas páginas wiki en revisiones futuras.
4. Actualizar o reetiquetar `auditoria-extrema-cumplimiento-paw.md` y `plan-ejecucion-remediacion-auditoria.md` para que queden claramente históricas o vigentes, pero no ambiguas.
5. Cerrar explícitamente la deuda `day_of_week`: migración real si legacy importa, o descope formal si ya no aplica.
6. Reemplazar las referencias a `docs/2026-04-02-reviewer-reset_v1.md` por un target existente del canon documental.

## Ver tambien

- [[auditoria-extrema-cumplimiento-paw]]
- [[plan-ejecucion-remediacion-auditoria]]
- [[remediacion-violaciones-paw]]
- [[2026-04-12_auditoria-y-plan-remediacion_v1]]
- [[configuracion-maven]]
