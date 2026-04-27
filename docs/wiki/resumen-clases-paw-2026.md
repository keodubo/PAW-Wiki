---
titulo: Resumen de Clases PAW por Etapa
tipo: fuente
fuentes: ["raw/PAW - clase 1 (TP1).pdf", "raw/PAW - clase 2 (TP1).pdf", "raw/PAW - clase 3 (TP1).pdf", "raw/PAW - clase 4 (TP1).pdf", "raw/PAW - clase 5 (TP1).pdf", "raw/PAW - clase 6 (TP1).pdf", "raw/PAW - clase 7 (TP2).pdf", "raw/PAW - clase 8 (TP2).pdf", "raw/PAW - clases 9 y 10 (TP final).pdf", "raw/PAW- Clase Teórica Front end (TP final).pdf", "raw/PAW- Clase Teórica - SPAs segunda parte (TP final).pdf"]
creado: 2026-04-27
actualizado: 2026-04-27
---

# Resumen de Clases PAW por Etapa

Esta pagina ingiere los PDFs nuevos de clases PAW agregados en `docs/raw/`. Son fuente prioritaria porque reflejan el recorrido de la catedra clase por clase. El objetivo no es reemplazar las paginas conceptuales existentes, sino darles un nodo fuente detallado, navegable y ordenado por etapa del proyecto.

## Posicion editorial

- Estas clases pasan a ser fuente de verdad fuerte para interpretar como trabaja la catedra en cada etapa: **TP1**, **TP2** y **TP final**.
- Las versiones concretas de dependencias que aparecen en los PDFs se consideran **historicas**. No usarlas como recomendacion vigente sin contrastar el checkout, el enunciado actual y el ecosistema actual.
- Para TP1, estas clases refuerzan el camino clasico: Maven multi-modulo, Spring MVC server-side, JSP/JSTL, JDBC, HSQLDB, Bean Validation, Spring Security, Logback y AOP transaccional.
- Para TP2, el foco cambia a migrar persistencia JDBC hacia JPA/Hibernate, entender `EntityManager`, mapear entidades y auditar queries generadas.
- Para TP final, el foco cambia a API REST + SPA: recursos HTTP, JSON, autenticacion stateless, frontend como thick client, estado del cliente, routing, i18n, testing frontend, optimizacion, cache y empaquetado del frontend.

## Nota de extraccion

- Las clases 1, 3, 4, 5, 6, 7, 8, 9/10 y las dos teoricas de final tienen texto embebido utilizable.
- `raw/PAW - clase 2 (TP1).pdf` no trae texto embebido; se extrajo por OCR local. La lectura es suficiente para conceptos de JSP/JSTL, pero no debe usarse para copiar snippets exactos de codigo sin revisar visualmente el PDF.
- No se modifico ningun archivo en `docs/raw/`.

## Mapa rapido por etapa

| Etapa | Clases | Stack dominante | Que cambia respecto de la etapa anterior |
|-------|--------|-----------------|------------------------------------------|
| TP1 | 1 a 6 | Spring MVC, JSP/JSTL, JDBC, Maven, HSQLDB, Spring Security, Logback | Construccion server-side clasica con capas estrictas y validacion web |
| TP2 | 7 a 8 | JPA/Hibernate sobre Spring ORM | Persistencia deja de ser SQL/JDBC directo y pasa a entidades managed + contexto de persistencia |
| TP final | 9/10 + teoricas frontend/SPAs | Jersey/JAX-RS, API REST, SPA, bundlers, frontend state | La UI deja de ser JSP server-side y se apoya en API stateless + cliente con estado |

## TP1 - Clase 1: Spring Web, Maven y capas

### Maven y dependencias

- El recorrido empieza con un pom padre y un modulo `webapp`; Maven se usa como ciclo de vida completo, no solo como descargador de jars.
- El `dependencyManagement` del padre fija dependencias disponibles, pero no las agrega automaticamente a los modulos.
- Los modulos hijos declaran solo lo que usan y heredan versiones desde el padre.
- La regla actual del wiki: tomar la **forma** del setup, no las versiones exactas escritas en el PDF.

