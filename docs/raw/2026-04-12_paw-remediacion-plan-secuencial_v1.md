# Plan Secuencial de Refactor PAW

- Fecha: `2026-04-12`
- Estado: `Listo para ejecutar`
- Objetivo: refactorizar el repo para alinearlo con las reglas de la wiki local de `/docs`, priorizando arquitectura y manteniendo UX correcta en formularios.

## Criterio de diseño de este plan

Este plan toma como base estas páginas de la wiki:

- [testing-unitario.md](/Users/keoni/Claude-Workspace/projects/paw-2026a-01/docs/wiki/testing-unitario.md)
- [validacion-formularios.md](/Users/keoni/Claude-Workspace/projects/paw-2026a-01/docs/wiki/validacion-formularios.md)
- [manejo-excepciones.md](/Users/keoni/Claude-Workspace/projects/paw-2026a-01/docs/wiki/manejo-excepciones.md)
- [spring-security.md](/Users/keoni/Claude-Workspace/projects/paw-2026a-01/docs/wiki/spring-security.md)
- [jsp-jstl.md](/Users/keoni/Claude-Workspace/projects/paw-2026a-01/docs/wiki/jsp-jstl.md)
- [configuracion-maven.md](/Users/keoni/Claude-Workspace/projects/paw-2026a-01/docs/wiki/configuracion-maven.md)
- [mailing.md](/Users/keoni/Claude-Workspace/projects/paw-2026a-01/docs/wiki/mailing.md)
- [n-plus-1-joins-java.md](/Users/keoni/Claude-Workspace/projects/paw-2026a-01/docs/wiki/n-plus-1-joins-java.md)

## Correcciones incorporadas

- `verify()` se elimina igual. Si algún test queda menos estricto, se acepta; la prioridad es cumplir la regla de la wiki y dejar tests sin `verify()`.
- Los errores de formularios deben seguir siendo **inline**. No se aceptan redirecciones a `413`, `415` o `409` para casos recuperables de form.

## Decisión Fuerte

Qué hacer:

- ejecutar primero todo lo que ordena capas y MVC en `webapp`;
- después resolver persistencia y servicios;
- recién al final reescribir los tests que hoy usan `verify()`.

Por qué:

- evita re-trabajo en controllers;
- permite sacar `try/catch` sin romper UX, porque primero se construye la validación declarativa;
- deja los tests para el final, cuando el comportamiento ya quedó estable.

Riesgo / downside:

- la fase más invasiva es la de validación inline, porque cruza forms, validators, controllers y posiblemente services.

Cómo rollback:

- hacer **un commit por fase**;
- si una fase rompe comportamiento, se revierte solo ese commit sin tocar las demás.

## Scope final

### Se ejecutan

1. Sacar Java estático de `cuisineLabel.tag`
2. Declarativizar access control con Spring Security
3. Preparar validación declarativa para mantener errores inline
4. Sacar `try/catch` de controllers sin perder errores inline
5. Resolver N+1 en `UserJdbcDao`
6. Usar locale del owner en mails de approve/reject
7. Agregar `ON DELETE CASCADE` en `reviews.user_id`
8. Reescribir tests sin `verify()`
9. Verificación final

### Se descartan del plan

- mover `spring-jdbc` fuera de `webapp/pom.xml`
- mover extracción de `MultipartFile` al service

Motivo:

- no aportan a cumplir la wiki hoy;
- agregan riesgo o discusión arquitectónica sin beneficio claro para este refactor.

## Orden Secuencial Recomendado

## Fase 0 — Preparación

Objetivo:

- fijar una forma de trabajo que permita rollback limpio.

Pasos:

1. Crear branch de refactor.
2. Ejecutar build base para tener línea de comparación:

```bash
mvn clean package -q
```

3. Guardar la regla operativa: `1 commit por fase`.

Salida esperada:

- build base verde o, si falla, falla documentada antes de tocar código.

## Fase 1 — Sacar Java estático del tag file

Regla wiki:

- [jsp-jstl.md](/Users/keoni/Claude-Workspace/projects/paw-2026a-01/docs/wiki/jsp-jstl.md)

Objetivo:

