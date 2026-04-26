---
titulo: Mailing — Patrones y Errores
tipo: concepto
fuentes: [raw/correcciones_tp1.md, raw/apuntes.txt]
creado: 2026-04-09
actualizado: 2026-04-17
---

# Mailing — Patrones y Errores

El mailing es un requerimiento core. La catedra evalua tanto la funcionalidad como la implementacion arquitectonica.

## Errores detectados

### Violacion de capas (error conceptual grave)
- `EmailService` que referencia templates/paths de webapp — el servicio no debe conocer la capa web
- La interfaz no debe distinguir entre `MailService` y `AsyncMailService` — async es detalle de implementacion

### Locale incorrecto
- Emails enviados con el idioma del sender en vez del destinatario
- Obtener Locale de `LocaleContextHolder` o inyectado por controller — usar `preferredLanguage` del destinatario
- Mail asincronos: el Locale del thread actual no es el del destinatario

### Envio
- Loop sincronico de envio de emails — usar hilo asincrono (`@Async`)
- Credenciales de Gmail hardcodeadas y trackeadas en git

### Contenido
- Mails sin formato HTML estandar
- Falta Header y CTAs (Call to Action) como botones
- Links como `href` sueltos en vez de botones estilizados
- Mail de pago sin detalles ni CBU en HTML
- Si el owner cancela una reserva, el usuario cancelado deberia recibir notificacion

## Elogios

Grupos con buen mailing fueron elogiados:
> "Agradable funcionalidad de mails con flujos y plantillas L10n bien compuestas. CTA asertivos que redirigen bien."

## Implementacion segun apuntes

- **Librerias**: Thymeleaf para templates + javax.mail
- **Formato**: header (web) + body (razon del mail) + CTA (redireccion a la pagina) + footer (opcional)
- **Capa**: SIEMPRE desde servicios, nunca desde webapp
- **No bloqueante**: usar `@Async` en el servicio de mail (habilitar con `@EnableAsync` en WebConfig)
- **Contenido**: mails deben contener informacion que el usuario no conozca
- **i18n**: mails internacionalizados, usar locale del destinatario

## Ver tambien
- [[comparacion-capas-web-services-persistence]]
- [[http-y-sesiones]]
- [[auth-flows]]
- [[buenas-practicas]]
- [[internacionalizacion]]
- [[logica-en-controllers]]
- [[modelo-capas]]
- [[resumen-enunciado]]
- [[resumen-correcciones]]
- [[resumen-notas-sprint-1]]
- [[resumen-notas-sprint-2]]
- [[resumen-apuntes]]
- [[ux-flows]]

- [[auditoria-extrema-cumplimiento-paw]]
- [[plan-implementacion-reservas]]
- [[resumen-spec-reservas]]
