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

## Prompt base para trabajar en una app PAW con skills

Usa este prompt cuando la tarea toca codigo, tests, auditoria o planificacion de una app PAW:

```text
Usa $paw-feature-master para trabajar esta tarea de mi app PAW.

Rutas:
- App PAW: /ruta/a/mi-app-paw
- Wiki PAW: /ruta/a/PAW-Wiki

Primero:
1. Resolve el checkout de la app.
2. Lee CLAUDE.md de la app.
3. Lee PAW-Wiki/docs/CLAUDE.md.
4. Lee PAW-Wiki/docs/index.md.
5. Revisa el codigo y tests afectados.

Despues:
- identifica si la tarea toca models, persistence contracts, persistence, service contracts, services, webapp o tests;
- invoca solo las subskills necesarias;
- si toca mas de una capa, trabaja en orden de dependencias;
- mostra un plan corto antes de editar;
- verifica antes de cerrar.
```

## Como `$paw-feature-master` enruta subskills

`$paw-feature-master` es la entrada principal. No implementa todo a ciegas: decide que subskill conviene usar segun la capa afectada.

| Capa o problema | Subskill que puede invocar |
| --- | --- |
| Domain models, enums, value objects | `$paw-models-layer` |
| DAO interfaces y contratos de persistencia | `$paw-persistence-contracts-layer` |
| SQL, schema, JDBC DAOs, HSQLDB tests | `$paw-persistence-layer` |
| Service interfaces, command DTOs, domain exceptions | `$paw-service-contracts-layer` |
| Business logic, transactions, mail, schedulers | `$paw-services-layer` |
| Controllers, forms, validators, JSP/JSTL, i18n, Spring Security, CSS | `$paw-webapp-layer` |
| Tests, fixtures, Maven gates, fallas de test | `$paw-testing-layer` |

Si la tarea es una feature completa, un bug que cruza capas o una auditoria, no empieces por una subskill: empezá por `$paw-feature-master`.

## Ejemplos de prompts

Feature mixta:

```text
Usa $paw-feature-master para implementar esta feature.
Primero lee el checkout, PAW-Wiki y tests afectados.
Decidi que subskills hacen falta y mostrame un plan por capas antes de editar.
```

Auditoria:

```text
Usa $paw-feature-master para auditar este flujo contra PAW-Wiki.
No edites todavia. Identifica paginas wiki relevantes, codigo afectado, riesgos y tests que deberian cubrirlo.
```

Bug:

```text
Usa $paw-feature-master para investigar este bug.
Si hay comportamiento inesperado, aplica systematic debugging.
Despues decidi que subskills de capa hacen falta para corregirlo.
```
