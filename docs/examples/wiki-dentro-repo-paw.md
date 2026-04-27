# Example: Wiki Dentro del Repo PAW

La forma recomendada de usar PAW-Wiki durante el desarrollo es tenerla dentro del checkout de tu app PAW, pero ignorada por el Git del proyecto que entregas.

Asi el agente puede encontrar la wiki al lado del codigo, pero no subis documentos auxiliares, planes, grafo de Obsidian ni archivos personales al `main` del proyecto de la materia.

## Estructura esperada

```text
mi-repo-paw/
  pom.xml
  webapp/
  services/
  persistence/
  PAW-Wiki/          <- repo separado, local o fork propio
  .gitignore        <- ignora /PAW-Wiki/
```

## Opcion A: clonar la wiki publica dentro de tu app

Desde la raiz de tu app PAW:

```bash
cd /ruta/a/mi-repo-paw
printf "\n# Local PAW wiki, not part of the delivered app\n/PAW-Wiki/\n" >> .gitignore
git clone https://github.com/keodubo/PAW-Wiki.git PAW-Wiki
```

Verificar:

```bash
git status --short --ignored=matching
git -C PAW-Wiki status --short --branch
```

En el repo de la app, `PAW-Wiki/` deberia aparecer como ignorado:

```text
!! PAW-Wiki/
```

## Opcion B: forkear primero y clonar tu fork

Usa esta opcion si queres guardar tus cambios de wiki en tu propio GitHub.

```bash
cd /ruta/a/mi-repo-paw
printf "\n# Local PAW wiki, not part of the delivered app\n/PAW-Wiki/\n" >> .gitignore
git clone git@github.com:<tu-usuario>/PAW-Wiki.git PAW-Wiki
```

Los commits de la wiki se hacen adentro de `PAW-Wiki/`:

```bash
cd PAW-Wiki
git status --short --branch
git add README.md docs
git commit -m "docs: mi cambio"
git push origin main
```

Los commits de la app PAW se hacen en la raiz de `mi-repo-paw/`. No mezcles ambos.

## Alternativa local sin tocar .gitignore

Si no queres modificar el `.gitignore` del proyecto entregable, podes ignorar la wiki solo en tu copia local:

```bash
cd /ruta/a/mi-repo-paw
printf "\n/PAW-Wiki/\n" >> .git/info/exclude
git clone https://github.com/keodubo/PAW-Wiki.git PAW-Wiki
```

Esta regla no se commitea. Sirve si la wiki es solo tu herramienta local.

## Por que no commitear PAW-Wiki dentro del repo de entrega

- Agrega documentos que no forman parte de la entrega de la app.
- Puede subir planes, auditorias, capturas o notas privadas por error.
- Puede convertir la wiki en submodule/gitlink si haces `git add PAW-Wiki`.
- Ensucia el diff del proyecto principal.

La app PAW y PAW-Wiki deben versionarse por separado.

## Checklist rapido

Desde la raiz de la app:

```bash
git status --short --ignored=matching
git ls-files PAW-Wiki
```

`git ls-files PAW-Wiki` no deberia imprimir nada.

Desde la wiki:

```bash
git -C PAW-Wiki status --short --branch
```
