---
titulo: Resumen de Apuntes de la Materia
tipo: fuente
fuentes: [raw/apuntes.txt, raw/pdfs/PAW - Apuntes.pdf]
creado: 2026-04-09
actualizado: 2026-05-11
---

# Resumen de Apuntes de la Materia

Apuntes completos de PAW (72.38), 1er cuatrimestre 2025. El documento tiene 11 unidades. Para TPE1/TP1, las unidades 1-7 concentran la base metodologica, web, MVC, testing, formularios, i18n, seguridad y logging. La unidad 8 suma el soporte practico para entender proxies y `@Transactional`. Las unidades 9-11 cubren el salto posterior hacia TP2 y TP final: Hibernate/JPA, Single Page Applications y API REST.

Nota de fuente: el `.txt` ingerido fue validado contra el PDF original; no aparecieron diferencias sustantivas tras normalizar la extraccion. El 2026-05-11 se reextrajeron directamente del PDF las paginas 68-96 para ampliar con mucho mas detalle las unidades 8 en adelante.

## Posicion editorial

- Esta es la **fuente teorica principal** del wiki para la materia.
- Para **TP1** conviene priorizar unidades 1-7 y sumar la unidad 8 como soporte de implementacion cuando aparezcan dudas sobre proxies o `@Transactional`.
- Para **TP2**, la unidad 9 es la base conceptual de la migracion JDBC -> JPA/Hibernate.
- Para **TP final**, las unidades 10 y 11 explican por que el producto cambia hacia SPA + API REST y que reglas de diseno aparecen con ese cambio.
- Para aterrizar implementacion, ejemplos y advertencias del profesor, conviene complementarla con [[resumen-notas]] y [[resumen-clases-paw-2026]].
- Para corroboracion auxiliar o clase reciente sobre agentes/formularios, usar [[resumen-transcripciones-clases-2-a-4]] con mas cautela.
- La ruta concreta de estudio esta resumida en [[tp1-vs-tpe2-final]].

## Cobertura por unidad y etapa

| Unidad | Tema | Paginas wiki relacionadas | Nota TP1 |
|--------|------|--------------------------|----------|
| 1 | Introduccion: Scrum, stories, workflow, AirBnB, MVP | [[scrum-metodologia]], [[ux-flows]] | Base metodologica del trabajo |
| 2 | Contexto historico: HTTP, CGI, sesiones, contenedores Java, mailing, imagenes | [[http-y-sesiones]], [[mailing]], [[manejo-imagenes]] | Contexto tecnico del stack server-side |
| 3 | Frontend: usabilidad, CSS, UI kits, JSP, JSTL | [[jsp-jstl]], [[ux-flows]] | UI server-side y vistas del TP1 |
| 4 | Spring: Maven, MVC server-side, modelo de capas, DI, controllers, JDBC | [[spring-web-mvc]], [[modelo-capas]], [[inyeccion-dependencias]], [[configuracion-maven]], [[persistencia-jdbc]] | Wiring real de capas y persistencia |
| 5 | Unit Testing: JUnit, HSQLDB, Mockito | [[testing-unitario]] | Cobertura y harness de tests |
| 6 | Formularios: Bean Validation, form beans, i18n | [[validacion-formularios]], [[internacionalizacion]], [[ux-flows]] | Binding, validacion y mensajes |
| 7 | Gestion de permisos: Spring Security, ACL, logging | [[spring-security]], [[auth-flows]], [[logging]] | Seguridad y observabilidad obligatorias |
| 8 | AOP (Aspect Oriented Programming) | [[spring-aop]], [[transactional]] | Si entra para el TP1; tenerlo en cuenta durante la implementacion por proxies y `@Transactional`. |
| 9 | Hibernate / JPA / ORM | [[hibernate-jpa]], [[transactional]], [[persistencia-jdbc]] | No es TP1; base de TP2 y migracion desde JDBC. |
| 10 | Single Page Applications | [[single-page-applications]], [[internacionalizacion]], [[ux-flows]] | No es TP1; base del frontend del TP final. |
| 11 | API REST | [[api-rest]], [[http-y-sesiones]], [[manejo-imagenes]] | No es TP1; contrato HTTP para SPA/API final. |

## Paginas por unidad

| Unidad | Pagina dedicada | Etapa principal |
|--------|-----------------|-----------------|
| 1 | [[paw-unidad-01-introduccion-scrum-mvp]] | Base metodologica |
| 2 | [[paw-unidad-02-contexto-historico-web]] | Base web server-side |
| 3 | [[paw-unidad-03-frontend-jsp-jstl]] | TP1 |
| 4 | [[paw-unidad-04-spring-mvc-capas-jdbc]] | TP1 |
| 5 | [[paw-unidad-05-unit-testing]] | TP1 |
| 6 | [[paw-unidad-06-formularios-i18n]] | TP1 |
| 7 | [[paw-unidad-07-seguridad-logging]] | TP1 |
| 8 | [[paw-unidad-08-aop-transacciones]] | Soporte TP1 y puente TP2 |
| 9 | [[paw-unidad-09-hibernate-jpa]] | TP2 |
| 10 | [[paw-unidad-10-single-page-applications]] | TP final |
| 11 | [[paw-unidad-11-api-rest]] | TP final |

