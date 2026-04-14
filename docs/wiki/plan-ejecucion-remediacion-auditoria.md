---
titulo: Plan de Ejecucion de Remediacion de Auditoria PAW
tipo: sintesis
fuentes: [wiki/auditoria-extrema-cumplimiento-paw.md, raw/2026-04-13_paw-remediacion-hallazgos-secuencial_v1.md]
creado: 2026-04-13
actualizado: 2026-04-13
---

# Plan de Ejecucion de Remediacion de Auditoria PAW

> Estado: historico/cerrado. No usar esta pagina como estado actual del repo.
>
> Referencia vigente: [[2026-04-13_auditoria-repo-docs_v1]]

Esta pagina consolida el plan secuencial que se uso para resolver los hallazgos de la auditoria extrema levantada el `2026-04-13`. Queda como registro historico del orden de implementacion elegido, no como reflejo fiel del estado actual del repo.

## Estado historico del plan

| Fase | Estado | Nota |
| --- | --- | --- |
| 1. JSP/JSTL e i18n | Resuelta | Bundle default creado, `pageContext.request.contextPath` removido de vistas auditadas, `ratingStars.tag` sin scriptlets y atributos visibles auditados bajo i18n. |
| 2. Limites de capas | Resuelta | Wiring JDBC/migraciones movido a `persistence`, wiring SMTP/Thymeleaf movido a `services`, `WebConfig` reducido a MVC y tests reubicados. |
| 3. Hardening de auth | Resuelta | `security.rememberme.key` paso a ser obligatoria en runtime y tests, `GET /lang/{language}` quedo limitado a sesion/request, y el setup local quedo alineado con `security.properties.example` + `.gitignore`. |
| 4. Password reset | Pendiente | Falta flujo completo con token, DAO, service, MVC, vistas y mail. |
| 5. Packaging y README | Resuelta | Packaging Maven cerrado y WAR verificado sin librerias de test; el README operativo queda diferido fuera de esta pasada editorial. |

## Secuencia recomendada

| Fase | Hallazgo | Accion sugerida | Nivel de esfuerzo |
| --- | --- | --- | --- |
| 1 | Falta `messages.properties` como bundle default | Crear `messages.properties` como bundle base en espanol, alinear keys con `messages_es.properties` y `messages_en.properties`, y usarlo como fallback canonico para todo texto auditado. | Bajo |
| 1 | JSP/tag/forms usan `pageContext.request.contextPath` | Reemplazar usos auditados en headers, vistas auth y vistas admin por `<c:url>` y por calculo de redirect basado en `servletPath`/`requestURI`, sin cambiar rutas ni parametros. | Medio |
| 1 | `ratingStars.tag` usa scriptlets JSP | Reescribir el tag con JSTL/EL puro, manteniendo contrato (`value`, `large`) y la misma salida visual y clases CSS. | Medio |
| 1 | Hay textos/atributos visibles sin i18n cerrada | Mover `alt`, `title`, `aria-label`, `placeholder` y labels visibles auditados a `spring:message`, agregando las keys faltantes en los tres bundles. | Medio |
| 2 | `webapp` posee `DataSource`, `JdbcTemplate`, migraciones y wiring SMTP | Mover ownership de infraestructura fuera de `webapp`: DB, transacciones e inicializacion de schema a `persistence`; mail, Thymeleaf y `@EnableAsync` a `services`; dejar `WebConfig` solo con MVC/i18n/validacion/multipart/recursos. | Alto |
| 2 | Servicio SMTP loguea eventos operativos a `INFO` | Bajar a `DEBUG` los logs exitosos de `SmtpMailServiceImpl` y dejar `WARN`/`ERROR` para fallos reales. | Bajo |
| 3 | `remember-me` cae a `forkd-remember-me-dev-key` si falta config | Eliminar el fallback hardcodeado de `WebAuthConfig`, volver `security.rememberme.key` obligatoria en runtime y tests, y alinear `security.properties.example` con ese requisito. | Medio |
| 3 | `GET /lang/{language}` persiste locale en DB | Convertir el cambio de idioma en un efecto solo de sesion/request, reservando la persistencia de idioma para flujos explicitos como registro, claim o edicion de perfil. | Medio |
| 4 | No existe flujo dedicado de password reset | Implementar tabla de tokens, DAO, service, mail template y endpoints `GET/POST /password-reset` y `GET/POST /password-reset/{token}` con respuesta generica en la solicitud para no filtrar existencia de email. | Alto |
| 5 | `webapp` y `persistence` exponen dependencias de test en compile | Pasar JUnit, Mockito, Spring Test, Spring Security Test, Hamcrest y HSQLDB a `test` scope, y verificar que el WAR final no empaquete librerias de test. | Medio |
| 5 | README no deja bootstrap demo exacto por rol | Reescribir `README.md` con bootstrap reproducible para reviewer y owner, manteniendo el admin seed y documentando rutas concretas de alta/login y resultado esperado por rol. | Bajo |

## Dependencias entre fases

- Fase 1 va primero porque toca templates, i18n y tags compartidos; hacerlo despues de seguridad o password reset multiplicaria conflictos en JSPs.
- Fase 2 va segunda porque reordena ownership de configuracion sin cambiar comportamiento funcional; conviene estabilizar esta base antes de tocar auth o flujos nuevos.
- Fase 3 depende de Fase 2 porque el hardening de seguridad debe hacerse una vez fijado el ownership final de configuracion y properties.
- Fase 4 depende de Fase 3 porque introduce el unico flujo funcional nuevo y no debe mezclarse con deuda previa de seguridad.
- Fase 5 cierra al final porque limpia packaging y documentacion una vez que los cambios funcionales y de wiring ya quedaron estables.

## Criterios de cierre por fase

### Fase 1

- `messages.properties` existe y queda alineado con `messages_es.properties` y `messages_en.properties`
- No quedan scriptlets reales en `WEB-INF`
- No quedan usos auditados de `pageContext.request.contextPath`
- Los atributos visibles auditados quedan resueltos por i18n

### Fase 2

- `webapp` deja de ser duenio de DB, transacciones, migraciones y mail
- `PersistenceConfig` y `ServicesConfig` absorben el wiring correspondiente
- Los tests de migraciones/guards viven en `persistence`
- Los logs exitosos de SMTP quedan en `DEBUG`

### Fase 3

- La app falla explicitamente si falta `security.rememberme.key`
- Ningun test depende del fallback hardcodeado anterior
- `GET /lang/{language}` no escribe en DB

### Fase 4

- Existe flujo completo de password reset en service, DAO, MVC, vistas y mail
- La solicitud responde de forma generica tanto si el email existe como si no
- Se cubren token invalido, vencido, consumido y exito final

### Fase 5

- El WAR no incluye librerias de test
- Los scopes Maven quedan alineados con el ownership real de cada modulo
- `README.md` deja un bootstrap reproducible por rol

## Validacion global

- Ejecutar `mvn clean test` al cerrar Fase 2, Fase 4 y Fase 5
- Ejecutar `mvn clean package` al cierre total
- Confirmar cero imports JDBC/SQL en `webapp`
- Confirmar que el flujo visible de auth, admin y owner sigue operativo en localhost

## Ver tambien

- [[auditoria-extrema-cumplimiento-paw]]
- [[remediacion-violaciones-paw]]
- [[comparacion-capas-web-services-persistence]]
- [[spring-security]]
- [[configuracion-maven]]
