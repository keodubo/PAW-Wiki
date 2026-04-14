# Auditoría del Plan de Remediación de Violaciones PAW

- Fecha: `2026-04-12`
- Estado: `Aprobado con ajustes importantes`
- Alcance auditado: código actual del repo
- Supuesto explícito: esta auditoría valida factibilidad técnica y riesgo sobre el estado real del proyecto. No contrasta el plan contra el wiki de PAW porque ese contenido no fue provisto aquí.

## Veredicto Ejecutivo

El plan está bien encaminado en intención, pero **no conviene ejecutarlo tal como está**.

Recomendación:

- **Mantener**: Tasks `2`, `4`, `5`, `6`, `7`, `10`, pero con ajustes.
- **Replantear**: Tasks `1` y `3`.
- **Sacar del plan**: Tasks `8` y `9`.

Riesgo principal del plan original:

- elimina validaciones útiles en tests;
- introduce regresiones de UX en formularios;
- deja incompleta la migración de `reviews.user_id` en bases PostgreSQL ya existentes;
- asume wiring de Spring que hoy no existe en `webapp`.

## Hallazgos Principales

### Críticos

1. **Task 1 está mal planteada: eliminar `verify()` a ciegas debilita tests reales**

   El plan asume que los `verify(...)` son redundantes, pero en el código actual no lo son.

   Evidencia:

   - `services/src/test/java/ar/edu/itba/paw/services/ReviewServiceImplTest.java`
   - `services/src/test/java/ar/edu/itba/paw/services/RestaurantServiceImplTest.java`

   Problema concreto:

   - En `ReviewServiceImplTest`, el assert sobre `page.getCurrentPage()` valida el valor que el servicio devuelve, pero **no valida** que el DAO haya recibido el `PageRequest` clamped.
   - En `RestaurantServiceImplTest`, el stub de `restaurantDao.create(...)` usa `any...`, así que `assertEquals(expected, result)` **no valida** canonicalización de tags, trim de `imageUrl` ni selección de `imageId`.

   Qué hacer:

   - Si `verify()` está prohibido, reemplazarlo por **captura dentro del stub** (`thenAnswer`) o por un **fake DAO** que guarde los argumentos y luego permita asertarlos.
   - No eliminar esas comprobaciones sin reemplazo.

2. **Task 3 propone una regresión funcional visible**

   Hoy los controllers atrapan excepciones de dominio para volver a renderizar el formulario con errores inline:

   - `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/OwnerDashboardController.java`
   - `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/AdminController.java`
   - `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/ProfileController.java`

   El plan propone dejar que `UnsupportedImageFormatException`, `UploadTooLargeException` y `UsernameAlreadyInUseException` caigan en `ErrorHandlingAdvice` y muestren páginas `413`, `415` o `409`.

   Eso cambia el comportamiento actual:

   - se pierde el error sobre el campo;
   - se pierde el formulario cargado;
   - en perfil se cambia una validación de negocio recuperable por una página de error.

   Recomendación:

   - **No mover esta lógica al `@ControllerAdvice` como atajo**.
   - Si la cátedra exige sacar el `try/catch` del controller, la solución correcta es mover estas validaciones a Bean Validation o a un validador de formulario.
   - Como mínimo, sacar solo el `catch (Exception)` genérico de `ProfileController`, que sí es un error claro.

3. **Task 7 está incompleta para esquemas ya existentes**

   El plan modifica `schema-base.sql` y `schema-postgres.sql`, pero el proyecto ya tiene una migración runtime para FKs de `reviews`:

   - `persistence/src/main/java/ar/edu/itba/paw/persistence/ReviewCascadeMigration.java`
   - `persistence/src/main/java/ar/edu/itba/paw/persistence/ReviewCascadeMigrationRunner.java`

   Problema concreto:

   - la migración actual solo revisa `reviews.restaurant_id`;
   - no repara `reviews.user_id` en instalaciones PostgreSQL existentes.

   Recomendación:

   - además de tocar los scripts SQL, extender la migración runtime para `user_id`;
   - agregar tests para ese caso en `ReviewCascadeMigrationTest`.

### Importantes

4. **Task 2 omite que `webapp.util` no está siendo escaneado por Spring**

   El plan quiere crear `webapp/src/main/java/ar/edu/itba/paw/webapp/util/CuisineKeyResolver.java` como `@Component`, pero `WebConfig` hoy solo escanea:

   - `ar.edu.itba.paw.webapp.controller`
   - `ar.edu.itba.paw.webapp.auth`
   - `ar.edu.itba.paw.webapp.validation`

   Evidencia:

   - `webapp/src/main/java/ar/edu/itba/paw/webapp/config/WebConfig.java`

   Recomendación:

   - opción recomendada: registrar `CuisineKeyResolver` como `@Bean` en `WebConfig`;
   - alternativa: ampliar `@ComponentScan` para incluir `webapp.util`.

