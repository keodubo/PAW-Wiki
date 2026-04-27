# Example: Checklist de Publicacion

Usa este checklist antes de commitear o pushear cambios al repo publico.

## 1. Estado de Git

```bash
git status --short --branch --ignored=matching
```

Esperado:

- Cambios publicos claros en archivos que queres subir.
- `docs/private/` como `!! docs/private/` o sin aparecer.
- Nada sensible como archivo trackeado.

## 2. Privacidad

```bash
git ls-files docs/private
```

No deberia imprimir nada.

Buscar terminos sensibles:

```bash
rg -i "token|secret|password|localhost|credencial" README.md docs skills -g '!docs/private/**'
```

Si aparece algo sensible, moverlo a `docs/private/` o reescribirlo como ejemplo generico.

## 2.1 Si PAW-Wiki esta dentro del repo de la app

Desde la raiz de la app PAW:

```bash
git status --short --ignored=matching
git ls-files PAW-Wiki
```

`git ls-files PAW-Wiki` no deberia imprimir nada. Si imprime archivos, estas por subir la wiki al repo entregable.

La raiz de la app deberia tener una regla como esta en `.gitignore`:

```gitignore
# Local PAW wiki, not part of the delivered app
/PAW-Wiki/
```

## 3. Links Markdown

```bash
python3 - <<'PY'
from pathlib import Path
import re, sys
files = [Path('README.md'), Path('docs/CLAUDE.md'), Path('docs/index.md'), Path('skills/README.md'), *Path('docs/examples').glob('*.md')]
missing = []
for p in files:
    text = p.read_text(encoding='utf-8')
    for m in re.finditer(r'\[[^\]]+\]\(([^)]+)\)', text):
        target = m.group(1).split('#', 1)[0]
        if not target or '://' in target or target.startswith('mailto:'):
            continue
        if not (p.parent / target).resolve().exists():
            missing.append((str(p), target))
if missing:
    for p, target in missing:
        print(f'{p}: missing {target}')
    sys.exit(1)
print('ok')
PY
```

## 4. Formato

```bash
git diff --check
```

## 5. Tree

Si agregaste, moviste o borraste archivos bajo `docs/`, actualizar:

```bash
find docs -path docs/private -prune -o -type f ! -name '.DS_Store' -print | sort | sed 's#^#PAW-Wiki/#' > docs/tree.txt
```

## 6. Commit y push

```bash
git add <archivos>
git commit -m "docs: descripcion corta"
git push origin main
```

Si Git dice que `main` no tiene upstream:

```bash
git branch --set-upstream-to=origin/main main
git push origin main
```
