# Lote 0 - Auditoria Corta Deploy Vs Checkout

## Objetivo

Separar bugs realmente pendientes en el checkout actual de bugs ya resueltos total o parcialmente en codigo, y de bugs que hoy son de infraestructura/deploy.

## Alcance

- Repo auditado: `paw-2026a-01`
- Plan base: `2026-04-20_deploy-bugs-remediation-plan_v1.md`
- Fuente original de bugs: `todo.md`
- Deploy auditado el `2026-04-20`: `http://pawserver.it.itba.edu.ar/paw-2026a-01`

## Metodo

- Lectura de `todo.md`, controllers, JSPs, tests y mensajes i18n del checkout actual.
- Verificacion puntual contra deploy para los bugs con estado dudoso o con sospecha de drift:
  - `#1`
  - `#3`
  - `#6`
  - `#9`
  - `#16`
  - `#19`
- Restriccion deliberada:
  - no hice cambios con efecto en datos de deploy;
  - no probe flows destructivos o que cambien contrasenas sobre usuarios reales del deploy.

## Estados Usados

- `Pendiente en checkout`: el bug sigue visible en codigo actual y debe implementarse.
- `Parcial en checkout`: hay cobertura o intentos claros, pero falta cerrar el caso reportado.
- `Resuelto en checkout/deploy`: no amerita entrar al backlog activo salvo que el alcance real sea mas amplio.
- `Probable deploy/infra`: el problema no entra hoy por la app o depende de Apache/proxy.
- `Consolidado con otro bug`: el wording original se corrige y se absorbe en otro item ya existente.
- `Requiere QA visual/runtime`: el codigo no alcanza para confirmar si ya esta bien o mal; hace falta reproducirlo.

## Resumen Ejecutivo

### Conteo

| Estado | Cantidad |
| --- | ---: |
| Pendiente en checkout | 12 |
| Parcial en checkout | 2 |
| Resuelto en checkout/deploy | 1 |
| Probable deploy/infra | 1 |
| Consolidado con otro bug | 1 |
| Requiere QA visual/runtime | 3 |

### Conclusiones Fuertes

- `#6` hoy exige coordinacion de infraestructura: el `414` lo responde Apache antes de llegar a la app, pero el objetivo sigue siendo mostrar una pagina custom de Forkd.
- `#9` no es admin delete: por aclaracion del usuario se consolida con `#13` y el gap real pasa a ser owner delete.
- `#16` parece ya resuelto: el form de menu compartido y el deploy auditado muestran un solo `Cancelar`.
- `#1`, `#3`, `#19` siguen pendientes tanto en checkout como en deploy.

## Matriz De Auditoria

### `#1` Register pending no debe permitir cambiar el email

- URL: `/register/pending`
- Rol: anonimo
- Paso minimo: abrir la pantalla pending luego de registrarse
- Esperado: mostrar a que email se reenvia y permitir solo reenviar
- Actual checkout: la vista sigue renderizando `<form:input path="email" ... type="email">`
- Actual deploy: la pantalla publica sigue mostrando un input editable de email
- Estado lote 0: `Pendiente en checkout`
- Evidencia:
  - `webapp/src/main/webapp/WEB-INF/views/auth/register-pending.jsp`
  - `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/AuthController.java`
- Siguiente lote: `Lote 2`

### `#2` Delete account descentrado

- URL: `/profile/{username}/delete`
- Rol: reviewer autenticado
- Paso minimo: entrar a eliminar cuenta desde perfil
- Esperado: pantalla y card visualmente centradas
- Actual checkout: la vista fue trabajada y tiene estilos localizados de layout/centrado
- Actual deploy: no verificado visualmente en este lote
- Estado lote 0: `Requiere QA visual/runtime`
- Evidencia:
  - `webapp/src/main/webapp/WEB-INF/views/profile/delete.jsp`
- Nota: el bug parece acotado a esa vista, pero no quise inferir resultado visual solo por HTML/CSS
- Siguiente lote: `Lote 2`

### `#3` Se sigue viendo el redondel gris en vez de la letra para users sin foto

- URL: `/restaurants/{id}` en reviews publicas
- Rol: anonimo o reviewer
- Paso minimo: abrir un restaurante con reviews de usuarios sin foto
- Esperado: usar la misma logica de fallback por inicial que el resto del perfil
- Actual checkout: `restaurant.jsp` sigue renderizando un `div` vacio gris en el fallback de reviews publicas
- Actual deploy: reproducido en `/restaurants/24` y `/restaurants/26`, donde aparecen avatars sin letra
- Estado lote 0: `Pendiente en checkout`
- Evidencia:
  - `webapp/src/main/webapp/WEB-INF/views/restaurant.jsp`
  - `webapp/src/main/webapp/WEB-INF/tags/userAvatar.tag`
