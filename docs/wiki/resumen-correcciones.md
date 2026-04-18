---
titulo: Resumen de Correcciones Previas
tipo: fuente
fuentes: [raw/correcciones.md]
creado: 2026-04-09
actualizado: 2026-04-17
---

# Resumen de Correcciones Previas

Segunda pasada exhaustiva sobre `raw/correcciones.md`. Esta pagina busca conservar todos los hallazgos relevantes del fuente, reorganizados por dominio para que no se pierdan entre paginas tematicas mas chicas.

## Hallazgos recurrentes

| Hallazgo | Frecuencia | Tipo | Ver tambien |
|----------|------------|------|-------------|
| `rememberMeKey` como `@Value`, no hardcodeada | 3 veces | Error | [[spring-security]], [[auth-flows]] |
| Validacion de email y cambio de contrasena presentes | 2 veces | Elogio | [[auth-flows]], [[validacion-formularios]] |
| `jvm.config` y `maven.config` vacios en el repo | 2 veces | Error | [[configuracion-maven]] |
| Uso de boxed primitives sin necesidad clara | 2 veces | Error | [[java-style]] |
| Clases utils solo parcialmente no instanciables | 2 veces | Error | [[java-style]] |
| Buena cobertura de tests en DAO | 2 veces | Elogio | [[testing-unitario]] |
| Buen uso de custom validators | 2 veces | Elogio | [[validacion-formularios]] |
| Modificadores de acceso omitidos sin motivo | 2 veces | Error | [[java-style]] |
| Versiones de dependencias repetidas o sobreescritas en poms hijos | 2 veces | Error | [[configuracion-maven]] |
| Faltante de escape de `%` y `_` en LIKE | 2 veces | Error | [[xss-prevencion]] |
| Modulos hijos redeclaran `<version>` heredada del padre | 2 veces | Error | [[configuracion-maven]] |

## Seguridad y control de acceso

### XSS, salida HTML y search

- Un intento de XSS en la creacion de menu termina en `500` en vez de degradar limpiamente.
- Quedo contenido dinamico sin `<c:out>`, marcado explicitamente como vulnerabilidad XSS grave.
- Se imprimen valores en JSP con `$(...)` o `${...}` directo en lugar de `<c:out>`.
- Se imprimen mensajes directamente sin escapar con `<c:out>`.
- Hay campos de texto libre que descansan en patterns de forms; si el pattern falla, la salida sigue quedando abierta a XSS.
- Se repite varias veces que se imprimen valores dinamicos sin escapar en JSP, incluso catalogado como error conceptual grave.
- Falta escapar `%` y `_` en search / LIKE; tambien se observa que algunos grupos no escapan `%%%`.
- Hubo elogios a grupos que escaparon `%` y `_`, aunque se registro que una busqueda como `" OR 1=1` colgaba la app.

### Reglas de autorizacion, ownership y alcance

- No protegen endpoints como `TurnTemplateController`.
- Se repite como error conceptual grave validar ownership o permisos desde el controller: editar lecciones, rooms, suscripciones, rutas con `if (loggedUser != null)`, `clubAdminService.validateAdmin(clubId)`, chequeos de `currentPatient` / `currentMedic`, etc.
- Los JSP hacen chequeos manuales para mostrar u ocultar partes de la UI, duplicando logica de Spring Security y dependiendo de magic strings; se pide usar la taglib de Spring Security.
- Se mezclan reglas entre `WebAuthConfig` y `@PreAuthorize`; la catedra pide consistencia.
- Parte de los `@PreAuthorize` contienen logica de negocio, no solo seguridad.
- Varias correcciones remarcan que validar solo roles no alcanza: falta control fino por ownership del recurso.
- Hay grupos que mueven control de acceso a services o usan validaciones tipo `NotAnAdmin` en controllers, tambien marcadas como mal enfoque.
- Persisten chequeos manuales de "usuario no nulo" o "tiene permisos" en controllers cuando deberian quedar absorbidos por Spring Security.
- Falta validar condiciones de dominio con impacto de seguridad logica, por ejemplo impedir ingreso a eventos llenos o del pasado.
- Se elogia el uso de `AccessHelper`, pero se observa que el bloqueo de usuarios no deberia quedar alli sino en `accountNonLocked`.
- Se marca falta de uso mas profundo de Spring Security para usuarios bloqueados.
- Se reportan fallas reales de ownership:
  - se pueden eliminar viajes ajenos
  - se pueden editar publicaciones ajenas
  - se puede entrar a `Myclubs` sin el rol correcto

