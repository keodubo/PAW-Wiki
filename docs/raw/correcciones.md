# PAW AI Categorized Instructions & Knowledge Base (Full Original Data)

A contunuación se presenta la recopilación **absoluta e íntegra (sin pérdida de información)** de los errores, falencias y virtudes encontradas a lo largo de las correcciones previas de la materia. Están agrupadas semánticamente para que otra IA (o desarrollador) las absorba de manera estructurada.

---

## 🚨 [CRÍTICO] ERRORES MÁS FRECUENTES (RECURRENTES) 

*(Estos errores han aparecido múltiples veces a lo largo de diferentes grupos)*
* **[3 veces]** La rememberMeKey podría ser un `@Value` en lugar de un método.
* **[2 veces]** Tiene validación de email y cambio de contraseña.
* **[2 veces]** Pushean archivos vacíos al repositorio como `jvm.config` y `maven.config`.
* **[2 veces]** Utilizan boxed primitives y no parece ser por diseño.
* **[2 veces]** Cumplen a medias con la no instanciabilidad de clases utils.
* **[2 veces]** Buena cobertura de Tests en el DAO.
* **[2 veces]** Bien por utilizar custom validators.
* **[2 veces]** Omiten modificadores de acceso y no parece ser por diseño.
* **[2 veces]** Tiene algunas definiciones de versiones de dependencias en los poms hijos, repitiendo o cambiando la del pom padre.
* **[2 veces]** No escapan los caracteres especiales de LIKE.
* **[2 veces]** Los módulos hijos declaran explícitamente `<version>` cuando estos se heredan del padre.

---

