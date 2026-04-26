---
titulo: Auditoria de Implementacion contra PAW-Wiki
tipo: sintesis
fuentes: [raw/enunciado.txt, raw/correcciones_tp1.md, raw/apuntes.txt, raw/specs_reserva_v2.txt, CLAUDE.md, README.md, pom.xml]
creado: 2026-04-24
actualizado: 2026-04-24
---

# Auditoria de Implementacion contra PAW-Wiki

## Resumen ejecutivo

Veredicto: el proyecto esta en buen estado operativo y compila/empaqueta correctamente, pero **no cumple al 100% con la wiki y sus convenciones**. Las capas Maven, la suite de tests, los contratos principales de Spring Security, i18n, JDBC, mail y errores estan mayormente alineados. Las brechas reales estan concentradas en reglas HTTP/seguridad, performance de reservas, configuracion de contextos Spring, deuda de JSP/CSS y empaquetado de configuracion sensible.

Prioridad recomendada:

1. Corregir `GET /lang/{language}` para que no persista locale en DB.
2. Eliminar el N+1 de mesas reasignables en la vista owner de reservas.
3. Definir estrategia de configuracion para que el WAR no transporte secretos locales.
4. Resolver el flujo de cambio de contrasena con contrasena actual.
5. Reducir deuda de estilos inline en JSPs.

## Alcance y metodologia

Fuentes canonicas leidas:

- `CLAUDE.md`
- `PAW-Wiki/docs/CLAUDE.md`
- `PAW-Wiki/docs/index.md`
- Paginas wiki de enunciado, capas, Maven, Spring MVC, Spring Security, auth, i18n, JSP/JSTL, XSS, validacion, excepciones, JDBC, N+1, transactional, AOP, mailing, logging, testing y plan de reservas.

Comandos de verificacion ejecutados:

```bash
mvn clean test
mvn clean package
jar tf webapp/target/webapp.war
rg -n "System\.out|printStackTrace" models service-contracts persistence-contracts persistence services webapp/src/main
```

Resultado:

- `mvn clean test`: **BUILD SUCCESS**, 271 tests, 0 failures, 0 errors, 0 skipped.
- `mvn clean package`: **BUILD SUCCESS**, WAR generado en `webapp/target/webapp.war`.
- WAR: no se detectaron librerias de test (`junit`, `mockito`, `spring-test`, `hsqldb`) empaquetadas.
- WAR: se detectaron `database.properties`, `mail.properties` y `security.properties` reales empaquetados junto con sus `.example`.
- Logging: no se detectaron `System.out` ni `printStackTrace` en `src/main`.

No se ejecuto smoke HTTP con Jetty/navegador en esta pasada.

## Checklist de cumplimiento

| Area wiki | Estado | Evidencia |
| --- | --- | --- |
| Stack obligatorio Java 21, Spring MVC, JSP/JSTL, JDBC, PostgreSQL, Maven | Cumple | `resumen-enunciado.md:19-28`; build Maven exitoso |
| Multi-modulo y scopes runtime | Cumple con variante | 6 modulos reales; `webapp` usa `services` y `persistence` en `runtime` (`webapp/pom.xml:30-39`) |
| `dependencyManagement` centralizado y deps test en `test` | Cumple | `configuracion-maven.md:21-27`, `webapp/pom.xml:84-123`; WAR sin libs test |
| Controllers sin SQL y validacion con forms/validators | Mayormente cumple | Formularios `@Valid`, `BindingResult` y advice centralizado; no se encontro `java.sql` en webapp |
| Servicios con transacciones y scheduler fuera de test | Cumple | Metodos service con `@Transactional`; `BackgroundExecutionConfig` usa perfil no-test |
| Persistencia JDBC aislada | Cumple | SQL/RowMappers en `persistence`; queries principales con `JOIN`, `ORDER BY`, `LIMIT/OFFSET` |
| Seguridad declarativa | Mayormente cumple | `WebAuthConfig` + `AccessHelper`; no `@PreAuthorize` disperso en controllers |
| Login/logout/remember-me | Cumple | Login/logout manejados por Spring Security; remember-me key de config |
| i18n | Mayormente cumple | 737 claves simetricas en default/ES/EN; falla por side effect de locale en GET |
| JSP sin scriptlets y URLs con `<c:url>` | Cumple | `ViewTemplateSecurityTest` recorre templates y prohibe scriptlets reales (`ViewTemplateSecurityTest.java:676-690`) |
| JSP sin estilos inline salvo excepciones puntuales | No cumple | 463 apariciones de `style=` en `WEB-INF` |
| XSS dinamico | Sin hallazgo critico | Tests y escaneos cubren `<c:out>`, `fn:escapeXml`, no scriptlets; no prueba formal total |
| Mailing en services, async y locale destinatario | Cumple | Templates en `services/src/main/resources/mail-templates`, `@Async`, locale por destinatario |
| Logging | Cumple en scan base | Sin `System.out` / `printStackTrace` en main |
| README con niveles de acceso | Cumple documentalmente | `README.md:154-191` cubre niveles y notas de deploy sin copiar valores aqui |
| Wiki workflow | Cumple con riesgo operativo | Esta pagina actualiza index/log/tree, pero `PAW-Wiki/` esta ignorado por git |

