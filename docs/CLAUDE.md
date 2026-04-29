# PAW Wiki - Guía de Mantenimiento

Este archivo define las convenciones, estructura y flujos de trabajo para la
base de conocimiento PAW por etapa (`TP1`, `TP2` y `TP final`). Los asistentes
de IA mantienen la wiki; el usuario cura fuentes, decide alcance y valida los
cambios.

Nota de distribución liviana: algunas páginas pueden conservar referencias a
`fuente historica no incluida (...)`. Eso significa que la fuente cruda se usó
para generar o auditar la wiki, pero no viaja en el repositorio Git actual. En
esos casos, usar la página sintética como referencia y no asumir que el archivo
original existe localmente.

## Arquitectura

```text
README.md                 <- Guía humana principal.
obsidian-graph-view.png   <- Captura de referencia de la vista de grafo.
docs/
  examples/                <- Ejemplos de prompts, flujos y checklists de uso.
  raw/                     <- Fuentes inmutables. Se leen, pero no se modifican.
    PAW*.pdf               <- PDFs de clases de la cátedra; fuentes prioritarias por etapa.
    pdfs/                  <- PDFs originales de la cátedra cuando se distribuyen en subcarpeta.
    *.txt / *.md / *.puml  <- Apuntes, correcciones, enunciados, specs y diagramas.
  wiki/                    <- Páginas generadas y mantenidas por asistentes.
  superpowers/plans/       <- Planes de ejecución largos.
  private/                 <- Carpeta local ignorada por Git para material personal.
  index.md                 <- Índice maestro del wiki.
  log.md                   <- Registro cronológico de actividad.
  tree.txt                 <- Snapshot plano del árbol de docs/.
  CLAUDE.md                <- Este archivo.
skills/
  paw-*                    <- Skills instalables para agentes que trabajan con PAW.
```

`docs/private/` no debe trackearse. Si aparece en `git status` como ignored,
está bien; si aparece como tracked, detenerse y corregir antes de publicar.

## Convenciones de Páginas

### Formato de página wiki

Cada archivo en `wiki/` sigue este formato:

```markdown
---
titulo: Nombre de la Página
tipo: concepto | entidad | fuente | sintesis | comparacion
fuentes: [lista de archivos en raw/ que informaron esta página]
creado: YYYY-MM-DD
actualizado: YYYY-MM-DD
---

# Título

Contenido de la página...

## Ver también
- [[Página Relacionada]]
```

### Tipos de página

- **concepto**: idea técnica o patrón, por ejemplo "Inyección de Dependencias",
  "XSS" o "Spring Security".
- **entidad**: componente, tecnología o actor concreto, por ejemplo "JDBC",
  "JSP/JSTL" o "Maven".
- **fuente**: resumen de un documento en `raw/`.
- **sintesis**: análisis cruzado que conecta múltiples fuentes o conceptos.
- **comparacion**: contraste entre enfoques, tecnologías o patrones.

### Nombres de archivo

- Kebab-case en español: `spring-security.md`, `errores-frecuentes.md`.
- Sin acentos en nombres de archivo: `inyeccion-dependencias.md`, no
  `inyección`.
- El título dentro del frontmatter sí lleva acentos.

### Enlaces internos

- Usar formato Obsidian: `[[nombre-archivo]]` sin extensión `.md`.
- Para secciones específicas: `[[nombre-archivo#seccion]]`.

## Flujos de Trabajo

### 1. Ingerir fuente (`ingest`)

Cuando el usuario dice "ingesta esto" o agrega un archivo a `raw/`:

1. Leer la fuente completa.
2. Resolver si la fuente afecta `TP1`, `TP2`, `TP final` o más de una etapa.
3. Si hay dudas reales de scope o contradicciones, preguntarlas antes de
   sintetizar; si no, avanzar.
4. Crear una página tipo `fuente` en `wiki/` con resumen estructurado y detalle
   suficiente para estudiar o implementar.
5. Crear o actualizar páginas de conceptos/entidades relevantes.
6. Actualizar `index.md` con las páginas nuevas o modificadas.
7. Agregar una entrada en `log.md`.
8. Actualizar `tree.txt` si cambia el árbol bajo `docs/`.

Si una fuente muestra una evolución de setup, por ejemplo un ejemplo inicial y
luego una versión final recomendada, documentar explícitamente el estado final y
aclarar la transición para no dejar contradicciones internas.

