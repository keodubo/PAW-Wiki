# Plan de Remediacion de Auditoria Actual PAW

> Estado: resuelto/cerrado. Esta pagina queda como registro del plan ejecutado el `2026-04-20`, no como backlog pendiente.

- Fecha: `2026-04-20`
- Estado: `Resuelto`
- Objetivo: cerrar los hallazgos vigentes de la auditoria actual y dejar repo, wiki operativa y bootstrap local alineados entre si.
- Canon usado: `PAW-Wiki/docs/wiki/*`, `CLAUDE.md` del repo y la auditoria manual del checkout actual.
- Alcance: README/bootstrap, seed operativa por rol, scheduler de reservas, higiene JDBC y ejemplos de configuracion.
- Fuera de alcance: deuda historica ya cerrada, redisenos UI y cambios funcionales que no ayuden a resolver estos 5 hallazgos.

## Cierre del plan

### Estado final de los hallazgos

| Hallazgo | Estado final | Nota de cierre |
| --- | --- | --- |
| `README.md` desalineado con rutas/flujos reales | Resuelto | README reescrito contra los controllers y redirects reales. |
| Credenciales reviewer/owner no reproducibles con el bootstrap versionado | Resuelto por alineacion operativa/documental | Se documentaron credenciales demo/QA reales y se dejo explicito que el bootstrap automatico solo garantiza el admin; por decision de alcance no se seedearon reviewer/owner/restaurantes en `schema-postgres.sql`. |
| `ReservationSchedulerImpl` fuera del patron de la wiki | Resuelto | El scheduler queda fino y delega en `ReservationService`; la logica transaccional y de notificaciones vive en el service. |
| `SELECT *` residuales en DAOs | Resuelto | Se reemplazaron por listas explicitas de columnas en los DAOs versionados afectados. |
| `.example` obsoletos | Resuelto | `mail.properties.example` y `database.properties.example` se alinearon con el wiring Spring real. |

### Verificacion final registrada

- `mvn clean test`
- `mvn -pl webapp -am package -DskipTests`
- Smoke manual en `localhost:8080`:
  - login reviewer, owner y admin;
  - navegacion por rutas documentadas en README;
  - perfil reviewer, ficha de restaurante e imagenes;
  - creacion de reserva, confirmacion owner y tick real del scheduler.

## Hallazgos de entrada

1. `README.md` documenta rutas y flujos de registro que no coinciden con los controllers reales.
2. `README.md` promete credenciales reviewer/owner que hoy no son reproducibles con el bootstrap versionado.
3. `ReservationSchedulerImpl` no sigue el patron documentado en la wiki de reservas: scheduler fino, sin ownership de DAOs ni de `@Transactional`.
4. Quedan `SELECT *` residuales en DAOs versionados.
5. `mail.properties.example` y `database.properties.example` arrastran referencias obsoletas.

## Recomendacion Fuerte

### Que hacer

- tomar `PAW-Wiki/docs` como fuente de verdad;
- alinear el codigo a la wiki en el caso del scheduler;
- alinear el README a la app real;
- hacer que reviewer y owner existan de verdad en el bootstrap automatico, no solo en el README;
- cerrar la pasada con verificacion tecnica y smoke manual acotado.

### Por que

- hoy el principal riesgo no es una falla de build sino una inconsistencia operativa: alguien sigue la documentacion y cae en rutas o credenciales falsas;
- el scheduler actual funciona, pero deja una desviacion literal contra el plan de reservas que puede reabrirse en futuras correcciones;
- la deuda de `SELECT *` y ejemplos obsoletos no es critica, pero es barata de cerrar mientras se toca documentacion y wiring.

### Riesgo / downside

- la unica decision no trivial es el scheduler: mover responsabilidad a `ReservationService` puede tocar interfaz de servicios y tests;
- seedear un owner util para QA puede requerir asignarle un restaurante existente, no solo darle el rol.

### Como rollback

- un commit por fase;
- si la fase del scheduler introduce ruido, se puede revertir sola y dejar las fases de documentacion/seed igualmente cerradas.

## Decision que conviene cerrar al inicio

### Scheduler: opcion recomendada

**Default recomendado:** alinear el codigo a la wiki.

Esto implica:

- mover la logica transaccional del scheduler a `ReservationService`;
- dejar `ReservationSchedulerImpl` como disparador fino con `@Scheduled` y sin `@Transactional`;
- quitar acceso directo del scheduler a `ReservationDao`, `ReservationNotificationDao` y `UserDao`.

### Scheduler: alternativa valida

**Alternativa:** aceptar la implementacion actual como correcta y actualizar la wiki para bendecirla.

Solo conviene elegir esto si queres declarar explicitamente que el repo paso a ser la nueva fuente de verdad para reservas. Como el pedido actual es "cumplir lo de la wiki", no es la opcion recomendada.