## Hallazgos clave por unidad

### Unidad 1 — Proceso Scrum

**Base metodologica**
- Una story representa una tarea concreta con requester y owner.
- Todas las tareas deberian agregar valor al usuario final.
- El `icelog` contiene stories sin priorizar; el `backlog`, las priorizadas.
- Prioridades usadas en clase: `low`, `medium`, `high`, `urgent`.

**Tipos de story**
- `Feature`: funcionalidad con valor directo; en general son las que se estiman.
- `Chore`: tarea tecnica necesaria, pero sin valor directo evidente para el usuario.
- `Bug`: comportamiento no esperado del sistema.

**Workflow sugerido**
1. Definir requerimientos y escribir stories.
2. Priorizar en backlog.
3. Discutir y estimar.
4. Tomar la siguiente story disponible.
5. Hacer check-in al terminar implementacion.
6. Deployar a staging/demo/test.
7. Aceptar o rechazar la story y cerrar feedback loop.

**Caso AirBnB y MVP**
- El ejemplo de AirBnB se usa para mostrar que producto y landing evolucionan alrededor del problema real a resolver.
- La idea no es "hacer una pagina linda", sino comunicar claramente problema, solucion y CTA.
- El MVP surge de identificar el problema, brainstormear features/stories y destilar la solucion a lo minimo viable cuando hay pocos recursos y mercado incierto.

### Unidad 2 — HTTP, sesiones y contexto historico

**HTTP y web dinamica**
- HTTP es request-response, iniciado por cliente y stateless.
- La web original servia archivos estaticos; luego Web 2.0 obliga a generar contenido dinamico.
- CGI/fastCGI aparecen para mapear requests a programas que generan respuesta sin lidiar directamente con sockets.

**Sesiones**
- La sesion HTTP es una convencion construida arriba de un protocolo stateless.
- El servidor genera un `session id`, guarda estado auxiliar y el browser reenvia la cookie en cada request.
- Esto escala mal si hay multiples servidores sin coordinacion; de ahi la necesidad de balanceadores y estrategias de sesion.

**Java del lado servidor**
- La unidad explica el camino hacia application containers, servlets y Front Controller.
- Este contexto justifica por que en el TP usamos Spring MVC y Spring Security en lugar de handlers manuales.

**Mailing e imagenes**
- **Mailing**: Thymeleaf para templates + javax.mail; formato esperado: header + body + CTA + footer; envio desde servicios y de forma asincrona con `@Async`.
- **Imagenes**: separar entidad e imagen en tablas distintas; contenido binario almacenado como `bytea`.

### Unidad 3 — Frontend, CSS, JSP y JSTL

**Usabilidad antes que "belleza"**
- La materia prioriza interfaces usables por sobre interfaces visualmente llamativas.
- CSS sigue siendo la herramienta base de estilo; hay que entender grid/flex, cascada y especificidad.
- Regla de clase: evitar estilo inline salvo excepciones muy puntuales.

**UI Kit vs Framework**
- Muchos frameworks JS orientados a SPA no aplican a la primera entrega.
- Si sirven los componentes HTML/CSS reutilizables de un UI kit: botones, modales, text-boxes, etc.

**JSP**
- Los objetos expuestos a vista deben cumplir forma de Bean: propiedades privadas, getters/setters, constructor publico sin argumentos e idealmente `Serializable`.
- Las JSP se traducen a servlets.
- Directivas vistas en apuntes: `page`, `include` estatico y action include con parametros.

**JSTL y tags**
- Familias principales: `core`, `i18n`, `functions`.
- `c:out` evita imprimir HTML malicioso directo.
- `c:url` genera URLs relativas correctas al deploy.
- `c:if` y `c:forEach` resuelven control de flujo en vista.
- Spring form tags facilitan binding directo con form beans.

### Unidad 4 — Arquitectura Spring

**Maven**
- Maven se presenta como gestor del ciclo de vida completo, no solo de dependencias.
- Se trabaja con proyecto multi-modulo generado por `mvn archetype:generate`.
- `groupId` representa la organizacion/paquetes; `artifactId`, el artefacto.
- `SNAPSHOT` indica version mutable de trabajo; un release ya no deberia editarse.
- El pom padre centraliza versiones y `dependencyManagement`.

**MVC server-side**
- `web.xml` registra `DispatcherServlet`, `load-on-startup` y configuracion basada en codigo.
- `WebConfig` centraliza beans y `@ComponentScan`.
- Los controllers se mapean con `@RequestMapping` y devuelven `ModelAndView`.
- Los JSP deben ir bajo `WEB-INF/jsp/` para no quedar expuestos por URL directa.
- `ViewResolver` evita repetir paths fisicos largos.

**DI y capas**
- El motor de DI separa configuracion y uso de dependencias.
- `@ComponentScan`, `@Controller`, `@Service`, `@Repository` y `@Autowired` hacen que Spring resuelva wiring.
- El modelo de capas funciona como cebolla: cada capa solo habla con la inmediatamente inferior.

**Persistencia**
- `JdbcTemplate`, `RowMapper` y `SimpleJdbcInsert` son las primitivas JDBC destacadas en esta unidad.

### Unidad 5 — Testing