### Spring MVC inicial

- `web.xml` registra el `DispatcherServlet` como Front Controller.
- `load-on-startup` fuerza inicializacion temprana.
- `AnnotationConfigWebApplicationContext`, `contextConfigLocation` y `ContextLoaderListener` conectan el despliegue web con configuracion Java.
- `WebConfig` habilita MVC, define `@ComponentScan` y luego agrega `ViewResolver`.
- Los controllers se declaran con `@Controller` y `@RequestMapping`; devuelven `ModelAndView` con nombre logico de vista y modelo.
- Las JSP se ubican bajo `WEB-INF/jsp/` para evitar acceso directo por URL.

### Multi-modulo y scopes

- La clase crea modulos de `services`, `persistence`, `interfaces` y `models`.
- El modulo `webapp` compila contra contratos y modelos, pero las implementaciones concretas entran en `runtime`.
- Ese scope `runtime` es deliberado: si un controller intenta instanciar una implementacion de servicio, Maven falla al compilar aunque el IDE pueda tolerarlo.
- La inyeccion con Spring reemplaza `new UserServiceImpl()` por `@Autowired` sobre la interfaz.

### DI y resolucion de beans

- `@Service` y `@Repository` marcan implementaciones para que Spring las registre.
- `@Autowired` conecta controller -> service -> DAO.
- Si hay dos implementaciones candidatas, Spring falla por ambiguedad; se resuelve con `@Qualifier` o `@Primary`.
- `@RequestParam` y `@PathVariable` se introducen como mecanismos de binding de request hacia parametros del controller.

## TP1 - Clase 2: Servlets, JSP, EL, JSTL y tags propios

### JSP como servlet generado

- JSP permite combinar HTML estatico con contenido dinamico.
- La pagina JSP se traduce a un servlet y luego se compila.
- Los objetos implicitos principales son `request`, `response`, `session`, `out` y `application`.
- Esta mecanica explica por que mezclar Java arbitrario en vistas escala mal: se termina escribiendo logica dentro de un servlet generado.

### Directivas y construcciones JSP

- Comentarios JSP: sirven para documentar la vista sin exponer el comentario en la salida HTML.
- Expresiones: evaluan Java y escriben en la salida; el wiki las trata como historicas, no como patron recomendado.
- Scriptlets: permiten codigo Java en la vista; para el criterio actual, evitarlos.
- Declaraciones: agregan miembros a la clase servlet generada.
- Directivas `page`, `include` y `taglib`: configuran imports/headers/sesion, incluyen fragmentos en traduccion o habilitan librerias de tags.
- Acciones JSP se ejecutan en cada request y pueden incluir comportamiento dinamico.

### Beans y Expression Language

- Los objetos expuestos a vista deben seguir convencion Bean: constructor default, propiedades privadas, getters y eventualmente setters.
- EL simplifica acceso a propiedades: `objeto.propiedad`, propiedades anidadas y colecciones indexadas.
- EL soporta operadores aritmeticos, relacionales, logicos y `empty`.
- Aun cuando EL sea comoda, valores dinamicos de usuario deben renderizarse con tags que escapen salida, como `c:out`.

### JSTL y taglibs

- JSTL evita codigo Java en JSPs y concentra operaciones comunes en tags.
- Familias destacadas:
  - `core`: salida, condicionales, iteracion, variables y URLs.
  - `xml`: procesamiento XML.
  - `i18n`: internacionalizacion y formato.
  - `database`: acceso a DB, pero no usarla en la arquitectura del TP porque rompe capas.
  - `functions`: utilidades de colecciones y strings.
- Tags relevantes para TP1:
  - `c:out`: imprime resultado de una expresion y ayuda a evitar XSS.
  - `c:if`: evalua contenido si la condicion es verdadera.
  - `c:choose`: modela ramas mutuamente excluyentes.
  - `c:forEach`: itera colecciones.
  - `c:set`: define variables.
  - `c:url`: genera URLs relativas y permite agregar parametros.

