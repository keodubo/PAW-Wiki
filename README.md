# PAW Wiki - Arquitectura de Conocimiento Persistente

Bienvenido al **PAW Wiki**, un "Segundo Cerebro" estructurado diseñado para almacenar, sintetizar y hacer evolucionar el conocimiento del proyecto **PAW (Programación de Aplicaciones Web)**.

Este repositorio sirve como un centro centralizado para conceptos técnicos, patrones arquitectónicos y documentación oficial, optimizado tanto para la lectura humana como para la ingesta por parte de LLMs.

---

## Quickstart

Este repo no necesita servidor, base de datos ni build. Funciona como una wiki Markdown que podes leer en GitHub, abrir en Obsidian o usar como contexto para un agente.

### 1. Clonar

Uso recomendado dentro de una app PAW:

```bash
cd /ruta/a/tu-repo-paw
printf "\n# Local PAW wiki, not part of the delivered app\n/PAW-Wiki/\n" >> .gitignore
git clone https://github.com/keodubo/PAW-Wiki.git PAW-Wiki
cd PAW-Wiki
```

Asi la wiki queda al lado del codigo, pero el repo principal que entregas no sube documentos auxiliares ni el historial del wiki.

Verificacion desde la raiz de la app PAW:

```bash
git status --short --ignored=matching
git ls-files PAW-Wiki
```

`PAW-Wiki/` deberia aparecer como ignorado o no aparecer. `git ls-files PAW-Wiki` no deberia imprimir nada.

Si queres usarlo fuera de la app:

```bash
git clone https://github.com/keodubo/PAW-Wiki.git
cd PAW-Wiki
```

### 2. Abrir la wiki

Opcion simple:

```bash
open README.md
```

Opcion recomendada:

1. Instalar Obsidian.
2. Elegir `Open folder as vault`.
3. Seleccionar la carpeta `PAW-Wiki`.
4. Abrir `docs/index.md`.
5. Usar la vista de grafo para navegar los links `[[...]]`.

### 3. Usar la wiki

1. Leer [docs/index.md](docs/index.md).
2. Resolver la etapa de trabajo: `TP1`, `TP2` o `TP final`.
3. Abrir [docs/wiki/resumen-clases-paw-2026.md](docs/wiki/resumen-clases-paw-2026.md) y [docs/wiki/tp1-vs-tpe2-final.md](docs/wiki/tp1-vs-tpe2-final.md) si la tarea depende de stack, migracion o catedra.
4. Abrir una pagina de `docs/wiki/`.
5. Seguir los links internos.
6. Si vas a agregar material, usar [docs/examples/ingesta-publica.md](docs/examples/ingesta-publica.md).

Si usas un agente de coding, pedile que antes de responder revise si la wiki tiene actualizaciones remotas:

```text
Antes de usar PAW-Wiki, ejecuta el flujo de docs/examples/actualizar-wiki.md.
Si PAW-Wiki esta limpia y hay cambios en origin/main, podes hacer git pull --ff-only.
Si hay cambios locales, no pises nada: mostrame git status y git diff.
```

### 4. Configurar material privado

```bash
mkdir -p docs/private/mi-webapp/raw
mkdir -p docs/private/mi-webapp/wiki
mkdir -p docs/private/mi-webapp/plans
touch docs/private/mi-webapp/README.md
touch docs/private/mi-webapp/wiki/nexo-wiki-publica.md
```

`docs/private/` esta ignorado por Git. Todo lo personal, especifico de tu webapp o sensible debe ir ahi.

### 5. Instalar skills opcionales

Las skills son opcionales. Solo sirven si queres que Codex, Claude u otro agente use reglas especializadas de PAW.

```bash
mkdir -p "${CODEX_HOME:-$HOME/.codex}/skills"
cp -R skills/paw-* "${CODEX_HOME:-$HOME/.codex}/skills/"
```

Despues abri una conversacion nueva del agente para que detecte las skills.

Para usarlas, empezá por la orquestadora:

```text
Usa $paw-feature-master para trabajar esta tarea de mi app PAW.
Primero lee el checkout, CLAUDE.md, PAW-Wiki/docs/CLAUDE.md y PAW-Wiki/docs/index.md.
Estoy en etapa TP1/TP2/TP final.
Despues decidí que subskills de capa o migracion hacen falta y usalas solo si corresponden.
```

`$paw-feature-master` puede enrutar a subskills como `$paw-webapp-layer`, `$paw-services-layer`, `$paw-persistence-layer`, `$paw-testing-layer`, `$paw-tp2-migration` o `$paw-tp-final-migration` segun la tarea y la etapa. Si no sabes que capa toca, no elijas una subskill directa: usa `$paw-feature-master`.

### 6. Verificar antes de commitear

```bash
git status --short --ignored=matching
git ls-files docs/private
```

`git ls-files docs/private` no deberia imprimir nada.

Mas detalle:

- [Setup local](docs/examples/setup-local.md)
- [Wiki dentro del repo PAW](docs/examples/wiki-dentro-repo-paw.md)
- [Actualizar wiki local](docs/examples/actualizar-wiki.md)
- [Instalacion de skills](docs/examples/instalar-skills.md)
- [Uso con agentes y skills](docs/examples/uso-con-agente.md)
- [Checklist de publicacion](docs/examples/checklist-publicacion.md)
- [Troubleshooting](docs/examples/troubleshooting.md)

## Arquitectura del Repositorio

El proyecto sigue un esquema de conocimiento compuesto definido en [CLAUDE.md](docs/CLAUDE.md).

```bash
README.md
obsidian-graph-view.png
docs/
├── examples/       # Ejemplos copy-paste para usar y mantener la wiki
├── raw/            # Fuentes inmutables o historicas
├── wiki/           # Paginas sintetizadas con frontmatter y links Obsidian
├── superpowers/    # Planes largos generados por Superpowers
├── index.md        # Indice maestro del wiki
├── log.md          # Registro cronologico de cambios
├── tree.txt        # Snapshot del arbol de docs/
└── CLAUDE.md       # Reglas de trabajo para agentes
skills/
└── paw-*           # Skills instalables basadas en PAW-Wiki
```

`docs/private/` es una carpeta local ignorada por Git. Usala para fuentes, planes o notas personales que no deban publicarse.

### Componentes Clave
- **`raw/`**: La fuente de verdad inmutable. Incluye documentos originales de la catedra, apuntes y PDFs `PAW*` de clases. Los PDFs viejos pueden contener versiones de dependencias historicas: no usarlas como recomendacion vigente sin contrastar el checkout o enunciado actual.
- **`wiki/`**: El cerebro. Archivos Markdown que utilizan metadatos frontmatter y enlaces de estilo Obsidian `[[como-este]]`. Las paginas canonicas para etapa son `resumen-clases-paw-2026.md` y `tp1-vs-tpe2-final.md`.
- **`index.md`**: El faro. Siempre actualizado para reflejar el estado actual del wiki.
- **`examples/`**: Ejemplos de prompts, flujos y checklists para usar la wiki sin adivinar el procedimiento.
- **`skills/`**: Skills reutilizables para que Codex implemente features siguiendo la wiki, el stack PAW, la etapa del proyecto y la separación por capas.

---

## Dominio PAW por Etapa

Este wiki ya no debe leerse como material solo de TP1. La catedra evoluciona el stack por etapa y la respuesta correcta depende de esa etapa.

| Etapa | Canon operativo | Skills esperadas |
| --- | --- | --- |
| TP1 | Spring Web MVC sin Boot, JSP/JSTL, JDBC, PostgreSQL/HSQLDB, Maven, capas clasicas y validacion/i18n/security server-side. | `$paw-feature-master` + subskills de capa. No introducir JPA, API REST o SPA por accidente. |
| TP2 | Migracion de persistencia desde JDBC hacia JPA/Hibernate manteniendo contratos de servicios y producto estable. | `$paw-feature-master` + `$paw-tp2-migration` + capas afectadas. |
| TP final | REST API + SPA/frontend, DTOs, auth stateless, build frontend, cache y separacion clara entre API y cliente. | `$paw-feature-master` + `$paw-tp-final-migration` + capas afectadas. |