## Orden Secuencial Recomendado

## Fase 0 - Baseline y criterio de cierre

### Objetivo

- fijar la secuencia, congelar el criterio de verdad y evitar retrabajo.

### Archivos / areas

- `README.md`
- `CLAUDE.md`
- `PAW-Wiki/docs/wiki/plan-implementacion-reservas.md`
- `PAW-Wiki/docs/wiki/2026-04-13_auditoria-repo-docs_v1.md`

### Pasos

1. Releer el hallazgo del scheduler y confirmar que la fuente de verdad sigue siendo la wiki local.
2. Aceptar el criterio operativo de esta pasada:
   - README y examples deben describir el estado real;
   - bootstrap por rol debe ser reproducible;
   - scheduler debe quedar alineado a wiki o, si no, la wiki debe actualizarse en la misma pasada.
3. Tomar baseline de verificacion:

```bash
mvn clean test
mvn -pl webapp -am package -DskipTests
```

### Salida esperada

- baseline verde y criterio de aceptacion congelado antes de editar.

## Fase 1 - Documentacion operativa y bootstrap real por rol

### Objetivo

- hacer que el README deje de mentir y que las credenciales que documenta existan realmente.

### Archivos

- `README.md`
- `persistence/src/main/resources/schema-postgres.sql`
- `persistence/src/main/resources/seed.sql`
- `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/ExploreController.java`
- `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/ProfileController.java`
- `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/OwnerDashboardController.java`
- `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/AuthController.java`
- `webapp/src/main/java/ar/edu/itba/paw/webapp/auth/AuthRedirectResolver.java`

### Decision recomendada

- no seguir apoyandose en `seed.sql` para credenciales operativas;
- mover las cuentas reviewer/owner demo al bootstrap automatico que ya existe en `schema-postgres.sql`;
- asignar al owner seed al menos un restaurante existente via `restaurants.owner_id` para que `/my-restaurants` y sus vistas derivadas sean realmente testeables.

### Pasos

1. Definir dos cuentas demo reales y estables:
   - reviewer: usuario, mail, password y locale validos;
   - owner: usuario, mail, password y locale validos.
2. Insertar esas cuentas en `schema-postgres.sql` con `password_hash`, `email_verified_at` y roles correctos.
3. Asignar un restaurante seed al owner demo para que el rol sea operable de punta a punta.
4. Dejar `seed.sql` solo como seed de contenido/catalogo, no como supuesto bootstrap de login.
5. Reescribir el README con:
   - rutas reales: `/explore`, `/profile/{username}`, `/my-restaurants`, `/register`;
   - flujo real de registro: selector reviewer/owner en un unico endpoint;
   - credenciales reales reproducibles por rol;
   - nota explicita de que `seed.sql` no es el mecanismo automatico de bootstrap.
6. Verificar que el post-login documentado coincida con `AuthRedirectResolver`.

### Riesgos

- si el owner demo queda sin restaurante asignado, el rol sera valido pero el smoke owner quedara incompleto;
- si se duplican reglas entre `schema-postgres.sql` y `seed.sql`, se reabre la ambiguedad documental.

### Criterio de cierre

- cualquier persona puede levantar la app y loguearse con reviewer, owner y admin usando solo lo versionado;
- el README describe rutas y flujos reales, no historicos.

## Fase 2 - Alinear scheduler de reservas con la wiki

### Objetivo

- hacer que la implementacion de reservas cumpla literalmente el patron documentado.

### Archivos

- `services/src/main/java/ar/edu/itba/paw/services/ReservationSchedulerImpl.java`
- `services/src/main/java/ar/edu/itba/paw/services/ReservationServiceImpl.java`
- `service-contracts/src/main/java/ar/edu/itba/paw/services/ReservationService.java`
- `services/src/test/java/ar/edu/itba/paw/services/ReservationSchedulerImplTest.java`
- tests nuevos o ajustados en `services/src/test/java/...ReservationService...`
- `PAW-Wiki/docs/wiki/plan-implementacion-reservas.md` solo si hace falta aclarar wording menor despues del refactor

### Implementacion recomendada

- agregar a `ReservationService` metodos explicitos para:
  - expirar pendientes vencidas y completar confirmadas vencidas;
  - enviar recordatorios diarios.
- mover a `ReservationServiceImpl`:
  - acceso a `ReservationDao`, `ReservationNotificationDao` y `UserDao`;
  - ownership de `@Transactional`;
  - envio de mails completados/reminders e idempotencia por `reservation_notifications`.
- dejar `ReservationSchedulerImpl` con esta forma:
  - resuelve fecha/hora si hace falta;
  - llama a `reservationService.*`;
  - loguea resumen final;
  - no declara `@Transactional`.

### Pasos