**Estructura de test**
- Setup, ejercitacion y validaciones.
- La ejercitacion idealmente es una sola llamada.
- Las postcondiciones deberian ser pocas; la clase habla de un maximo orientativo de 4 asserts.

**Dobles de prueba**
- `stub`: implementacion vacia.
- `mock`: implementacion programable.
- `fake`: implementacion limitada pero funcional.

**Persistencia**
- HSQLDB se usa por ser embebida y reproducible.
- El ejemplo didactico crea `schema.sql` en `src/test/resources`, pero la idea evoluciona a compartir esquema con la app.
- Spring Test con `@RunWith(SpringJUnit4ClassRunner.class)` y `@ContextConfiguration`.
- `JdbcTestUtils.countRowsInTable` y `deleteFromTables` ayudan a independencia y verificacion contra BD real.

**Mockito**
- `mock()` da un "nice mock": `null`, `0` o `false` segun el tipo si no se programa respuesta.
- `@Mock`, `@InjectMocks` y el runner de Mockito simplifican setup.
- Regla fuerte: no usar `verify()` para validar implementacion; interesa comportamiento observable.
- Hay casos donde solo se quiere asegurar que el metodo no falle; en clase se mostro un `try/catch` para eso.

### Unidad 6 — Formularios e i18n

**Formularios**
- Los formularios son un area sensible de UX: hay que dar feedback claro y no perder valores cargados.
- El ejemplo de clase amplifica registro con `repeat password`.
- Se crean form beans simples (`UserForm`) dentro de webapp.
- `@Valid` + `BindingResult` eliminan validacion manual repetitiva.
- `@ModelAttribute` facilita binding entre request, bean y JSP.
- `<form:errors/>` permite customizar elemento HTML, clase CSS y estilo para errores.

**Internacionalizacion**
- `MessageSource` extrae mensajes visibles a archivos `messages*.properties`.
- Jerarquia vista en clase: `messages.properties`, `messages_en.properties`, `messages_en_US.properties`.
- La resolucion busca el archivo mas especifico posible y cae al default si falta.
- Tambien se internacionalizan mensajes de validacion y templates con placeholders.
- Regla critica: internacionalizar el mensaje completo, no concatenar fragmentos.

**@ModelAttribute a nivel metodo**
- Un metodo anotado con `@ModelAttribute` puede agregar datos comunes a todos los requests del controller, por ejemplo usuario logueado.

### Unidad 7 — Seguridad y Logging

**Spring Security**
- Spring Security implementa ACL sobre roles, recursos y permisos.
- La cursada distingue **roles** de **perfiles de usuario**: no conviene asumir mapeo 1:1.
- `springSecurityFilterChain` se registra en `web.xml` como filtro.
- Providers autentican y terminan delegando en `UserDetailsService` y `PasswordEncoder`.
- `PawAuthUser` extiende `User` de Spring Security para agregar datos propios y estados de cuenta.
- `BCryptPasswordEncoder` es la opcion correcta para el TP; `PlainTextPasswordEncoder` aparece solo como ejemplo basico.
- Las authorities viven en un `Set`, por lo que multiples roles por usuario son normales.

**Logging**
- Logback aparece como framework recomendado.
- La configuracion se apoya en arbol jerarquico de paquetes, loggers, appenders y root logger.
- En desarrollo sirve `DEBUG` por consola; en produccion no conviene ni `DEBUG` ni STDOUT.
- Se usan dos configuraciones: `logback-test.xml` para desarrollo y `logback.xml` para produccion.
- Con SLF4J hay que usar placeholders `{}` y pasar excepciones como ultimo argumento `Throwable`.

## Reingesta detallada de unidades 8 a 11

Estas unidades son el puente entre la entrega server-side clasica y las etapas posteriores. No conviene leerlas como una lista de tecnologias sueltas: la secuencia es **AOP/transacciones -> ORM/JPA -> cliente SPA -> API REST stateless**. Cada paso mueve una responsabilidad distinta del sistema.

### Unidad 8 — AOP, proxies y transacciones

**Problema que resuelve**

- AOP aparece para concerns transversales que atraviesan muchas clases y no encajan bien en herencia ni en una entidad de dominio.
- Ejemplos de concern transversal: logging, seguridad/autenticacion, manejo de errores, transacciones, metricas y performance.
- Sin AOP, el equipo depende de convenciones manuales: recordar abrir/cerrar transacciones, medir tiempos, loguear o manejar errores en todos los puntos correctos.
- Esa repeticion es fragil porque nada obliga a que todos los DAOs o servicios apliquen la misma politica.
- El ejemplo conceptual de "hacer una clase abstracta de DAOs" no escala: puede haber DAOs de librerias, clases que no comparten una herencia real o concerns que no representan una abstraccion de negocio.

**Modelo mental correcto**

- AOP separa el codigo principal de negocio del codigo que debe ejecutarse alrededor de muchos metodos.
- La unidad usa las ideas de `Before`, `After` y `Around`: ejecutar logica antes, despues o rodeando una invocacion.
- En Spring, esto se implementa de forma practica con anotaciones y proxies.
- El proxy envuelve al bean real y expone la misma interfaz; para quien consume el servicio/DAO, sigue pareciendo el mismo contrato.
- Cuando la llamada entra al proxy, Spring puede abrir transaccion, ejecutar advice, delegar al metodo real y despues hacer commit/rollback o ejecutar logica de cierre.