### HTTP statuses, reglas y configuracion

- Nota editorial del wiki: usar como regla base `403` para recurso existente sin permiso y `404` para recurso inexistente; las observaciones inversas de la fuente deben leerse como casos concretos de endpoints mal modelados, no como una norma universal.
- Editar un torneo ajeno devuelve `404` cuando deberia ser `403`.
- Buscar un freelancer inexistente devuelve `403` cuando deberia ser `404`.
- Tambien se registra el caso inverso: hay situaciones donde los `403` deberian ser `404`.
- El orden de reglas puede romper autorizacion por rol: `/**.authenticated()` antes de rutas de `ORGANIZER` deja esas rutas solo autenticadas.
- Se detecta configuracion contradictoria al ignorar rutas `403/404` en `WebSecurity` y luego incluirlas en `HttpSecurity`.
- Tambien aparecen rutas que primero son restrictivas y despues terminan en `permitAll()`.
- Es incorrecto implementar un endpoint POST `/logout` manual; Spring Security ya lo resuelve.

### Flujos de cuenta, password y sesion

- Falta recuperacion de contrasena.
- En login falta un boton o CTA claro para volver a la home.
- Al cambiar contrasena deberian pedir la contrasena actual.
- Falta confirmar contrasena.
- `resetPassword` en `UserServiceImpl` mezcla validacion de usuario con el reseteo.
- En reset de password envian `userId` ademas del token, aunque el token ya identifica al usuario.
- Se borran usuarios expirados no verificados en vez de permitir regenerar el token de verificacion.
- La `rememberMe` key aparece hardcodeada y trivial; compromete el "recordarme".
- Credenciales de Gmail, mailing y DB aparecen hardcodeadas o trackeadas en el repo.

### Sesion e i18n con impacto de seguridad

- Se actualiza el locale del usuario en DB durante un `GET /`, marcado como mala practica HTTP y de sesion.

## Arquitectura, MVC y estilo de codigo

### Controllers con logica de negocio

- Se repite multiples veces que hay demasiada logica de negocio en controllers, incluso marcada como error conceptual grave.
- Los ejemplos citados incluyen `viewCommunityFollowers`, `JobApplicationController` "en todos sus metodos", gran parte de `UserController`, `event/{id}` y `editEnterprise`.
- Se valida un form a mano en el controller en vez de usar custom validator.
- Se hacen multiples llamados a services para una unica creacion donde deberia existir un metodo transaccional unico.
- En `EventController` aparece `getEventIdByResponseId` como chequeo que no deberia vivir en controller.
- En `newEditProfile` y `uploadGameImage` el controller guarda imagen, actualiza nombre o edita juegos favoritos.
- En `tournamentStart` el controller busca el torneo y luego valida el `tournamentId`.
- `TripsController` popula listas pasadas por referencia, marcado como violacion grave de OOP / SOLID.

### Creacion de modelos, DTOs y limites de capa

- `RoomController` instancia objetos de dominio, marcado como error conceptual grave.
- Se critica crear modelos en controllers tanto para altas como para updates, porque aparecen entidades fuera de persistencia y con informacion parcial.
- Las clases `data` son descritas como una capa extra tipo DTO sin necesidad clara.
- Tambien se critica instanciar entidades de dominio en services; esa responsabilidad deberia quedar en DAO.
- Se sugiere que builders o `fromModel` podrian mejorar la creacion de DTOs, aunque no siempre sean estrictamente necesarios.
- Implementar services en la capa web rompe el modelo jerarquico.
- `interfaces` dependiendo de `spring-jdbc` o `spring-web` expone infraestructura y viola limites de capa.
- Existen services que hablan en terminos de vistas y recursos web.
- `EmailService` conoce nombres y paths relativos de templates de `webapp`, otra violacion de capas.
- La actualizacion de perfil deberia quedar encapsulada en un unico metodo de service.
- Se critican services con getters triviales o wrappers como `UserTripsData` que no representan modelos del sistema.

### Excepciones y responsabilidades web