### Tags propios

- La API de JSP permite crear tags custom.
- Un tag handler class implementa la logica del tag.
- Un TLD define nombres, atributos y clase handler.
- El TLD debe vivir bajo `WEB-INF`.
- Para el TP, esto importa como criterio de reutilizacion de UI: si un patron de JSP se repite mucho, un tag propio puede ser mejor que copiar bloques.

## TP1 - Clase 3: Spring JDBC y testing

### JDBC con Spring

- La clase introduce `spring-jdbc`, driver PostgreSQL, `DataSource`, `JdbcTemplate`, `RowMapper` y `SimpleJdbcInsert`.
- `JdbcTemplate` reduce boilerplate JDBC y concentra ejecucion de queries.
- `RowMapper` transforma una fila del `ResultSet` en un objeto del dominio.
- El `DataSource` se configura como bean para que Spring pueda construir DAOs.
- En el ejemplo didactico aparece configuracion directa en `WebConfig`; en un repo real, credenciales y secrets no deben quedar hardcodeados.

### Creacion de entidades y schema

- Para inserts, `SimpleJdbcInsert` evita armar manualmente `INSERT` + recupero de id.
- Inicializar helpers JDBC una sola vez por DAO, no por request ni por metodo.
- La clase primero crea tablas desde el DAO, pero luego corrige el enfoque: el schema no debe estar enterrado en el constructor de un DAO.
- La solucion didactica termina en `schema.sql` cargado por `@Sql` en tests y por `DataSourceInitializer` en la app.
- En este repo, la version robusta es separar schema base y dialectos (`schema-base.sql`, `schema-postgres.sql`, `schema-hsqldb.sql`) para no mezclar SQL incompatible entre motores.

### Testing unitario

- Un buen test unitario testea una sola cosa, es deterministico, ejercita una sola llamada principal y valida pocas postcondiciones.
- El formato esperado es:
  1. precondiciones,
  2. ejercitacion,
  3. postcondiciones.
- Para servicios, se usan mocks de DAOs; para DAOs, se usa una DB embebida real.
- Mockito aparece con `@Mock`, `@InjectMocks`, `MockitoJUnitRunner` y `when(...).thenReturn(...)`.
- La regla editorial del wiki se mantiene: preferir asserts sobre comportamiento/estado observable antes que `verify()` acoplado a implementacion.

### Tests de persistencia

- Mockear JDBC completo no es razonable: habria que simular `Connection`, `Statement`, `ResultSet` y detalles internos de helpers.
- HSQLDB permite DB en memoria y arranque deterministico.
- Spring Test permite levantar contexto con `@RunWith(SpringJUnit4ClassRunner.class)` y `@ContextConfiguration`.
- `JdbcTestUtils.deleteFromTables` y `countRowsInTable` ayudan a preparar y verificar estado sin usar el DAO bajo test como oraculo.
- Los scripts SQL deben cargarse por classpath, no por paths absolutos fragiles.

## TP1 - Clase 4: Web Forms, Bean Validation e i18n

### Form beans

- La validacion de formularios se modela con POJOs propios de `webapp`, no con entidades de dominio directamente.
- Esos form beans tienen getters/setters y anotaciones JSR-303/Bean Validation.
- La implementacion concreta puede ser Hibernate Validator aunque no se este usando Hibernate ORM.
- La capa web valida y presenta errores; el servicio recibe datos ya parseados y decide reglas de negocio.

### Controller y binding

- `@ModelAttribute` bindea request -> objeto y tambien lo agrega al modelo para renderizar la vista.
- `@Valid` pide a Spring validar el form antes de entrar en el cuerpo del controller.
- `BindingResult` debe ir inmediatamente despues del objeto validado para capturar errores.
- Si hay errores, se redibuja el formulario con el mismo `modelAttribute` y se preservan valores.
- El controller no debe reemplazar esto por validacion manual imperativa.