**AOP en Spring**

- Spring no exige que el codigo de negocio llame manualmente a un objeto transaccional.
- El contenedor crea una instancia proxy en paralelo al bean real.
- Mientras el proxy implemente la interfaz esperada, el resto de la aplicacion puede recibirlo sin saber que hay una capa alrededor.
- La transparencia es util, pero tambien peligrosa: si no se entiende por donde pasa la llamada, se puede anotar codigo que nunca sera interceptado.

**Donde aplicar aspectos**

- Aplicar aspectos en servicios suele ser la opcion mas coherente para reglas de negocio porque el servicio representa el caso de uso completo.
- Una transaccion en servicio puede cubrir varias operaciones de persistencia y, segun el diseno, efectos asociados como envio de mail.
- Aplicar aspectos en persistencia tambien puede ser valido, pero hay que sostener el mismo criterio en todo el proyecto.
- Mezclar bordes transaccionales sin criterio vuelve dificil razonar que se commitea y que se revierte.

**Restricciones criticas del proxy**

- Solo se interceptan llamadas que pasan por el proxy.
- Con proxies basados en interfaz, solo aplican correctamente los metodos disponibles en la interfaz expuesta.
- Las invocaciones internas como `this.otroMetodo()` no pasan por el proxy: son llamadas directas al objeto real.
- Por eso un metodo anotado puede no tener efecto si se invoca desde dentro de la misma clase.
- Los metodos privados no sirven como borde AOP: el proxy no puede interceptarlos como punto de entrada externo.
- Esto explica por que no alcanza con "tirar `@Transactional` en cualquier metodo"; importa el punto real por donde entra el request desde controller -> service.

**Implicancia directa para TP1**

- Los metodos publicos de servicios deben ser el borde natural de `@Transactional`.
- Los metodos de lectura deberian usar `@Transactional(readOnly = true)` cuando corresponda.
- Los metodos que escriben deberian quedar transaccionales en el caso de uso, no escondidos en helpers privados.
- Si un controller llama a un metodo publico anotado, el proxy puede actuar.
- Si un servicio llama internamente a otro helper anotado con `this.helper()`, no hay garantia de advice.
- Anotar toda la clase puede ser aceptable cuando todos los metodos publicos comparten una politica de transaccion; aun asi, hay que revisar excepciones y read-only.

**Checklist de lectura practica**

- Verificar que `@EnableTransactionManagement` este activo en la configuracion Spring.
- Verificar que exista un `PlatformTransactionManager` compatible con la tecnologia de persistencia usada.
- Ubicar las llamadas reales desde controllers hacia services.
- Confirmar que los metodos publicos usados como casos de uso estan cubiertos.
- Evitar confiar en anotaciones sobre metodos privados o invocaciones internas.
- Recordar que stack traces mas largos son esperables por proxies y capas generadas.

### Unidad 9 — Hibernate, JPA y ORM

**Cambio de paradigma**

- La unidad 9 abandona JDBC directo y presenta ORM como tecnica para trabajar con tablas relacionales desde objetos del lenguaje.
- En JDBC, el programador escribe SQL, mapea filas a objetos y decide manualmente cuando ejecutar cada query.
- Con JPA/Hibernate, el programador trabaja con entidades y un contexto de persistencia; el proveedor traduce operaciones sobre objetos a SQL.
- La ganancia principal es simplicidad y velocidad de desarrollo.
- El costo principal es perder visibilidad inmediata sobre queries, orden de ejecucion y performance fina.
- Por eso el apunte insiste en conocer las convenciones del ORM elegido: la abstraccion no elimina el modelo relacional.

**ORM, JPA e Hibernate**

- ORM significa Object Relational Mapping: puente entre objetos y base relacional.
- JPA es la especificacion estandar: define interfaces, anotaciones y reglas esperadas.
- Hibernate es un proveedor/implementacion de JPA.
- JPA no persiste por si misma; necesita un proveedor como Hibernate, EclipseLink u otro.
- El punto no es dejar de saber SQL, sino reducir boilerplate sin perder criterio sobre lo que se genera.

**Impedancia objeto-relacional**

- Objetos y tablas no modelan igual.
- En objetos son naturales herencia, colecciones, referencias y `null`.
- En SQL, las relaciones se expresan con claves, joins, constraints y `NULL` con semantica propia.
- SQL `NULL` no equivale exactamente al `null` de Java: en comparaciones puede significar desconocido y alterar filtros.
- Hibernate reduce la friccion, pero introduce sus propias convenciones para resolver diferencias.

**Configuracion Spring/JPA**

- Hay que declarar dependencias en el pom padre y en los modulos que correspondan: persistence, models y webapp segun el layout.
- Spring ORM conecta Spring con el proveedor ORM.
- La configuracion crea un `EntityManagerFactory` con `DataSource`, paquetes de entidades, vendor adapter y properties del proveedor.
- El transaction manager deja de ser puramente JDBC y pasa a adaptarse a JPA.
- La property de schema debe tratarse con extremo cuidado: `create` destruye y recrea esquema; para una base existente es peligroso.
- `update` hace un best effort, pero no debe verse como migrador confiable.
- Mostrar SQL por stdout sirve para desarrollo, auditoria y aprendizaje; no debe quedar activo en produccion.
- Cualquier version o property concreta del PDF debe tratarse como historica y contrastarse con el checkout vigente.

