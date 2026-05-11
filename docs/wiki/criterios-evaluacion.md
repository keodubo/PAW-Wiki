---
titulo: Criterios de Evaluacion y Penalizaciones
tipo: concepto
fuentes: [raw/enunciado.txt, raw/enunciado_tpe2.txt, raw/pdfs/Enunciado_TPE2.pdf]
creado: 2026-04-09
actualizado: 2026-05-11
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
| Feedback anterior no corregido | **-1 punto por cada correccion no resuelta** | [[resumen-correcciones]] |

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

## Ver tambien
- [[resumen-enunciado]]
- [[resumen-enunciado-tpe2]]
- [[calendario-entregas]]
- [[scrum-metodologia]]
- [[resumen-correcciones]]
- [[resumen-notas-sprint-1]]
- [[buenas-practicas]]
- [[comparacion-seguridad-web]]
- [[comparacion-testing-servicios-y-daos]]

- [[auditoria-extrema-cumplimiento-paw]]
- [[plan-implementacion-reservas]]