### Spring form tags

- `<form:form>` conecta la vista con el form bean.
- `<form:input path="...">`, `<form:password>` y `<form:errors>` reducen repeticion y mantienen binding consistente.
- `<form:errors>` permite elegir elemento HTML y clase CSS para mensajes de error.
- `c:url` debe usarse para action URLs y assets.

### MessageSource

- `ReloadableResourceBundleMessageSource` centraliza textos visibles y mensajes de validacion.
- La resolucion de `messages.properties`, `messages_en.properties`, `messages_en_US.properties`, etc. busca lo mas especifico y cae al default si falta.
- Los mensajes de validacion pueden ser genericos por anotacion o especificos por clase/campo.
- La regla fuerte del wiki sigue siendo internacionalizar frases completas con placeholders, no concatenar fragmentos.

### `@ModelAttribute` a nivel metodo

- Un metodo del controller anotado con `@ModelAttribute` se ejecuta para todos los requests mapeados en ese controller.
- Sirve para adjuntar datos comunes, por ejemplo usuario logueado o contexto de layout.
- Usarlo con cuidado: no debe esconder queries pesadas ni reglas de negocio que correspondan a servicios.

## TP1 - Clase 5: Spring Security y logging

### Seguridad como filtro

- Spring Security se instala como filtro `springSecurityFilterChain` en `web.xml`.
- Intercepta requests antes del `DispatcherServlet`.
- La configuracion se separa en `WebAuthConfig`, no se mezcla con cualquier controller.
- `HttpSecurity` define reglas de URL, form login, remember-me, logout, manejo de excepciones y CSRF.
- El orden de `authorizeRequests()` importa: la primera regla que matchea gana; el catch-all debe ir al final.

### Autenticacion y roles

- Spring Security usa providers; en el caso simple, todos terminan delegando en `UserDetailsService`.
- `UserDetailsService` adapta usuarios propios del sistema a `UserDetails` de Spring Security.
- `PasswordEncoder` participa al comparar password hasheada.
- Plain text aparece como ejemplo basico; para el TP real usar BCrypt.
- Las authorities son un `Set`: multiples roles por usuario son normales.
- Los flags de usuario permiten modelar bloqueado, desactivado, credenciales expiradas y flujos similares.

### Login, remember-me y logout

- La clase muestra solo el GET de login; el POST lo maneja Spring Security.
- Lo mismo para logout: no crear controllers manuales para reemplazar el mecanismo de Spring Security salvo razon explicita.
- Remember-me depende de una key criptografica. Si la key cambia, se invalidan cookies existentes.
- Para el repo real, la key debe venir de config externa o propiedad segura, nunca hardcodeada.

### Logging inicial

- Logback es la implementacion recomendada sobre SLF4J.
- El primer setup usa `ConsoleAppender` y root `DEBUG`, util para desarrollo pero no para produccion.
- En produccion no conviene loguear en STDOUT ni con nivel demasiado bajo.
- Los loggers deben ser SLF4J (`Logger`, `LoggerFactory`) para no acoplarse a implementacion concreta.

## TP1 - Clase 6: Logging productivo y AOP

### Configuracion dual de Logback

- `logback-test.xml` se carga antes que `logback.xml`.
- La clase aprovecha eso para tener configuracion de desarrollo y excluirla del WAR con Maven.
- `logback.xml` productivo escribe a archivos con `RollingFileAppender`, rotacion temporal e historial acotado.
- El root logger deberia estar en nivel restrictivo para produccion.
- Un logger de paquete propio puede capturar mas detalle, con `additivity="false"` para evitar duplicados.
- Los mensajes deben usar placeholders `{}`; no concatenar strings si el log podria estar deshabilitado.

### Por que AOP

- Transacciones, logs de auditoria, locks, metricas y caching son concerns transversales.
- Repetir begin/commit/rollback manualmente en cada DAO o servicio es fragil.
- Spring AOP aplica comportamiento en runtime a clases/metodos anotados.

