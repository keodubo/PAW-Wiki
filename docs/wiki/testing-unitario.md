---
titulo: Testing Unitario — Criterios de Evaluacion
tipo: concepto
fuentes: [raw/correcciones.md, raw/enunciado.txt, raw/apuntes.txt, raw/notas.txt]
creado: 2026-04-09
actualizado: 2026-04-13
---

# Testing Unitario — Criterios de Evaluacion

La cobertura de tests es exigida estrictamente. Su ausencia o baja calidad se penaliza con hasta **1 punto** segun el enunciado (ver: [[criterios-evaluacion]]).

## Errores detectados

### Mockito — uso incorrecto (error grave)
- Usar `verify()` y `never()` para validar **implementacion** (orden y llamados exactos)
- Lo correcto: validar **estado final** con asserts sobre resultados
- Tests que verifican cambios de estado en objetos mockeados no son unitarios

### Tests de persistencia
- Nunca validan estado de la DB (hacen execute sin count/assert posterior)
- Usan DAOs para setear precondiciones en vez de scripts SQL
- Falta `@Rollback` para independencia transaccional entre tests
- Usar HSQLDB como esquema liviano para tests

### Cobertura insuficiente
- Metodos con logica de negocio grande sin tests
- Servicios enteros con cobertura nula (a veces comentada)
- No testear casos no felices (edge cases, errores)

### Tests no unitarios
- Tests que llaman multiples clases a la vez
- Dependencias entre tests (uno depende del estado dejado por otro)
- Usar JUnit 3 en vez de JUnit 5

## Patron correcto

**Tests de servicio:**
- Mockear DAOs
- Verificar con asserts sobre el resultado retornado
- Cubrir caminos felices Y no felices

**Tests de persistencia:**
- Precondiciones via scripts SQL (no DAOs)
- `@Rollback` en cada test
- Validar estado de DB post-operacion (count, select, assert)
- HSQLDB como motor de test

## Fundamentos de los apuntes

### Estructura de un test
1. **Setup**: precondiciones (no usar el objeto siendo testeado)
2. **Ejercitar**: una unica llamada al metodo
3. **Validaciones**: postcondiciones (maximo 4 asserts)

### JUnit y clases concretas

- En apuntes se mencionan JUnit y TestNG; la cursada usa JUnit.
- El test puede instanciar clases concretas; no hace falta limitarse a la interfaz publica si eso esconde comportamiento importante.

### HSQLDB para tests de persistencia
- Base de datos embebida en Java, persistencia en memoria
- Garantiza condiciones iniciales consistentes sin importar cuantas veces se corra
- En el ejemplo inicial del curso, el schema arranca en `src/test/resources/schema.sql`
- Luego la idea evoluciona a compartir el esquema desde `src/main/resources` del modulo `persistence`
- En este repo, esa version final conviene documentarla como `schema-base.sql` + script dialectal (`schema-postgres.sql` / `schema-hsqldb.sql`)
- Popular BD inicial via `@Sql` o `JdbcTestUtils`

### Configuracion de Spring Test
- `@RunWith(SpringJUnit4ClassRunner.class)`
- `@ContextConfiguration(classes = TestConfig.class)`
- `@Autowired` para inyectar DataSource y DAOs

### Mockito para tests de servicios
- `@Mock` para crear mocks (reemplaza `mock(Clase.class)`)
- `@InjectMocks` busca automaticamente mocks para inyectar
- `when(...).thenReturn(...)` para programar respuestas
- `MockitoJUnitRunner` como runner (no necesita Spring)
- El objeto testeado NO se manipula en pre/postcondiciones, solo en ejercitacion

### Dobles de prueba
- **stub**: implementacion vacia o minima para satisfacer una dependencia
- **mock**: implementacion programable
- **fake**: implementacion limitada pero funcional

En apuntes tambien se explica que `mock()` crea un "nice mock":

- si el retorno es nullable, devuelve `null`
- si es numerico, devuelve `0`
- si es booleano, devuelve `false`

## Detalles practicos de las clases

### Schema compartido entre tests y app
- El esquema compartido vive en `src/main/resources` del modulo persistence (no en test/resources)
- En el estado final del repo se carga como `schema-base.sql` + script dialectal segun el motor
- Tanto `TestConfig` como `WebConfig` usan `DataSourceInitializer` + `DatabasePopulator` para cargar el mismo esquema final
- Esto evita tener schemas duplicados que se desincronizan

### JdbcTestUtils para independencia entre tests
- `JdbcTestUtils.countRowsInTable(jdbcTemplate, "users")` para validar estado de la BD post-operacion
- `JdbcTestUtils.deleteFromTables(jdbcTemplate, "users")` en `@Before` para limpiar la tabla antes de cada test
- Garantiza que los tests sean independientes y que el orden no importe

### verify() esta prohibido
- El profesor advirtio explicitamente que usar `verify()` de Mockito sera penalizado
- `verify()` valida implementacion (que se llamo tal metodo N veces), no estado final
- Lo correcto: asserts sobre el resultado retornado por el metodo testeado
- Los controllers no tienen logica de negocio, por lo tanto no necesitan tests (si se implementan correctamente)

### Tests que solo buscan "que no falle"

- Hay casos donde la clase muestra un `try/catch` y el assert relevante es que no se lance excepcion.
- Eso sigue siendo valido siempre que el objetivo del test este claro y no se convierta en una excusa para no validar nada importante.

## Ver tambien
- [[tp1-vs-tpe2-final]]
- [[comparacion-testing-servicios-y-daos]]
- [[transactional]]
- [[persistencia-jdbc]]
- [[buenas-practicas]]
- [[resumen-correcciones]]
- [[criterios-evaluacion]]
- [[resumen-enunciado]]
- [[resumen-apuntes]]
- [[resumen-notas]]
- [[remediacion-violaciones-paw]]
