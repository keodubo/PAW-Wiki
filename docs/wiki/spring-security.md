---
titulo: Spring Security — Control de Acceso Declarativo
tipo: concepto
fuentes: [raw/correcciones_tp1.md, raw/apuntes.txt, raw/notas.txt]
creado: 2026-04-09
actualizado: 2026-04-17
---

# Spring Security — Control de Acceso Declarativo

Para el criterio de la catedra, el control de acceso no debe resolverse manualmente en controllers o servicios; debe concentrarse en Spring Security y sus mecanismos declarativos.

## Regla central

En general, los chequeos de permisos deben resolverse de forma **declarativa** via Spring Security, no con `if-else` manuales dispersos por el codigo.

## Anti-patrones detectados en correcciones

### Chequeos manuales en controllers (error conceptual grave)

Estos patrones estan prohibidos:
- `if (loggedUser == null) { return "redirect:/login"; }`
- `if (!user.isOwner(resource)) { return "403"; }`
- `clubAdminService.validateAdmin(clubId);` en controllers
- `if (currentPatient == null)` como doble chequeo de seguridad

Estos chequeos duplican logica, usan magic strings para roles, y son fragiles.

### Inconsistencia en la configuracion
- Mezclar reglas en `WebAuthConfig` con `@PreAuthorize` en controllers — elegir una sola forma
- `@PreAuthorize` con logica de negocio (ej: verificar ownership) cuando deberia ser un Access Decision Voter o expression custom

### Falta de control por ownership
- Solo validar roles en rutas no alcanza
- Se necesita control fino: "este recurso pertenece a este usuario"
- Implementar voters o expressions custom de Spring Security

### Errores de configuracion de rutas
- `/**` con `.authenticated()` antes de rutas especificas anula reglas de rol — **error grave**
- Rutas con nivel restrictivo seguido de `permitAll()` se contradicen
- Ignorar rutas en `WebSecurity` y luego incluirlas en `HttpSecurity`

## Patron correcto

1. **WebAuthConfig** define reglas de URL por rol y autenticacion
2. **Voters/expressions** custom resuelven ownership de recursos
3. **Taglib de Spring Security** en JSPs para mostrar/ocultar elementos segun rol
4. **Codigos HTTP correctos**: como regla base, 403 si el recurso existe pero no tiene permisos; 404 si el recurso no existe
5. **Logout** lo maneja Spring Security — no crear endpoint POST `/logout` manual

## Otros puntos

- Usuarios bloqueados: usar `accountNonLocked` de Spring Security, no chequeos manuales
- `rememberMeKey` debe ser un `@Value` inyectado, no hardcodeado (mencionado 3 veces)

### Flujos de cuenta

- Login, logout, remember-me, reset de password y verificacion de cuenta deben verse como un flujo completo, no solo como una lista de endpoints.
- CTA de vuelta a home, confirmacion de password y regeneracion de tokens vencidos son detalles de UX, pero tambien evitan flujos incompletos de autenticacion.
- Ver tambien: [[auth-flows]]

## Implementacion segun apuntes

### Componentes clave
- **springSecurityFilterChain**: Filter en web.xml que intercepta todos los requests
- **WebSecurityConfigurerAdapter**: clase base para WebAuthConfig
- **UserDetailsService**: implementar `PawUserDetailService` que busca usuarios en la BD
- **PawAuthUser**: clase custom que extiende User de Spring Security, permite agregar datos propios
- **BCryptPasswordEncoder**: obligatorio para el TP (no PlainText). Encodear al crear/modificar password

### Roles
- Authorities son un Set: un usuario puede tener multiples roles (ROLE_USER, ROLE_ADMIN, ROLE_TEAM_OWNER)
- No asumir herencia entre roles — definir cada regla explicitamente
- Los constructores de User de Spring Security diferencian usuarios bloqueados, desactivados, con passwords expiradas

### Version
- Spring Security tiene versionado independiente de Spring Framework
- Definir su version aparte en el pom padre

## Detalles practicos de las clases

### Arquitectura de filtro
- Spring Security opera como un `<filter>` en `web.xml`, no como servlet
- El filter name debe ser exactamente `springSecurityFilterChain` (Spring lo busca por ese nombre)
- Intercepta TODOS los requests antes de llegar al DispatcherServlet
- Recursos estaticos (CSS, JS) se excluyen via `WebSecurity.ignoring()` para evitar evaluacion innecesaria

### Login y logout automaticos
- Spring Security maneja el POST de login automaticamente: recupera credenciales, valida, genera sesion, redirige
- Solo se implementa el GET del login (dibujar el formulario); el POST nunca se crea en un controller
- El logout tambien es automatico al configurar `.logout().logoutUrl("/logout")`
- `defaultSuccessUrl("/", false)` — el `false` redirige a la pagina que el usuario intentaba acceder antes del login

### Obtener usuario actual
- `SecurityContextHolder.getContext().getAuthentication()` retorna el usuario logueado
- Internamente usa `ThreadLocal`: cada thread tiene su propia sesion de seguridad
- `LocaleContextHolder` funciona igual para el idioma del usuario

### RememberMe — cookie y key criptografica
- La cookie de RememberMe usa primitivas criptograficas para validar integridad y frescura
- Si la key se genera en runtime (default), reiniciar el servidor invalida todas las cookies
- Solucion: generar key con `openssl rand -base64 2048`, guardarla en archivo en `src/main/resources`, leerla con `@Value`
- No hardcodear la key en el codigo

### Mensajes de error en login
- No revelar si el email existe o si la password es incorrecta (leakea informacion)
- Usar mensaje generico: "credenciales invalidas"

### Expression Language para ownership
- Para control de acceso basado en ownership (editar solo mis posts): `.access("@AccessHelper.canEdit")`
- Permite atar funciones Java con reglas de negocio directamente a la configuracion de seguridad
- Documentado en Spring Security bajo "Expression Language"

## Ver tambien
- [[comparacion-seguridad-web]]
- [[http-y-sesiones]]
- [[spring-web-mvc]]
- [[auth-flows]]
- [[tp1-vs-tpe2-final]]
- [[logging]]
- [[buenas-practicas]]
- [[criterios-evaluacion]]
- [[xss-prevencion]]
- [[logica-en-controllers]]
- [[modelo-capas]]
- [[resumen-enunciado]]
- [[resumen-correcciones]]
- [[resumen-apuntes]]
- [[resumen-notas]]

- [[auditoria-extrema-cumplimiento-paw]]
- [[plan-ejecucion-remediacion-auditoria]]
- [[plan-implementacion-reservas]]
- [[resumen-spec-reservas]]
