---
titulo: Resumen de Correcciones TP2
tipo: fuente
fuentes: [raw/correcciones_tp2.md, raw/pdfs/Devolucion_TP2_2025_C1.pdf, raw/pdfs/Devolucion_TP2_2025_C2.pdf]
creado: 2026-05-15
actualizado: 2026-05-15
---

# Resumen de Correcciones TP2

Ingesta estructurada de dos devoluciones historicas de TP2 (`2025 C1` y `2025 C2`). La fuente consolidada completa queda en `raw/correcciones_tp2.md`; esta pagina resume como usarla dentro del canon de la wiki.

## Que aporta esta fuente

- Convierte 32 devoluciones por grupo en patrones reutilizables de auditoria TP2.
- Refuerza que TP2 no es solo "pasar a Hibernate": tambien exige cerrar feedback TP1, preservar flujos, cuidar UX, sostener tests y mantener deploy limpio.
- Agrega ejemplos concretos de fallas JPA/Hibernate que no aparecian con tanta fuerza en [[resumen-correcciones]]: modelo 1+1, `FetchType.EAGER`, lazy loading en async, relaciones mapeadas como ids y queries sin paginacion.
- Mantiene tambien las observaciones positivas: buen uso de Plane, buena cobertura de tests, `@Formula`, builder pattern, filtros, componentes custom y flujos completos.

## Hallazgos dominantes

| Area | Patron de correccion | Impacto practico |
|------|----------------------|------------------|
| Paginacion y fetch | No usar modelo 1+1, traer colecciones completas o paginar en memoria | Riesgo alto de N+1, memoria, lentitud y resultados incorrectos |
| Arquitectura MVC | Controllers con logica de negocio, validaciones manuales o multiples llamadas de caso de uso | Rompe separacion de capas y transacciones |
| Seguridad | Ownership manual o incompleto; links visibles hacia recursos prohibidos | Permite editar/ver recursos ajenos o deriva en 403/404 evitables |
| Testing | `Mockito.verify()`, tests no unitarios, falta de `flush()` o estado DB final | Tests verdes que no prueban comportamiento real |
| JPA/Hibernate | Relaciones como ids, `EAGER` innecesario, lazy en async, `JOIN FETCH` explosivo | La migracion puede compilar pero no escalar ni respetar el dominio |
| UX/i18n | CTAs escondidos, back por history, mails incompletos, keys visibles, encoding roto | Flujos core confusos o inutilizables durante demo |
| Entrega | Repo pesado/sucio, `schema.sql` en `webapp`, dependencias mal scopeadas, credenciales README rotas | Riesgo de penalizacion por forma de entrega y deploy |

## Reglas operativas que quedan reforzadas

1. Auditar cada listado paginado con SQL real y cardinalidad esperada.
2. Revisar mappings JPA por defecto: preferir `LAZY` y cargar relaciones con queries explicitas cuando la pantalla lo necesita.
3. Mantener los controllers delgados: un llamado de caso de uso, forms validados y errores por advice.
4. Resolver ownership con Spring Security o un mecanismo declarativo consistente.
5. Probar servicios core, queries complejas, casos borde y estado final de base de datos.
6. Chequear UX de demo completa: mail, back navigation, CTAs, zero states, i18n y confirmaciones.
7. Verificar repo entregable: Maven, WAR, README, archivos generados, logs y configuracion.

## Como usarla en una auditoria TP2

- Para planificar una migracion: leer primero [[resumen-enunciado-tpe2]], despues esta pagina y finalmente el detalle completo en `raw/correcciones_tp2.md`.
- Para revisar una app ya migrada: cruzar los hallazgos de esta fuente con [[hibernate-jpa]] y ejecutar pruebas contra los flujos reales.
- Para priorizar fixes: usar [[criterios-evaluacion]]; los patrones de arquitectura, seguridad, perdida de datos, testing y usabilidad son los que mas puntos arriesgan.
- Para comparar contra feedback TP1: leer junto con [[resumen-correcciones]], porque muchas devoluciones TP2 penalizan reincidencias ya marcadas.

## Ver tambien

- [[resumen-enunciado-tpe2]]
- [[hibernate-jpa]]
- [[criterios-evaluacion]]
- [[tp1-vs-tpe2-final]]
- [[resumen-correcciones]]
