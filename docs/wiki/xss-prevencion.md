---
titulo: Prevencion de XSS y Sanitizacion
tipo: concepto
fuentes: [raw/correcciones.md]
creado: 2026-04-09
actualizado: 2026-04-17
---

# Prevencion de XSS y Sanitizacion

La prevencion de XSS es uno de los puntos mas evaluados. Multiples grupos fueron marcados con **"error grave"** por no escapar valores dinamicos en JSPs.

## Regla absoluta

**Todo valor dinamico en un JSP debe imprimirse con `<c:out>`**. Nunca usar `${...}` directamente.

```jsp
<!-- MAL — vulnerable a XSS -->
<p>${user.name}</p>

<!-- BIEN — escapado -->
<p><c:out value="${user.name}"/></p>
```

## Errores detectados en correcciones

- Usar `$(...)` en vez de `<c:out>` para imprimir valores
- Campos de texto libre del usuario sin escapar (aunque tengan patterns en el form, si el pattern falla queda abierto)
- Busquedas con XSS posible en el searchbar
- Intentar XSS causa 500 en vez de ser bloqueado limpiamente

## Sanitizacion de LIKE

Los metacaracteres `%` y `_` en queries LIKE deben pre-escaparse. Mencionado 2+ veces como error.

```java
// Escapar antes de pasar al DAO
String sanitized = input
    .replace("%", "\\%")
    .replace("_", "\\_");
```

Grupos que escaparon correctamente fueron **elogiados explicitamente**.

## Ver tambien
- [[comparacion-seguridad-web]]
- [[comparacion-jsp-formularios-e-i18n]]
- [[buenas-practicas]]
- [[internacionalizacion]]
- [[jsp-jstl]]
- [[spring-security]]
- [[persistencia-jdbc]]
- [[resumen-correcciones]]
- [[remediacion-violaciones-paw]]

- [[plan-implementacion-reservas]]
