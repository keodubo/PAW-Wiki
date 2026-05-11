---
titulo: PAW Unidad 1 - Introduccion, Scrum y MVP
tipo: fuente
fuentes: [raw/apuntes.txt, raw/pdfs/PAW - Apuntes.pdf]
creado: 2026-05-11
actualizado: 2026-05-11
---

# PAW Unidad 1 - Introduccion, Scrum y MVP

Esta unidad ubica la materia en una forma de trabajo orientada a producto: entender problema, priorizar valor y avanzar con entregas incrementales. No es una unidad de codigo, pero condiciona como leer los TPs: cada decision tecnica deberia sostener una necesidad visible de usuario.

## Idea central

- Una story es una unidad de trabajo con requester, owner y valor esperado.
- Las tareas no se ordenan por gusto tecnico sino por prioridad y aporte al producto.
- El backlog contiene stories priorizadas; el icelog conserva ideas sin priorizar.
- La metodologia busca cerrar loops cortos: estimar, implementar, deployar, demo, aceptar o corregir.

## Tipos de trabajo

| Tipo | Uso | Riesgo si se confunde |
| --- | --- | --- |
| Feature | Funcionalidad con valor directo para el usuario. | Hacer features sin problema real produce alcance inflado. |
| Chore | Tarea tecnica necesaria para sostener el sistema. | Si se vende como feature, se pierde foco de producto. |
| Bug | Comportamiento observado que contradice lo esperado. | Si no se reproduce, se arreglan sintomas sin causa. |

## Caso AirBnB

- El apunte usa la evolucion de AirBnB para mostrar que la interfaz cambia cuando cambia el problema del usuario.
- La barra de busqueda, exploracion de destinos y experiencias no son decoracion: reflejan distintas hipotesis de producto.
- El foco no es "landing linda", sino expresar con claridad que problema se resuelve, para quien y con que accion principal.

## MVP

- Primero se identifica el problema.
- Luego se brainstormean features y stories posibles.
- Finalmente se destila la solucion minima viable.
- MVP no significa entregar algo descuidado: significa resolver lo minimo que valida la hipotesis con pocos recursos.

## Para usar en una app PAW

- Antes de ampliar alcance, escribir que usuario gana valor y que flujo queda mejor.
- Separar deuda tecnica real de feature visible.
- Priorizar tareas que destraban demo, feedback o entrega.
- Mantener trazabilidad entre story, implementacion y criterio de aceptacion.

## Ver tambien

- [[resumen-apuntes]]
- [[scrum-metodologia]]
- [[ux-flows]]
- [[criterios-evaluacion]]
- [[paw-unidad-02-contexto-historico-web]]
