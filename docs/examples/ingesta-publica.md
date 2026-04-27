# Example: Ingesta Publica

Usa este flujo cuando la fuente sirve para cualquier estudiante de PAW y no contiene informacion privada de una webapp concreta.

## Escenario

Agregaste una fuente compartible:

```text
docs/raw/clase-validacion.md
```

Queres transformarla en conocimiento navegable dentro de la wiki.

## Prompt para el agente

```text
Lee docs/CLAUDE.md y docs/index.md.
Despues lee docs/raw/clase-validacion.md completa.

Objetivo:
- crear o actualizar paginas en docs/wiki/ con frontmatter valido;
- usar links Obsidian hacia paginas relacionadas;
- citar la fuente raw en frontmatter y en el texto cuando corresponda;
- actualizar docs/index.md;
- agregar una entrada en docs/log.md;
- actualizar docs/tree.txt si cambian archivos bajo docs/.

No modifiques docs/raw/clase-validacion.md.
```

## Salida esperada

```text
docs/wiki/validacion-formularios.md
docs/index.md
docs/log.md
docs/tree.txt
```

Si el concepto ya existe, actualizar la pagina existente en vez de duplicarla.

## Verificacion

```bash
git diff --check
rg -n "\\[\\[[^]]+\\]\\]" docs/wiki docs/index.md
git status --short
```

## Criterio de cierre

- La fuente original queda intacta en `docs/raw/`.
- La pagina wiki tiene frontmatter completo.
- `docs/index.md` permite encontrar la pagina nueva o modificada.
- `docs/log.md` registra que se hizo.
- No se agrega material privado.
