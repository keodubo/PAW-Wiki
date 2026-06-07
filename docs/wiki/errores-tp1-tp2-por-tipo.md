---
titulo: Errores TP1 y TP2 por Tipo
tipo: sintesis
fuentes: [raw/correcciones_tp1.md, raw/correcciones_tp2.md, raw/apuntes.txt, raw/pdfs/PAW - Apuntes.pdf]
creado: 2026-06-06
actualizado: 2026-06-06
---

# Errores TP1 y TP2 por Tipo

Sintesis combinada de los errores historicos de `docs/raw/correcciones_tp1.md`
y `docs/raw/correcciones_tp2.md`, con contexto de capas de
`docs/raw/pdfs/PAW - Apuntes.pdf`, agrupados por tipo de error para usarlos
como base de una auditoria por fases antes de entregar TP2.

Esta pagina no es todavia un plan de correccion. Es un inventario de patrones a
chequear: cada seccion puede convertirse luego en una fase.

## Alcance al cruzar con apuntes

- Para estar por entregar **TP2**, el foco real es: unidades 1-9 de
  `PAW - Apuntes.pdf`, mas las correcciones historicas TP1/TP2.
- Las unidades 10 y 11, sobre SPA y API REST, son contexto de **TP final**. Para
  TP2 funcionan como guardrail negativo: no introducir SPA, API REST, auth
  stateless, frontend build tooling ni cambios de contrato HTTP salvo pedido
  explicito.
- Si el repo ya tuviera algo de TP final por decision propia, auditarlo contra
  esas unidades. Si no, no convertirlo en fase de remediacion TP2.

## Prioridad recomendada de fases

| Orden | Tipo de error | Motivo |
| --- | --- | --- |
| 1 | Paginacion, consultas y performance | Es uno de los errores mas repetidos y en TP2 se agrava con Hibernate. |
| 2 | Impedancia objeto-relacional y paginacion `OneToMany` | Es el error clasico de Hibernate: SQL pagina filas, Java/Hibernate reconstruye entidades. |
| 3 | JPA/Hibernate y modelo de relaciones | Define si la migracion TP2 esta bien hecha o solo compila. |
| 4 | HTTP, sesiones y semantica web | Evita estado implicito, side effects por GET y flujos que dependen mal de session/cookie. |
| 5 | Seguridad, ownership y XSS | Puede permitir acciones sobre recursos ajenos o exponer datos de usuario. |
| 6 | Responsabilidades de capa y dominio | Captura errores como poner logica de negocio donde no corresponde, incluso en models. |
| 7 | Arquitectura MVC, DI y capas | Evita controllers gordos, wiring manual, transacciones rotas y acoplamiento web-dominio. |
| 8 | JSP/JSTL, vistas y tags server-side | Cubre scriptlets, JSP expuestas, tags con negocio y errores propios de vista. |
| 9 | Testing | La catedra penaliza fuerte tests verdes que no prueban comportamiento. |
| 10 | Transacciones y excepciones | Impacta consistencia, rollback, errores 500 y diagnostico. |
| 11 | Forms, validacion, i18n, UX y mails | Suele aparecer en demo y en flujos principales. |
| 12 | Manejo de imagenes y archivos | Es requisito core TP1/TP2 y cruza upload, DB, cache, endpoints y validacion. |
| 13 | Maven, configuracion y repo hygiene | Riesgo de entrega, deploy, secretos y build reproducible. |
| 14 | Seguimiento y ownership del equipo | Riesgo de proceso, explicabilidad y defensa oral. |

## Paginacion, consultas y performance

Errores combinados:

- [TP2] No usar modelo 1+1 de paginacion: primero IDs de la pagina y luego carga
  controlada del grafo necesario.
- [TP2] Paginar directamente sobre entidades con relaciones, especialmente con
  `JOIN FETCH`, colecciones `toMany`, `EAGER` o grafos grandes.
- [TP1/TP2] Traer toda una tabla o coleccion y paginar, filtrar, ordenar o
  deduplicar en memoria.
- [TP1/TP2] Hacer loops de `findById`, rowmappers con subqueries por fila o
  consultas N+1 en listados.
- [TP1] Resolver joins en Java en lugar de usar `JOIN`, `WHERE`, `ORDER BY`,
  `LIMIT` y `OFFSET` en la base.
- [TP2] Usar `.size()` sobre colecciones lazy cuando solo se necesita un
  `COUNT`.
- [TP2] Usar `.clear()`, `.add()` o `.remove()` sobre colecciones managed sin
  considerar que pueden materializar listas completas.
- [TP2] No paginar colecciones potencialmente grandes: comentarios, hijos de
  comentarios, amigos, likes, suscriptores, enrollments, participantes,
  equipos, reservas, estudios, turnos, doctores autorizados o notificaciones.
- [TP2] Endpoints auxiliares que devuelven listas sin limite o fallan con 500 si
  falta un query param.
- [TP1/TP2] Aceptar `page` o `size` negativos y terminar en 500.
- [TP2] Mostrar una pagina inexistente tipo "Pagina 55 / 3" en vez de clamplear,
  redirigir o devolver una respuesta consistente.
- [TP1] Usar limites enormes como `Integer.MAX_VALUE` para simular "traer todo".
- [TP1] Construir queries de `count` reemplazando strings sobre la query
  principal sin asegurar que el resultado sea correcto.
- [TP2] No revisar el SQL generado por Hibernate antes de asumir que el listado
  es eficiente.

Chequeo de fase:

- Verificar cada listado, filtro, busqueda y dashboard con cardinalidad alta.
- Confirmar que la query trae la pagina exacta y que no hay loops de queries por
  fila.
- Auditar el SQL real generado por Hibernate para listados TP2.

## Impedancia objeto-relacional y paginacion `OneToMany`

Errores combinados:

- [TP2] No respetar la impedancia objeto-relacional: pensar que paginar
  entidades Java es equivalente a paginar filas SQL.