## Hallazgos

### P1 - `GET /lang/{language}` persiste cambios en DB

La wiki marca como mala practica que reglas de sesion produzcan side effects en GET, especialmente persistir locale en DB (`auth-flows.md:42-45`, `internacionalizacion.md:22-25`). La implementacion actual hace exactamente eso para usuarios autenticados:

- `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/LanguageController.java:29-46`
- `webapp/src/test/java/ar/edu/itba/paw/webapp/LanguageControllerMvcTest.java:58-102`

Riesgo: un link, refresh, crawler o redireccion de idioma cambia estado persistente de cuenta. Ademas, los tests actuales protegen el comportamiento contrario a la wiki.

Recomendacion: dejar `GET /lang/{language}` solo como cambio de sesion. Si se quiere preferencia persistente, moverla a `POST` de perfil/configuracion con CSRF y test nuevo que garantice que GET no escribe.

Rollback: revertir a la version actual de `LanguageController` y tests si aparece incompatibilidad funcional.

### P1 - El WAR empaqueta configuracion real potencialmente sensible

El build empaqueta estos archivos:

- `WEB-INF/classes/database.properties`
- `WEB-INF/classes/mail.properties`
- `WEB-INF/classes/security.properties`
- y sus `.example`

Los archivos reales existen localmente en `webapp/src/main/resources/`. No se inspecciono ni se copia su contenido en esta auditoria. La wiki de auth indica que credenciales/secretos no deben vivir en el repo (`auth-flows.md:23-30`); aunque estan ignorados por git, **si quedan dentro del WAR** pasan a formar parte del artefacto distribuible.

Riesgo: compartir/subir el WAR puede exponer credenciales de DB, SMTP o remember-me key.

Recomendacion: elegir una estrategia unica:

- Recomendada: empaquetar solo `.example` y cargar config real por archivo externo/JVM properties/env del contenedor.
- Alternativa de catedra/local: si se acepta config dentro del WAR para deploy academico, documentar explicitamente que el WAR no debe compartirse y generar artefactos por entorno.

Rollback: restaurar el empaquetado actual si el contenedor academico exige properties en classpath.

### P2 - N+1 en mesas reasignables de reservas owner

La wiki prohibe iterar filas y hacer N accesos a DB para armar relaciones (`n-plus-1-joins-java.md:11-37`). La vista owner lista reservas y luego resuelve mesas reasignables por cada reserva pendiente:

- `OwnerReservationPageSupport.java:45-57` arma la pagina y agrega `reassignableTablesByReservationId`.
- `OwnerReservationPageSupport.java:67-72` itera reservas y llama `reservationService.listReassignableTables(...)` por reserva pendiente.
- `ReservationServiceImpl.java:312-315` delega una query.
- `RestaurantTableJdbcDao.java:153-172` ejecuta la query para una reserva.

Riesgo: la pagina escala con `1 + cantidad_de_pending` queries, justo en una pantalla operativa de owner.

