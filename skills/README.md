# PAW Skills

Esta carpeta distribuye skills para trabajar con una app PAW usando la wiki como fuente de conocimiento.

Las skills nacieron del proyecto Forkd, pero se pueden adaptar a cualquier webapp de la materia si le indicas al agente donde esta tu checkout y que dominio estas implementando.

Canon de contexto para cualquier skill:

- `README.md`
- `docs/CLAUDE.md`
- `docs/index.md`
- `docs/wiki/resumen-clases-paw-2026.md`
- `docs/wiki/tp1-vs-tpe2-final.md`

Las versiones de dependencias mencionadas en PDFs antiguos son historicas. Las skills deben usar el checkout/enunciado actual como fuente para versiones concretas.

## Regla principal de uso

Usa siempre `$paw-feature-master` como entrada para trabajo real sobre una app PAW e indicá la etapa cuando sea relevante: `TP1`, `TP2` o `TP final`.

Si no conoces la etapa, pedile al agente que la resuelva con evidencia antes de proponer cambios de stack:

```text
Usa $paw-feature-master.
No estoy seguro de la etapa: primero resolvela leyendo PAW-Wiki/docs/index.md, resumen-clases-paw-2026 y el checkout actual. Si queda ambiguo, preguntame antes de migrar stack.
```

`$paw-feature-master` es la skill orquestadora. Su trabajo es:

1. Ubicar el checkout de la app PAW.
2. Leer `CLAUDE.md` de la app.
3. Leer `PAW-Wiki/docs/CLAUDE.md` y `PAW-Wiki/docs/index.md`.
4. Revisar el codigo y tests afectados antes de confiar en planes viejos.
5. Resolver la etapa del proyecto.
6. Decidir que subskills de capa o migracion hacen falta.
7. Activar solo las subskills necesarias.
8. Ordenar el trabajo por dependencias de capa.
9. Cerrar con verificaciones.

No empieces con `$paw-webapp-layer`, `$paw-services-layer` o `$paw-persistence-layer` salvo que la tarea sea estrictamente de una sola capa y ya tengas claro el scope. Para features, bugs, auditorias o planes mixtos, el prompt inicial debe nombrar `$paw-feature-master`.

Prompt base:

```text
Usa $paw-feature-master para trabajar esta tarea de mi app PAW.
Primero ubica el checkout, lee CLAUDE.md, PAW-Wiki/docs/CLAUDE.md y PAW-Wiki/docs/index.md.
Estoy en etapa TP1/TP2/TP final.
Despues decide que subskills de capa o migracion hacen falta y usalas solo cuando correspondan.
Mostrame un plan corto antes de editar si la tarea toca mas de una capa.
```

## Contenido

| Skill | Uso principal |
| --- | --- |
| `paw-feature-master` | Entrada principal. Orquesta features, bugs, auditorias y planes multi-capa con PAW-Wiki, Superpowers y subskills. |
| `paw-models-layer` | Cambios en `models`: entidades, enums, value objects, paginacion y filtros. |
| `paw-persistence-contracts-layer` | Contratos DAO en `persistence-contracts`. |
| `paw-persistence-layer` | SQL, schema, JDBC DAOs, migraciones y tests HSQLDB. |
| `paw-service-contracts-layer` | Interfaces de servicio, DTOs de caso de uso y excepciones de dominio. |
| `paw-services-layer` | Logica de negocio, transacciones, mail, schedulers y tests de servicios. |
| `paw-webapp-layer` | Controllers, forms, validators, JSP/JSTL, i18n, Spring Security, CSS/JS y tests MVC. |
| `paw-testing-layer` | Tests, fixtures, HSQLDB, Maven gates, MVC/security/template checks y revisiones de calidad. |
| `paw-tp2-migration` | Migracion TP2 de JDBC a JPA/Hibernate: entidades, EntityManager, mappings, fetch/cascade, SQL generado y tests. |
| `paw-tp-final-migration` | Migracion TP final a REST API + SPA: recursos, DTOs, auth stateless, frontend build, routing, estado, cache y packaging. |

## Instalacion

Las skills son carpetas completas. Copia siempre cada directorio `paw-*` con su `SKILL.md`, `references/` y `agents/`.