- [TP2] Paginar una query con `JOIN FETCH` sobre una coleccion `@OneToMany` o
  `@ManyToMany`.
- [TP2] Usar `setFirstResult` / `setMaxResults`, `LIMIT` / `OFFSET` o una
  abstraccion de paginado sobre una query que expande la entidad raiz en varias
  filas por cada hijo.
- [TP2] Confiar en que Hibernate va a deduplicar entidades raiz sin alterar la
  semantica de pagina.
- [TP2] Obtener menos padres que `pageSize` porque el `LIMIT` corto filas
  duplicadas del join antes de reconstruir entidades.
- [TP2] Tener paginas inestables: un padre con muchos hijos desplaza otros
  padres a paginas siguientes.
- [TP2] Cargar colecciones parciales o inconsistentes porque la pagina corto
  solo una parte de los hijos de una entidad.
- [TP2] Calcular `count` sobre filas expandidas por join en vez de contar
  entidades raiz.
- [TP2] Resolver el problema con `distinct` sobre toda la query sin entender si
  se esta deduplicando en SQL, en memoria o despues de haber aplicado el limite.
- [TP2] Ignorar warnings de Hibernate asociados a paginar con collection fetch,
  como paginacion aplicada en memoria.

Por que es un error:

- SQL pagina filas.
- Hibernate/JPA devuelve objetos.
- Una entidad raiz con una coleccion `toMany` aparece repetida en varias filas
  SQL, una por cada hijo del join.
- Si se aplica `LIMIT/OFFSET` sobre esas filas, la pagina resultante ya no
  representa "N entidades raiz", sino "N filas del producto join".

Patron estable esperado:

1. Query de IDs de la entidad raiz con filtros, orden real y paginacion.
2. Query de carga por `id IN (:ids)` para traer las entidades y relaciones que
   necesita la pantalla.
3. Reordenar los resultados segun el orden de IDs de la pagina.
4. Hacer el `count` sobre entidades raiz, no sobre filas multiplicadas por el
   join.

Chequeo de fase:

- Buscar `JOIN FETCH` o `LEFT JOIN FETCH` combinado con paginacion.
- Revisar tambien Criteria API, query builders y DAOs helper que oculten
  `setFirstResult`, `setMaxResults`, `LIMIT` u `OFFSET`.
- Distinguir: `ManyToOne`/`OneToOne` fetch join suele ser seguro para paginar;
  collection fetch join (`OneToMany`/`ManyToMany`) es la frontera peligrosa.
- Probar con un padre que tenga muchos hijos y varios padres con pocos hijos. Si
  aparecen menos padres, paginas raras o counts inflados, hay bug.

## JPA/Hibernate y modelo de relaciones

Errores combinados:

- [TP2] Migrar a Hibernate conservando IDs pelados donde deberian existir
  relaciones JPA reales.
- [TP2] No declarar foreign keys o constraints coherentes con el dominio.
- [TP2] Usar `FetchType.EAGER` por conveniencia, especialmente en `ManyToOne` o
  relaciones `toMany`.
- [TP2] Dejar relaciones `toMany` sin `LAZY` o sin estrategia explicita de carga
  y paginacion.
- [TP2] Usar `JOIN FETCH` como solucion universal y multiplicar filas o traer
  combinaciones enormes.
- [TP2] Acceder relaciones lazy en contextos `@Async` sin materializarlas dentro
  de una transaccion.
- [TP2] Arrastrar bytes de imagenes o archivos por relaciones cuando deberian
  servirse por endpoint separado.
- [TP2] No usar `distinct` cuando se obtienen IDs para un patron 1+1 que lo
  requiere.
- [TP2] Usar `em.unwrap(Session.class)` o `addScalar` sin motivo claro.
- [TP2] No aprovechar Hibernate para representar invitaciones, reservas, clubes,
  usuarios o relaciones como entidades del dominio.
- [TP2] Persistir contadores derivados cuando una solucion como `@Formula` o
  `COUNT` podria evitar inconsistencias.
- [TP2] Usar queries nativas por inercia cuando HQL/JPQL expresaria mejor el
  caso.
- [TP2] Query builders complejos sin tests suficientes para combinaciones de
  filtros, parametros ausentes y precedencia logica.
- [TP2] Queries con precedencia incorrecta, por ejemplo `A AND B AND C OR D`,
  que traen datos invalidos en casos borde.

Chequeo de fase:

- Revisar mappings, fetches, cascades, constraints y cardinalidades.
- Probar listados con datos suficientes para detectar N+1, duplicados y
  explosiones de grafo.
- Confirmar que JSP/services no dependen de lazy loading fuera de transaccion.

## Persistencia JDBC, schema y ownership de tablas

Errores combinados:

- [TP1] Dejar `schema.sql` en `webapp` cuando corresponde a persistence.
- [TP2] Mantener `schema.sql` en el modulo incorrecto durante la migracion.
- [TP1] Usar `java.sql` en models, webapp o services; la base debe ser detalle
  de persistence.
- [TP1] Usar `java.sql.Date` o `Timestamp` fuera de persistence en lugar de
  tipos como `LocalDate`, `LocalDateTime` o `Instant`.
- [TP1] DAOs que modifican tablas ajenas, dejando multiples owners de una misma
  tabla.
- [TP1] Eliminar relaciones a mano en vez de modelar cascadas o constraints
  apropiadas.
- [TP1] Inicializar `SimpleJdbcInsert` o helpers equivalentes en cada llamada,
  con riesgo de concurrencia y overhead.
- [TP1] Usar `jdbcTemplate.update` para inserts donde el wrapper de insert
  inyectable era mas claro.
- [TP1] RowMappers no `private static final` instanciados repetidamente.
- [TP1] Leer columnas nullable con `rs.getLong()` sin distinguir `NULL` de `0`.
- [TP1] Precondiciones de tests de persistence armadas con DAOs en vez de SQL o
  fixtures controladas.
- [TP1] No capturar excepciones de FK o constraints en DAO y dejar que exploten
  como 500 sin contrato amistoso.

