---
titulo: Single Page Applications
tipo: concepto
fuentes: [raw/apuntes.txt]
creado: 2026-04-13
actualizado: 2026-04-13
---

# Single Page Applications

Esta pagina resume la **unidad 10**. Es material orientado a TPE2/final y **fuera del foco de TP1**.

## Cambio de paradigma

- Una SPA mueve gran parte de la responsabilidad al frontend.
- Se pasa de un **thin client** a un **thick client**.
- El cliente maneja dinamicamente mas input del usuario y evita round-trips innecesarios al backend.

## Autenticacion y seguridad en SPA

- Parte de la autenticacion deja de resolverse con vistas server-side y cookies clasicas.
- El frontend pasa a manejar login, registro, cambio de password, sesion y parte del acceso.
- En apuntes se plantea el pasaje desde cookies hacia almacenamiento/control del lado cliente.
- Eso da mas control sobre cuando se envia informacion, pero tambien cambia la superficie de riesgo y el modelo mental del flujo.

## Internacionalizacion en frontend

- La i18n deja de estar encapsulada en JSPs y `MessageSource`.
- Hay que elegir una libreria de internacionalizacion del lado cliente.
- La migracion no deberia perder features: placeholders, variantes de idioma, fallback y consistencia de textos.

## Testing

- Al crecer la logica de frontend, crece la necesidad de testear esa capa.
- Ya no alcanza con pensar solo en unit tests de backend.
- Los apuntes nombran herramientas como `Jest` y `Vitest`, y de mocking como `Nock` y `Sinon`.
- Aun asi, la capa de servicios/estado sigue siendo la pieza que "si o si" tiene que estar testeada.

## Optimizacion

- La SPA se descarga una vez y por eso importan mucho tamano, parseo y tiempo de carga inicial.
- Minificacion de JS y CSS es una optimizacion base.
- No conviene asumir que el framework ya lo hace perfecto; hay que verificarlo.
- `Lighthouse` aparece como herramienta para medir performance, accesibilidad, SEO y buenas practicas.

## Hosting y build

- La entrega final deberia poder empaquetar tambien el frontend.
- Los apuntes mencionan `frontend-maven-plugin` para integrar el build al flujo Maven.
- `webapp` debe configurarse para servir los nuevos recursos estaticos.
- Si hay un modulo frontend dedicado, deberia ejecutarse antes del empaquetado final.

## SSR y limites del entorno

- Se menciona server-side rendering como tecnica posible, pero no exigida.
- Si existe un primer render en servidor, no se puede depender de `window` o `localStorage` en ese momento.

## Cache y file revving

- El cache fuerte apunta a usuarios recurrentes, no a mejorar el primer load.
- Se distingue entre cache condicional y no condicional.
- Regla fuerte: el root `/` no deberia cachearse como si fuera un asset inmutable.
- Los assets versionados usan **file revving**: cambia el nombre del archivo cuando cambia el contenido.
- Spring tambien necesita configuracion consistente para acompañar esa estrategia.

## Ver tambien

- [[api-rest]]
- [[internacionalizacion]]
- [[tp1-vs-tpe2-final]]
- [[ux-flows]]
- [[resumen-apuntes]]
