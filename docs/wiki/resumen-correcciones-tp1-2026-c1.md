---
titulo: Devolucion TP1 2026 C1
tipo: fuente
fuentes: [raw/devolucion_tp1_2026_c1.txt, raw/pdfs/Devolucion_TP1_2026_C1.pdf]
creado: 2026-05-31
actualizado: 2026-05-31
---

# Devolucion TP1 2026 C1

Ingesta publica de la devolucion TP1 generada por la catedra el 2026-05-27.
El PDF original queda preservado en `raw/pdfs/Devolucion_TP1_2026_C1.pdf` y
el texto extraido queda en `raw/devolucion_tp1_2026_c1.txt` para busqueda.

La fuente cubre 14 grupos, con secciones de demo, codigo, seguimiento y nota.
Aplica a **TP1**: Spring MVC server-side, JSP/JSTL, JDBC, Spring Security,
Logback, Maven multi-modulo y testing unitario. Sirve como checklist de riesgo
antes de cerrar TP1 y tambien como deuda a no arrastrar a TP2.

## Lectura rapida

| Tema | Riesgo | Donde revisar |
|------|--------|---------------|
| Controllers con logica de negocio, validacion manual o permisos manuales | Critico | [[logica-en-controllers]], [[spring-security]], [[validacion-formularios]] |
| N+1, joins en Java, filtrado/paginado en memoria | Critico | [[n-plus-1-joins-java]], [[persistencia-jdbc]] |
| Tests no unitarios, `verify`, `doAnswer`, `AtomicReference`, reflection o asserts sobre fuente | Critico | [[testing-unitario]], [[comparacion-testing-servicios-y-daos]] |
| Remember-me key hardcodeada, secretos, ownership incompleto, XSS y LIKE sin escape | Critico | [[spring-security]], [[auth-flows]], [[xss-prevencion]] |
| Dependencias/versiones en POM hijos, scopes incorrectos, WAR/target/scraping versionados | Alto | [[configuracion-maven]] |
| Locale, mails, links absolutos, `LocaleContextHolder` en async y texto hardcodeado | Alto | [[internacionalizacion]], [[mailing]], [[jsp-jstl]] |
| `@Transactional` ausente o sin `readOnly=true`, WebConfig usado para migraciones | Alto | [[transactional]], [[modelo-capas]] |
| UX de demo: flujos rotos, estados inconsistentes, CTA ausente, modales grandes, paginacion confusa | Medio-alto | [[ux-flows]], [[buenas-practicas]] |

## Distribucion de notas

| Nota | Grupos |
|------|--------|
| 7 | 8 |
| 6 | 10 |
| 5 | 1, 12, 13 |
| 4 | 3, 11 |
| 3 | 2, 4, 5, 6, 7, 9, 14 |

Promedio aproximado: **4.07**. El seguimiento no resto puntos en los grupos
registrados, pero el grupo 11 deployo durante la demo de otros grupos y la
catedra tomo el horario del mail posterior como horario de entrega. La leccion
operativa es separar demo, deploy y cierre de commit: no publicar durante una
instancia evaluativa.

## Reincidencias criticas

### Controllers fuera de rol

La devolucion repite en casi todos los grupos que los controllers no deben
contener reglas de dominio, autorizacion, parsing complejo, normalizacion,
validaciones manuales, creacion coordinada de varias entidades ni queries
indirectas.

Patrones penalizados:

- `hasRole(...)`, `isAdmin()`, `isMatchVisibleToUser` o chequeos de ownership en
  controllers.
- Redirecciones manuales a login en vez de dejar que Spring Security intercepte
  el POST o la ruta protegida.
- Controllers que orquestan varios services para una unica accion de dominio.
- Clases `MvcSupport` que esconden logica de negocio pegada a webapp.
- Parsing manual de numeros, enums, fechas, `trim`, `normalize` y reglas de
  formato que deberian quedar en forms, converters o Bean Validation.
- Controllers que deciden HTTP status inspeccionando subtipos internos en vez
  de delegar a excepciones de dominio y `ControllerAdvice`.