Si instalas desde una copia local de `PAW-Wiki`, primero actualiza la wiki con `docs/examples/actualizar-wiki.md`. Si el pull modifica `skills/`, copia de nuevo las carpetas `paw-*` y abre una conversacion nueva del agente.

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
Usa $paw-feature-master para planificar esta feature de mi app PAW segun la wiki y las capas.
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
Lee skills/paw-feature-master/SKILL.md y segui sus instrucciones. Cuando una tarea toque tests, lee tambien skills/paw-testing-layer/SKILL.md.
```

## Uso recomendado

Para una feature completa, bug mixto, auditoria o plan:

```text
Usa $paw-feature-master para planificar esta feature de mi app PAW segun la wiki y las capas. Primero revisa el checkout actual y arma un plan antes de editar.
```

La skill orquestadora decide si necesita subskills. Ejemplos de ruteo:

| Si la tarea toca... | `$paw-feature-master` puede enrutar a... |
| --- | --- |
| Entidades, enums, value objects, paginacion | `$paw-models-layer` |
| Interfaces DAO | `$paw-persistence-contracts-layer` |
| SQL, schemas, DAOs JDBC, tests HSQLDB | `$paw-persistence-layer` |
| Interfaces de servicios, DTOs, excepciones | `$paw-service-contracts-layer` |
| Logica de negocio, transacciones, mails, schedulers | `$paw-services-layer` |
| Controllers, forms, validators, JSP, i18n, seguridad web, CSS | `$paw-webapp-layer` |
| Tests, fixtures, fallas Maven, estrategia de verificacion | `$paw-testing-layer` |
| Migracion TP2 JDBC -> JPA/Hibernate | `$paw-tp2-migration` |
| Migracion TP final REST + SPA | `$paw-tp-final-migration` |

Para una capa puntual y acotada, podes nombrar la subskill directamente:

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

Si no estas seguro de que capa toca, usa `$paw-feature-master`.

## Etapas

| Etapa | Default recomendado |
| --- | --- |
| TP1 | Mantener Spring MVC + JSP/JSTL + JDBC. No introducir JPA/SPA por accidente. |
| TP2 | Usar `$paw-tp2-migration` para migrar persistencia a JPA/Hibernate sin cambiar producto innecesariamente. |
| TP final | Usar `$paw-tp-final-migration` para planificar REST API + SPA, build frontend, auth stateless y cache. |

Prompts especificos:

```text
Usa $paw-feature-master. Estamos en TP2.
Si el cambio toca persistencia, usa $paw-tp2-migration para planificar entidades, mappings, EntityManager, transacciones y tests antes de editar.
```

```text
Usa $paw-feature-master. Estamos en TP final.
Si el cambio toca API, SPA, auth stateless, build frontend, cache o packaging, usa $paw-tp-final-migration antes de las subskills de capa.
```

## Relacion con Superpowers

`$paw-feature-master` tambien decide cuando conviene aplicar flujos de Superpowers:

| Situacion | Flujo recomendado |
| --- | --- |
| Idea nueva o comportamiento nuevo | `superpowers:brainstorming` antes de implementar |
| Plan multi-paso | `superpowers:writing-plans` |
| Ejecucion de checklist existente | `superpowers:executing-plans` |
| Bug, test roto o comportamiento inesperado | `superpowers:systematic-debugging` |
| Feature o bugfix con codigo | `superpowers:test-driven-development` |
| Antes de declarar terminado | `superpowers:verification-before-completion` |

Si tu entorno no tiene el plugin Superpowers, igual podes usar `$paw-feature-master`; ejecuta manualmente el equivalente: aclarar scope, planificar, implementar en slices chicos, testear y verificar antes de cerrar.

## Configuracion de ruta del proyecto

Las skills intentan usar el directorio actual como checkout de la app PAW. Si trabajas desde otro lugar, exporta:

```bash
export PAW_APP_REPO="/ruta/a/tu-checkout-paw"
```

La skill maestra espera encontrar:

```text
$PAW_APP_REPO/CLAUDE.md
$PAW_APP_REPO/PAW-Wiki/docs/index.md
```

Si usas este wiki como repo separado, indica en el prompt donde esta la app y donde esta este wiki.

Ejemplo:

```text
App PAW: /ruta/a/paw-2026a-01
Wiki PAW: /ruta/a/PAW-Wiki
Reglas: lee README.md, docs/CLAUDE.md, docs/index.md y el ejemplo docs/examples/uso-con-agente.md antes de proponer cambios.
```

## Ejemplos de uso de la wiki

Antes de usar una skill, revisa los ejemplos publicos del wiki:

- `docs/examples/setup-local.md`: como clonar y abrir la wiki.
- `docs/examples/actualizar-wiki.md`: como revisar `origin/main` y hacer `pull --ff-only` si la wiki local esta limpia.
- `docs/examples/instalar-skills.md`: instalacion paso a paso de skills.
- `docs/examples/uso-con-agente.md`: prompt base para agentes.
- `docs/examples/consulta-wiki.md`: como responder usando paginas wiki.
- `docs/examples/ingesta-publica.md`: como convertir una fuente en conocimiento publico.
- `docs/examples/second-brain-privado.md`: como mantener material personal fuera de Git.
- `docs/examples/troubleshooting.md`: problemas comunes de Git, Obsidian y deteccion de skills.

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
