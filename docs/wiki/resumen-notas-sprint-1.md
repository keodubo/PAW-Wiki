---
titulo: Resumen de Notas — Sprint 1
tipo: fuente
fuentes: [fuente historica no incluida (raw/notas_sprint_1.txt)]
creado: 2026-04-11
actualizado: 2026-04-13
---

# Resumen de Notas — Sprint 1

Feedback recibido durante la revision del sprint 1. Incluye observaciones de los profesores (Alejo, Nico, Sotuyo) sobre el producto, la arquitectura y la gestion del proyecto.

## Modelo de Negocio y Actores

- **Masividad**: el profesor cuestiona el modelo centralizado donde solo los admins agregan restaurantes. Recomienda que los usuarios puedan incorporar restaurantes.
  - Si un usuario crea la pagina de un restaurant, el restaurant no tiene acceso al sistema de reservas.
  - Nico recomienda explorar un servicio tipo *Maitre* para la gestion de reservas.
  - Dualidad propuesta: un restaurant puede incorporarse (con sistema de reserva) O un usuario puede incorporar un restaurant (sin sistema de reserva inicialmente).
- **Segundo actor**: la definicion actual esta "medio medio". El mail debe dispararse ante acciones que no realiza el propio usuario (ej: "dejaron una review en tu restaurant"). (ver: [[mailing]])

## Paginacion y Filtros

- Las paginas de reviews tienen tamaño fijo (5). **El usuario deberia poder elegir cuantos elementos ver** (parametro `&pageSize=N`). (ver: [[persistencia-jdbc]])
  - Ejemplo: un `curl` que pide 500 reviews no deberia requerir 100 paginas.
- **Filtros deben ser ordenables**: poder ordenar por menor precio dentro de una cocina, invertir orden de puntajes (menor puntaje primero).
- **Queries y filtros no son compatibles**: buscar texto pierde el filtro activo y viceversa. Deben persistirse mutuamente.
- **Cambio de UX en filtros**: en vez de dropdowns, mostrar los primeros X con mas resultados y un boton "mostrar mas".
  - Ejemplo: `Argentina (52) / Mediterranea (35) / ... / Mostrar mas`

## UI/UX

- **Navbar en admin page**: comprimir para no perder funcionalidad en pantallas reducidas.
- **Reviews con texto vacio**: no deberian mostrarse las comillas si el comentario esta vacio.
- **Word-break en comentarios**: falta un `word-break` duro; una palabra larga sin breaks expande la ventana. (ver: [[buenas-practicas]])
- **Boton de favoritos**: no esta completamente a la derecha cuando se agranda la ventana (probable `max-width` faltante en el contenedor).
- **Errores custom**: los mensajes de error son default del navegador. Se prefieren errores customizados (puntualmente el de exceso de caracteres en comentario de review). (ver: [[validacion-formularios]])
- **Call to action en mail**: falta un CTA en el correo. (ver: [[mailing]])
- **Busqueda sin indicador**: si busco "asd", la pagina no muestra "Resultados para 'asd'".
- **Formulario de restaurant**: usar combobox sin escritura para barrios (mejor UX).
- **Separar formulario y listado**: el form de agregar restaurant y el menu de todos los restaurantes deberian estar en paginas distintas.
- **Boton cancelar verde**: el cancelar no deberia ser verde (color asociado a accion positiva).

## Validacion y Seguridad

- **Validacion de imagen server-side**: el chequeo de imagen debe hacerse tambien en el backend, no solo en el cliente. (ver: [[validacion-formularios]], [[manejo-imagenes]])
- **Endpoint `/api/autocomplete`**: a Sotuyo no le gusta para la entrega final. Evaluar alternativa. (ver: [[buenas-practicas]])

## Identificadores

- **Slug con sufijo numerico**: no les gusta el patron `-2` (cantidad de duplicados con mismo nombre). Alternativa propuesta: `nombre-id`.

## Gestion de Proyecto (Plane)

- Las tareas estan bien planteadas pero **no estan en un ciclo**.
- Las tareas **no estan puntuadas**. (ver: [[scrum-metodologia]])
- Agregar un ciclo nuevo con las tareas ya completadas.
- Agregar una planning para el siguiente sprint (tareas planificadas).

## Direccion Estrategica para Sprint 2

El profesor sugiere profundizar en uno de dos ejes:

### Eje Transaccional
- **Reservas**: el restaurant debe poder tomar control. Una persona puede manejar mas de un local (reutilizar admin page como multi-restaurant admin).
  - Definir: cuantas mesas, tamaño de cada mesa, horarios disponibles.
- **Menu**: categorias → platos en cada categoria → nombre + precio. En futuro: promociones.

### Eje Social
- Que el restaurant pueda tomar control de su pagina, hacer promos, mostrar el menu.
- Nuevas features: ver quien fue a donde, historial de pedidos.

## Ver tambien
- [[resumen-notas]]
- [[resumen-correcciones]]
- [[resumen-enunciado]]
- [[criterios-evaluacion]]
- [[scrum-metodologia]]
- [[validacion-formularios]]
- [[persistencia-jdbc]]
- [[mailing]]
- [[resumen-notas-sprint-2]]
- [[buenas-practicas]]
- [[manejo-imagenes]]
