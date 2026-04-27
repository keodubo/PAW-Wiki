# PAW Wiki - Schema del Segundo Cerebro

Este archivo define las convenciones, estructura y flujos de trabajo para el wiki de conocimiento del proyecto PAW por etapa (`TP1`, `TP2` y `TP final`). El LLM mantiene el wiki; el usuario cura fuentes y dirige el analisis.

Nota de distribucion liviana: algunas paginas pueden conservar referencias a `fuente historica no incluida (...)`. Eso significa que la fuente cruda se uso para generar o auditar el wiki, pero no viaja en el repositorio Git actual. En esos casos, usar la pagina sintetica como referencia y no asumir que el archivo original existe localmente.

## Arquitectura

```
README.md                 <- Guia humana principal.
obsidian-graph-view.png   <- Captura de referencia de la vista de grafo.
docs/
  examples/                <- Ejemplos de prompts, flujos y checklists de uso.
  raw/                     <- Fuentes inmutables. El LLM lee pero NUNCA modifica.
    PAW*.pdf               <- PDFs de clases de la catedra; fuentes prioritarias por etapa.
    pdfs/                  <- PDFs originales de la catedra cuando se distribuyen en subcarpeta.
    *.txt / *.md / *.puml  <- Apuntes, correcciones, enunciados, specs y diagramas.
  wiki/                    <- Paginas generadas y mantenidas por el LLM
  superpowers/plans/       <- Planes de ejecucion largos (salida de /superpowers:writing-plans)
  private/                 <- Carpeta local ignorada por Git para material personal.
  index.md                 <- Indice maestro del wiki (categorizado)
  log.md                   <- Registro cronologico de actividad
  tree.txt                 <- Snapshot plano del arbol de docs/
  CLAUDE.md                <- Este archivo (schema)
skills/
  paw-*                    <- Skills instalables para agentes que trabajan con PAW.
```

`docs/private/` no debe trackearse. Si aparece en `git status` como ignored, esta bien; si aparece como tracked, detenerse y corregir antes de publicar.

## Convenciones de Paginas

### Formato de pagina wiki

Cada archivo en `wiki/` sigue este formato:

```markdown
---
titulo: Nombre de la Pagina
tipo: concepto | entidad | fuente | sintesis | comparacion
fuentes: [lista de archivos en raw/ que informaron esta pagina]
creado: YYYY-MM-DD
actualizado: YYYY-MM-DD
---

# Titulo

Contenido de la pagina...

## Ver tambien
- [[Pagina Relacionada]]
```

### Tipos de pagina

- **concepto**: Idea tecnica o patron (ej: "Inyeccion de Dependencias", "XSS", "Spring Security")
- **entidad**: Componente, tecnologia o actor concreto (ej: "JDBC", "JSP/JSTL", "Maven")
- **fuente**: Resumen de un documento en raw/ (ej: resumen de correcciones, resumen del enunciado)
- **sintesis**: Analisis cruzado que conecta multiples fuentes o conceptos
- **comparacion**: Contraste entre enfoques, tecnologias o patrones

### Nombres de archivo

- Kebab-case en espanol: `spring-security.md`, `errores-frecuentes.md`
- Sin acentos en nombres de archivo: `inyeccion-dependencias.md` (no `inyección`)
- El titulo dentro del frontmatter SI lleva acentos

### Links internos

- Usar formato Obsidian: `\[\[nombre-archivo\]\]` (sin extension .md)
- Para secciones especificas: `\[\[nombre-archivo#seccion\]\]`

## Flujos de Trabajo

### 1. Ingerir fuente (`ingest`)

Cuando el usuario dice "ingesta esto" o agrega un archivo a `raw/`:

1. Leer la fuente completa
2. Resolver si la fuente afecta `TP1`, `TP2`, `TP final` o mas de una etapa
3. Si hay dudas reales de scope o contradicciones, preguntarlas antes de sintetizar; si no, avanzar
4. Crear pagina tipo `fuente` en `wiki/` con resumen estructurado y suficiente detalle para estudiar o implementar
5. Crear o actualizar paginas de conceptos/entidades relevantes
6. Actualizar `index.md` con las paginas nuevas/modificadas
7. Agregar entrada en `log.md`
8. Actualizar `tree.txt` si cambia el arbol bajo `docs/`