Chequeo de fase:

- Confirmar que persistence es la unica capa que conoce detalles SQL/JDBC.
- Revisar que cada tabla tenga un owner claro.
- Auditar bootstrap, schema de test y schema runtime.

## HTTP, sesiones y semantica web

Errores combinados:

- [Apuntes/TP1/TP2] Asumir que HTTP mantiene estado por si mismo. HTTP es
  request-response y stateless; si un flujo necesita contexto, debe quedar claro
  si usa request, session, cookie, token o base de datos.
- [Apuntes/TP1/TP2] Guardar en session datos que podrian viajar por un form
  validado, por la URL o resolverse desde base con permisos.
- [Apuntes/TP1/TP2] Depender de `session.getAttribute(...)` como contrato
  implicito de un flujo multi-paso sin validar presencia, expiracion ni
  pertenencia.
- [TP1/TP2] Mutar estado con `GET`, por ejemplo actualizar locale, confirmar
  acciones, cancelar entidades o disparar side effects.
- [TP1/TP2] Usar `history.back()` o redirects genericos que no respetan la
  semantica de PRG, pueden reenviar POST o dejan al usuario atrapado.
- [TP1/TP2] Manejar login, logout, remember-me o usuario actual con session HTTP
  manual cuando Spring Security ya provee el contexto.
- [TP1/TP2] Suponer que ocultar un boton en la UI alcanza para proteger una
  accion. El servidor debe volver a decidir permisos en cada request.
- [Apuntes/TP1/TP2] Diseñar flujos que solo funcionan con un servidor unico y
  session local, sin tener en cuenta que session/cookies complican escalado si
  hay multiples nodos.
- [TP1/TP2] Construir base URLs desde `HttpServletRequest` para links externos,
  mails o callbacks, en vez de usar una property confiable.
- [TP2] Dejar dependencias de lazy loading hasta la vista o hasta una session que
  ya no deberia estar abierta.

Chequeo de fase:

- Para cada flujo core, anotar explicitamente que estado viaja en request, URL,
  cookie/session y DB.
- Buscar `session.getAttribute`, `getSession`, `HttpServletRequest`,
  `history.back`, handlers `GET` con side effects y redirects libres.
- Confirmar PRG para acciones de escritura: POST/redirect/GET, no mutaciones por
  GET.
- Validar que el usuario no pueda operar solo por conservar una cookie/session
  vieja despues de cambios de cuenta, permisos o bloqueo.

## Responsabilidades de capa y dominio

Errores combinados:

- [TP1/TP2] Poner logica de negocio en una capa que no corresponde. El caso mas
  repetido es controller, pero el error aplica tambien a `models`,
  persistence, contracts, JSPs, helpers web o servicios de infraestructura.
- [TP1/TP2] Modelos que intentan resolver casos de uso completos, por ejemplo
  decidir permisos, coordinar creaciones, ejecutar flujos de reserva, mandar
  mails, conocer rutas, leer sesion o depender de Spring Security.
- [TP1/TP2] Modelos que conocen detalles de infraestructura: HTTP, JSP,
  `ModelAndView`, DAOs, SQL, `java.sql`, transacciones, templates de mail,
  paths de webapp o properties de configuracion.
- [TP1/TP2] Modelos usados como bolsas de datos para una pantalla especifica en
  vez de representar conceptos del dominio.
- [TP1/TP2] Modelos con informacion parcial creada desde controllers o services,
  sin pasar por invariantes claras ni por persistence cuando corresponde.
- [TP2] Entidades Hibernate que guardan solo IDs pelados y obligan a rearmar el
  dominio manualmente en services/controllers.
- [TP1/TP2] Services que hablan en terminos de vistas o recursos web en vez de
  casos de uso del dominio.
- [TP1/TP2] Persistence que compensa reglas de negocio que deberian estar en
  services, o services que compensan queries pobres con loops y filtros en Java.
- [TP1/TP2] Contracts contaminados con detalles de implementacion, como
  dependencias a `spring-web`, `spring-jdbc`, tipos SQL o clases concretas de
  persistence.
- [TP1/TP2] JSPs o tag files decidiendo reglas de negocio o seguridad, en lugar
  de solo renderizar un estado ya autorizado y preparado.

Aclaracion importante:

- No todo comportamiento en `models` esta mal. Puede ser razonable que un modelo
  tenga invariantes simples, metodos derivados o reglas locales del objeto. Lo
  que esta mal es poner ahi logica de aplicacion: flujos, permisos, IO, queries,
  transacciones, mails, HTTP/session o coordinacion entre multiples agregados.

Contexto de `PAW - Apuntes.pdf`:

- El modelo de capas se presenta como una cebolla: cada capa habla con la capa
  inmediatamente inferior, no saltea capas.
- La logica de negocio propiamente dicha vive en capas intermedias.
- La capa dedicada para los casos de uso se llama `services`.
- Un service funciona como fachada del caso de uso: recibe una intencion de la
  webapp, aplica reglas de negocio, coordina DAOs y define el borde
  transaccional.
- `models` contiene entidades/objetos de dominio compartidos. Puede expresar
  estado e invariantes locales, pero no debe coordinar casos de uso ni conocer
  infraestructura.

Regla operativa para correr esta fase:

| Capa | Debe hacer | No debe hacer |
| --- | --- | --- |
| `webapp` | HTTP, binding, `@Valid`, forms, vista/redirect | reglas de negocio, SQL, crear entidades parciales, ownership manual |
| `services` | casos de uso, reglas de negocio, transacciones, orquestar DAOs, mails de negocio | conocer JSPs, paths web, `ModelAndView`, SQL concreto |
| `persistence` | JPQL/SQL, DAOs, mappings, fetches, joins, paginacion eficiente | reglas de negocio, decisiones de UI, seguridad web |
| `models` | entidades/POJOs, estado del dominio, invariantes locales simples | HTTP/session, Spring Security, DAOs, queries, transacciones, mails, flujos |
| `contracts` | interfaces y DTOs de contrato | dependencias de implementacion o infraestructura |

