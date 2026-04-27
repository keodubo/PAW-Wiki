# PAW Wiki - Arquitectura de Conocimiento Persistente

Bienvenido al **PAW Wiki**, un "Segundo Cerebro" estructurado diseñado para almacenar, sintetizar y hacer evolucionar el conocimiento del proyecto **PAW (Programación de Aplicaciones Web)**.

Este repositorio sirve como un centro centralizado para conceptos técnicos, patrones arquitectónicos y documentación oficial, optimizado tanto para la lectura humana como para la ingesta por parte de LLMs.

---

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
- **`raw/`**: La fuente de verdad. Documentos originales de la cátedra o manuales técnicos.
- **`wiki/`**: El cerebro. Archivos Markdown que utilizan metadatos frontmatter y enlaces de estilo Obsidian `[[como-este]]`.
- **`index.md`**: El faro. Siempre actualizado para reflejar el estado actual del wiki.
- **`examples/`**: Ejemplos de prompts, flujos y checklists para usar la wiki sin adivinar el procedimiento.
- **`skills/`**: Skills reutilizables para que Codex implemente features siguiendo la wiki, el stack PAW y la separación por capas.

---

## Stack Tecnológico (PAW TPE1)

Este wiki se especializa en el desarrollo del proyecto PAW utilizando el siguiente stack:

- **Core**: Java 21, Spring Web MVC (Puro, sin Spring Boot).
- **Frontend**: JSP (JavaServer Pages), JSTL.
- **Base de Datos**: PostgreSQL (Producción), HSQLDB (Testing), JDBC para conectividad.
- **Herramienta de Construcción**: Maven.
- **Patrones**: Domain Driven Design (DDD), Inyección de Dependencias, Thin Controllers.

---

## Flujo de Trabajo para LLMs

Este repositorio está diseñado para ser "Nativo para IA". Si eres un asistente de IA trabajando en este repo:

1. **Ingerir**: Lee las fuentes en `docs/raw/` y sintetízalas en `docs/wiki/`.
2. **Enlazar**: Usa `[[enlaces-internos]]` para conectar conceptos y mantener la bidireccionalidad.
3. **Registrar**: Cada ingesta o cambio significativo debe quedar registrado en `docs/log.md` y referenciado en `docs/index.md`.
4. **Seguir el Esquema**: Adhiérete estrictamente a las reglas definidas en [CLAUDE.md](docs/CLAUDE.md).

---

## Cómo Usar

### Para Humanos
- Comienza explorando el [Índice Maestro](docs/index.md).
- Busca conceptos específicos (ej: `spring-security`, `jdbc-patterns`) en la carpeta `docs/wiki/`.
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

- [Ingesta publica](docs/examples/ingesta-publica.md)
- [Consulta a la wiki](docs/examples/consulta-wiki.md)
- [Uso con agentes](docs/examples/uso-con-agente.md)
- [Second brain privado](docs/examples/second-brain-privado.md)

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
Usa $paw-feature-master para planificar esta feature de Forkd según la wiki y las capas.
```

Más detalles para otros asistentes compatibles en [skills/README.md](skills/README.md).

---

*Mantenedor: keodubo*
