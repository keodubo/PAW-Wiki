---
titulo: PAW Unidad 9 - Hibernate, JPA y ORM
tipo: fuente
fuentes: [raw/apuntes.txt, raw/pdfs/PAW - Apuntes.pdf]
creado: 2026-05-11
actualizado: 2026-05-11
---

# PAW Unidad 9 - Hibernate, JPA y ORM

Esta unidad es la base conceptual de TP2. El cambio no es "usar otra libreria de SQL": es migrar desde JDBC explicito hacia entidades gestionadas por JPA/Hibernate y un contexto de persistencia.

## Cambio de paradigma

- JDBC obliga a escribir SQL, ejecutar queries y mapear filas a objetos.
- ORM permite trabajar con objetos y delegar al proveedor la traduccion a SQL.
- JPA define la especificacion estandar.
- Hibernate implementa esa especificacion como proveedor.
- Se gana velocidad y menos boilerplate.
- Se pierde control fino sobre queries, tiempos de ejecucion y algunos costos.

## Impedancia objeto-relacional

- Objetos y tablas no modelan igual.
- En objetos hay herencia, referencias, colecciones y `null`.
- En relacional hay tablas, claves, joins, constraints y `NULL` con semantica propia.
- Hibernate no elimina esta diferencia: la encapsula con convenciones.
- El desarrollador debe conocer esas convenciones para no confundir comodidad con correccion.

## Configuracion Spring/JPA

- Se agregan dependencias de Spring ORM y Hibernate en los modulos correctos.
- La configuracion define `EntityManagerFactory`, `DataSource`, paquetes de entidades y properties del proveedor.
- El transaction manager debe cambiar a uno compatible con JPA.
- `create` para schema es peligroso porque puede dropear y recrear datos.
- `update` no es migrador confiable: hace best effort, pero no cubre todos los cambios.
- Mostrar SQL por stdout sirve en desarrollo, no en produccion.
- Las versiones concretas del PDF son historicas y deben contrastarse con el checkout actual.

## EntityManager

- `@PersistenceContext` inyecta un `EntityManager` gestionado.
- `EntityManager` persiste, busca, elimina y consulta entidades.
- El DAO sigue existiendo como `@Repository`.
- Services no deberian conocer `EntityManager`.
- Queries JPQL hablan de entidades y atributos, no directamente de tablas y columnas.

## Entidades y columnas

- `@Entity` marca una clase persistente.
- `@Table` corrige el nombre de tabla cuando el default no sirve.
- `@Id` marca clave primaria.
- `@GeneratedValue` define generacion de IDs.
- `@SequenceGenerator` permite usar secuencias existentes.
- `@Column` explicita nombres, largos y restricciones.
- Hibernate necesita constructor default para instanciar por reflection.
- Cambiar una anotacion no garantiza que el schema existente quede actualizado.

## Relaciones

- `@ManyToOne` aparece cuando muchas entidades apuntan a una.
- `@OneToMany` suele ser el lado inverso de una FK en la tabla hija.
- `mappedBy` indica que ese lado no es duenio de la relacion.
- `optional` documenta si la relacion puede faltar.
- `orphanRemoval` elimina hijos que dejan de estar asociados.
- `FetchType.EAGER` debe ser excepcional porque puede traer demasiados datos.
- `FetchType.LAZY` es mas eficiente, pero exige sesion/contexto activo.

## Contexto de persistencia

- Una entidad obtenida dentro de una transaccion activa queda managed.
- Hibernate observa cambios sobre entidades managed.
- Al `flush()` o commit, sincroniza cambios con la base.
- Esto se conoce como dirty checking.
- Es potente porque reduce llamadas explicitas a update.
- Es riesgoso porque puede persistir cambios sin una llamada visible.
- Si una entidad esta detached, Hibernate no la sincroniza automaticamente.
- Acceder a una relacion lazy fuera de sesion puede fallar.

## Paginacion

- Con joins, `LIMIT` sobre filas SQL no equivale siempre a cantidad de entidades.
- La estrategia robusta es paginar IDs primero.
- Luego se cargan las entidades de esa pagina con sus relaciones necesarias.
- Esto evita paginas incompletas o resultados duplicados por multiplicacion de filas.

## Sesion abierta en vista

- Un filtro puede extender el contexto de persistencia hasta renderizar la vista.
- Debe mapearse despues de Spring Security si se usa.
- Ventaja: evita algunos errores de lazy loading en vista.
- Costo: mantiene conexion mas tiempo y permite queries durante rendering.
- Para una app limpia, conviene cargar lo necesario en service/DAO y no depender de magia en JSP.

## Herencia

- `@MappedSuperclass` sirve para una base abstracta que aporta campos sin tabla propia.
- `SINGLE_TABLE` usa una tabla y discriminador; lee rapido, pero puede tener muchas columnas null.
- `JOINED` normaliza en varias tablas; requiere joins.
- `TABLE_PER_CLASS` duplica campos heredados por subclase; complica consultas polimorficas.
- La eleccion depende del dominio y de las queries esperadas.

## Riesgos a auditar en TP2

- DAOs JDBC y JPA compitiendo por la misma interfaz.
- `EAGER` usado por comodidad.
- `show_sql` activo en produccion.
- Services recibiendo o exponiendo `EntityManager`.
- Entidades detached tratadas como managed.
- Paginacion sobre joins sin estrategia de IDs.
- Schema auto-update usado como reemplazo de migraciones.

## Ver tambien

- [[resumen-apuntes]]
- [[hibernate-jpa]]
- [[transactional]]
- [[persistencia-jdbc]]
- [[modelo-capas]]
- [[paw-unidad-08-aop-transacciones]]
- [[paw-unidad-10-single-page-applications]]
