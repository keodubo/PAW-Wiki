# Examples de Uso de PAW Wiki

Esta carpeta contiene ejemplos copy-paste para usar, mantener y extender la wiki sin tener que reconstruir el flujo cada vez.

## Archivos

| Archivo | Para que sirve |
| --- | --- |
| `ingesta-publica.md` | Convertir una fuente compartible en paginas de `docs/wiki/`. |
| `consulta-wiki.md` | Hacer preguntas usando el indice y las paginas canonicas. |
| `uso-con-agente.md` | Darle a Codex, Claude u otro agente un prompt de trabajo claro. |
| `second-brain-privado.md` | Separar material personal en `docs/private/` sin publicarlo. |

## Regla general

- Si el contenido es reusable por cualquier estudiante, puede ir a `docs/raw/` y `docs/wiki/`.
- Si el contenido depende de tu webapp, rutas reales, capturas, diagramas, decisiones internas o credenciales, va a `docs/private/`.
- Si una fuente privada se vuelve util para todos, reescribila como patron general antes de publicarla.

## Checklist rapido

```bash
git status --short --ignored=matching
git ls-files docs/private
find docs -path docs/private -prune -o -type f ! -name '.DS_Store' -print | sort
```

`git ls-files docs/private` no deberia imprimir nada.
