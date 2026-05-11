---
titulo: PAW Unidad 3 - Frontend, JSP y JSTL
tipo: fuente
fuentes: [raw/apuntes.txt, raw/pdfs/PAW - Apuntes.pdf]
creado: 2026-05-11
actualizado: 2026-05-11
---

# PAW Unidad 3 - Frontend, JSP y JSTL

Esta unidad cubre la interfaz server-side del TP1. La idea fuerte es que frontend no significa framework SPA: para esta etapa, la UI se construye con HTML/CSS, JSP, JSTL, tags y criterios de usabilidad.

## Diseno de interfaz

- La materia prioriza usabilidad antes que impacto visual aislado.
- CSS sigue siendo la herramienta base: cascada, especificidad, layout y componentes reutilizables importan.
- Los estilos inline son excepcionales; si se repiten, pertenecen a una clase CSS.
- UI kits pueden aportar componentes HTML/CSS, pero no justifican introducir una arquitectura SPA en TP1.

## JSP como vista server-side

- Una JSP se traduce a servlet y genera HTML dinamico.
- Los objetos expuestos a vista deberian tener forma de Bean: propiedades privadas, getters y constructor default.
- La vista recibe datos preparados por controller/modelo; no deberia resolver reglas de negocio.
- Scriptlets y Java arbitrario en JSP son historicos y deben evitarse.

## EL, JSTL y tags

- Expression Language simplifica acceso a propiedades y colecciones.
- JSTL reemplaza codigo Java en vista por tags declarativos.
- `c:out` es importante porque escapa salida y reduce riesgo de XSS.
- `c:url` genera rutas compatibles con context path y deploy real.
- `c:if`, `c:choose` y `c:forEach` cubren control de flujo de presentacion.
- Spring form tags conectan formularios con form beans y errores.

## Tags propios

- Un tag propio puede encapsular un patron repetido de UI.
- El TLD define nombre, atributos y handler.
- Tiene sentido cuando reduce duplicacion real, no para esconder logica de negocio.

## Para TP1

- Ubicar JSPs bajo `WEB-INF/jsp/` para evitar acceso directo.
- Escapar toda salida dinamica de usuario.
- Usar `c:url` para links, actions y assets.
- Mantener vistas como presentacion: decisiones de negocio van a services.
- No introducir SPA por accidente en la etapa server-side.

## Ver tambien

- [[resumen-apuntes]]
- [[jsp-jstl]]
- [[xss-prevencion]]
- [[ux-flows]]
- [[validacion-formularios]]
- [[paw-unidad-02-contexto-historico-web]]
- [[paw-unidad-04-spring-mvc-capas-jdbc]]
