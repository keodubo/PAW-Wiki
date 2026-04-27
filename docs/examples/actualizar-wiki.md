# Example: Actualizar Wiki Local

Usa este flujo cuando `PAW-Wiki` esta clonado dentro de una app PAW y queres que un agente revise si hay cambios nuevos antes de usar la wiki.

La regla segura es:

- hacer `git fetch` siempre que haya red;
- revisar estado local y cambios remotos;
- hacer `git pull --ff-only` automaticamente solo si la wiki esta limpia;
- si hay cambios locales, no pisar nada: mostrar `status` y `diff` y pedir decision.

## Desde la raiz de la app PAW

```bash
git -C PAW-Wiki status --short --branch
git -C PAW-Wiki fetch origin
git -C PAW-Wiki status --porcelain
git -C PAW-Wiki log --oneline --decorate HEAD..origin/main
git -C PAW-Wiki diff --stat HEAD..origin/main
```

Si `git -C PAW-Wiki status --porcelain` no imprime nada y `log HEAD..origin/main` muestra commits nuevos:

```bash
git -C PAW-Wiki pull --ff-only
```

Si no hay commits nuevos, seguir usando la wiki local.

Si ya estas parado dentro de `PAW-Wiki`, usa los mismos comandos sin `-C PAW-Wiki`:

```bash
git status --short --branch
git fetch origin
git status --porcelain
git log --oneline --decorate HEAD..origin/main
git diff --stat HEAD..origin/main
```

## Si hay cambios locales

No ejecutar `pull` automaticamente. Primero mostrar:

```bash
git -C PAW-Wiki status --short
git -C PAW-Wiki diff -- README.md docs skills
```

Despues decidir con el usuario:

- guardar/commitear los cambios locales de la wiki;
- stashear temporalmente;
- descartar cambios solo si el usuario lo confirma explicitamente;
- o seguir sin actualizar.

## Despues de actualizar

Leer de nuevo:

```text
PAW-Wiki/README.md
PAW-Wiki/docs/CLAUDE.md
PAW-Wiki/docs/index.md
PAW-Wiki/docs/wiki/resumen-clases-paw-2026.md
PAW-Wiki/docs/wiki/tp1-vs-tpe2-final.md
```

Si usas skills instaladas desde esta wiki y el pull cambio `skills/`, reinstalarlas copiando carpetas completas:

```bash
mkdir -p "${CODEX_HOME:-$HOME/.codex}/skills"
cp -R PAW-Wiki/skills/paw-* "${CODEX_HOME:-$HOME/.codex}/skills/"
```

Para Claude Code:

```bash
mkdir -p "$HOME/.claude/skills"
cp -R PAW-Wiki/skills/paw-* "$HOME/.claude/skills/"
```

Abrir una conversacion nueva del agente para que vuelva a cargar las skills.

## Prompt para agente

```text
Antes de usar PAW-Wiki, verifica si hay cambios remotos.
Ejecuta git -C PAW-Wiki fetch origin.
Mostrame git -C PAW-Wiki status --short --branch y git -C PAW-Wiki log --oneline HEAD..origin/main.
Si PAW-Wiki esta limpia y hay commits nuevos, ejecuta git -C PAW-Wiki pull --ff-only.
Si hay cambios locales, no los pises: mostrame git -C PAW-Wiki diff y preguntame como seguir.
Despues lee PAW-Wiki/README.md, PAW-Wiki/docs/CLAUDE.md y PAW-Wiki/docs/index.md.
```

## Verificacion

```bash
git -C PAW-Wiki status --short --branch
git -C PAW-Wiki log --oneline -1
git ls-files PAW-Wiki
```

`git ls-files PAW-Wiki` se ejecuta desde la raiz de la app PAW y no deberia imprimir nada.
