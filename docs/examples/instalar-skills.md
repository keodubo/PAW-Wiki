# Example: Instalar Skills

Las skills son opcionales. Instalalas solo si queres que un agente tenga reglas especializadas para trabajar con PAW, capas, tests, persistencia y webapp.

## Instalar en Codex

Desde la raiz del repo:

```bash
mkdir -p "${CODEX_HOME:-$HOME/.codex}/skills"
cp -R skills/paw-* "${CODEX_HOME:-$HOME/.codex}/skills/"
```

Despues abri una conversacion nueva de Codex.

## Como usarlas despues de instalar

La regla practica es simple: para trabajo real, empezá por `$paw-feature-master`.

`$paw-feature-master` es la skill orquestadora. Primero lee el checkout, la wiki y el contexto; despues decide si necesita llamar a subskills como `$paw-webapp-layer`, `$paw-services-layer`, `$paw-persistence-layer` o `$paw-testing-layer`.

No hace falta que vos adivines todas las capas de entrada. Pedile a `$paw-feature-master` que haga ese ruteo.

Prompt recomendado:

```text
Usa $paw-feature-master para trabajar esta tarea de mi app PAW.

Contexto:
- App PAW: /ruta/a/mi-app-paw
- Wiki PAW: /ruta/a/PAW-Wiki

Primero:
1. Lee CLAUDE.md de la app.
2. Lee PAW-Wiki/docs/CLAUDE.md.
3. Lee PAW-Wiki/docs/index.md.
4. Revisa el codigo y tests afectados.

Despues:
- decidí que subskills de capa hacen falta;
- usa solo las subskills necesarias;
- mostra un plan corto antes de editar si toca mas de una capa;
- verifica antes de decir que esta terminado.
```

## Cuando usar subskills directamente

Usa subskills directas solo si el alcance es obvio y de una sola capa.

| Caso | Skill directa aceptable |
| --- | --- |
| Solo entidades/enums/value objects | `$paw-models-layer` |
| Solo contrato DAO | `$paw-persistence-contracts-layer` |
| Solo SQL/JDBC/DAO/test HSQLDB | `$paw-persistence-layer` |
| Solo interfaz de servicio/DTO/excepcion | `$paw-service-contracts-layer` |
| Solo logica de negocio/mail/scheduler | `$paw-services-layer` |
| Solo controller/form/JSP/i18n/CSS | `$paw-webapp-layer` |
| Solo tests o estrategia de verificacion | `$paw-testing-layer` |

Si no estas seguro, no elijas una subskill: usa `$paw-feature-master`.

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
Explicame que subskills usarias antes de tocar un cambio de Spring Security y por que.
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
