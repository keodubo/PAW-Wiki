---
titulo: Manejo de Excepciones
tipo: concepto
fuentes: [raw/correcciones.md, raw/notas.txt]
creado: 2026-04-09
actualizado: 2026-04-13
---

# Manejo de Excepciones

Las excepciones deben manejarse de forma centralizada via `@ControllerAdvice`, no con try/catch disperso.

## Errores detectados

- Try/catch de `Exception` en controllers — debe ir en `@ControllerAdvice`
- `@ControllerAdvice` definidos pero nunca escaneados por Spring (paquete fuera del component-scan)
- Configurar exception resolvers Y exception handlers para las mismas excepciones en lugares distintos
- Uso excesivo de try/catch solo para loggear errores sin propagarlos a MVC
- Arrojar `RuntimeException` cuando un `NotFoundException` custom es mas apropiado
- No usar custom exceptions (catchear `Exception` en plano)
- No agrupar excepciones por jerarquia (todas heredan de Exception base)
- `ForeignKeyException` no capturada en DAO — causa 500 en vez de 400

## Patron correcto

1. Crear excepciones custom por dominio (ej: `ResourceNotFoundException`, `UnauthorizedAccessException`)
2. Agruparlas en jerarquias logicas
3. Un solo `@ControllerAdvice` que mapee cada tipo a un HTTP status y vista apropiados
4. Los servicios arrojan excepciones; los controllers no las catchean

## Codigos HTTP correctos

- 404: recurso no encontrado
- 403: sin permisos sobre un recurso existente
- 400: input invalido (ej: constraint violation)
- 500: solo para errores internos genuinos, nunca por validacion fallida
- En correcciones puntuales puede aparecer un endpoint cuyo status esperado sea distinto por su semantica concreta; documentarlo como caso particular y no como regla general.

## @ControllerAdvice como AOP (segun clases)

- `@ControllerAdvice` es un **Advice** en terminologia AOP (Advice + PointCut + Weaving — analogias con costureria)
- Metodos anotados con `@ExceptionHandler(MiExcepcion.class)` mapean cada tipo de excepcion a una vista/status
- Se puede restringir a ciertos controllers con `@assignableTypes`
- El `@ComponentScan` lo detecta automaticamente si esta en el paquete de controllers
- Ejemplo: `UserNotFoundException` -> vista `usernotfound.jsp` sin que ningun controller tenga try/catch

## Ver tambien
- [[comparacion-testing-servicios-y-daos]]
- [[auth-flows]]
- [[java-style]]
- [[logica-en-controllers]]
- [[spring-aop]]
- [[validacion-formularios]]
- [[transactional]]
- [[resumen-correcciones]]
- [[resumen-notas]]
