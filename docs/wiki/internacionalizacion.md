---
titulo: Internacionalizacion (i18n)
tipo: concepto
fuentes: [raw/correcciones_tp1.md, raw/apuntes.txt, raw/notas.txt, "raw/final paw/Clase 8 - FINAL.pdf", "raw/final paw/como esta implementado el tp de ejemplo.docx"]
creado: 2026-04-09
actualizado: 2026-07-05
---

# Internacionalizacion (i18n)

La internacionalizacion nativa y completa es un requerimiento core obligatorio.

## Errores detectados

- `MessageSource` configurado pero apuntando a archivos inexistentes — requisito fallado explicitamente
- Titulos de paginas no internacionalizados
- Alt texts de imagenes no internacionalizados
- Textos estaticos en plural sin pluralizacion dinamica (usar `ChoiceFormat`)
- Mensajes crudos como "email.duplicate" derramandose al frontend sin resolver
- Errores de encoding en caracteres tildados (tildes, n)

### Locale en mailing
- Emails con idioma del sender en vez del destinatario (ver: [[mailing]])
- Actualizar Locale en DB durante GET a `/` — mala practica HTTP

## Patron correcto

- Archivos `messages_xx.properties` para cada idioma soportado
- Todo texto visible al usuario resuelto via `<spring:message>`
- Alt texts y titulos incluidos en i18n
- Pluralizacion con `ChoiceFormat` o messages parametrizados
- Encoding UTF-8 en todos los JSPs y properties

## Implementacion segun apuntes

- Crear `MessageSource` bean en WebConfig
- Archivos en `src/main/resources/i18n/`: `messages.properties` (default), `messages_es.properties`, `messages_en.properties`
- Los mensajes de error de validacion tambien se resuelven via MessageSource (Bean Validation busca por codigo)
- `LocaleResolver` + `LocaleChangeInterceptor` para cambiar idioma via parametro

## Jerarquia de archivos y fallback

- `messages.properties` es el default
- `messages_en.properties` aplica al idioma ingles
- `messages_en_US.properties` aplica al locale mas especifico ingles de Estados Unidos
- La busqueda intenta primero el archivo mas especifico y cae progresivamente a uno menos especifico

## Claves de error y mensajes por campo

- Puede definirse una clave global, por ejemplo `Size=Incorrect Length`
- Si hace falta mas precision, se puede bajar a nivel de formulario/campo: `Email.registerForm.email=Field must be an email`
- Los placeholders permiten incorporar parametros dinamicos sin romper la gramatica del idioma
- Ejemplo de clase: `Size.registerForm.password=Password needs to have at least {2} characters`

## Regla critica de interpolacion (segun clases)

- **Nunca concatenar** strings parciales para construir mensajes i18n (la estructura gramatical varia entre idiomas)
- **Siempre** internacionalizar el mensaje completo con placeholders: `<spring:message code="greeting" arguments="${name}"/>`
- El atributo `arguments` acepta lista separada por comas (configurable con `argumentSeparator`)
- `<spring:message>` tambien permite escapar para HTML/JavaScript (similar a `<c:out>`)

## TP final: i18n frontend

- La SPA ya no puede depender solo de `MessageSource` y JSPs: necesita catalogos del lado cliente.
- React suele usar `react-i18next`; Angular puede usar `@angular/localize`; Vue puede usar `vue-i18n` u otra libreria equivalente.
- Mantener pluralizacion, interpolacion, fallback y formatos por locale (fecha, hora, moneda) tambien en el frontend.
- Las keys del frontend y backend no tienen que ser identicas, pero deben cubrir los mismos flujos visibles: formularios, errores API, estados vacios, auth y navegacion.
- No concatenar fragmentos traducidos en componentes; usar mensajes completos con placeholders.

## Ver tambien
- [[comparacion-jsp-formularios-e-i18n]]
- [[auth-flows]]
- [[single-page-applications]]
- [[checklist-tp-final-rest-spa]]
- [[mailing]]
- [[xss-prevencion]]
- [[jsp-jstl]]
- [[ux-flows]]
- [[validacion-formularios]]
- [[resumen-enunciado]]
- [[resumen-correcciones]]
- [[resumen-apuntes]]
- [[resumen-notas]]
- [[remediacion-violaciones-paw]]

- [[auditoria-extrema-cumplimiento-paw]]
- [[plan-implementacion-reservas]]
- [[2026-04-24_auditoria-implementacion-contra-wiki_v1]]
