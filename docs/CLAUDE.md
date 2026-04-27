# PAW Wiki - Schema del Segundo Cerebro

Este archivo define las convenciones, estructura y flujos de trabajo para el wiki de conocimiento del proyecto PAW (TPE1). El LLM mantiene el wiki; el usuario cura fuentes y dirige el analisis.

Nota de distribucion liviana: algunas paginas pueden conservar referencias a `fuente historica no incluida (...)`. Eso significa que la fuente cruda se uso para generar o auditar el wiki, pero no viaja en el repositorio Git actual. En esos casos, usar la pagina sintetica como referencia y no asumir que el archivo original existe localmente.

## Arquitectura

```
README.md                 <- Guia humana principal.
obsidian-graph-view.png   <- Captura de referencia de la vista de grafo.
docs/
  examples/                <- Ejemplos de prompts, flujos y checklists de uso.
  raw/                     <- Fuentes inmutables. El LLM lee pero NUNCA modifica.
    pdfs/                  <- PDFs originales de la catedra cuando se distribuyen.
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
2. Discutir hallazgos clave con el usuario
3. Crear pagina tipo `fuente` en `wiki/` con resumen estructurado
4. Crear o actualizar paginas de conceptos/entidades relevantes
5. Actualizar `index.md` con las paginas nuevas/modificadas
6. Agregar entrada en `log.md`
7. Actualizar `tree.txt` si cambia el arbol bajo `docs/`

Si una fuente muestra una evolucion de setup (ej: ejemplo inicial y luego version final recomendada), documentar explicitamente el estado final y aclarar la transicion para no dejar contradicciones internas.
Si existe una dupla `PDF original + txt extraido` con contenido equivalente, no duplicar paginas fuente: usar el `.txt` para trabajar y registrar el PDF original como fuente validada en frontmatter, `index.md` y `log.md`.
Si la fuente es una transcripcion de audio (`.vtt`, `.srt`, etc.), tratarla como **fuente auxiliar de baja confianza**: solo integrar afirmaciones corroboradas por fuentes mas confiables o por pasajes muy claros; lo dudoso debe quedar aislado como nota de incertidumbre y no contaminar paginas conceptuales existentes.

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
4. El ejemplo de `docs/examples/` que coincida con la tarea
5. Las paginas concretas de `docs/wiki/`

Ejemplo completo: ver [examples/uso-con-agente.md](examples/uso-con-agente.md).

### 5. Onboarding y setup

Cuando el usuario pregunte como instalar, clonar, abrir o poner en funcionamiento la wiki:

1. Explicar que no hay servidor, build ni base de datos.
2. Indicar `git clone`, `cd PAW-Wiki` y apertura en Obsidian.
3. Si quiere usar agentes, linkear la instalacion opcional de skills.
4. Si quiere agregar material propio, crear `docs/private/<proyecto>/`.
5. Si va a publicar cambios, usar el checklist de publicacion.

Ejemplos completos:

- [examples/setup-local.md](examples/setup-local.md)
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

## Dominio: PAW TPE1

Este wiki esta enfocado en el desarrollo del Trabajo Practico Especial 1 de la materia PAW (Programacion de Aplicaciones Web). Temas centrales:

- **Stack**: Java 21, Spring Web MVC (sin Boot), JSP/JSTL, JDBC, PostgreSQL, Maven
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
- No commitear `docs/private/`
