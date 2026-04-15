# Spec funcional consolidado — Sistema de reservas para restaurantes

## 1. Objetivo

Permitir que cada restaurante configure si acepta reservas o no, y en caso de aceptarlas, definir su operativa de reservas dentro de la plataforma mediante:

- modalidad de confirmación
- layout de mesas
- slots horarios de reserva

El sistema debe permitir que usuarios autenticados creen, vean y cancelen reservas, y que los restaurant admins gestionen las reservas de sus restaurantes.

También debe contemplar restaurantes que no aceptan reservas internas y, en esos casos, mostrar el canal alternativo correspondiente.

## 2. Alcance

Este spec cubre:

- configuración de reservas en crear/editar restaurante
- modo legacy para restaurantes ya existentes
- layout de mesas reservables
- generación y uso de slots horarios
- creación de reservas por usuarios autenticados
- asignación de mesa concreta
- estados de reserva
- bloqueo de disponibilidad
- gestión de reservas por usuario y por restaurante
- emails transaccionales
- relación entre reserva completada y review

Este spec no cubre:

- reprogramación de reservas
- combinación de mesas
- reservas para usuarios no autenticados
- horarios arbitrarios fuera de slots
- buffers extra entre reservas

## 3. Configuración de reservas en el restaurante

### 3.1 Campo principal

En el form de crear/editar restaurante existirá el campo:

`accepts_reservations`

Valores posibles:

- null
- false
- true

### 3.2 Significado de cada valor

**accepts_reservations = null**

Significa que el restaurante es legacy y todavía no configuró este comportamiento.

En UI debe tratarse como:

- estado no definido

No debe interpretarse automáticamente ni como que acepta ni como que no acepta reservas.

**accepts_reservations = false**

Significa que el restaurante no acepta reservas internas en la plataforma.

**accepts_reservations = true**

Significa que el restaurante sí acepta reservas internas en la plataforma.

## 4. Comportamiento del form según accepts_reservations

### 4.1 Caso null

La UI debe mostrar que el estado de reservas está no definido.

### 4.2 Caso false

Se debe abrir un subform específico para restaurantes que no aceptan reservas internas.

El restaurant admin debe elegir una de estas opciones públicas:

- "No aceptamos reservas"
- "Solo aceptamos reservas por WhatsApp"
- "Solo aceptamos reservas por llamada telefónica"
- "Solo aceptamos reservas a través de nuestra página {link}"

**Link externo**

Si elige la opción de página externa, el restaurant admin debe ingresar un link.

Ese link:

- debe validarse contra XSS
- debe considerarse un dato sensible al modificarse
- solo puede ser modificado por el restaurant admin

En todos los casos de accepts_reservations = false, el sistema no crea reservas internas.

### 4.3 Caso true

Se debe abrir un subform específico para reservas internas.

Ese subform incluye:

- modalidad de confirmación
- layout de mesas
- configuración de slots de reserva

## 5. Modalidad de confirmación

Cuando accepts_reservations = true, el restaurante debe poder elegir cómo se confirma una reserva.

Valores posibles:

- automatic
- manual

### 5.1 Confirmación automática

Si existe disponibilidad, la reserva se crea y queda directamente en estado confirmed.

### 5.2 Confirmación manual

Si existe disponibilidad, la reserva se crea en estado pending y luego debe ser confirmada manualmente por el restaurant admin.

## 6. Layout de mesas

### 6.1 Objetivo

Representar las mesas físicas reservables del restaurante.

### 6.2 Modelo de mesa

Cada mesa debe existir como entidad individual y tener al menos:

- id
- type

**id**

Identifica de forma única la mesa.

**type**

Indica para cuántas personas es la mesa.

Valores válidos:

- 1
- 2
- 3
- ...
- 11
- 12+

### 6.3 Carga desde UI

El restaurant admin carga:

1. el tipo de mesa
2. la cantidad de mesas de ese tipo

La cantidad por tipo debe poder ingresarse entre:

- mínimo 1
- máximo 30

Luego el sistema genera las mesas individuales con su id correspondiente.

### 6.4 Ejemplo

Si el admin carga:

- 5 mesas de 2
- 4 mesas de 4
- 2 mesas de 6
- 1 mesa de 8

El sistema genera todas esas mesas individualmente, cada una con su propio id.

## 7. Regla de capacidad y asignación de mesa