- Siguiente lote: `Lote 6`

### `#4` Saved restaurants deberia tener boton rojo para quitar de guardado

- URL: `/profile/{slug}/saved-restaurants`
- Rol: reviewer autenticado
- Paso minimo: abrir guardados propios
- Esperado: CTA destructiva visualmente marcada
- Actual checkout: el boton usa `btn btn--secondary`, no un estilo de accion destructiva
- Actual deploy: no verificado en este lote, pero el checkout no refleja lo pedido
- Estado lote 0: `Pendiente en checkout`
- Evidencia:
  - `webapp/src/main/webapp/WEB-INF/views/profile/saved-restaurants.jsp`
- Siguiente lote: `Lote 6`

### `#5` En `/admin/pending` falta link de revision al restaurante

- URL: `/admin/pending`
- Rol: admin
- Paso minimo: abrir listado de pendientes
- Esperado: tener una forma directa de ir a `/restaurants/{id}` para revisar el restaurante
- Actual checkout: la pantalla ofrece approve, reject y assign-owner, pero no preview publica
- Actual deploy: no re-verificado; el gap ya es claro en checkout
- Estado lote 0: `Pendiente en checkout`
- Evidencia:
  - `webapp/src/main/webapp/WEB-INF/views/admin/pending-restaurants.jsp`
- Siguiente lote: `Lote 4`

### `#6` Agregar error para Request-URI Too Long

- URL: busqueda con query extremadamente larga
- Rol: cualquiera
- Paso minimo: pedir `/explore?query=<muy largo>`
- Esperado: pagina custom `414` de Forkd, no la default de Apache/Tomcat
- Actual checkout: existen `error/414.jsp`, mensajes i18n y `error-page` en `web.xml`
- Actual deploy: reproducido y devuelve HTML default de Apache con `Server: Apache/2.4.29 (Ubuntu)` y host `aa.it.itba.edu.ar`
- Estado lote 0: `Probable deploy/infra`
- Evidencia:
  - `webapp/src/main/webapp/WEB-INF/views/error/414.jsp`
  - `webapp/src/main/webapp/WEB-INF/web.xml`
- Nota: este bug no se arregla solo en Java/JSP si Apache corta antes del contenedor; hay que garantizar que deploy termine mostrando la 414 custom de Forkd
- Siguiente lote: `Lote 1`, pero como ticket de infraestructura

### `#7` Desactivar multiclick

- URL: acciones sensibles con submit
- Rol: multiple
- Paso minimo: doble click rapido en approve/reject/save/delete
- Esperado: un submit efectivo por accion
- Actual checkout: existe confirm dialog para algunos forms, pero no un guardrail compartido de `single-submit`
- Actual deploy: no verificado manualmente para no disparar acciones reales
- Estado lote 0: `Pendiente en checkout`
- Evidencia:
  - `webapp/src/main/webapp/js/forkd.js`
- Nota: el JS actual intercepta confirmaciones, pero no bloquea de forma general el multiclick
- Siguiente lote: `Lote 1`

### `#8` Mejorar las cards de pendientes

- URL: `/admin/pending`
- Rol: admin
- Paso minimo: abrir listado de pendientes
- Esperado: cards mas claras, con mejor jerarquia y accion de revision
- Actual checkout: la vista ya tiene cards trabajadas; si siguen “mal”, el gap es visual/producto, no ausencia total de implementacion
- Actual deploy: no cerrado visualmente en este lote
- Estado lote 0: `Requiere QA visual/runtime`
- Evidencia:
  - `webapp/src/main/webapp/WEB-INF/views/admin/pending-restaurants.jsp`
- Siguiente lote: `Lote 4`

### `#9` Wording corregido por usuario: consolidado con `#13` owner delete

- Aclaracion del usuario: el objetivo correcto no era admin delete sino que los owners puedan borrar su propio restaurante
- Actual checkout para el wording original: el delete admin ya existe, por eso la auditoria inicial lo habia marcado como resuelto
- Estado lote 0: `Consolidado con otro bug`
- Evidencia:
  - `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/AdminController.java`
  - `webapp/src/main/webapp/WEB-INF/views/admin/restaurants.jsp`
- Nota: desde esta aclaracion, el trabajo real de `#9` vive junto con `#13` en owner lifecycle
- Siguiente lote: `Lote 5`

### `#10` Agregar filtros en `/admin/restaurants/assign-owner`

- URL: `/admin/restaurants/assign-owner`
- Rol: admin
- Paso minimo: abrir listado de restaurantes sin owner
- Esperado: filtros comparables al resto de los listados admin
- Actual checkout: el controller solo acepta `page` y `pageSize`; no hay filtros en la vista
- Actual deploy: no re-verificado; el faltante ya es evidente en checkout
- Estado lote 0: `Pendiente en checkout`
- Evidencia:
  - `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/AdminController.java`
  - `webapp/src/main/webapp/WEB-INF/views/admin/unassigned-restaurants.jsp`