**EntityManager y DAOs**

- `@PersistenceContext` inyecta un `EntityManager` gestionado por el contenedor.
- `EntityManager` concentra persistir, buscar, eliminar y consultar entidades.
- Los DAOs siguen siendo `@Repository`: la responsabilidad de acceso a datos no desaparece.
- La diferencia es que el DAO ya no arma manualmente cada `PreparedStatement` o `RowMapper`.
- `persist` y operaciones relacionadas deben usarse respetando el ciclo de vida de la entidad; no conviene construir flows que dependan de asignar IDs manualmente.
- Las queries de JPA/JPQL hablan en terminos de atributos del modelo, no de nombres de columnas.
- Ejemplo conceptual: `u.username` refiere al atributo `username` de la entidad, no necesariamente a una columna llamada igual.

**Mapeo basico de entidades**

- `@Entity` marca una clase persistible.
- `@Table` permite fijar la tabla fisica cuando el default no coincide con el schema real.
- `@Id` marca la clave primaria.
- `@GeneratedValue` define estrategia de generacion.
- `@SequenceGenerator` permite reutilizar secuencias existentes, por ejemplo en PostgreSQL.
- `@Column` explicita nombre, constraints y largo cuando el default no alcanza.
- Hibernate necesita constructor default para instanciar por reflection.
- No todo cambio de anotacion modifica la tabla existente: agrandar un `length`, borrar columnas o ajustar constraints requiere migracion real.

**Enums y legibilidad**

- `@Enumerated` define como persistir enums.
- `EnumType.STRING` suele ser mas legible porque guarda nombres.
- La contra es que hay que cuidar el ancho de columna y la estabilidad de los nombres.
- `EnumType.ORDINAL` ahorra espacio, pero queda atado al orden del enum y se rompe facilmente si se reordena.

**Relaciones**

- Las relaciones principales son `@ManyToOne`, `@OneToMany`, `@OneToOne` y `@ManyToMany`.
- La relacion debe pensarse desde el lado donde esta parada la entidad.
- En un issue, el autor o asignado son `ManyToOne` hacia usuario porque muchos issues pueden apuntar al mismo usuario.
- `optional = false` expresa relacion obligatoria; `optional = true` permite ausencia.
- `FetchType.EAGER` carga la relacion inmediatamente; solo deberia usarse cuando se sabe que siempre se necesita y que no arrastra media base.
- `FetchType.LAZY` difiere carga hasta acceder a la propiedad; es mas eficiente, pero exige contexto de persistencia abierto.
- `mappedBy` indica el lado inverso en una relacion bidireccional.
- El lado duenio es el que controla la clave foranea; el lado con `mappedBy` refleja lo que controla el otro lado.
- `orphanRemoval` borra hijos que dejan de pertenecer al padre.

**Contexto de persistencia y dirty checking**

- El contexto de persistencia no es solo una conexion: es un espacio gestionado donde viven entidades monitoreadas por Hibernate.
- Si una entidad fue obtenida dentro de una transaccion activa, queda en estado managed.
- Una entidad managed puede modificarse en memoria y Hibernate detecta cambios al hacer `flush()` o al cerrar la transaccion.
- Esto se llama dirty checking.
- La comodidad es grande: se reduce codigo explicito de update.
- El riesgo tambien: pueden ejecutarse queries sin que el programador haya llamado manualmente a `save`.
- Si el objeto llega como parametro y no pertenece al contexto actual, no hay garantia de que Hibernate lo sincronice.
- Acceder a una relacion lazy fuera de sesion puede fallar.
- Reasociar entidades con operaciones como `merge` existe, pero exponer ese detalle al servicio ensucia la frontera de capas.

**Persistencia implicita vs explicita**

- El apunte muestra un caso donde ordenar una coleccion dentro de una transaccion termina persistiendo cambios aunque no haya un `update` visible.
- Esto explica por que JPA puede ser sorprendente en auditorias: el cambio no siempre aparece como llamada directa a DAO.
- Una alternativa es hacer explicita la persistencia con un metodo de DAO, pero el equipo debe ser consistente.
- La decision no es puramente tecnica: afecta legibilidad, testeo y control del ciclo de vida de entidades.

**Paginacion en JPA**

- En entidades con joins, `LIMIT` sobre filas SQL no siempre equivale a "cantidad de entidades".
- Una pagina de 10 entidades puede expandirse a mas filas si se cargan relaciones.
- La estrategia recomendada es de dos pasos:
  1. obtener IDs de la pagina con una query segura para paginacion;
  2. cargar esas entidades y relaciones con JPA.
- Esta tecnica evita resultados truncados o paginas inconsistentes.
- La misma idea puede aplicar tambien a JDBC cuando los joins multiplican filas.

**Sesiones y Open Session in View**