1. Extender la interfaz `ReservationService` con las operaciones scheduler-driven.
2. Mover la logica de `tick()` y `sendDailyReminders()` a `ReservationServiceImpl`.
3. Reusar helpers existentes de locale/notificaciones para no duplicar logica.
4. Reducir `ReservationSchedulerImpl` a orquestacion.
5. Reescribir tests:
   - scheduler: verificar delegacion y scheduling, no detalle de DAO;
   - service: verificar transiciones y mails bajo la nueva responsabilidad.

### Riesgos

- duplicar helpers de notificacion entre scheduler y service;
- dejar metodos publicos en servicio sin cobertura para casos borde ya cubiertos por los tests actuales del scheduler.

### Rollback

- revertir solo esta fase y volver al scheduler actual si la reubicacion rompe demasiado; las fases 1, 3 y 4 quedan validas igual.

### Criterio de cierre

- `ReservationSchedulerImpl` deja de depender de DAOs;
- `@Transactional` vive en el service, no en el scheduler;
- la wiki de reservas y el codigo vuelven a contar la misma historia.

## Fase 3 - Higiene JDBC y cierre de `SELECT *`

### Objetivo

- eliminar los `SELECT *` remanentes para dejar la persistencia menos fragil ante cambios de esquema.

### Archivos

- `persistence/src/main/java/ar/edu/itba/paw/persistence/UserJdbcDao.java`
- `persistence/src/main/java/ar/edu/itba/paw/persistence/ImageJdbcDao.java`
- `persistence/src/main/java/ar/edu/itba/paw/persistence/RestaurantHoursJdbcDao.java`

### Pasos

1. Reemplazar cada `SELECT *` por columnas explicitas y en el orden esperado por el `RowMapper`.
2. Aprovechar para nombrar consistentemente aliases si hoy el mapper depende de nombres ambiguos.
3. Ejecutar tests del modulo `persistence` y luego la suite completa.

### Riesgo

- romper un `RowMapper` si se omite una columna usada indirectamente.

### Mitigacion

- hacer este cambio despues de cerrar scheduler y bootstrap, cuando la base funcional ya este estable;
- correr primero tests dirigidos del modulo.

### Criterio de cierre

- no quedan `SELECT *` en DAOs versionados del repo.

## Fase 4 - Limpiar examples y cerrar la pasada

### Objetivo

- eliminar referencias obsoletas y dejar verificacion final lista para entregar.

### Archivos

- `webapp/src/main/resources/mail.properties.example`
- `webapp/src/main/resources/database.properties.example`
- opcionalmente `PAW-Wiki/docs/wiki/2026-04-13_auditoria-repo-docs_v1.md` o nueva nota de auditoria si queres dejar trazabilidad documental

### Pasos

1. Reescribir `mail.properties.example` para que describa el wiring SMTP real y deje de mencionar `DevMailServiceImpl` inexistente.
2. Reescribir `database.properties.example` para que no apunte a `docs/...` inexistente en este checkout.
3. Si queres dejar evidencia documental durable, publicar una nota corta en wiki con:
   - hallazgos cerrados;
   - decision tomada sobre scheduler;
   - fecha de verificacion final.
4. Ejecutar verificacion total:

```bash
mvn clean test
mvn -pl webapp -am package -DskipTests
```

5. Smoke manual minimo:
   - login reviewer;
   - login owner;
   - login admin;
   - navegar a las rutas documentadas en README;
   - confirmar que owner entra a `/my-restaurants` con restaurante seed visible;
   - confirmar que el flujo de reservas no rompio con el refactor del scheduler.

### Criterio de cierre

- no quedan ejemplos que mencionen clases o docs inexistentes;
- build, tests y package quedan verdes;
- README, wiki operativa y app real quedan alineados.

## Orden exacto de implementacion

1. Fase 0 - Baseline y criterio de cierre
2. Fase 1 - Documentacion operativa y bootstrap real por rol
3. Fase 2 - Alinear scheduler de reservas con la wiki
4. Fase 3 - Higiene JDBC y cierre de `SELECT *`
5. Fase 4 - Limpiar examples y cerrar la pasada

## Verificacion por fase

| Fase | Verificacion minima |
| --- | --- |
| 0 | `mvn clean test` + `mvn -pl webapp -am package -DskipTests` |
| 1 | smoke de login reviewer/owner/admin + contraste README vs rutas reales |
| 2 | tests `services` del scheduler/service + smoke rapido de reservas |
| 3 | `mvn -pl persistence test` y luego suite completa |
| 4 | suite completa + WAR + smoke final |

## Estado final

- [x] Confirmada la decision de scheduler: alinear codigo a wiki.
- [x] Ejecutada Fase 1 con cierre documental/operativo del hallazgo de bootstrap.
- [x] Ejecutada Fase 2 sin mezclarla conceptualmente con el cleanup de docs/examples.
- [x] Cerrada la pasada con README, examples, tests, package y smoke manual.