## 🛡️ SEGURIDAD Y CONTROL DE ACCESO (Auth, XSS, Autenticación)
* Intentar XSS en creación de menú tira 500.
* No hay recuperación de contraseña.
* No protegen los endpoints de TurnTemplateController.
* Quedó un contenido dinámico sin C:OUT; son vulerables a inyecciones XSS. Esto es un error grave.
* Imprimen valores a JSP usando `$(...)` y no `<c:out>`, lo que los expone a vulnerabilidades XSS.
* Imprimen mensajes directamente sin utilizar `<c:out>` sin escapar el contenido, lo que los hace vulnerables a XSS.
* Hay textos sin `<c:out>` que descansan en patterns bien implementados desde la parte de forms. Esos campos muestran texto libre del usuario que de fallar el pattern abre la puerta a XSS.
* Imprimen valores en los JSP sin escaparlos, siendo susceptibles a XSS. Esto es un error grave.
* Imprimen valores en el JSP sin escapar correctamente, siendo vulnerables a XSS. Esto es un error conceptual grave.
* Falta escapar `%` y `_` en el search. Posible XSS en el searchbar.
* Bien por escapar los `%` y `_`. Si busco `" OR 1=1` se cuelga la app.
* No parecen escapar los `%%%` en la búsqueda por texto.
* La validación de si un usuario puede editar una lección se hace manualmente en el controller, esto es un error conceptual grave. Esto debería ser una regla de seguridad declarativa mediante Spring Security.
* Rutas de suscripción / desuscripción sin protección explícita. La verificación `if (loggedUser != null)` es manual. Debería estar protegida por Spring Security.
* Los JSP tienen chequeos manuales de acceso para decidir mostrar u ocultar partes de la web. Estos chequeos duplican lógica que corresponde a Spring Security y depende de magic strings. Adoptar la taglib de spring-security.
* Chequeos manuales de acceso/pertenencia en controllers (debe ser declarativo en spring-security). Esto es un error.
* Validan control de acceso tanto desde WebAuthConfig como usando `@PreAuthorize` en los controllers. Debiera ser consistente y usar una forma u otra.
* La lógica de autenticación está partida, con parte de las reglas en el WebAuthConfig y el resto regado por el código en distintos controllers via `@PreAuthorize`.
* Solo poseen validación de roles en las rutas, falta un nivel mas fino de control de accesos para evitar que un usuario pueda realizar acciones sobre recursos que no le pertenecen.
* Parte de los preauthorize parecen ser lógica de negocio más que chequeos de seguridad.
* Faltaría un control de accesos a recursos a partir del ownership del user.
* Manejan control de acceso en los servicios en lugar de hacerlo a través de Spring Security.
* Existen controllers que realizan validación de NotAnAdmin en vez de utilizar un Access Control.
* Como se menciono, falta un control de accesos por ownership del recurso para evitar que cualquier user autenticado pueda realizar acciones sobre recursos que no le pertenecen.
* Realizan validaciones que deberían ser parte de Spring Security en el controller.
* Tienen chequeos de que el usuario no sea nulo o tenga permisos en los controllers, cuando pertenece a spring security.
* `clubAdminService.validateAdmin(clubId);` en varios controllers debería ser verificado por Spring Security.
* Al hacer un GET a `/`, se actualiza el Locale del usuario en la base de datos (Malas prácticas HTTP y de sesión).
* En el login falta un botón para volver a la home.
* Al cambiar contraseña debieran pedir la contraseña actual.
* No hay confirmar contraseña.
* El método `resetPassword` en `UserServiceImpl`, además de intentar el reinicio de password, realiza la validación de usuario (se mezclan responsabilidades).
* En la lógica de resetPassword envían el userId además del token. Un token tiene asociado un usuario.
* Borran los usuarios que expiraron y no verificaron, en vez de proveer la posibilidad de regenerar el token de verificación.
* Al validar si currentPatient o currentMedic es null en los controllers, están haciendo un doble chequeo de seguridad que debería estar manejado por spring security.
* La validación de si un usuario es owner de un room se hace manualmente en el RoomController, esto es un error conceptual grave.
* Faltan validaciones de si se puede ingresar a un evento ya lleno y del pasado (vulnerabilidad lógica).
* Buen uso del `AccessHelper`. Sin embargo validan bloqueo de usuarios en el `AccessHelper`, cuando un usuario bloqueado directamente no debería poder autenticarse usando el field `accountNonLocked`.
* Falta un mayor uso de spring security, como por ejemplo para usuarios bloqueados.
* Al intentar editar un torneo del cual no sos dueño tira 404. Debería tirar 403.
* Si busco un freelancer que no existe, la aplicación muestra 403 en lugar de 404.
* En algunos casos los 403 deberían ser 404.
* Puedo eliminar los viajes de otras personas.
* Puedo editar las publicaciones ajenas.
* Puedo ingresar a ver Myclubs por más de no tener el ROL correcto.
* Orden de reglas rompe autorización por rol. `/**.authenticated()` antecede a rutas de ORGANIZER, por lo que estas quedan solo autenticadas. Esto es un error grave.
* En configure de WebSecurity ignoran las rutas de 403 y 404, pero posteriormente los incluyen al configurar el HttpSecurity.
* Hay algunas rutas en WebConfig que poseen primero un nivel mas restrictivo pero luego hacen un `permitAll()`.
* Credenciales de Gmail hardcodeadas y trackeadas en git.
* Tienen credenciales en el repositorio.
* Tienen credenciales (e.g. mailing, db) en el repositorio.
* La rememberme key está hardcodeada y es trivial. Compromete la seguridad de "recordarme".
* Es incorrecto tener un endpoint POST `/logout` ya que eso lo maneja el propio Spring Security.

---