- Siguiente lote: `Lote 4`

### `#11` Luego de asignar owner debe redirigir a `/admin/restaurants/assign-owner`

- URL: `POST /admin/restaurants/{id}/assign-owner`
- Rol: admin
- Paso minimo: asignar owner a un restaurante
- Esperado: volver al listado de unassigned
- Actual checkout: el POST hoy redirige a `/admin/restaurants`
- Actual deploy: no ejecutado para no mutar datos
- Estado lote 0: `Pendiente en checkout`
- Evidencia:
  - `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/AdminController.java`
- Siguiente lote: `Lote 4`

### `#12` Agregar filtros en `/my-restaurants`

- URL: `/my-restaurants`
- Rol: owner
- Paso minimo: abrir dashboard owner
- Esperado: poder filtrar, no solo paginar
- Actual checkout: el controller solo maneja `page` y `pageSize`; la vista solo muestra selector de page size
- Actual deploy: no re-verificado; el faltante ya es evidente en checkout
- Estado lote 0: `Pendiente en checkout`
- Evidencia:
  - `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/OwnerDashboardController.java`
  - `webapp/src/main/webapp/WEB-INF/views/owner/dashboard.jsp`
- Siguiente lote: `Lote 5`

### `#13` Los owners deberian poder borrar sus restaurantes

- URL: `/my-restaurants`
- Rol: owner
- Paso minimo: abrir menu de acciones sobre un restaurante propio
- Esperado: accion de delete con autorizacion correcta; esta entrada ahora cubre tambien la aclaracion de `#9`
- Actual checkout: no existe ruta owner delete ni opcion visible en dashboard
- Actual deploy: no re-verificado; el faltante ya es evidente en checkout
- Estado lote 0: `Pendiente en checkout`
- Evidencia:
  - `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/OwnerDashboardController.java`
  - `webapp/src/main/webapp/WEB-INF/views/owner/dashboard.jsp`
- Siguiente lote: `Lote 5`

### `#14` Orden y filtrado de tags en creacion/edicion de menu

- URL: menu owner/admin
- Rol: owner/admin
- Paso minimo: abrir editor de menu y revisar tags sugeridos
- Esperado: orden estable y mecanismo de filtrado/busqueda
- Actual checkout: hay lista fija de `suggestedTags` y toggle mostrar mas, pero no buscador/filtro
- Actual deploy: no re-verificado; el faltante funcional ya es claro en checkout
- Estado lote 0: `Pendiente en checkout`
- Evidencia:
  - `webapp/src/main/webapp/WEB-INF/views/shared/menu-form-body.jspf`
  - `models/src/main/java/ar/edu/itba/paw/models/RestaurantCatalog.java`
- Nota: el orden actual depende del catalogo, pero no cubre el requerimiento completo
- Siguiente lote: `Lote 3`

### `#15` No me deja editar/crear el menu

- URL: `/my-restaurants/{id}/menu` y `/admin/restaurants/{id}/menu`
- Rol: owner/admin
- Paso minimo: abrir y enviar el wizard de menu
- Esperado: crear/editar menu sin bloquearse
- Actual checkout: existen rutas, form compartido y submit; la causa del bug no queda cerrada solo leyendo codigo
- Actual deploy: no reproducido en este lote para evitar cambios de datos
- Estado lote 0: `Requiere QA visual/runtime`
- Evidencia:
  - `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/OwnerDashboardController.java`
  - `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/AdminController.java`
  - `webapp/src/main/webapp/WEB-INF/views/shared/menu-form-body.jspf`
- Nota: sigue siendo el bug bloqueante del siguiente lote funcional aunque la causa raiz aun no este aislada
- Siguiente lote: `Lote 3`

### `#16` El boton de cancelar dice cancelar dos veces

- URL: editor de menu
- Rol: owner/admin
- Paso minimo: abrir `/admin/restaurants/{id}/menu` o equivalente owner
- Esperado: una sola CTA `Cancelar`
- Actual checkout: el body compartido de menu contiene una sola CTA `Cancelar`
- Actual deploy: auditado en `/admin/restaurants/47/menu`; el HTML actual contiene una sola ocurrencia relevante de `Cancelar`
- Estado lote 0: `Resuelto en checkout/deploy`
- Evidencia:
  - `webapp/src/main/webapp/WEB-INF/views/shared/menu-form-body.jspf`
- Siguiente lote: sacar del backlog activo salvo reproduccion concreta en otra superficie

### `#17` Chequear backside de la subida de fotos en perfil y menu

