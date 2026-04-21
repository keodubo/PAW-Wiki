# Plan De Implementacion Secuencial Para Bugs De Deploy

## Objetivo

Corregir los 20 bugs reportados en deploy agrupandolos por seam compartido para minimizar retrabajo, reducir regresiones y poder verificar cada lote con alcance claro.

## Recomendacion Fuerte

Seguir un orden **riesgo primero + leverage point compartido**.

No recomiendo atacar esto como 20 tickets sueltos porque:

- varios bugs caen sobre el mismo controller, JSP compartido o JS comun;
- hay items que en el checkout actual ya aparecen parcialmente resueltos;
- hay al menos un bug (`414`) que parece requerir coordinacion de **app + servidor/deploy** para que en produccion se vea una pagina custom de Forkd y no la default del servidor.

## Hallazgos Del Checkout Actual

Estos puntos importan para no planificar trabajo duplicado:

- `#1 /register/pending`: hoy sigue permitiendo editar el email; este bug existe en el checkout.
- `#2 delete account`: el centrado depende casi por completo de `profile/delete.jsp`; es un ajuste localizado de vista/layout.
- `#3 avatar fallback`: ya existe `userAvatar.tag`, pero `restaurant.jsp` en reviews publicas sigue renderizando un fallback gris manual.
- `#6 414`: la app ya tiene `error/414.jsp`, mensajes i18n y `error-page` en `web.xml`; si en deploy aparece la pagina default de Apache, falta revisar la capa de servidor/proxy para que termine mostrando una pagina custom de Forkd.
- `#9 wording corregido`: consolidarlo con `#13`; el gap real es **owner delete**, no admin delete. El delete admin ya existe en el checkout actual.
- `#19 post reset`: el flow actual ya redirige a `/login?reset=true`, pero la vista solo muestra un mensaje; no hay CTA explicita.

## Agrupacion Recomendada

| Grupo | Bugs | Leverage point principal | Tipo |
| --- | --- | --- | --- |
| A. Infra y comportamiento compartido | `6`, `7`, `17`, `18` | `ErrorHandlingAdvice`, `MultipartRequestSizeFilter`, `forkd.js`, servicios de mail | cross-cutting |
| B. Auth y cuenta | `1`, `2`, `19`, `20` | `AuthController`, `ProfileController`, vistas auth/profile | vertical auth/account |
| C. Menu y media | `14`, `15`, `16`, parte de `17` | `shared/menu-form-body.jspf`, `forkd.js`, `MenuForm`, `RestaurantServiceImpl` | shared owner/admin |
| D. Moderacion admin | `5`, `8`, `10`, `11` | `AdminController`, `pending-restaurants.jsp`, `unassigned-restaurants.jsp`, `assign-owner.jsp` | vertical admin |
| E. Dashboard owner | `9`, `12`, `13` | `OwnerDashboardController`, `owner/dashboard.jsp`, delete contract | owner lifecycle |
| F. Perfil/publico | `3`, `4` | `restaurant.jsp`, `userAvatar.tag`, `saved-restaurants.jsp` | low-risk UI |

## Secuencia Recomendada

### Lote 0. Auditoria Corta Deploy Vs Checkout

**Objetivo:** separar bugs realmente pendientes de bugs ya resueltos en codigo pero no reflejados en deploy.

**Incluye:**

- confirmar estado real de `#3`, `#6` y `#19`, y normalizar el alcance correcto de `#9/#13`;
- capturar para cada bug: URL, rol, pasos minimos, resultado esperado, resultado actual;
- confirmar si el `414` lo responde Apache antes de llegar a Tomcat.

**Salida esperada:**

- matriz `bug -> missing in checkout / partially fixed / likely deploy-only`.

**Por que va primero:**

- evita reimplementar cosas ya hechas;
- evita mezclar fixes de app con fixes de infraestructura.

### Lote 1. Infra Y Guardrails Compartidos

**Bugs:** `6`, `7`, `17`, `18`

**Orden interno recomendado:**

1. `#17` auditar backend de subida de fotos en **perfil** y **menu**.
2. `#7` desactivar multiclick en submits sensibles.
3. `#18` cerrar mails que todavia no respeten `preferredLocale`.
4. `#6` resolver el caso `414` para que en deploy se vea una pagina custom de Forkd y no la default del servidor.

**Por que estos juntos:**

- tocan comportamiento transversal que puede afectar varios flujos al mismo tiempo;
- conviene estabilizarlos antes de arreglar UX puntual arriba de ellos.

**Alcance tecnico:**

- uploads:
  - validar `ProfileEditForm`, `MenuForm`, `ValidUploadedImageValidator`, persistencia de `imageId` al editar;
  - revisar errores inline vs pagina global;
  - chequear que editar sin subir nueva imagen no borre imagen existente.
- multiclick:
  - agregar proteccion compartida en `forkd.js` o en forms/botones comunes;
  - cuidar no romper formularios con `data-confirm-form`.
