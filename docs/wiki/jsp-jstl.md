---
titulo: JSP, JSTL y Tags de Spring
tipo: concepto
fuentes: [raw/apuntes.txt, raw/correcciones.md]
creado: 2026-04-09
actualizado: 2026-04-13
---

# JSP, JSTL y Tags de Spring

La vista se compone de archivos JSP con JSTL. Prohibido incrustar codigo Java estatico en JSPs.

## Criterio de interfaz de la materia

- La cursada prioriza una UI usable por sobre una UI "linda" pero confusa.
- CSS sigue siendo la base principal de estilo.
- Para layout y tamano conviene usar primitivas nativas como `flex` y `grid`.
- Regla de clase: evitar estilos inline salvo excepciones muy puntuales.

## UI Kit vs Framework

- Los frameworks JS orientados a SPA no son la herramienta correcta para la primera entrega.
- Si resultan utiles los componentes HTML/CSS de un UI kit: botones, modales, inputs, etc.
- La idea es reutilizar piezas visuales compatibles con JSP server-side, no cambiar de paradigma.

## Beans expuestos a la vista

Los objetos usados desde JSP deberian cumplir el formato Bean:

- propiedades privadas
- getters/setters con nombres estandar
- constructor publico sin argumentos
- `Serializable`

## Taglibs principales

```jsp
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
```

## Directivas y primitivas JSP

### page

- Configura aspectos generales de la pagina JSP.
- Puede definir headers, encoding y si se usaran sesiones.

### include estatico

- Inserta codigo al momento de traduccion a servlet.
- Sirve para componentes estaticos o fragmentos comunes sin parametros.

### action include

- Permite invocar componentes dinamicos y pasar parametros.
- Los parametros se reciben como `param.nombre`.
- A diferencia del include estatico, se resuelve en tiempo de ejecucion.

## Familias de JSTL

- `core`: control de flujo, variables y salida
- `i18n`: internacionalizacion y formateo
- `functions`: utilidades para colecciones y strings

## Tags JSTL core

### c:out — Salida sanitizada (obligatorio)
```jsp
<c:out value="${user.name}"/>
```
Previene XSS. Nunca usar `${...}` directamente para valores dinamicos. (ver: [[xss-prevencion]])

### c:url — URLs relativas (obligatorio)
```jsp
<c:url value="/users/${user.id}"/>
```
Se encarga de resolver la posicion relativa correctamente al deployar. Nunca usar `${pageContext.request.contextPath}` ni paths hardcodeados.

### c:if — Condicionales
```jsp
<c:if test="${not empty users}">...</c:if>
```

### c:forEach — Iteracion
```jsp
<c:forEach var="user" items="${users}">...</c:forEach>
```

## Spring form tags

Para binding directo entre formularios y form beans:

```jsp
<form:form modelAttribute="userForm" action="${loginUrl}" method="POST">
    <form:input path="email"/>
    <form:errors path="email" cssClass="error"/>
    <form:password path="password"/>
</form:form>
```

- `path` mapea automaticamente al campo del form bean
- `<form:errors>` muestra errores de validacion por campo
- Soporta customizacion de elemento HTML, clase CSS, estilo inline

## Spring Security taglib

Para mostrar u ocultar partes de la UI segun sesion o rol, preferir la taglib de Spring Security antes que `if` manuales con magic strings en el JSP.

- Evita tabs visibles pero inaccesibles para usuarios anonimos.
- Mantiene la decision de autorizacion alineada con la configuracion central.
- Complementa, pero no reemplaza, las reglas de backend.

## Ubicacion de JSPs

Los JSPs deben estar dentro de `WEB-INF/jsp/` para que sean privados (no accesibles directamente por URL). Organizar por controller:
```
WEB-INF/jsp/
  helloworld/
    index.jsp
  users/
    register.jsp
    profile.jsp
```

El `ViewResolver` de Spring agrega automaticamente el prefijo y sufijo.

## Spring message tag (i18n)

```jsp
<spring:message code="user.name.label"/>
```
Resuelve desde MessageSource. (ver: [[internacionalizacion]])

## Ver tambien
- [[comparacion-jsp-formularios-e-i18n]]
- [[auth-flows]]
- [[spring-web-mvc]]
- [[xss-prevencion]]
- [[internacionalizacion]]
- [[ux-flows]]
- [[validacion-formularios]]
- [[resumen-apuntes]]