- URL: perfil edit y menu forms
- Rol: reviewer/owner/admin
- Paso minimo: subir valida/invalida/oversize y editar sin reemplazar imagen
- Esperado: validacion y persistencia correctas en todos los flows
- Actual checkout: hay mucha cobertura de validacion inline, `413/415`, forms y advice; igual falta cerrar el caso de edicion/no reemplazo con smoke dirigido
- Actual deploy: no auditado con uploads reales en este lote
- Estado lote 0: `Parcial en checkout`
- Evidencia:
  - `webapp/src/test/java/ar/edu/itba/paw/webapp/InlineValidationMvcTest.java`
  - `webapp/src/test/java/ar/edu/itba/paw/webapp/ErrorHandlingAdviceTest.java`
  - `webapp/src/test/java/ar/edu/itba/paw/webapp/FormValidationTest.java`
- Siguiente lote: `Lote 1`

### `#18` Algunos mails no respetan el locale del recibidor

- URL: varios flows de mail
- Rol: reviewer/owner/admin
- Paso minimo: disparar mails a usuarios con `preferredLocale` distinto
- Esperado: locale del receptor real, no del request
- Actual checkout: la mayor parte de los servicios ya resuelve `preferredLocale` del receptor y lo pasa a `SmtpMailServiceImpl`
- Actual deploy: no auditado mail por mail en este lote
- Estado lote 0: `Parcial en checkout`
- Evidencia:
  - `services/src/main/java/ar/edu/itba/paw/services/UserServiceImpl.java`
  - `services/src/main/java/ar/edu/itba/paw/services/RestaurantServiceImpl.java`
  - `services/src/main/java/ar/edu/itba/paw/services/ReservationServiceImpl.java`
  - `services/src/main/java/ar/edu/itba/paw/services/ReservationSchedulerImpl.java`
  - `services/src/main/java/ar/edu/itba/paw/services/ReviewServiceImpl.java`
- Nota: no encontre en este lote un smoking gun actual, pero el plan sigue siendo auditar sender por sender antes de cerrarlo
- Siguiente lote: `Lote 1`

### `#19` Agregar boton para ir al inicio de sesion luego de resetear contraseña

- URL: `/login?reset=true`
- Rol: anonimo
- Paso minimo: completar reset y caer en login
- Esperado: CTA explicita hacia login
- Actual checkout: la vista solo muestra el mensaje `auth.login.reset` y el formulario normal
- Actual deploy: verificado; sigue sin CTA dedicada, solo mensaje + formulario existente
- Estado lote 0: `Pendiente en checkout`
- Evidencia:
  - `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/AuthController.java`
  - `webapp/src/main/webapp/WEB-INF/views/auth/login.jsp`
- Siguiente lote: `Lote 2`

### `#20` Cambiar contraseña logueado mantiene la sesion

- URL: `/profile/{username}/change-password` -> reset flow
- Rol: reviewer autenticado
- Paso minimo: iniciar cambio de contraseña desde perfil
- Esperado: mantener sesion luego del cambio
- Actual checkout: el flow actual emite token y entra al reset anonimo; el POST final del reset redirige a `/login?reset=true` y no refresca autenticacion
- Actual deploy: no ejecutado para no cambiar credenciales reales
- Estado lote 0: `Pendiente en checkout`
- Evidencia:
  - `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/ProfileController.java`
  - `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/AuthController.java`
- Siguiente lote: `Lote 2`

## Bugs Que Salen Del Backlog Activo Despues Del Lote 0

- `#6`: no tratar como bug puramente de app; abrir frente de infraestructura/deploy para que la salida final sea la 414 custom de Forkd.
- `#9`: consolidar con `#13`; no reabrir admin delete.
- `#16`: no tocar salvo reproduccion concreta en otro flujo o estado del wizard.

## Riesgos Abiertos Que El Lote 0 No Cierra

- `#2`, `#8`, `#15` necesitan reproduccion visual/runtime para cerrar diagnostico fino.
- `#17` y `#18` no conviene darlos por cerrados sin smoke dirigido o tests adicionales.
- `#20` toca seguridad/sesion; no hay que improvisarlo en el siguiente lote.

## Recomendacion Operativa

### Que haria a continuacion

1. Sacar `#9` y `#16` del backlog activo del plan.
2. Separar `#6` como item de infraestructura Apache/proxy con outcome funcional claro: pagina 414 custom de Forkd.
3. Arrancar implementacion por `Lote 1`, pero ya sin mezclar `#6` con fixes de app.
4. Mantener `#15` como bug bloqueante del siguiente lote funcional.

### Orden sugerido despues de este lote 0

1. `Lote 1` app-only: `#7`, `#17`, `#18`
2. `Lote 2`: `#1`, `#19`, `#20`, y luego decidir si `#2` entra como quick fix visual
3. `Lote 3`: entrar con reproduccion concreta de `#15` antes de tocar polish de tags
4. `Lote 5`: tomar owner delete como un unico entregable para `#9/#13`