Si existe una dupla `PDF original + txt extraído` con contenido equivalente, no
duplicar páginas fuente: usar el `.txt` para trabajar y registrar el PDF
original como fuente validada en frontmatter, `index.md` y `log.md`.

Si la fuente es una transcripción de audio (`.vtt`, `.srt`, etc.), tratarla como
**fuente auxiliar de baja confianza**: solo integrar afirmaciones corroboradas
por fuentes más confiables o por pasajes muy claros. Lo dudoso debe quedar
aislado como nota de incertidumbre y no contaminar páginas conceptuales
existentes.

Los PDFs `PAW*` de clases del profesor son fuentes prioritarias. Ingerirlos con
más detalle que un resumen corto y mantener trazabilidad hacia
`[[resumen-clases-paw-2026]]`. Las versiones de dependencias que aparezcan en
PDFs viejos son históricas: no copiarlas como recomendación vigente sin
contrastar el checkout, el enunciado actual o la configuración real.

Ejemplo completo: ver [examples/ingesta-publica.md](examples/ingesta-publica.md).

### 2. Consultar (`query`)

Cuando el usuario hace una pregunta sobre el wiki:

1. Leer `index.md` para localizar páginas relevantes.
2. Leer las páginas identificadas.
3. Sintetizar la respuesta con citas a páginas wiki.
4. Si la respuesta genera conocimiento valioso, ofrecerla como nueva página
   wiki.

Ejemplo completo: ver [examples/consulta-wiki.md](examples/consulta-wiki.md).

### 3. Auditar (`lint`)

Cuando el usuario pide una revisión de salud del wiki:

1. Buscar contradicciones entre páginas.
2. Identificar páginas huérfanas, sin enlaces entrantes.
3. Detectar conceptos mencionados sin página propia.
4. Proponer fuentes adicionales a buscar.
5. Verificar que el índice esté completo.
6. Verificar que `docs/tree.txt` refleje los archivos bajo `docs/`.
7. Verificar que `docs/private/` no quede trackeado.

Comandos útiles:

```bash
git status --short --ignored=matching
git ls-files docs/private
find docs -path docs/private -prune -o -type f ! -name '.DS_Store' -print | sort
```

### 4. Trabajar con agentes

Cuando un agente use este repositorio, debe leer en este orden:

1. `README.md`
2. `docs/CLAUDE.md`
3. `docs/index.md`
4. `docs/examples/actualizar-wiki.md` si hay acceso a Git remoto o si la wiki
   está clonada dentro de una app PAW.
5. `docs/wiki/resumen-clases-paw-2026.md` y
   `docs/wiki/tp1-vs-tpe2-final.md` si la tarea toca stack, migración, cátedra
   o etapa.
6. El ejemplo de `docs/examples/` que coincida con la tarea.
7. Las páginas concretas de `docs/wiki/`.

Ejemplo completo: ver [examples/uso-con-agente.md](examples/uso-con-agente.md).

Para trabajo sobre una app PAW con skills instaladas, indicar que el agente
empiece por `$paw-feature-master` y pasarle la etapa (`TP1`, `TP2` o
`TP final`). Esa skill es la orquestadora: lee la app, lee PAW-Wiki, decide qué
capas están afectadas y recién ahí enruta a subskills como `$paw-webapp-layer`,
`$paw-services-layer`, `$paw-persistence-layer`, `$paw-testing-layer`,
`$paw-tp2-migration` o `$paw-tp-final-migration`.

No recomendar subskills directas como primer paso salvo que el usuario ya haya
acotado la tarea a una única capa.

Ruteo por etapa:

| Etapa | Regla para agentes |
| --- | --- |
| TP1 | Mantener Spring MVC + JSP/JSTL + JDBC. No introducir JPA, REST API o SPA salvo pedido explícito. |
| TP2 | Usar `$paw-tp2-migration` antes de tocar persistencia JPA/Hibernate o mappings. |
| TP final | Usar `$paw-tp-final-migration` para REST API, DTOs, auth stateless, SPA/frontend, build, cache o hosting. |

### 5. Onboarding y setup

Cuando el usuario pregunte cómo instalar, clonar, abrir o poner en
funcionamiento la wiki:

1. Explicar que no hay servidor, build ni base de datos.
2. Recomendar clonarla o forkearla dentro del repositorio de la app PAW como
   carpeta `PAW-Wiki/`.
