# PAW Wiki - Base de Conocimiento para PAW

**PAW Wiki** es una base de conocimiento en Markdown para organizar material de
**PAW (Programación de Aplicaciones Web)** por etapa: `TP1`, `TP2` y
`TP final`.

El repositorio centraliza conceptos técnicos, patrones arquitectónicos,
criterios de cursada, ejemplos operativos y skills reutilizables para agentes.
Está pensado para lectura humana, navegación en Obsidian y uso como contexto de
trabajo para asistentes de IA.

---

## Quickstart

Este repositorio no necesita servidor, base de datos ni build. Funciona como una
wiki Markdown que puedes leer en GitHub, abrir en Obsidian o usar como contexto
para un agente.

### 1. Clonar

Uso recomendado dentro de una app PAW:

```bash
cd /ruta/a/tu-repo-paw
printf "\n# Local PAW wiki, not part of the delivered app\n/PAW-Wiki/\n" >> .gitignore
git clone https://github.com/keodubo/PAW-Wiki.git PAW-Wiki
cd PAW-Wiki
```

Así la wiki queda junto al código, pero el repositorio principal de la app no
incluye documentos auxiliares ni el historial de este repositorio.

Verificación desde la raíz de la app PAW:

```bash
git status --short --ignored=matching
git ls-files PAW-Wiki
```

`PAW-Wiki/` debería aparecer como ignorado o no aparecer. `git ls-files
PAW-Wiki` no debería imprimir nada.

Si quieres usarlo fuera de una app:

```bash
git clone https://github.com/keodubo/PAW-Wiki.git
cd PAW-Wiki
```

### 2. Abrir la wiki

Opción simple:

```bash
open README.md
```

Opción recomendada:

1. Instalar Obsidian.
2. Elegir `Open folder as vault`.
3. Seleccionar la carpeta `PAW-Wiki`.
4. Abrir `docs/index.md`.
5. Usar la vista de grafo para navegar los enlaces `[[...]]`.

### 3. Usar la wiki

1. Leer [docs/index.md](docs/index.md).
2. Resolver la etapa de trabajo: `TP1`, `TP2` o `TP final`.
3. Abrir [docs/wiki/resumen-clases-paw-2026.md](docs/wiki/resumen-clases-paw-2026.md)
   y [docs/wiki/tp1-vs-tpe2-final.md](docs/wiki/tp1-vs-tpe2-final.md) si la
   tarea depende de stack, migración o cátedra.
4. Abrir una página de `docs/wiki/`.
5. Seguir los enlaces internos.
6. Si vas a agregar material, usar
   [docs/examples/ingesta-publica.md](docs/examples/ingesta-publica.md).

Si usas un agente de coding, indícale que antes de responder revise si la wiki
tiene actualizaciones remotas:

```text
Antes de usar PAW-Wiki, ejecuta el flujo de docs/examples/actualizar-wiki.md.
Si PAW-Wiki está limpia y hay cambios en origin/main, puedes hacer git pull --ff-only.
Si hay cambios locales, no modifiques nada: muestra git status y git diff.
```

### 4. Configurar material privado

```bash
mkdir -p docs/private/mi-webapp/raw
mkdir -p docs/private/mi-webapp/wiki
mkdir -p docs/private/mi-webapp/plans
touch docs/private/mi-webapp/README.md
touch docs/private/mi-webapp/wiki/nexo-wiki-publica.md
```

`docs/private/` está ignorado por Git. Todo material personal, específico de tu
webapp o sensible debe ir allí.

### 5. Instalar skills opcionales

Las skills son opcionales. Sirven para que Codex, Claude u otro agente use
reglas especializadas de PAW.

```bash
mkdir -p "${CODEX_HOME:-$HOME/.codex}/skills"
cp -R skills/paw-* "${CODEX_HOME:-$HOME/.codex}/skills/"
```

Después abre una conversación nueva del agente para que detecte las skills.

Para usarlas, empieza por la orquestadora:

