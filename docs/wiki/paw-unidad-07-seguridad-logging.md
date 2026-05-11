---
titulo: PAW Unidad 7 - Seguridad y Logging
tipo: fuente
fuentes: [raw/apuntes.txt, raw/pdfs/PAW - Apuntes.pdf]
creado: 2026-05-11
actualizado: 2026-05-11
---

# PAW Unidad 7 - Seguridad y Logging

Esta unidad cierra la base TP1 con control de acceso y observabilidad. Seguridad no es solo login; tambien es ownership, roles, estados de cuenta, CSRF, remember-me, errores y logs que permitan operar la app.

## Spring Security

- Spring Security se instala como filtro antes del `DispatcherServlet`.
- La configuracion de auth debe vivir separada de controllers.
- `HttpSecurity` define reglas de URL, login, logout, remember-me, CSRF y errores.
- El orden de reglas importa: la primera regla que matchea gana.
- El catch-all debe quedar al final.

## Autenticacion

- El flujo delega en providers y `UserDetailsService`.
- `UserDetailsService` adapta usuarios propios a `UserDetails`.
- `PasswordEncoder` compara credenciales hasheadas.
- BCrypt es el criterio correcto para TP real.
- Plain text solo aparece como ejemplo basico, no como recomendacion.

## Roles y perfiles

- La cursada distingue roles de perfiles de usuario.
- No conviene asumir mapeo 1:1 entre "tipo de usuario" y authority.
- Las authorities viven en un `Set`, por lo que multiples roles por usuario son normales.
- Ownership y permisos de recurso deben validarse del lado servidor.

## Login, logout y remember-me

- El GET de login renderiza la vista.
- El POST de login lo maneja Spring Security.
- Logout tambien deberia apoyarse en Spring Security salvo razon explicita.
- Remember-me depende de una key criptografica; si cambia, invalida cookies previas.
- Esa key no debe quedar hardcodeada en codigo fuente.

## Logging

- SLF4J es la fachada; Logback es la implementacion.
- Desarrollo puede usar consola y `DEBUG`.
- Produccion no deberia usar `DEBUG` global ni depender de stdout.
- `logback-test.xml` permite configuracion local/de tests.
- `logback.xml` productivo puede usar archivos, rolling policy e historial acotado.
- Usar placeholders `{}` y pasar excepciones como ultimo argumento.

## Para TP1

- No confiar en UI para permisos.
- Modelar 401, 403, logout y access denied con rutas claras.
- Usar BCrypt y config externa para secretos.
- No loguear datos sensibles.
- Revisar logs productivos antes de entregar.

## Ver tambien

- [[resumen-apuntes]]
- [[spring-security]]
- [[auth-flows]]
- [[logging]]
- [[xss-prevencion]]
- [[paw-unidad-06-formularios-i18n]]
- [[paw-unidad-08-aop-transacciones]]