## 🏗️ ARQUITECTURA Y MVC (Controllers, Models, Clean Code)
* Validan un form a mano en el controller en vez de usar un custom validator. Hacen llamados a varios services al crear para crear el modelo y cargar imagen, cuando debería ser un único método al que se le encargue toda la creación.
* Muchísima lógica de negocio en controllers, esto es un error conceptual grave. Ej viewCommunityFollowers.
* Los controllers contienen lógica de negocio.
* Poseen lógica de negocio en los controllers.
* Hay MUCHA lógica de negocio en los controllers, solo para ejemplificar el `JobApplicationController` tiene logica de negocio en TODOS sus metodos.
* El UserController tiene lógica de negocio en gran parte de sus endpoints. Esto es un error conceptual grave.
* Lógica de negocios en los controllers ej: `event/{id}`. Esto es un error conceptual grave.
* Hay bastante lógica de negocio en los controllers. Esto no solo rompe con la división en capas sino que para algo transaccional de esta índole evita toda transaccionalidad.
* Algunos controllers contienen lógica de negocio ej: editEnterprise.
* Dentro del `EventController` llaman una funcion llamado `getEventIdByResponseId`, que se utiliza para verificar si la respuesta corresponde al evento. Esto es un checkeo que no corresponde en el controller.
* En `newEditProfile` están guardando la imagen, actualizando el nombre o editando los juegos favoritos. Esto es lógica de negocio que debería ir en el service. Pasa lo mismo en `uploadGameImage`.
* En `tournamentStart` del controller estan pidiendo por el Tournament y después comenzando el `tournamentId` como validacion. Esto es logica de negocio que no corresponde en controller.
* El controller `TripsController` popula listas pasadas por referencia. Violación grave de OOP y principios SOLID.
* El controller RoomController instancia objetos de dominio, esto es un error conceptual grave.
* Instancian modelos en el controller, lo mejor seria utilizar un DTO para asegurarse que todos modelos creados provienen de la capa de persistencia.
* Para los updates, crean el modelo en el controller, esto hace que existan modelos en la app que no fueron creados por la capa de persistencia.
* Las clases de sus modelos en "data" no son realmente modelos sino una especie de DTO. No es necesaria esta capa extra.
* Instancian entidades del dominio a nivel servicio. En conjunto con el punto siguiente, hay instancias que existen con información parcial.
* Los servicios no deben instanciar entidades de dominio. Es responsabilidad del DAO.
* La creación de DTOs se mejoraría con builders o métodos fromModel, si bien no parece ser necesario el uso de los mismos.
* Implementan servicios en la capa web. Esto es una violación al modelo de capas jerárquicas establecido. Es un error conceptual grave.
* Interfaces depende de `spring-jdbc`/`spring-web` (infraestructura) viola límites de capa / expone detalles de implementación.
* Definen distintos métodos en los servicios que podrían ser simples getters de los modelos.
* `UserTripsData` no es modelo de su sistema, sino un wrapper de varios modelos del sistema.
* Las actualizaciones a un perfil deberían estar encapsuladas en un único método de un servicio para su llamado desde el controller.
* Manejan rutas para casos de PageNotFound en vez de delegarlos a Spring.
* En `web.xml` están mapeando tanto el Exception como el Throwable a 500. Uno de los mappings es innecesario.
* El ErrorController está mal nombrado, es un ControllerAdvice.
* El `SecurityErrorBridgeController` no tiene sentido que exista.
* `ExpiredPassTokenException` tiene un campo privado sin final ni setter.
* Arrojan una RuntimeException al no encontrar un user en `VerificationServiceImpl`, cuando un NotFound custom o el default son más apropiados.
* Existen servicios que hablan en términos de vistas y hacen referencia a recursos de la web. Esto es un error conceptual grave.
* El EmailService conoce nombres y paths relativos de templates que no existen en ninguna capa que servicios conozca, sino en webapp. Esto es una violación al modelo jerárquico.
* El Locale que usan al enviar mails al dueño es el del cliente (El mail se manda asíncrono, usar locale del destinatario).
* Emails se envían con el idioma del sender.
* En el email service, obtienen el Locale del `LocaleContextHolder` o inyectado por controller en lugar del `preferredLanguage` del destinatario.
* Definen un `MailService` y un `AsyncMailService`. El si los métodos son async o no es un detalle de implementación, no debiera definir a la interfaz.
* Se está haciendo un loop de envío de emalis en el ContactService. Se recomienda manejar este tipo de procesos en un hilo asincrónico aparte.
* El código hace abuso de magic strings, que son repetidos y conocidos a lo largo y ancho de la aplicación, atravesando múltiples capas de la misma.
* Números mágicos en controllers sin explicación.
* El orderBy debería ser un enum más que un magic string.
* En varias instancias hacen uso de una lista de strings para los días, la cual debería reemplazarse por un enum.
* El string tab debería ser un enum.
* No se está utilizando el enum SwapStatus en el modelo Contact, en cambio se utiliza con un String permitiendo valores inválidos.
* Hay constantes en los controllers sin uso.
* Inyectan servicios que luego no utilizan.
* Las clases piden que les inyecten atributos que luego no usan.
* Los métodos de una interface no necesitan ser anotados como public. En las interfaces la declaración de que el método es publico es redundante.
* Clases como el `ScheduleServiceImpl` son extremadamente complejas, denotando una pobre modularización y modelos anémicos.
* No siguen la convención de nombres de Java, con clases como `Relation_Task` `TaskSchedule`.
* Usan snake_case en algunos casos, siendo camelCase el estándar en Java.
* La convención para el nombre de los modelos es la forma singular. Por ejemplo `Users` debiera ser `User`.
* No usan el propósito de la clase Optional de manera correcta.
* Utilizan `get()` de un Optional sin validar previamente su éxito (`isPresent()`).
* En Event tienen un field que es Optional. Optional solo debería ser usado para resultado de función o retornos fluidos.
* Usan null para indicar valores inexistentes, y a veces usan Optional. O incluso arrojan una excepción como `ScheduleNotFoundException`.
* Mezclan usos de null, Empty y un objeto instanciado con id negativo para indicar inexistencia de un dato.
* Hay funciones que realizan auto-unboxing que puede lanzar NullPointerException si se pasa null.
* Los métodos create de los services son inconsistentes donde algunos retornan la nueva entidad creada y otros son void.
* Tienen diferentes métodos dependiendo de qué parámetros aplican. Lo adecuado seria tener un solo método que acepte parámetros opcionales.
* Obtener el usuario autenticado podría extraerse a un método o ControllerAdvice para evitar reescribirlo. 
* El método getCurrentUser no es necesario ya que se puede obtener con un `@ModelAttribute` en un controller advice.
* El `LoggedUserAdvice` es un advice, que no sólo lidia con el logged used... sino que también provee Exception Handling.
* LOS `@ControllerAdvice` presentes nunca se usan, ya que no están en un paquete escaneado por Spring.
* Configuran simultáneamente exception resolvers y exception handlers para las mismas excepciones en lugares distintos de la aplicación.
* Hacer try catch de exception en los controllers no es correcto, estos errores deberian ser manejados con el Exception Handler.
* Hay una oportunidad desaprovechada en el manejo de las excepciones. En vez de heredar todas de la Excepción base podrían agruparlas.
* Hacen un uso excesivo de try/catch para loggear errores sin tirarlos formalmente a MVC.
* Try catchean una Exception en plano en `NoExistingJourneyValidator`. Esto indica que no usan custom exceptions.
* Overridean `equals()` pero no `hashCode()`.
* Tienen restricciones que validan en capa de servicios pero no en los forms vía Constraints Validator lo que obliga hacer un feo Exception Handling en el Controller al crear.
* Tienen validaciones para los formularios en los controllers, los cuales deberían ser constraint validators. 

