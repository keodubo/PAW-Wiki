# Indice del Wiki PAW

Catalogo de todas las paginas del wiki, organizado por categoria.

Nota de distribucion liviana: las entradas marcadas como `fuente historica no incluida (...)` fueron fuentes usadas para crear o auditar el wiki, pero ya no viajan en este repositorio. La pagina sintetica queda como referencia; no se debe esperar que exista el archivo crudo indicado entre parentesis.

---

## Examples operativos

Estos archivos no son paginas wiki canonicas; son guias de uso para humanos y agentes.

| Archivo | Uso |
|--------|-----|
| [examples/README.md](examples/README.md) | Indice de ejemplos de uso de la wiki |
| [examples/setup-local.md](examples/setup-local.md) | Setup desde cero: clonar, abrir en Obsidian y crear espacio privado |
| [examples/wiki-dentro-repo-paw.md](examples/wiki-dentro-repo-paw.md) | Flujo para clonar o forkear PAW-Wiki dentro del repo PAW sin subirlo a la entrega |
| [examples/actualizar-wiki.md](examples/actualizar-wiki.md) | Flujo para que un agente revise `origin/main` y actualice la wiki local con `git pull --ff-only` cuando sea seguro |
| [examples/instalar-skills.md](examples/instalar-skills.md) | Instalacion opcional de skills en Codex, Claude u otros agentes |
| [examples/ingesta-publica.md](examples/ingesta-publica.md) | Flujo para convertir una fuente publica en paginas wiki |
| [examples/consulta-wiki.md](examples/consulta-wiki.md) | Flujo para responder preguntas usando el indice y paginas canonicas |
| [examples/uso-con-agente.md](examples/uso-con-agente.md) | Prompt base para Codex, Claude u otros agentes |
| [examples/second-brain-privado.md](examples/second-brain-privado.md) | Flujo para mantener material personal en `docs/private/` |
| [examples/checklist-publicacion.md](examples/checklist-publicacion.md) | Checklist antes de commitear o pushear |
| [examples/troubleshooting.md](examples/troubleshooting.md) | Solucion de problemas comunes de Git, Obsidian, imagenes y skills |

## Fuentes

| Pagina | Descripcion | Origen |
|--------|-------------|--------|
| [[resumen-correcciones]] | Ingesta exhaustiva de errores, aciertos y observaciones recurrentes de correcciones previas | `raw/correcciones_tp1.md` |
| [[resumen-enunciado]] | Especificacion oficial del TPE1: stack, requerimientos, entrega | `raw/enunciado.txt` + `raw/pdfs/Enunciado_TPE1.pdf` |
| [[resumen-apuntes]] | Apuntes completos de la materia (11 unidades; 1-7 como base TP1, 8 como soporte de implementacion y 9-11 reingestadas en detalle para TP2/final; incluye paginas dedicadas por unidad) | `raw/apuntes.txt` + `raw/pdfs/PAW - Apuntes.pdf` |
| [[resumen-notas]] | Notas de clase con walkthroughs practicos y advertencias del profesor | `raw/notas.txt` + `raw/pdfs/Notas clases PAW.pdf` |
| [[resumen-clases-paw-2026]] | Ingesta detallada de PDFs nuevos de clase, ordenada por TP1, TP2 y TP final; las versiones de dependencias quedan tratadas como historicas | `raw/PAW*.pdf` |
| [[resumen-notas-sprint-1]] | Feedback puntual del sprint 1 sobre producto, UX, filtros y direccion del proyecto | `fuente historica no incluida (raw/notas_sprint_1.txt)` |
| [[resumen-notas-sprint-2]] | Checklist de cierre del sprint 2: auth, owner/admin, UX, correcciones ya resueltas y checks manuales remanentes | `fuente historica no incluida (raw/notas_para_sprint_2.txt)` |
| [[resumen-transcripciones-clases-2-a-4]] | Revision cautelosa de transcripciones VTT; clases 2-3 como apoyo y clase reciente sobre agentes/formularios | `fuente historica no incluida (raw/audio_transcript/*.VTT)` |
| [[resumen-spec-reservas]] | Ingesta del spec funcional del sistema de reservas v2 (vigente) con diff frente a v1, reglas, modalidades, slots, estados y resoluciones completas de ambiguedades v2 | `fuente historica no incluida (raw/specs_reserva_v2.txt)` + `fuente historica no incluida (raw/reservas/spec-funcional-reservas.md)` (historico) + `fuente historica no incluida (raw/diagrama_bd.md)` |

## Unidades de PAW - Apuntes

Estas paginas separan `[[resumen-apuntes]]` por unidad para estudio y navegacion. Las unidades 8-11 tienen mayor detalle porque conectan directamente con AOP/transacciones, TP2 y TP final.