### Proxies y restricciones

- El modo default de Spring usa proxies que decoran el bean real.
- El proxy solo intercepta llamadas publicas expuestas por interfaz.
- Si un metodo llama a otro metodo del mismo bean con `this.otroMetodo()`, esa llamada no pasa por el proxy.
- Anotar metodos privados o no expuestos no produce el efecto esperado y puede fallar en silencio.

### `@Transactional`

- `spring-tx` habilita transacciones declarativas.
- `@EnableTransactionManagement` y un `PlatformTransactionManager` conectan Spring con el mecanismo de transaccion.
- La cursada muestra transacciones en servicios porque el caso de uso puede incluir varias operaciones de DB y otros efectos de negocio.
- `@Rollback` en tests aprovecha el mismo modelo para aislar cambios.
- `@Scheduled` se menciona como otro aspecto comun para tareas recurrentes.

## TP2 - Clase 7: Introduccion a Hibernate y JPA

### Cambio de paradigma

- TP2 migra persistencia desde JDBC directo hacia JPA con Hibernate como implementacion.
- JPA es el estandar; Hibernate es el proveedor.
- El problema base es la impedancia objeto-relacional: objetos y tablas no mapean de forma perfecta.
- El ORM acelera desarrollo y reduce SQL repetitivo, pero exige entender sus convenciones, queries generadas y trade-offs.

### Configuracion

- Aparece `spring-orm`, dependencias de Hibernate/JPA y `LocalContainerEntityManagerFactoryBean`.
- Se configura un `JpaVendorAdapter`, dialecto y propiedades de Hibernate.
- El `PlatformTransactionManager` pasa de JDBC a `JpaTransactionManager`.
- `databasePopulator` puede dejar de ser el dueño del esquema si Hibernate lo administra; esto es una decision de etapa, no una regla universal para TP1.
- Las properties que imprimen SQL son utiles para depurar, no para produccion.

### DAOs JPA

- El DAO usa `@PersistenceContext` para inyectar `EntityManager`.
- `em.persist()` crea entidades.
- `em.find()` busca por primary key.
- Queries se escriben en JPQL/HQL: entidades y propiedades, no tablas y columnas.
- Si conviven DAO JDBC y DAO Hibernate con el mismo contrato, hay que evitar que Spring encuentre dos beans candidatos.

### Entidades

- Las clases de dominio se anotan con `@Entity`.
- `@Table` y `@Column` ajustan nombres, nullabilidad, largo y constraints.
- `@Id`, `@GeneratedValue` y `@SequenceGenerator` conectan ids con secuencias existentes.
- Hibernate necesita constructor default para instanciar por reflection.
- El mapping debe respetar el schema real si ya existe una base previa.

## TP2 - Clase 8: Relaciones JPA, fetch y contexto de persistencia

### Relaciones

- `@ManyToOne` modela referencias a otra entidad.
- `optional = false` o `true` expresa si la relacion es requerida.
- `@OneToMany(mappedBy = "...")` modela el lado inverso.
- `orphanRemoval` cambia el ciclo de vida de hijos que dejan de estar asociados.
- `@Enumerated(EnumType.STRING)` favorece legibilidad, pero obliga a cuidar ancho de columna ante nuevos valores.

### Fetch y performance

- `FetchType.EAGER` puede disparar cargas masivas invisibles.
- `FetchType.LAZY` difiere carga, pero requiere contexto de persistencia abierto al acceder.
- La clase insiste en mirar los logs SQL: JPA simplifica codigo, pero no elimina la responsabilidad de auditar queries.
- En local puede parecer correcto y en produccion con miles de filas volverse un problema de performance.

### Dirty checking y contexto managed

