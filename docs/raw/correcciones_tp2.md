# PAW AI Categorized Instructions & Knowledge Base TP2 (Full Original Data)

A continuacion se presenta la recopilacion estructurada de errores, falencias, virtudes y criterios de correccion observados en devoluciones de **TP2**. El objetivo es que otra IA, desarrollador o auditor pueda absorber el criterio de la catedra sin tener que recorrer grupo por grupo.

Fuentes primarias:

- `docs/raw/pdfs/Devolucion_TP2_2025_C1.pdf`
- `docs/raw/pdfs/Devolucion_TP2_2025_C2.pdf`

Cobertura de la fuente:

- `Devolucion_TP2_2025_C1.pdf`: 15 grupos.
- `Devolucion_TP2_2025_C2.pdf`: 17 grupos.
- Total consolidado: 32 devoluciones.

---

## [CRITICO] ERRORES MAS FRECUENTES EN TP2

Estos patrones aparecen repetidos o con alto impacto conceptual en las devoluciones. La frecuencia es orientativa: agrupa menciones equivalentes aunque cambie el dominio de la app.

- **[Muy recurrente] No usar el modelo 1+1 de paginacion**: paginan directo sobre entidades con relaciones, traen colecciones completas, usan `EAGER`, o hacen paginacion en memoria. En TP2 esto se vuelve mas grave porque Hibernate puede multiplicar queries o traer grafos completos.
- **[Muy recurrente] Logica de negocio en controllers**: controllers que buscan entidades, validan ownership, hacen updates parciales, deciden flujos de negocio o arman DTOs con multiples llamadas a services.
- **[Muy recurrente] Tests de baja calidad**: tests no unitarios, uso de `Mockito.verify()` para validar implementacion, tests de persistencia sin verificar estado final de DB, falta de `flush()`, tests vacios y baja cobertura de servicios complejos.
- **[Muy recurrente] Control de acceso insuficiente o manual**: ownership validado en controller/service, rutas solo autenticadas pero no protegidas por recurso, links visibles hacia acciones prohibidas, o falta de uso fino de Spring Security.
- **[Muy recurrente] Relaciones JPA mal modeladas**: entidades que guardan ids pelados en vez de relaciones, ausencia de foreign keys, `FetchType.EAGER` innecesario, colecciones `toMany` sin paginar, uso incorrecto de `JOIN FETCH`, y lazy loading usado desde contextos async.
- **[Muy recurrente] Falta de i18n y problemas de encoding**: textos en ingles dentro de paginas en espanol, keys visibles, `lang="es"` hardcodeado, mails siempre en un idioma, enums/tags/monedas sin traducir y caracteres rotos.
- **[Recurrente] XSS y salida sin escape**: faltan `<c:out>` o `c:url`, se imprimen campos de usuario en JSP, hay campos textuales susceptibles a XSS y algunos intentos de XSS terminan con efectos parciales.
- **[Recurrente] Forms y validaciones en capa incorrecta**: validaciones manuales en controller/service, falta de custom validators, errores de maxlength sin parametro, inputs numericos que aceptan negativos, archivos sin validar tipo/tamano.
- **[Recurrente] Manejo pobre de excepciones**: `IllegalArgumentException`, `NoSuchElementException`, `Optional.get()`, `try/catch` en controllers, `NullPointerException` catcheado y handlers que devuelven errores o logs incorrectos.
- **[Recurrente] Uso incorrecto de `@Transactional`**: metodos publicos de service sin `@Transactional(readOnly = true)`, inserts/deletes sin transaccion, combinacion `@Async` + relaciones lazy sin materializacion.
- **[Recurrente] Repo y Maven sucios**: `schema.sql` en `webapp`, `hsqldb` en `compile`, versiones repetidas en poms hijos, archivos `.iml`, `.mvn`, `out/artifacts`, backups de Vim, fonts vendoreadas y fatjars trackeados.
- **[Recurrente] Flujos de mail incompletos**: links rotos o a `localhost`, mails sin CTA o sin datos clave, idioma incorrecto, duplicacion de mails, asuntos en ingles, y falta de notificacion ante cambios relevantes.
- **[Recurrente] Optional, null y boxing mal usados**: `Optional.get()` sin check, `Optional` usado como contrato de create cuando nunca es empty, `Long`/`Boolean` boxed sin necesidad, auto-unboxing susceptible a NPE, ids `-1` o `Optional.empty` mezclados.
- **[Recurrente] Navegacion y UX de flujo core confusas**: botones de back basados en history, CTAs escondidos, estados sin explicacion, links hacia 403/404, acciones destructivas sin confirmacion y elementos que parecen clickeables pero no lo son.