- Hibernate mantiene una sesion asociada al contexto de persistencia.
- Ese contexto nace normalmente al iniciar una transaccion y muere al terminarla.
- Multiples llamadas transaccionales separadas no comparten automaticamente el mismo objeto managed.
- Un filtro web puede mantener sesion abierta durante mas tiempo, incluso hasta la vista.
- Ese filtro debe ordenarse despues de Spring Security si se usa en la app.
- La contra es que la conexion vive mas tiempo y pueden aparecer queries durante renderizado, lejos del lugar donde el programador esperaba.

**Herencia en JPA**

- Si una clase base no debe ser entidad independiente, `@MappedSuperclass` permite heredar campos persistentes sin tabla propia.
- Si hay jerarquia de entidades, JPA permite varias estrategias.
- `SINGLE_TABLE` guarda toda la jerarquia en una tabla con columna discriminadora; suele leer rapido, pero puede dejar muchas columnas vacias.
- `JOINED` crea tabla base y tablas hijas; es mas normalizado, pero requiere joins.
- `TABLE_PER_CLASS` crea una tabla por subclase con atributos heredados repetidos; simplifica algunas independencias, pero complica consultas polimorficas.
- Ninguna estrategia es universalmente correcta: se elige segun dominio, queries esperadas y constraints.
- Si no se configuran generadores, Hibernate puede usar una secuencia global para entidades; eso evita colisiones, pero puede no respetar secuencias historicas del schema.

**Element collections**

- Si un atributo es una coleccion de valores simples, como `Set<String>`, no es una entidad.
- En ese caso corresponde un mapeo de coleccion de elementos, no una relacion uno-a-muchos normal.
- La decision importa porque define tabla auxiliar, ownership y ciclo de vida de los valores.

**Riesgos practicos para TP2**

- Creer que `update` reemplaza migraciones reales.
- Dejar `show_sql` o logs SQL verbosos en produccion.
- Usar `EAGER` por comodidad y crear cascadas de queries pesadas.
- Exponer `EntityManager` o entidades managed fuera de persistence sin criterio.
- Depender de lazy loading en una capa donde la sesion ya cerro.
- No revisar SQL generado despues de cambiar mappings.
- Migrar DAOs sin revisar candidatos duplicados de Spring para la misma interfaz.

### Unidad 10 — Single Page Applications

**Cambio de arquitectura**

- Una SPA convierte el frontend en un cliente mas pesado.
- Se pasa de thin client server-side a thick client: mas estado, mas interaccion y mas decisiones viven en el navegador.
- El backend deja de renderizar cada vista completa y empieza a exponer informacion para que el cliente decida como presentarla.
- Esto reduce round-trips para muchas interacciones, pero mueve complejidad al frontend.
- Validaciones, mensajes, errores, auth, estado de UI y llamadas asincronicas ya no quedan todos encapsulados en Spring MVC/JSP.

**Autenticacion y seguridad**

- La autenticacion cambia especialmente del lado cliente.
- En JSP/server-side, cookies y Spring Security cubren gran parte del flujo.
- En SPA, el frontend tiene que manejar login, registro, cambio de password, sesion visible, expiracion y respuestas de acceso.
- Errores como 401/403 pueden necesitar tratamiento global del cliente, no solo una pagina de error server-side.
- El apunte plantea pasar de cookies a almacenamiento/control del lado cliente para tener mas control sobre cuando enviar credenciales.
- La decision trae trade-offs: cookies se envian automaticamente y pueden sumar overhead/riesgo; local storage puede quedar expuesto frente a XSS.
- Para la entrega final, no alcanza con copiar el flujo viejo: hay que redefinir como el cliente guarda, renueva y envia credenciales.

**Internacionalizacion**

- La i18n deja de vivir solo en `MessageSource` y JSP tags.
- El frontend necesita su propia libreria de traducciones.
- La migracion no debe perder features: keys, placeholders, fallback por idioma y consistencia de textos.
- Herramientas de extraccion de keys ayudan a mantener catalogos traducibles.
- La regla editorial del wiki se mantiene: traducir mensajes completos, no fragmentos concatenados.

**Testing del frontend**

- Al crecer la logica de cliente, aparece una nueva capa que debe testearse.
- La unidad diferencia esto del unit testing backend clasico.
- No necesariamente se exige end-to-end completo, pero la capa de servicios/estado del frontend debe tener cobertura.
- Herramientas mencionadas: Jest y Vitest para testing; Nock y Sinon para mocks/stubs.
- En una SPA, el store o capa de estado funciona como backend del frontend: orquesta requests, transforma respuestas y alimenta componentes.
- Si esa capa falla, los componentes pueden verse bien pero el flujo real queda roto.

**Optimizacion**

- La SPA se carga una vez, por eso pesa mucho el tamano inicial de JS/CSS y assets.
- Minificar JS y CSS elimina espacios, nombres largos y contenido innecesario sin cambiar comportamiento.
- El codigo minificado es ilegible para humanos, pero mejora transferencia y parseo.
- Muchos frameworks lo hacen, pero la indicacion de clase es verificar, no asumir.
- Lighthouse sirve para medir performance, accesibilidad, SEO, buenas practicas y experiencia mobile.
- La optimizacion ya no es decorativa: afecta usabilidad real, especialmente en first load.

**Hosting e integracion con Maven**

