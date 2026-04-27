# Example: Consulta a la Wiki

Usa este flujo cuando queres responder una pregunta usando la wiki como fuente de verdad.

## Escenario

Pregunta:

```text
Segun la wiki, donde deberia vivir la logica de negocio de un flujo web?
```

## Prompt para el agente

```text
Lee docs/CLAUDE.md.
Usa docs/index.md para ubicar paginas relevantes.
Lee las paginas necesarias de docs/wiki/.
Si la pregunta depende de stack, etapa, migracion, API o frontend, lee tambien docs/wiki/resumen-clases-paw-2026.md y docs/wiki/tp1-vs-tpe2-final.md.

Respondeme con:
- regla general de la wiki;
- etapa aplicable si corresponde: TP1, TP2 o TP final;
- paginas consultadas;
- implicancia practica para una app PAW;
- riesgos si se viola la regla.

No inventes clases, rutas ni modulos que la wiki no mencione.
```

## Lectura esperada

```text
docs/index.md
docs/wiki/modelo-capas.md
docs/wiki/logica-en-controllers.md
docs/wiki/comparacion-capas-web-services-persistence.md
docs/wiki/resumen-clases-paw-2026.md
docs/wiki/tp1-vs-tpe2-final.md
```

## Formato de respuesta recomendado

```markdown
## Regla de la wiki

La logica de negocio no deberia concentrarse en controllers. El controller coordina request, binding, validacion web y vista; el caso de uso pertenece a servicios.

## Paginas consultadas

- [[modelo-capas]]
- [[logica-en-controllers]]
- [[comparacion-capas-web-services-persistence]]

## Riesgo

Si la regla vive en controller, se duplica comportamiento, se complica testear y se rompe la separacion por capas.
```

## Criterio de cierre

- La respuesta cita paginas concretas.
- Si la pregunta toca stack o herramientas, la respuesta separa `TP1`, `TP2` y `TP final`.
- La conclusion separa regla general de aplicacion practica.
- No se extrapola mas alla del material consultado.