5. **Task 5 necesita un ajuste para no depender de nombres de parámetros**

   La idea de pasar a `@PreAuthorize` es razonable, pero expresiones como:

   - `@PreAuthorize("@accessHelper.canManageRestaurant(authentication, #id)")`
   - `@PreAuthorize("@accessHelper.canEditReviewerProfile(authentication, #username)")`

   dependen del descubrimiento de nombres de parámetros. En este repo no hay configuración explícita de `-parameters` en Maven.

   Recomendación:

   - usar `#p0`, `#p1`, etc., o confirmar que la resolución de nombres compile/runtime funciona;
   - verificar si conviene `@EnableGlobalMethodSecurity(prePostEnabled = true)` en vez de `@EnableMethodSecurity`, según compatibilidad efectiva con la versión usada.

6. **Task 6 es válida, pero suma consultas y duplica lógica de locale**

   El hardcode `Locale.of("es", "AR")` existe realmente en:

   - `services/src/main/java/ar/edu/itba/paw/services/RestaurantServiceImpl.java`

   Pero agregar `findOwnerPreferredLocale(...)` como consulta separada haría más chatty el flujo.

   Recomendación:

   - preferir un único método DAO que traiga email + locale del owner, o ampliar el método actual;
   - reutilizar una estrategia de parseo consistente con `UserServiceImpl` para no duplicar normalización de locale.

7. **Task 8 no debería seguir como task del plan**

   El propio plan concluye que no conviene cambiar `spring-jdbc`, y además la conclusión es correcta:

   - `webapp/src/main/java/ar/edu/itba/paw/webapp/config/WebConfig.java` usa clases de `spring-jdbc` en compile time;
   - mover esa dependencia a `runtime` rompería compilación.

   Recomendación:

   - sacar esta task del plan de remediación;
   - si se quiere dejar registro, moverla a “deuda técnica evaluada y descartada”.

8. **Task 3 omite archivos adicionales si se crea una vista `409`**

   Si igualmente se insistiera con una `error/409.jsp`, faltan al menos:

   - mensajes i18n en `messages_es.properties` y `messages_en.properties`;
   - tests de vista/handler equivalentes a los que ya existen para `413` y `415`;
   - definición clara de cuándo `409` reemplaza un error inline y cuándo no.

## Matriz por Task

| Task | Estado | Recomendación |
| --- | --- | --- |
| 1. Eliminar `verify()` | `Rehacer` | Reemplazar `verify()` por captura de argumentos en stub o fake DAO; no borrar asserts de interacción sin reemplazo. |
| 2. Sacar Java estático de `cuisineLabel.tag` | `Ajustar` | Válida, pero registrar el resolver como bean real en `WebConfig` o ampliar `ComponentScan`. |
| 3. Mover `try-catch` a `@ControllerAdvice` | `Rehacer` | No usar páginas `413/415/409` como sustituto de errores inline. Resolver con validación de formulario o mantener catches específicos por ahora. |
| 4. Resolver N+1 en `UserJdbcDao` | `Ajustar` | Válida. Agregar tests de roles y asegurar orden determinista en el `JOIN`. |
| 5. Declarativizar acceso con `@PreAuthorize` | `Ajustar` | Válida. Usar expresiones robustas (`#p0`/`#p1`) y confirmar anotación de method security compatible. |
| 6. Fix locale hardcodeado | `Ajustar` | Válida. Preferir una sola query para owner notification data y sumar tests. |
| 7. Agregar `ON DELETE CASCADE` en `reviews.user_id` | `Ajustar` | Válida, pero también hay que actualizar migración runtime y tests. |
| 8. Mover `spring-jdbc` fuera de `webapp/pom.xml` | `Descartar` | No corresponde como remediación ahora. |
| 9. Mover extracción de imagen al servicio | `Descartar` | El plan ya concluye correctamente que el patrón actual es aceptable. |
| 10. Verificación final E2E | `Ajustar` | Mantener, pero sumar pruebas específicas de los cambios nuevos. |

## Plan Revisado Recomendado

### Task 1: Reemplazar `verify()` sin perder cobertura

Objetivo:

- eliminar `verify()` si está prohibido por la cátedra;
- mantener validación de argumentos relevantes.

Pasos recomendados:

1. En `ReviewServiceImplTest`, capturar el `PageRequest` dentro del `when(...).thenAnswer(...)` del DAO y asertarlo después.
2. En `RestaurantServiceImplTest`, capturar dentro del stub de `restaurantDao.create(...)` los argumentos sensibles:
   - `slug`
   - `imageUrl`
   - `imageId`
   - `tags`
3. Eliminar imports de Mockito que queden sin uso.
4. Correr:

```bash
mvn -pl services test -Dtest="ReviewServiceImplTest,RestaurantServiceImplTest" -q
```

### Task 2: Sacar Java estático de `cuisineLabel.tag`

Objetivo:

- quitar scriptlets/declaraciones Java del tag file.

Pasos recomendados:

1. Crear `CuisineKeyResolver` como clase simple.
2. Registrar el bean explícitamente en `WebConfig`:
   - recomendado: `@Bean`
   - alternativa: ampliar `@ComponentScan`
