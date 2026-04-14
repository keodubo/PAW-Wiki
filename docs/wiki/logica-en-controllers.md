---
titulo: Anti-patron — Logica de Negocio en Controllers
tipo: concepto
fuentes: [raw/correcciones.md, raw/notas.txt]
creado: 2026-04-09
actualizado: 2026-04-13
---

# Anti-patron — Logica de Negocio en Controllers

El error mas recurrente y mas severamente penalizado en las correcciones. Marcado como **"error conceptual grave"** multiples veces.

## Regla

Los controllers deben ser **finos**: recibir request, validar declarativamente (Bean Validation), delegar al servicio, retornar vista. Sin logica intermedia.

## Violaciones detectadas

### Armado de entidades en controllers (error conceptual grave)
- Instanciar modelos de dominio en el controller
- Crear objetos con informacion parcial que nunca pasaron por persistencia
- Poplar listas pasadas por referencia (violacion SOLID)

### Logica de negocio directa (error conceptual grave)
- Multiples llamados a servicios para una operacion que deberia ser atomica
- Filtrar, ordenar o transformar datos en el controller
- Validar precondiciones de negocio (ej: "esta lleno el evento?", "le pertenece el torneo?")
- Guardar imagen + actualizar perfil como operaciones separadas en el controller

### Responsabilidades mezcladas
- Controllers que hacen exception handling con try/catch en vez de delegarlo a `@ControllerAdvice`
- Obtener usuario autenticado repetidamente — extraer a `@ModelAttribute` en un ControllerAdvice
- Validaciones de formulario manuales en vez de custom validators

## Patron correcto

```
Controller:
  1. Recibe @Valid FormDTO
  2. Pasa los datos combinados a un DTO inmutable centralizado (Ej: SaveRestaurantData)
  3. Llama UN solo metodo del servicio pasándole el DTO
  4. Retorna vista o redirect

Servicio:
  - Toda la logica de negocio recibiendo el DTO unificado
  - @Transactional
  - Orquesta DAOs
  - Crea/modifica entidades

ControllerAdvice:
  - Manejo centralizado de excepciones
  - @ModelAttribute para datos comunes (usuario logueado)
```

## Ver tambien
- [[comparacion-seguridad-web]]
- [[comparacion-capas-web-services-persistence]]
- [[criterios-evaluacion]]
- [[java-style]]
- [[mailing]]
- [[modelo-capas]]
- [[spring-security]]
- [[validacion-formularios]]
- [[manejo-excepciones]]
- [[transactional]]
- [[resumen-correcciones]]
- [[resumen-notas]]
