---
titulo: Validacion de Formularios — Bean Validation y Custom Validators
tipo: concepto
fuentes: [raw/correcciones.md, raw/apuntes.txt, raw/notas.txt]
creado: 2026-04-09
actualizado: 2026-04-17
---

# Validacion de Formularios — Bean Validation y Custom Validators

La validacion de datos de entrada debe ser **declarativa** usando JSR380 (Bean Validation), no imperativa en controllers.

## Regla

- Validar con anotaciones `@NotNull`, `@Size`, `@Email`, etc. en los DTOs/forms
- Crear custom validators (`@Constraint`) para validaciones complejas (match passwords, unicidad de email)
- El controller solo recibe `@Valid` y el `BindingResult`

## Errores detectados

- Validar formularios a mano en el controller
- Restricciones validadas en servicios pero no en forms via constraints — obliga exception handling feo en controllers
- Falta de constraint validators para reglas como unicidad o formato

## Elogios de la catedra

Los grupos que implementaron custom validators bien abstraidos fueron **elogiados explicitamente**:
> "Separar y abstraer todo modelo de control de inputs dentro del Annotator @ en forma JSR380 ayuda de maravilla a limpiar el codigo MVC"

Tambien se elogio:
- Validacion de extension de imagen al subir (Multipart)
- Catch al subir imagenes filtrando extensiones no genuinas

## Flujo segun apuntes

1. Crear **Form Bean** (DTO) en `webapp/src/main/java` con getters/setters (ej: `UserForm` con email, password, repeat)
2. Anotar campos con `@NotNull`, `@Size`, `@Email`, etc.
3. En controller: recibir `@Valid UserForm form, BindingResult errors`
4. Usar `@ModelAttribute` para binding automatico entre JSP y form bean
5. En JSP: usar `<form:form modelAttribute="userForm">` con `<form:input path="...">` y `<form:errors path="...">`
6. Para validaciones cross-field (ej: match passwords): crear custom `@Constraint`

## Criterio UX de formularios

- Los formularios son una de las zonas mas delicadas de una app web.
- No alcanza con validar: hay que dar feedback claro, mantener los valores ingresados y no perder al usuario.
- El ejemplo de clase agrega `repeat password` justamente para mostrar un caso real de validacion cross-field.
- Los errores deberian verse y entenderse rapido; por eso la clase muestra estilos y mensajes visibles para invalidaciones.

## Detalles practicos de las clases

### @ModelAttribute como entrada y salida
- `@ModelAttribute` no solo crea el objeto desde el request sino que lo agrega automaticamente al `ModelAndView`
- Si hay errores de validacion, el formulario se redibuja con los valores que el usuario ingreso (no se pierden)
- Tambien se puede usar a nivel metodo: un metodo anotado con `@ModelAttribute` agrega atributos a TODOS los MAVs del controller (util para usuario actual, carrito, etc.)

### Beans vs Records
- Un **Bean** tiene constructor default + getters + setters (usar para formularios en TPE1)
- Un **Record** requiere todos los parametros en el constructor (usar para REST API en TPE2)
- Spring popula Beans automaticamente: matchea `@RequestParam` con setters por nombre

### Groups para validaciones condicionales
- Misma clase formulario puede usarse para crear y editar con validaciones distintas
- Definir interfaces de grupo y asignar anotaciones: `@NotEmpty(groups = CreateGroup.class)`
- Ejemplo: password requerida al registrar pero no al editar perfil; username validado solo en creacion

### Internacionalizacion de mensajes de validacion
- Las anotaciones JSR 303 tienen atributo `message` con clave default (ej: `{javax.validation.constraints.Size.message}`)
- Hibernate Validator shippea archivos properties con traducciones por idioma
- Se customiza creando claves correspondientes en `messages_xx.properties`
- **Regla critica**: internacionalizar el mensaje completo con placeholders, nunca concatenar strings parciales (la estructura gramatical varia entre idiomas)

## Ver tambien
- [[resumen-transcripciones-clases-2-a-4]]
- [[tp1-vs-tpe2-final]]
- [[comparacion-seguridad-web]]
- [[comparacion-jsp-formularios-e-i18n]]
- [[auth-flows]]
- [[buenas-practicas]]
- [[manejo-imagenes]]
- [[logica-en-controllers]]
- [[manejo-excepciones]]
- [[jsp-jstl]]
- [[internacionalizacion]]
- [[ux-flows]]
- [[resumen-correcciones]]
- [[resumen-apuntes]]
- [[resumen-notas]]
- [[resumen-notas-sprint-1]]

- [[plan-implementacion-reservas]]
- [[resumen-spec-reservas]]