### 7.1 Regla general

Cada reserva debe quedar asociada a una sola mesa concreta.

No existe combinación de mesas en esta versión.

### 7.2 Regla de redondeo

La cantidad de personas siempre se resuelve redondeando hacia arriba al primer tipo de mesa compatible.

Ejemplos:

- 2 personas → mesa de 2
- 3 personas → mesa de 3, o de 4 si no hay de 3
- 5 personas → mesa de 6, o de 8 si no hay de 6
- 7 personas → mesa de 8

### 7.3 Criterio de asignación

Si hay varias mesas posibles, se debe elegir una mesa libre cuyo type sea igual o superior a la cantidad solicitada, tomando la más chica posible que cumpla.

Ejemplo: si el usuario reserva para 5 y hay mesas libres de 6 y de 8, debe asignarse una de 6.

## 8. Días, horarios y slots de reserva

### 8.1 Fuente de días y horarios

Los días y horarios operativos del restaurante ya existen en el form actual del restaurante.

El sistema de reservas debe apoyarse en esa configuración existente.

### 8.2 Slots obligatorios

Las reservas no pueden hacerse en horarios arbitrarios.

El usuario no puede elegir una hora libre como, por ejemplo, 20:37.

Las reservas deben hacerse exclusivamente sobre slots predefinidos.

### 8.3 Duración del slot

El restaurant admin debe elegir una duración de franja horaria de reservas entre:

- 1 hora
- 1 hora y media
- 2 horas

Ejemplos:

- 20:00 a 21:00
- 20:00 a 21:30
- 20:00 a 22:00

### 8.4 Fin del bloqueo horario

La mesa queda ocupada exactamente hasta el final del slot.

Ejemplo: si la reserva va de 20:00 a 21:30, la mesa vuelve a estar disponible exactamente a las 21:30.

No existe buffer adicional entre reservas.

## 9. Disponibilidad

### 9.1 Regla general

Un slot está disponible si existe al menos una mesa compatible con la cantidad de personas solicitada y esa mesa no está ocupada por una reserva bloqueante en ese mismo slot.

### 9.2 Estados que bloquean disponibilidad

Los estados que bloquean disponibilidad son:

- pending
- confirmed

### 9.3 Estados que no bloquean disponibilidad

Los estados que no bloquean disponibilidad son:

- cancelled
- completed

### 9.4 Flag no_show

El flag no_show no cambia por sí mismo la lógica de disponibilidad. Es solo información adicional sobre la reserva.

## 10. Creación de reservas por el usuario

### 10.1 Requisito de autenticación

Solo usuarios autenticados pueden crear reservas.

### 10.2 Datos del usuario

El backend debe completar automáticamente, usando los datos del usuario autenticado:

- nombre
- email

### 10.3 Datos que ingresa el cliente

El usuario debe ingresar:

- cantidad de personas
- fecha
- slot horario
- comentario opcional

### 10.4 Flujo de creación

1. el usuario entra a la página del restaurante
2. selecciona cantidad de personas
3. selecciona fecha
4. selecciona uno de los slots disponibles
5. agrega comentario opcional
6. confirma la reserva

### 10.5 Resultado según modalidad

**Restaurante con confirmación automática**

Si existe disponibilidad:

- se crea la reserva
- se asigna una mesa concreta
- queda en estado confirmed

**Restaurante con confirmación manual**

Si existe disponibilidad:

- se crea la reserva
- se asigna una mesa concreta
- queda en estado pending

En ambos casos la reserva bloquea ese slot de esa mesa.

## 11. Gestión de reservas por el usuario

### 11.1 Página de reservas del usuario

Cada usuario debe tener su propia página de gestión de reservas en:

`/perfil/{unique_user_slug}/reservas`

### 11.2 Objetivo

Permitir que el usuario vea y gestione sus reservas.

### 11.3 Contenido mínimo

La página debe mostrar las reservas del usuario, incluyendo al menos:

- restaurante
- fecha
- slot horario
- cantidad de personas
- estado

### 11.4 Acción permitida

Desde esta página el usuario puede:

- ver sus reservas
- cancelar sus reservas

### 11.5 Acciones no permitidas

Desde esta página el usuario no puede:

- reprogramar reservas
- editar reservas existentes

### 11.6 Múltiples reservas

Un usuario puede tener múltiples reservas al mismo tiempo:

- en distintos restaurantes
- en un mismo restaurante

### 11.7 Paginación

Esta página debe usar la paginación existente del sistema.

## 12. Gestión de reservas por el restaurante

### 12.1 Página de reservas del restaurante

Cada restaurante debe tener su propia página de gestión de reservas en:

`/mis-restaurantes/{restaurant_id}/reservas`

### 12.2 Objetivo

Permitir que el restaurant admin vea y gestione las reservas de ese restaurante específico.

### 12.3 Contenido mínimo

La página debe mostrar las reservas del restaurante, incluyendo al menos:

- usuario
- fecha
- slot horario
- cantidad de personas
- mesa asignada
- estado
- comentario opcional
- flag no_show si aplica

### 12.4 Acciones permitidas

Desde esta página el restaurant admin puede:

- ver reservas del restaurante
- confirmar reservas pending
- cancelar reservas
- marcar no_show

### 12.5 Paginación

Esta página debe usar la paginación existente del sistema.

## 13. Cancelación por parte del usuario

### 13.1 Regla

El usuario puede cancelar su propia reserva.

### 13.2 Reprogramación

El usuario no puede reprogramar una reserva existente.

Si desea cambiar fecha u horario, debe cancelar la actual y crear una nueva.

## 14. Restricción de cambios de layout

### 14.1 Regla principal

El restaurant admin no puede modificar el layout si existen reservas no finalizadas que bloquean mesas.

### 14.2 Estados bloqueantes para editar layout

Se consideran bloqueantes:

- pending
- confirmed

Para poder modificar el layout, el restaurant admin debe cancelar primero esas reservas.

## 15. Estados de reserva

### 15.1 Estados principales

Los estados principales de la reserva son:

- pending
- confirmed
- cancelled
- completed

### 15.2 Flag adicional

Existe un flag adicional:

- no_show

no_show no reemplaza al estado de la reserva. Es una marca complementaria.

Ese flag debe aparecer en el perfil del usuario.

## 16. Reglas de transición de estados

### 16.1 Alta de reserva

**Confirmación manual**

Nueva reserva → pending

**Confirmación automática**

Nueva reserva → confirmed

### 16.2 Confirmación manual

pending → confirmed

La realiza el restaurant admin.

### 16.3 Cancelación

Una reserva en:

- pending
- confirmed

puede pasar a:

- cancelled

La cancelación puede hacerla:

- el usuario
- el restaurant admin

### 16.4 Completado automático

Una reserva pasa a completed cuando se cumplen ambas condiciones:

1. terminó la franja horaria de la reserva
2. la reserva estaba en confirmed

Entonces:

confirmed → completed

### 16.5 No-show

El restaurant admin puede marcar no_show sobre la reserva.

Ese dato debe reflejarse en el perfil del usuario.

## 17. Emails transaccionales

### 17.1 Email de confirmación

Se envía cuando la reserva pasa a confirmed.

Debe incluir un CTA que lleve a la página de reservas del usuario.

Destino funcional: `/perfil/{unique_user_slug}/reservas`

### 17.2 Email de cancelación

Se envía cuando la reserva pasa a cancelled.

Debe incluir un CTA que lleve a la página de explorar.

### 17.3 Email de recordatorio

Se envía el mismo día de la reserva a las 11:00 AM.

**Reglas**

- se envía a todas las reservas de ese día
- aplica tanto para reservas de mediodía como de noche
- si una reserva se crea después de las 11:00 AM de ese mismo día, no recibe recordatorio

### 17.4 Email post-completion

Se envía cuando la reserva pasa a completed.

Debe incluir un CTA para dejar una review y dirigir al usuario a la página del restaurante donde completó la reserva.

## 18. Reviews

### 18.1 Requisito

Solo usuarios creados/autenticados pueden dejar reviews.

### 18.2 Origen válido

La review debe estar asociada a una reserva completada.

### 18.3 Regla de unicidad

Cada reserva completada habilita una sola review.

## 19. Restaurantes sin reservas internas

Cuando accepts_reservations = false, el sistema no debe crear reservas internas.

Solo debe mostrar una de estas salidas públicas:

- "No aceptamos reservas"
- "Solo aceptamos reservas por WhatsApp {numero_de_telefono}" El admin puedo ingresar su telefono
- "Solo aceptamos reservas por llamada telefónica {numero_de_telefono}" El admin puedo ingresar su telefono
- "Solo aceptamos reservas a través de nuestra página {link}" El admin puedo ingresar su pagina