Chequeo de fase:

- Para cada regla, preguntar: si cambio la UI, la DB o Spring Security, esta
  regla deberia seguir igual? Si si, probablemente es dominio/service; si depende
  de HTTP, vista o infraestructura, no pertenece a `models`.
- Si la regla coordina multiples entidades, decide un flujo o necesita
  transaccion, debe caer en `services`.
- Revisar imports de `models` y contracts: no deberian revelar web,
  persistence, SQL, Spring MVC ni security.
- Revisar modelos grandes o "data wrappers" que existan solo para alimentar una
  JSP.

## Arquitectura MVC, DI y separacion de capas

Errores combinados:

- [TP1/TP2] Controllers con logica de negocio: validaciones de dominio,
  ownership, updates parciales, armado de DTOs complejos o decisiones de flujo.
- [TP1/TP2] Controllers que llaman multiples services para una unica accion de
  negocio en lugar de delegar a un metodo de caso de uso.
- [TP1/TP2] Controllers que instancian modelos de dominio o entidades parciales.
- [TP1] Controllers que populan listas por referencia o manipulan objetos de
  dominio como estructura mutable auxiliar.
- [TP1/TP2] Validar forms a mano en controller en lugar de usar Bean Validation,
  custom validators, converters o forms dedicados.
- [TP2] Services que conocen vistas, `Model`, `Map<String, Object>`,
  `ModelAndView`, nombres de templates o estructuras pensadas para JSP.
- [TP1/TP2] Servicios o interfaces que dependen de infraestructura que no les
  corresponde, como `spring-web`, `spring-jdbc` o recursos de webapp.
- [TP1] Implementar services dentro de la capa web.
- [Apuntes/TP1/TP2] Instanciar dependencias manualmente, por ejemplo
  `new ServiceImpl()` desde controllers, en vez de usar DI por contrato.
- [Apuntes/TP1/TP2] Acoplarse a implementaciones concretas cuando la capa
  deberia compilar contra interfaces/contratos.
- [Apuntes/TP1/TP2] Component scan demasiado amplio o duplicado que termina
  registrando beans que no corresponden.
- [Apuntes/TP1/TP2] Resolver multiples implementaciones por accidente, sin una
  decision explicita via `@Qualifier`, `@Primary` o configuracion.
- [TP1/TP2] Duplicar logica transversal con helpers web en vez de usar
  `ControllerAdvice`, validators, security o services.
- [TP1/TP2] Mezclar exception handling con logica de usuario logueado en un
  mismo advice.
- [TP1] `ControllerAdvice` fuera de paquetes escaneados por Spring.
- [TP1] Configurar exception resolvers y exception handlers duplicados para las
  mismas excepciones.
- [TP1] Mapear manualmente rutas de errores que Spring/MVC deberia resolver.
- [TP1/TP2] Abusar de magic strings y numeros magicos para roles, tabs, dias,
  estados, order by o secciones.

Chequeo de fase:

- Revisar controllers por verbo: binding, validacion, delegacion y vista o
  redirect. Si hace mas que eso, mover responsabilidad.
- Identificar flujos con muchas llamadas a services y convertirlos luego en
  casos de uso transaccionales.
- Revisar imports y constructores: controllers/services no deberian crear
  implementaciones concretas con `new`, ni compilar contra modulos que solo
  deberian estar en runtime.

## JSP/JSTL, vistas y tags server-side

Errores combinados:

- [Apuntes/TP1/TP2] Usar scriptlets o Java arbitrario en JSP.
- [Apuntes/TP1/TP2] Dejar JSPs accesibles por URL directa fuera de `WEB-INF`,
  salteando controllers y filtros esperados.
- [Apuntes/TP1/TP2] Usar JSPs, tags o fragments para resolver reglas de negocio,
  seguridad u ownership.
- [Apuntes/TP1/TP2] Crear tags propios para esconder logica compleja en vez de
  encapsular un patron repetido de presentacion.
- [Apuntes/TP1/TP2] Renderizar vistas con modelos incompletos que obligan a
  hacer queries, lazy loading o decisiones de dominio durante rendering.
- [Apuntes/TP1/TP2] No usar EL/JSTL/Spring form tags cuando eso evitaria
  duplicacion, errores de binding o errores de preservacion de input.
- [TP1/TP2] Mezclar estilo inline repetido, markup duplicado y componentes sin
  criterio, en vez de CSS/tags reutilizables.
- [Apuntes/TP2] Introducir SPA o tooling frontend por accidente en una etapa
  server-side cuando el stack esperado sigue siendo Spring MVC + JSP/JSTL.

Chequeo de fase:

- Buscar `<%`, scriptlets, JSPs fuera de `WEB-INF`, tags con llamadas a services
  o queries y fragments que deciden permisos.
- Revisar que las vistas solo presenten datos ya preparados y autorizados.
- Confirmar que links/assets/forms pasan por tags adecuados: `c:url`, `c:out`,
  Spring form tags y mensajes i18n.

## Seguridad, autorizacion y ownership

Errores combinados:

- [TP1/TP2] Proteger solo por autenticacion o rol general sin validar ownership
  del recurso.
- [TP1/TP2] Permitir editar, borrar, reservar, asignar o aceptar recursos ajenos
  conociendo IDs o URLs directas.
- [TP1/TP2] Validar permisos con `if (loggedUser != null)`, `user == null`,
  `hasRole`, `isAdmin`, `validateAdmin` o chequeos equivalentes en controllers.
- [TP1/TP2] Repartir reglas entre `WebAuthConfig`, `@PreAuthorize`,
  controllers, services y JSP sin estrategia consistente.
- [TP2] Poner `@PreAuthorize` en services si el criterio del proyecto es que
  seguridad de rutas y ownership viva en web/security.
- [Apuntes/TP1/TP2] Ignorar CSRF en acciones de escritura o no sostener una
  configuracion clara de Spring Security para forms.
