---
titulo: Remediación de Violaciones PAW
tipo: sintesis
fuentes: [raw/correcciones.md, raw/apuntes.txt, raw/notas.txt, raw/enunciado.txt]
creado: 2026-04-10
actualizado: 2026-04-13
---

# Remediación de Violaciones PAW

> Estado: historico/parcial. No usar esta pagina sola como estado actual del repo.
>
> Referencia vigente: [[2026-04-13_auditoria-repo-docs_v1]]

Esta sintesis cruza los criterios de correccion de PAW con una revision puntual del repo y queda retenida como registro historico de una remediacion previa. Su unico pendiente residual documentado es la compatibilidad legacy de `day_of_week`.

## Estado historico por fase

| Fase | Estado | Nota |
| --- | --- | --- |
| 1. `day_of_week` | Parcial | `schema-base.sql` + scripts dialectales, tests y `RestaurantForm` ya usan `1..7`; sigue faltando una migracion explicita si importa cubrir despliegues legacy con datos previos. |
| 2. `ON DELETE CASCADE` | Resuelta | El schema actual ya define `ON DELETE CASCADE` en `reviews.restaurant_id`. |
| 3. N+1 en restaurantes | Resuelta | Ya existen `findPopularWithHours`, `searchWithHours`, `findByIdWithHours` y `findBySlugWithHours`, con cobertura en DAO y service. |
| 4. XSS en `restaurant.jsp` | Resuelta | Ambos `title` ya usan `fn:escapeXml(comingSoon)` y el test de seguridad ahora valida el escape y la ausencia de la variante insegura. |
| 5. i18n en `RestaurantSearchForm` | Resuelta | Los `@Size` ya usan claves de `MessageSource` y la validacion queda cubierta con un test dedicado de Bean Validation. |

## Pendientes activos

### Fase 1 — `day_of_week`

La normalizacion a `1..7` ya esta reflejada en el esquema final compartido desde persistence, en los tests y en el form, pero no hay una migracion dedicada equivalente a las migraciones aditivas ya presentes en webapp. Si el despliegue objetivo puede contener datos legacy, todavia queda abierta esa deuda de compatibilidad.

## Siguiente paso recomendado

La unica deuda residual de este plan queda en la fase 1: decidir si el despliegue objetivo necesita compatibilidad con datos legacy de `day_of_week`. Si no existe ese escenario, el plan de remediacion queda efectivamente cerrado.

## Ver tambien

- [[resumen-correcciones]]
- [[xss-prevencion]]
- [[internacionalizacion]]
- [[persistencia-jdbc]]
- [[testing-unitario]]