- La entrega final deberia poder ejecutar `mvn package` y producir un artefacto que incluya tambien el frontend construido.
- `frontend-maven-plugin` aparece como herramienta para integrar build frontend con Maven.
- El modulo webapp debe servir los nuevos recursos estaticos generados.
- Si existe modulo frontend, tiene que ejecutarse antes del empaquetado del WAR final.
- La integracion correcta evita que el frontend quede como paso manual separado o que el deploy suba assets viejos.

**Server-side rendering**

- SSR se menciona como tecnica posible, pero no exigida.
- La idea es generar una primera impresion rapida en servidor y luego hidratar/cargar el cliente completo.
- Si hay SSR, el primer render no corre en browser.
- Por eso no se puede depender de `window`, `localStorage` u otras APIs del navegador durante ese primer render.
- Aunque no sea requisito, entender este limite ayuda a no escribir componentes acoplados al ambiente equivocado.

**Cache**

- Cache es requisito importante para la entrega final, pero no mejora el Time To First Load.
- Mejora la experiencia de usuarios recurrentes porque evita descargas repetidas.
- Cache condicional: el navegador pregunta si el recurso cambio; ahorra descarga, pero mantiene round-trip.
- Cache no condicional: el navegador usa la copia local mientras no expire; elimina incluso el ping.
- El root `/` o `index.html` no debe cachearse como asset inmutable.
- Los assets que pueden cachearse agresivamente deben usar file revving: si cambia el contenido, cambia el nombre del archivo.
- El hash/nombre versionado obliga al browser a pedir el nuevo asset cuando corresponde.
- Spring tambien debe configurarse para servir esos recursos con headers consistentes.

**Riesgos practicos para TP final**

- Migrar a SPA sin reimplementar correctamente i18n y errores.
- Dejar autenticacion a mitad de camino entre cookies server-side y tokens cliente.
- No testear la capa de estado porque "es frontend".
- Construir localmente con assets nuevos pero empaquetar un WAR con assets viejos.
- Cachear `index.html` como si fuera inmutable y dejar usuarios atrapados en bundles antiguos.

### Unidad 11 — API REST

**REST como cambio de contrato**

- REST no significa "hacer CRUD en JSON".
- Representational State Transfer implica que el servidor transmite representaciones de recursos, no pantallas.
- La SPA decide como mostrar la informacion.
- La API se apoya en HTTP: statelessness, verbos, headers, status codes y negociacion de contenido.
- El backend ya no debe depender de JSPs ni de estado de sesion para exponer el contrato principal al cliente.

**Statelessness**

- REST abraza la naturaleza stateless de HTTP.
- Si no hay estado de sesion en servidor, cada request debe traer la informacion necesaria para autenticarse/autorizarse.
- Esto favorece escalabilidad e integraciones, pero obliga a disenar bien auth, headers y errores.
- El servidor no deberia asumir que el cliente comparte una sesion estilo JSP.

**Recursos y URNs**

- Los recursos se modelan como sustantivos.
- Cada recurso debe tener una URN/path estable.
- Colecciones fuertes viven en paths como `/users`, `/posts` o `/issues`.
- Un recurso individual vive en `/users/{id}`.
- Entidades debiles pueden colgar de la fuerte: `/users/{id}/addresses`.
- El path deberia reflejar estructura de recursos, no acciones.
- Paths contextuales como `/me` rompen la idea de URN universal si dependen de quien hace el request.

**Operaciones via verbos HTTP**

- Las acciones se expresan con metodos HTTP, no con verbos inventados en el path.
- `GET /users` lista usuarios.
- `POST /users` crea un usuario.
- `PUT /users/{id}` reemplaza/actualiza representacion del usuario segun contrato.
- `DELETE /users/{id}` elimina ese recurso si la operacion aplica.
- Un path como `/users/{id}/validate` es una senal de diseno no REST porque `validate` es accion, no recurso.

**Status codes esperados**

- Para `GET`, usar `200 OK` cuando hay representacion, `204 No Content` cuando la operacion es correcta pero no hay cuerpo util, `404 Not Found` si no existe, `403 Forbidden` si se conoce al usuario pero no tiene permiso y `401` si no se sabe quien es.
- Para `POST`, una creacion correcta debe devolver `201 Created` y header `Location` apuntando al nuevo recurso.
- `405 Method Not Allowed` aplica cuando la URN existe pero ese metodo no corresponde.
- Para `DELETE`, `204 No Content` suele ser la respuesta correcta cuando borra sin cuerpo.
- La semantica importa porque clientes genericos pueden interpretar estos codigos sin conocer el dominio.

**Paginacion con headers**

- El payload deberia contener la data principal, no metadata de navegacion mezclada arbitrariamente.
- Para listas paginadas, el apunte favorece usar header `Link`.
- Relaciones estandar como `first`, `prev`, `next` y `last` describen navegacion.
- La lista de recursos queda limpia y el cliente obtiene la navegacion desde headers.
- Esto mantiene el contrato mas generico y menos acoplado a una forma de UI.

**Hipervinculos**