- [Apuntes/TP1/TP2] Guardar passwords en texto plano o usar encoders de ejemplo
  en lugar de BCrypt.
- [TP1/TP2] Links visibles que llevan a 403 o 404 evitables.
- [TP1/TP2] Rutas con orden de reglas incorrecto, por ejemplo un
  `/**.authenticated()` que deja sin efecto reglas mas restrictivas.
- [TP1] Configurar rutas 403/404 como ignoradas y luego incluirlas tambien en
  `HttpSecurity`.
- [TP1] Mezclar reglas restrictivas y luego `permitAll()` para las mismas rutas.
- [TP1/TP2] No bloquear sesiones ya abiertas cuando se banea o bloquea un
  usuario.
- [TP1] Usar `accountNonLocked` de forma incompleta o validar bloqueos en
  helpers en lugar de integrarlos a Spring Security.
- [TP1/TP2] Usar 403 cuando corresponde 404 o 404 cuando corresponde 403, sin
  criterio de seguridad claro.
- [TP1/TP2] Manejar manualmente login/sesion HTTP en controller cuando Spring
  Security ya lo resuelve.
- [TP1/TP2] Flujos de reset/verificacion donde el token no alcanza por si mismo,
  se manda tambien `userId`, o se permite operar con links de token sin chequear
  usuario actual.
- [TP1] Endpoint POST `/logout` propio cuando el logout ya lo maneja Spring
  Security.
- [TP1/TP2] Remember-me key hardcodeada, trivial, con fallback publico o en el
  repo.
- [TP1/TP2] Credenciales de Gmail, DB, mailing u otros secretos versionados.

Chequeo de fase:

- Probar con URLs directas y usuarios cruzados, no solo desde la UI.
- Revisar orden de reglas de Spring Security y estrategia unica de ownership.
- Confirmar que secretos y keys salen de config externa.
- Confirmar BCrypt, CSRF y logout/login delegados a Spring Security salvo razon
  explicita.

## XSS, salida HTML, URLs y busquedas LIKE

Errores combinados:

- [TP1/TP2] Imprimir contenido dinamico en JSP con `${...}` sin `<c:out>` o
  escape equivalente.
- [TP1/TP2] Imprimir atributos, mensajes, opciones, nombres o texto libre del
  usuario sin escape.
- [TP1/TP2] Depender solo de patterns de forms para evitar XSS.
- [TP2] Un intento de XSS devuelve error pero igual persiste datos o deja estado
  parcial.
- [TP1] Intentar XSS en creacion de menu termina en 500.
- [TP1/TP2] No usar `<c:url>` para URLs internas y romper context paths, proxys
  o generar rutas inseguras.
- [TP1] Usar `$(pageContext.request.contextPath)` o variantes incorrectas en vez
  de `<c:url>`.
- [TP1/TP2] No escapar `%` y `_` en busquedas `LIKE`.
- [TP1] Manejar mal busquedas con `%%%` o entradas tipo `" OR 1=1`.
- [TP1/TP2] Doble slash en rutas o busquedas que termina en 400.
- [TP1/TP2] App base URL para mails calculada desde `HttpServletRequest` en vez
  de una property confiable.

Chequeo de fase:

- Revisar JSPs, tags y fragments donde aparezcan datos de usuario.
- Probar search con `%`, `_`, comillas, HTML y payloads XSS simples.
- Confirmar que URLs internas usan `c:url` y mails usan base URL configurada.

## Forms, validaciones y contratos de entrada

Errores combinados:

- [TP1/TP2] Validaciones manuales en controller/service en vez de constraints,
  form objects y custom validators.
- [TP1/TP2] Falta de validacion backend para email, telefonos, URLs, links,
  fechas, campos numericos, archivos y comprobantes.
- [TP2] Inputs numericos que aceptan negativos cuando el dominio no los admite.
- [TP2] Mensajes de maxlength sin limite, limites invertidos o mensajes poco
  utiles.
- [TP2] Campos obligatorios no marcados consistentemente.
- [TP2] Form que pierde datos ante errores, como fechas, tags o filtros.
- [TP2] Depender de `session.getAttribute(...)` para datos de pasos previos en
  vez de usar hidden inputs validados u otro contrato explicito.
- [TP2] `MultipartFile image_content` o nombres similares que no respetan Java
  style y ademas no validan tipo/tamano.
- [TP1] Validaciones para formularios en controllers que deberian ser
  constraint validators.
- [TP1] Excepciones de validacion de servicios que obligan a exception handling
  feo en controllers porque faltan validators.
- [TP1/TP2] Falta de confirmar contrasena o de pedir contrasena actual al
  cambiar password.
- [TP1/TP2] Flujos sin recuperacion de contrasena, verificacion de email o
  ingreso claro del codigo/token cuando aplica.

Chequeo de fase:

- Revisar forms por contrato: restricciones, mensajes, preservacion de input y
  validacion server-side.
- Probar inputs borde, archivos invalidos, numeros negativos y campos vacios.

## Testing unitario, blackbox y comportamiento observable

Errores combinados:

- [Apuntes/TP1/TP2] Tests sin estructura clara de setup, ejercitacion y
  validacion.
- [Apuntes/TP1/TP2] Tests que ejercitan varias llamadas principales y mezclan
  multiples conductas en un mismo caso.
- [TP1/TP2] Usar `Mockito.verify()`, `Mockito.never()` o verificaciones de
  interacciones como objetivo central del test.
- [TP1] Encubrir `verify` con `doAnswer`, `AtomicReference`, `AtomicBoolean` o
  capturas de argumentos.
- [TP1/TP2] Tests no unitarios que llaman multiples metodos de la clase bajo
  test o ejercitan varios DAOs/services en un mismo caso.
- [TP1/TP2] Tests de persistence que no verifican estado final de DB.
- [TP1/TP2] Tests de persistence que preparan o verifican con el mismo DAO bajo
  test.
- [TP2] Tests JPA sin `em.flush()` cuando la validacion real depende de llegar a
  DB.
