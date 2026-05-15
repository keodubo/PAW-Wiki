---
titulo: Hibernate, JPA y ORM
tipo: concepto
fuentes: [raw/apuntes.txt, raw/PAW - clase 7 (TP2).pdf, raw/PAW - clase 8 (TP2).pdf, raw/correcciones_tp2.md]
creado: 2026-04-13
actualizado: 2026-05-15
---

# Hibernate, JPA y ORM

Esta pagina resume la migracion a **Hibernate/JPA**. Es material relevante para **TP2**, pero **fuera del foco de TP1** salvo que el usuario pida explicitamente preparar esa migracion.

Nota: las clases TP2 traen ejemplos con versiones y propiedades antiguas. No tomar esas versiones como canon; usar el checkout/enunciado vigente y conservar solo el criterio arquitectonico.

## ORM, JPA e Hibernate

- ORM busca interactuar con la base relacional usando objetos del lenguaje en vez de SQL directo.
- **JPA** es la especificacion estandar de persistencia para Java.
- **Hibernate** es un proveedor que implementa JPA.
- La ganancia principal es simplicidad y velocidad de desarrollo.
- El costo principal es perder parte del control fino sobre queries, timings y performance.

## Migracion TP2 desde JDBC

- La migracion no es "agregar Hibernate encima": cambia el contrato de persistencia de SQL explicito hacia entidades JPA managed por `EntityManager`.
- `spring-orm` conecta Spring con JPA y el transaction manager pasa a ser `JpaTransactionManager`.
- `LocalContainerEntityManagerFactoryBean` declara paquetes de entidades, `DataSource`, vendor adapter y propiedades del proveedor.
- Si el proyecto ya tiene schema y datos, revisar con cuidado cualquier autogeneracion/actualizacion de schema antes de usarla en una base real.
- Los DAOs JPA usan `@PersistenceContext` y `EntityManager`; no exponer `EntityManager` fuera de persistence.
- Si conviven DAO JDBC y DAO Hibernate para el mismo contrato, evitar que Spring registre dos candidatos para una misma interfaz.

## Impedancia objeto-relacional

- El mundo objeto y el mundo relacional no coinciden bien.
- En objetos son naturales la herencia, las colecciones y ciertos usos de `null`.
- En SQL esos conceptos existen de otra forma o con semantica distinta.
- Hibernate reduce trabajo repetitivo, pero obliga a conocer bien sus convenciones y limites.

## Configuracion basica

- Hay que agregar dependencias de ORM/Hibernate en el pom padre y en los modulos necesarios.
- En `WebConfig` se configuran properties del proveedor.
- Regla fuerte de clase: no usar `create` para autogeneracion del esquema en una base existente; `update` hace un best effort mas seguro.
- Las properties que imprimen SQL en `STDOUT` sirven para desarrollo, no para produccion.
- El `transactionManager` tambien debe adaptarse a JPA/Hibernate.

## EntityManager y entidades

- `@PersistenceContext` inyecta el `EntityManager`.
- `EntityManager` pasa a ser la interfaz principal para persistir, actualizar, borrar y consultar entidades.
- Las clases de dominio se mapean con anotaciones como:
  - `@Entity`
  - `@Table`
  - `@Id`
  - `@GeneratedValue`
  - `@SequenceGenerator`
- Hibernate necesita constructor default para poder instanciar entidades.
- `EnumType.STRING` es la opcion mas legible para enums, aunque exige cuidar el ancho de columna.

## Relaciones y fetch

- Relaciones tipicas: `@ManyToOne`, `@OneToMany`, `@OneToOne`, `@ManyToMany`.
- `mappedBy` indica que lado es el duenio de la relacion.
- `orphanRemoval` elimina hijos que dejan de estar asociados al padre.
- `FetchType.EAGER` trae relaciones inmediatamente y puede disparar sobrecarga.
- `FetchType.LAZY` difiere la carga hasta acceder al atributo.
- `optional = false/true` documenta si una relacion es requerida o nullable.
- `EnumType.STRING` favorece legibilidad, pero exige cuidar que los nombres entren en el ancho de columna.
- Cada cambio de mapping debe auditar el SQL generado; el hecho de que el caso funcione localmente no prueba que escale.

## Alertas desde correcciones TP2

- No mapear relaciones como ids pelados si el dominio pide una relacion JPA real. Las devoluciones TP2 lo marcan como desaprovechar Hibernate y seguir pensando en columnas JDBC.
- Evitar `FetchType.EAGER` por defecto, incluso en relaciones que hoy parecen chicas. Listados paginados pueden multiplicar queries o traer grafos innecesarios.
- Para colecciones `toMany`, asumir crecimiento y disenar paginacion o queries especificas. No exponer getters de listas grandes si despues se materializan por accidente.
- No acceder relaciones lazy desde `@Async` sin materializarlas previamente dentro de la transaccion o sin query con fetch controlado.
- Usar modelo 1+1 para paginar entidades con relaciones: primero ids, despues carga del grafo necesario. Ver [[resumen-correcciones-tp2]] para ejemplos recurrentes.

## Contexto de persistencia

- Dentro de una transaccion, Hibernate mantiene entidades **managed**.
- Si cambia una entidad managed, Hibernate puede detectar el cambio y sincronizarlo al `flush()` o `commit`.
- Esa comodidad introduce menos control explicito sobre cuando salen queries.
- Si una entidad sale de ese contexto y despues se reutiliza en otra transaccion, ya no esta automaticamente asociada.
- Ahi aparecen problemas tipicos como lazy loading fuera de sesion.
- El dirty checking puede persistir cambios sin una llamada explicita de update. Eso es potente, pero acopla el comportamiento al contexto JPA.
- Operaciones simples sobre colecciones pueden generar deletes/inserts en tablas de relacion. Revisar logs SQL antes de asumir costo bajo.

## Paginacion y sesiones

- Para paginar entidades complejas, la clase recomienda estrategia de dos pasos:
  - primero obtener ids de la pagina
  - despues cargar esas entidades con sus relaciones
- Tambien aparece la idea de mantener sesion abierta mas tiempo via filtro web, pero eso extiende vida de la conexion y reduce control.
- Si se usa, el mapping del filtro deberia quedar **despues** de Spring Security.

## Herencia en JPA

- La herencia puede mapearse con distintas estrategias:
  - `SINGLE_TABLE`
  - `JOINED`
  - `TABLE_PER_CLASS`
- Ninguna es "la correcta" universalmente; todas traen trade-offs.
- Si la clase base es abstracta, `@MappedSuperclass` permite heredar atributos persistentes sin tabla propia.

## Ver tambien

- [[tp1-vs-tpe2-final]]
- [[resumen-clases-paw-2026]]
- [[persistencia-jdbc]]
- [[transactional]]
- [[resumen-apuntes]]
- [[resumen-correcciones-tp2]]
