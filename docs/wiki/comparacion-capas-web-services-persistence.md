---
titulo: Comparacion — Capas Web, Services y Persistence
tipo: comparacion
fuentes: [raw/correcciones_tp1.md, raw/apuntes.txt, raw/notas.txt, raw/enunciado.txt]
creado: 2026-04-09
actualizado: 2026-04-17
---

# Comparacion — Capas Web, Services y Persistence

La mayor parte de los errores graves del TPE1 vienen de romper los limites entre capas. Esta comparacion resume que pertenece a cada una.

## Vista general

| Capa | Pregunta que responde | Hace | No hace |
|------|------------------------|------|---------|
| Webapp | Que pidio el usuario y que vista devolvemos | Binding, validacion declarativa, routing, render | Logica de negocio, SQL, seguridad manual |
| Services | Cual es el caso de uso | Orquesta DAOs, transacciones, reglas de negocio | Conocer JSPs, templates o paths web |
| Persistence | Como se leen/escriben datos | JDBC, SQL, RowMappers, schema | Regla de negocio, decisiones de UI |

## Comparacion puntual

| Tema | Webapp | Services | Persistence |
|------|--------|----------|-------------|
| Form input | `@ModelAttribute`, `@Valid` | Recibe DTO/valores ya parseados | No participa |
| Seguridad | Integra vistas/taglibs | No chequea permisos manualmente | No participa |
| Transacciones | No abre/cierra | Si, via `@Transactional` | Se ejecuta dentro de ellas |
| SQL / `java.sql` | Prohibido | Prohibido | Permitido |
| Mailing | No arma ni envia | Si, como caso de uso | No participa |

## Fugas tipicas

| Fuga | Diagnostico | Pagina relacionada |
|------|-------------|--------------------|
| Controller crea entidades o decide ownership | Logica fuera de services/security | [[logica-en-controllers]] |
| Service conoce templates o paths web | Violacion de capas | [[mailing]] |
| `java.sql` aparece fuera de persistence | Detalle de implementacion filtrado | [[persistencia-jdbc]] |
| POM deja visibles implementaciones al compilar | Rompe enforcement por Maven | [[configuracion-maven]] |

## Regla practica

Si un cambio te obliga a tocar JSP, reglas de negocio y SQL en el mismo metodo, probablemente las capas ya estan mezcladas.

## Ver tambien
- [[modelo-capas]]
- [[configuracion-maven]]
- [[logica-en-controllers]]
- [[mailing]]
- [[persistencia-jdbc]]
- [[inyeccion-dependencias]]
- [[resumen-correcciones]]

- [[auditoria-extrema-cumplimiento-paw]]
- [[plan-ejecucion-remediacion-auditoria]]
