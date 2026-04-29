# PAW Skills

Esta carpeta distribuye skills para trabajar con una app PAW usando esta wiki
como fuente de conocimiento.

Las skills nacieron del proyecto Forkd, pero pueden adaptarse a cualquier webapp
de la materia si el agente recibe el checkout correcto, la etapa de trabajo y el
dominio que se está implementando.

Canon de contexto para cualquier skill:

- `README.md`
- `docs/CLAUDE.md`
- `docs/index.md`
- `docs/wiki/resumen-clases-paw-2026.md`
- `docs/wiki/tp1-vs-tpe2-final.md`

Las versiones de dependencias mencionadas en PDFs antiguos son históricas. Las
skills deben usar el checkout/enunciado actual como fuente para versiones
concretas.

## Regla Principal de Uso

Usa siempre `$paw-feature-master` como entrada para trabajo real sobre una app
PAW e indica la etapa cuando sea relevante: `TP1`, `TP2` o `TP final`.

Si no conoces la etapa, pide al agente que la resuelva con evidencia antes de
proponer cambios de stack:

```text
Usa $paw-feature-master.
No estoy seguro de la etapa: primero resuélvela leyendo PAW-Wiki/docs/index.md, resumen-clases-paw-2026 y el checkout actual. Si queda ambiguo, pregúntame antes de migrar stack.
```

`$paw-feature-master` es la skill orquestadora. Su trabajo es:

1. Ubicar el checkout de la app PAW.
2. Leer `CLAUDE.md` de la app.
3. Leer `PAW-Wiki/docs/CLAUDE.md` y `PAW-Wiki/docs/index.md`.
4. Revisar el código y tests afectados antes de confiar en planes viejos.
5. Resolver la etapa del proyecto.
6. Decidir qué subskills de capa o migración hacen falta.
7. Activar solo las subskills necesarias.
8. Ordenar el trabajo por dependencias de capa.
9. Cerrar con verificaciones.

No empieces con `$paw-webapp-layer`, `$paw-services-layer` o
`$paw-persistence-layer` salvo que la tarea sea estrictamente de una sola capa y
el scope ya esté claro. Para features, bugs, auditorías o planes mixtos, el
prompt inicial debe nombrar `$paw-feature-master`.

Prompt base:

```text
Usa $paw-feature-master para trabajar esta tarea de mi app PAW.
Primero ubica el checkout, lee CLAUDE.md, PAW-Wiki/docs/CLAUDE.md y PAW-Wiki/docs/index.md.
Estoy en etapa TP1/TP2/TP final.
Después decide qué subskills de capa o migración hacen falta y úsalas solo cuando correspondan.
Muéstrame un plan corto antes de editar si la tarea toca más de una capa.
```

## Contenido

| Skill | Uso principal |
| --- | --- |
| `paw-feature-master` | Entrada principal. Orquesta features, bugs, auditorías y planes multi-capa con PAW-Wiki, Superpowers y subskills. |
| `paw-models-layer` | Cambios en `models`: entidades, enums, value objects, paginación y filtros. |
| `paw-persistence-contracts-layer` | Contratos DAO en `persistence-contracts`. |
| `paw-persistence-layer` | SQL, schema, JDBC DAOs, migraciones y tests HSQLDB. |
| `paw-service-contracts-layer` | Interfaces de servicio, DTOs de caso de uso y excepciones de dominio. |
| `paw-services-layer` | Lógica de negocio, transacciones, mail, schedulers y tests de servicios. |
| `paw-webapp-layer` | Controllers, forms, validators, JSP/JSTL, i18n, Spring Security, CSS/JS y tests MVC. |
| `paw-testing-layer` | Tests, fixtures, HSQLDB, Maven gates, MVC/security/template checks y revisiones de calidad. |
| `paw-tp2-migration` | Migración TP2 de JDBC a JPA/Hibernate: entidades, EntityManager, mappings, fetch/cascade, SQL generado y tests. |
| `paw-tp-final-migration` | Migración TP final a REST API + SPA: recursos, DTOs, auth stateless, frontend build, routing, estado, cache y packaging. |

## Instalación

Las skills son carpetas completas. Copia siempre cada directorio `paw-*` con su
`SKILL.md`, `references/` y `agents/`.

Si instalas desde una copia local de `PAW-Wiki`, primero actualiza la wiki con
`docs/examples/actualizar-wiki.md`. Si el pull modifica `skills/`, copia de
nuevo las carpetas `paw-*` y abre una conversación nueva del agente.

### Codex

```bash
mkdir -p "${CODEX_HOME:-$HOME/.codex}/skills"
cp -R skills/paw-* "${CODEX_HOME:-$HOME/.codex}/skills/"
```

Abre una conversación nueva de Codex para que las skills aparezcan en el
contexto inicial.

Si tu instalación de Codex usa el registro compartido de Agent Skills, también
puedes instalar en:

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

Después abre una conversación nueva de Claude Code y pide, por ejemplo:

```text
Usa $paw-feature-master para planificar esta feature de mi app PAW según la wiki y las capas.
```

### Otros Asistentes Compatibles

Para cualquier asistente que soporte Agent Skills, instala copiando las carpetas
`paw-*` completas al directorio de skills que use esa herramienta.

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

Si el asistente no reconoce skills automáticamente, puedes usar este repositorio
como prompt manual:

```text
Lee skills/paw-feature-master/SKILL.md y sigue sus instrucciones. Cuando una tarea toque tests, lee también skills/paw-testing-layer/SKILL.md.
```

## Uso Recomendado

Para una feature completa, bug mixto, auditoría o plan:

```text
Usa $paw-feature-master para planificar esta feature de mi app PAW según la wiki y las capas. Primero revisa el checkout actual y arma un plan antes de editar.
```

