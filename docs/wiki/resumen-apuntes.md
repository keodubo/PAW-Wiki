---
titulo: Resumen de Apuntes de la Materia
tipo: fuente
fuentes: [raw/apuntes.txt, raw/pdfs/PAW - Apuntes.pdf]
creado: 2026-04-09
actualizado: 2026-04-13
---

# Resumen de Apuntes de la Materia

Apuntes completos de PAW (72.38), 1er cuatrimestre 2025. El documento tiene 11 unidades, pero para TPE1/TP1 las unidades 1-7 concentran la base metodologica, web, MVC, testing, formularios, i18n, seguridad y logging, mientras que la unidad 8 suma el soporte practico para entender proxies y `@Transactional`.

Nota de fuente: el `.txt` ingerido fue validado contra el PDF original; no aparecieron diferencias sustantivas tras normalizar la extraccion.

## Posicion editorial

- Esta es la **fuente teorica principal** del wiki para la materia.
- Para **TP1** conviene priorizar unidades 1-7 y sumar la unidad 8 como soporte de implementacion cuando aparezcan dudas sobre proxies o `@Transactional`.
- Para aterrizar implementacion, ejemplos y advertencias del profesor, conviene complementarla con [[resumen-notas]].
- Para corroboracion auxiliar o clase reciente sobre agentes/formularios, usar [[resumen-transcripciones-clases-2-a-4]] con mas cautela.
- La ruta concreta de estudio esta resumida en [[tp1-vs-tpe2-final]].

## Cobertura relevante para TP1

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

## Material que puede quedar para TPE2 / final

| Unidad | Tema | Paginas wiki relacionadas |
|--------|------|--------------------------|
| 9 | Hibernate / JPA / ORM | [[hibernate-jpa]] |
| 10 | Single Page Applications | [[single-page-applications]] |
| 11 | API REST | [[api-rest]] |

## Ver tambien
- [[tp1-vs-tpe2-final]]
- [[http-y-sesiones]]
- [[spring-web-mvc]]
- [[comparacion-jsp-formularios-e-i18n]]
- [[resumen-correcciones]]
- [[resumen-enunciado]]
- [[resumen-notas]]
- [[resumen-transcripciones-clases-2-a-4]]
- [[modelo-capas]]
- [[configuracion-maven]]
- [[jsp-jstl]]
- [[inyeccion-dependencias]]
- [[scrum-metodologia]]
- [[manejo-imagenes]]
- [[ux-flows]]
- [[spring-aop]]
- [[hibernate-jpa]]
- [[single-page-applications]]
- [[api-rest]]
