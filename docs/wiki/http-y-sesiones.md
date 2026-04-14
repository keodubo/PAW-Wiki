---
titulo: HTTP, Sesiones y Evolucion de la Web
tipo: concepto
fuentes: [raw/apuntes.txt]
creado: 2026-04-13
actualizado: 2026-04-13
---

# HTTP, Sesiones y Evolucion de la Web

La unidad 2 no es solo contexto historico: explica por que el TP1 se implementa con HTTP stateless, sesiones por cookie, MVC server-side y frameworks Java.

## Base de HTTP

- HTTP es un protocolo **request-response** cuya dinamica siempre la inicia el cliente.
- Es **stateless**: el protocolo no mantiene estado entre requests.
- En la web original se servian archivos estaticos; por eso la base del protocolo es simple.
- La optimizacion clasica para servir archivos fue `sendfile()`, moviendo parte del trabajo al kernel.

## Web 2.0 y contenido dinamico

- Cuando la web pasa de "solo lectura" a "lectura y escritura", deja de alcanzar con mapear URLs a archivos estaticos.
- CGI y luego fastCGI permiten mapear requests a programas que leen entrada estandar y escriben la respuesta.
- Esto evita lidiar directamente con sockets, pero vuelve mas compleja la coordinacion de paginas y flujos.
- Ese aumento de complejidad es una de las razones por las que aparecen frameworks y herramientas de mas alto nivel.

## Sesiones HTTP: una convencion sobre un protocolo stateless

- La "sesion" no cambia la naturaleza del protocolo: sigue siendo stateless.
- Cuando un usuario se autentica, el servidor genera un `session id` suficientemente aleatorio.
- Ese identificador se guarda del lado del servidor junto con informacion auxiliar, y el browser lo vuelve a mandar en cookies.
- Por eso se dice que la sesion HTTP es una "inmensa mentira": el estado real vive afuera del protocolo.
- Si alguien envia la cookie correcta, el servidor no puede distinguir por HTTP puro si es "la misma persona".

## Limites operativos de la sesion

- Guardar sesion localmente en un servidor fisico complica el escalado horizontal.
- Si hay multiples servidores detras de una misma app, se necesita coordinacion adicional o afinidad de sesion.
- Los load balancers aparecen justamente para distribuir trafico entre nodos sin romper la ilusion de continuidad.

## Java del lado servidor

- El ecosistema Java termina estandarizando contenedores y APIs para aplicaciones web.
- En ese modelo aparecen los **servlets** y metodos como `doGet`, `doPost`, `doPut` y `doDelete`.
- La idea de **application container** abstrae parte de la infraestructura para concentrarse en el comportamiento de la app.
- El patron **Front Controller** organiza la entrada en un punto central en vez de dispersarla entre multiples handlers sueltos.

## Por que esto importa para el TP1

- No conviene inventar un manejo manual de sesion si Spring Security ya resuelve autenticacion, cookies y contexto de usuario.
- La capa web debe entender HTTP, requests y vistas; no mezclar eso con logica de negocio o persistencia.
- Los flujos de login, logout, remember-me y ownership tienen sentido porque la app vive sobre esta capa de sesiones "simuladas".
- Mailing e imagenes aparecen en esta unidad porque son preocupaciones tipicas de aplicaciones web reales, no solo del dominio.

## Ver tambien

- [[api-rest]]
- [[spring-web-mvc]]
- [[spring-security]]
- [[auth-flows]]
- [[mailing]]
- [[manejo-imagenes]]
- [[resumen-apuntes]]
