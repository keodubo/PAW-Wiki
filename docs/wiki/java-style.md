---
titulo: Estilo Java y Consistencia de API
tipo: concepto
fuentes: [raw/correcciones.md]
creado: 2026-04-13
actualizado: 2026-04-13
---

# Estilo Java y Consistencia de API

Varias correcciones no discuten arquitectura gruesa sino disciplina de codigo: nombres, tipos, APIs y pequenos olores que degradan mantenibilidad.

## Convenciones basicas

- Declarar modificadores de acceso explicitamente cuando el diseno lo requiere.
- Mantener clases utilitarias realmente no instanciables.
- Usar `camelCase` y evitar `snake_case`.
- Nombrar modelos en singular (`User`, no `Users`).
- No declarar `public` en metodos de interfaces: es redundante.

## Tipos y enums

- Evitar boxed primitives cuando no aportan semantica.
- Preferir enums a magic strings para `orderBy`, `tab`, dias o estados como `SwapStatus`.
- Evitar magic numbers en controllers.
- Mantener consistencia para representar ausencia: no mezclar `null`, `Optional`, objetos con ID negativo o colecciones vacias de manera arbitraria.

## Uso correcto de Optional

- `Optional` deberia usarse en retornos, no como field de entidad.
- No llamar `get()` sin validar presencia.
- No combinar `Optional` con excepciones y `null` para el mismo concepto.
- Cuidar auto-unboxing sobre valores que pueden ser `null`.

## Contratos de metodos y objetos

- Los metodos `create` de services deberian seguir una convencion consistente.
- Evitar proliferacion de overloads cuando alcanza con un contrato mas claro o parametros opcionales.
- Si se redefine `equals()`, tambien hay que redefinir `hashCode()`.
- Quitar constantes, servicios o atributos inyectados que no se usan.
- Revisar wrappers o DTOs sin valor agregado real.

## Modularizacion

- Clases demasiado grandes como `ScheduleServiceImpl` indican pobre modularizacion.
- Builders o `fromModel` pueden ayudar cuando la conversion es legitima, pero no para ocultar un modelo mal partido.
- El usuario autenticado deberia resolverse una sola vez por `ControllerAdvice` / `@ModelAttribute`, no con helpers duplicados.

## Regla practica

Si una decision de estilo termina afectando contratos, excepciones, nullabilidad o legibilidad transversal, deja de ser "cosmetica" y pasa a ser una deuda de arquitectura menor.

## Ver tambien

- [[logica-en-controllers]]
- [[persistencia-jdbc]]
- [[configuracion-maven]]
- [[manejo-excepciones]]
- [[resumen-correcciones]]
