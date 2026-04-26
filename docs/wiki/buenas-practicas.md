---
titulo: Buenas Practicas Elogiadas por la Catedra
tipo: sintesis
fuentes: [raw/correcciones_tp1.md]
creado: 2026-04-09
actualizado: 2026-04-17
---

# Buenas Practicas Elogiadas por la Catedra

Patrones que recibieron elogios explicitos en correcciones previas. Replicar estos.

## Flujo de QA y organizacion

- Uso de tableros con Features separadas de Chores
- Control de herramientas con estados (`Done`, etc.) bien definidos

## Custom Validators JSR380

Abstraer validaciones en anotaciones `@Constraint`:
- Match passwords
- Largo de description
- Unicidad de email
- Validacion de extension de imagen en Multipart upload

> "Ayuda de maravilla a limpiar el codigo MVC"

(ver: [[validacion-formularios]])

## Mailing de calidad

- Templates con L10n bien compuestas
- CTAs asertivos que redirigen correctamente
- Formato HTML estandar con Header y botones

(ver: [[mailing]])

## Frontend robusto

- Paginacion bien implementada
- Filtros dinamicos sin refresh agresivo
- Autocomplete y selectores ricos
- Creacion asincrona
- CTAs y zero states cuidados
- Jerarquias de roles mostradas de forma consistente en la UI

## Zero states y error handling

- Zero states amigables cuando listas vuelven vacias
- Redireccion limpia a 404/403 cuando un recurso expira
- Control customizado de errores con vistas dedicadas

## Seguridad bien resuelta

- Escapar `%` y `_` en LIKE
- Uso de `<c:out>` consistente
- Taglib de Spring Security para mostrar/ocultar elementos por rol
- Jerarquias de roles manejadas limpiamente

## Cobertura de tests

- Buena cobertura en DAO (mencionado 2 veces como positivo)

## Manejo de imagenes

- Buen catch al subir imagenes para detectar extensiones no genuinas desde `Multipart`

## Ver tambien
- [[ux-flows]]
- [[validacion-formularios]]
- [[mailing]]
- [[spring-security]]
- [[xss-prevencion]]
- [[testing-unitario]]
- [[manejo-imagenes]]
- [[scrum-metodologia]]
- [[criterios-evaluacion]]
- [[resumen-correcciones]]
- [[resumen-notas-sprint-1]]

- [[plan-implementacion-reservas]]
