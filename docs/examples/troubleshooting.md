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

## PAW-Wiki aparece como untracked en el repo de la app

Si clonaste PAW-Wiki dentro de tu repo PAW y ves algo asi desde la raiz de la app:

```text
?? PAW-Wiki/
```

Agrega la regla al `.gitignore` del repo de la app:

```bash
printf "\n# Local PAW wiki, not part of the delivered app\n/PAW-Wiki/\n" >> .gitignore
```

Verifica:

```bash
git status --short --ignored=matching
git ls-files PAW-Wiki
```

`PAW-Wiki/` deberia aparecer como `!! PAW-Wiki/`, y `git ls-files PAW-Wiki` no deberia imprimir nada.

Si no queres tocar el `.gitignore` del repo de la app, usa una regla local:

```bash
printf "\n/PAW-Wiki/\n" >> .git/info/exclude
```

## Quiero actualizar docs/tree.txt

```bash
find docs -path docs/private -prune -o -type f ! -name '.DS_Store' -print | sort | sed 's#^#PAW-Wiki/#' > docs/tree.txt
```
