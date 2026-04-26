---
titulo: TP1 vs TPE2 Final — Ruta de Estudio
tipo: sintesis
fuentes: [raw/apuntes.txt, raw/notas.txt, fuente historica no incluida (raw/audio_transcript/audio_transcript clase 2.VTT), fuente historica no incluida (raw/audio_transcript/audio_transcript clase 3.VTT), fuente historica no incluida (raw/audio_transcript/audio_transcript clase 4.VTT)]
creado: 2026-04-13
actualizado: 2026-04-13
---

# TP1 vs TPE2 Final — Ruta de Estudio

Esta pagina deja explicito que conviene estudiar primero para **TP1** y que conviene dejar para **TPE2/final**, usando el estado actual del wiki.

## Tabla rapida

| Bloque | Prioridad | Que estudiar ahora | Que dejar para despues | Fuente principal |
|--------|-----------|--------------------|------------------------|------------------|
| Enunciado y criterios | Maxima | Requisitos, fechas, penalizaciones y forma de entrega | Nada | [[resumen-enunciado]] |
| TP1 — Base teorica | Maxima | Unidades 1-7: Scrum, HTTP, JSP, Spring MVC, testing, formularios, i18n, security, logging | Nada del bloque core | [[resumen-apuntes]] |
| TP1 — Aterrizaje practico | Maxima | Setup real, wiring, ejemplos, warnings, decisiones operativas | Nada | [[resumen-notas]] |
| TP1 — Soporte de implementacion | Alta | AOP aplicado a proxies y `@Transactional` en services | Pointcuts complejos y usos avanzados de AspectJ | [[spring-aop]], [[transactional]] |
| TP1 — Conceptos para implementar | Maxima | Paginas concepto ligadas a unidades 1-7 mas `transactional` | Material de SPA/REST/JPA | [[spring-web-mvc]], [[testing-unitario]], [[validacion-formularios]], [[spring-security]], [[transactional]] |
| Transcripciones VTT | Media | Solo corroborar ideas ya presentes o apoyar clase reciente de formularios/agentes | No convertirlas en fuente canonica aislada | [[resumen-transcripciones-clases-2-a-4]] |
| TPE2 / final — Extension tecnica | Baja por ahora | Nada si estas cerrando TP1 | JPA/Hibernate, SPA y API REST | [[hibernate-jpa]], [[single-page-applications]], [[api-rest]] |

## Orden recomendado

1. Leer [[resumen-enunciado]] para fijar restricciones y criterios de correccion.
2. Estudiar en [[resumen-apuntes]] las unidades 1-7 como base.
3. Bajar eso a implementacion con [[resumen-notas]].
4. Revisar [[spring-aop]] y [[transactional]] cuando definas servicios, bordes transaccionales o tengas dudas sobre proxies.
5. Ir a las paginas concepto del bloque que estes tocando en codigo.
6. Usar [[resumen-correcciones]] para evitar errores recurrentes de la catedra.
7. Dejar [[hibernate-jpa]], [[single-page-applications]] y [[api-rest]] para despues de estabilizar TP1.

## Regla editorial del wiki

- **Canon TP1**: `resumen-enunciado` + `resumen-apuntes` (1-7 como base + unidad 8 como soporte de implementacion) + `resumen-notas`.
- **Auxiliar**: `resumen-transcripciones-clases-2-a-4`.
- **Fuera de foco TP1**: unidades 9-11, ya ingeridas pero separadas en su propio bloque.

## Ver tambien

- [[resumen-enunciado]]
- [[resumen-apuntes]]
- [[resumen-notas]]
- [[resumen-transcripciones-clases-2-a-4]]
- [[resumen-correcciones]]
- [[spring-aop]]
- [[hibernate-jpa]]
- [[single-page-applications]]
- [[api-rest]]