- eliminar código Java incrustado de `cuisineLabel.tag`.

Archivos:

- `webapp/src/main/webapp/WEB-INF/tags/cuisineLabel.tag`
- `webapp/src/main/java/ar/edu/itba/paw/webapp/config/WebConfig.java`
- `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/CurrentUserAdvice.java`
- nuevo resolver utilitario en `webapp`

Pasos:

1. Crear `CuisineKeyResolver`.
2. Registrarlo explícitamente en Spring.
3. Exponerlo vía `@ModelAttribute` global para uso en JSP/tag.
4. Reescribir `cuisineLabel.tag` sin scriptlets ni declaraciones Java.
5. Compilar:

```bash
mvn -pl webapp compile -q
```

Por qué va primero:

- es un cambio aislado, de bajo riesgo, y elimina una violación muy explícita de la wiki.

Rollback:

- revertir solo el commit de esta fase.

## Fase 2 — Declarativizar access control

Regla wiki:

- [spring-security.md](/Users/keoni/Claude-Workspace/projects/paw-2026a-01/docs/wiki/spring-security.md)

Objetivo:

- sacar chequeos manuales de permisos desde controllers y moverlos a Spring Security declarativo.

Archivos:

- `webapp/src/main/java/ar/edu/itba/paw/webapp/config/WebAuthConfig.java`
- `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/OwnerDashboardController.java`
- `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/ProfileController.java`
- `webapp/src/main/java/ar/edu/itba/paw/webapp/auth/AccessHelper.java`

Pasos:

1. Habilitar method security compatible con la versión usada.
2. Mover ownership checks a `@PreAuthorize` o `.access("@accessHelper...")`.
3. Eliminar llamadas manuales a `requireManageRestaurant(...)` y `requireEditableReviewerProfile(...)` desde controllers donde ya queden cubiertas declarativamente.
4. Dejar `AccessHelper` como expression helper, no como mecanismo invocado manualmente desde endpoints.
5. Compilar:

```bash
mvn -pl webapp compile -q
```

Por qué va antes que la limpieza de exceptions:

- primero se ordena la responsabilidad de seguridad;
- después se limpia el flujo MVC de validación y errores.

Riesgo:

- mezclar reglas de URL y reglas de método de forma inconsistente.

Mitigación:

- al cerrar esta fase, debe quedar claro qué parte resuelve URLs y cuál resuelve ownership fino.

## Fase 3 — Construir validación declarativa para errores inline

Reglas wiki:

- [validacion-formularios.md](/Users/keoni/Claude-Workspace/projects/paw-2026a-01/docs/wiki/validacion-formularios.md)
- [comparacion-jsp-formularios-e-i18n.md](/Users/keoni/Claude-Workspace/projects/paw-2026a-01/docs/wiki/comparacion-jsp-formularios-e-i18n.md)

Objetivo:

- reemplazar validaciones que hoy terminan en excepciones por validaciones de form con `BindingResult`, para mantener errores inline.

Decisión:

- **no** usar `ErrorHandlingAdvice` para errores recuperables de formulario.
- `ErrorHandlingAdvice` queda para errores reales, no para feedback normal del usuario.

Archivos probables:

- `webapp/src/main/java/ar/edu/itba/paw/webapp/form/RestaurantForm.java`
- `webapp/src/main/java/ar/edu/itba/paw/webapp/form/ProfileEditForm.java`
- `webapp/src/main/java/ar/edu/itba/paw/webapp/validation/*`
- `services/src/main/java/ar/edu/itba/paw/services/UserServiceImpl.java`
- `services/src/main/java/ar/edu/itba/paw/services/ImageServiceImpl.java` si hace falta alinear reglas

Pasos:

1. Crear un validador custom para upload de imagen:
   - formato válido
   - tamaño máximo
   - aplicable a `RestaurantForm` y `ProfileEditForm`
2. Hacer update-aware la validación de username en edición de perfil.
3. Si hace falta, agregar al form un dato de contexto como `currentUsername` o equivalente para validar sin excepciones.
4. Asegurar que los mensajes queden en `BindingResult` y se muestren con `<form:errors>`.

Salida esperada:

- el controller puede recibir forms inválidos y volver a dibujar la misma JSP con errores inline;
- ya no depende de `try/catch` para imagen inválida, imagen muy grande o username duplicado.

Por qué esta fase es la clave:

- sin esto, sacar `try/catch` obliga a mandar al usuario a páginas de error, que es justo lo que no querés.

## Fase 4 — Sacar `try/catch` de controllers sin romper UX

Reglas wiki:

- [manejo-excepciones.md](/Users/keoni/Claude-Workspace/projects/paw-2026a-01/docs/wiki/manejo-excepciones.md)
- [validacion-formularios.md](/Users/keoni/Claude-Workspace/projects/paw-2026a-01/docs/wiki/validacion-formularios.md)

Objetivo:

- dejar controllers finos: reciben, delegan, responden.

Archivos:

- `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/OwnerDashboardController.java`
- `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/AdminController.java`
- `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/ProfileController.java`
- `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/ErrorHandlingAdvice.java`

Pasos:

1. Eliminar `try/catch` de `UnsupportedImageFormatException` y `UploadTooLargeException` una vez que eso ya sea validación declarativa.
2. Eliminar `try/catch` de `UsernameAlreadyInUseException` una vez que la unicidad ya esté resuelta a nivel form.
3. Eliminar sí o sí el `catch (Exception)` genérico de `ProfileController`.
4. Dejar `ErrorHandlingAdvice` solo para errores no recuperables por formulario.

No hacer:

- no agregar `error/409.jsp` para un username duplicado;
- no cambiar errores de usuario por páginas `413`/`415`.

Salida esperada:

- mismo comportamiento visual para el usuario;
- mejor arquitectura interna.

## Fase 5 — Resolver N+1 en `UserJdbcDao`

Regla wiki:

- [n-plus-1-joins-java.md](/Users/keoni/Claude-Workspace/projects/paw-2026a-01/docs/wiki/n-plus-1-joins-java.md)

Objetivo:

- evitar query extra por roles en cada `find*`.

Archivos:

- `persistence/src/main/java/ar/edu/itba/paw/persistence/UserJdbcDao.java`
- `persistence/src/test/java/ar/edu/itba/paw/persistence/UserJdbcDaoTest.java`

Pasos:

1. Reemplazar `attachRoles(...)` por queries con `LEFT JOIN user_roles`.
2. Usar extractor que reconstruya un `User` con roles acumulados.
3. Mantener orden determinista para roles.
4. Agregar tests de persistencia para usuario con roles y sin roles.
5. Correr:

```bash
mvn -pl persistence test -q
```

Por qué va después de webapp:

- es una mejora clara de persistence, independiente, sin bloquear las fases MVC.

## Fase 6 — Usar locale del owner en mails de approve/reject

Regla wiki:

- [mailing.md](/Users/keoni/Claude-Workspace/projects/paw-2026a-01/docs/wiki/mailing.md)

Objetivo:

- dejar de enviar mails con `es-AR` hardcodeado cuando el destinatario tiene locale preferido.

Archivos:

- `persistence-contracts/src/main/java/ar/edu/itba/paw/persistence/RestaurantDao.java`
- `persistence/src/main/java/ar/edu/itba/paw/persistence/RestaurantJdbcDao.java`
- `services/src/main/java/ar/edu/itba/paw/services/RestaurantServiceImpl.java`
- tests de service si aplica

Pasos:

1. Agregar acceso al locale del owner desde DAO.
2. Usar ese locale en `approve()` y `reject()`.
3. Mantener fallback a `es-AR` si no existe preferencia.
4. Correr:

```bash
mvn -pl services,persistence test -q
```

Recomendación:

- si es fácil, traer email + locale del owner en una sola consulta.

## Fase 7 — Agregar `ON DELETE CASCADE` en `reviews.user_id`

Reglas wiki:

- [persistencia-jdbc.md](/Users/keoni/Claude-Workspace/projects/paw-2026a-01/docs/wiki/persistencia-jdbc.md)
- criterio reflejado también en `docs/raw/correcciones.md`

Objetivo:

- resolver la cascada en schema nuevo y también en PostgreSQL ya desplegado.

Archivos:

- `persistence/src/main/resources/schema-base.sql`
- `persistence/src/main/resources/schema-postgres.sql`
- `persistence/src/main/java/ar/edu/itba/paw/persistence/ReviewCascadeMigration.java`
- `persistence/src/test/java/ar/edu/itba/paw/persistence/ReviewCascadeMigrationTest.java`

Pasos:

1. Cambiar FK en `schema-base.sql`.
2. Agregar migración SQL correspondiente.
3. Extender la migración runtime para cubrir también `reviews.user_id`, no solo `restaurant_id`.
4. Agregar tests para ambos casos.
5. Correr:

```bash
mvn -pl persistence test -q
```

Por qué no alcanza solo con SQL:

- porque hay instalaciones PostgreSQL existentes y hoy la migración runtime no cubre `user_id`.

## Fase 8 — Reescribir tests sin `verify()`

Regla wiki:

- [testing-unitario.md](/Users/keoni/Claude-Workspace/projects/paw-2026a-01/docs/wiki/testing-unitario.md)

Objetivo:

- eliminar `verify()` y `verifyNoInteractions()` de los tests señalados y dejar tests aceptables según la wiki.

Decisión explícita:

- no se busca preservar exactitud de interacción al 100%;
- se prioriza dejar tests sin `verify()` y con asserts razonables de comportamiento.

Archivos:

- `services/src/test/java/ar/edu/itba/paw/services/ReviewServiceImplTest.java`
- `services/src/test/java/ar/edu/itba/paw/services/RestaurantServiceImplTest.java`

Pasos:

1. Quitar `verify()`, `verifyNoInteractions()` y `ArgumentCaptor` donde existan.
2. Reemplazar por:
   - asserts sobre el resultado retornado;
   - o stubs con captura mínima si hace falta, pero sin `verify()`.
3. Eliminar imports de Mockito que queden muertos.
4. Correr:

```bash
mvn -pl services -am test -Dtest="ReviewServiceImplTest,RestaurantServiceImplTest" -Dsurefire.failIfNoSpecifiedTests=false -q
```

Por qué va casi al final:

- así se reescriben una sola vez, ya con el comportamiento final de services estabilizado.

## Fase 9 — Verificación final

Objetivo:

- cerrar el refactor con validación técnica y funcional mínima.

Pasos:

1. Build completo:

```bash
mvn clean package -q
```

2. Suite completa:

```bash
mvn test -q
```

3. Smoke manual:
   - login correcto;
   - crear restaurante con imagen inválida: error inline;
   - editar restaurante con imagen demasiado grande: error inline;
   - editar perfil con username duplicado: error inline;
   - `cuisineLabel.tag` renderiza bien;
   - ownership protegido por Spring Security;
   - approve/reject manda mail con locale del owner;
   - borrar usuario elimina reviews por cascada.

## Orden Final Resumido

1. Fase 0 — Preparación
2. Fase 1 — Sacar Java estático del tag file
3. Fase 2 — Declarativizar access control
4. Fase 3 — Construir validación declarativa para errores inline
5. Fase 4 — Sacar `try/catch` de controllers
6. Fase 5 — Resolver N+1 en `UserJdbcDao`
7. Fase 6 — Usar locale del owner en mails
8. Fase 7 — Agregar `ON DELETE CASCADE` en `reviews.user_id`
9. Fase 8 — Reescribir tests sin `verify()`
10. Fase 9 — Verificación final

## Diff rápido respecto del plan anterior

- `Reordenado`: ahora el plan sigue dependencias reales y no mezcla tasks incompatibles.
- `Corregido`: la limpieza de exceptions ahora depende primero de validación declarativa.
- `Corregido`: se preservan errores inline en vez de páginas de error para casos de formulario.
- `Corregido`: `verify()` se elimina al final, con reescritura de tests sin bloquear el resto del refactor.
- `Aclarado`: `spring-jdbc` y extracción de imagen salen del scope.

## Next actions

- [ ] Confirmar si querés que este sea el plan maestro definitivo.
- [ ] Si sí, ejecutar primero la `Fase 1`.
- [ ] Mantener `1 commit por fase` para rollback limpio.
