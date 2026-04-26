---
titulo: Auth Flows y Ciclo de Cuenta
tipo: concepto
fuentes: [raw/correcciones_tp1.md]
creado: 2026-04-13
actualizado: 2026-04-13
---

# Auth Flows y Ciclo de Cuenta

Las correcciones de PAW no evaluan solo si "hay login". Tambien miran si los flujos de cuenta quedan completos, seguros y coherentes con Spring Security.

## Flujos que la catedra espera cubiertos

- Login
- Logout
- Cambio de contrasena
- Recuperacion de contrasena
- Verificacion de cuenta
- Bloqueo / estado del usuario
- Ownership sobre recursos ya autenticados

## Login, logout y sesion

- El login deberia ofrecer una salida clara a home; se marco la ausencia de ese CTA.
- El POST de login no deberia existir en un controller propio; Spring Security lo maneja automaticamente.
- El logout tampoco deberia exponerse como endpoint manual POST `/logout`.
- La `rememberMeKey` debe venir de configuracion (`@Value`), no quedar hardcodeada ni ser trivial.
- Las credenciales o secretos de auth / mailing / DB no deben vivir en el repo.

## Cambio y recuperacion de contrasena

- El cambio de contrasena deberia pedir la contrasena actual.
- Tambien deberia existir confirmacion de contrasena.
- Falta recuperacion de contrasena en varios casos corregidos.
- `resetPassword` no deberia mezclar validacion de usuario con el reseteo mismo.
- En reset de contrasena no deberia enviarse `userId` junto al token; el token ya identifica al usuario.
- Como mejora de UX, tras restablecer la contrasena puede tener sentido auto-loguear al usuario.

## Verificacion y estado de cuenta

- No conviene borrar directamente usuarios expirados no verificados; es preferible permitir regenerar el token.
- El bloqueo de usuarios deberia resolverse con `accountNonLocked`, no con chequeos ad-hoc en helpers o controllers.
- Las reglas de sesion no deberian producir side effects en `GET /`, como persistir locale en DB.

## Ownership y acceso a recursos

- No alcanza con proteger por rol; hace falta ownership de recursos.
- Se marcaron casos concretos donde un usuario podia editar publicaciones ajenas, eliminar viajes ajenos o acceder a vistas sin el rol correcto.
- La regla correcta es concentrar estas decisiones en Spring Security, no dispersarlas en controllers o services.
- Como regla base del wiki, los codigos HTTP deben respetar esta semantica:
  - `403` cuando el recurso existe pero el usuario no tiene permiso
  - `404` cuando el recurso no existe
- La fuente registra algunos endpoints concretos donde la implementacion devolvia el status inverso; tratar esos casos como decisiones de endpoint y no como una regla universal para esconder recursos.

## Implementacion recomendada

1. Resolver login/logout/remember-me con Spring Security.
2. Llevar cambio, reset y verificacion de cuenta a servicios dedicados con contratos claros.
3. Validar formularios de auth con Bean Validation, no a mano en controller.
4. Mantener ownership declarativo con expressions, voters o helpers conectados a Spring Security.
5. Completar UX minima: CTA de vuelta a home, confirmacion de password y mails de recuperacion/verificacion utiles.

## Ver tambien

- [[http-y-sesiones]]
- [[spring-security]]
- [[internacionalizacion]]
- [[jsp-jstl]]
- [[validacion-formularios]]
- [[mailing]]
- [[manejo-excepciones]]
- [[ux-flows]]
- [[resumen-apuntes]]
- [[resumen-correcciones]]
- [[resumen-notas-sprint-2]]
- [[2026-04-24_auditoria-implementacion-contra-wiki_v1]]
