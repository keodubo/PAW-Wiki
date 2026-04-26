---
titulo: UX Flows y Calidad de Interfaz
tipo: concepto
fuentes: [raw/correcciones_tp1.md, raw/apuntes.txt]
creado: 2026-04-13
actualizado: 2026-04-13
---

# UX Flows y Calidad de Interfaz

La catedra no corrige solo funcionalidad: tambien mira experiencia de uso, claridad de la interfaz, copy, estados vacios y si la UI comunica bien el problema que la app resuelve.

## Criterio base de diseno en PAW

- La materia prioriza una interfaz **usable** por sobre una interfaz solo "linda".
- CSS es la herramienta base de estilo; dominar cascada, especificidad y layout importa mas que sumar frameworks.
- Para ordenar componentes conviene `flex` o `grid`.
- Regla de clase: evitar estilos inline salvo excepciones puntuales.

## UI Kit vs Framework

- Para el TP1 no corresponde moverse a un stack SPA completo.
- Si tiene sentido tomar componentes HTML/CSS de un UI kit compatible con JSP.
- La decision correcta suele ser reutilizar patrones visuales server-side, no introducir complejidad de frontend innecesaria.

## Navegacion, layout y ruteo

- Todas las vistas relevantes deberian compartir favicon.
- El ancho de contenido deberia ser consistente.
- Las URLs deberian construirse con `<c:url>` para evitar dobles barras y problemas de proxy.
- Evitar `${pageContext.request.contextPath}` hardcodeado en JSP.

## Copy, i18n y presentacion

- No dejar plurales hardcodeados cuando el texto cambia por cantidad; usar `ChoiceFormat` o equivalente.
- Cuidar wording contextual correcto (`solicitudes` vs `invitaciones`, por ejemplo).
- Corregir encoding roto en tildes y caracteres especiales.
- Internacionalizar titulos, `alt` texts y mensajes visibles.
- No dejar que claves crudas como `email.duplicate` lleguen al usuario.

## Estados vacios y listados

- Los zero states deberian tener CTAs claros.
- Los sorts deben responder a un criterio real; `relevance` no puede ser humo.
- En listados extensos faltan filtros utiles como precio o rating.
- Conviene redondear reviews o puntajes a dos decimales para lectura humana.

## Flujos de cuenta y reserva con impacto UX

- El flujo de registro no deberia dejar al usuario bloqueado en una pantalla intermedia.
- Tras resetear contrasena puede ser razonable auto-loguear.
- Los usuarios anonimos no deberian ver tabs imposibles de usar.
- Si una reserva es cancelada por el owner, el usuario afectado deberia enterarse por mail.

## MVP, landing y comunicacion del problema

- El caso AirBnB de la unidad 1 se usa para mostrar que la landing tiene que explicar el problema y la solucion.
- CTA, jerarquia visual y texto deberian alinearse con el usuario principal de cada iteracion.
- Para el TP1, una home correcta no es solo una vista funcional: tambien tiene que ayudar a entender rapido que ofrece la aplicacion.

## Emails como parte de la UX

- Los mails tambien son interfaz: deberian tener HTML estandar, header y CTA tipo boton.
- Los correos funcionales deberian incluir la informacion necesaria, no solo un link desnudo.
- Un mail de cobro sin detalle ni CBU se considera flujo incompleto.

## Ver tambien

- [[jsp-jstl]]
- [[internacionalizacion]]
- [[mailing]]
- [[auth-flows]]
- [[single-page-applications]]
- [[validacion-formularios]]
- [[scrum-metodologia]]
- [[buenas-practicas]]
- [[resumen-correcciones]]
- [[resumen-notas-sprint-2]]
- [[resumen-apuntes]]