```text
Usa $paw-feature-master para trabajar esta tarea de mi app PAW.
Primero lee el checkout, CLAUDE.md, PAW-Wiki/docs/CLAUDE.md y PAW-Wiki/docs/index.md.
Estoy en etapa TP1/TP2/TP final.
Después decide qué subskills de capa o migración hacen falta y úsalas solo si corresponden.
```

`$paw-feature-master` puede enrutar a subskills como `$paw-webapp-layer`,
`$paw-services-layer`, `$paw-persistence-layer`, `$paw-testing-layer`,
`$paw-tp2-migration` o `$paw-tp-final-migration` según la tarea y la etapa. Si
no sabes qué capa toca, no elijas una subskill directa: usa
`$paw-feature-master`.

### 6. Verificar antes de publicar cambios

```bash
git status --short --ignored=matching
git ls-files docs/private
```

`git ls-files docs/private` no debería imprimir nada.

Más detalle:

- [Setup local](docs/examples/setup-local.md)
- [Wiki dentro del repo PAW](docs/examples/wiki-dentro-repo-paw.md)
- [Actualizar wiki local](docs/examples/actualizar-wiki.md)
- [Instalación de skills](docs/examples/instalar-skills.md)
- [Uso con agentes y skills](docs/examples/uso-con-agente.md)
- [Checklist de publicación](docs/examples/checklist-publicacion.md)
- [Troubleshooting](docs/examples/troubleshooting.md)

## Arquitectura del Repositorio

El repositorio sigue un esquema de conocimiento compuesto definido en
[docs/CLAUDE.md](docs/CLAUDE.md).

```bash
README.md
obsidian-graph-view.png
docs/
├── examples/       # Ejemplos copy-paste para usar y mantener la wiki
├── raw/            # Fuentes inmutables o históricas
├── wiki/           # Páginas sintetizadas con frontmatter y enlaces Obsidian
├── superpowers/    # Planes largos generados por Superpowers
├── index.md        # Índice maestro del wiki
├── log.md          # Registro cronológico de cambios
├── tree.txt        # Snapshot del árbol de docs/
└── CLAUDE.md       # Reglas de trabajo para agentes
skills/
└── paw-*           # Skills instalables basadas en PAW-Wiki
```

`docs/private/` es una carpeta local ignorada por Git. Úsala para fuentes,
planes o notas personales que no deban publicarse.

### Componentes clave

- **`raw/`**: fuentes inmutables. Incluye documentos originales de la cátedra,
  apuntes y PDFs `PAW*` de clases. Los PDFs viejos pueden contener versiones de
  dependencias históricas: no usarlas como recomendación vigente sin contrastar
  el checkout o enunciado actual.
- **`wiki/`**: páginas Markdown con frontmatter y enlaces estilo Obsidian
  `[[como-este]]`. Las páginas canónicas para etapa son
  `resumen-clases-paw-2026.md` y `tp1-vs-tpe2-final.md`.
- **`index.md`**: índice maestro del repositorio. Debe reflejar el estado actual
  de la wiki.
- **`examples/`**: prompts, flujos y checklists para usar y mantener la wiki.
- **`skills/`**: skills reutilizables para que agentes trabajen con PAW siguiendo
  la etapa, el stack y la separación por capas.

---

## Dominio PAW por Etapa

Este wiki no debe leerse como material solo de TP1. La cátedra evoluciona el
stack por etapa y la respuesta correcta depende de esa etapa.

| Etapa | Canon operativo | Skills esperadas |
| --- | --- | --- |
| TP1 | Spring Web MVC sin Boot, JSP/JSTL, JDBC, PostgreSQL/HSQLDB, Maven, capas clásicas y validación/i18n/security server-side. | `$paw-feature-master` + subskills de capa. No introducir JPA, API REST o SPA por accidente. |
| TP2 | Migración de persistencia desde JDBC hacia JPA/Hibernate manteniendo contratos de servicios y producto estable. | `$paw-feature-master` + `$paw-tp2-migration` + capas afectadas. |
| TP final | REST API + SPA/frontend, DTOs, auth stateless, build frontend, cache y separación clara entre API y cliente. | `$paw-feature-master` + `$paw-tp-final-migration` + capas afectadas. |