---

## Seguridad y control de acceso

### Ownership y autorizacion por recurso

- No alcanza con verificar que un usuario este autenticado o tenga un rol general. TP2 sigue penalizando fuerte no validar ownership del recurso.
- Se detectaron casos donde se podia:
  - acceder o modificar tareas de otros usuarios;
  - cargar o editar camiones de otros fleteros;
  - asignar camiones de otras empresas a drivers propios;
  - editar vinilos ajenos desde URL directa;
  - acceder a `/ban` de un restaurante sin ser mod/admin;
  - aceptar requests de cualquier viaje siendo conductor;
  - unirse unilateralmente a cualquier equipo y ver sus reservas/miembros;
  - cargar acciones de admin o user-management desde vistas no apropiadas.
- Las rutas de suscripcion/desuscripcion no deben protegerse con `if (loggedUser != null)`; eso corresponde a Spring Security.
- Si la regla depende de path params, Spring Security ya permite parsearlos y pasarlos a funciones de autorizacion. No conviene parsear strings manualmente en controllers.
- Los chequeos como `if (user == null) throw new UnauthorizedException(...)` son validaciones de seguridad y deben quedar en Spring Security.
- Usar `@PreAuthorize`, `.access(...)`, voters o helpers de seguridad declarativa para reglas de recurso. Evitar repartir reglas entre controllers, services y JSP.
- No conviene poner `@PreAuthorize` en services si el contrato acordado del proyecto es que seguridad de rutas y ownership viva en la capa web/security; mantener una estrategia consistente.
- Jerarquias complejas de roles pueden modelarse mejor con `AccessDecisionVoter` o mecanismos de autorizacion de Spring, no agregando authorities escalonadas manualmente.
- Si un link visible lleva a un `403`, el flujo esta mal disenado: o el usuario no deberia ver ese link o deberia haber una vista/accion acorde a sus permisos.

### XSS, URLs y salida HTML

- Imprimir valores en JSP con `${...}` sin `<c:out>` sigue siendo una vulnerabilidad grave.
- Faltaron escapes en selectores, campos como coverage/plan, opciones de edificio y otros textos libres.
- La falta de `c:url` puede generar XSS o rutas mal formadas; tambien evita que la app funcione bien detras de context paths/proxys.
- Un intento de XSS en reviews devolvia `400` pero igual generaba la review: no alcanza con mostrar error si se persiste el dato.
- Un intento de XSS en inscripcion a evento dejaba estado "pendiente" y luego no era visible en perfil, combinando seguridad con inconsistencia de flujo.
- Los campos de texto libre deben validarse y escaparse en salida. No depender solo de patrones de formularios.

### Sesion y flujos de cuenta

- El `LoginController` no deberia manejar manualmente sesion HTTP si Spring Security ya resuelve autenticacion.
- La recuperacion/cambio de contrasena deberia dejar al usuario logueado cuando el flujo lo permite, o explicar claramente el siguiente paso.
- Faltaron flujos de recuperacion de password, verificacion de email o cambio de contrasena en algunos grupos.
- La pantalla de verificacion no puede solo reenviar codigo si no ofrece una forma clara de ingresarlo.
- El mail de validacion deberia tener link directo de validacion, no exigir copiar y pegar codigo manualmente.

---

## Arquitectura, MVC y responsabilidades

### Controllers con logica de negocio

- La catedra vuelve a marcar como error conceptual grave que haya logica de negocio en controllers.
- Ejemplos detectados:
  - controllers que llaman varias veces a `menuService.addItem` y chequean reglas de menu;
  - `editMenu` haciendo `findById` y validaciones antes de editar;
  - controllers que obtienen `Reservation` y `ReservationTurnInstance` antes de llamar a `modifyReservation`;
  - `editRestaurant` con multiples updates separados;
  - `edit club` que obtiene entidad, decide campo por campo y vuelve a llamar al service;
  - `OrgCreateController` creando organizacion y asignando manager;
  - `OpenPositionManagementController` rechazando/reabriendo aplicaciones ante cambios;
  - `JobApplicationController` llamando metodos de decision (`canCancelApplication`, `canAcceptApplication`) individualmente en vez de recibir un DTO ya resuelto;
  - `declineApp` buscando primero la application y luego pasandole el id al mismo servicio.