| Unidad | Pagina | Foco |
|--------|--------|------|
| 1 | [[paw-unidad-01-introduccion-scrum-mvp]] | Scrum, stories, workflow, AirBnB y MVP |
| 2 | [[paw-unidad-02-contexto-historico-web]] | HTTP, CGI, sesiones, Java server-side, mailing e imagenes |
| 3 | [[paw-unidad-03-frontend-jsp-jstl]] | Usabilidad, CSS, JSP, EL, JSTL y tags |
| 4 | [[paw-unidad-04-spring-mvc-capas-jdbc]] | Maven, Spring MVC, DI, capas y JDBC |
| 5 | [[paw-unidad-05-unit-testing]] | JUnit, dobles, Mockito, HSQLDB y tests DAO/service |
| 6 | [[paw-unidad-06-formularios-i18n]] | Form beans, Bean Validation, Spring form tags e i18n |
| 7 | [[paw-unidad-07-seguridad-logging]] | Spring Security, roles, remember-me y Logback |
| 8 | [[paw-unidad-08-aop-transacciones]] | AOP, proxies, restricciones y `@Transactional` |
| 9 | [[paw-unidad-09-hibernate-jpa]] | ORM, JPA/Hibernate, mappings, contexto y paginacion |
| 10 | [[paw-unidad-10-single-page-applications]] | SPA, auth cliente, i18n frontend, testing, hosting y cache |
| 11 | [[paw-unidad-11-api-rest]] | Recursos REST, verbos, status codes, links, content negotiation y JWT |

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

### Ruta de estudio y patrones

| Pagina | Descripcion |
|--------|-------------|
| [[tp1-vs-tpe2-final]] | Ruta de estudio para distinguir material prioritario de TP1, AOP como soporte de implementacion y temas para TPE2/final |
| [[buenas-practicas]] | Inventario de patrones elogiados por la catedra para reutilizar como checklist positivo al implementar o auditar |

### Auditorias y remediacion historica

| Pagina | Descripcion |
|--------|-------------|
| [[2026-04-13_auditoria-repo-docs_v1]] | Contraste canonico entre `docs/` y el repo auditado al `2026-04-13`; sigue siendo la referencia vigente para leer el cluster historico de remediacion |
| [[2026-04-12_auditoria-y-plan-remediacion_v1]] | Absorcion historica del par auditoria + plan secuencial del `2026-04-12`; deja trazado el puente entre los documentos de trabajo previos y el canon posterior del wiki |
| [[2026-04-20_bugs-deploy-lote-0-y-plan-remediacion_v1]] | Absorbe el backlog crudo de `20` bugs de deploy, la auditoria corta del lote 0 y el plan secuencial por lotes del `2026-04-20`; sirve como nodo historico de postcierre |
| [[2026-04-24_auditoria-implementacion-contra-wiki_v1]] | Auditoria exhaustiva del estado actual del repo contra PAW-Wiki y reglas de cursada; registra build/package verde, fortalezas y brechas priorizadas de idioma, reservas, configuracion sensible, auth, contextos Spring, JSP/CSS y workflow wiki |
| [[2026-04-24_correcciones-sprint-2_v1]] | Plan/checklist secuencial para ejecutar correcciones de demo del Sprint 2 sin ingesta resumida; conserva los items completos, bloques verificables y decisiones de producto |
| [[auditoria-extrema-cumplimiento-paw]] | Baseline historico inicial de la auditoria extrema del `2026-04-13`; util para reconstruir hallazgos originales, no para leer estado actual sin contraste |
| [[plan-ejecucion-remediacion-auditoria]] | Orden historico de ejecucion de la remediacion; varias fases quedaron desactualizadas frente al repo actual y deben leerse junto a `[[2026-04-13_auditoria-repo-docs_v1]]` |
| [[remediacion-violaciones-paw]] | Recorte historico/parcial del plan de violaciones; hoy sirve sobre todo para seguir la deuda legacy de `day_of_week` |

### Reservas, cierre e informe tecnico

| Pagina | Descripcion |
|--------|-------------|
| [[plan-implementacion-reservas]] | Plan operativo por fases para implementar el sistema de reservas: modelo, configuracion, disponibilidad, flujos, scheduler, emails, reviews y hardening. Auditado 2026-04-17 contra las reglas del wiki (mail templates en `services/`, JOINs explicitos en listados, rechazo de reservas con slot vencido, recordatorio solo a confirmed, reassignacion sin restriccion de direccion, pluralizacion i18n, scheduler desactivado en tests) |
| [[2026-04-17_cierre-implementacion-reservas_v1]] | Registro de cierre de la implementación del sistema de reservas; resume el estado final, hardening realizado y estabilización del test suite |
| [[2026-04-19_informe-implementaciones-desde-f81eb49_v1]] | Ingesta estructurada del informe tecnico shareable del `2026-04-19`; deja alcance, epicas, QA manual, matriz de stories y la relacion entre la copia cruda en `raw/` y el `tex/pdf` canonico |