3. Indicar que el repositorio padre de la app debe ignorar `/PAW-Wiki/` en
   `.gitignore` o `.git/info/exclude` para no subir docs auxiliares al `main`
   entregable.
4. Indicar `git clone`, `cd PAW-Wiki` y apertura en Obsidian.
5. Explicar que, al volver a usar la wiki, un agente puede revisar
   `origin/main` con `git fetch`, comparar `HEAD..origin/main` y hacer
   `git pull --ff-only` solo si el checkout está limpio.
6. Si quiere usar agentes, linkear la instalación opcional de skills.
7. Si quiere agregar material propio, crear `docs/private/<proyecto>/`.
8. Si va a publicar cambios, usar el checklist de publicación.

Ejemplos completos:

- [examples/setup-local.md](examples/setup-local.md)
- [examples/wiki-dentro-repo-paw.md](examples/wiki-dentro-repo-paw.md)
- [examples/actualizar-wiki.md](examples/actualizar-wiki.md)
- [examples/instalar-skills.md](examples/instalar-skills.md)
- [examples/checklist-publicacion.md](examples/checklist-publicacion.md)
- [examples/troubleshooting.md](examples/troubleshooting.md)

### 6. Usar material privado

Si el usuario trae fuentes personales, specs de su webapp, capturas, auditorías
locales o planes que no deben publicarse:

1. Guardarlas bajo `docs/private/<proyecto>/raw/`.
2. Sintetizarlas bajo `docs/private/<proyecto>/wiki/`.
3. Conectarlas con la wiki pública desde un nexo privado.
4. No actualizar `docs/index.md` ni `docs/log.md` salvo que se cree una versión
   pública y genérica.

Ejemplo completo: ver [examples/second-brain-privado.md](examples/second-brain-privado.md).

## Dominio: PAW por Etapa

Este wiki nació enfocado en el Trabajo Práctico Especial 1 de PAW
(Programación de Aplicaciones Web), pero ahora conserva una ruta explícita por
etapa:

- **TP1**: stack server-side clásico con Spring MVC, JSP/JSTL, JDBC, Maven,
  security, forms, i18n, logging, AOP y transacciones.
- **TP2**: migración de persistencia a JPA/Hibernate, `EntityManager`,
  mappings, fetch/cascade, dirty checking y tests de persistencia.
- **TP final**: REST API + SPA/frontend, recursos/DTOs, auth stateless,
  routing, estado, i18n frontend, testing, build, cache y hosting.

Para resolver etapa, leer `docs/wiki/resumen-clases-paw-2026.md` y
`docs/wiki/tp1-vs-tpe2-final.md`. Las versiones de dependencias incluidas en
PDFs viejos son históricas: no usarlas como recomendación vigente sin contrastar
el checkout/enunciado actual.

Temas centrales:

- **Stack por etapa**: TP1 usa Spring Web MVC/JSP/JDBC; TP2 agrega
  JPA/Hibernate; TP final agrega REST API + SPA/frontend.
- **Arquitectura**: Domain Driven Design e inyección de dependencias.
- **Seguridad**: Spring Security, prevención XSS, sanitización LIKE y
  validación.
- **Testing**: JUnit, Mockito, HSQLDB y `@Rollback`.
- **Evaluación**: criterios de corrección de la cátedra y errores frecuentes.
- **Operativo**: i18n, logging, mailing y manejo de imágenes.

## Reglas

- Todo el contenido del wiki se escribe en **español**.
- Las fuentes en `raw/` son **inmutables**: nunca modificarlas.
- Cada ingesta pública debe tocar `index.md` y `log.md`.
- Si se agregan, mueven o eliminan archivos bajo `docs/`, actualizar `tree.txt`.
- Preferir páginas cortas y enfocadas sobre páginas largas y genéricas.
- Enlaces bidireccionales: si A referencia B, B debe referenciar A en
  "Ver también".
- Citar fuentes específicas: `(ver: [[resumen-correcciones]], sección Seguridad)`.
- No inventar nombres de módulos, paquetes o clases si la fuente no los define
  explícitamente; usar la nomenclatura real del material.
- Si dos pasajes parecen contradecirse, priorizar el setup final recomendado por
  la cátedra y dejar nota de la evolución.
- Antes de usar `$paw-feature-master`, indicar etapa o dejar explícitamente que
  el agente la resuelva con evidencia.
- No usar versiones de dependencias de PDFs viejos como canon vigente sin
  verificar contra el checkout.
- No agregar `docs/private/` a Git.