- El controller deberia invocar un caso de uso unico y dejar al service la transaccion, validaciones de negocio y consistencia.
- Si un flujo necesita muchas llamadas a services para una unica accion, probablemente falta un metodo de caso de uso.

### Servicios acoplados a vistas

- `HomeDisplayService`, `HomeService` o funciones como `getHomeModel` que populan `Map<String, Object>` mezclan frontend con dominio.
- El concepto de "home page" pertenece a `webapp`; los services deben modelar casos de uso y dominio, no pantallas.
- `ManagementData` como contenedor de todos los modelos requeridos por `/management` acopla el service a que muestra una vista.
- Servicios que reciben o devuelven estructuras pensadas para JSP/ModelAndView son fragiles: si se rediseña la pantalla, no deberia cambiar el service por ese motivo.

### Forms, validators y contratos de entrada

- Validar forms a mano en controller sigue penalizado. Usar Bean Validation y custom validators.
- Validaciones de email recibidas como request params deben hacerse en backend, no confiar en el frontend.
- `MultipartFile image_content` deberia validar tipo y tamano; ademas, usar nombres Java como `imageContent`.
- Para comprobantes, imagenes, recibos, links, telefonos y campos numericos, el form debe expresar restricciones claras y mensajes localizados.
- Inputs numericos no deberian aceptar negativos si el dominio no los admite.
- Errores de longitud deben parametrizar minimo/maximo en el mensaje y no invertirlos.
- Si un dato viene de un paso previo, preferir pasarlo por el form (por ejemplo hidden input validado) antes que depender implicitamente de `session.getAttribute(...)`.

### Modelos, DTOs y tipos

- En TP2 se espera aprovechar Hibernate: si una entidad representa una invitacion, reserva, club, usuario o relacion, no pedir cada campo por separado y revalidarlo manualmente como si fueran columnas sueltas.
- Los ids pasan a ser detalle de implementacion cuando hay relaciones JPA bien modeladas.
- Hay contratos pobres cuando:
  - DAOs `create` devuelven `Optional` pero nunca pueden devolver empty;
  - metodos de persistencia que crean entidad devuelven solo el id en vez de la entidad creada;
  - funciones `join`/`leave` devuelven `int` 0/1 en vez de boolean o resultado de dominio;
  - servicios retornan `0`, lista vacia o `null` para esconder errores reales.
- Evitar `Optional.get()` sin check, `orElseThrow(NoSuchElementException)` generico, ids `-1`, y mezclas de `Optional.empty` con centinelas.
- Evitar boxed primitives (`Long`, `Boolean`) si `null` no es parte del dominio. El auto-unboxing puede disparar NPE.
- Usar enums para `role`, `day`, `status`, criterios de orden, request params y valores de dominio. Evitar convertir enums a strings proactivamente.
- Evitar fields `String` para conceptos que ya tienen enum, como `Location`, `Province` o `Category`.
- Clases utilitarias deben ser `final` y no instanciables con constructor privado.
- Omitir modificadores de acceso sin diseno explicito sigue apareciendo como mala practica.
- Evitar `java.sql` en modelos; la base de datos es detalle de persistencia.

### Mailing como caso de uso

- El servicio de email debe encapsular sujeto, template y variables; otros servicios no deberian armar manualmente 10 lineas de mail.
- Enviar DTOs artificiales al servicio de mailing puede ser innecesario si el service puede recibir modelos y decidir que datos necesita.
- El locale del mail debe ser el del destinatario, no el del usuario activo (`LocaleContextHolder`) ni el del sender.
- Mails de negocio deberian incluir los datos necesarios para actuar: detalles de reserva, pago, monto, titulo de notificacion, CTA claro y link correcto.

---

## Persistencia JPA/Hibernate, DB y performance

### Modelo 1+1 y paginacion

