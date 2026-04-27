# Example: Second Brain Privado

Usa este flujo cuando el material pertenece a tu webapp o a tu proceso personal y no deberia publicarse.

## Estructura recomendada

```bash
mkdir -p docs/private/mi-webapp/raw
mkdir -p docs/private/mi-webapp/wiki
mkdir -p docs/private/mi-webapp/plans
touch docs/private/mi-webapp/README.md
touch docs/private/mi-webapp/wiki/nexo-wiki-publica.md
```

## Prompt para ingesta privada

```text
Lee README.md, docs/CLAUDE.md y docs/index.md.
Si la fuente toca stack, persistencia, API o frontend, lee tambien docs/wiki/resumen-clases-paw-2026.md y docs/wiki/tp1-vs-tpe2-final.md.
Despues lee docs/private/mi-webapp/raw/spec-flujo.md.

Objetivo:
- crear docs/private/mi-webapp/wiki/flujo.md;
- resolver etapa si corresponde: TP1, TP2 o TP final;
- conectar la decision privada con paginas publicas como [[resumen-clases-paw-2026]], [[tp1-vs-tpe2-final]], [[modelo-capas]], [[spring-security]], [[testing-unitario]], [[hibernate-jpa]], [[api-rest]] o [[single-page-applications]];
- actualizar docs/private/mi-webapp/wiki/nexo-wiki-publica.md;
- no tocar docs/index.md ni docs/log.md salvo que se cree una version publica y generica.
```

## Nexo privado-publico

Ejemplo de fila:

```markdown
| Tema privado | Fuente privada | Paginas publicas usadas | Decision privada |
| --- | --- | --- | --- |
| Alta de flujo propio | `raw/spec-flujo.md` | `[[modelo-capas]]`, `[[validacion-formularios]]` | La validacion web queda en form/validator y la regla de negocio queda en services. |
| Migracion de persistencia | `raw/spec-migracion.md` | `[[tp1-vs-tpe2-final]]`, `[[hibernate-jpa]]` | Si el proyecto esta en TP2, la migracion se planifica con JPA/Hibernate sin mezclar REST/SPA. |
```

## Publicar una version generica

Antes de mover algo privado a `docs/wiki/`:

1. Sacar nombres de producto.
2. Sacar rutas reales, capturas, IDs, secretos y datos internos.
3. Cambiar ejemplos concretos por patrones.
4. Crear o actualizar una pagina publica.
5. Actualizar `docs/index.md`, `docs/log.md` y `docs/tree.txt`.

## Verificacion antes de commitear

```bash
git status --short --ignored=matching
git ls-files docs/private
rg -i "token|secret|password|localhost" README.md docs -g '!docs/private/**'
```

`git ls-files docs/private` no deberia imprimir nada.