---

## 🗄️ PERSISTENCIA, DB Y PERFORMANCE
* Tienen un archivo `schema.sql` a nivel webapp, cuando esto concierne a la capa de persistencia.
* El archivo `schema.sql` corresponde en la capa de persistencia.
* Restringir la ubicación de scripts tipo `schema.sql` exclusivamente a los resources de persistence. El schema.sql esta vacío.
* Utilizan el paquete `java.sql` en capa de modelos.
* Utlizan clases de `java.sql` fuera de la capa de persistencia. La existencia de una base de datos es un detalle de implementación del DAO.
* Handlean exceptions de SQL en la capa de webapp y utilizan la clase `java.sql.Date`.
* Usan `java.sql.Timestamp` en la capa models para convertir un Instant a Date. Debieran usar tipos como `LocalDate` y `LocalDateTime`.
* No se está haciendo buen uso de la base de datos, en vez de hacer JOIN, se itera y se obtiene recursos haciendo N accesos a la db, lo cual luego se nota en la velocidad de la página.
* Hacen "Joins" en Java en lugar de utilizar el motor de base de datos, esto es ineficiente.
* En `ActivityService`, `categoriesIds.stream().map(...)` causa N+1 queries. Se debería crear `CategoryDao.findByIds (WHERE IN)`. Mismo en BookingService.
* Hay operaciones realizadas en java que implican JOINs o filtros de datos. Estas deberían ser en la capa de persistencia.
* Realizan filtrado de resultados en Java. Esto debiera realizarse directamente en la base de datos.
* Para buscar los exámenes pendientes, se traen 1000 exámenes y filtran en memoria.
* Utilizan DAOs para setear las precondiciones en los tests de persistence en vez de usar scripts.
* No están utilizando `@Transactional(readOnly = true)` para las funciones que no modifican la db.
* Solo utilizan `@Transactional` en 4 métodos de transaccionalidad, esto es incorrecto.
* Hay muchos métodos sin `@Transactional`. Todos los métodos deberían tenerlo en la capa Service y no únicamente los que realizan escrituras.
* No utilizan `@Transactional` en ningún lugar de la aplicación.
* Faltan `@Transactional` en métodos de Servicio que utilizan inserts, deletes, o updates.
* Eliminan a mano relaciones en cascada interdependientes en lugar de usar ON DELETE CASCADE en base de datos.
* Sus modelos no se guardan el id de referencia a la entidad relacionada sino que usan composición. Esto causa que deban traerse todas las entidades relacionadas en los rowmappers (acoplamiento forzado).
* Esperaría un nivel de testing mayor para `TradeDaoJdbcImpl`. Recomendable separarlo en distintos daos y reusar rowmappers.
* Para actualizaciones, cada tabla debe ser modificada por un solo DAO. Hay DAOs editando tablas ajenas.
* Tampoco catchean ForeignKeyException en el DAO generando un 500 en vez de devolver un StatusCode 400 amistoso.
* Rankings de juego se iteran mal. En `TournamentJdbcDao.getRound` utilizan un `RowCallbackHandler` con closure en vez de un `ResultSetExtractor`.
* Esta mal utilizado el JDBC insert ya que en lugar de inicializarlo una unica vez lo inicializan en cada llamada a método. Esto no es thread safe y causa problemas de concurrencia.
* Utilizan `jdbcTemplate.update` para insertar datos en lugar de usar la wrapper `jdbcInsert` inyectable más sencilla e infalible.
* Existen Row mappers que no son `private static final` y son instanciados en cada ejecución repetidamente agotando heap de memoria.
* Tendría más sentido que los static rowmappers se definan como los strings estáticos que tienen.
* El uso del StringBuilder en `JdbcDaoUtils` no es muy correcto cuando no hay inserciones dinámicas dinámicas (solo append estatico constante). Utilizar concat o String.format puro.
* La capa de persistence recibe offset como parámetro. Sería más adecuado usar interfaces estilo `Page` para abstraer la lógica global de UI en Paginado.
* Retornan colecciones sin una paginación adecuada. Las "secciones" se usan como magic strings.
* El `EmailServiceImpl` está anotado como `@Transactional` pese a no usar la base de datos y tener todos los métodos públicos asíncronos marcados como `@Async`.

