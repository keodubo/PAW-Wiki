---
titulo: PAW Unidad 8 - AOP, Proxies y Transacciones
tipo: fuente
fuentes: [raw/apuntes.txt, raw/pdfs/PAW - Apuntes.pdf]
creado: 2026-05-11
actualizado: 2026-05-11
---

# PAW Unidad 8 - AOP, Proxies y Transacciones

Esta unidad es la bisagra entre el TP1 y temas posteriores. No agrega una feature visible por si misma, pero explica por que `@Transactional`, `@Async`, `@Scheduled` y otros concerns transversales funcionan solo bajo ciertas condiciones.

## Problema que resuelve AOP

- AOP separa concerns transversales del codigo de negocio.
- Ejemplos: logging, seguridad, manejo de errores, transacciones, metricas y performance.
- Estos concerns aparecen en muchas clases y no representan una entidad del dominio.
- Modelarlos con herencia es incorrecto: el problema no es "todos heredan de una clase", sino "todos necesitan una politica alrededor de ciertos metodos".
- Resolverlo por convencion manual es fragil porque nada obliga a aplicarlo en todos los lugares correctos.

## Modelo de Spring

- Spring toma ideas de AspectJ como `Before`, `After` y `Around`.
- En vez de forzar XML pesado, la materia lo presenta mediante anotaciones.
- La implementacion se apoya en proxies.
- El proxy wrappea el bean real e implementa el mismo contrato.
- Si una llamada entra por el proxy, Spring puede ejecutar advice antes/despues/alrededor del metodo real.

## Por que importan los proxies

- El proxy solo intercepta llamadas externas que pasan por el objeto proxy.
- Una llamada interna con `this.otroMetodo()` no pasa por el proxy.
- Un metodo privado no es un punto de entrada interceptable.
- Si se usan proxies por interfaz, los metodos relevantes deben estar expuestos en la interfaz.
- Esto explica errores silenciosos: la anotacion puede estar presente, pero no activarse en runtime.

## `@Transactional`

- `@Transactional` es el caso mas importante de AOP para PAW.
- El borde natural es el metodo publico de service que representa el caso de uso.
- En lecturas, usar `readOnly = true` cuando corresponda.
- En escrituras, el metodo de service deberia cubrir todas las operaciones que deben committear o rollbackear juntas.
- Poner transacciones solo en persistence puede dejar casos de negocio partidos si un service orquesta varias operaciones.
- Ponerlas en services exige consistencia y entender que helpers internos no abren una nueva transaccion si se llaman con `this`.

## Nivel de aplicacion

| Nivel | Ventaja | Riesgo |
| --- | --- | --- |
| Service | Cubre caso de uso completo y reglas de negocio. | Hay que distinguir lectura/escritura y no esconder llamados internos. |
| Persistence | Cerca de la operacion de DB. | Puede atomizar demasiado y no cubrir caso de uso completo. |
| Controller | Tiene contexto HTTP. | Mezcla web con transacciones de negocio; no es el borde recomendado. |

## Relacion con otros aspectos

- `@Async` usa un modelo similar de invocacion por proxy.
- `@Scheduled` tambien depende de configuracion y de que Spring gestione el bean.
- `@Cacheable` introduce caching transversal.
- `@ControllerAdvice` puede pensarse como manejo transversal de errores web.

## Checklist de implementacion

- Confirmar `@EnableTransactionManagement`.
- Confirmar `PlatformTransactionManager` correcto para JDBC o JPA segun etapa.
- Revisar que los services publicos tengan anotacion coherente.
- Buscar metodos privados anotados: son sospechosos.
- Buscar llamadas internas que esperan comportamiento transaccional.
- No asumir que un stack trace largo indica error: puede mostrar capas de proxy.

## Lectura por etapa

- En TP1, esta unidad sostiene `@Transactional` sobre services JDBC.
- En TP2, vuelve a importar porque JPA y contexto de persistencia dependen fuertemente de transacciones.
- En TP final, el mismo modelo mental ayuda con concerns como auth, errores, async y cache.

## Ver tambien

- [[resumen-apuntes]]
- [[spring-aop]]
- [[transactional]]
- [[modelo-capas]]
- [[paw-unidad-07-seguridad-logging]]
- [[paw-unidad-09-hibernate-jpa]]
