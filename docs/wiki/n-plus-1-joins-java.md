---
titulo: Anti-patron — N+1 Queries y Joins en Java
tipo: concepto
fuentes: [raw/correcciones.md]
creado: 2026-04-09
actualizado: 2026-04-09
---

# Anti-patron — N+1 Queries y Joins en Java

La base de datos debe procesar joins, filtros y ordenamientos. Hacerlo en Java es un **error conceptual grave**.

## Regla

Nunca traer datos y luego filtrar/unir en memoria. El motor SQL existe para esto.

## Errores detectados

### Joins en Java
- Iterar resultados y hacer N accesos a la DB para armar relaciones
- `categoriesIds.stream().map(...)` causando N+1 queries — usar `WHERE IN`
- Traer 1000 registros y filtrar en memoria para encontrar los que cumplen condicion

### Filtrado en Java
- Operaciones de filtro que implican JOINs resueltas en codigo
- Ordenamiento hecho en Java en vez de `ORDER BY` en SQL

## Impacto

Se nota directamente en la velocidad de la pagina. Un listado que deberia tardar 50ms puede tardar 5 segundos con N+1.

## Solucion

- Queries con `JOIN` en SQL para relaciones
- `WHERE IN (?)` para lotes de IDs
- `ORDER BY` y `LIMIT/OFFSET` en SQL
- Paginacion resuelta en la query, no en Java

## Ver tambien
- [[persistencia-jdbc]]
- [[transactional]]
- [[resumen-enunciado]]
- [[resumen-correcciones]]
