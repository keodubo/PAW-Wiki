# Example: Instalar Skills

Las skills son opcionales. Instalalas solo si queres que un agente tenga reglas especializadas para trabajar con PAW, capas, tests, persistencia y webapp.

## Instalar en Codex

Desde la raiz del repo:

```bash
mkdir -p "${CODEX_HOME:-$HOME/.codex}/skills"
cp -R skills/paw-* "${CODEX_HOME:-$HOME/.codex}/skills/"
```

Despues abri una conversacion nueva de Codex.

## Instalar en Agent Skills compartido

Algunas instalaciones leen skills desde `~/.agents/skills`:

```bash
mkdir -p "$HOME/.agents/skills"
cp -R skills/paw-* "$HOME/.agents/skills/"
```

## Instalar en Claude Code

```bash
mkdir -p "$HOME/.claude/skills"
cp -R skills/paw-* "$HOME/.claude/skills/"
```

Despues abri una conversacion nueva de Claude Code.

## Verificar instalacion local

```bash
find skills -name SKILL.md | sort
```

Deberias ver archivos como:

```text
skills/paw-feature-master/SKILL.md
skills/paw-testing-layer/SKILL.md
skills/paw-webapp-layer/SKILL.md
```

## Prompt de prueba

```text
Usa $paw-feature-master.
Lee README.md, docs/CLAUDE.md, docs/index.md y docs/examples/uso-con-agente.md.
Explicame que paginas de la wiki deberia leer antes de tocar un cambio de Spring Security.
No edites archivos todavia.
```

## Si la skill no aparece

1. Confirmar que copiaste la carpeta completa `paw-*`, no solo `SKILL.md`.
2. Cerrar y abrir una conversacion nueva.
3. Verificar la ruta de instalacion de tu herramienta.
4. Si el asistente no soporta skills, usar el prompt manual:

```text
Lee skills/paw-feature-master/SKILL.md y segui sus instrucciones.
```