- [TP2] `assertTrue(true)` o "no exploto" como test.
- [TP1/TP2] Tests vacios o cobertura nula/comentada en services complejos.
- [TP1/TP2] Solo happy paths, sin casos no felices, errores ni bordes.
- [TP2] Query builders, filtros y DAOs complejos sin tests de combinaciones y
  precedencia.
- [TP1] Tests de views que leen JSP/HTML/Thymeleaf y buscan strings exactos como
  objetivo principal.
- [TP1] Tests con reflection o acoplados a metodos privados, estructura interna
  o strings de implementacion.
- [TP1] Mezclar JUnit 3, JUnit 4 y JUnit 5 sin necesidad.
- [TP1] Tests enormes con setups custom de cientos o miles de lineas.
- [Apuntes/TP1/TP2] Tests con demasiadas postcondiciones no relacionadas, donde
  falla una cosa y no queda claro que conducta se estaba probando.
- [TP1] No usar `@Rollback` en tests de DAO cuando hace falta aislar estado.
- [TP1] Baja cobertura de services con logica de negocio grande.

Chequeo de fase:

- Cada test debe probar contrato observable: retorno, estado final, DB final,
  status/redirect/modelo publico, salida visible o evento.
- Mantener una ejercitacion principal por test cuando sea posible.
- Eliminar o reescribir tests whitebox antes de usarlos como evidencia.
- Para persistence, verificar con DB final, `flush`/`clear` o fixtures
  controladas segun corresponda.

## Transacciones, consistencia y async

Errores combinados:

- [TP1/TP2] Metodos publicos de service sin borde `@Transactional` acorde.
- [TP1/TP2] Lecturas sin `@Transactional(readOnly = true)`.
- [TP1/TP2] Inserts, deletes o updates sin transaccion.
- [TP1] Usar `@Transactional` solo en pocos metodos o no usarlo en ningun lugar.
- [Apuntes/TP1/TP2] Anotar metodos privados esperando que el proxy los
  intercepte.
- [Apuntes/TP1/TP2] Llamar helpers anotados mediante `this.helper()` y asumir
  que AOP/`@Transactional` va a aplicar.
- [Apuntes/TP1/TP2] No exponer en la interfaz el metodo publico que deberia ser
  punto de entrada transaccional cuando se usan proxies por interfaz.
- [Apuntes/TP1/TP2] Falta de `@EnableTransactionManagement` o transaction
  manager coherente con JDBC/JPA.
- [TP2] Combinar `@Async`, `@Transactional` y lazy relations sin diseno
  explicito.
- [TP1] Anotar servicios sin DB como `EmailServiceImpl` con `@Transactional`
  aunque sus metodos sean async y no usen persistencia.
- [TP2] Cron o job con loop infinito que envia mails infinitos.
- [TP1/TP2] Acciones con side effects que luego muestran 500, dejando al usuario
  sin saber si la accion ocurrio.

Chequeo de fase:

- Revisar services por metodo publico: lectura, escritura, async o sin DB.
- Probar rollback y consistencia en flujos que modifican varias entidades.
- Buscar anotaciones en privados, self-invocation y bordes transaccionales que
  no coinciden con el caso de uso real.

## Manejo de errores, excepciones y logging

Errores combinados:

- [TP1/TP2] Usar `RuntimeException`, `IllegalArgumentException` o
  `NoSuchElementException` genericas para errores de dominio.
- [TP1/TP2] Usar `Optional.get()` sin verificacion previa.
- [TP1/TP2] Mezclar `null`, `Optional.empty`, listas vacias, ids `-1` o
  excepciones para expresar inexistencia sin contrato uniforme.
- [TP1/TP2] Controllers con `try/catch` para resolver errores en vez de
  `ControllerAdvice` o exception handlers.
- [TP2] Catchear `NullPointerException` en vez de chequear null.
- [TP2] Handlers que reciben o loguean un tipo de excepcion incorrecto por
  copy-paste.
- [TP2] `MissingServletRequestParameterException` o query params invalidos que
  terminan en 500.
- [TP1] `ExpiredPassTokenException` u otras excepciones con campos mutables sin
  necesidad.
- [TP1] Arrojar `RuntimeException` al no encontrar user cuando corresponde un
  NotFound custom o contrato equivalente.
- [TP1/TP2] Falta de logs en `ControllerAdvice` o services para acciones no
  triviales.
- [TP1/TP2] Logger obtenido desde una clase ajena por copy-paste.
- [TP1] `printStackTrace()` o logs a STDOUT en vez de logger.
- [TP1] Loguear a DEBUG/INFO inapropiado en produccion o sin configuracion de
  logback.

Chequeo de fase:

- Mapear excepciones de dominio a respuestas coherentes.
- Confirmar que no hay 500 por input invalido esperable.
- Revisar logging de produccion y test.

## Modelado de dominio, tipos, Optional y enums

Errores combinados:

- [TP1/TP2] Usar boxed primitives (`Long`, `Boolean`) sin que `null` sea parte
  del dominio.
- [TP1/TP2] Auto-unboxing susceptible a `NullPointerException`.
- [TP1/TP2] Usar `Optional` como field, como contrato de create que nunca es
  empty, o con `get()` sin check.
- [TP1/TP2] Devolver `0`, `null`, lista vacia, `Optional.empty` o id `-1` para
  esconder errores reales.
- [TP1/TP2] Servicios `create` inconsistentes: algunos devuelven entidad, otros
  id, otros `void` u `Optional`.
- [TP1/TP2] Usar strings para roles, dias, estados, tabs, ordenamientos,
  categorias, provincias o conceptos con enum claro.
- [TP1] No usar enums ya existentes, por ejemplo guardar status como `String`.
- [TP1/TP2] Omitir modificadores de acceso sin diseno explicito.
- [TP1/TP2] Clases utilitarias no `final` o instanciables.
- [TP1] Override de `equals()` sin `hashCode()`.
- [TP1] Modelos en plural, snake_case o nombres fuera de convencion Java.
- [TP1] Wrappers que no son modelos reales sino bolsas de varios modelos.
- [TP1/TP2] DTOs o entidades parciales creadas fuera de persistence sin
  invariantes claras.

