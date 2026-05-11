---
titulo: PAW Unidad 2 - Contexto Historico Web
tipo: fuente
fuentes: [raw/apuntes.txt, raw/pdfs/PAW - Apuntes.pdf]
creado: 2026-05-11
actualizado: 2026-05-11
---

# PAW Unidad 2 - Contexto Historico Web

Esta unidad explica por que el stack server-side de PAW existe: HTTP nace simple y stateless, la web dinamica agrega escritura, y Java/Spring aparecen para organizar aplicaciones que ya no pueden ser solo archivos estaticos.

## HTTP y web dinamica

- HTTP es request-response, iniciado por el cliente y sin estado propio.
- La web estatica podia mapear `GET` a archivos; la web dinamica necesita ejecutar logica.
- CGI/fastCGI abstraen sockets y permiten que programas generen respuestas.
- Esa solucion es simple conceptualmente, pero dificil de mantener cuando crecen rutas, estado, seguridad y vistas.

## Sesiones HTTP

- La sesion es una convencion sobre un protocolo stateless.
- El servidor genera un session id y asocia datos a ese identificador.
- El navegador reenvia la cookie para que el servidor reconozca el contexto.
- En multiples servidores, esto complica escalabilidad: se necesita storage compartido o sticky sessions.

## Java del lado servidor

- Java entra como plataforma portable y termina apoyandose en contenedores de aplicaciones.
- Servlets formalizan el modelo request-response en Java.
- El Front Controller concentra entrada y delegacion.
- Spring MVC se entiende mejor como una capa que organiza ese flujo, no como magia aislada.

## Mailing e imagenes

- Mailing aparece como funcionalidad de infraestructura de la app, no como tarea de controller.
- Templates, cuerpo, CTA y footer pertenecen a una preocupacion de servicio/comunicacion.
- Imagenes deben tratarse como datos con validacion y almacenamiento claro.
- Separar entidad e imagen evita mezclar metadata de dominio con bytes o variantes de recurso.

## Para TP1

- No asumir que HTTP conserva estado: cada flujo debe explicar si usa request, session, cookie o base.
- No resolver permisos solo con "mostrar u ocultar botones"; el servidor sigue decidiendo.
- No poner envio de mails o manejo de imagenes directo en JSP/controller.
- Recordar que el deploy puede tener varios nodos o configuraciones externas aunque localmente sea uno solo.

## Ver tambien

- [[resumen-apuntes]]
- [[http-y-sesiones]]
- [[mailing]]
- [[manejo-imagenes]]
- [[spring-web-mvc]]
- [[paw-unidad-01-introduccion-scrum-mvp]]
- [[paw-unidad-03-frontend-jsp-jstl]]