- Se manejan rutas para `PageNotFound` manualmente en vez de delegarlo al framework.
- En `web.xml` mapean tanto `Exception` como `Throwable` a `500`; uno de los mappings sobra.
- `ErrorController` esta mal nombrado porque en realidad es un `ControllerAdvice`.
- `SecurityErrorBridgeController` se marca como innecesario.
- `ExpiredPassTokenException` tiene un campo privado sin `final` ni setter.
- `VerificationServiceImpl` arroja una `RuntimeException` cuando corresponderia un `NotFound` custom o equivalente.
- El locale usado al enviar mails al dueno es el del cliente; al ser asincrono, deberia ser el del destinatario.
- Los emails se envian con idioma del sender.
- El email service toma locale de `LocaleContextHolder` o del controller en lugar del `preferredLanguage` del destinatario.
- Diferenciar `MailService` de `AsyncMailService` en la interfaz expone un detalle de implementacion.
- `ContactService` hace loops sincronicos de envio de mails; se recomienda moverlos a un hilo asincrono.

### Estilo Java, consistencia y mantenibilidad

- Se marca abuso de magic strings repetidos en varias capas.
- Hay numeros magicos en controllers sin explicacion.
- `orderBy` deberia ser un enum.
- Varias listas de strings para dias deberian reemplazarse por enums.
- El string `tab` deberia ser un enum.
- No usar `SwapStatus` y persistir strings deja pasar valores invalidos.
- Hay constantes en controllers sin uso.
- Se inyectan services o atributos que despues no se usan.
- Los metodos de interfaces no necesitan declararse `public`.
- `ScheduleServiceImpl` es descrito como extremadamente complejo, con pobre modularizacion y modelos anemicos.
- No se siguen convenciones de nombres de Java: aparecen clases como `Relation_Task`, uso de `snake_case` y modelos en plural (`Users` en vez de `User`).
- Se critica el mal uso de `Optional`: usar `get()` sin `isPresent()`, tener fields `Optional` en entidades, mezclar `null`, `Optional`, excepciones, `Empty` u objetos con ID negativo para representar ausencia.
- Hay funciones con auto-unboxing que pueden disparar `NullPointerException`.
- Los metodos `create` de services son inconsistentes: algunos retornan la entidad creada y otros `void`.
- Existen metodos duplicados segun parametros cuando podria haber uno solo con parametros opcionales.
- Obtener el usuario autenticado deberia extraerse a `@ModelAttribute` o `ControllerAdvice`; `getCurrentUser` se critica como innecesario.
- `LoggedUserAdvice` mezcla usuario logueado con exception handling.
- Los `@ControllerAdvice` presentes ni siquiera son detectados por Spring por estar fuera del package scan.
- Se configuran `exception resolvers` y `exception handlers` para las mismas excepciones en distintos puntos.
- Se critican `try/catch` en controllers y un uso excesivo de `try/catch` solo para loggear sin devolver el error formalmente a MVC.
- `NoExistingJourneyValidator` catchea `Exception` plana, indicando falta de custom exceptions.
- Se desaprovecha la oportunidad de agrupar excepciones en una jerarquia.
- Se overridea `equals()` sin `hashCode()`.
- Hay restricciones validadas en services y no en forms, forzando exception handling feo en controllers.
- Tambien se repite que las validaciones de formularios no deberian vivir en controllers sino en constraint validators.

## Persistencia, DB y performance

### Ubicacion del schema y aislamiento de JDBC

- `schema.sql` aparece en `webapp` cuando deberia vivir en persistence.
- Se repite que scripts tipo `schema.sql` deben restringirse a `resources` del modulo de persistence; incluso se menciona un `schema.sql` vacio.
- Se usa `java.sql` en models o fuera de persistencia.
- Se manejan excepciones SQL en `webapp` y se usa `java.sql.Date` fuera de la capa.
- Se usa `java.sql.Timestamp` en models para convertir `Instant`, cuando deberian usarse `LocalDate` / `LocalDateTime`.

### Queries, joins y filtrado

- Se critica reiteradamente resolver joins en Java o mediante N accesos a DB.
- En `ActivityService`, `categoriesIds.stream().map(...)` causa N+1; se recomienda un `findByIds` con `WHERE IN`.
- Tambien se menciona `BookingService` en el mismo patron.
- Hay filtros de datos y joins hechos en Java que deberian bajar a la capa de persistencia.
- Se trae un volumen grande de examenes pendientes y luego se filtra en memoria.

### Transacciones, DAOs y contratos