Si una fuente muestra una evolucion de setup (ej: ejemplo inicial y luego version final recomendada), documentar explicitamente el estado final y aclarar la transicion para no dejar contradicciones internas.
Si existe una dupla `PDF original + txt extraido` con contenido equivalente, no duplicar paginas fuente: usar el `.txt` para trabajar y registrar el PDF original como fuente validada en frontmatter, `index.md` y `log.md`.
Si la fuente es una transcripcion de audio (`.vtt`, `.srt`, etc.), tratarla como **fuente auxiliar de baja confianza**: solo integrar afirmaciones corroboradas por fuentes mas confiables o por pasajes muy claros; lo dudoso debe quedar aislado como nota de incertidumbre y no contaminar paginas conceptuales existentes.
Los PDFs `PAW*` de clases del profesor son fuentes prioritarias. Ingerirlos con mas detalle que un resumen corto y mantener trazabilidad hacia `[[resumen-clases-paw-2026]]`. Las versiones de dependencias que aparezcan en PDFs viejos son historicas: no copiarlas como recomendacion vigente sin contrastar el checkout, el enunciado actual o la configuracion real.

Ejemplo completo: ver [examples/ingesta-publica.md](examples/ingesta-publica.md).

### 2. Consultar (`query`)

Cuando el usuario hace una pregunta sobre el wiki:

1. Leer `index.md` para localizar paginas relevantes
2. Leer las paginas identificadas
3. Sintetizar respuesta con citas a paginas wiki
4. Si la respuesta genera conocimiento valioso, ofrecerla como nueva pagina wiki

Ejemplo completo: ver [examples/consulta-wiki.md](examples/consulta-wiki.md).

### 3. Auditar (`lint`)

Cuando el usuario pide una revision de salud del wiki:

1. Buscar contradicciones entre paginas
2. Identificar paginas huerfanas (sin links entrantes)
3. Detectar conceptos mencionados sin pagina propia
4. Proponer fuentes adicionales a buscar
5. Verificar que el indice este completo
6. Verificar que `docs/tree.txt` refleje los archivos bajo `docs/`
7. Verificar que `docs/private/` no quede trackeado

Comandos utiles:

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
4. `docs/examples/actualizar-wiki.md` si hay acceso a Git remoto o si la wiki esta clonada dentro de una app PAW
5. `docs/wiki/resumen-clases-paw-2026.md` y `docs/wiki/tp1-vs-tpe2-final.md` si la tarea toca stack, migracion, catedra o etapa
6. El ejemplo de `docs/examples/` que coincida con la tarea
7. Las paginas concretas de `docs/wiki/`

Ejemplo completo: ver [examples/uso-con-agente.md](examples/uso-con-agente.md).

Para trabajo sobre una app PAW con skills instaladas, indicar que el agente empiece por `$paw-feature-master` y pasarle la etapa (`TP1`, `TP2` o `TP final`). Esa skill es la orquestadora: lee la app, lee PAW-Wiki, decide que capas estan afectadas y recien ahi enruta a subskills como `$paw-webapp-layer`, `$paw-services-layer`, `$paw-persistence-layer`, `$paw-testing-layer`, `$paw-tp2-migration` o `$paw-tp-final-migration`.

No recomendar subskills directas como primer paso salvo que el usuario ya haya acotado la tarea a una unica capa.

Ruteo por etapa:

| Etapa | Regla para agentes |
| --- | --- |
| TP1 | Mantener Spring MVC + JSP/JSTL + JDBC. No introducir JPA, REST API o SPA salvo pedido explicito. |
| TP2 | Usar `$paw-tp2-migration` antes de tocar persistencia JPA/Hibernate o mappings. |
| TP final | Usar `$paw-tp-final-migration` para REST API, DTOs, auth stateless, SPA/frontend, build, cache o hosting. |

### 5. Onboarding y setup

Cuando el usuario pregunte como instalar, clonar, abrir o poner en funcionamiento la wiki:

1. Explicar que no hay servidor, build ni base de datos.
2. Recomendar clonarla o forkearla dentro del repo de la app PAW como carpeta `PAW-Wiki/`.
3. Indicar que el repo padre de la app debe ignorar `/PAW-Wiki/` en `.gitignore` o `.git/info/exclude` para no subir docs auxiliares al `main` entregable.
4. Indicar `git clone`, `cd PAW-Wiki` y apertura en Obsidian.
5. Explicar que cuando vuelva a usar la wiki, un agente puede revisar `origin/main` con `git fetch`, comparar `HEAD..origin/main` y hacer `git pull --ff-only` solo si el checkout esta limpio.
6. Si quiere usar agentes, linkear la instalacion opcional de skills.
7. Si quiere agregar material propio, crear `docs/private/<proyecto>/`.
8. Si va a publicar cambios, usar el checklist de publicacion.

Ejemplos completos:

- [examples/setup-local.md](examples/setup-local.md)
- [examples/wiki-dentro-repo-paw.md](examples/wiki-dentro-repo-paw.md)
- [examples/actualizar-wiki.md](examples/actualizar-wiki.md)
- [examples/instalar-skills.md](examples/instalar-skills.md)
- [examples/checklist-publicacion.md](examples/checklist-publicacion.md)
- [examples/troubleshooting.md](examples/troubleshooting.md)

### 6. Usar material privado

Si el usuario trae fuentes personales, specs de su webapp, capturas, auditorias locales o planes que no deben publicarse:

1. Guardarlas bajo `docs/private/<proyecto>/raw/`
2. Sintetizarlas bajo `docs/private/<proyecto>/wiki/`
3. Conectarlas con la wiki publica desde un nexo privado
4. No actualizar `docs/index.md` ni `docs/log.md` salvo que se cree una version publica y generica

Ejemplo completo: ver [examples/second-brain-privado.md](examples/second-brain-privado.md).

## Dominio: PAW por etapa

Este wiki nacio enfocado en el Trabajo Practico Especial 1 de PAW (Programacion de Aplicaciones Web), pero ahora conserva una ruta explicita por etapa:

- **TP1**: stack server-side clasico con Spring MVC, JSP/JSTL, JDBC, Maven, security, forms, i18n, logging, AOP y transacciones.
- **TP2**: migracion de persistencia a JPA/Hibernate, `EntityManager`, mappings, fetch/cascade, dirty checking y tests de persistencia.
- **TP final**: REST API + SPA/frontend, recursos/DTOs, auth stateless, routing, estado, i18n frontend, testing, build, cache y hosting.

Para resolver etapa, leer `docs/wiki/resumen-clases-paw-2026.md` y `docs/wiki/tp1-vs-tpe2-final.md`. Las versiones de dependencias incluidas en PDFs viejos son historicas: no usarlas como recomendacion vigente sin contrastar el checkout/enunciado actual.

Temas centrales:

- **Stack por etapa**: TP1 usa Spring Web MVC/JSP/JDBC; TP2 agrega JPA/Hibernate; TP final agrega REST API + SPA/frontend
- **Arquitectura**: Domain Driven Design, inyeccion de dependencias
- **Seguridad**: Spring Security, prevencion XSS, sanitizacion LIKE, validacion
- **Testing**: JUnit, Mockito, HSQLDB, @Rollback
- **Evaluacion**: Criterios de correccion de la catedra, errores frecuentes
- **Operativo**: i18n, logging, mailing, manejo de imagenes

## Reglas

- Todo el contenido del wiki se escribe en **espanol**
- Las fuentes en `raw/` son **inmutables** -- nunca modificarlas
- Cada ingesta publica debe tocar `index.md` y `log.md`
- Si se agregan, mueven o eliminan archivos bajo `docs/`, actualizar `tree.txt`
- Preferir paginas cortas y enfocadas sobre paginas largas y genericas
- Links bidireccionales: si A referencia B, B debe referenciar A en "Ver tambien"
- Citar fuentes especificas: `(ver: [[resumen-correcciones]], seccion Seguridad)`
- No inventar nombres de modulos, paquetes o clases si la fuente no los define explicitamente; usar la nomenclatura real del material
- Si dos pasajes parecen contradecirse, priorizar el setup final recomendado por la catedra y dejar nota de la evolucion
- Antes de usar `$paw-feature-master`, indicar etapa o dejar explicitamente que el agente la resuelva con evidencia
- No usar versiones de dependencias de PDFs viejos como canon vigente sin verificar contra el checkout
- No commitear `docs/private/`
