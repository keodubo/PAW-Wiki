# Log del Wiki PAW

Registro cronologico de actividad del wiki.

---

## [2026-04-13] synth | AOP reclasificado como soporte de TP1

- Actualizadas `wiki/resumen-apuntes.md`, `wiki/spring-aop.md` y `wiki/tp1-vs-tpe2-final.md` para dejar explicito que la unidad 8 si entra en TP1 como soporte de implementacion
- La nota editorial ahora aclara que AOP importa por proxies y `@Transactional`, no por ampliar el alcance funcional del trabajo
- Actualizado `index.md` para mover `spring-aop` al bloque de conceptos y dejar `TPE2 / Final` reservado para unidades 9-11

## [2026-04-13] synth | paginas viejas de auditoria marcadas como historicas

- Actualizadas `wiki/auditoria-extrema-cumplimiento-paw.md`, `wiki/plan-ejecucion-remediacion-auditoria.md` y `wiki/remediacion-violaciones-paw.md` para dejar un banner explicito de `historico` y evitar que se lean como estado actual
- Las tres paginas ahora redirigen a `wiki/2026-04-13_auditoria-repo-docs_v1.md` como referencia vigente

## [2026-04-13] synth | auditoria repo contra docs

- Creada `wiki/2026-04-13_auditoria-repo-docs_v1.md` como auditoria canonica del estado real del repo frente a `docs/`
- Clasificadas las reglas documentadas en `Verified`, `Mismatch`, `Regression / unresolved known issue` y `Documentation ambiguity`
- Actualizado `index.md` para registrar la nueva sintesis y rebajar a historico el wording de las paginas de auditoria previa
- `docs/raw/` se mantiene intacto en esta pasada

## [2026-04-13] synth | normalizacion de fuentes y ruta de estudio

- Normalizado el tono editorial entre `wiki/resumen-apuntes.md`, `wiki/resumen-notas.md` y `wiki/resumen-transcripciones-clases-2-a-4.md`
- Las tres fuentes ahora explicitan mejor su posicion en el wiki: teorica principal, practica principal y auxiliar de menor confianza
- Creada `wiki/tp1-vs-tpe2-final.md` como sintesis canonica de prioridad de estudio
- Actualizado `index.md` para registrar la nueva pagina en Sintesis
- Cerrados backlinks entre la nueva ruta de estudio y las tres paginas fuente, mas paginas relacionadas de MVC/modulos/formularios

## [2026-04-13] synth | fase 3 de remediacion marcada como resuelta

- Actualizada `wiki/plan-ejecucion-remediacion-auditoria.md` para marcar la Fase 3 (`Hardening de auth`) como resuelta
- La nota de estado ahora refleja remember-me sin fallback inseguro, cambio de idioma limitado a sesion/request y setup local alineado con `security.properties.example`
- Actualizado `index.md` para dejar visible que las fases 1-3 ya quedaron resueltas y que restan las fases 4 y 5

## [2026-04-13] synth | fase 5 registrada como resuelta por packaging

- Actualizada `wiki/plan-ejecucion-remediacion-auditoria.md` para marcar la Fase 5 (`Packaging y README`) como resuelta
- La nota de estado deja explicito que el cierre registrado corresponde al cleanup de packaging Maven y a la verificacion del WAR sin librerias de test
- `README.md` queda diferido fuera de esta pasada editorial, segun decision operativa del trabajo actual
- Actualizado `index.md` para reflejar el nuevo estado visible del plan

## [2026-04-13] synth | auditoria extrema de cumplimiento

- Creada `wiki/auditoria-extrema-cumplimiento-paw.md` como nueva fuente de verdad del plan de auditoria y remediacion
- La pagina fija el baseline del repo al `2026-04-13` con `mvn clean test` y `mvn clean package` en verde
- Consolidada una matriz inicial de hallazgos confirmados por dominio, severidad y fase de cierre
- Actualizado `index.md` para registrar la nueva sintesis y reclasificar `raw/2026-04-13_paw-remediacion-hallazgos-secuencial_v1.md` como antecedente historico ya absorbido

## [2026-04-13] synth | plan de ejecucion de remediacion de auditoria

- Creada `wiki/plan-ejecucion-remediacion-auditoria.md` como pagina canonica para el orden de implementacion y seguimiento por fase
- La pagina consolida el plan secuencial completo de la auditoria y explicita dependencias entre fases para minimizar conflictos
- Registrado el estado actual: Fase 1 y Fase 2 resueltas; Fases 3, 4 y 5 pendientes
- Actualizado `index.md` para dejar visible la nueva sintesis junto a la auditoria extrema