- Se usan DAOs para preparar precondiciones de tests de persistence en lugar de scripts.
- Falta `@Transactional(readOnly = true)` en lecturas.
- Algunos grupos solo usan `@Transactional` en 4 metodos o no la usan en absoluto.
- La expectativa es que todos los metodos publicos de service queden bajo `@Transactional`, no solo escrituras.
- Faltan transacciones en inserts, deletes y updates.
- Se eliminan relaciones en cascada a mano en lugar de usar `ON DELETE CASCADE`.
- Los modelos no guardan solo IDs de referencia y fuerzan composicion, lo que acopla rowmappers y obliga a traer entidades relacionadas.
- Se esperaba mayor testing y mejor separacion de `TradeDaoJdbcImpl`.
- Cada tabla deberia ser modificada por un solo DAO; se detectan DAOs tocando tablas ajenas.
- No catchear `ForeignKeyException` en DAO termina generando `500` en vez de un `400` amigable.
- En `TournamentJdbcDao.getRound` se usa `RowCallbackHandler` con closure donde seria mas razonable `ResultSetExtractor`.
- `SimpleJdbcInsert` se inicializa en cada llamada en lugar de una sola vez, con riesgo de concurrencia.
- Se usa `jdbcTemplate.update` para inserts donde se prefiere `jdbcInsert`.
- Hay row mappers que no son `private static final` y se instancian repetidamente, con riesgo de heap.
- Incluso se sugiere definir los rowmappers estaticos junto a los strings SQL estaticos.
- `JdbcDaoUtils` usa `StringBuilder` donde no hay dinamismo real.
- Persistence recibe `offset` crudo; se sugiere abstraer paginado en una interfaz tipo `Page`.
- Se retornan colecciones sin paginacion adecuada y se usan "secciones" como magic strings.
- `EmailServiceImpl` aparece anotado con `@Transactional` aunque no usa DB y sus metodos son `@Async`.

## Infraestructura, configuracion y repo hygiene

### Logging

- Se loguea a `DEBUG` en produccion.
- Se loguea a `INFO` por salida estandar en produccion.
- Se usa `printStackTrace()` / salida estandar en lugar de logger formal, marcado como error grave.
- Aunque existen dependencias de Logback, no hay configuracion y todo cae en `catalina.out`.

### Repo hygiene y build

- Se trackean `.vscode`, `.mvn`, `.PVS-Studio`, `bin`, `out` y otros directorios generados por IDEs.
- Se suben archivos vacios de configuracion Maven.

### Maven y wiring

- Se repite la mala practica de definir versiones de dependencias en poms hijos.
- El pom de `models` declara Java 17 cuando el global deberia ser 21 y ademas define JUnit 5 de forma aislada.
- Esta duplicada `validation-api`.
- Se agrega SpringFramework a `interfaces` innecesariamente.
- Aparece dependencia de Spring Boot donde se esperan librerias canonicas puras.
- `hsqldb` aparece en `compile` en vez de `test`.
- El package de configuraciones Spring se escanea a si mismo sin necesidad.
- Se crea un bean para `CharacterEncodingFilter` que no se usa porque ya esta en `web.xml`.
- Hay multiples scans repetidos sobre los mismos packages.
- Se observa uso de `ConcurrentMapCacheManager` para cachear imagenes; al no evictar, puede terminar en OOM.

## UI, UX y flujos vitales

### Layout, URLs y navegacion

- La ruta `01/restaurants/1/reservations/create` no tiene favicon.
- El ancho del contenido es inconsistente.
- Aparece doble barra `//` en search y otras URLs, generando `400`; se recomienda `c:url`.
- Tambien se marca el uso de `${pageContext.request.contextPath}` en vez de `<c:url>`, con fallas en proxys y ruteo.

### i18n, copy y presentacion

- Se imprimen plurales estaticos en vez de usar pluralizacion dinamica (`ChoiceFormat`).
- El wording "invitaciones" vs "solicitudes" esta mal contextualizado.
- Hay errores de encoding con tildes y `n`.
- La aplicacion no queda realmente internacionalizada pese a definir `MessageSource`, porque apunta a archivos inexistentes.
- Titulos de paginas y `alt` texts no estan internacionalizados.
- Se derraman mensajes default o crudos como `email.duplicate` hacia el frontend.

### Calidad del flujo frontend

- Faltan CTAs claros en zero states.
- Existe un sort `relevance` sin criterio real de relevancia.
- Faltan filtros como precio o rating en listados extensos.
- El numero de digitos del review deberia redondearse a 2 para una visualizacion mas limpia.

### Flujos de cuenta, reservas y comunicacion

