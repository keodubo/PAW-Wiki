---
titulo: Resumen de Notas de Clase
tipo: fuente
fuentes: [raw/notas.txt, raw/pdfs/Notas clases PAW.pdf]
creado: 2026-04-09
actualizado: 2026-04-13
---

# Resumen de Notas de Clase

Notas detalladas clase a clase del curso PAW. A diferencia de los apuntes teoricos, contienen el recorrido practico del profesor: comandos reales, walkthroughs de codigo, explicaciones de *por que* las cosas funcionan asi, y advertencias explicitas de errores que penalizan.

Nota de fuente: el `.txt` usado para trabajar coincide con el PDF original al normalizar espacios, ligaduras y puntuacion tipografica.

## Posicion editorial

- Esta es la **fuente practica principal** del wiki para TP1.
- Complementa a [[resumen-apuntes]] con walkthroughs, decisiones de setup, advertencias y criterios operativos.
- No reemplaza a los apuntes: sirve para aterrizar la teoria en implementacion concreta.
- Las transcripciones siguen siendo fuente auxiliar y de menor confianza: [[resumen-transcripciones-clases-2-a-4]].
- La secuencia sugerida de estudio esta sintetizada en [[tp1-vs-tpe2-final]].

## Clases cubiertas

### Clase 2 — Setup Maven y Spring MVC
- `mvn archetype:generate` para proyecto multi-modulo
- Configuracion de `web.xml` (DispatcherServlet, context-param)
- `@Controller`, `@RequestMapping`, `ModelAndView`, `ViewResolver`
- Mover JSPs bajo `WEB-INF/jsp` para que no sean accesibles por URL directa
- `load-on-startup` y `@ComponentScan` acotado como parte del arranque ordenado de Spring MVC
- Explicacion de DI con analogia Auto/Motor (el auto no crea el motor, lo recibe)

### Clase 3 — Interfaces, scopes Maven, DI
- Programar contra interfaces: `webapp` y `services` compilan contra `interfaces`; las implementaciones concretas entran en runtime
- Maven scopes (`compile`/`runtime`) como mecanismo de enforcement de capas
- `dependencyManagement` en el pom padre para centralizar versiones sin forzar dependencias a todos los modulos
- `@Service`, `@Repository`, `@Autowired` (field, setter, constructor)
- `@Qualifier`, `@Primary` para resolver ambiguedades
- `@RequestParam`, `@PathVariable`, `ResourceHandler` para archivos estaticos

### Clase 4 — JDBC, testing, Mockito
- `JdbcTemplate`, `RowMapper` como interfaz funcional, `SimpleJdbcInsert`
- Prevencion de SQL injection via placeholders (`?`)
- HSQLDB como base embebida para tests de persistencia
- Spring Test: `TestConfig`, `DataSourceInitializer`, `JdbcTestUtils`
- Mockito: `@Mock`, `@InjectMocks`, `when().thenReturn()`
- **Advertencia explicita**: `verify()` esta mal y sera penalizado
- Evolucion del esquema en la cursada:
  - el ejemplo didactico empieza con un `schema.sql` compartido en `src/main/resources` del modulo persistence
  - en el repo final conviene expresarlo como `schema-base.sql` + script dialectal por motor, cargados juntos por tests y por WebConfig

### Clase 5 — Formularios, Bean Validation, i18n
- `@ModelAttribute` funciona como entrada Y salida (se agrega automaticamente al MAV)
- Beans (constructor default + getters + setters) vs Records (para REST API mas adelante)
- JSR 303 con implementacion Hibernate Validator
- **Groups** para validaciones condicionales (crear vs editar perfil)
- `@Valid` + `BindingResult` eliminan toda validacion manual
- Spring form taglib: `<form:form>`, `<form:input path="...">`, `<form:errors>`
- Preservacion automatica de valores ante errores de validacion (UX)
- Custom validators para reglas de negocio (`@UniqueUser`, `@FieldsMustMatch`)
- i18n correcta: **internacionalizar el mensaje completo** con placeholders, nunca concatenar
- `ReloadableResourceBundleMessageSource` con archivos en `i18n/`
- Validation messages se resuelven via MessageSource (claves coinciden con anotaciones)
- `@ModelAttribute` a nivel metodo para atributos compartidos en todas las vistas del controller

