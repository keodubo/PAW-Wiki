# PAW Wiki - Schema del Segundo Cerebro

Este archivo define las convenciones, estructura y flujos de trabajo para el wiki de conocimiento del proyecto PAW (TPE1). El LLM mantiene el wiki; el usuario cura fuentes y dirige el analisis.

## Arquitectura

```
docs/
  raw/                     <- Fuentes inmutables. El LLM lee pero NUNCA modifica.
    pdfs/                  <- PDFs originales de la catedra (enunciado, apuntes, notas)
    audio_transcript/      <- Transcripciones VTT de clases (fuente auxiliar de baja confianza)
    reservas/              <- Spec funcional historica de reservas
    2026-*_paw-*.md        <- Planes y auditorias versionados en raiz de raw/
    *.txt / *.md           <- Apuntes, correcciones, enunciado, notas, specs de reservas
    diagrama_bd.{md,puml}  <- Modelo de datos de referencia
  wiki/                    <- Paginas generadas y mantenidas por el LLM
  superpowers/plans/       <- Planes de ejecucion largos (salida de /superpowers:writing-plans)
  index.md                 <- Indice maestro del wiki (categorizado)
  log.md                   <- Registro cronologico de actividad
  tree.txt                 <- Snapshot plano del arbol de docs/
  CLAUDE.md                <- Este archivo (schema)
```

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

- Usar formato Obsidian: `[[nombre-archivo]]` (sin extension .md)
- Para secciones especificas: `[[nombre-archivo#seccion]]`

## Flujos de Trabajo

### 1. Ingerir fuente (`ingest`)

Cuando el usuario dice "ingesta esto" o agrega un archivo a `raw/`:

1. Leer la fuente completa
2. Discutir hallazgos clave con el usuario
3. Crear pagina tipo `fuente` en `wiki/` con resumen estructurado
4. Crear o actualizar paginas de conceptos/entidades relevantes
5. Actualizar `index.md` con las paginas nuevas/modificadas
6. Agregar entrada en `log.md`

Si una fuente muestra una evolucion de setup (ej: ejemplo inicial y luego version final recomendada), documentar explicitamente el estado final y aclarar la transicion para no dejar contradicciones internas.
Si existe una dupla `PDF original + txt extraido` con contenido equivalente, no duplicar paginas fuente: usar el `.txt` para trabajar y registrar el PDF original como fuente validada en frontmatter, `index.md` y `log.md`.
Si la fuente es una transcripcion de audio (`.vtt`, `.srt`, etc.), tratarla como **fuente auxiliar de baja confianza**: solo integrar afirmaciones corroboradas por fuentes mas confiables o por pasajes muy claros; lo dudoso debe quedar aislado como nota de incertidumbre y no contaminar paginas conceptuales existentes.

### 2. Consultar (`query`)

Cuando el usuario hace una pregunta sobre el wiki:

1. Leer `index.md` para localizar paginas relevantes
2. Leer las paginas identificadas
3. Sintetizar respuesta con citas a paginas wiki
4. Si la respuesta genera conocimiento valioso, ofrecerla como nueva pagina wiki

### 3. Auditar (`lint`)

Cuando el usuario pide una revision de salud del wiki:

1. Buscar contradicciones entre paginas
2. Identificar paginas huerfanas (sin links entrantes)
3. Detectar conceptos mencionados sin pagina propia
4. Proponer fuentes adicionales a buscar
5. Verificar que el indice este completo

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
- Cada ingesta debe tocar `index.md` y `log.md`
- Preferir paginas cortas y enfocadas sobre paginas largas y genericas
- Links bidireccionales: si A referencia B, B debe referenciar A en "Ver tambien"
- Citar fuentes especificas: `(ver: [[resumen-correcciones]], seccion Seguridad)`
- No inventar nombres de modulos, paquetes o clases si la fuente no los define explicitamente; usar la nomenclatura real del material
- Si dos pasajes parecen contradecirse, priorizar el setup final recomendado por la catedra y dejar nota de la evolucion
