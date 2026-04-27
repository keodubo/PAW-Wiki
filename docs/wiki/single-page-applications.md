---
titulo: Single Page Applications
tipo: concepto
fuentes: [raw/apuntes.txt, raw/PAW - clases 9 y 10 (TP final).pdf, raw/PAW- Clase Teórica Front end (TP final).pdf, raw/PAW- Clase Teórica - SPAs segunda parte (TP final).pdf]
creado: 2026-04-13
actualizado: 2026-04-27
---

# Single Page Applications

Esta pagina resume el salto a **SPA** para TP final. Es material fuera del foco de TP1 y posterior a la migracion TP2 de persistencia.

## Cambio de paradigma

- Una SPA mueve gran parte de la responsabilidad al frontend.
- Se pasa de un **thin client** a un **thick client**.
- El cliente maneja dinamicamente mas input del usuario y evita round-trips innecesarios al backend.
- La ventaja es dinamismo y separacion de concerns; la desventaja es duplicar logica que antes estaba solo server-side, especialmente validacion, errores y estado.
- La descarga inicial puede crecer por framework, runtime y assets; hay que medir performance.

## Modulos, runtime y bundling

- JS Modules permiten unidades independientes de codigo reusable.
- Node, Deno y Bun aparecen como runtimes para ejecutar tooling.
- Package managers y registries organizan paquetes publicos, privados o self-hosted.
- Bundlers como Webpack, esbuild y Vite combinan, transpilan, minifican y optimizan JS/CSS/HTML para browsers.
- El criterio de la clase no es casarse con una herramienta concreta, sino entender el pipeline de build que despues debe integrarse con el packaging.

## Componentes, formularios y estado

- Los componentes son la unidad minima de negocio del frontend y deberian tender a ser puros.
- Las librerias de componentes pueden ser unstyled o styled; elegir segun cuanto control visual se necesita.
- Los formularios validan en cliente para feedback inmediato, sin eliminar validacion backend.
- La capa de estado funciona como "backend del front": guarda estado, concentra llamadas asincronicas y conecta componentes.
- El flujo unidireccional busca una unica fuente de verdad; stores como Redux/Zustand o primitives como `useState` dependen de complejidad.
- En Angular, RxJS/Observables y `BehaviorSubject` cumplen un rol importante para datos asincronicos y estado compartido.

## Autenticacion y seguridad en SPA

- Parte de la autenticacion deja de resolverse con vistas server-side y cookies clasicas.
- El frontend pasa a manejar login, registro, cambio de password, sesion y parte del acceso.
- En apuntes se plantea el pasaje desde cookies hacia almacenamiento/control del lado cliente.
- Eso da mas control sobre cuando se envia informacion, pero tambien cambia la superficie de riesgo y el modelo mental del flujo.
- El frontend debe manejar 401/403/404, expiracion de sesion/token y errores globales.
- Interceptors permiten comportamiento transversal sobre requests HTTP.

## Internacionalizacion en frontend

- La i18n deja de estar encapsulada en JSPs y `MessageSource`.
- Hay que elegir una libreria de internacionalizacion del lado cliente.
- La migracion no deberia perder features: placeholders, variantes de idioma, fallback y consistencia de textos.
- Herramientas como i18next y scanners de keys ayudan a mantener catalogos de textos.

## Testing

- Al crecer la logica de frontend, crece la necesidad de testear esa capa.
- Ya no alcanza con pensar solo en unit tests de backend.
- Los apuntes nombran herramientas como `Jest` y `Vitest`, y de mocking como `Nock` y `Sinon`.
- Aun asi, la capa de servicios/estado sigue siendo la pieza que "si o si" tiene que estar testeada.
- La clase separa tests de componentes puros y tests de capa de estado.

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
- Esta integracion no debe romper `mvn package`: el WAR final debe contener los assets listos para servir.

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
- [[resumen-clases-paw-2026]]
- [[internacionalizacion]]
- [[tp1-vs-tpe2-final]]
- [[ux-flows]]
- [[resumen-apuntes]]
