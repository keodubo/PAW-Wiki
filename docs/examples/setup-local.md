# Example: Setup Local

Este flujo deja la wiki funcionando desde cero. No hay servidor, build ni base de datos: el repo se usa como Markdown, Obsidian vault y contexto para agentes.

## Requisitos

Obligatorio:

- Git.
- Un editor de texto.

Recomendado:

- Obsidian para navegar links `[[...]]` y ver el grafo.
- Codex, Claude u otro agente si queres ingestar fuentes o generar planes.

## 1. Clonar el repo

Recomendado si estas trabajando en una app PAW:

```bash
cd /ruta/a/mi-repo-paw
printf "\n# Local PAW wiki, not part of the delivered app\n/PAW-Wiki/\n" >> .gitignore
git clone https://github.com/keodubo/PAW-Wiki.git PAW-Wiki
cd PAW-Wiki
```

Esto deja la wiki dentro del repo de la app, pero ignorada por el Git del proyecto principal que se entrega.

Si preferis tenerla en otra carpeta:

```bash
git clone https://github.com/keodubo/PAW-Wiki.git
cd PAW-Wiki
```

## 2. Verificar que estas en main

```bash
git branch --show-current
git status --short --branch
```

Salida esperada:

```text
main
## main...origin/main
```

Si Git dice que no hay upstream:

```bash
git branch --set-upstream-to=origin/main main
```

Si clonaste la wiki dentro de tu app PAW, tambien verificar desde la raiz de la app:

```bash
git status --short --ignored=matching
git ls-files PAW-Wiki
```

`git ls-files PAW-Wiki` no deberia imprimir nada.

## 3. Abrir en Obsidian

1. Abrir Obsidian.
2. Elegir `Open folder as vault`.
3. Seleccionar la carpeta `PAW-Wiki`.
4. Abrir `docs/index.md`.
5. Activar Graph View si queres ver relaciones entre paginas.

La imagen `obsidian-graph-view.png` muestra como deberia verse el grafo cuando Obsidian detecta los links.

## 4. Crear espacio privado local

```bash
mkdir -p docs/private/mi-webapp/raw
mkdir -p docs/private/mi-webapp/wiki
mkdir -p docs/private/mi-webapp/plans
touch docs/private/mi-webapp/README.md
touch docs/private/mi-webapp/wiki/nexo-wiki-publica.md
```

Ese contenido no se sube a Git porque `docs/private/` esta ignorado.

## 5. Leer la wiki

Orden recomendado:

1. `README.md`
2. `docs/CLAUDE.md`
3. `docs/index.md`
4. Una pagina concreta de `docs/wiki/`
5. Un example de `docs/examples/`

## 6. Verificar que no vas a subir privado

```bash
git status --short --ignored=matching
git ls-files docs/private
```

`git ls-files docs/private` no deberia imprimir nada.

Si `docs/private/` aparece con `!!`, esta ignorado correctamente.