## [2026-04-13] ingest | apuntes.txt (tercera pasada: notas + unidades 8-11)

- Hecha una pasada chica sobre `wiki/resumen-notas.md` para alinearla mejor con las paginas nuevas de MVC/AOP y con ejemplos practicos de la cursada
- Ingeridas las unidades 8-11 de `raw/apuntes.txt` en paginas separadas y explicitamente marcadas como **fuera de TP1**:
  - `wiki/spring-aop.md`
  - `wiki/hibernate-jpa.md`
  - `wiki/single-page-applications.md`
  - `wiki/api-rest.md`
- Actualizado `wiki/resumen-apuntes.md` para dejar enlazadas las unidades 8-11 sin mezclar foco con TP1
- Actualizado `index.md` con una seccion `TPE2 / Final` para que el material futuro no quede mezclado con conceptos nucleares del TP1
- Cerrados backlinks con paginas relacionadas (`transactional`, `persistencia-jdbc`, `logging`, `manejo-excepciones`, `spring-web-mvc`, `internacionalizacion`, `ux-flows`, `http-y-sesiones`, `manejo-imagenes`, `configuracion-maven`, `inyeccion-dependencias`)

## [2026-04-13] ingest | apuntes.txt (segunda pasada TP1)

- Auditada la ingesta de `raw/apuntes.txt` / `raw/pdfs/PAW - Apuntes.pdf`; se confirmo que `wiki/resumen-apuntes.md` habia quedado demasiado sintetico para unidades 1-7
- Reescrita `wiki/resumen-apuntes.md` para cubrir mejor Scrum, HTTP/sesiones, JSP/JSTL, Spring MVC, Maven, testing, formularios, i18n, seguridad y logging
- Creadas 2 paginas nuevas para cubrir huecos relevantes del TP1:
  - `wiki/http-y-sesiones.md`
  - `wiki/spring-web-mvc.md`
- Paginas ampliadas con contenido faltante de unidades 1-7:
  - `wiki/scrum-metodologia.md` — workflow, AirBnB y MVP
  - `wiki/jsp-jstl.md` — usabilidad, CSS, UI kit vs framework, directivas JSP y familias JSTL
  - `wiki/configuracion-maven.md` — lifecycle, archetypes, `groupId`/`artifactId`, `SNAPSHOT`, herencia y `dependencyManagement`
  - `wiki/inyeccion-dependencias.md` — separacion entre configuracion y uso, contexto web y reemplazo de defaults
  - `wiki/testing-unitario.md` — JUnit/TestNG, stubs/mocks/fakes, nice mocks y casos "no deberia fallar"
  - `wiki/validacion-formularios.md`, `wiki/internacionalizacion.md`, `wiki/ux-flows.md` — feedback de formularios, fallback de locales, mensajes por campo y criterio UI de la cursada
- Actualizado `index.md` para registrar las nuevas paginas y dejar trazada la segunda pasada editorial sobre `PAW - Apuntes.pdf`
- Ajustados backlinks en paginas relacionadas (`spring-security`, `mailing`, `manejo-imagenes`, `modelo-capas`, `auth-flows`, `ux-flows`)

## [2026-04-13] lint | consistencia editorial del wiki

- Cerrados los backlinks faltantes detectados por el lint para volver a cumplir la regla de reciprocidad entre paginas
- `index.md` ahora registra `wiki/resumen-notas-sprint-1.md` y explicita el estado de todas las fuentes reales presentes en `docs/raw/`
- Reconciliadas las reglas editoriales de `403/404` para tratarlas como regla base con excepciones de endpoint, en vez de afirmaciones absolutas contradictorias
- Reconciliada la evolucion del esquema: ejemplo inicial con `schema.sql` versus estado final del repo con `schema-base.sql` + scripts dialectales
- Actualizadas paginas afectadas (`spring-security`, `manejo-excepciones`, `auth-flows`, `testing-unitario`, `persistencia-jdbc`, `resumen-notas`, `remediacion-violaciones-paw` y backlinks asociados)

## [2026-04-13] ingest | correcciones.md (segunda pasada exhaustiva)