Regla util: el controller hace binding, validacion, delegacion y vista/redirect.
Si una decision seguiria existiendo aunque la UI cambiara, pertenece a service,
security, validator o persistence.

### Seguridad y privacidad

Errores fuertes detectados:

- Remember-me key hardcodeada, debil o con fallback publico en el repo.
- Secretos, URLs o claves en codigo fuente/config versionada.
- App base URL para mails calculada desde `HttpServletRequest`, abriendo riesgo
  de Host header injection. Debe ser una property, por ejemplo `app.base-url`.
- Login programatico luego de verificar email sin invalidar sesion previa.
- Ban que solo impide login futuro, pero permite operar si la sesion ya estaba
  abierta.
- Ownership parcial: por ejemplo reservar con `vehicleId` ajeno si se conoce el
  ID, o editar/eliminar recursos ajenos si el controller olvida validar.
- JSPs que imprimen datos dinamicos sin `<c:out>` o sin escape en atributos.
- Busquedas `LIKE` que agregan `%` pero no escapan `%` y `_`.
- Links por token que ignoran al usuario autenticado actual y permiten acciones
  a cualquiera que tenga el link.

Buenas practicas observadas como positivas: verificaciones de permisos que
resisten `curl`, custom access checks consistentes y escape correcto de entradas
de busqueda.

### Persistencia y performance

La catedra marca como error conceptual grave resolver en Java lo que debe hacer
la base de datos.

Patrones repetidos:

- Buscar IDs y luego hacer `findById` dentro de un loop.
- RowMappers que hacen subqueries por fila.
- Filtrar, ordenar, deduplicar o repaginar en memoria.
- Usar `Integer.MAX_VALUE` o limites enormes para simular "traer todo".
- Construir `count` acoplado con reemplazos de strings sobre la query principal.
- Leer columnas nullable con `rs.getLong()` y perder la diferencia entre `NULL`
  y `0`.
- DAOs que modifican tablas ajenas a su entidad, generando multiples owners de
  una misma tabla.
- WebConfig ejecutando queries o migraciones manuales al iniciar la app.

Regla util: para listados y dashboards, la query debe traer exactamente la
pagina necesaria con `WHERE`, `JOIN`, `ORDER BY`, `LIMIT` y `OFFSET`. La capa de
services puede orquestar, pero no debe compensar una query pobre con loops.

### Testing

La devolucion refuerza el criterio mas importante del wiki: los tests deben ser
unitarios, blackbox y de comportamiento observable.

Antipatrones penalizados:

- `Mockito.verify()` o variantes encubiertas con `doAnswer`,
  `AtomicReference`, `AtomicBoolean` o capturas de argumentos.
- Tests de persistencia que preparan o verifican usando el mismo DAO bajo test.
- Tests que ejercitan multiples metodos de la clase bajo test para setup y
  validacion.
- Tests que solo hacen `assertDoesNotThrow(...)` sin observar el contrato.
- Tests de views que leen archivos JSP/HTML/Thymeleaf y buscan strings exactos.
- Tests con reflection para acoplarse a nombres internos.
- Tests enormes con setup custom de cientos o miles de lineas.
- JUnit 4 y JUnit 5 mezclados sin necesidad.

La catedra tambien penaliza cobertura pobre en clases con mucha logica, sobre
todo services. Si no hay forma de observar el comportamiento por contrato
publico, el problema es de diseno o de falta de surface testeable, no una excusa
para testear internals.

### Maven, repo hygiene y configuracion

Reincidencias:

- Versiones de dependencias o plugins redeclaradas en POM hijos en vez de
  `dependencyManagement` / `pluginManagement` del padre.
- Scopes incorrectos: `spring-jdbc` en webapp, `mockito-junit-jupiter` sin
  `test`, `javax.servlet-api` sin `provided`, services dependiendo de
  persistence en vez de persistence-contracts.
- Java 17 redeclarado en un modulo cuando el padre define Java 21.
- JUnit 4 y JUnit 5 definidos a la vez.
- `target/`, `webapp.war`, `.vscode`, `.mvn` vacios o carpetas de scraping
  pesadas versionadas.