- Dentro de una transaccion, las entidades recuperadas por el `EntityManager` quedan managed.
- Cambiar una entidad managed puede persistirse al salir de la transaccion aunque no se llame explicitamente a un DAO de update.
- Eso reduce codigo, pero acopla la logica al modelo JPA/Hibernate.
- Si una entidad se pasa por argumento fuera del contexto, lazy loading puede fallar.
- Exponer metodos de reasociacion del `EntityManager` a servicios ensucia capas y filtra detalles de persistencia.

### Riesgo de operaciones generadas

- Cambios simples en colecciones pueden traducirse en deletes/inserts de relaciones si el mapping usa orden fisico.
- La migracion TP2 debe revisar SQL generado para casos reales, no solo compilar.

## TP final - Clases 9 y 10: Jersey, REST y API

### Por que API

- Una SPA obliga a tener API.
- La API habilita terceros, mobile y clientes alternativos.
- Puede mejorar tiempos de respuesta al transferir menos HTML repetido, pero empeora la descarga inicial del cliente.
- REST debe pensarse como recursos HTTP, no como controllers MVC que devuelven JSON por costumbre.

### REST y stateless

- Los paths deben modelar recursos.
- Los status codes comunican resultado: created, no content, not found, forbidden, etc.
- La paginacion y los links forman parte del contrato API.
- Content types propios y documentados ayudan a versionar representaciones sin destruir identidad de recursos.
- Si la API es RESTful, no puede depender de sesion/cookies server-side para autenticacion.
- La clase menciona JWT u otros tokens por header como camino razonable para este TP; HTTPS seria deseable aunque el servidor de catedra no lo exija para evaluacion.

### Jersey/JAX-RS

- Jersey implementa JAX-RS.
- En el material, Jersey reemplaza a Spring MVC para controllers REST, mientras Spring sigue manejando seguridad, persistencia y servicios.
- `ServletContainer` se registra como filtro.
- El mapping de Jersey debe quedar detras de OpenEntityManagerInView y Spring Security para que esos filtros apliquen primero.
- Los recursos usan `@Path`, `@GET`, `@POST`, `@DELETE`, `@Produces`, `@PathParam`, `Response` y DTOs.
- `201 Created` debe incluir `Location` cuando se crea un recurso.
- `204 No Content` aplica para deletes exitosos sin body.

## TP final - Clase teorica Front end

### SPA como thick client

- Una SPA maneja input del usuario dinamicamente y busca minimizar round-trips al backend.
- El cliente pasa de thin client a thick client.
- La ventaja principal es dinamismo y separacion de concerns.
- La desventaja es duplicar parte de la logica: validacion cliente, manejo de estado, errores y performance.

### Ecosistema JS

- Modules permiten codigo reusable e independiente.
- La clase recorre CommonJS/RequireJS/AMD y ES Modules como historia de carga modular.
- Runtime JS: Node, Deno y Bun aparecen como entornos para ejecutar tooling y desarrollo.
- Package managers y registries gestionan dependencias: npm, yarn, pnpm, deno, bun, JSR, GitHub, AWS, etc.
- Bundlers combinan y procesan JS/CSS/HTML para el browser.
- Webpack fue historico; Vite/esbuild representan tooling moderno.

### Frameworks y routing

- React y Angular representan enfoques con distinto nivel de opinion.
- En React aparecen router, Vite/Next.js, Tailwind/Shadcn, Remix/Zustand, Jest/Vitest como piezas posibles.
- La URL sigue siendo relevante por bookmarks, compartir links, refresh y estados 404/403.
- Aunque el routeo sea interno, el browser debe poder refrescar y caer en un estado coherente.

## TP final - SPAs segunda parte

### Componentes y formularios

- Los componentes son la unidad de negocio mas chica del frontend.
- Deben tender a ser puros para ser predecibles y testeables.
- Component libraries pueden ser unstyled (Radix, Headless UI) o styled (Shadcn, Material UI).
- Los formularios deben validar en cliente y mostrar errores en tiempo real cuando aporta UX.
- Librerias como react-hook-form y zod/yup aparecen como opciones para validacion.

### Estado del cliente