3. Exponer el resolver con `@ModelAttribute` global, idealmente en `CurrentUserAdvice`.
4. Reescribir `cuisineLabel.tag` para usar EL + `spring:message`.
5. Verificar:

```bash
mvn -pl webapp compile -q
```

Riesgo:

- si EL method invocation falla en el container, la alternativa limpia es una EL function/TLD.

### Task 3: Limpiar exception handling de controllers sin romper UX

Objetivo:

- reducir smell arquitectónico sin perder errores inline.

Recomendación por defecto:

- **no mover todavía** `UnsupportedImageFormatException`, `UploadTooLargeException` ni `UsernameAlreadyInUseException` a páginas globales de error;
- **sí eliminar** el `catch (Exception)` genérico de `ProfileController`.

Si la regla PAW exige cero `try-catch` en controllers:

1. Agregar validadores de formulario para imagen inválida / tamaño máximo / username duplicado.
2. Convertir esas validaciones en `BindingResult`.
3. Recién después eliminar los `try-catch` específicos del controller.

Nota:

- `ProfileEditForm` hoy no tiene un validador de unicidad apto para update; el `UniqueUsernameValidator` actual no excluye al usuario actual.

### Task 4: Resolver N+1 de roles en `UserJdbcDao`

Objetivo:

- evitar query extra por roles en cada `find*`.

Pasos recomendados:

1. Cambiar `findByEmail`, `findByUsername`, `findByNormalizedName` y `findById` a `LEFT JOIN user_roles`.
2. Usar extractor que reconstruya el `User` con roles.
3. Mantener orden determinista (`ORDER BY ur.role`) para no meter ruido en tests/debug.
4. Agregar tests que verifiquen que:
   - el usuario vuelve con roles;
   - sin roles sigue devolviendo `Set` vacío.

### Task 5: Declarativizar autorización con `@PreAuthorize`

Objetivo:

- mover control de acceso de lógica manual en controller a seguridad declarativa.

Pasos recomendados:

1. Habilitar method security con la anotación compatible con la versión efectiva.
2. Agregar `@PreAuthorize` en los endpoints hoy protegidos con `AccessHelper`.
3. Preferir expresiones con parámetros posicionales si no se confirma `-parameters`.
4. Eliminar inyecciones de `AccessHelper` solo donde deje de usarse.
5. Verificar acceso con tests MVC.

Riesgo:

- doble lookup del recurso si `AccessHelper` valida y el controller vuelve a cargar la entidad.

### Task 6: Usar locale real del owner en mails de approve/reject

Objetivo:

- dejar de hardcodear `es-AR`.

Pasos recomendados:

1. Agregar método DAO que traiga la info de notificación del owner en una sola consulta.
2. Parsear locale con una estrategia consistente con la ya usada en `UserServiceImpl`.
3. Agregar tests unitarios de:
   - locale explícito del owner;
   - fallback cuando el locale no exista o esté vacío.

### Task 7: Corregir `reviews.user_id` con `ON DELETE CASCADE`

Objetivo:

- alinear esquema base, migración y bases ya desplegadas.

Pasos recomendados:

1. Actualizar `schema-base.sql`.
2. Agregar ajuste correspondiente en `schema-postgres.sql`.
3. Extender `ReviewCascadeMigration` para revisar también `user_id`.
4. Agregar tests para `user_id` además de `restaurant_id`.
5. Correr:

```bash
mvn -pl persistence test -q
```

### Task 10: Verificación final

Checklist mínimo:

1. Build completo:

```bash
mvn clean package -q
```

2. Tests completos:

```bash
mvn test -q
```

3. Verificaciones focalizadas:
   - formularios de restaurante mantienen errores inline;
   - edición de perfil mantiene error inline para username duplicado;
   - `cuisineLabel.tag` renderiza bien;
   - `@PreAuthorize` da `403` donde corresponde;
   - borrar usuario elimina reviews en PostgreSQL legado y nuevo.

## Decisión Recomendada

Lo más sólido para este repo es ejecutar el plan en este orden:

1. `Task 2`
2. `Task 4`
3. `Task 6`
4. `Task 7`
5. `Task 5`
6. `Task 1` ya corregida
7. `Task 3` solo después de definir la estrategia de validación
8. `Task 10`

Motivo:

- primero se corrigen problemas objetivos y de bajo riesgo;
- después se ataca seguridad declarativa;
- se deja para el final la parte más riesgosa de controllers/form UX.

## Diff Rápido del Plan Original

- `Agregado`: hallazgos críticos contra el código real.
- `Corregido`: Task 1 para no perder cobertura de tests.
- `Corregido`: Task 2 para registrar bien el bean en Spring.
- `Corregido`: Task 3 para evitar regresión funcional.
- `Corregido`: Task 5 para no depender de nombres de parámetros.
- `Corregido`: Task 6 para evitar query extra innecesaria y duplicación de lógica.
- `Corregido`: Task 7 para incluir migración runtime y tests.
- `Eliminado del scope`: Tasks 8 y 9 como remediación inmediata.
