---
titulo: Resumen de Notas — Sprint 2
tipo: fuente
fuentes: [raw/notas_para_sprint_2.txt]
creado: 2026-04-19
actualizado: 2026-04-19
---

# Resumen de Notas — Sprint 2

Checklist operativo del sprint 2 con foco en cierre funcional, UX y hardening del producto. A diferencia de `[[resumen-notas-sprint-1]]`, esta nota ya funciona como bitacora de cierre: mezcla pedidos originales, items marcados como resueltos y checks manuales pendientes.

## Ejes que la nota deja cerrados

| Tema | Cierre registrado en la nota | Como quedo |
|---|---|---|
| Auth base | Marcado como resuelto | Quedan cubiertos registro, claim, reset de password, confirmacion por mail y autologin de confirmacion. Ver `[[auth-flows]]`. |
| Menu reviewer + gestion owner/admin | Marcado como resuelto | El menu gana entidad propia y la edicion owner/admin separa restaurante, menu y reservas. Ver `[[ux-flows]]` y `[[2026-04-17_cierre-implementacion-reservas_v1]]`. |
| Admin nueva + owner dashboard | Marcado como resuelto | La nota consolida que ambas vistas ya quedaron suficientemente encaminadas para el sprint. |
| URLs en ingles | Marcado como resuelto | La convencion final de ruteo se estabiliza en ingles. |
| Cambio de password dentro de perfil | Marcado como resuelto | El flujo deja de vivir solo como parte de auth y pasa a existir tambien desde perfil. Ver `[[auth-flows]]`. |
| `pageSize` en reviews | Marcado como resuelto | El repo queda orientado a paginacion configurable, no fija en 5 elementos. |
| Rechazo de restaurante + reenvio a pending | Marcado como resuelto | La correccion esperada queda fijada: sale de pending, owner ve rechazado y puede re-enviar tras editar. |
| CTA de mail por review | Marcado como resuelto | El mail al restaurante debe apuntar a la gestion especifica de ese restaurante. Ver `[[mailing]]`. |
| Verified reviews | Marcado como resuelto | Las reviews vinculadas a reserva pasan a distinguirse visualmente. |
| Favoritos | Marcado como resuelto | Los usuarios pueden guardar restaurantes y acceder a esa lista desde perfil. |

## Decisiones posteriores que reinterpretan la nota

- **Roles separados**: el pedido original hablaba de registro unico y de fusionar comportamiento reviewer/owner, pero la decision de producto posterior fue **mantener roles separados**. El item se considera cerrado por decision de alcance, no por implementar unificacion.
- **Filtros de explore**: la nota marca como hecho el cambio de UX de filtros y el usuario decidio **no reabrirlo** en esta pasada, aunque el tema haya tenido debate posterior.
- **Formulario de registro**: el acortamiento visual del form se tomo como ajuste reciente del usuario y queda fuera de nuevas auditorias en esta ingesta.
- **Tags reales/legacy**: la nota deja cerrado el drift funcional. La decision posterior del usuario es tratarlos como tags reales editables; ese follow-up tecnico quedo cerrado el `2026-04-19`, dejando `seed.sql` cubierto contra todo el conjunto hoy expuesto por `RestaurantCatalog.getSuggestedTags()`.

## Lo que la nota deja como seguimiento operativo

- **Prueba manual integral del flujo de reservas**: sigue explicitamente abierto como check manual.
- **Prueba manual integral del flujo auth**: sigue explicitamente abierto como check manual.

## Cierre tecnico aplicado despues de la nota

- **Tests sin `verify` / `spy`**: el repo quedo alineado con `[[testing-unitario]]` en la pasada del `2026-04-19`, reemplazando las ultimas aserciones de interaccion por capturas observables y asserts de estado.
- **Cluster postcierre de deploy**: al dia siguiente se absorbio aparte `[[2026-04-20_bugs-deploy-lote-0-y-plan-remediacion_v1]]`, que no contradice este checklist sino que documenta el backlog de bugs concretos detectado luego del cierre de sprint y del informe tecnico.

## Lectura util para el equipo

- Esta fuente conviene leerla como **cierre de sprint** y no como backlog crudo.
- Los items marcados en `raw/notas_para_sprint_2.txt` mezclan:
  - correcciones efectivamente implementadas,
  - decisiones de producto ya aceptadas,
  - checks manuales que todavia no equivalen a una garantia automatizada,
  - y pequeños cierres tecnicos posteriores como la limpieza final del estilo de tests.
- Para reservar el contexto de implementacion ya estabilizado, conviene cruzarla con `[[2026-04-17_cierre-implementacion-reservas_v1]]`.

## Ver tambien

- [[resumen-notas-sprint-1]]
- [[resumen-correcciones]]
- [[auth-flows]]
- [[ux-flows]]
- [[mailing]]
- [[testing-unitario]]
- [[2026-04-17_cierre-implementacion-reservas_v1]]
- [[2026-04-19_informe-implementaciones-desde-f81eb49_v1]]
- [[2026-04-20_bugs-deploy-lote-0-y-plan-remediacion_v1]]
- [[2026-04-24_correcciones-sprint-2_v1]]