- Reescrita `wiki/resumen-correcciones.md` para que la ingesta de `raw/correcciones.md` quede realmente exhaustiva y no solo resumida
- Creadas 3 paginas concepto nuevas para cubrir clusters antes difusos u omitidos:
  - `wiki/auth-flows.md`
  - `wiki/java-style.md`
  - `wiki/ux-flows.md`
- Paginas ampliadas para absorber huecos de la fuente original:
  - `wiki/spring-security.md` — flujo completo de autenticacion y backlink a `auth-flows`
  - `wiki/mailing.md` — notificaciones funcionales y backlinks a `auth-flows` / `ux-flows`
  - `wiki/jsp-jstl.md` — uso de taglib de Spring Security para UI condicionada
  - `wiki/buenas-practicas.md` — elogios faltantes sobre imagenes y calidad de interfaz
  - `wiki/internacionalizacion.md`, `wiki/validacion-formularios.md`, `wiki/configuracion-maven.md`, `wiki/logica-en-controllers.md`, `wiki/persistencia-jdbc.md`, `wiki/manejo-excepciones.md` — backlinks y fecha de actualizacion
- Actualizado `index.md` para registrar las 3 paginas nuevas y reclasificar `resumen-correcciones.md` como ingesta exhaustiva
- Objetivo editorial: que los hallazgos de `raw/correcciones.md` queden cubiertos sin perder detalle entre resumen y paginas derivadas

## [2026-04-10] synth | remediacion-violaciones-paw

- Creada `wiki/remediacion-violaciones-paw.md` como sintesis canonica del plan de remediacion
- Reclasificado el plan original segun el estado real del repo: fase 1 parcial; fases 2, 3 y 4 resueltas; fase 5 pendiente
- Actualizada la sintesis tras cerrar fase 4 en codigo: escape de `comingSoon` en `restaurant.jsp` y cobertura reforzada en `ViewTemplateSecurityTest`
- Actualizada la sintesis tras cerrar fase 5 en codigo: `RestaurantSearchForm` ahora usa claves i18n y la validacion tiene cobertura dedicada
- Actualizado `index.md` para registrar la nueva pagina en Sintesis
- Agregados backlinks desde `wiki/resumen-correcciones.md`, `wiki/xss-prevencion.md`, `wiki/internacionalizacion.md`, `wiki/persistencia-jdbc.md` y `wiki/testing-unitario.md`

## [2026-04-09] setup | Inicializacion del wiki

- Creada estructura de directorios: `raw/`, `wiki/`, `raw/pdfs/`, `raw/assets/`
- Migradas fuentes existentes desde `docs/tmp/` a `docs/raw/`
- Creado schema (`CLAUDE.md`), indice (`index.md`) y este log
- Fuentes disponibles para ingesta:
  - `correcciones.md` (26KB) -- feedback de correcciones previas
  - `enunciado.txt` (6KB) -- especificacion del TPE1
  - `apuntes.txt` (143KB) -- apuntes de la materia
  - `notas.txt` (175KB) -- notas de clases
  - 3 PDFs originales en `raw/pdfs/`

## [2026-04-09] ingest | correcciones.md

- Fuente: `raw/correcciones.md` (26KB, ~265 lineas)
- Contenido: recopilacion integra de errores, falencias y virtudes de correcciones previas de PAW
- Paginas creadas (14 total):
  - `wiki/resumen-correcciones.md` (fuente) — resumen estructurado por severidad
  - `wiki/spring-security.md` (concepto) — control de acceso declarativo
  - `wiki/xss-prevencion.md` (concepto) — escapado con c:out, sanitizacion LIKE
  - `wiki/logica-en-controllers.md` (concepto) — anti-patron de logica en controllers
  - `wiki/transactional.md` (concepto) — uso de @Transactional
  - `wiki/n-plus-1-joins-java.md` (concepto) — anti-patron N+1 queries
  - `wiki/testing-unitario.md` (concepto) — criterios de evaluacion de tests
  - `wiki/validacion-formularios.md` (concepto) — Bean Validation y custom validators
  - `wiki/persistencia-jdbc.md` (concepto) — patrones JDBC, RowMappers
  - `wiki/manejo-excepciones.md` (concepto) — ControllerAdvice y custom exceptions
  - `wiki/mailing.md` (concepto) — arquitectura de email
  - `wiki/configuracion-maven.md` (concepto) — multi-modulo y dependencias
  - `wiki/internacionalizacion.md` (concepto) — i18n
  - `wiki/logging.md` (concepto) — SLF4J/Logback
  - `wiki/buenas-practicas.md` (sintesis) — patrones elogiados por la catedra