Recomendacion: agregar DAO batch `findReassignableForReservations(List<Long>)` con `WHERE res.id IN (...)` y devolver `Map<Long, List<RestaurantTable>>`; cubrir con test de conteo de queries.

Rollback: mantener el metodo unitario como fallback y usarlo solo cuando la lista tenga un unico elemento.

### P2 - Cambio de contrasena no pide contrasena actual

La wiki espera cambio de contrasena con contrasena actual y confirmacion (`auth-flows.md:31-38`). El flujo actual de perfil emite un token de reset y redirige al reset:

- `ProfileController.java:138-145`
- `PasswordResetForm.java:9-18` solo pide nueva contrasena y confirmacion.

Riesgo: se mezcla "cambio autenticado" con "recuperacion por token". El usuario autenticado puede cambiar password sin reautenticacion por contrasena actual, lo que no coincide con la convencion de la materia.

Recomendacion: crear `ChangePasswordForm` con `currentPassword`, `password`, `passwordConfirm`; validar hash actual en servicio. Mantener el reset por token solo para recuperacion.

Rollback: conservar el endpoint de token como fallback temporal hasta migrar UI/tests.

### P2 - Contexto web cargado en root Spring context

La wiki describe `ContextLoaderListener` como contexto global y `DispatcherServlet`/`WebConfig` como configuracion web MVC (`spring-web-mvc.md:20-31`). En el repo, `WebConfig` y `WebAuthConfig` se cargan en el root context:

- `webapp/src/main/webapp/WEB-INF/web.xml:13-21`
- El `DispatcherServlet` queda con `contextConfigLocation` vacio: `web.xml:88-95`.

Riesgo: funciona, pero desdibuja el limite root/web y dificulta defender el setup contra la convencion teorica de la wiki. No es una falla de runtime verificada.

Recomendacion: decidir si se alinea a contexto root + servlet child. Si se alinea, dejar root con persistence/services/background/security compartida y mover `WebConfig` MVC al servlet context. Si se conserva el setup actual, documentar la excepcion.

Rollback: volver a cargar `WebConfig` en root si aparece conflicto con Spring Security o tests MVC.

### P2 - Deuda masiva de estilos inline en JSP

La wiki acepta CSS como base principal y pide evitar estilos inline salvo excepciones puntuales (`jsp-jstl.md:13-19`). En `WEB-INF` hay 463 apariciones de `style=` en JSP/JSPF/tag files. Ejemplos concentrados aparecen en:

- `webapp/src/main/webapp/WEB-INF/views/restaurant.jsp`
- `webapp/src/main/webapp/WEB-INF/views/error/*.jsp`
- `webapp/src/main/webapp/WEB-INF/tags/header.tag`
- `webapp/src/main/webapp/WEB-INF/tags/adminHeader.tag`

Riesgo: no rompe seguridad directamente, pero degrada mantenibilidad, consistencia visual y cumplimiento de convencion de cursada. Algunos tests incluso fijan fragmentos de estilo inline, por lo que la deuda quedo parcialmente institucionalizada.

Recomendacion: extraer estilos por clusters a `forkd.css`/clases compartidas. Priorizar header/adminHeader, error pages y `restaurant.jsp`.

Rollback: cambios CSS son reversibles por archivo; mantener diffs chicos por pantalla.

### P3 - Referencia documental rota en `LegacySchemaGuard`

`LegacySchemaGuard` muestra una ruta de docs que no existe en el repo visible:

- `persistence/src/main/java/ar/edu/itba/paw/persistence/LegacySchemaGuard.java:81-92`
- `persistence/src/test/java/ar/edu/itba/paw/persistence/LegacySchemaGuardTest.java:75-78`

Riesgo: ante schema local incompatible, el mensaje manda a una pagina ausente.

Recomendacion: cambiar la referencia a una pagina real del wiki o al bloque vigente de `CLAUDE.md`/README.

Rollback: revertir solo el texto y el assert.

### P3 - `PAW-Wiki/` esta ignorado por git

El flujo del wiki exige actualizar `index.md`, `log.md` y `tree.txt` cuando se crean paginas (`PAW-Wiki/docs/CLAUDE.md:66-80`). Esta auditoria lo hace, pero `.gitignore` ignora `/PAW-Wiki`:

- `.gitignore:67-69`

Riesgo: los cambios del wiki quedan locales/ignorados salvo `git add -f` o cambio de politica. Para trabajo compartido, puede parecer que la auditoria no existe.

Recomendacion: si el wiki debe versionarse en este repo, dejar de ignorar `PAW-Wiki/` o documentar `git add -f PAW-Wiki/docs/...`.

Rollback: mantener el ignore si el wiki se versiona en un repositorio separado.

## Fortalezas confirmadas

- Arquitectura Maven multi-modulo coherente con variante explicita de contratos separados.
- `webapp` compila contra contratos/modelos y carga `services`/`persistence` en runtime.
- Suite de tests amplia y verde: modelos, persistencia, servicios y webapp.
- JSPs privados bajo `WEB-INF`; no se encontraron scriptlets reales.
- Seguridad declarativa centralizada con Spring Security y helper de ownership.
- Validacion con form beans, `@Valid`, custom validators y errores inline.
- `ErrorHandlingAdvice` centraliza 400/403/404/409/413/415/500/503 y casos recuperables.
- Persistencia usa JDBC aislado, RowMappers, schema base + dialecto y queries principales con JOIN/paginacion.
- Mailing vive en `services`, con Thymeleaf, `@Async` y locale del destinatario.
- i18n tiene bundles simetricos para default/ES/EN.
- Logging base sin `System.out` ni `printStackTrace` en main.

## Ambiguedades/documentacion

- El canon de la catedra habla de un modulo `interfaces`; el repo lo divide en `service-contracts` y `persistence-contracts`. La wiki ya reconoce que separar mas es opcional (`modelo-capas.md:27-44`, `configuracion-maven.md:54-62`). No lo trato como incumplimiento.
- La wiki menciona JSPs bajo `WEB-INF/jsp/`; el repo usa `WEB-INF/views/`. Al estar bajo `WEB-INF`, cumple privacidad. Es una diferencia nominal, no una falla.
- README incluye credenciales por nivel de acceso y tambien aclara que reviewer/owner demo no son bootstrap automatico. Eso cumple el requisito documental de entrega, pero no prueba disponibilidad en una base limpia.
- La auditoria no valida manualmente UX/browser ni un deploy real con PostgreSQL limpio.

## Plan de remediacion recomendado

1. Locale GET sin DB write:
   - Quitar `userService.updatePreferredLocale(...)` de `LanguageController`.
   - Ajustar `LanguageControllerMvcTest` para afirmar que GET autenticado no persiste.
   - Agregar POST futuro si se quiere preferencia persistente.

2. Batch de mesas reasignables:
   - Agregar metodo batch en contrato DAO.
   - Implementar query con `res.id IN (...)`.
   - Cambiar `OwnerReservationPageSupport` para llamar una vez por pagina.
   - Testear cantidad de queries.

3. Configuracion sensible fuera del WAR:
   - Definir si el entorno academico exige properties en classpath.
   - Si no lo exige, excluir reales del WAR y cargar externos.
   - Mantener `.example` empaquetables/documentales.

4. Cambio de contrasena autenticado:
   - Crear form separado.
   - Validar password actual en service.
   - Mantener reset por token para forgot-password.

5. Spring context split:
   - Decidir si se prioriza alineacion teorica o estabilidad actual.
   - Si se alinea, mover `WebConfig` al DispatcherServlet context y correr toda la suite.

6. CSS/JSP cleanup:
   - Migrar inline styles por pantalla a clases.
   - Evitar refactor masivo; hacerlo por clusters verificables.

7. Docs/wiki:
   - Corregir link roto de `LegacySchemaGuard`.
   - Decidir politica git de `PAW-Wiki/`.

## Ver tambien

- [[resumen-enunciado]]
- [[modelo-capas]]
- [[configuracion-maven]]
- [[spring-web-mvc]]
- [[auth-flows]]
- [[internacionalizacion]]
- [[jsp-jstl]]
- [[n-plus-1-joins-java]]
- [[plan-implementacion-reservas]]
- [[2026-04-13_auditoria-repo-docs_v1]]
- [[2026-04-24_correcciones-sprint-2_v1]]