La skill orquestadora decide si necesita subskills. Ejemplos de ruteo:

| Si la tarea toca... | `$paw-feature-master` puede enrutar a... |
| --- | --- |
| Entidades, enums, value objects, paginación | `$paw-models-layer` |
| Interfaces DAO | `$paw-persistence-contracts-layer` |
| SQL, schemas, DAOs JDBC, tests HSQLDB | `$paw-persistence-layer` |
| Interfaces de servicios, DTOs, excepciones | `$paw-service-contracts-layer` |
| Lógica de negocio, transacciones, mails, schedulers | `$paw-services-layer` |
| Controllers, forms, validators, JSP, i18n, seguridad web, CSS | `$paw-webapp-layer` |
| Tests, fixtures, fallas Maven, estrategia de verificación | `$paw-testing-layer` |
| Migración TP2 JDBC -> JPA/Hibernate | `$paw-tp2-migration` |
| Migración TP final REST + SPA | `$paw-tp-final-migration` |

Para una capa puntual y acotada, puedes nombrar la subskill directamente:

```text
Usa $paw-persistence-layer para agregar esta tabla, DAO y tests HSQLDB.
```

```text
Usa $paw-webapp-layer para implementar este cambio en controller, form, JSP e i18n.
```

Para diseñar o revisar tests:

```text
Usa $paw-testing-layer para definir los tests correctos según la wiki antes de implementar este cambio.
```

Si no estás seguro de qué capa toca, usa `$paw-feature-master`.

## Etapas

| Etapa | Default recomendado |
| --- | --- |
| TP1 | Mantener Spring MVC + JSP/JSTL + JDBC. No introducir JPA/SPA por accidente. |
| TP2 | Usar `$paw-tp2-migration` para migrar persistencia a JPA/Hibernate sin cambiar producto innecesariamente. |
| TP final | Usar `$paw-tp-final-migration` para planificar REST API + SPA, build frontend, auth stateless y cache. |

Prompts específicos:

```text
Usa $paw-feature-master. Estamos en TP2.
Si el cambio toca persistencia, usa $paw-tp2-migration para planificar entidades, mappings, EntityManager, transacciones y tests antes de editar.
```

```text
Usa $paw-feature-master. Estamos en TP final.
Si el cambio toca API, SPA, auth stateless, build frontend, cache o packaging, usa $paw-tp-final-migration antes de las subskills de capa.
```

## Relación con Superpowers

`$paw-feature-master` también decide cuándo conviene aplicar flujos de
Superpowers:

| Situación | Flujo recomendado |
| --- | --- |
| Idea nueva o comportamiento nuevo | `superpowers:brainstorming` antes de implementar |
| Plan multi-paso | `superpowers:writing-plans` |
| Ejecución de checklist existente | `superpowers:executing-plans` |
| Bug, test roto o comportamiento inesperado | `superpowers:systematic-debugging` |
| Feature o bugfix con código | `superpowers:test-driven-development` |
| Antes de declarar terminado | `superpowers:verification-before-completion` |

Si tu entorno no tiene el plugin Superpowers, igual puedes usar las skills
`paw-*`; ejecuta manualmente el equivalente: aclarar scope, planificar,
implementar en slices chicos, testear y verificar antes de cerrar.

## Configuración de Ruta del Proyecto

Las skills intentan usar el directorio actual como checkout de la app PAW. Si
trabajas desde otro lugar, exporta:

```bash
export PAW_APP_REPO="/ruta/a/tu-checkout-paw"
```

La skill maestra espera encontrar:

```text
$PAW_APP_REPO/CLAUDE.md
$PAW_APP_REPO/PAW-Wiki/docs/index.md
```

Si usas este wiki como repositorio separado, indica en el prompt dónde está la
app y dónde está este wiki.

Ejemplo:

```text
App PAW: /ruta/a/paw-2026a-01
Wiki PAW: /ruta/a/PAW-Wiki
Reglas: lee README.md, docs/CLAUDE.md, docs/index.md y el ejemplo docs/examples/uso-con-agente.md antes de proponer cambios.
```

## Ejemplos de Uso de la Wiki

Antes de usar una skill, revisa los ejemplos públicos del wiki:

- `docs/examples/setup-local.md`: cómo clonar y abrir la wiki.
- `docs/examples/actualizar-wiki.md`: cómo revisar `origin/main` y hacer
  `pull --ff-only` si la wiki local está limpia.
- `docs/examples/instalar-skills.md`: instalación paso a paso de skills.
- `docs/examples/uso-con-agente.md`: prompt base para agentes.
- `docs/examples/consulta-wiki.md`: cómo responder usando páginas wiki.
- `docs/examples/ingesta-publica.md`: cómo convertir una fuente en conocimiento
  público.
- `docs/examples/second-brain-privado.md`: cómo mantener material personal fuera
  de Git.
- `docs/examples/troubleshooting.md`: problemas comunes de Git, Obsidian y
  detección de skills.

## Dependencia Recomendada

La skill maestra referencia skills del plugin Superpowers
(`superpowers:brainstorming`, `superpowers:writing-plans`,
`superpowers:test-driven-development`, etc.). Si tu entorno no tiene
Superpowers, igual puedes usar las skills `paw-*`; simplemente ejecuta el flujo
equivalente de planificación, TDD, debugging y verificación manualmente.

## Verificación Rápida

Cada skill debe tener esta forma mínima:

```text
skills/paw-*/SKILL.md
skills/paw-*/references/*.md
skills/paw-*/agents/openai.yaml
```

Si tienes instalada la skill `skill-creator`, puedes validar una skill con:

```bash
python3 ~/.codex/skills/.system/skill-creator/scripts/quick_validate.py skills/paw-feature-master
```