Chequeo de fase:

- Revisar tipos publicos y contratos de services/DAOs.
- Reemplazar magic strings por enums cuando el conjunto de valores sea cerrado.
- Uniformar contratos de inexistencia y creacion.

## Internacionalizacion, encoding y texto visible

Errores combinados:

- [TP1/TP2] Textos visibles hardcodeados en JSPs, tags, controllers,
  aria-labels, emails y mensajes de error.
- [TP1/TP2] Keys visibles en frontend como `email.duplicate` o
  `invite.friends.send`.
- [TP2] `lang="es"` hardcodeado cuando la app soporta idiomas.
- [TP2] Monedas, enums, tags, areas, periodicidad, asuntos de email, alt texts e
  instrucciones sin internacionalizar.
- [TP1/TP2] Concatenar textos en vistas en vez de usar mensajes interpolados.
- [TP1/TP2] Pluralizacion rota, por ejemplo "1 reseñas".
- [TP1/TP2] Encoding roto con caracteres como `â`, `Ã`, tildes o `ñ` mal
  renderizados.
- [TP2] Cambiar idioma del browser no afecta la app si ese era el resolver
  elegido, o se usa `SessionLocaleResolver` sin justificacion.
- [TP1/TP2] Locale persistido o resuelto manualmente por query param sin UX
  clara.
- [TP1] Hacer un GET a `/` que actualiza el locale del usuario en DB.
- [TP1/TP2] Mails enviados con locale del sender o `LocaleContextHolder` en vez
  del locale/preferredLanguage del destinatario.
- [TP1/TP2] Alt texts presentes pero no localizados.

Chequeo de fase:

- Buscar texto hardcodeado en JSP/tag/controller/mail.
- Probar cambio de idioma y revisar mails con destinatarios de distinto locale.
- Revisar encoding en browser, no solo en archivos.

## Mailing, notificaciones y comunicacion

Errores combinados:

- [TP1/TP2] Mails sin header, CTA claro, datos completos o tono consistente con
  la app.
- [TP1/TP2] Links de mail rotos, a `localhost`, mal formados o sin base URL
  configurada.
- [TP2] Mails de reserva o pago sin detalle de reserva, monto, datos de pago o
  aclaracion de estado.
- [TP1] Mail pidiendo pago sin CBU/datos de pago incorporados en HTML.
- [TP1/TP2] Eventos relevantes sin notificacion al usuario afectado: reserva
  cancelada, review borrada, ban, aceptacion/rechazo, torneo ganado, cambio de
  reserva o announcement.
- [TP2] Mails duplicados que podrian combinar onboarding y creacion de cuenta.
- [TP1/TP2] Email service acoplado a templates o paths de webapp.
- [TP1/TP2] Otros services armando manualmente subject, cuerpo y variables de
  mail en vez de delegar el caso de uso a mailing.
- [TP1] Loop de envio de emails en un service sin aislar proceso asincronico.
- [TP1/TP2] Interfaz `AsyncMailService` separada solo por detalle de
  implementacion async.
- [TP2] Mail de validacion sin link directo, obligando a copiar/pegar codigo.

Chequeo de fase:

- Recorrer cada evento de negocio que afecta a otro usuario.
- Verificar idioma, link, datos accionables y destinatario correcto.

## Manejo de imagenes y archivos

Errores combinados:

- [Apuntes/TP1/TP2] Mezclar metadata de dominio con bytes de imagen/archivo en
  una misma entidad cuando corresponde separar entidad y recurso binario.
- [Apuntes/TP1] No modelar imagenes con tabla/recurso propio, por ejemplo
  `image(id, content bytea)` y FK desde la entidad dueña cuando aplica.
- [Apuntes/TP1/TP2] Servir imagenes siempre embebidas en entidades/listados en
  vez de endpoint dedicado con `Content-Type` correcto.
- [TP2] Mapear relaciones JPA que arrastran bytes completos de imagen en cada
  listado o card.
- [TP1/TP2] Validar upload solo por nombre/extensión declarada sin verificar
  contenido/tipo real y tamano.
- [TP1/TP2] Hacer manejo de imagenes en JSP/controller en vez de delegar a
  service/persistence segun corresponda.
- [TP1/TP2] Cachear imagenes con `ConcurrentMapCacheManager` sin eviction, con
  riesgo de memoria.
- [Apuntes/TP final] Si existiera API/SPA, devolver siempre original pesado en
  vez de variantes o miniaturas coherentes con el cliente.

Chequeo de fase:

- Buscar `MultipartFile`, endpoints de imagen, tablas/entidades de imagen y
  relaciones JPA hacia bytes.
- Confirmar validacion de tipo/tamano y respuesta con content type correcto.
- Revisar listados para asegurar que no cargan bytes de imagen innecesarios.

## UX, navegacion y flujos principales

Errores combinados:

- [TP1/TP2] Login/register sin forma clara de volver a home o al flujo iniciado.
- [TP1/TP2] Despues de registro, reset o login interceptado, mandar siempre a
  home en vez de continuar la accion esperada.
- [TP2] Botones "volver" basados en `history.back()` que pueden dejar al usuario
  atrapado o reenviar POST.
- [TP1/TP2] CTAs core escondidos o ausentes: reservar, crear, asistir, cargar
  comprobante, administrar, historial.
- [TP1/TP2] Empty states sin CTA ni mensaje claro.
- [TP2] Links compartidos que conservan query params transitorios, como
  `commentPublished=true`.
- [TP2] Search que borra filtros sin hacerlo visible.
- [TP2] Tags/chips visualmente clickeables que no funcionan como filtros.
- [TP1/TP2] Faltan filtros esperables por dominio: estado, precio, rating,
  ubicacion, amenities, tipo, torneo terminado, notificacion, etc.
- [TP1/TP2] Explore o pantallas de descubrimiento demasiado simples para el core
  del producto.
