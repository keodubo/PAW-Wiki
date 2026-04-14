---
titulo: Auditoria Extrema de Cumplimiento PAW
tipo: sintesis
fuentes: [raw/2026-04-13_paw-remediacion-hallazgos-secuencial_v1.md, raw/correcciones.md, raw/enunciado.txt, raw/apuntes.txt, raw/notas.txt]
creado: 2026-04-13
actualizado: 2026-04-13
---

# Auditoria Extrema de Cumplimiento PAW

> Estado: historico. No usar esta pagina como fuente de verdad del estado actual del repo.
>
> Referencia vigente: [[2026-04-13_auditoria-repo-docs_v1]]

Esta pagina queda como baseline historico de la auditoria extrema levantada el `2026-04-13`. Reemplaza operativamente al documento de trabajo en `raw/2026-04-13_paw-remediacion-hallazgos-secuencial_v1.md`, que queda solo como antecedente historico y no vuelve a modificarse.

El plan secuencial historico asociado vive en [[plan-ejecucion-remediacion-auditoria]], pero el contraste vigente entre documentacion e implementacion vive en [[2026-04-13_auditoria-repo-docs_v1]].

## Baseline historico verificado

- Fecha de corte: `2026-04-13`
- Build canonico: `mvn clean package` en verde
- Suite canonica: `mvn clean test` en verde
- Estado del repo al iniciar la remediacion: sin cambios trackeados pendientes

## Scorecard inicial

| Dominio | Estado | Nota |
| --- | --- | --- |
| Arquitectura por capas | Riesgo alto | `webapp` todavia concentra wiring JDBC, migraciones y mail |
| Seguridad y auth flows | Riesgo alto | `remember-me` con fallback hardcodeado, `GET /lang/{language}` persiste locale y falta password reset |
| JSP, XSS e i18n | Riesgo alto | Hay scriptlets, `pageContext.request.contextPath` y bundle default faltante |
| Maven y packaging | Riesgo medio-alto | Scopes de test filtrados a compile en `webapp` y `persistence` |
| Logging y mailing | Riesgo medio | Mail bien orientado, pero con wiring fuera de capa y ruido de `INFO` innecesario |
| Testing | Riesgo medio | La base es buena, pero faltan tests de enforcement y del flujo nuevo de reset |
| UX y rutas | Riesgo medio | README operativo incompleto y algunos textos/atributos visibles sin i18n cerrada |

## Hallazgos confirmados

| Severidad | Hallazgo | Evidencia | Fase |
| --- | --- | --- | --- |
| P1 | `ratingStars.tag` sigue usando scriptlets JSP | `webapp/WEB-INF/tags/ratingStars.tag` | 2 |
| P1 | `webapp` todavia posee `DataSource`, `JdbcTemplate`, migraciones y wiring SMTP | `WebConfig`, `LegacySchemaGuard`, `ReviewRatingSchemaMigration` | 3 |
| P1 | `remember-me` cae a `forkd-remember-me-dev-key` si falta config | `WebAuthConfig` | 4 |
| P1 | `GET /lang/{language}` persiste locale en DB | `LanguageController` | 4 |
| P1 | No existe flujo dedicado de password reset | `AuthController`, `UserService`, vistas auth | 4 |
| P2 | JSP/tag/forms todavia usan `pageContext.request.contextPath` | `header.tag`, `adminHeader.tag`, `admin/*.jsp`, `auth/*.jsp` | 2 |
| P2 | Falta `messages.properties` como bundle default | `webapp/src/main/resources/i18n/` | 2 |
| P2 | `webapp` y `persistence` exponen dependencias de test en compile | `webapp/pom.xml`, `persistence/pom.xml` | 5 |
| P2 | README no deja bootstrap demo exacto por rol | `README.md` | 5 |
| P3 | Servicio SMTP loguea eventos operativos a `INFO` | `SmtpMailServiceImpl` | 3 |
| P3 | Hay atributos visibles sin i18n cerrada en templates auditados | `admin/restaurants.jsp` y vistas relacionadas | 2 |

## Criterios de cierre

- Cero scriptlets reales en `WEB-INF`
- Cero `pageContext.request.contextPath` en JSP/tag auditados
- Cero imports JDBC/SQL en `webapp`
- Cero side effects de persistencia disparados por `GET /lang/{language}`
- Flujo de password reset completo con DTOs, DAO, service, MVC, vistas y mail
- WAR sin librerias de test
- `README.md` con cuentas demo reproducibles por rol

## Resumen diff-like

- Agregado:
  - Pagina canonica de auditoria extrema y baseline consolidado.
- Movido:
  - Pendiente de completar durante la remediacion.
- Eliminado:
  - Pendiente de completar durante la remediacion.
- Endurecido:
  - Pendiente de completar durante la remediacion.

## Ver tambien

- [[remediacion-violaciones-paw]]
- [[plan-ejecucion-remediacion-auditoria]]
- [[criterios-evaluacion]]
- [[configuracion-maven]]
- [[comparacion-capas-web-services-persistence]]
- [[spring-security]]
- [[jsp-jstl]]
- [[internacionalizacion]]
- [[mailing]]