## Comparaciones

| Pagina | Descripcion |
|--------|-------------|
| [[comparacion-seguridad-web]] | Autorizacion, validacion y escapado: que resuelve cada capa de seguridad |
| [[comparacion-testing-servicios-y-daos]] | Diferencia entre testear servicios, persistencia y transacciones |
| [[comparacion-capas-web-services-persistence]] | Limites reales entre webapp, services y persistence |
| [[comparacion-jsp-formularios-e-i18n]] | Relacion entre JSP, form beans, errores y textos internacionalizados |

---

### Estado de fuentes raw

- [x] `raw/correcciones_tp1.md` -- Errores y aciertos recopilados de correcciones previas (~26KB)
- [x] `raw/enunciado.txt` -- Enunciado oficial del TPE1
- [x] `raw/apuntes.txt` -- Apuntes de la materia (~143KB, 21K lineas)
- [x] `raw/pdfs/PAW - Apuntes.pdf` -- PDF original validado contra `raw/apuntes.txt`; reingesta 2026-05-11 amplia unidades 8-11 en `[[resumen-apuntes]]` y crea paginas dedicadas `[[paw-unidad-01-introduccion-scrum-mvp]]` a `[[paw-unidad-11-api-rest]]`
- [x] `raw/notas.txt` -- Notas de clases (~175KB)
- [x] `raw/PAW - clase 1 (TP1).pdf` -- Clase TP1: Spring Web, Maven, WebConfig, ViewResolver, modulos y DI; procesada en `[[resumen-clases-paw-2026]]`
- [x] `raw/PAW - clase 2 (TP1).pdf` -- Clase TP1: Servlets, JSP, EL, JSTL y tags propios; procesada con OCR en `[[resumen-clases-paw-2026]]`
- [x] `raw/PAW - clase 3 (TP1).pdf` -- Clase TP1: Spring JDBC, schema, HSQLDB, Mockito y tests DAO/servicio; procesada en `[[resumen-clases-paw-2026]]`
- [x] `raw/PAW - clase 4 (TP1).pdf` -- Clase TP1: Web Forms, Bean Validation, Spring form tags e i18n; procesada en `[[resumen-clases-paw-2026]]`
- [x] `raw/PAW - clase 5 (TP1).pdf` -- Clase TP1: Spring Security y logging inicial; procesada en `[[resumen-clases-paw-2026]]`
- [x] `raw/PAW - clase 6 (TP1).pdf` -- Clase TP1: logging productivo, AOP, proxies y `@Transactional`; procesada en `[[resumen-clases-paw-2026]]`
- [x] `raw/PAW - clase 7 (TP2).pdf` -- Clase TP2: introduccion a Hibernate/JPA, `EntityManager`, DAOs JPA y entidades; procesada en `[[resumen-clases-paw-2026]]`
- [x] `raw/PAW - clase 8 (TP2).pdf` -- Clase TP2: relaciones JPA, fetch, contexto de persistencia y dirty checking; procesada en `[[resumen-clases-paw-2026]]`
- [x] `raw/PAW - clases 9 y 10 (TP final).pdf` -- TP final: Jersey/JAX-RS, REST, DTOs y autenticacion stateless; procesada en `[[resumen-clases-paw-2026]]`
- [x] `raw/PAW- Clase Teórica Front end (TP final).pdf` -- TP final: SPA, modulos JS, runtime, package managers, bundlers, frameworks y routing; procesada en `[[resumen-clases-paw-2026]]`
- [x] `raw/PAW- Clase Teórica - SPAs segunda parte (TP final).pdf` -- TP final: componentes, forms, estado, auth, i18n, testing, optimizacion, hosting, SSR y cache; procesada en `[[resumen-clases-paw-2026]]`
- [x] `fuente historica no incluida (raw/notas_sprint_1.txt)` -- Procesado en `[[resumen-notas-sprint-1]]`
- [x] `fuente historica no incluida (raw/notas_para_sprint_2.txt)` -- Checklist de cierre del sprint 2 procesado en `[[resumen-notas-sprint-2]]`
- [x] `raw/pdfs/Enunciado_TPE1.pdf` -- PDF original validado contra `raw/enunciado.txt`
- [x] `raw/pdfs/Notas clases PAW.pdf` -- PDF original validado contra `raw/notas.txt`
- [x] `fuente historica no incluida (raw/audio_transcript/audio_transcript clase 2.VTT)` -- Revisado con cautela; solo uso auxiliar
- [x] `fuente historica no incluida (raw/audio_transcript/audio_transcript clase 3.VTT)` -- Revisado con cautela; solo uso auxiliar
- [x] `fuente historica no incluida (raw/audio_transcript/audio_transcript clase 4.VTT)` -- Revisado con cautela; clase reciente sobre agentes y formularios, afectada por errores de ASR
- [x] `fuente historica no incluida (raw/2026-04-10_paw-violaciones-plan_v1.md)` -- Documento de trabajo absorbido por `[[remediacion-violaciones-paw]]` como antecedente historico del plan de violaciones
- [x] `fuente historica no incluida (raw/2026-04-12_paw-remediacion-plan-audit_v1.md)` -- Documento de trabajo absorbido por `[[2026-04-12_auditoria-y-plan-remediacion_v1]]`
- [x] `fuente historica no incluida (raw/2026-04-12_paw-remediacion-plan-secuencial_v1.md)` -- Documento de trabajo absorbido por `[[2026-04-12_auditoria-y-plan-remediacion_v1]]`
- [x] `fuente historica no incluida (raw/2026-04-13_paw-remediacion-hallazgos-secuencial_v1.md)` -- Documento de trabajo ya absorbido por `[[auditoria-extrema-cumplimiento-paw]]`; queda como antecedente historico
- [x] `fuente historica no incluida (raw/2026-04-19_informe-implementaciones-desde-f81eb49_v1.pdf)` -- Copia inmutable del informe tecnico del `2026-04-19`; absorbida por `[[2026-04-19_informe-implementaciones-desde-f81eb49_v1]]` y validada byte a byte contra `fuente historica no incluida (reports/2026-04-19_informe-implementaciones-desde-f81eb49_v1.pdf)`
- [x] `fuente historica no incluida (raw/2026-04-20_deploy-bugs-lote-0-audit_v1.md)` -- Auditoria corta del cluster de deploy; absorbida por `[[2026-04-20_bugs-deploy-lote-0-y-plan-remediacion_v1]]`
- [x] `fuente historica no incluida (raw/2026-04-20_deploy-bugs-remediation-plan_v1.md)` -- Plan secuencial del mismo cluster; absorbido por `[[2026-04-20_bugs-deploy-lote-0-y-plan-remediacion_v1]]`
- [x] `fuente historica no incluida (raw/todo.md)` -- Lista fuente original de `20` bugs de deploy; absorbida por `[[2026-04-20_bugs-deploy-lote-0-y-plan-remediacion_v1]]`
- [x] `fuente historica no incluida (raw/reservas/spec-funcional-reservas.md)` -- Spec v1 del sistema de reservas; conservado como antecedente historico, superado por v2
- [x] `fuente historica no incluida (raw/specs_reserva_v2.txt)` -- Spec v2 del sistema de reservas (vigente); procesado en `[[resumen-spec-reservas]]` y siendo propagado a `[[plan-implementacion-reservas]]`
- [x] `fuente historica no incluida (raw/diagrama_bd.md)` -- Version narrativa del diagrama de reservas; absorbida por `[[resumen-spec-reservas]]`
- [x] `fuente historica no incluida (raw/diagrama_bd.puml)` -- Fuente editable del mismo diagrama; registrada como soporte de `[[resumen-spec-reservas]]`

