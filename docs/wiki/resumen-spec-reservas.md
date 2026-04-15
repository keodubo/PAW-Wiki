---
titulo: Resumen del Spec Funcional de Reservas
tipo: fuente
fuentes: [raw/reservas/spec-funcional-reservas.md, raw/diagrama_bd.md]
creado: 2026-04-15
actualizado: 2026-04-15
---

# Resumen del Spec Funcional de Reservas

Ingesta del spec consolidado entregado el 2026-04-15. El documento define el alcance completo del sistema de reservas internas para restaurantes y fija las reglas de negocio que acompañan al modelo de datos ya plasmado en `raw/diagrama_bd.md`.

## Idea central

Cada restaurante elige uno de tres estados sobre `accepts_reservations`: null (legacy, no definido), false (no acepta reservas internas, muestra canal alternativo) o true (acepta reservas internas con layout, modalidad y slots configurados). El usuario autenticado reserva siempre contra un slot discreto y una mesa concreta; no hay combinación de mesas ni horarios arbitrarios.

## Ejes que fija el spec

- **Configuración por restaurante**: `accepts_reservations`, modalidad (`automatic` o `manual`), duración de slot (60/90/120 minutos) y layout de mesas (tipo + cantidad, 1 a 30 por tipo).
- **Asignación de mesa**: redondeo hacia arriba, se toma la mesa libre más chica compatible. No se combinan mesas.
- **Slots**: se derivan del horario operativo existente (`restaurant_hours`) más la duración configurada; la mesa se libera exactamente al fin del slot, sin buffer.
- **Disponibilidad**: bloquean `pending` y `confirmed`; liberan `cancelled` y `completed`. `no_show` es flag complementario, no altera disponibilidad.
- **Estados y transiciones**: `pending → confirmed` manual, alta directa a `confirmed` en automática, `pending|confirmed → cancelled`, y `confirmed → completed` automático al terminar el slot.
- **Gestión**: usuario en `/perfil/{slug}/reservas` (ver + cancelar), restaurant admin en `/mis-restaurantes/{id}/reservas` (ver, confirmar pending, cancelar, marcar no_show). Ambas páginas reutilizan la paginación existente.
- **Emails transaccionales**: confirmación, cancelación, recordatorio a las 11:00 AM del día de la reserva (solo si se creó antes de esa hora) y post-completion con CTA a review.
- **Reviews**: una review solo puede surgir de una reserva `completed` del propio usuario y hay unicidad por reserva.
- **Restaurantes sin reservas internas**: se muestra una salida pública entre cuatro opciones (sin reservas, WhatsApp, llamada o link externo). El link externo se sanitiza contra XSS y se trata como dato sensible.

## Casos borde explícitos

- Un usuario puede tener múltiples reservas simultáneas, incluso en el mismo restaurante.
- Una reserva `pending` ya ocupa mesa y slot y bloquea modificaciones de layout.
- Una reserva para 7 se asigna a mesa de 8 (nunca combina mesas).
- Una reserva solo se completa automáticamente si estaba `confirmed`; el spec no describe explícitamente qué pasa con una `pending` cuyo slot venció.
- Una reserva creada después de las 11:00 AM del mismo día no recibe recordatorio.
- `accepts_reservations = null` es legacy: la UI debe decir "no definido" sin asumir acepta ni rechaza.

## Fuera de alcance declarado

- Reprogramación de reservas (se cancela y se crea otra).
- Combinación de mesas para grupos grandes.
- Reservas para usuarios no autenticados.
- Horarios arbitrarios fuera de los slots predefinidos.
- Buffers extra entre reservas.

## Relación con el modelo de datos

El modelo de `raw/diagrama_bd.md` ya incluye las tablas y columnas necesarias: extensiones en `restaurants`, nuevas tablas `restaurant_tables`, `reservations`, `reservation_notifications`, y la FK única `reservation_id` en `reviews`. Hay algunos nombres ligeramente distintos entre spec y diagrama: `reservation_disabled_reason` (spec) vs `reservation_disabled_mode` (diagrama), `reservation_slot_duration` (spec) vs `reservation_slot_duration_minutes` (diagrama). Resolver esas dos diferencias al momento de escribir la migración.

## Ambigüedades a confirmar antes de implementar

- **Transición de pending vencido**: el spec solo cubre `confirmed → completed`. Hay que decidir qué hacer con `pending` cuyo slot terminó: ¿se cancela automáticamente? ¿se deja manual?
- **Teléfono para WhatsApp/llamada**: el §19 introduce número de teléfono para las salidas alternativas, pero el §21 no lista ese campo en el modelo conceptual. Sumar `reservation_contact_phone` o equivalente.
- **Definición de `reservation_slot_duration`**: spec dice "1h, 1h30, 2h"; el diagrama usa minutos (`SMALLINT`). Dejar explícito que el enum permite 60/90/120.
- **`no_show` en perfil del usuario**: spec dice que "debe aparecer en el perfil del usuario". Definir si se trata de un contador visible o solo una marca dentro de cada reserva listada.
- **Marca horaria del recordatorio**: "11:00 AM" no explicita zona horaria ni si se usa la timezone del restaurante o del servidor.

## Ver también

- [[plan-implementacion-reservas]]
- [[mailing]]
- [[persistencia-jdbc]]
- [[validacion-formularios]]
- [[spring-security]]