---

## ⚙️ INFRAESTRUCTURA Y CONFIGURACIÓN (Maven, Logs, Git)
* Loggean a nivel debug en producción.
* Loguean a salida estándar sin utilizar un logger adecuado. Esto es un error grave. Por ejemplo `exception.printStackTrace();`.
* Loggean en Info a STDOUT en producción. Esto es un error conceptual grave.
* Loguean a DEBUG en producción.
* Aunque incluyen las dependencias de logback, no hay configuración. Loguean todo por defecto en catalina.out (localhost.log). Requisito fallado explícitamente.
* Guardan en el repositorio archivos de configuración del editor de texto (.vscode) y de configuración de mvn con archivos vacíos (.mvn).
* Hay archivos que no deberían estar en el repositorio (carpeta `.PVS-Studio`).
* Todas las capas tienen un directorio bin. Además pushean el directorio out / src bifurcado de las IDEs.
* Definen versión de dependencias en pom hijo (a veces estáticas manuales en vez de usar `<dependencyManagement>`).
* El pom de models declara java 17 cuando a nivel global debería ser 21. También declara en el dependency management JUnit 5 aislado.
* Tienen duplicada la dependencia validation-api.
* Agregan SpringFramework a la capa de interfaces de manera innecesaria.
* Uso de dependencia de spring-boot (cuando en realidad deben usar librerías canónicas puras sin el framework contenedor como spring-context).
* Incluyen la dependencia sobre hsqldb en compile. Esto es un error conceptual, debe ser solo de scope Test.
* El paquete de configuraciones de spring no necesita escanearse a sí mismo, las clases ya están mapeadas en el `web.xml`.
* Se crea un bean para `CharacterEncodingFilter` que luego no se usa, el filtro está configurado directamente en el `web.xml`.
* Se configura para escanear múltiples veces los mismos paquetes en forma innecesaria.
* El uso general del CacheManager en Memoria esta bien, pero están cacheando imágenes con `ConcurrentMapCacheManager` lo cual no evicta. Causa que si cachean suficientes imágenes se cae el sistema (Out Of Memory).

---