- Indice actualizado con 15 paginas

## [2026-04-09] ingest | enunciado.txt

- Fuente: `raw/enunciado.txt` (6KB, 122 lineas)
- Contenido: especificacion oficial del TPE1 — stack, requerimientos, calendario, evaluacion
- Paginas creadas (3):
  - `wiki/resumen-enunciado.md` (fuente) — resumen estructurado del enunciado
  - `wiki/criterios-evaluacion.md` (concepto) — penalizaciones por area y prioridades
  - `wiki/calendario-entregas.md` (concepto) — fechas de sprints y entrega final
- Paginas actualizadas (3):
  - `wiki/testing-unitario.md` — agregada penalizacion de -1 punto
  - `wiki/configuracion-maven.md` — agregados requisitos de entrega (war, README)
  - `wiki/resumen-correcciones.md` — cross-links a enunciado
- Total paginas wiki: 18

## [2026-04-09] ingest | apuntes.txt

- Fuente: `raw/apuntes.txt` (143KB, 21521 lineas)
- Contenido: apuntes completos de PAW — 11 unidades, de las cuales 1-7 son relevantes para TPE1
- Unidades procesadas: 1 (Scrum), 2 (HTTP/mailing/imagenes), 3 (JSP/JSTL), 4 (Spring/Maven/capas/JDBC), 5 (Testing), 6 (Formularios/i18n), 7 (Spring Security/Logging)
- Unidades omitidas (TPE2): 8 (AOP), 9 (Hibernate), 10 (SPA), 11 (REST API)
- Paginas creadas (6):
  - `wiki/resumen-apuntes.md` (fuente) — resumen de las 11 unidades
  - `wiki/modelo-capas.md` (concepto) — modelo de capas DDD con scopes Maven
  - `wiki/jsp-jstl.md` (concepto) — tags JSTL y Spring form tags
  - `wiki/inyeccion-dependencias.md` (concepto) — Spring DI
  - `wiki/manejo-imagenes.md` (concepto) — almacenamiento y upload de imagenes
  - `wiki/scrum-metodologia.md` (concepto) — proceso Scrum para el curso
- Paginas actualizadas (8):
  - `wiki/spring-security.md` — UserDetailsService, PawAuthUser, BCrypt, roles como Set
  - `wiki/mailing.md` — Thymeleaf templates, @Async, regla de capa
  - `wiki/persistencia-jdbc.md` — JdbcTemplate, RowMapper como interfaz funcional
  - `wiki/logging.md` — Logback, arbol jerarquico, appenders
  - `wiki/testing-unitario.md` — HSQLDB setup, @Mock/@InjectMocks, estructura de test
  - `wiki/validacion-formularios.md` — flujo completo con form beans y Spring form tags
  - `wiki/internacionalizacion.md` — MessageSource, LocaleResolver
  - `wiki/configuracion-maven.md` — fuente agregada
- Total paginas wiki: 24

## [2026-04-09] ingest | notas.txt

- Fuente: `raw/notas.txt` (175KB, 633 lineas)
- Contenido: notas clase a clase con walkthroughs practicos, comandos reales y advertencias del profesor
- Clases procesadas: Clase 2 (Maven/Spring MVC), Clase 3 (interfaces/scopes/DI), Clase 4 (JDBC/testing/Mockito), Clase 5 (formularios/Bean Validation/i18n), Clase debida (Spring Security), Clase logging, Clase AOP/@Transactional/@ControllerAdvice
- Nota: contenido refuerza apuntes teoricos pero agrega detalles practicos unicos (analogias, limitaciones de proxy, historia SLF4J, verify() penalizado)
- Paginas creadas (1):
  - `wiki/resumen-notas.md` (fuente) — resumen estructurado por clase
- Paginas actualizadas (9):
  - `wiki/validacion-formularios.md` — @ModelAttribute como I/O, Beans vs Records, groups, i18n de mensajes
  - `wiki/spring-security.md` — filtro en web.xml, login/logout automatico, RememberMe key, Expression Language
  - `wiki/logging.md` — historia SLF4J, interpolacion con {}, config dual logback-test.xml, RollingFileAppender, additivity
  - `wiki/transactional.md` — mecanismo proxy AOP, limitaciones (self-invocation, metodos privados), @Async/@Scheduled/@Cacheable
  - `wiki/manejo-excepciones.md` — @ControllerAdvice como AOP Advice, @ExceptionHandler, @assignableTypes
  - `wiki/persistencia-jdbc.md` — schema.sql compartido entre tests y app via DatabasePopulator
  - `wiki/testing-unitario.md` — JdbcTestUtils, schema compartido, verify() explicitamente prohibido
  - `wiki/internacionalizacion.md` — regla de interpolacion completa (no concatenar), spring:message arguments
  - `wiki/logica-en-controllers.md` — fuente agregada
