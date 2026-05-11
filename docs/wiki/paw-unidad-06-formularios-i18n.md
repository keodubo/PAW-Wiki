---
titulo: PAW Unidad 6 - Formularios e Internacionalizacion
tipo: fuente
fuentes: [raw/apuntes.txt, raw/pdfs/PAW - Apuntes.pdf]
creado: 2026-05-11
actualizado: 2026-05-11
---

# PAW Unidad 6 - Formularios e Internacionalizacion

Esta unidad aterriza el trabajo web de TP1 en flujos concretos: capturar input, validarlo, preservar valores, mostrar errores y traducir textos sin romper UX.

## Formularios

- Los formularios son un punto sensible de UX porque concentran errores de usuario.
- La app debe dar feedback claro y no borrar valores cargados sin necesidad.
- El form bean vive en webapp y representa input, no entidad persistente.
- Las anotaciones de Bean Validation expresan restricciones de entrada.
- `@Valid` dispara validacion automatica.
- `BindingResult` debe ir inmediatamente despues del objeto validado.

## Binding MVC

- `@ModelAttribute` bindea request a objeto y lo expone al modelo.
- Si hay errores, el controller redibuja el formulario con el mismo objeto.
- El servicio no deberia recibir strings crudos si la web ya puede parsear y validar estructura.
- Las reglas de negocio siguen perteneciendo al servicio.

## Spring form tags

- `<form:form>` conecta JSP con el form bean.
- `<form:input>`, `<form:password>` y similares mantienen el path consistente.
- `<form:errors>` muestra errores por campo o globales.
- Esto evita duplicar HTML y condicionamiento manual de errores.

## Internacionalizacion

- `MessageSource` centraliza textos visibles.
- La resolucion busca el archivo mas especifico y cae al default.
- Los mensajes de validacion pueden ser genericos o especificos por campo.
- Se deben traducir frases completas, no concatenar pedazos.
- Placeholders permiten interpolar valores sin romper gramatica.

## `@ModelAttribute` a nivel metodo

- Un metodo anotado puede agregar datos comunes a todos los requests de un controller.
- Sirve para usuario logueado, listas auxiliares o datos de layout.
- No debe ocultar queries pesadas ni reglas de negocio.

## Para TP1

- Usar form beans, no entidades como formularios.
- Mantener `BindingResult` en la posicion correcta.
- Preservar input al redibujar.
- Mover textos visibles a properties.
- Revisar que errores de validacion sean utiles y traducibles.

## Ver tambien

- [[resumen-apuntes]]
- [[validacion-formularios]]
- [[internacionalizacion]]
- [[ux-flows]]
- [[jsp-jstl]]
- [[paw-unidad-05-unit-testing]]
- [[paw-unidad-07-seguridad-logging]]
