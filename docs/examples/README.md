# Examples de Uso de PAW Wiki

Esta carpeta contiene ejemplos copy-paste para usar, mantener y extender la wiki sin tener que reconstruir el flujo cada vez.

Desde la ingesta de las clases `PAW*`, los ejemplos deben asumirse por etapa: cuando una tarea toque stack, arquitectura, persistencia, API o frontend, primero indicar o resolver `TP1`, `TP2` o `TP final`.

## Archivos

| Archivo | Para que sirve |
| --- | --- |
| `setup-local.md` | Clonar, abrir en Obsidian y dejar listo el espacio privado. |
| `wiki-dentro-repo-paw.md` | Clonar o forkear la wiki dentro del repo PAW sin subirla al repo de entrega. |
| `actualizar-wiki.md` | Revisar si hay cambios en `origin/main` y actualizar la wiki local de forma segura. |
| `instalar-skills.md` | Instalar las skills opcionales en Codex, Claude u otros agentes. |
| `ingesta-publica.md` | Convertir una fuente compartible en paginas de `docs/wiki/`. |
| `consulta-wiki.md` | Hacer preguntas usando el indice y las paginas canonicas. |
| `uso-con-agente.md` | Darle a Codex, Claude u otro agente un prompt de trabajo claro, incluyendo etapa y skills de migracion cuando correspondan. |
| `second-brain-privado.md` | Separar material personal en `docs/private/` sin publicarlo. |
| `checklist-publicacion.md` | Verificar que el repo este listo antes de commitear o pushear. |
| `troubleshooting.md` | Resolver problemas comunes de Git, Obsidian, imagenes y privacidad. |

## Regla general

- Si el contenido es reusable por cualquier estudiante, puede ir a `docs/raw/` y `docs/wiki/`.
- Si el contenido depende de tu webapp, rutas reales, capturas, diagramas, decisiones internas o credenciales, va a `docs/private/`.
- Si una fuente privada se vuelve util para todos, reescribila como patron general antes de publicarla.
- Si un PDF viejo menciona versiones de dependencias, tratarlas como dato historico; no copiarlas como recomendacion vigente sin verificar.

## Checklist rapido

```bash
git status --short --ignored=matching
git ls-files docs/private
find docs -path docs/private -prune -o -type f ! -name '.DS_Store' -print | sort
```

`git ls-files docs/private` no deberia imprimir nada.

## Camino recomendado para una persona nueva

1. Leer `setup-local.md`.
2. Si trabaja en una app PAW, leer `wiki-dentro-repo-paw.md`.
3. Si la wiki ya estaba clonada, leer `actualizar-wiki.md` antes de usarla.
4. Abrir `../index.md`.
5. Revisar `../wiki/resumen-clases-paw-2026.md` y `../wiki/tp1-vs-tpe2-final.md`.
6. Revisar `consulta-wiki.md`.
7. Si va a usar un agente, leer `uso-con-agente.md` e indicar etapa.
8. Si va a agregar material propio, leer `second-brain-privado.md`.
9. Antes de commitear, correr `checklist-publicacion.md`.
