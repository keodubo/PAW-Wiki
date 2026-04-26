# PAW Forkd Skills

Esta carpeta distribuye skills para trabajar con el proyecto PAW/Forkd usando la wiki como fuente de conocimiento.

## Contenido

| Skill | Uso principal |
| --- | --- |
| `paw-feature-master` | Orquestar features, auditorias y planes multi-capa con PAW-Wiki y Superpowers. |
| `paw-models-layer` | Cambios en `models`: entidades, enums, value objects, paginacion y filtros. |
| `paw-persistence-contracts-layer` | Contratos DAO en `persistence-contracts`. |
| `paw-persistence-layer` | SQL, schema, JDBC DAOs, migraciones y tests HSQLDB. |
| `paw-service-contracts-layer` | Interfaces de servicio, DTOs de caso de uso y excepciones de dominio. |
| `paw-services-layer` | Logica de negocio, transacciones, mail, schedulers y tests de servicios. |
| `paw-webapp-layer` | Controllers, forms, validators, JSP/JSTL, i18n, Spring Security, CSS/JS y tests MVC. |
| `paw-testing-layer` | Tests, fixtures, HSQLDB, Maven gates, MVC/security/template checks y revisiones de calidad. |

## Instalacion

Las skills son carpetas completas. Copia siempre cada directorio `paw-*` con su `SKILL.md`, `references/` y `agents/`.

### Codex

```bash
mkdir -p "${CODEX_HOME:-$HOME/.codex}/skills"
cp -R skills/paw-* "${CODEX_HOME:-$HOME/.codex}/skills/"
```

Abri una conversacion nueva de Codex para que las skills aparezcan en el contexto inicial.

Si tu instalacion de Codex usa el registro compartido de Agent Skills, tambien podes instalar en:

```bash
mkdir -p "$HOME/.agents/skills"
cp -R skills/paw-* "$HOME/.agents/skills/"
```

### Claude Code

Claude Code busca skills personales en `~/.claude/skills`:

```bash
mkdir -p "$HOME/.claude/skills"
cp -R skills/paw-* "$HOME/.claude/skills/"
```

Despues abri una conversacion nueva de Claude Code y pedi, por ejemplo:

```text
Usa $paw-feature-master para planificar esta feature de Forkd segun la wiki y las capas.
```

### Otros asistentes compatibles

Para cualquier asistente que soporte Agent Skills, instala copiando las carpetas `paw-*` completas al directorio de skills que use esa herramienta.

Estructura esperada:

```text
<skills-dir>/
  paw-feature-master/
    SKILL.md
    references/
    agents/
  paw-testing-layer/
    SKILL.md
    references/
    agents/
```

Si el asistente no reconoce skills automaticamente, podes usar el repo como prompt manual:

```text
Lee skills/paw-feature-master/SKILL.md y seguí sus instrucciones. Cuando una tarea toque tests, lee tambien skills/paw-testing-layer/SKILL.md.
```

## Uso recomendado

Para una feature completa:

```text
Usa $paw-feature-master para planificar esta feature de Forkd segun la wiki y las capas. Primero revisa el checkout actual y arma un plan antes de editar.
```

Para una capa puntual:

```text
Usa $paw-persistence-layer para agregar esta tabla, DAO y tests HSQLDB.
```

```text
Usa $paw-webapp-layer para implementar este cambio en controller, form, JSP e i18n.
```

Para disenar o revisar tests:

```text
Usa $paw-testing-layer para definir los tests correctos segun la wiki antes de implementar este cambio.
```

## Configuracion de ruta del proyecto

Las skills intentan usar el directorio actual como checkout de la app PAW/Forkd. Si trabajas desde otro lugar, exporta:

```bash
export PAW_APP_REPO="/ruta/a/tu-checkout-paw"
```

La skill maestra espera encontrar:

```text
$PAW_APP_REPO/CLAUDE.md
$PAW_APP_REPO/PAW-Wiki/docs/index.md
```

Si usas este wiki como repo separado, indica en el prompt donde esta la app y donde esta este wiki.

## Dependencia recomendada

La skill maestra referencia skills del plugin Superpowers (`superpowers:brainstorming`, `superpowers:writing-plans`, `superpowers:test-driven-development`, etc.). Si tu entorno no tiene Superpowers, igual podes usar las skills `paw-*`; simplemente ejecuta el flujo equivalente de planificacion, TDD, debugging y verificacion manualmente.

## Verificacion rapida

Cada skill debe tener esta forma minima:

```text
skills/paw-*/SKILL.md
skills/paw-*/references/*.md
skills/paw-*/agents/openai.yaml
```

Si tenes instalada la skill `skill-creator`, podes validar una skill con:

```bash
python3 ~/.codex/skills/.system/skill-creator/scripts/quick_validate.py skills/paw-feature-master
```