- Total paginas wiki: 25

## [2026-04-09] lint | saneamiento del wiki

- Corregidas inconsistencias factuales sobre `schema.sql`: se aclaro la transicion entre el ejemplo inicial de tests y el setup final compartido en `src/main/resources` del modulo `persistence`
- Corregido el modelo de modulos para alinearlo con las fuentes reales de la materia: `webapp`, `services`, `interfaces`, `persistence`, `models`
- Normalizados los links de "Ver tambien" para cumplir la regla de backlinks bidireccionales definida en `CLAUDE.md`
- Unificada la nomenclatura de Spring Security (`PawAuthUser`) para evitar divergencias entre paginas

## [2026-04-09] ingest | PDFs originales

- Fuentes validadas:
  - `raw/pdfs/Enunciado_TPE1.pdf` contra `raw/enunciado.txt`
  - `raw/pdfs/Notas clases PAW.pdf` contra `raw/notas.txt`
  - `raw/pdfs/PAW - Apuntes.pdf` contra `raw/apuntes.txt`
- Resultado: no se detectaron diferencias sustantivas tras normalizar espacios, ligaduras y puntuacion tipografica
- Decision editorial: no crear paginas fuente duplicadas; se registran los PDFs como originales canonicos de las ingestas ya existentes
- Paginas actualizadas:
  - `wiki/resumen-enunciado.md` — agregado PDF original a frontmatter y nota de validacion
  - `wiki/resumen-notas.md` — agregado PDF original a frontmatter y nota de validacion
  - `wiki/resumen-apuntes.md` — agregado PDF original a frontmatter y nota de validacion
  - `index.md` — PDFs marcados como procesados y vinculados a sus fuentes de texto

## [2026-04-09] lint | segunda pasada de afirmaciones fuertes

- Matizadas formulaciones demasiado absolutas en `wiki/transactional.md` y `wiki/spring-security.md` para alinearlas mejor con el criterio de la catedra y no presentarlas como verdades universales
- Corregida una inconsistencia menor en `wiki/logging.md`: produccion ahora queda explicitamente alineada con `WARNING` en vez de `INFO`

## [2026-04-09] synth | paginas de comparacion

- Creadas 4 paginas tipo `comparacion` para cubrir huecos del indice:
  - `wiki/comparacion-seguridad-web.md`
  - `wiki/comparacion-testing-servicios-y-daos.md`
  - `wiki/comparacion-capas-web-services-persistence.md`
  - `wiki/comparacion-jsp-formularios-e-i18n.md`
- Actualizado `index.md` para dejar de tener la seccion Comparaciones vacia
- Agregados backlinks en paginas conceptuales relacionadas para mantener reciprocidad

## [2026-04-09] ingest | transcripciones de audio (revision cautelosa)

- Fuentes revisadas:
  - `raw/audio_transcript/audio_transcript clase 2.VTT`
  - `raw/audio_transcript/audio_transcript clase 3.VTT`
  - `raw/audio_transcript/audio_transcript clase 4.VTT`
- Resultado editorial:
  - Clases 2 y 3 sirven como corroboracion parcial de ideas ya presentes en fuentes mas confiables
  - La "clase 4" corresponde a una clase mas reciente sobre prompteo, agentes y formularios; no se mezcla con la cronologia clasica de `resumen-notas`
- Decision: crear `wiki/resumen-transcripciones-clases-2-a-4.md` como fuente auxiliar de baja confianza, sin contaminar el resto del wiki con afirmaciones dudosas

## [2026-04-09] lint | reclasificacion de clase 4 VTT

- Aclarado contexto: `raw/audio_transcript/audio_transcript clase 4.VTT` no es una clase "desalineada", sino una clase mas actual centrada en agentes, prompting y formularios web
- Ajustada la interpretacion editorial: el riesgo principal pasa a ser el reconocimiento de voz (ASR), no una contradiccion con las notas historicas del curso
- Actualizados `wiki/resumen-transcripciones-clases-2-a-4.md` e `index.md` para reflejar esta reclasificacion