### Clase debida — Spring Security
- Patron ACL: roles (granulares, no 1:1 con perfiles), recursos, permisos
- Historia ACEGI -> Spring Security; versionado independiente
- `WebAuthConfig` separado de `WebConfig`, registrado en `web.xml`
- `@EnableWebSecurity` + `WebSecurityConfigurerAdapter`
- `HttpSecurity` como interfaz fluida: reglas de URL, login, logout, rememberMe
- **Orden de reglas**: mas especificas primero; `/**` al final
- Spring Security maneja POST login/logout automaticamente (no crear controllers)
- `UserDetailsService`: interfaz funcional, adapter entre la BD y Spring Security
- `PawAuthUser` extiende User de Spring Security (evitar conflicto de nombres)
- Constructor largo con flags: `accountNonLocked`, `enabled`, etc.
- Roles hardcodeados inicialmente, luego granularizar
- **springSecurityFilterChain** en `web.xml` como `<filter>` con `<filter-mapping>`
- `SecurityContextHolder.getContext().getAuthentication()` para obtener usuario actual
- Mensaje de error generico en login (no revelar si el mail existe o no)
- RememberMe: cookie con key criptografica; si la key cambia, las cookies anteriores se invalidan. Generar key con `openssl rand -base64 2048` y leerla de archivo
- `PasswordEncoder` como `@Bean` BCryptPasswordEncoder
- Inyectar `PasswordEncoder` en `UserServiceImpl` para hashear al crear/modificar
- Expression Language: `.access("@AccessHelper.canEdit")` para ownership checks

### Clase — Logging
- `java.util.logging` limitado: config global por JVM, no portable
- Log4J: config XML embebida pero abandonado por limitaciones de diseno
- **SLF4J**: interfaz estandar; bridges para java.util.logging y Log4J
- **Logback**: implementacion de referencia de SLF4J
- Interpolacion con `{}` placeholders (no concatenar strings):
  `LOGGER.debug("user {}", user.getEmail())`
- Lambdas para operaciones costosas: `LOGGER.debug("data {}", () -> heavyOp())`
- **No usar System.out**: es sincronico, bloquea hilos, llena disco, corrompe BD
- Configuracion dual:
  - `logback-test.xml` en `src/main/resources` para desarrollo (DEBUG, consola)
  - `logback.xml` para produccion (WARNING, RollingFileAppender)
  - Maven excluye `logback-test.xml` del war: `<packagingExcludes>**/logback-test.xml</packagingExcludes>`
- RollingFileAppender con politica por tiempo (1 archivo/dia, historial de 5 dias)
- Nombres unicos por grupo: `logs/paw2024a-03.warnings`
- `additivity="false"` para evitar logs duplicados cuando un logger y root capturan el mismo mensaje
- Pattern: `%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n`
- Spring usa java.util.logging internamente; bridge `jul-to-slf4j` lo redirige a Logback

### Clase — AOP y @Transactional
- Aspectos resuelven concerns transversales que OOP no maneja bien (timing, transacciones, caching)
- Java Proxy (java.lang.reflect.Proxy) como mecanismo: Spring genera decorators en runtime
- La idea de proxy explica por que solo cuentan llamadas que pasan por la interfaz y vienen desde afuera del bean
- `@EnableTransactionManagement` en WebConfig + `PlatformTransactionManager` bean
- **Limitacion del proxy**: solo aplica a metodos publicos de la interfaz; `this.otroMetodo()` no pasa por el proxy
- Metodos privados anotados con `@Transactional` no tienen efecto (sin error ni warning)
- `@Transactional(readOnly = true)` importante para Hibernate JPA en segunda entrega
- Otros aspectos disponibles: `@Async` (fire and forget), `@Scheduled` (cron), `@Cacheable`
- Stack traces mas largos por overhead de proxies (normal, no alarmar)

### Clase — @ControllerAdvice como AOP
- `@ControllerAdvice` es un Advice (terminologia AOP: Advice + PointCut + Weaving)
- `@ExceptionHandler(UserNotFoundException.class)` mapea excepcion a vista/status
- `@assignableTypes` para restringir a ciertos controllers
- El `@ComponentScan` lo detecta automaticamente si esta en el paquete de controllers

## Alineacion editorial

- Las clases 2 y 3 son el lado practico de [[spring-web-mvc]], [[configuracion-maven]] e [[inyeccion-dependencias]].
- La clase de AOP complementa [[spring-aop]] y explica el mecanismo que hace funcionar [[transactional]].
- Estas notas siguen enfocadas en ejemplos practicos; la cobertura teorica mas amplia de unidades 8-11 vive en [resumen-apuntes.md](/Users/keoni/Claude-Workspace/projects/paw-2026a-01/docs/wiki/resumen-apuntes.md:1).

## Ver tambien
- [[tp1-vs-tpe2-final]]
- [[comparacion-testing-servicios-y-daos]]
- [[comparacion-jsp-formularios-e-i18n]]
- [[resumen-apuntes]]
- [[resumen-correcciones]]
- [[resumen-enunciado]]
- [[resumen-transcripciones-clases-2-a-4]]
- [[spring-security]]
- [[spring-web-mvc]]
- [[spring-aop]]
- [[internacionalizacion]]
- [[logica-en-controllers]]
- [[validacion-formularios]]
- [[logging]]
- [[transactional]]
- [[testing-unitario]]
- [[persistencia-jdbc]]
- [[manejo-excepciones]]
- [[resumen-notas-sprint-1]]