- El modelo 1+1 es una expectativa central de TP2 cuando se paginan entidades con relaciones: primero obtener ids de la pagina y luego cargar el grafo necesario.
- No usar 1+1 fue marcado repetidamente como error conceptual grave, incluso cuando hoy no haya relaciones `EAGER`; el diseno queda propenso a romperse si cambia un mapping.
- No basta con "tener paginacion" si:
  - la query trae toda la tabla y pagina en memoria;
  - se cargan colecciones completas para mostrar una pagina;
  - se hacen N queries por cada fila;
  - se pagina sobre un `JOIN FETCH` que duplica filas;
  - no se usa `distinct` al obtener ids en queries 1+1;
  - `page` o `size` negativos terminan en 500;
  - pedir una pagina mayor al maximo muestra "Pagina 55 / 3" en vez de clamplear o redirigir.
- Colecciones como comentarios, hijos de comentarios, friends, likes, suscriptores, enrollments, participantes, equipos, reservas, estudios, turnos, doctores autorizados o notificaciones deben paginarse si pueden crecer.
- Endpoints auxiliares como `/books/authors` no pueden traer listas de tamano desconocido ni fallar con 500 si falta un query param.

### Relaciones JPA y fetch

- Mapear relaciones como ids pelados rompe el modelo de dominio y desaprovecha JPA.
- Las relaciones deberian tener semantica real: entidades relacionadas, foreign keys y constraints cuando corresponde.
- `FetchType.EAGER` en `ManyToOne` o `toMany` fue criticado muchas veces. Aunque la cardinalidad parezca baja, puede explotar al listar muchos registros.
- Relacion `toMany` sin `LAZY` puede traer cantidades grandes a memoria sin paginar.
- Imagenes o archivos no deberian arrastrar bytes completos por relaciones si se acceden por endpoint separado.
- Un llamado a `.size()` sobre coleccion lazy materializa toda la coleccion; si solo se necesita cantidad, usar `COUNT`.
- `.clear()`, `.add()` y `.remove()` sobre colecciones managed pueden materializar listas completas para cambios simples.
- `JOIN FETCH` no es solucion universal: mal usado puede multiplicar filas y traer combinaciones enormes (por ejemplo fletes x conductores x transportes).
- En contexto `@Async`, acceder relaciones lazy sin materializar antes puede fallar. Se debe forzar carga explicita dentro de la transaccion o usar fetch joins controlados en DAO.
- `@Formula` fue elogiado para evitar persistir contadores derivados o mapeos innecesarios de queries frecuentes.

### Queries y SQL generado

- TP2 exige revisar el SQL generado, no solo que compile.
- Se penaliza filtrar y agregar en Java cuando debe hacerlo la base:
  - filtros de turnos activos;
  - disponibilidad de doctores;
  - cantidad de matches del ultimo mes;
  - agregaciones o rankings;
  - busquedas por genero/equipo/participantes.
- Queries nativas pueden ser razonables, pero si Hibernate/HQL resuelve el caso de forma mas expresiva, conviene aprovecharlo.
- Query builders complejos con parametros compartidos requieren tests fuertes; si son privados y solo se testean indirectamente, aumenta la fragilidad.
- Una query con precedencia incorrecta (`A AND B AND C OR D`) puede traer datos invalidos aunque parezca pasar casos simples.
- Si un metodo se llama `findAll`, igual necesita paginacion cuando la cardinalidad no esta acotada.
- Evitar `em.unwrap(Session.class)` y `addScalar` sin motivo claro.

### Transacciones

- Todos los metodos publicos de service relevantes deben tener borde transaccional acorde.
- Lecturas deberian usar `@Transactional(readOnly = true)`.
- Inserts, deletes y updates no deben quedar sin `@Transactional`.
- La combinacion `@Async` + `@Transactional` + lazy relations requiere diseno explicito; no asumir que el contexto se mantiene.
- Un cron con loop infinito que envia infinitos mails es error grave y muestra falta de tests/guardas.

---

## Manejo de errores, excepciones y logging

