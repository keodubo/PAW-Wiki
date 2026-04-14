---
titulo: Comparacion — JSP, Formularios e i18n
tipo: comparacion
fuentes: [raw/correcciones.md, raw/apuntes.txt, raw/notas.txt]
creado: 2026-04-09
actualizado: 2026-04-13
---

# Comparacion — JSP, Formularios e i18n

Estas tres piezas se tocan en la capa web, pero cumplen funciones distintas. Cuando se las mezcla mal aparecen bugs de UX, encoding y validacion.

## Mapa rapido

| Pieza | Funcion principal | Herramienta | Error tipico |
|-------|-------------------|-------------|--------------|
| JSP/JSTL | Renderizar vista | [[jsp-jstl]] | Imprimir contenido sin escapar |
| Form beans + validation | Capturar y validar input | [[validacion-formularios]] | Validar a mano y perder errores por campo |
| Internacionalizacion | Resolver textos correctos por idioma | [[internacionalizacion]] | Concatenar mensajes o dejar textos hardcodeados |

## Flujo correcto

1. El controller recibe un form bean con `@Valid`.
2. Si hay errores, vuelve a la JSP con el mismo `modelAttribute`.
3. La JSP renderiza inputs, errores y labels con taglibs.
4. Los textos visibles salen de `messages_xx.properties`.

## Que problema resuelve cada capa

| Problema | Respuesta correcta |
|----------|--------------------|
| Preservar input y errores del usuario | Spring form tags + `BindingResult` |
| Mostrar etiquetas en varios idiomas | `MessageSource` + `<spring:message>` |
| Evitar XSS al dibujar datos dinamicos | `<c:out>` |
| Cambiar wording por locale | i18n, no `if` manual por idioma |

## Mezclas incorrectas

| Mezcla | Riesgo |
|--------|--------|
| JSP con `${...}` directo para texto libre | XSS |
| Controller armando errores campo por campo | Mucho boilerplate y UX pobre |
| Mensajes concatenados en distintos fragmentos | Frases incorrectas al cambiar de idioma |

## Recomendacion operativa

Pensalo asi:  
JSP dibuja.  
Form bean valida.  
i18n redacta.

## Ver tambien
- [[jsp-jstl]]
- [[validacion-formularios]]
- [[internacionalizacion]]
- [[xss-prevencion]]
- [[resumen-apuntes]]
- [[resumen-notas]]
- [[resumen-correcciones]]
