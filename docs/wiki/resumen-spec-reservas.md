---
titulo: Resumen del Spec Funcional de Reservas
tipo: fuente
fuentes: [raw/specs_reserva_v2.txt, fuente historica no incluida (raw/reservas/spec-funcional-reservas.md), fuente historica no incluida (raw/diagrama_bd.md), raw/diagrama_bd.puml]
creado: 2026-04-15
actualizado: 2026-04-20 (resoluciones v2 previas + inventario de fuentes del diagrama de reservas)
---

# Resumen del Spec Funcional de Reservas

Versión vigente: **v2** (`raw/specs_reserva_v2.txt`, ingresada 2026-04-16). Supera a `fuente historica no incluida (raw/reservas/spec-funcional-reservas.md)` (v1, 2026-04-15). La v1 se conserva como antecedente histórico; cualquier contradicción entre ambas se resuelve a favor de v2. El diagrama en `fuente historica no incluida (raw/diagrama_bd.md)` sigue siendo la referencia del esquema de tablas y `raw/diagrama_bd.puml` conserva su fuente editable.

## Idea central

Cada restaurante elige uno de tres estados sobre `accepts_reservations`: `NOT_DEFINED` (legacy), `DISABLED` (no acepta reservas internas, muestra canal alternativo) o `ENABLED` (acepta reservas internas con layout, modalidad y slots configurados). El usuario autenticado reserva siempre contra un slot discreto y una mesa concreta; no hay combinación de mesas ni horarios arbitrarios. La feature `no_show` mencionada en v2 quedó fuera de alcance por decisión del usuario (ver resoluciones abajo).

## Ejes que fija el spec

- **Configuración por restaurante**: `accepts_reservations` (null/false/true), modalidad (`automatic` o `manual`), duración de slot (60/90/120 minutos) y layout de mesas (`type` + cantidad, 1 a 30 por tipo, con `type` entre 1 y 12 donde 12 representa "12+").
- **Asignación de mesa (v2)**: redondeo hacia arriba, se toma la mesa libre más chica compatible. No se combinan mesas. **Novedad v2**: el usuario puede reservar para cualquier cantidad de comensales; si la mesa asignada difiere "por mucho" del `people_count`, la reserva **no se aprueba automáticamente** aun en modalidad `automatic` y se notifica al restaurante.
- **Slots**: se derivan del horario operativo existente más la duración configurada; la mesa se libera exactamente al fin del slot, sin buffer.
- **Disponibilidad**: bloquean `pending` y `confirmed`; liberan `cancelled` y `completed`. `no_show` es flag complementario, no altera disponibilidad.
- **Estados y transiciones**: `pending → confirmed` manual, alta directa a `confirmed` en automática (salvo gap grande, ver arriba), `pending|confirmed → cancelled`, y `confirmed → completed` automático al terminar el slot.
- **Reassignación manual (v2, §5.2)**: cuando la reserva entra `pending` y la mesa asignada es más grande que lo solicitado, el restaurant admin puede **elegir qué mesa asignar** antes de confirmar. El spec deja ambiguo si esto aplica a todo `pending` o solo al gatillado por el gap, y qué mesas son elegibles.
- **Gestión**: usuario en `/perfil/{slug}/reservas` (ver + cancelar), restaurant admin en `/mis-restaurantes/{id}/reservas` (ver, confirmar pending, cancelar, marcar `no_show`). Ambas páginas reutilizan la paginación existente.
- **Emails transaccionales**: confirmación, cancelación, recordatorio a las 11:00 AM del día de la reserva (solo si se creó antes de esa hora) y post-completion con CTA a review.
- **Reviews**: una review solo puede surgir de una reserva `completed` del propio usuario y hay unicidad por reserva.
- **Restaurantes sin reservas internas**: se muestra una salida pública entre cuatro opciones (sin reservas, WhatsApp, llamada o link externo). El link externo se sanitiza contra XSS y se trata como dato sensible.

## Cambios v2 vs v1

| Tema | v1 | v2 |
|------|-----|-----|
| `no_show` | presente como campo (§21) y como acción del owner (§18.5) | presente explícitamente (§12.4/§21/§22.7) pero **descartado del plan** por decisión del usuario — no se implementa |
| Asignación y aprobación | redondeo hacia arriba estricto, `automatic` aprueba si hay mesa | redondeo hacia arriba + **gap grande → no aprueba automática, notifica al restaurante** (§7.2 anotación) |
| Reassignación de mesa | no mencionada | el owner puede elegir mesa al confirmar cuando la asignada es más grande que lo requerido (§5.2) |
| `accepts_reservations` | tratamiento lógico tristate (null/false/true) | §3.1: "debe ser un enum" con los mismos 3 valores |
| Modelo conceptual | campos básicos | `Reserva.people_count` (v1 usaba `party_size`), `Mesa.type` (v1 usaba capacidad) — rename superficial |

## Casos borde explícitos

