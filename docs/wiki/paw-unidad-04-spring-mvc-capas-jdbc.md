---
titulo: PAW Unidad 4 - Spring MVC, Capas y JDBC
tipo: fuente
fuentes: [raw/apuntes.txt, raw/pdfs/PAW - Apuntes.pdf]
creado: 2026-05-11
actualizado: 2026-05-11
---

# PAW Unidad 4 - Spring MVC, Capas y JDBC

Esta unidad es el nucleo tecnico del TP1: Maven multi-modulo, Spring MVC server-side, inyeccion de dependencias, separacion por capas y persistencia JDBC.

## Maven y modulos

- Maven no es solo gestor de dependencias: define ciclo de vida, modulos, packaging y scopes.
- El pom padre centraliza versiones y `dependencyManagement`.
- Los modulos hijos declaran lo que usan; heredar version no implica agregar dependencia.
- `SNAPSHOT` indica version mutable de trabajo.
- Los scopes ayudan a proteger capas: una app puede compilar contra interfaces y cargar implementaciones en runtime.

## Spring MVC

- `web.xml` registra el `DispatcherServlet` como Front Controller.
- `WebConfig` habilita MVC, component scan y resolucion de vistas.
- Los controllers mapean requests y devuelven vista + modelo.
- `ViewResolver` desacopla nombre logico de path fisico.
- JSPs bajo `WEB-INF/jsp/` no quedan expuestas por URL directa.

## Inyeccion de dependencias

- `@Controller`, `@Service` y `@Repository` describen roles de beans.
- `@Autowired` conecta por contrato, idealmente interfaces.
- Si hay mas de una implementacion, se resuelve con `@Qualifier`, `@Primary` o una decision de configuracion explicita.
- La app no deberia usar `new ServiceImpl()` desde controllers.

## Modelo de capas

- Webapp adapta HTTP y vistas.
- Services concentran reglas de negocio y casos de uso.
- Persistence implementa acceso a datos.
- Models contiene entidades/objetos de dominio compartidos.
- La capa superior habla con contratos de la inferior; no saltea capas.

## Persistencia JDBC

- `JdbcTemplate` reduce boilerplate de JDBC.
- `RowMapper` transforma filas en objetos.
- `SimpleJdbcInsert` simplifica inserts y recupero de IDs.
- El schema no deberia estar escondido en constructores de DAO.
- Los DAOs no deben filtrar tipos `java.sql` hacia services.

## Para TP1

- Controller fino, service con caso de uso, DAO con SQL.
- Configuracion sensible fuera del codigo fuente cuando sea posible.
- Revisar scopes Maven para que webapp no compile contra implementaciones.
- Evitar joins resueltos en Java si la base puede resolverlos correctamente.

## Ver tambien

- [[resumen-apuntes]]
- [[spring-web-mvc]]
- [[modelo-capas]]
- [[inyeccion-dependencias]]
- [[configuracion-maven]]
- [[persistencia-jdbc]]
- [[n-plus-1-joins-java]]
- [[paw-unidad-03-frontend-jsp-jstl]]
- [[paw-unidad-05-unit-testing]]
