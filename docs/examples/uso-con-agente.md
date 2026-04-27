# Example: Uso con Agente

Usa este prompt cuando quieras que Codex, Claude u otro asistente trabaje dentro de la wiki.

## Prompt base

```text
Trabaja en PAW-Wiki.

Antes de editar:
1. Lee README.md.
2. Lee docs/CLAUDE.md.
3. Lee docs/index.md.
4. Si hay acceso a Git remoto, lee docs/examples/actualizar-wiki.md y verifica si hay cambios nuevos antes de usar la wiki.
5. Lee el ejemplo de docs/examples/ que corresponda a la tarea.

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
- Etapa: TP1 / TP2 / TP final

Primero:
1. Resolve el checkout de la app.
2. Verifica si PAW-Wiki tiene cambios remotos con PAW-Wiki/docs/examples/actualizar-wiki.md.
3. Lee CLAUDE.md de la app.
4. Lee PAW-Wiki/docs/CLAUDE.md.
5. Lee PAW-Wiki/docs/index.md.
6. Lee PAW-Wiki/docs/wiki/resumen-clases-paw-2026.md si la etapa o el stack importan.
7. Revisa el codigo y tests afectados.

Despues:
- identifica si la tarea toca TP1, TP2 o TP final;
- si es TP2, usa $paw-tp2-migration para migracion JDBC -> JPA/Hibernate;
- si es TP final, usa $paw-tp-final-migration para REST API + SPA;
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
| Migracion TP2 JDBC -> JPA/Hibernate | `$paw-tp2-migration` |
| Migracion TP final REST API + SPA | `$paw-tp-final-migration` |

Si la tarea es una feature completa, un bug que cruza capas o una auditoria, no empieces por una subskill: empezá por `$paw-feature-master`.

## Ejemplos de prompts

Feature mixta:

```text
Usa $paw-feature-master para implementar esta feature.
Etapa: TP1.
Primero revisa si PAW-Wiki debe actualizarse, lee el checkout, PAW-Wiki y tests afectados.
Decidi que subskills hacen falta y mostrame un plan por capas antes de editar.
```

Migracion TP2:

```text
Usa $paw-feature-master para planificar esta migracion.
Etapa: TP2.
Si toca persistencia, usa $paw-tp2-migration antes de subskills de capa.
No copies versiones viejas de dependencias desde los PDFs; revisa el checkout y el enunciado actual.
```

Migracion TP final:

```text
Usa $paw-feature-master para planificar esta migracion.
Etapa: TP final.
Si toca API, SPA, frontend build, auth stateless o cache, usa $paw-tp-final-migration.
Primero separa contrato REST, servicios backend, frontend y packaging.
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