- `AGENTS.md` agregado al `.gitignore`, lo que deja al equipo sin reglas
  compartidas.
- Multiples archivos de contexto solapados (`CLAUDE.md`, `AGENTS.md`) con
  informacion duplicada, irrelevante o contradictoria.

Regla util: el POM padre centraliza versiones y scopes globales. Los hijos
declaran necesidad, no politica de versionado.

### Internacionalizacion, mails y JSP

Patrones repetidos:

- Textos visibles hardcodeados en JSPs, tag files, controllers, aria-labels y
  mensajes de error.
- Locale persistido o resuelto de forma manual por query param sin respetar
  negociacion HTTP ni una UX clara de override.
- Mails que usan `LocaleContextHolder` en contexto async en vez del locale del
  destinatario.
- Templates o recursos de mail repartidos entre webapp y services.
- Links de mail mal formados, sin `c:url` en vistas, o apuntando a rutas
  incorrectas.
- Pluralizacion rota, por ejemplo "1 reseñas".

Regla util: el texto visible sale de `messages*.properties`; el mail debe usar
el locale del destinatario; las URLs internas en JSP deben pasar por `<c:url>`.

### UX y demo

La demo no se evalua solo por cantidad de features. Se penaliza que los flujos
principales no sean claros, que haya estados muertos o que el equipo no pueda
explicar la aplicacion.

Problemas recurrentes:

- Despues de login/registro interceptado, volver a home en vez de continuar la
  accion original.
- Paginacion incompleta, pagina negativa con `500`, `pageSize` ignorado o solo
  aceptado si viene del dropdown.
- Modales con formularios grandes o multi-step.
- CTAs ausentes en empty states, busquedas sin boton limpiar, acciones
  terminales sin confirmacion.
- Funcionalidad a medias expuesta en UI: estados imposibles, botones rotos,
  filtros no conectados, previews que no funcionan.
- Date pickers, horarios y zonas horarias inconsistentes.
- Navbar o item activo que no refleja la pagina actual.
- Locale que no cambia realmente de idioma o no respeta input numerico.
- Imagenes pixeladas, carruseles sin limite, paginas sobrecargadas de texto o
  informacion.

Una observacion especialmente importante: la catedra marco que no puede pasar
que el equipo no sepa responder como funciona la aplicacion. Usar agentes exige
entender, validar y hacerse responsable de los cambios introducidos.

## Checklist para nuestro TP1/Forkd

Antes de cerrar o migrar deuda desde TP1, revisar:

1. No hay endpoints protegidos solo por checks manuales en controllers.
2. No hay business logic de reservas, reviews, restaurantes, listas o owner
   dashboard en controllers o helpers web.
3. Los listados usan queries paginadas y con joins, no loops de `findById`.
4. `remember-me` usa una key externa fuerte, sin fallback publico.
5. Mails usan base URL configurada y locale del destinatario.
6. Todo texto visible de JSPs, tags, mails y aria labels esta internacionalizado.
7. `LIKE` escapa `%` y `_`.
8. Salida dinamica en JSP usa `<c:out>` o equivalente seguro.
9. POM padre centraliza versiones/scopes; webapp no depende de JDBC ni de
   implementaciones de persistence.
10. Tests nuevos o modificados son unitarios, blackbox y de comportamiento.
11. No hay `verify`, `doAnswer`, `AtomicReference`, reflection ni asserts sobre
    strings de fuente como objetivo del test.
12. La demo principal tiene CTAs, empty states, paginacion y redirects
    consistentes.

## Ver tambien

- [[resumen-correcciones]]
- [[resumen-correcciones-tp2]]
- [[criterios-evaluacion]]
- [[tp1-vs-tpe2-final]]
- [[modelo-capas]]
- [[logica-en-controllers]]
- [[spring-security]]
- [[validacion-formularios]]
- [[persistencia-jdbc]]
- [[n-plus-1-joins-java]]
- [[testing-unitario]]
- [[configuracion-maven]]
- [[internacionalizacion]]
- [[mailing]]
- [[ux-flows]]
- [[xss-prevencion]]