- locale mails:
  - auditar cada emisor contra el locale del receptor real;
  - reforzar tests de servicios para no volver a request-locale accidentalmente.
- `414`:
  - si el request llega a la app, usar la `414` propia de Forkd;
  - si Apache corta antes, configurar `ErrorDocument 414` o equivalente para que deploy muestre una pagina custom de Forkd y no la default de Apache/Tomcat.

**Riesgos:**

- `#7` puede romper modales de confirmacion o dobles submits legitimos si se implementa demasiado agresivo;
- `#18` toca flows async y conviene dejar cobertura automatizada antes de deployar.

**Verificacion minima:**

- `mvn -pl webapp -am test`
- `mvn -pl services -am test`
- smoke manual de:
  - upload perfil valido/invalido/oversize;
  - upload menu valido/invalido/oversize;
  - doble click en approve/reject/save/delete;
  - mails ES/EN por reviewer, owner y admin;
  - URL/search demasiado larga en deploy.

### Lote 2. Auth Y Cuenta

**Bugs:** `1`, `2`, `19`, `20`

**Orden interno recomendado:**

1. `#1` arreglar `/register/pending`.
2. `#20` mantener sesion luego de cambiar contrasena estando logueado.
3. `#19` agregar CTA clara para ir al login luego del reset.
4. `#2` centrar confirmacion de borrado de cuenta.

**Por que estos juntos:**

- caen sobre `AuthController`, `ProfileController` y vistas del mismo dominio funcional;
- `#19` y `#20` comparten el flow de password reset/change-password.

**Alcance tecnico:**

- `#1`:
  - sacar input editable del email;
  - mostrar a que direccion se reenvia;
  - dejar solo accion de reenviar;
  - idealmente no depender de que el usuario vuelva a postear el mail a mano.
- `#20`:
  - evitar que el flow actual “emitir token -> reset anonimo -> redirect login” rompa la sesion del usuario autenticado;
  - resolver si se refresca autenticacion despues del cambio o si se agrega un endpoint especifico para cambio autenticado.
- `#19`:
  - puede resolverse en la vista de login cuando `reset=true`, sin crear otra pantalla.
- `#2`:
  - ajuste visual localizado; hacerlo dentro de este lote evita otra pasada sobre account UX.

**Riesgos:**

- `#20` toca seguridad/sesion; hay que validar que no se abra un bypass raro de password reset.

**Verificacion minima:**

- tests MVC de registro pending, resend, password reset y change-password;
- smoke manual:
  - registro nuevo -> pending -> resend;
  - usuario logueado cambia password y sigue autenticado;
  - reset anonimo muestra CTA clara;
  - delete account queda centrado.

### Lote 3. Menu Y Media

**Bugs:** `14`, `15`, `16`, parte menu de `17`

**Orden interno recomendado:**

1. `#15` diagnosticar y corregir “no me deja editar/crear el menu”.
2. `#16` corregir el “Cancelar” duplicado.
3. `#14` agregar orden y filtrado a tags.
4. cerrar restos de `#17` especificos del menu.

**Por que este lote va antes que admin/owner listados:**

- `#15` es un bug de uso bloqueante;
- admin y owner comparten exactamente el mismo body del menu.

**Alcance tecnico:**

- revisar `shared/menu-form-body.jspf`, `forkd.js`, `MenuForm`, `OwnerDashboardController`, `AdminController`;
- confirmar si el problema esta en:
  - wizard steps;
  - submit oculto/visible;
  - binding de `menuSections`;
  - validacion;
  - uploads de items;
  - rerender con errores.
- `#14`:
  - agregar filtrado local sobre `suggestedTags`;
  - definir orden estable, idealmente alfabetico o por canon del catalogo;
  - no romper el orden persistido de tags ya seleccionados.

**Riesgos:**

- es facil arreglar solo owner o solo admin y dejar el otro roto;
- el JS compartido puede introducir regresiones silenciosas si no se prueba ambos flows.

**Verificacion minima:**

- owner create/update menu;
- admin create/update menu;
- error de validacion re-renderiza en paso correcto;
- tags se pueden encontrar/ordenar;
- uploads de items conservan imagen previa si no se reemplaza.

### Lote 4. Moderacion Admin

**Bugs:** `5`, `8`, `10`, `11`

**Orden interno recomendado:**

1. `#5` agregar link de revision desde `/admin/pending` a `/restaurants/{id}`.
2. `#8` mejorar cards de pendientes.
3. `#10` agregar filtros en `/admin/restaurants/assign-owner`.
4. `#11` redirigir POST de assign-owner a `/admin/restaurants/assign-owner`.
**Por que estos juntos:**

- todo cae sobre `AdminController` y las vistas de moderacion;
- `#10` y `#11` comparten el mismo listado y el mismo estado GET;
- `#5` y `#8` son la misma pantalla.

**Alcance tecnico:**

- `#5`:
  - usar `/restaurants/{id}`; el checkout actual ya soporta visibilidad condicionada por rol en detalle/menu.