### Estado de reportes

- [x] `fuente historica no incluida (reports/2026-04-19_informe-implementaciones-desde-f81eb49_v1.tex)` -- Fuente editable del informe tecnico, enlazada desde `[[2026-04-19_informe-implementaciones-desde-f81eb49_v1]]`
- [x] `fuente historica no incluida (reports/2026-04-19_informe-implementaciones-desde-f81eb49_v1.pdf)` -- Render final shareable del mismo informe; coincide byte a byte con `fuente historica no incluida (raw/2026-04-19_informe-implementaciones-desde-f81eb49_v1.pdf)`
- [x] Cleanup `2026-04-20` -- Eliminados `fuente historica no incluida (reports/2026-04-19_informe-implementaciones-desde-f81eb49_v1.aux)`, `fuente historica no incluida (reports/2026-04-19_informe-implementaciones-desde-f81eb49_v1.log)`, `fuente historica no incluida (reports/2026-04-19_informe-implementaciones-desde-f81eb49_v1.out)` y `fuente historica no incluida (reports/2026-04-19_informe-implementaciones-desde-f81eb49_v1.toc)` por ser artefactos derivados no canónicos
- [x] `fuente historica no incluida (reports/assets/manual-qa/)` -- Capturas de apoyo del QA manual documentado en el informe
