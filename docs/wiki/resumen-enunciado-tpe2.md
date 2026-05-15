---
titulo: Resumen del Enunciado TPE2
tipo: fuente
fuentes: [raw/enunciado_tpe2.txt, raw/pdfs/Enunciado_TPE2.pdf]
creado: 2026-05-11
actualizado: 2026-05-15
---

# Resumen del Enunciado TPE2

Especificacion oficial del Trabajo Practico Especial - Segunda Entrega de PAW.

Nota de fuente: el `.txt` fue extraido desde el PDF original `Enunciado_TPE2.pdf` para facilitar busqueda e ingesta. El PDF original queda preservado en `docs/raw/pdfs/`.

## Objetivo

Crear una aplicacion web con Java 21, metodologia Scrum y despliegue productivo en un servidor web.

## Cambio central respecto de TPE1

La segunda entrega exige reemplazar la persistencia JDBC de la iteracion anterior por **JPA + Hibernate**, sin perdida de funcionalidad ni de informacion en el servidor.

Esto convierte a TPE2 en una migracion de persistencia, no en un cambio de producto desde cero:

- conservar lo que ya funcionaba en TPE1;
- incorporar completo el feedback de la catedra;
- migrar DAOs, entidades, mappings y transacciones hacia JPA/Hibernate;
- ejecutar migraciones de base de datos sin perder datos existentes;
- seguir agregando y puliendo funcionalidades de valor para el usuario final.

## Stack obligatorio

- Java 21.
- Arquitectura segun Domain Driven Design.
- Inyeccion de dependencias con Spring.
- Tests unitarios con cobertura suficiente y buena calidad.
- Capa web con Spring Web MVC, MVC y Front Controller.
- Vistas JSP con JSTL, sin codigo Java embebido.
- Persistencia con JPA y Hibernate.
- Maven para administracion y construccion.
- PostgreSQL como base de datos.
- Git provisto por la catedra para versionado.

## Alcance funcional

La catedra no fija una lista universal de features para todos los grupos. El alcance puntual se negocia con cada equipo, considerando:

- valor para el usuario final;
- complejidad;
- continuidad de los flujos ya entregados;
- nuevos flujos core del proyecto;
- correcciones de feedback de la entrega anterior.

El enunciado eleva la expectativa de calidad respecto de TPE1: no alcanza con repetir la funcionalidad previa si la aplicacion no mejora, no corrige feedback o no sostiene bien los flujos existentes.

## Iteraciones y fechas

Las iteraciones son de 2 semanas. La demo de fin de iteracion es obligatoria aunque no todos los cierres sean evaluados formalmente.

| Fecha | Evento | Notas |
|-------|--------|-------|
| 2026-05-13 | Cierre de iteracion | Demo obligatoria |
| 2026-05-27 | Cierre de iteracion | Demo obligatoria |
| 2026-06-08 19:00 | Entrega final TPE2 | Email a la catedra con hash del commit deployado y demo ese mismo dia en horario de clase |

Cada iteracion debe incluir:

1. Incorporacion completa del feedback de la catedra de la entrega anterior.
2. Iteracion de flujos ya entregados, conforme se completan y complejizan.
3. Nuevos flujos core del proyecto, no solo ABMs.

## Produccion y migraciones de datos

La aplicacion debe poder ponerse en produccion desde la etapa mas temprana posible.

La responsabilidad sobre la base de datos recae en el equipo:

- hacer todas las migraciones necesarias iteracion a iteracion;
- garantizar que no se pierda informacion;
- mantener PostgreSQL funcionando con permisos adecuados;
- permitir que el deploy genere las tablas necesarias para usar la aplicacion.

En TPE2 esto es especialmente critico porque la migracion JDBC -> JPA/Hibernate puede romper schema, relaciones, ids, constraints, datos precargados o queries existentes.

## Entrega

- Todo el codigo debe estar en el repositorio Git provisto por la catedra.
- No incluir archivos innecesarios: configuraciones de IDE, dependencias vendoreadas o archivos generados por Maven.
- `mvn clean package` debe producir un `.war` usable.
- El `.war` debe funcionar al deployarse en un Application Container con una base PostgreSQL disponible.
- La aplicacion debe estar funcionando correctamente en el servidor provisto por la catedra.
- Si hay autenticacion, `README.md` debe indicar credenciales para al menos un usuario por cada nivel de acceso.
- La entrega se hace por email a la catedra con el hash del commit deployado, a mas tardar el 2026-06-08 a las 19:00.

## Evaluacion

La catedra evalua cumplimiento de requerimientos, funcionamiento, usabilidad, diseno de objetos, codigo, commits y contribuciones individuales.

| Criterio | Penalizacion maxima |
|----------|---------------------|
| Incumplimiento de Scrum, Pivotal Tracker, priorizacion o valor por iteracion | hasta 2 puntos |
| Unit testing insuficiente o de mala calidad | hasta 1 punto |
| Forma de entrega no acorde al enunciado | hasta 1 punto |
| Fallos de funcionalidad y usabilidad | hasta 4 puntos |
| Problemas de arquitectura, DDD, separacion de capas, responsabilidades o buenas practicas | hasta 4 puntos |
| Perdidas de informacion | 2 puntos |
| Feedback anterior no corregido | hasta 1 punto por cada correccion no resuelta |

## Errores no recuperables

Se reprueba sin posibilidad de recuperar si ocurre cualquiera de estos casos:

- no realizar una demo de fin de iteracion;
- no tener cambios para presentar en una demo;
- no cumplir los requerimientos tecnicos establecidos;
- presentar inconsistencias en el hash del commit entregado.

## Bibliografia recomendada

- *Effective Java, 3rd Edition* - Joshua Bloch.
- *Domain Driven Design* - Eric Evans.

## Lectura operativa

Para implementar o auditar TPE2, tratar esta pagina como el contrato oficial de entrega y cruzarla con:

- [[hibernate-jpa]] para migracion de persistencia.
- [[transactional]] para limites transaccionales.
- [[modelo-capas]] para preservar separacion por capas.
- [[testing-unitario]] para calidad de tests.
- [[criterios-evaluacion]] para priorizar riesgos.
- [[calendario-entregas]] para fechas de demo y entrega.
- [[resumen-correcciones-tp2]] para patrones reales de devolucion sobre migraciones TP2.

## Ver tambien

- [[hibernate-jpa]]
- [[transactional]]
- [[modelo-capas]]
- [[criterios-evaluacion]]
- [[calendario-entregas]]
- [[tp1-vs-tpe2-final]]
- [[resumen-enunciado]]
- [[resumen-correcciones-tp2]]