- Los recursos deberian incluir links a recursos relacionados.
- En vez de embeber todo el grafo de objetos, se enlazan relaciones relevantes.
- Ejemplo conceptual: un usuario puede tener `self` y `created_issues` como links.
- El cliente no necesita conocer detalles internos de claves o joins si recibe links navegables.
- Evitar embedding excesivo tambien reduce duplicacion de formas de modificar lo mismo.

**Por que no embeber todo**

- Si direcciones aparecen embebidas dentro de usuario y tambien existen como recurso propio, hay multiples maneras de manipularlas.
- Eso crea ambiguedades: borrar una direccion desde `/users/{id}/addresses` o cambiar el usuario completo podria representar el mismo efecto.
- Multiples caminos para la misma mutacion complican permisos y consistencia.
- El criterio REST prefiere una URN clara por recurso y links entre recursos.

**Negociacion de contenido**

- Versionar en el path, como `/v1/users`, se presenta como mala practica porque duplica la identidad del recurso.
- El usuario existe independientemente de que haya una representacion v1 o v2.
- El problema de versionado es de representacion, no de identidad.
- HTTP ya tiene mecanismos para negociar representaciones: `Accept` y `Content-Type`.
- Un MIME type propio puede expresar version y formato, por ejemplo `application/vnd.userlist.v1+json`.
- `Accept` indica que representacion espera el cliente en un `GET`.
- `Content-Type` indica que representacion esta enviando el cliente en un `POST`, `PUT` o `PATCH`.
- El cliente REST ideal conoce HTTP, verbos y links; no deberia necesitar detalles internos del servidor.

**Descriptor de API**

- La unidad menciona que `GET /` puede devolver un descriptor con recursos disponibles y parametros opcionales.
- No es obligatorio para el final, pero es consistente con una API mas navegable.
- Esto ayuda a que el cliente descubra capacidades sin hardcodear todo el mapa de endpoints.

**Autenticacion**

- En el modelo REST de la unidad no hay sesiones server-side ni cookies tradicionales para autenticar cada request.
- La autenticacion viaja como metadata HTTP.
- Un primer request puede usar `Authorization: Basic ...` y recibir tokens.
- Luego los requests usan `Authorization: Bearer {token}`.
- JWT aparece como token autocontenido: incluye informacion para identificar origen y validez.
- El apunte usa headers propios para token y refresh token; el criterio conceptual es que las credenciales viajen por headers y el servidor mantenga statelessness.
- No deberia existir un endpoint verbo como `/login` si se interpreta literalmente como accion; la autenticacion debe modelarse con recursos/metadata de forma consistente.

**Clase practica: password reset y content-types**

- El recupero de contraseña puede modelarse como operaciones sobre recursos existentes, no como pantallas.
- Si el usuario conoce su identidad, puede plantearse una actualizacion parcial sobre `/users/{id}`.
- Si no conoce datos suficientes, se puede distinguir contrato con content types propios.
- El punto importante no es copiar el endpoint exacto, sino entender que diferentes operaciones sobre un recurso pueden diferenciarse por representacion y `Content-Type`.
- La clase enfatiza usar muchos content-types propios cuando el contrato lo necesita.

**Imagenes**

- Para imagenes, la API no deberia devolver siempre el original si el cliente necesita una miniatura.
- Es valido ofrecer variantes por ancho/alto o parametros de transformacion.
- Esto reduce transferencia y evita que el frontend cargue assets mucho mas grandes que lo visible.
- El contrato debe seguir siendo claro: recurso imagen, representacion/tamano pedido y cache coherente.

**Riesgos practicos para TP final**

- Diseñar endpoints con verbos en paths en vez de recursos.
- Usar `/v1/...` por costumbre sin justificar versionado por representacion.
- Mezclar metadata de paginacion dentro del body cuando hay headers estandar.
- Embeber entidades relacionadas hasta duplicar caminos de escritura.
- Implementar auth como sesion server-side disfrazada y romper statelessness.
- Devolver status codes genericos que obligan al cliente a interpretar strings de error.

## Ver tambien
- [[tp1-vs-tpe2-final]]
- [[http-y-sesiones]]
- [[spring-web-mvc]]
- [[comparacion-jsp-formularios-e-i18n]]
- [[resumen-correcciones]]
- [[resumen-enunciado]]
- [[resumen-notas]]
- [[resumen-clases-paw-2026]]
- [[resumen-transcripciones-clases-2-a-4]]
- [[modelo-capas]]
- [[configuracion-maven]]
- [[jsp-jstl]]
- [[inyeccion-dependencias]]
- [[scrum-metodologia]]
- [[manejo-imagenes]]
- [[ux-flows]]
- [[paw-unidad-01-introduccion-scrum-mvp]]
- [[paw-unidad-02-contexto-historico-web]]
- [[paw-unidad-03-frontend-jsp-jstl]]
- [[paw-unidad-04-spring-mvc-capas-jdbc]]
- [[paw-unidad-05-unit-testing]]
- [[paw-unidad-06-formularios-i18n]]
- [[paw-unidad-07-seguridad-logging]]
- [[paw-unidad-08-aop-transacciones]]
- [[paw-unidad-09-hibernate-jpa]]
- [[paw-unidad-10-single-page-applications]]
- [[paw-unidad-11-api-rest]]
- [[spring-aop]]
- [[hibernate-jpa]]
- [[single-page-applications]]
- [[api-rest]]
