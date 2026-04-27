---
titulo: Spring Web MVC y DispatcherServlet
tipo: concepto
fuentes: [raw/apuntes.txt]
creado: 2026-04-13
actualizado: 2026-04-27
---

# Spring Web MVC y DispatcherServlet

La parte mas operativa de la unidad 4 no es "usar Spring" en abstracto, sino entender como se arma una app web server-side con `web.xml`, `WebConfig`, controllers, `ModelAndView` y `ViewResolver`.

## DispatcherServlet y Front Controller

- Spring MVC usa un **Front Controller** central para recibir requests y despacharlos al controller correcto.
- En vez de escribir servlets sueltos, se mapean handlers de forma declarativa con anotaciones.
- El controller recibe input web, prepara el modelo y elige una vista.
- La logica de negocio no vive ahi: el controller no deberia "ejercitar el modelo" por su cuenta.

## web.xml y arranque de la app

- `web.xml` registra el `DispatcherServlet`.
- `load-on-startup` fuerza inicializacion eager en vez de lazy.
- `contextConfigLocation` permite indicar configuracion basada en codigo en lugar del XML default.
- `ContextLoaderListener` levanta el contexto global de Spring antes de que empiecen a entrar requests.

## WebConfig y descubrimiento de componentes

- `WebConfig.java` centraliza la configuracion del modulo web.
- `@ComponentScan` le dice a Spring donde buscar controllers y otros beans.
- Acotar el paquete a escanear mejora eficiencia y evita configurar clases que no interesan.
- El motor de DI permite separar configuracion y uso: la clase no crea sus dependencias, las recibe.

## Controllers, mapeos y ModelAndView

- `@Controller` y `@RequestMapping` declaran que requests se atienden y donde.
- `ModelAndView` encapsula dos cosas: datos a exponer y nombre/logica de vista a renderizar.
- El valor real es declarativo: se describe el mapeo y Spring se encarga del plumbing web.

## JSP privadas y ViewResolver

- Si un JSP queda publico dentro de `webapp`, puede abrirse por URL directa y saltearse el controller.
- La solucion correcta es mover las vistas a `WEB-INF/jsp/`, donde no son accesibles directamente.
- `ViewResolver` evita repetir paths largos como `WEB-INF/jsp/helloworld/index.jsp`.
- Se configura un prefijo y sufijo comunes para que el controller trabaje con nombres logicos de vista.

## Tooling y entorno local

- La cursada usa contenedor Java local para probar el war; en los apuntes se menciona Jetty.
- Maven no solo resuelve dependencias: compila, empaqueta y deja la app lista para correr en contenedor.

## Ver tambien

- [[http-y-sesiones]]
- [[spring-security]]
- [[configuracion-maven]]
- [[inyeccion-dependencias]]
- [[modelo-capas]]
- [[jsp-jstl]]
- [[resumen-transcripciones-clases-2-a-4]]
- [[resumen-clases-paw-2026]]
- [[tp1-vs-tpe2-final]]
- [[resumen-notas]]
- [[resumen-apuntes]]
- [[2026-04-24_auditoria-implementacion-contra-wiki_v1]]