- `#8`:
  - rediseñar la card sin cambiar el contrato del approve/reject;
  - preservar filtros, page, sort y contexto al volver.
- `#10`:
  - hoy `/assign-owner` solo pagina; esto probablemente requiere extender service + DAO, no solo JSP.
- `#11`:
  - despues de asignar owner, volver al listado de unassigned, idealmente preservando estado si se decide soportarlo.
**Riesgos:**

- `#10` puede crecer si no se decide rapido si los filtros seran por `query`, `cuisine`, `neighborhood`, `status` o subset;
- `#11` sin preservar GET state puede sentirse inconsistente si se agregan filtros en el mismo lote.

**Verificacion minima:**

- admin pending con preview, approve y reject;
- assign-owner con filtros + paginacion + redirect correcto.

### Lote 5. Dashboard Owner Y Ciclo De Vida

**Bugs:** `9`, `12`, `13`

**Orden interno recomendado:**

1. `#12` filtros en `/my-restaurants`.
2. `#9/#13` permitir borrar restaurantes a owners.

**Por que estos juntos:**

- ambos tocan `OwnerDashboardController` y `owner/dashboard.jsp`;
- ambos afectan el lifecycle operativo del owner sobre su lista;
- `#9` queda absorbido aca porque el usuario aclaro que el wording correcto era owner delete.

**Alcance tecnico:**

- `#12`:
  - hoy `/my-restaurants` solo pagina;
  - probablemente requiere extender contrato de service/DAO, igual que `assign-owner`.
- `#9/#13`:
  - hoy no existe ruta owner delete;
  - reutilizar `restaurantService.delete(...)` solo si la autorizacion queda cerrada del lado owner;
  - agregar confirmacion y feedback consistente.

**Riesgos:**

- `#9/#13` no debe permitir borrar restaurantes ajenos ni saltearse reglas de ownership;
- si `#12` agrega filtros, hay que preservar `page`, `pageSize`, `sort` y filtros luego de delete.

**Verificacion minima:**

- owner filtra, pagina y mantiene estado;
- owner borra solo sus restaurantes;
- luego de delete vuelve al dashboard en estado coherente.

### Lote 6. Perfil/Publico De Bajo Riesgo

**Bugs:** `3`, `4`

**Orden interno recomendado:**

1. `#3` reemplazar fallback gris por letra en las superficies que falten.
2. `#4` boton rojo para quitar guardado en `/profile/{slug}/saved-restaurants`.

**Por que al final:**

- son fixes acotados y de bajo riesgo;
- dependen poco de los lotes anteriores.

**Alcance tecnico:**

- `#3`:
  - reutilizar `userAvatar.tag` donde todavia haya markup manual;
  - el hueco visible hoy esta en `restaurant.jsp` reviews.
- `#4`:
  - es vista/estilo sobre un POST ya existente que preserva estado GET.

**Verificacion minima:**

- reviewer sin foto ve inicial en restaurant detail y demas superficies;
- remove saved restaurant se ve como accion destructiva y mantiene filtros/paginacion.

## Dependencias Entre Lotes

- `Lote 0` desbloquea todo el resto.
- `Lote 1` debe ir antes de `Lote 2` y `Lote 3` porque toca uploads, submit behavior y locale mail.
- `Lote 3` conviene antes de `Lote 4` y `Lote 5` porque contiene un bug bloqueante de menu.
- `Lote 4` y `Lote 5` comparten patron de filtros/listados; pueden reutilizar la misma estrategia una vez resuelto el primero.

## Verificacion Final Recomendada

Al cerrar todo, hacer una pasada final por:

- reviewer:
  - register pending, login, reset password, delete account, saved restaurants, avatars;
- owner:
  - my-restaurants, filtros, delete, menu create/edit, uploads, reservation settings;
- admin:
  - pending, preview, approve/reject, assign-owner filtros, redirect;
- deploy/infrastructure:
  - `414`, mails ES/EN, multiclick en acciones destructivas y de guardado.

## Orden Alternativo

**Alternativa posible:** hacer primero los “quick wins” visuales (`2`, `3`, `4`, `8`, `16`) y despues los flows complejos.

**No la recomiendo** porque:

- deja abierto `#15`, que es bloqueante;
- posterga `#7`, `#17` y `#18`, que pueden seguir generando fallas transversales;
- puede hacer mas dificil distinguir regresiones nuevas de bugs viejos.

## Next Actions

- [ ] Ejecutar `Lote 0` y marcar para cada bug si ya esta resuelto parcial o totalmente en el checkout.
- [ ] Confirmar la estrategia final para que `#6` muestre una pagina custom de Forkd en deploy y no la default del servidor.
- [ ] Arrancar por `Lote 1` y `Lote 2` antes de tocar polish visual.
- [ ] Tratar `#15` como bug bloqueante del siguiente lote funcional.
- [ ] Reusar tests MVC/service por lote y cerrar con una smoke QA final en deploy.