## 💅🏻 UI / UX Y FLUJOS VITALES (Vistas, i18n y Lógica Frontend)
* `01/restaurants/1/reservations/create` no tiene favicon al igual que el resto.
* El ancho del contenido es inconsistente.
* Hay una doble barra `//` en el search y en algunas URLs arrojando 400 por doble `/paw/`. Usa `c:url`.
* Usan `$(pageContext.request.contextPath)` en vez de `<c:url>`, fallando en proxys e incurriendo en errores de ruteo.
* Imprimen textos estáticos manuales en plural: Podrían realizar pluralización dinámica (`ChoiceFormat`).
* En lugar de invitaciones son llamadas solicitudes contextualmente. Reemplazar wording.
* La página posee errores de encoding en caracteres tildados (tildes `ñ`).
* La aplicación no está internacionalizada. Pese a que definen un MessageSource, el mismo referencia a archivos inexistentes.
* Los títulos de las paginas no están internacionalizados.
* Usan alt texts para imágenes, pero no están internacionalizados.
* Mensajes default "email.duplicate" o crudos se logran derramar impresos textualmente hacia el Frontend.
* Faltan botones CTA claros ante Zero States (cuando las listas vuelven vacías sin resultados).
* Tienen un sort "relevance" que no se basa en ningún parámetro relacionado a relevancia (sorting estigmatizado o hardcodeado pseudo random temporalmente creado).
* Faltarían filtros como por ejemplo precio o rating al listar elementos extensos.
* La cantidad de dígitos del review debería estar redondeado a 2 para visualizacion amigable.
* El registro está raro... "registro mi empresa", para caer a una pantalla que bloquea. Deberían redireccionar (Auto-login post registro).
* Al restablecer contraseña, estaría bueno loguear al usuario al final del proceso. 
* El usuario anónimo solo puede ver el contenido pero tiene tabs inaccesibles engañosas. Ocultar los ítems tab con librerias JSTL authorization.
* Si el owner cancela reserva, el mail no le llega al cancelado dejándolo sin visibilidad. 
* Los emails enviados necesitan tener format HTML estándar con Header, e incluir Call to Actions (CTAs limpios) estilo botón, en lugar de un `href` sucio suelto.
* El mail pidiendo pago no tiene los detalles y el CBU incorporado en HTML.

---

## 🧪 TESTING (Test de Persistencia & Servicios)
* Hacen uso de clases de JUnit 3.
* Los tests de persistencia nunca validan el estado de la base de datos (Ej: hacen execute pero omiten count de DB o Assert). Esto es un error grave.
* No testean casos importantes como por ejemplo crear un elemento principal complejo en la capa de persistence.
* Falta cobertura de casos para los métodos de servicios complejos, o para las dependencias no felices.
* Utilizan Mockito verify, esto valida la implementación exacta (orden y llamados) y no el comportamiento del resultado general. Error grave de unit testing. En su lugar usar Asserts a resultados o mocks controlados.
* La cobertura de tests es muy escasa teniendo métodos que realizan lógica de negocio grande, múltiples servicios con cobertura nula comentada.
* Hay unit tests que no son unitarios, validando cambios de estados de objetos mockeados o llamando a múltiples clases a la misma vez. 
* En los tests hacen verificaciones con `Mockito.verify()` y `Mockito.never()`. Validar estado final.
* Hay dependencias en los test, por lo que no son unitarios. Se utilizan métodos aparte del que se esta testeando.
* Realizan inserciones directas en base de datos en los diferentes tests por jdbcTemplate para setear precondiciones, en lugar de partir de un estado global definido adecuadamente en sql script.
* Para los tests de DAO pueden y DEBEN usar explícitamente la anotación `@Rollback` para evitar tener dependencias transaccionales al vaciar entre un test y otro.

---

## ✨ MÓDULO DE BUENAS PRÁCTICAS RECOMENDADAS ✨

*(Elogios y cosas realizadas exitosamente a lo largo de las correcciones pasadas para asimilar)*
* **Buen Setup Flujo QA (BugTracker):** Excelente uso del control de herramientas (`Done`, Features bien separadas de los Chores. El uso explícito y documentado en tableros mejora el orden).
* **Mailing:** Agradable funcionalidad de mails con flujos y plantillas L10n bien compuestas. CTA asertivos que redirigen bien e invitan exitosamente la re-interaccion.
* **Componentes Robustos Frontales:** Buen flujo de creación asíncrona, Paginación bien implementada. Selectores ricos visualmente (Autocomplete, Filtros dinámicos sin Refresh agresivo). 
* **Custom Validators Excelentes:** Separar y abstraer todo modelo de control de inputs (Largo del description, match passwords) dentro del Annotator `@` en forma JSR380 ayuda de maravilla a limpiar el código MVC, concentrando la seguridad y control de nulos en validaciones estandarizadas. 
* **Vulnerabilities Catching Exception:** Genial el uso del Catch al subir imágenes logrando filtrar si la extensión no es genuina desde el Multipart.
* **Views e i18n:** Escudo exitoso con CTAs, y zero states amigables resueltos con directivas robustas y un control customizado de redireccion 404/403 si un recurso explorado expira. Múltiples roles de Admin se manejaron limpiamente en jerarquías MVC.
