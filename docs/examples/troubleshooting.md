# Example: Troubleshooting

Problemas comunes al usar PAW Wiki.

## Git dice que cree una branch nueva llamada main

Probable causa: tu `main` local no esta conectado a `origin/main`.

```bash
git fetch origin
git switch main
git branch --set-upstream-to=origin/main main
git status --short --branch
```

Si tu `main` local quedo desfasado y queres alinearlo con GitHub:

```bash
git branch backup-before-sync
git reset --hard origin/main
```

Usar `reset --hard` solo si ya guardaste o no te importan los cambios locales.

## Obsidian no muestra el grafo

1. Abrir la carpeta raiz `PAW-Wiki`, no solo `docs/`.
2. Confirmar que las paginas usan links `[[nombre-archivo]]`.
3. Abrir Graph View desde Obsidian.
4. Revisar que `docs/wiki/` tenga archivos `.md`.

## La imagen del README no aparece

Verificar que exista:

```bash
ls -lh obsidian-graph-view.png
```

El README debe apuntar a:

```markdown
![Vista del grafo del wiki en Obsidian](./obsidian-graph-view.png)
```

## Codex o Claude no detecta las skills

1. Copiar las carpetas completas:

```bash
mkdir -p "${CODEX_HOME:-$HOME/.codex}/skills"
cp -R skills/paw-* "${CODEX_HOME:-$HOME/.codex}/skills/"
```

2. Abrir una conversacion nueva.
3. Si sigue sin aparecer, usar prompt manual:

```text
Lee skills/paw-feature-master/SKILL.md y segui esas reglas.
```

## docs/private aparece en git status

Si aparece asi:

```text
!! docs/private/
```

Esta bien: significa que esta ignorado.

Si aparece asi:

```text
A  docs/private/...
M  docs/private/...
```

Esta mal: se esta trackeando contenido privado. Revisar con:

```bash
git ls-files docs/private
```

## Quiero actualizar docs/tree.txt

```bash
find docs -path docs/private -prune -o -type f ! -name '.DS_Store' -print | sort | sed 's#^#PAW-Wiki/#' > docs/tree.txt
```
