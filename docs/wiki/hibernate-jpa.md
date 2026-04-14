---
titulo: Hibernate, JPA y ORM
tipo: concepto
fuentes: [raw/apuntes.txt]
creado: 2026-04-13
actualizado: 2026-04-13
---

# Hibernate, JPA y ORM

Esta pagina resume la **unidad 9**. Es material relevante para TPE2/final, pero **fuera del foco de TP1**.

## ORM, JPA e Hibernate

- ORM busca interactuar con la base relacional usando objetos del lenguaje en vez de SQL directo.
- **JPA** es la especificacion estandar de persistencia para Java.
- **Hibernate** es un proveedor que implementa JPA.
- La ganancia principal es simplicidad y velocidad de desarrollo.
- El costo principal es perder parte del control fino sobre queries, timings y performance.

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

## Contexto de persistencia

- Dentro de una transaccion, Hibernate mantiene entidades **managed**.
- Si cambia una entidad managed, Hibernate puede detectar el cambio y sincronizarlo al `flush()` o `commit`.
- Esa comodidad introduce menos control explicito sobre cuando salen queries.
- Si una entidad sale de ese contexto y despues se reutiliza en otra transaccion, ya no esta automaticamente asociada.
- Ahi aparecen problemas tipicos como lazy loading fuera de sesion.

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
- [[persistencia-jdbc]]
- [[transactional]]
- [[resumen-apuntes]]