- El flujo de registro "registro mi empresa" cae en una pantalla bloqueante y deberia redirigir o auto-loguear.
- Tras restablecer contrasena, seria deseable loguear al usuario automaticamente.
- El usuario anonimo ve tabs inaccesibles y enganiosas; deberian ocultarse con taglibs de autorizacion.
- Si el owner cancela una reserva, el usuario cancelado deberia recibir mail.
- Los emails enviados deberian tener formato HTML con header y CTA tipo boton, no links sueltos.
- El mail pidiendo pago deberia incluir detalles y CBU en HTML.

## Testing

- Aparecen tests con clases de JUnit 3.
- Los tests de persistencia ejecutan operaciones sin validar el estado final de la DB, marcado como error grave.
- Falta testear casos importantes en persistence, por ejemplo crear entidades principales complejas.
- Falta cobertura de servicios complejos y caminos no felices.
- Se critica el uso de `Mockito.verify()` / `never()` por acoplarse a implementacion en vez de validar comportamiento.
- La cobertura general se percibe como escasa, con servicios sin tests o con tests comentados.
- Hay tests "unitarios" que en realidad llaman multiples clases o dependen de otras.
- Se insertan datos por `jdbcTemplate` para preparar precondiciones en lugar de partir de scripts SQL canonicos.
- Se pide explicitamente usar `@Rollback` en tests DAO para evitar dependencias entre casos.

## Buenas practicas elogiadas

- Buen setup de flujo QA / bugtracker, con `Done`, Features y Chores bien separados.
- Buen mailing con flujos y plantillas L10n bien compuestas, y CTAs que redirigen bien.
- Componentes frontales robustos: creacion asincrona, paginacion bien resuelta, autocomplete y filtros dinamicos sin refresh agresivo.
- Custom validators muy bien abstraidos para description, match passwords y validaciones similares.
- Buen manejo de carga de imagenes filtrando extensiones no genuinas desde `Multipart`.
- Views e i18n con CTAs, zero states amigables, redirecciones 404/403 cuidadas y jerarquias de roles bien manejadas.

## Resolución y Hardening (Fase 10)

Al 2026-04-17 se ha completado una pasada de remediación exhaustiva centrada en el sistema de reservas pero extendida a toda la arquitectura web:

- **Seguridad**: Se migró a un modelo 100% declarativo en `WebAuthConfig`. Se eliminó `@PreAuthorize` de los controllers nuevos y se centralizó el bypass de `ROLE_ADMIN` en `AccessHelper`.
- **Manejo de Errores**: Se implementó un `ErrorController` real para manejar forwards de Spring Security (`/error/403`, etc.), asegurando que las excepciones de seguridad se integren con el flujo MVC y los tests de integración.
- **XSS y Salida**: Se auditó el uso de `<c:out>` en todas las vistas nuevas, protegiendo campos de texto libre como comentarios de reservas y links externos.
- **i18n**: Se implementó pluralización dinámica (`ChoiceFormat`) y se garantizó la simetría entre `messages_en.properties` y `messages_es.properties` mediante tests automáticos.
- **Logging**: Se estandarizó el uso de SLF4J en la capa de servicios, eliminando salidas por consola remanentes.
- **Estabilidad de Tests**: Se estabilizó la suite de 140 tests de `webapp`, resolviendo problemas de flushing de contexto y orden de filtros.

## Ver tambien

- [[plan-implementacion-reservas]]
- [[tp1-vs-tpe2-final]]
- [[auth-flows]]
- [[java-style]]
- [[ux-flows]]
- [[spring-security]]
- [[xss-prevencion]]
- [[logica-en-controllers]]
- [[transactional]]
- [[n-plus-1-joins-java]]
- [[testing-unitario]]
- [[validacion-formularios]]
- [[persistencia-jdbc]]
- [[mailing]]
- [[internacionalizacion]]
- [[configuracion-maven]]
- [[manejo-excepciones]]
- [[logging]]
- [[buenas-practicas]]
- [[comparacion-seguridad-web]]
- [[comparacion-jsp-formularios-e-i18n]]
- [[comparacion-capas-web-services-persistence]]
- [[comparacion-testing-servicios-y-daos]]
- [[resumen-apuntes]]
- [[resumen-notas]]
- [[resumen-notas-sprint-1]]
- [[resumen-enunciado]]
- [[criterios-evaluacion]]
- [[remediacion-violaciones-paw]]

- [[2026-04-17_cierre-implementacion-reservas_v1]]
