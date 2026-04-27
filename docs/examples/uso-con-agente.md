# Example: Uso con Agente

Usa este prompt cuando quieras que Codex, Claude u otro asistente trabaje dentro de la wiki.

## Prompt base

```text
Trabaja en PAW-Wiki.

Antes de editar:
1. Lee README.md.
2. Lee docs/CLAUDE.md.
3. Lee docs/index.md.
4. Lee el ejemplo de docs/examples/ que corresponda a la tarea.

Reglas:
- docs/raw/ es inmutable;
- las paginas sintetizadas van en docs/wiki/;
- cada ingesta publica actualiza docs/index.md y docs/log.md;
- si cambia el arbol bajo docs/, actualiza docs/tree.txt;
- no commitees docs/private/;
- usa links Obsidian con nombres de archivo sin extension.

Al terminar:
- resume que archivos cambiaste;
- corre verificaciones basicas;
- indica riesgos o pendientes.
```

## Prompt para auditar la wiki

```text
Audita la salud de PAW-Wiki.

Revisa:
- links Obsidian rotos;
- paginas huerfanas;
- paginas fuera de docs/index.md;
- frontmatter faltante o invalido;
- fuentes raw no mencionadas;
- docs/private/ trackeado por error;
- docs/tree.txt desactualizado.

No cambies contenido todavia. Primero reporta hallazgos con archivos concretos.
```

## Prompt para editar una pagina existente

```text
Lee docs/CLAUDE.md, docs/index.md y la pagina objetivo.
Actualiza la pagina sin romper su frontmatter ni sus backlinks.
Si agregas links nuevos, revisa si hace falta backlink desde la pagina relacionada.
Agrega entrada en docs/log.md si el cambio es editorialmente relevante.
```

## Verificacion rapida

```bash
git diff --check
git status --short --ignored=matching
git ls-files docs/private
```
