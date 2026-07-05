---
titulo: Criterios de Evaluacion y Penalizaciones
tipo: concepto
fuentes: [raw/enunciado.txt, raw/enunciado_tpe2.txt, raw/pdfs/Enunciado_TPE2.pdf, raw/correcciones_tp2.md, raw/devolucion_tp1_2026_c1.txt, raw/pdfs/Devolucion_TP1_2026_C1.pdf, "raw/final paw/Correcciones viejas_.docx", "raw/final paw/presentacion-api.pdf", "raw/final paw/presentacion-spring-frontend.pdf", "raw/final paw/Optimización REST_ Caching Frontend y Backend.pdf"]
creado: 2026-04-09
actualizado: 2026-07-05
---

# Criterios de Evaluacion y Penalizaciones

La catedra evalua funcionalidad, calidad de codigo, arquitectura, usabilidad, proceso y contribuciones individuales. Auditan codigo y commits.

## Penalizaciones en entrega final

| Criterio | Penalizacion maxima | Wiki relacionado |
|----------|-------------------|------------------|
| Arquitectura e implementacion incorrecta | **-4 puntos** | [[logica-en-controllers]], [[spring-security]], [[persistencia-jdbc]] |
| Funcionalidad y usabilidad | **-2 puntos** | [[buenas-practicas]] |
| Unit testing insuficiente o de baja calidad | **-1 punto** | [[testing-unitario]] |
| Forma de entrega incorrecta | **-1 punto** | [[configuracion-maven]] |

## Penalizaciones especificas TPE2

| Criterio | Penalizacion maxima | Wiki relacionado |
|----------|---------------------|------------------|
| Incumplimiento de Scrum, Pivotal Tracker, priorizacion o valor por iteracion | **-2 puntos** | [[scrum-metodologia]] |
| Unit testing insuficiente o de baja calidad | **-1 punto** | [[testing-unitario]] |
| Forma de entrega incorrecta | **-1 punto** | [[configuracion-maven]] |
| Funcionalidad y usabilidad del producto | **-4 puntos** | [[ux-flows]], [[buenas-practicas]] |
| Arquitectura, DDD, separacion de capas, responsabilidades o buenas practicas | **-4 puntos** | [[modelo-capas]], [[hibernate-jpa]] |
| Perdidas de informacion | **-2 puntos** | [[hibernate-jpa]], [[transactional]] |
| Feedback anterior no corregido | **-1 punto por cada correccion no resuelta** | [[resumen-correcciones]], [[resumen-correcciones-tp2]] |

## Penalizaciones por sprint

| Criterio | Penalizacion |
|----------|-------------|
| Perdida de informacion entre iteraciones | **-2 puntos** |
| Fallos de funcionalidad/usabilidad por sprint | **-1 punto** |
| Incumplimiento de Scrum por sprint intermedio | **-0.5 puntos** |

## Errores no recuperables (reprobado directo)

- No realizar cualquier demo de fin de iteracion
- No tener cambios a presentar en una demo
- No cumplir requerimientos tecnicos del enunciado
- Inconsistencias en el hash del commit presentado

## Que se evalua

- **Funcional:** que la app funcione y sea usable
- **Codigo:** auditoria de calidad, DDD, separacion de capas, Effective Java
- **Individual:** contribuciones por miembro via commits (la catedra puede evaluar individualmente)
- **Proceso:** uso de Plane, priorizacion, valor por iteracion

## Prioridades de riesgo

Basado en el peso de penalizaciones, el orden de prioridad es:

1. **Arquitectura** (-4 pts) — DDD, capas, responsabilidades, Spring Security
2. **Perdida de datos** (-2 pts) — migraciones de BD
3. **Funcionalidad/UX** (-2 pts final, -1 pt/sprint)
4. **Testing** (-1 pt) — cobertura y calidad
5. **Forma de entrega** (-1 pt) — war, README, git limpio

Las devoluciones TP2 consolidan que las reincidencias pesan mucho: varios grupos recibieron observaciones porque errores ya marcados en TP1 seguian abiertos en la migracion JPA/Hibernate. Ver [[resumen-correcciones-tp2]].

La devolucion TP1 2026 C1 muestra el mismo patron desde la entrega inicial:
controllers con logica de negocio, seguridad manual, tests no unitarios, N+1,
configuracion versionada y uso no revisado de agentes aparecen como errores
conceptuales graves. Ver [[resumen-correcciones-tp1-2026-c1]].

## Riesgos especificos TP final

Las correcciones finales del lote `raw/final paw` muestran patrones que pueden costar mucho aunque la app "funcione":

- API con sesion (`JSESSIONID`) o auth no stateless.
- Endpoints con acciones, paths versionados, siempre `200 OK`, `Location` invalido o paginacion en body.
- HATEOAS ausente: cliente construye URLs internas en vez de seguir links.
- Vendor media types usados para elegir operaciones secretas, no para representar/versionar recursos.
- Datos sensibles expuestos: emails masivos, flags admin, tokens o recursos ajenos sin ownership.
- Refresh token reenviado en cada response o refresh implementado con roundtrip innecesario.
- Cache incorrecto: assets sin cache immutable pese a file revving, o imagenes/recursos actualizables con URL estable cacheados como inmutables.
- Browser history roto: back/forward no preserva filtros, resultados o navegacion SPA.
- `webapp` sin dependencia explicita del frontend, generando WAR sin assets o con version vieja.
- Tests frontend/API pobres, placeholders o no unitarios; tests que usan la misma clase bajo test como oraculo.
- Fugas de capa: tipos servlet/web en capas no web, magic strings de queries desde controllers, conteos hechos trayendo toda la tabla, ids sueltos en relaciones no mapeadas.

## Ver tambien
- [[resumen-enunciado]]
- [[resumen-enunciado-tpe2]]
- [[calendario-entregas]]
- [[scrum-metodologia]]
- [[resumen-correcciones]]
- [[resumen-correcciones-tp1-2026-c1]]
- [[resumen-correcciones-tp2]]
- [[resumen-final-paw-2026]]
- [[checklist-tp-final-rest-spa]]
- [[resumen-notas-sprint-1]]
- [[buenas-practicas]]
- [[comparacion-seguridad-web]]
- [[comparacion-testing-servicios-y-daos]]

- [[auditoria-extrema-cumplimiento-paw]]
- [[plan-implementacion-reservas]]
