# Plan Secuencial de Remediacion PAW

## Resumen

- Primera accion mutante completada: este plan queda guardado en `docs/raw` antes de tocar codigo.
- Alcance bloqueado: resolver los 5 hallazgos auditados y endurecer solo las zonas adyacentes del mismo seam tecnico.
- Criterio de avance: cada fase debe cerrar en verde antes de abrir la siguiente; minimo `mvn clean test` tras fases 2, 4 y 5, y `mvn clean package` al final.

## Linea base de aceptacion

- Hallazgos abiertos al inicio:
  - Java embebido en `ratingStars.tag`.
  - Wiring JDBC y migraciones en `webapp`.
  - Scopes Maven de test demasiado amplios en `webapp` y `persistence`.
  - `remember-me` con fallback hardcodeado.
  - `README` sin bootstrap reproducible claro para reviewer y owner.
- Estado inicial verificado antes de implementar:
  - `mvn clean test` en verde.
  - `mvn clean package` en verde.
  - `webapp/target/webapp.war` generado correctamente.
- Criterios de cierre global:
  - Cero scriptlets en `WEB-INF`.
  - Cero imports JDBC/SQL en `webapp`.
  - Tests y package en verde.
  - `README` actualizado con acceso operativo reproducible por rol.

## Cambios clave

- No habra cambios de API HTTP, rutas, DTOs de formularios ni modelo de dominio.
- Cambio interno de wiring: `PersistenceConfig` pasa a ser dueno de `DataSource`, `JdbcTemplate`, transacciones, inicializacion de schema y migraciones; `WebConfig` queda solo para MVC/i18n/multipart/static/view resolution.
- Cambio de contrato de configuracion: `security.rememberme.key` deja de tener fallback hardcodeado y pasa a ser un requisito explicito de runtime y de tests que cargan seguridad.
- Cambio operativo en documentacion: `README` deja de apoyarse solo en credenciales fijas y documenta bootstrap reproducible para reviewer y owner; el admin seed se mantiene.

## Fases secuenciales

### Fase 0 - Congelar el plan y la linea base

- Guardar este contenido en `docs/raw/2026-04-13_paw-remediacion-hallazgos-secuencial_v1.md`.
- Anotar en este documento la linea base de aceptacion: hallazgos abiertos, comandos verdes actuales y criterios de cierre por fase.
- No empezar cambios de implementacion hasta que el archivo exista.

### Fase 1 - Cumplimiento JSP/JSTL sin scriptlets

- Reescribir `ratingStars.tag` sin `<%`, `<%=`, ni logica Java embebida.
- Mantener el mismo contrato del tag: atributos `value` y `large`, misma semantica visual y mismos hooks CSS que consumen las vistas actuales.
- Implementacion elegida: usar JSTL/EL para clamp del rating y porcentaje de fill, con render fijo de estrellas y overlay CSS; no mover esta logica a controllers.
- Endurecer tests de vistas para que fallen si aparece cualquier patron `<%`, `<%=`, `<%!` en `WEB-INF`.

### Fase 2 - Restaurar limites de capas

- Mover de `WebConfig` a `PersistenceConfig` los beans de `DataSource`, `JdbcTemplate`, `PlatformTransactionManager`, `DataSourceInitializer`, property source de BD, `@EnableTransactionManagement` y el wiring de `LegacySchemaGuard` y de la migracion de rating.
- Reubicar `LegacySchemaGuard` y `ReviewRatingSchemaMigration` al modulo `persistence`, junto con sus tests.
- Mantener `web.xml` con la misma carga de configs; no cambiar rutas ni ciclo de arranque, solo el dueno de la infraestructura.
- Regla de cierre: `webapp` no debe quedar con imports JDBC/SQL ni logica de bootstrap de base.

### Fase 3 - Hardening de Maven y classpath

- Ajustar scopes de test en `webapp` y `persistence`: `junit`, `mockito`, `spring-test`, `spring-security-test`, `hamcrest`, `hsqldb` en `test`.
- Eliminar de `webapp` las dependencias JDBC/Postgres que dejen de ser necesarias despues de Fase 2.
- Agregar al modulo correcto solo las dependencias de compilacion necesarias para el wiring movido, sin relajar el enforcement por capas.
- Verificar que el WAR final no empaquete librerias de test.

### Fase 4 - Hardening de remember-me

- Quitar el fallback `forkd-remember-me-dev-key` de `WebAuthConfig`.
- Hacer que la ausencia de `security.rememberme.key` falle explicitamente al arrancar, en vez de degradar a una key predecible.
- Mantener `security.properties.example` como plantilla minima obligatoria.
- Actualizar los tests que cargan seguridad para inyectar `security.rememberme.key` explicitamente via propiedades de test, no por fallback implicito.

### Fase 5 - README y accesos operativos

- Mantener la credencial fija del admin seed.
- Documentar un bootstrap reproducible para reviewer y owner usando los flows actuales `/register/reviewer` y `/register/owner`, con credenciales demo exactas para crear localmente en la primera ejecucion.
- Dejar en README una matriz clara por rol: como crear/iniciar sesion, que ruta usar y que capacidad valida cada rol.
- Explicitar que reviewer y owner son reproducibles, no preseeded permanentes.

### Fase 6 - Verificacion final y cierre

- Ejecutar `mvn clean test`.
- Ejecutar `mvn clean package`.
- Confirmar cero scriptlets en `WEB-INF`.
- Confirmar cero imports JDBC/SQL en `webapp`.
- Confirmar que las keys de `messages_es.properties` y `messages_en.properties` siguen alineadas.
- Actualizar este plan en `docs/raw` con un resumen diff-like por fase: agregado, movido, eliminado, endurecido.

## Plan de tests

- Tests automaticos existentes deben seguir verdes sin bajar cobertura observable.
- Los tests de migraciones/guards se mudan con sus clases al modulo `persistence`.
- Se agrega al menos un test de enforcement para ausencia de scriptlets.
- Se ajusta la configuracion de tests de seguridad para suministrar `security.rememberme.key` explicitamente.
- Validacion de empaquetado: inspeccionar WAR o dependency tree para ausencia de `junit`, `mockito`, `hsqldb` y `spring-security-test`.

## Supuestos y defaults

- Se eligio `Hallazgos + hardening`.
- Se eligio `Bootstrap reproducible` para README, no cuentas reviewer/owner preseeded.
- No se cambia comportamiento funcional de negocio, solo cumplimiento, wiring y documentacion operativa.
- Si durante Fase 2 aparece otro bean de infraestructura de BD todavia colgado de `webapp`, se mueve en esa misma fase y no se difiere.

## Resumen diff-like

- Agregado:
  - Documento de plan secuencial y linea base de aceptacion.
- Movido:
  - Pendiente de completar al cerrar cada fase.
- Eliminado:
  - Pendiente de completar al cerrar cada fase.
- Endurecido:
  - Pendiente de completar al cerrar cada fase.