Fuentes clave:

- [docs/wiki/resumen-clases-paw-2026.md](docs/wiki/resumen-clases-paw-2026.md)
- [docs/wiki/tp1-vs-tpe2-final.md](docs/wiki/tp1-vs-tpe2-final.md)
- [docs/wiki/hibernate-jpa.md](docs/wiki/hibernate-jpa.md)
- [docs/wiki/api-rest.md](docs/wiki/api-rest.md)
- [docs/wiki/single-page-applications.md](docs/wiki/single-page-applications.md)

---

## Flujo de Trabajo para Asistentes de IA

Si eres un asistente de IA trabajando en este repositorio:

1. **Ingerir**: leer las fuentes en `docs/raw/` y sintetizarlas en `docs/wiki/`.
2. **Enlazar**: usar `[[enlaces-internos]]` para conectar conceptos y mantener la bidireccionalidad.
3. **Registrar**: cada ingesta o cambio significativo debe quedar registrado en `docs/log.md` y referenciado en `docs/index.md`.
4. **Actualizar contexto**: si hay acceso a Git remoto, revisar `origin/main` antes de usar la wiki; hacer `pull --ff-only` solo si el checkout está limpio.
5. **Resolver etapa**: antes de recomendar stack, migraciones o tooling, distinguir `TP1`, `TP2` y `TP final`.
6. **No congelar dependencias viejas**: si un PDF antiguo menciona versiones concretas, tratarlas como contexto histórico y validar contra el checkout actual.
7. **Seguir el esquema**: respetar las reglas definidas en [docs/CLAUDE.md](docs/CLAUDE.md).

---

## Cómo Usar

### Para humanos

- Comienza explorando el [índice maestro](docs/index.md).
- Busca conceptos específicos, por ejemplo `spring-security` o
  `persistencia-jdbc`, en la carpeta `docs/wiki/`.
- Sigue los enlaces internos para navegar entre temas relacionados.
- El repositorio está preparado para usarse desde Obsidian.
- Usa [docs/examples/README.md](docs/examples/README.md) cuando necesites
  prompts o flujos de ejemplo.

![Vista del grafo del wiki en Obsidian](./obsidian-graph-view.png)

### Para colaboradores

1. Agrega nuevas fuentes a `docs/raw/`.
2. Ejecuta un flujo de ingesta con un asistente de IA para procesar la nueva información.
3. Verifica que el `index.md` y el `log.md` se actualicen correctamente.
4. Si la fuente es personal o específica de tu proyecto, guárdala en
   `docs/private/` y no la publiques.

Ejemplos listos para usar:

- [Setup local](docs/examples/setup-local.md)
- [Wiki dentro del repo PAW](docs/examples/wiki-dentro-repo-paw.md)
- [Actualizar wiki local](docs/examples/actualizar-wiki.md)
- [Ingesta pública](docs/examples/ingesta-publica.md)
- [Consulta a la wiki](docs/examples/consulta-wiki.md)
- [Uso con agentes](docs/examples/uso-con-agente.md)
- [Second brain privado](docs/examples/second-brain-privado.md)
- [Checklist de publicación](docs/examples/checklist-publicacion.md)
- [Troubleshooting](docs/examples/troubleshooting.md)

### Para instalar las skills en Codex, Claude u otros asistentes

Desde la raíz de este repositorio:

```bash
mkdir -p "${CODEX_HOME:-$HOME/.codex}/skills"
cp -R skills/paw-* "${CODEX_HOME:-$HOME/.codex}/skills/"
```

Para Claude Code:

```bash
mkdir -p "$HOME/.claude/skills"
cp -R skills/paw-* "$HOME/.claude/skills/"
```

Después abre una conversación nueva y usa:

```text
Usa $paw-feature-master para planificar esta feature de mi app PAW según la wiki y las capas.
Estoy en etapa TP1/TP2/TP final.
```

Más detalles para otros asistentes compatibles en [skills/README.md](skills/README.md).

---

*Mantenedor: keodubo*