- Usar `IllegalArgumentException`, `NoSuchElementException` o `RuntimeException` genericos oculta el contrato real del dominio.
- `NoSuchElementException` es de estructuras de datos; no deberia representar "usuario no existe" o "recurso no encontrado" en negocio.
- `Optional.get()` sin verificacion previa fue marcado como problema recurrente.
- Catchear `NullPointerException` en `resolveLocale` en vez de chequear null proactivamente es mala practica.
- Controllers no deberian hacer `try/catch` para resolver errores: usar `ControllerAdvice` / exception handlers.
- El handler no debe loggear o tipar excepciones incorrectas, por ejemplo manejar `DataAccessException`/`BusinessException` pero recibir `ImagePersistenceException` como parametro.
- Faltan logs en `ControllerAdvice` y en service para acciones no triviales.
- No obtener logger de una clase ajena por copy-paste.
- `MissingServletRequestParameterException` y errores de query params deben mapearse a respuestas amigables, no 500.
- Acciones que realizan side effects y luego muestran 500 (por ejemplo unsuspend) son especialmente peligrosas: el usuario no sabe si la accion ocurrio.
- README con credenciales rotas o sin usuario admin falla la operacion y la evaluacion.

---

## Infraestructura, Maven, configuracion y repo hygiene

- `schema.sql` debe estar en el modulo de persistence, no en `webapp`.
- Dependencias de test como `hsqldb` no deben quedar en `compile`.
- Evitar definir versiones en poms hijos si se heredan del padre.
- Evitar setear Java 11/15 en plugins de modulos si el padre define la version correcta.
- Propiedades referenciadas como `security.rememberme.key` deben existir en configuracion.
- No usar `webapp_base_url` en JSP si `<c:url>` resuelve el context path.
- El repo no debe incluir `.iml`, `.mvn` generada, `out/artifacts`, backups `*.*~`, fatjars, fonts vendoreadas innecesarias, ni directorios de herramientas descargables.
- No falta solo "limpieza": repos pesados hacen lenta la operacion y complican el trabajo con Git.
- Si se trackea un fatjar de `google-java-format`, preferir plugin de Maven o script que descargue en directorio ignorado.
- Si se usan fonts publicas como Fira Sans, no incluir 20 variantes pesadas si se pueden cargar de un CDN o empaquetar solo las necesarias.
- `CharacterEncodingFilter` no debe declararse como bean inutil si ya esta configurado en `web.xml`.
- La ausencia de filtro UTF-8 o mal encoding visible se considera problema de calidad.
- `taskExecutor.queueCapacity = 25` puede limitar arbitrariamente tareas `@Async`; justificar o ajustar.
- `ConcurrentMapCacheManager` para imagenes sin eviction puede crecer sin limite y provocar problemas de memoria.
- Debe existir configuracion de logging de test si el proyecto la requiere; no depender solo de produccion.

---

## UI, UX y flujos vitales

### Navegacion, CTAs y estados

- Botones de "volver" no deberian hacer simplemente `history.back()` si eso puede dejar al usuario en la misma pantalla o reenviar un POST.
- Se repitieron problemas de volver a eventos, home, jobs, equipos o formularios despues de acciones.
- Login/register deberian ofrecer forma clara de volver a la pagina principal.
- Tras registrarse o autenticarse, conviene volver al flujo iniciado cuando corresponde, no siempre mandar a home.
- CTAs core no deben estar escondidos: reservar, asistir a evento, cargar comprobante, crear viaje como usuario, ver historial o administrar reservas deben estar ubicados donde el usuario los espera.
- Las vistas vacias necesitan CTA o mensaje claro: turnos vacios, boliches no descubribles por falta de mesas/eventos, sellers sin datos de pago, etc.
- Links presentados al usuario no deberian llevar a `403` o `404` evitables.
- Acciones destructivas como cancelar, borrar o eliminar reviews necesitan confirmacion.
- Los elementos que parecen clickeables deben ser clickeables; evitar hover sobre elementos no accionables.
- Prevenir doble click en acciones importantes. Si un boton entra en loading, deshabilitar acciones relacionadas.

### Busqueda, filtros y paginacion UI

