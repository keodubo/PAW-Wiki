---
titulo: PAW Unidad 5 - Unit Testing
tipo: fuente
fuentes: [raw/apuntes.txt, raw/pdfs/PAW - Apuntes.pdf]
creado: 2026-05-11
actualizado: 2026-05-11
---

# PAW Unidad 5 - Unit Testing

Esta unidad fija el criterio de testeo de la materia. La regla practica es escribir tests deterministas, pequenos y orientados a comportamiento observable, no a detalles internos.

## Estructura de test

- Setup: preparar datos y dependencias.
- Ejercitacion: idealmente una llamada principal.
- Validacion: pocas postcondiciones relevantes.
- El apunte usa como orientacion mantener pocos asserts y una intencion clara por test.

## Dobles de prueba

- Stub: implementacion minima o vacia para permitir ejecutar el caso.
- Mock: doble programable para controlar respuestas.
- Fake: implementacion limitada pero funcional, util cuando representa estado observable.
- En este wiki, para PAW, se privilegia comportamiento/estado frente a verificaciones internas.

## Servicios

- Los servicios pueden testearse con DAOs dobles cuando el foco es regla de negocio.
- El test no deberia depender de SQL si la unidad bajo prueba es service.
- Las validaciones deben mirar resultado, excepcion o efecto observable.
- Evitar acoplarse a "se llamo tal metodo interno" como criterio principal.

## Persistencia

- Mockear JDBC completo no es razonable.
- HSQLDB permite una base embebida reproducible para DAOs.
- Spring Test levanta contexto y scripts de schema/datos.
- `JdbcTestUtils.deleteFromTables` y `countRowsInTable` ayudan a preparar y validar estado.
- Los scripts deben cargarse por classpath, no por paths absolutos.

## Mockito

- `mock()` devuelve valores default si no se programa respuesta.
- `@Mock`, `@InjectMocks` y runners reducen setup.
- La regla local del wiki/profesor es no validar implementacion con `verify()` como patron de cierre.
- Si se necesita observar salida, preferir estado, retorno, excepcion o fake registrador.

## Para TP1

- Testear una conducta por caso.
- No testear metodos privados.
- No hacer tests que solo reflejan implementacion.
- Separar tests de services y DAOs.
- Mantener fixtures simples y deterministas.

## Ver tambien

- [[resumen-apuntes]]
- [[testing-unitario]]
- [[comparacion-testing-servicios-y-daos]]
- [[persistencia-jdbc]]
- [[criterios-evaluacion]]
- [[paw-unidad-04-spring-mvc-capas-jdbc]]
- [[paw-unidad-06-formularios-i18n]]
