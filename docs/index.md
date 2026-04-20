# Indice del Wiki PAW

Catalogo de todas las paginas del wiki, organizado por categoria.

---

## Fuentes

| Pagina | Descripcion | Origen |
|--------|-------------|--------|
| [[resumen-correcciones]] | Ingesta exhaustiva de errores, aciertos y observaciones recurrentes de correcciones previas | `raw/correcciones.md` |
| [[resumen-enunciado]] | Especificacion oficial del TPE1: stack, requerimientos, entrega | `raw/enunciado.txt` + `raw/pdfs/Enunciado_TPE1.pdf` |
| [[resumen-apuntes]] | Apuntes completos de la materia (11 unidades; 1-7 como base TP1 y 8 como soporte de implementacion) | `raw/apuntes.txt` + `raw/pdfs/PAW - Apuntes.pdf` |
| [[resumen-notas]] | Notas de clase con walkthroughs practicos y advertencias del profesor | `raw/notas.txt` + `raw/pdfs/Notas clases PAW.pdf` |
| [[resumen-notas-sprint-1]] | Feedback puntual del sprint 1 sobre producto, UX, filtros y direccion del proyecto | `raw/notas_sprint_1.txt` |
| [[resumen-notas-sprint-2]] | Checklist de cierre del sprint 2: auth, owner/admin, UX, correcciones ya resueltas y checks manuales remanentes | `raw/notas_para_sprint_2.txt` |
| [[resumen-transcripciones-clases-2-a-4]] | Revision cautelosa de transcripciones VTT; clases 2-3 como apoyo y clase reciente sobre agentes/formularios | `raw/audio_transcript/*.VTT` |
| [[resumen-spec-reservas]] | Ingesta del spec funcional del sistema de reservas v2 (vigente) con diff frente a v1, reglas, modalidades, slots, estados y resoluciones completas de ambiguedades v2 | `raw/specs_reserva_v2.txt` + `raw/reservas/spec-funcional-reservas.md` (historico) + `raw/diagrama_bd.md` |

## Conceptos

| Pagina | Descripcion |
|--------|-------------|
| [[spring-security]] | Control de acceso declarativo, ownership, UserDetailsService, BCrypt |
| [[auth-flows]] | Login, logout, reset de password, verificacion y ownership como flujo completo |
| [[http-y-sesiones]] | HTTP stateless, cookies, session id, CGI y contexto historico de la web server-side |
| [[spring-web-mvc]] | DispatcherServlet, web.xml, WebConfig, ModelAndView y ViewResolver |
| [[xss-prevencion]] | Prevencion XSS con `<c:out>`, sanitizacion de LIKE |
| [[logica-en-controllers]] | Anti-patron: logica de negocio en controllers |
| [[java-style]] | Convenciones Java, Optional, enums, visibilidad y consistencia de APIs |
| [[transactional]] | Uso correcto de @Transactional en servicios |
| [[spring-aop]] | Proxies, cross-cutting concerns y limites practicos de `@Transactional` que si impactan TP1 |
| [[n-plus-1-joins-java]] | Anti-patron: N+1 queries y joins resueltos en Java |
| [[testing-unitario]] | JUnit, HSQLDB, Mockito, criterios de evaluacion |
| [[validacion-formularios]] | Bean Validation JSR380, custom validators, Spring form tags |
| [[persistencia-jdbc]] | RowMappers, JdbcInsert, JdbcTemplate, aislamiento de java.sql |
| [[manejo-excepciones]] | ControllerAdvice, custom exceptions, codigos HTTP |
| [[mailing]] | Thymeleaf templates, @Async, locale del destinatario |
| [[configuracion-maven]] | Multi-modulo, dependencyManagement, scopes, fugas de capa |
| [[internacionalizacion]] | MessageSource, messages_xx.properties, LocaleResolver |
| [[logging]] | SLF4J/Logback, appenders, niveles jerarquicos |
| [[ux-flows]] | Navegacion, microcopy, zero states, emails y calidad de interfaz |
| [[criterios-evaluacion]] | Penalizaciones por area, errores no recuperables, prioridades |
| [[calendario-entregas]] | Fechas de sprints, demos obligatorias, entrega final |
| [[modelo-capas]] | Modelo de capas DDD con scopes Maven (compile/runtime) |
| [[jsp-jstl]] | Tags JSTL core, Spring form tags, ubicacion de JSPs |
| [[inyeccion-dependencias]] | Spring DI, @Autowired, beans, @ComponentScan |
| [[manejo-imagenes]] | Almacenamiento en BD, Multipart upload, validacion |
| [[scrum-metodologia]] | Stories, backlog, estimacion, tablero, Plane |

## TPE2 / Final

| Pagina | Descripcion |
|--------|-------------|
| [[hibernate-jpa]] | ORM, JPA, EntityManager, mapeos y contexto de persistencia fuera de TP1 |
| [[single-page-applications]] | Thick client, frontend state, build, cache y optimizacion para la entrega final |
| [[api-rest]] | Recursos, verbos, hipervinculos, content negotiation y JWT para la API del final |