- Tags/chips deben ser clickeables si visualmente parecen filtros.
- Si existe autocomplete para cargar tags, tambien puede necesitarse para buscar por tag.
- El search no debe borrar filtros salvo que sea una decision explicita y visible.
- Filtros deben verse en URL cuando aportan navegabilidad, pero links compartidos no deben conservar query params transitorios como `commentPublished=true`.
- Faltaron filtros por estado critico, torneo terminado, tipo de notificacion, ubicacion, amenities, precio, rating o estado segun dominio.
- El explore no puede quedar demasiado simple si el producto depende de descubrimiento.
- Selectores de lessons, comments, estudios, medicos o listas largas deben paginarse.
- Paginacion visual debe ser consistente: cantidad por fila vs total por pagina, paginas maximas, hijos de comentarios y listas secundarias.

### Forms, validacion visible y feedback

- Mensajes de maxlength deben decir el limite.
- Telefonos no deberian aceptar letras si el dominio exige numeros.
- Campos obligatorios deben marcarse consistentemente con asterisco.
- Inputs numericos no deberian aceptar negativos si no son validos.
- Campos como peso, altura, fecha de nacimiento, telefono o precio necesitan mensajes de error utiles.
- Helper text puede evitar errores repetidos en datos delicados.
- El form no debe perder datos ante errores, por ejemplo fechas o tags.
- Modales demasiado chicos, apretados o con muchos pasos innecesarios aumentan friccion.
- Un modal de confirmacion de compra no deberia exigir tres clicks si puede mostrar toda la informacion y confirmar en un paso.
- Si se abre un modal por segunda vez, no deberia romperse.

### i18n, encoding y copy

- Cambiar idioma del browser debe afectar la app si se usa `AcceptHeaderLocaleResolver`; si se usa `SessionLocaleResolver`, debe estar justificado.
- No hardcodear `lang="es"` en JSPs si la app soporta idiomas.
- Monedas, enums, tags, areas, periodicidad, asuntos de email, alt texts e instrucciones deben internacionalizarse.
- No concatenar textos en vistas; usar mensajes interpolados porque el orden de palabras depende del idioma.
- Evitar keys visibles como `invite.friends.send` o mensajes sin key asignada.
- Corregir encoding roto: caracteres como `â`, `Ã...`, tildes mal renderizadas o textos mezclados.
- Plurales como "1 reseñas" requieren pluralizacion dinamica.

### Mails, notificaciones y comunicacion

- Announcements o eventos relevantes deben tener notificacion explicita, por mail o in-app segun el flujo.
- Si borran una review, banean un owner, aceptan/rechazan postulacion, se gana un torneo o cambia una reserva, el usuario afectado debe enterarse.
- Mails no deben apuntar a `localhost` ni a URLs mal formadas.
- Mails deben tener header, CTA, datos completos y tono visual consistente con la app.
- El mail de reserva o pago debe incluir detalle de reserva, monto/datos de pago y aclarar si falta pago.
- El mail de validacion deberia permitir validar directamente.
- El mail de notificacion necesita titulo o contexto suficiente.
- Evitar mails duplicados si se pueden combinar onboarding y creacion de cuenta.
- El idioma del mail debe respetar al destinatario.

### Visual, layout y accesibilidad

- Botones necesitan padding, alineacion, margen y codigos de color coherentes.
- Iconos, lupas, cards, dropdowns y popups deben alinearse y no exceder contenedores.
- Contraste insuficiente, por ejemplo texto verde sobre glass, dificulta lectura.
- Scroll horizontal/vertical raro en secciones con pocos elementos es mala senal de layout.
- Imagenes en botones no deben pisar texto.
- Alt texts son positivos para accesibilidad, pero tambien deben estar localizados.
- Perfil, rankings, vistas de match, reportes y dashboards deben exponer informacion accionable, no solo logs o datos incompletos.

---

## Testing

