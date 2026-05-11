---
titulo: TP1 vs TPE2 Final — Ruta de Estudio
tipo: sintesis
fuentes: [raw/apuntes.txt, raw/notas.txt, raw/enunciado_tpe2.txt, raw/pdfs/Enunciado_TPE2.pdf, raw/PAW - clase 1 (TP1).pdf, raw/PAW - clase 2 (TP1).pdf, raw/PAW - clase 3 (TP1).pdf, raw/PAW - clase 4 (TP1).pdf, raw/PAW - clase 5 (TP1).pdf, raw/PAW - clase 6 (TP1).pdf, raw/PAW - clase 7 (TP2).pdf, raw/PAW - clase 8 (TP2).pdf, raw/PAW - clases 9 y 10 (TP final).pdf, raw/PAW- Clase Teórica Front end (TP final).pdf, raw/PAW- Clase Teórica - SPAs segunda parte (TP final).pdf, fuente historica no incluida (raw/audio_transcript/audio_transcript clase 2.VTT), fuente historica no incluida (raw/audio_transcript/audio_transcript clase 3.VTT), fuente historica no incluida (raw/audio_transcript/audio_transcript clase 4.VTT)]
creado: 2026-04-13
actualizado: 2026-05-11
---

# TP1 vs TPE2 Final — Ruta de Estudio

Esta pagina deja explicito que conviene estudiar primero para **TP1** y que conviene dejar para **TPE2/final**, usando el estado actual del wiki.

La ingesta de [[resumen-clases-paw-2026]] vuelve mas fuerte la separacion por etapa: las clases nuevas no son solo teoria suelta, sino una ruta de migracion progresiva de stack.

## Contrato por etapa

| Etapa | Decision de stack | Como trabaja el agente |
|-------|-------------------|------------------------|
| TP1 | Spring MVC server-side, JSP/JSTL, JDBC, HSQLDB, Spring Security, Logback | Preservar el stack clasico; no introducir JPA ni SPA salvo instruccion explicita |
| TP2 | Migracion de persistencia a JPA/Hibernate sin perdida de funcionalidad ni informacion | Concentrarse en entidades, `EntityManager`, mappings, transaction manager JPA, fetch/cascadas, SQL generado y migraciones de datos |
| TP final | API REST + SPA | Separar recursos REST/DTOs/auth stateless del frontend con estado, routing, build, cache y testing |

Las versiones concretas de dependencias que aparecen en PDFs viejos son historicas. Para implementar, priorizar el checkout actual, el enunciado vigente y la compatibilidad real del proyecto.

## Tabla rapida

| Bloque | Prioridad | Que estudiar ahora | Que dejar para despues | Fuente principal |
|--------|-----------|--------------------|------------------------|------------------|
| Enunciado y criterios | Maxima | Requisitos, fechas, penalizaciones y forma de entrega | Nada | [[resumen-enunciado]] y [[resumen-enunciado-tpe2]] |
| TP1 — Base teorica | Maxima | Unidades 1-7: Scrum, HTTP, JSP, Spring MVC, testing, formularios, i18n, security, logging | Nada del bloque core | [[resumen-apuntes]], [[resumen-clases-paw-2026]] |
| TP1 — Aterrizaje practico | Maxima | Setup real, wiring, ejemplos, warnings, decisiones operativas | Nada | [[resumen-notas]] |
| TP1 — Soporte de implementacion | Alta | AOP aplicado a proxies y `@Transactional` en services | Pointcuts complejos y usos avanzados de AspectJ | [[spring-aop]], [[transactional]] |
| TP1 — Conceptos para implementar | Maxima | Paginas concepto ligadas a unidades 1-7 mas `transactional` | Material de SPA/REST/JPA | [[spring-web-mvc]], [[testing-unitario]], [[validacion-formularios]], [[spring-security]], [[transactional]] |
| Transcripciones VTT | Media | Solo corroborar ideas ya presentes o apoyar clase reciente de formularios/agentes | No convertirlas en fuente canonica aislada | [[resumen-transcripciones-clases-2-a-4]] |
| TPE2 / final — Extension tecnica | Alta solo si estas en esa etapa | Nada si estas cerrando TP1 | JPA/Hibernate para TP2; SPA y API REST para final | [[hibernate-jpa]], [[single-page-applications]], [[api-rest]], [[resumen-clases-paw-2026]] |

## Orden recomendado

1. Leer [[resumen-enunciado]] para fijar restricciones y criterios de correccion.
2. Estudiar en [[resumen-apuntes]] las unidades 1-7 como base.
3. Bajar eso a implementacion con [[resumen-notas]].
4. Revisar [[spring-aop]] y [[transactional]] cuando definas servicios, bordes transaccionales o tengas dudas sobre proxies.
5. Ir a las paginas concepto del bloque que estes tocando en codigo.
6. Usar [[resumen-correcciones]] para evitar errores recurrentes de la catedra.
7. Dejar [[hibernate-jpa]], [[single-page-applications]] y [[api-rest]] para despues de estabilizar TP1.

## Regla editorial del wiki

- **Canon TP1**: `resumen-enunciado` + `resumen-apuntes` (1-7 como base + unidad 8 como soporte de implementacion) + `resumen-notas` + clases TP1 de [[resumen-clases-paw-2026]].
- **Canon TP2**: [[resumen-enunciado-tpe2]] + clases 7-8 de [[resumen-clases-paw-2026]] + [[hibernate-jpa]], siempre contrastadas contra checkout actual.
- **Canon TP final**: clases 9/10 y teoricas SPA de [[resumen-clases-paw-2026]] + [[api-rest]] + [[single-page-applications]].
- **Auxiliar**: `resumen-transcripciones-clases-2-a-4`.
- **Fuera de foco TP1**: unidades 9-11, ya ingeridas pero separadas en su propio bloque.

## Ver tambien

- [[resumen-enunciado]]
- [[resumen-enunciado-tpe2]]
- [[resumen-apuntes]]
- [[resumen-notas]]
- [[resumen-clases-paw-2026]]
- [[resumen-transcripciones-clases-2-a-4]]
- [[resumen-correcciones]]
- [[spring-aop]]
- [[hibernate-jpa]]
- [[single-page-applications]]
- [[api-rest]]