## Sintesis

| Pagina | Descripcion |
|--------|-------------|
| [[2026-04-13_auditoria-repo-docs_v1]] | Auditoria de cumplimiento del repo contra el canon actual de `docs/`; clasifica reglas verificadas, mismatches activos, deuda legacy y ambiguedades documentales vigentes | 
| [[tp1-vs-tpe2-final]] | Ruta de estudio para distinguir material prioritario de TP1, AOP como soporte de implementacion y temas para TPE2/final |
| [[buenas-practicas]] | Patrones elogiados por la catedra para replicar |
| [[remediacion-violaciones-paw]] | Estado actual de las violaciones detectadas y pendientes reales en el repo |
| [[auditoria-extrema-cumplimiento-paw]] | Baseline historico de la auditoria extrema levantada el 2026-04-13; conserva hallazgos originalmente detectados, varios ya remediados en el repo actual |
| [[plan-ejecucion-remediacion-auditoria]] | Plan secuencial historico de remediacion; su tabla interna de estados ya no coincide por completo con el repo actual y debe leerse junto a `[[2026-04-13_auditoria-repo-docs_v1]]` |
| [[plan-implementacion-reservas]] | Plan operativo por fases para implementar el sistema de reservas: modelo, configuracion, disponibilidad, flujos, scheduler, emails, reviews y hardening. Auditado 2026-04-17 contra las reglas del wiki (mail templates en `services/`, JOINs explicitos en listados, rechazo de reservas con slot vencido, recordatorio solo a confirmed, reassignacion sin restriccion de direccion, pluralizacion i18n, scheduler desactivado en tests) |
| [[2026-04-17_cierre-implementacion-reservas_v1]] | Registro de cierre de la implementación del sistema de reservas; resume el estado final, hardening realizado y estabilización del test suite |

## Comparaciones

| Pagina | Descripcion |
|--------|-------------|
| [[comparacion-seguridad-web]] | Autorizacion, validacion y escapado: que resuelve cada capa de seguridad |
| [[comparacion-testing-servicios-y-daos]] | Diferencia entre testear servicios, persistencia y transacciones |
| [[comparacion-capas-web-services-persistence]] | Limites reales entre webapp, services y persistence |
| [[comparacion-jsp-formularios-e-i18n]] | Relacion entre JSP, form beans, errores y textos internacionalizados |

---

### Estado de fuentes raw

- [x] `raw/correcciones.md` -- Errores y aciertos recopilados de correcciones previas (~26KB)
- [x] `raw/enunciado.txt` -- Enunciado oficial del TPE1
- [x] `raw/apuntes.txt` -- Apuntes de la materia (~143KB, 21K lineas)
- [x] `raw/pdfs/PAW - Apuntes.pdf` -- PDF original validado contra `raw/apuntes.txt`; pasadas editoriales ya cubren unidades 1-11, tratando la 8 como soporte de TP1 y manteniendo 9-11 fuera de foco
- [x] `raw/notas.txt` -- Notas de clases (~175KB)
- [x] `raw/notas_sprint_1.txt` -- Procesado en `[[resumen-notas-sprint-1]]`
- [x] `raw/notas_para_sprint_2.txt` -- Checklist de cierre del sprint 2 procesado en `[[resumen-notas-sprint-2]]`
- [x] `raw/pdfs/Enunciado_TPE1.pdf` -- PDF original validado contra `raw/enunciado.txt`
- [x] `raw/pdfs/Notas clases PAW.pdf` -- PDF original validado contra `raw/notas.txt`
- [x] `raw/audio_transcript/audio_transcript clase 2.VTT` -- Revisado con cautela; solo uso auxiliar
- [x] `raw/audio_transcript/audio_transcript clase 3.VTT` -- Revisado con cautela; solo uso auxiliar
- [x] `raw/audio_transcript/audio_transcript clase 4.VTT` -- Revisado con cautela; clase reciente sobre agentes y formularios, afectada por errores de ASR
- [ ] `raw/2026-04-10_paw-violaciones-plan_v1.md` -- Documento de trabajo presente en `raw/`, todavia sin ingesta canonica en `wiki/`
- [ ] `raw/2026-04-12_paw-remediacion-plan-audit_v1.md` -- Documento de trabajo presente en `raw/`, todavia sin ingesta canonica en `wiki/`
- [ ] `raw/2026-04-12_paw-remediacion-plan-secuencial_v1.md` -- Documento de trabajo presente en `raw/`, todavia sin ingesta canonica en `wiki/`
- [x] `raw/2026-04-13_paw-remediacion-hallazgos-secuencial_v1.md` -- Documento de trabajo ya absorbido por `[[auditoria-extrema-cumplimiento-paw]]`; queda como antecedente historico
- [x] `raw/reservas/spec-funcional-reservas.md` -- Spec v1 del sistema de reservas; conservado como antecedente historico, superado por v2
- [x] `raw/specs_reserva_v2.txt` -- Spec v2 del sistema de reservas (vigente); procesado en `[[resumen-spec-reservas]]` y siendo propagado a `[[plan-implementacion-reservas]]`