- Un usuario puede tener múltiples reservas simultáneas, incluso en el mismo restaurante.
- Una reserva `pending` ya ocupa mesa y slot y bloquea modificaciones de layout.
- Una reserva para 7 se asigna a mesa de 8 (nunca combina mesas).
- Una reserva para 2 en mesa de 8 entra `pending` aun bajo modalidad automática (nueva regla v2).
- Una reserva solo se completa automáticamente si estaba `confirmed`; el spec no describe explícitamente qué pasa con una `pending` cuyo slot venció (decidido en plan: borrado físico).
- Una reserva creada después de las 11:00 AM del mismo día no recibe recordatorio.
- `accepts_reservations = null` es legacy: la UI debe decir "no definido" sin asumir acepta ni rechaza.

## Fuera de alcance declarado

- Reprogramación de reservas (se cancela y se crea otra).
- Combinación de mesas para grupos grandes.
- Reservas para usuarios no autenticados.
- Horarios arbitrarios fuera de los slots predefinidos.
- Buffers extra entre reservas.

## Relación con el modelo de datos

El modelo de `fuente historica no incluida (raw/diagrama_bd.md)` cubre las tablas necesarias: extensiones en `restaurants`, nuevas tablas `restaurant_tables`, `reservations`, `reservation_notifications`, y la FK única `reservation_id` en `reviews`. `raw/diagrama_bd.puml` conserva la misma estructura en formato editable para futuras iteraciones del esquema. Nombres ligeramente distintos entre spec y diagrama: `reservation_disabled_reason` (spec) vs `reservation_disabled_mode` (diagrama, y nombre elegido por el plan); `reservation_slot_duration` (spec) vs `reservation_slot_duration_minutes` (diagrama). El plan actual usa los nombres del diagrama.

## Ambigüedades v2 — resoluciones aplicadas

Cerradas el 2026-04-16. Las resoluciones viven en la tabla "Decisiones lockeadas" de [[plan-implementacion-reservas]].

- **Umbral del "gap grande" (§7.2)**: ratio `assigned_table.type / people_count >= 2` degrada a `pending` aunque la modalidad sea `automatic` (ej: 2 personas en mesa de 4 → pending).
- **Notificación al restaurante**: email único `reservation-pending-owner.html` con CTA a la reserva; se envía a **todo** `pending` recién creado (por `manual` o por gap), no solo al gap.
- **Alcance de la reassignación (§5.2)**: aplica a **cualquier** `pending`; el owner puede confirmar con la mesa asignada o elegir otra (opcional). En `manual` el owner maneja el flujo completo.
- **Dirección de la reassignación (§5.2)**: el owner puede mover la reserva a **cualquier** mesa con `capacity >= people_count` y libre en el slot, en cualquier dirección (a una mesa más chica o más grande que la asignada por el algoritmo). **Nunca** a una mesa con `capacity < people_count` (ej: 3 comensales jamás caen en mesa de 1 o 2). La prosa "de menos comensales a una para más" se interpreta como redacción ambigua, no como restricción de dirección. Confirmado con el usuario 2026-04-16.
- **`accepts_reservations` enum vs boolean**: se elige **enum** (`NOT_DEFINED`, `DISABLED`, `ENABLED`) para habilitar futuros estados sin migración disruptiva.
- **`no_show` (§12.4/§21/§22.7)**: **fuera de alcance**. El usuario decidió no implementar la feature; no hay columna, ni acción, ni badge, ni efecto sobre transiciones/reviews.
- **Marca horaria del recordatorio**: "11:00 AM" en `America/Argentina/Buenos_Aires` (heredado de v1, sigue vigente).
- **Destinatarios del recordatorio 11:00 AM**: **solo reservas `confirmed`**. Las `pending` no reciben recordatorio porque pueden terminar canceladas y el mail induciría al usuario a presentarse sin reserva real. Confirmado con el usuario 2026-04-16.
- **"Dato sensible" del link externo (§4.2)**: se interpreta estrictamente como **escapado XSS** (`<c:out>` siempre; validador rechaza `javascript:` / `data:`, solo admite `http(s)://`). No hay requisito adicional de logging especial, cifrado en reposo ni ocultamiento en endpoints — el link es dato público visible en la página del restaurante. Confirmado con el usuario 2026-04-16.
- **Reserva para hoy con `slot_start` ya vencido**: **se rechaza en la creación** con error in-line. El servicio valida `reservation_date == today && slot_start > now()` en `America/Argentina/Buenos_Aires` y lanza `SlotAlreadyPastException` → HTTP 400 con mensaje i18n `reservation.error.slotAlreadyPast`. No se confía solo en `@Future` del form porque ese constraint aplica sobre `reservation_date` y no distingue slots del día mismo. Confirmado con el usuario 2026-04-16.

## Ver también

- [[plan-implementacion-reservas]]
- [[mailing]]
- [[persistencia-jdbc]]
- [[validacion-formularios]]
- [[spring-security]]