- [TP1/TP2] Paginacion visual inconsistente: cantidad por fila, total por
  pagina, hijos de comentarios, listas secundarias o page size ignorado.
- [TP1/TP2] Acciones destructivas sin confirmacion.
- [TP2] Doble click en acciones importantes sin loading/disable.
- [TP2] Modales demasiado chicos, apretados, con pasos innecesarios o que se
  rompen al abrirlos de nuevo.
- [TP1/TP2] Elementos con hover o estilo de link que no son clickeables.
- [TP1/TP2] Botones, iconos, cards, dropdowns o popups mal alineados o fuera de
  contenedor.
- [TP2] Contraste insuficiente o scroll raro en secciones pequenas.
- [TP1/TP2] Imagenes pixeladas, carruseles sin limite o vistas sobrecargadas de
  texto.
- [TP1/TP2] Estados imposibles, tabs inaccesibles para anonimos, botones rotos o
  previews que no funcionan.
- [TP1/TP2] No tener favicon o ancho de contenido inconsistente.

Chequeo de fase:

- Recorrer flujos core como usuario anonimo, autenticado, owner/admin y usuario
  sin permisos.
- Validar empty states, redirects, back navigation, errores y confirmaciones.

## Maven, configuracion, build y repo hygiene

Errores combinados:

- [TP1/TP2] Versiones de dependencias o plugins redeclaradas en POM hijos en vez
  de heredarlas del padre.
- [TP1/TP2] Modulos hijos declarando `<version>` innecesario.
- [TP1/TP2] Java version distinta en un modulo respecto del padre.
- [TP1/TP2] Dependencias con scope incorrecto: `hsqldb` en compile,
  `javax.servlet-api` sin provided, Mockito sin test, `spring-jdbc` o
  `spring-web` donde no corresponde.
- [TP1] Agregar Spring Boot cuando la materia espera librerias Spring canonicas
  sin Boot.
- [TP1/TP2] Dependencias duplicadas, como validation-api.
- [TP1/TP2] Archivos generados, IDE o pesados trackeados: `.iml`, `.vscode`,
  `.mvn` vacia, `jvm.config`, `maven.config`, `target`, `webapp.war`,
  `out/artifacts`, `bin`, backups de Vim, fatjars, fonts innecesarias,
  `.PVS-Studio`.
- [TP1/TP2] Credenciales, passwords, keys o configs reales versionadas.
- [TP2] README con credenciales rotas o sin usuario admin operativo.
- [TP1] Logback agregado como dependencia pero sin configuracion efectiva.
- [TP1/TP2] `CharacterEncodingFilter` declarado como bean inutil si ya esta en
  `web.xml`, o ausencia/mala configuracion UTF-8.
- [TP1] Escaneo multiple e innecesario de paquetes Spring.
- [TP2] `taskExecutor.queueCapacity = 25` u otros limites arbitrarios sin
  justificacion.
- [TP1/TP2] `ConcurrentMapCacheManager` para imagenes sin eviction, con riesgo
  de memoria.
- [TP1/TP2] Repo pesado que dificulta Git, build o evaluacion.

Chequeo de fase:

- Revisar `git status`, archivos trackeados, POM padre/hijos, scopes y build
  reproducible.
- Confirmar que configs sensibles se documentan como placeholders, no como
  secretos reales.

## Seguimiento, Scrum, entrega y defensa

Errores combinados:

- [TP2] Treatar una paginacion nueva como chore cuando agrega valor funcional y
  deberia ser feature.
- [TP2] Story generica tipo "Correcciones demo entrega" sin descomponer en
  tareas accionables.
- [TP2] Cards en `In Progress` o `Todo` al cierre.
- [TP2] Commits sin sentido como `"aaaaaa"`.
- [TP2] TODOs en codigo, especialmente comentarios que indiquen que lo hizo una
  IA y el equipo no entiende que hace.
- [TP2] Entrega tardia o deploy durante instancia evaluativa.
- [TP1/TP2] No poder explicar como funciona la app o los cambios introducidos.
- [TP2] No revisar delta contra la entrega anterior: TP2 no es solo migrar ORM,
  tambien debe mostrar valor y correcciones reales.

Chequeo de fase:

- Revisar tablero, commits, TODOs, README y recorrido de demo.
- Preparar explicacion de las decisiones tecnicas relevantes, especialmente JPA,
  paginacion, seguridad y tests.

## Buenas practicas historicamente elogiadas

No son errores, pero sirven como criterio de cierre:

- Paginacion bien implementada con filtros y query params visibles.
- Custom validators claros y reutilizables.
- Buen uso de enums, custom exceptions y builder pattern.
- Buen mapeo Hibernate con relaciones correctas.
- Uso de `@Formula` cuando evita persistir datos derivados.
- Tests con buena cobertura y comportamiento observable.
- Plane/Scrum con features, chores, bugs, estimaciones y seguimiento visible.
- Confirmaciones para borrar o ejecutar acciones destructivas.
- Feedback claro al navegar, zero states con CTA y flujos sin dead ends.
- Mails con localizacion, CTA y datos completos.
- Dashboards, reportes o admin con informacion accionable.

## Ver tambien

- [[resumen-correcciones-tp1-2026-c1]]
- [[resumen-correcciones-tp2]]
- [[criterios-evaluacion]]
- [[tp1-vs-tpe2-final]]
- [[resumen-apuntes]]
- [[http-y-sesiones]]
- [[jsp-jstl]]
- [[manejo-imagenes]]
- [[paw-unidad-04-spring-mvc-capas-jdbc]]
- [[modelo-capas]]
- [[comparacion-capas-web-services-persistence]]
- [[n-plus-1-joins-java]]
- [[hibernate-jpa]]
- [[spring-security]]
- [[spring-aop]]
- [[logging]]
- [[testing-unitario]]
- [[logica-en-controllers]]
- [[validacion-formularios]]
- [[internacionalizacion]]
- [[mailing]]
- [[ux-flows]]
