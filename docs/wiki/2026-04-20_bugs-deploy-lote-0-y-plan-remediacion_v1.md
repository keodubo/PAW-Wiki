---
titulo: Bugs de Deploy - Lote 0 y Plan de Remediacion
tipo: sintesis
fuentes: [raw/todo.md, raw/2026-04-20_deploy-bugs-lote-0-audit_v1.md, raw/2026-04-20_deploy-bugs-remediation-plan_v1.md]
creado: 2026-04-20
actualizado: 2026-04-20
---

# Bugs de Deploy - Lote 0 y Plan de Remediacion

Esta sintesis absorbe el cluster documental del `2026-04-20` sobre bugs detectados o reportados contra el deploy: la lista cruda original de `20` items (`raw/todo.md`), la auditoria corta que separa checkout vs deploy (`raw/2026-04-20_deploy-bugs-lote-0-audit_v1.md`) y el plan secuencial por lotes (`raw/2026-04-20_deploy-bugs-remediation-plan_v1.md`). Conviene leerla como backlog historico de postcierre, no como foto vigente del repo.

## Que aporta cada fuente

| Fuente | Rol dentro del cluster |
| --- | --- |
| `raw/todo.md` | Inventario original de `20` bugs expresados como backlog crudo por el usuario. |
| `raw/2026-04-20_deploy-bugs-lote-0-audit_v1.md` | Corte diagnostico: separa pendientes reales, parciales, cerrados, infra y casos que todavia requerian QA visual/runtime. |
| `raw/2026-04-20_deploy-bugs-remediation-plan_v1.md` | Orden de ejecucion recomendado por seam compartido para evitar retrabajo y mezclar fixes de app con temas de deploy. |

## Reencuadre que dejo fijo la auditoria

- La lista cruda no debia ejecutarse item por item sin filtro: despues del lote 0 quedo reclasificada en `12` bugs pendientes en checkout, `2` parciales, `1` resuelto en checkout/deploy, `1` probable infra/deploy, `1` consolidado con otro bug y `3` que aun requerian QA visual/runtime.
- `#6` (`414 Request-URI Too Long`) dejo de tratarse como bug puramente de app: el checkout ya tenia `414.jsp` y `web.xml`, pero el deploy auditado seguia respondiendo con la pagina default de Apache. El outcome correcto pasaba a ser coordinacion de infraestructura para mostrar una pagina Forkd custom.
- `#9` quedo reinterpretado por aclaracion del usuario: el problema real no era admin delete sino owner delete, por lo que se consolido con `#13`.
- `#16` salio del backlog activo: el lote 0 encontro una sola CTA `Cancelar` en el form compartido y tambien una sola ocurrencia relevante en el deploy auditado.

## Lotes operativos recomendados

| Lote | Bugs | Idea fuerza |
| --- | --- | --- |
| `Lote 1` | `#7`, `#17`, `#18` y coordinacion infra de `#6` | Atacar primero guardrails y comportamientos cross-cutting antes de UI puntual. |
| `Lote 2` | `#1`, `#19`, `#20`, luego `#2` | Resolver auth/cuenta como bloque coherente. |
| `Lote 3` | `#14`, `#15`, restos menu/media de `#17` | Entrar al editor de menu con diagnostico runtime primero. |
| `Lote 4` | `#5`, `#8`, `#10`, `#11` | Moderacion admin y assign-owner como mismo seam. |
| `Lote 5` | `#12`, `#13` (absorbiendo `#9`) | Dashboard owner y lifecycle del restaurante propio. |
| `Lote 6` | `#3`, `#4` | Ajustes visuales/publicos de menor riesgo tecnico. |

## Como leer este nodo hoy

- Sirve para reconstruir la transicion entre un cierre documental optimista y el backlog de bugs efectivamente observado contra el deploy.
- No reemplaza la inspeccion del checkout actual: varios items del cluster ya quedaron implementados despues, otros dependian de QA visual y al menos uno (`#6`) no podia cerrarse solo desde Java/JSP.
- El valor mas durable del cluster no es la lista de bugs en si, sino el criterio de separar backlog de app, problemas de infraestructura y hallazgos cuya unica confirmacion honesta era runtime.

## Relacion con otros nodos del wiki

- `[[2026-04-19_informe-implementaciones-desde-f81eb49_v1]]` documenta el cierre tecnico y QA manual inmediatamente anterior; este nodo explica por que al dia siguiente todavia hacia falta un backlog de postcierre centrado en deploy.
- `[[resumen-notas-sprint-2]]` conserva el checklist de cierre funcional previo; este cluster muestra el ajuste posterior cuando el foco paso de “estado de sprint” a “bugs concretos vistos o reportados en deploy”.

## Ver tambien

- [[2026-04-19_informe-implementaciones-desde-f81eb49_v1]]
- [[resumen-notas-sprint-2]]
- [[2026-04-24_correcciones-sprint-2_v1]]