- `Mockito.verify()` valida implementacion, no comportamiento; fue marcado varias veces como error conceptual grave.
- Tests no unitarios llaman multiples metodos de la class under test, otros DAOs o flujos completos durante setup/verificacion.
- Nombres como `create_and_findById_ok` evidencian que un test mezcla operaciones.
- Tests de persistencia deben validar estado final de DB, no solo el valor retornado por el metodo.
- Si se usa JPA, algunos tests necesitan `em.flush()` para que la validacion realmente llegue a DB.
- `assertTrue(true)` para "no hubo error" es mal test; conviene que el metodo devuelva algo verificable o assertar estado observable.
- No dejar tests vacios.
- Cubrir casos no felices, errores y bordes; no solo happy paths.
- Servicios con logica pesada necesitan tests: `TaskScheduleServiceImpl`, `TaskServiceImpl`, `RestaurantService`, `SizeService`, `TournamentService`, `AnnouncementServiceImpl`, `EventServiceImpl`, `ImageServiceImpl`, `RoomAvailabilityService`, `createJourney`, `createBatchTemplates`, entre otros ejemplos.
- DAOs complejos o query builders necesitan tests sobre combinaciones de filtros, presencia/ausencia de parametros y precedencia.
- La cobertura de persistence puede ser buena y aun asi faltar cobertura en ReportDao, estadisticas, FriendJpaDao u otros DAOs puntuales.
- Si se preparan fixtures de DB, hacerlo de forma consistente; no mezclar `queryForMap` y `countRowsInTableWhere` sin criterio.

---

## Seguimiento, Scrum y entrega

- Se elogia el uso correcto de Plane, features, chores, estimaciones y seguimiento visible.
- Agregar paginacion a una vista que no la tenia puede ser feature, no chore, porque agrega valor al usuario.
- Una story tipo "Correcciones demo entrega" es demasiado general; debe descomponerse en tareas accionables.
- Quedar con cards en `In Progress` o `Todo` al cierre resta calidad de seguimiento.
- Commits como `"aaaaaa"` no son aceptables.
- TODOs en codigo, especialmente comentarios tipo "lo hizo GPT y no entiendo que hace", son mala senal de ownership tecnico.
- Entrega tardia, por ejemplo a las 20:52, queda asentada como problema de proceso.
- Revisar delta de funcionalidad contra entrega anterior: TP2 no es solo migrar ORM, tambien debe mostrar valor y correcciones reales.

---

## Buenas practicas elogiadas

- Corregir satisfactoriamente problemas de la primera entrega suma y se menciona de forma explicita.
- Implementar paginacion correctamente fue elogiado cuando estuvo bien resuelto.
- Buen uso de Plane, chores, bugs, features, estimaciones y features canceladas por feedback.
- Buen uso de custom exceptions y enums para ordenamiento.
- Buen uso de decoradores o validadores de capacidad para reservas.
- Buen uso de filtros multiples y query params visibles en URL.
- Confirmacion al borrar eventos o acciones destructivas cuando esta bien implementada.
- Feedback al navegar la aplicacion.
- Multiselect con autocomplete para autores.
- Vistas de reportes/admin completas con accionables esperados.
- Componentes custom y flujos nuevos bien implementados.
- No presentar errores o dead ends en el flujo principal.
- Excelente o muy buena cobertura de tests en algunos grupos.
- Buen mapeo de Hibernate cuando las relaciones estan correctamente implementadas.
- Buen uso de builder pattern.
- Buen uso de `@Formula` para evitar persistir contadores o mapeos innecesarios.
- Recursos como `PageableQueryBuilder` y `PagingJpa` fueron valorados, con caveat de que `getId` no implementado se vuelve error runtime.
- Buen uso de Javadocs a nivel DAO.
- Loguear al usuario despues de recuperar contrasena fue elogiado.
- Boton para volver desde login a home fue elogiado.
- Dashboards, estadisticas, onboarding, animaciones y respuestas visuales pueden sumar cuando el flujo principal esta completo.

---

## Lectura operativa para una app TP2

Al auditar o implementar TP2, priorizar en este orden:

1. **No romper datos ni funcionalidad previa**: migracion JPA/Hibernate con schema, ids, constraints y datos existentes verificados.
2. **Cerrar feedback TP1**: muchos errores de TP2 son reincidencias de TP1, y la catedra lo penaliza explicitamente.
3. **Paginacion 1+1 y fetch controlado**: revisar queries reales, no solo mappings.
4. **Seguridad declarativa y ownership**: rutas, recursos y links visibles deben respetar permisos.
5. **Controllers delgados**: un caso de uso por llamada, validaciones en forms/services, errores por advice.
6. **Tests que prueban comportamiento**: DB final, bordes, lazy/async, queries complejas y servicios core.
7. **UX de flujo completo**: mail, notificaciones, CTAs, back navigation, zero states, i18n y mensajes localizados.
8. **Repo entregable limpio**: Maven, WAR, README, credenciales de prueba, archivos generados fuera de Git y configuracion real.