Fuentes clave:

- [docs/wiki/resumen-clases-paw-2026.md](docs/wiki/resumen-clases-paw-2026.md)
- [docs/wiki/tp1-vs-tpe2-final.md](docs/wiki/tp1-vs-tpe2-final.md)
- [docs/wiki/hibernate-jpa.md](docs/wiki/hibernate-jpa.md)
- [docs/wiki/api-rest.md](docs/wiki/api-rest.md)
- [docs/wiki/single-page-applications.md](docs/wiki/single-page-applications.md)

---

## Flujo de Trabajo para LLMs

Este repositorio está diseñado para ser "Nativo para IA". Si eres un asistente de IA trabajando en este repo:

1. **Ingerir**: Lee las fuentes en `docs/raw/` y sintetízalas en `docs/wiki/`.
2. **Enlazar**: Usa `[[enlaces-internos]]` para conectar conceptos y mantener la bidireccionalidad.
3. **Registrar**: Cada ingesta o cambio significativo debe quedar registrado en `docs/log.md` y referenciado en `docs/index.md`.
4. **Actualizar contexto**: Si hay acceso a Git remoto, revisar `origin/main` antes de usar la wiki; hacer `pull --ff-only` solo si el checkout esta limpio.
5. **Resolver etapa**: Antes de recomendar stack, migraciones o tooling, distinguir `TP1`, `TP2` y `TP final`.
6. **No congelar dependencias viejas**: Si un PDF antiguo menciona versiones concretas, tratarlas como contexto historico y validar contra el checkout actual.
7. **Seguir el Esquema**: Adhiérete estrictamente a las reglas definidas en [CLAUDE.md](docs/CLAUDE.md).

---

## Cómo Usar

### Para Humanos
- Comenza explorando el [Indice Maestro](docs/index.md).
- Busca conceptos especificos (ej: `spring-security`, `persistencia-jdbc`) en la carpeta `docs/wiki/`.
- Sigue los enlaces internos para navegar entre temas relacionados.
- Esta pensado para usar desde Obsidian.
- Usa [docs/examples/README.md](docs/examples/README.md) cuando necesites prompts o flujos de ejemplo.

![Vista del grafo del wiki en Obsidian](./obsidian-graph-view.png)

### Para Colaboradores
1. Agrega nuevas fuentes a `docs/raw/`.
2. Ejecuta un flujo de ingesta con un asistente de IA para procesar la nueva información.
3. Verifica que el `index.md` y el `log.md` se actualicen correctamente.
4. Si la fuente es personal o especifica de tu proyecto, guardala en `docs/private/` y no la subas.

Ejemplos listos para usar:

- [Setup local](docs/examples/setup-local.md)
- [Wiki dentro del repo PAW](docs/examples/wiki-dentro-repo-paw.md)
- [Actualizar wiki local](docs/examples/actualizar-wiki.md)
- [Ingesta publica](docs/examples/ingesta-publica.md)
- [Consulta a la wiki](docs/examples/consulta-wiki.md)
- [Uso con agentes](docs/examples/uso-con-agente.md)
- [Second brain privado](docs/examples/second-brain-privado.md)
- [Checklist de publicacion](docs/examples/checklist-publicacion.md)
- [Troubleshooting](docs/examples/troubleshooting.md)

### Para Instalar las Skills en Codex, Claude u otros asistentes

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

Después abrí una conversación nueva y usa:

```text
Usa $paw-feature-master para planificar esta feature de mi app PAW según la wiki y las capas.
Estoy en etapa TP1/TP2/TP final.
```

Más detalles para otros asistentes compatibles en [skills/README.md](skills/README.md).

---

*Mantenedor: keodubo*