En estos casos no existen entidades de reserva creadas dentro del sistema.

## 20. Validaciones funcionales

### 20.1 Validaciones del layout

- el tipo de mesa debe ser válido
- la cantidad de mesas por tipo debe estar entre 1 y 30
- cada mesa debe tener id único dentro del restaurante

### 20.2 Validaciones de reserva

- el usuario debe estar autenticado
- la fecha debe ser válida
- el slot debe existir
- el slot debe pertenecer al horario operativo del restaurante
- debe existir una mesa compatible disponible
- la cantidad de personas debe resolverse redondeando hacia arriba al primer tipo posible

### 20.3 Validaciones del link externo

- el link es obligatorio si la opción elegida es página externa
- debe sanitizarse contra XSS
- su modificación debe tratarse como dato sensible

## 21. Modelo conceptual mínimo

**Restaurante**

Campos mínimos:

- id
- accepts_reservations (null | false | true)
- reservation_disabled_reason
- reservation_external_link
- reservation_confirmation_mode (automatic | manual)
- reservation_slot_duration
- opening_hours o equivalente existente

**Mesa**

Campos mínimos:

- id
- restaurant_id
- type
- identificador visible o número de mesa

**Reserva**

Campos mínimos:

- id
- restaurant_id
- user_id
- table_id
- people_count
- date
- slot_start
- slot_end
- comment
- status (pending | confirmed | cancelled | completed)
- no_show (true | false)
- created_at
- updated_at

**Review**

Campos mínimos:

- id
- user_id
- restaurant_id
- reservation_id
- contenido y/o rating
- created_at

## 22. Reglas consolidadas de negocio

### 22.1 Regla sobre null

accepts_reservations = null representa un restaurante legacy sin configuración de reservas y debe mostrarse como "no definido" en UI.

### 22.2 Regla sobre pending

Una reserva pending:

- ya tiene mesa asignada
- ya bloquea ese slot
- impide modificaciones de layout junto con confirmed

### 22.3 Regla sobre horario

El usuario no elige horarios libres arbitrarios; solo slots predefinidos por el restaurante.

### 22.4 Regla sobre mesa

Toda reserva debe quedar asociada a una mesa concreta.

### 22.5 Regla sobre grupos

No se combinan mesas. La capacidad se resuelve redondeando hacia arriba.

### 22.6 Regla sobre reprogramación

No existe reprogramación. Solo cancelación y nueva creación.

### 22.7 Regla sobre no-show

no_show es un flag adicional, no un estado principal.

### 22.8 Regla sobre reviews

Solo puede dejar review quien completó una reserva, y una sola vez por reserva.

## 23. Casos borde ya resueltos por este spec

- un usuario puede tener varias reservas activas simultáneamente
- puede tener varias en un mismo restaurante
- una reserva pending ya ocupa mesa y slot
- una reserva creada después de las 11:00 AM del mismo día no recibe recordatorio
- una reserva para 7 nunca combina mesas: se asigna una mesa de 8
- una reserva solo se completa automáticamente si estaba en confirmed
- si el restaurante no usa reservas internas, no se crea ninguna reserva en el sistema
- el layout no puede modificarse si hay reservas pending o confirmed

## 24. Resumen ejecutivo

El sistema queda definido así:

- el restaurante puede tener reservas en estado no definido, deshabilitadas o habilitadas
- si no acepta reservas internas, se muestra un mensaje o canal alternativo
- si acepta reservas internas, configura:
  - confirmación automática o manual
  - layout de mesas físicas
  - duración de slots
- el usuario autenticado reserva por:
  - cantidad de personas
  - fecha
  - slot
  - comentario opcional
- el sistema asigna una mesa concreta, redondeando siempre hacia arriba
- pending y confirmed bloquean disponibilidad
- el usuario gestiona sus reservas en `/perfil/{unique_user_slug}/reservas`
- el restaurante gestiona las suyas en `/mis-restaurantes/{restaurant_id}/reservas`
- ambas páginas usan la paginación existente
- la reserva evoluciona entre pending, confirmed, cancelled y completed
- no_show es un flag adicional
- los emails acompañan confirmación, cancelación, recordatorio y completion
- una review solo puede surgir de una reserva completada