- La capa de estado funciona como "backend del front": contiene conexiones asincronicas, logica de negocio del cliente y estado.
- El objetivo es mantener single source of truth y flujo unidireccional.
- Stores separan dato y logica de actualizacion.
- `useState`, Redux y Zustand son variantes segun complejidad.
- En Angular, RxJS/Observables y `BehaviorSubject` sirven para compartir datos asincronicos y estado actual.

### Auth, errores e interceptors

- Authorization header y cookies tienen trade-offs.
- LocalStorage es vulnerable ante XSS.
- Cookies pueden enviarse automaticamente con demasiados requests, sumando overhead y riesgo.
- El frontend debe manejar errores 404/403 y estados de sesion.
- Interceptors permiten agregar comportamiento global a requests HTTP.

### I18n, testing y optimizacion

- i18next se menciona para internacionalizacion.
- Herramientas de scanner pueden extraer keys.
- Hay que testear componentes puros y capa de estado.
- Herramientas mencionadas: Jest/Vitest, Nock/Sinon.
- Minificacion JS/CSS elimina comentarios, espacios y nombres largos para reducir payload.
- Lighthouse mide performance, accesibilidad, SEO y buenas practicas.

### Hosting, SSR y cache

- El entregable final debe poder salir con `mvn package`, incluyendo el frontend.
- `frontend-maven-plugin` actua como puente entre Maven y el mundo JS.
- `webapp` debe servir recursos estaticos nuevos.
- Si hay modulo frontend, debe ejecutarse antes del empaquetado final.
- SSR mejora first impression, pero limita acceso a APIs de browser como `window` y `localStorage` durante pre-render.
- Cache condicional siempre revalida; cache no condicional evita request pero puede dejar assets stale si no hay cache busting.
- File revving cambia nombres de assets via hash/version para que el cache no condicional sea seguro.
- En Spring MVC se puede configurar cache de recursos con resource handlers o filtros, pero el root `/` no debe tratarse como asset inmutable.

## Reglas operativas derivadas

### Si estas en TP1

- No migrar a JPA/Hibernate ni SPA salvo pedido explicito.
- Mantener Spring MVC + JSP/JSTL + JDBC.
- Cuidar scopes Maven para que implementaciones no se filtren a compile.
- Validar formularios con Bean Validation y Spring form tags.
- Mantener seguridad en Spring Security y no en controllers.
- Testear persistencia con HSQLDB y servicios con mocks/fakes orientados a estado.

### Si estas migrando a TP2

- La migracion central es de persistencia: entidades JPA, DAOs con `EntityManager`, transaction manager JPA y mappings.
- No asumir que "compila" significa "performante": revisar SQL generado, fetches y cascadas.
- Mantener servicios como borde transaccional y evitar filtrar `EntityManager` hacia webapp o service contracts.
- Revisar schema existente antes de habilitar autogeneracion o updates automáticos.
- Las versiones del PDF no son canon; elegir dependencias desde el checkout/enunciado actual.

### Si estas migrando a TP final

- Separar API REST de vistas JSP: recursos, DTOs, status codes, `Location`, content negotiation y auth stateless.
- Definir contrato de frontend: routing, estado, formularios, errores, i18n, testing y build.
- Integrar build frontend al empaquetado sin romper `mvn package`.
- Diseñar cache/file revving para assets y no cachear como inmutable la entrada HTML/root.
- Evaluar seguridad de tokens/cookies y XSS antes de decidir storage del auth state.

## Ver tambien

- [[tp1-vs-tpe2-final]]
- [[configuracion-maven]]
- [[modelo-capas]]
- [[spring-web-mvc]]
- [[jsp-jstl]]
- [[persistencia-jdbc]]
- [[testing-unitario]]
- [[validacion-formularios]]
- [[spring-security]]
- [[logging]]
- [[spring-aop]]
- [[transactional]]
- [[hibernate-jpa]]
- [[api-rest]]
- [[single-page-applications]]
- [[resumen-apuntes]]
- [[resumen-notas]]
