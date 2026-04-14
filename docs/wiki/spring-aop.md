---
titulo: Spring AOP y Cross-Cutting Concerns
tipo: concepto
fuentes: [raw/apuntes.txt, raw/notas.txt]
creado: 2026-04-13
actualizado: 2026-04-13
---

# Spring AOP y Cross-Cutting Concerns

Esta pagina resume la **unidad 8**. No es un bloque funcional central del TP1, pero **si entra como soporte de implementacion** porque explica el mecanismo que hace funcionar `@Transactional` y otros aspectos basados en proxies.

## Por que importa en TP1

- El TP1 exige una capa de servicios correctamente anotada con `@Transactional`.
- Entender AOP evita errores silenciosos: metodos privados anotados, llamadas internas con `this` y bordes transaccionales puestos en el lugar incorrecto.
- La regla practica para implementacion es dejar el punto de entrada transaccional en metodos publicos de servicio expuestos por interfaz.
- Lo que sigue fuera de foco para TP1 es profundizar pointcuts complejos o usos avanzados de AspectJ que no hacen falta para la entrega.

## Que problema resuelve AOP

- AOP separa concerns transversales del codigo de negocio.
- Ejemplos tipicos: logging, autenticacion, manejo de errores, transacciones, metricas.
- El problema de fondo es que estas piezas no siguen bien una jerarquia de herencia ni una unica entidad de dominio.
- Si se resuelven por convencion a mano, nada garantiza consistencia en todo el codigo.

## Como lo implementa Spring

- Spring retoma la idea de `Before`, `After` y `Around` de AspectJ.
- En vez de mezclar XML pesado, trabaja principalmente con anotaciones.
- La implementacion se apoya en **proxies**: Spring genera una clase envoltorio que wrappea el bean original.
- Mientras el proxy implemente la misma interfaz, para el consumidor es transparente si recibe el bean real o el proxy.

## Restricciones importantes del modelo por proxy

- Las anotaciones AOP solo aplican a llamadas que pasan por el proxy.
- En la practica eso significa:
  - metodos expuestos por la interfaz
  - invocaciones externas al bean
- Una llamada interna como `this.otroMetodo()` no pasa por el proxy.
- Por eso no alcanza con "anotar cualquier metodo"; importa el punto de entrada real del caso de uso.

## Donde conviene aplicar los aspectos

- Aplicarlo en **services** suele ser la opcion mas clara para transacciones de negocio y es el caso que mas impacta en TP1.
- Aplicarlo en **persistence** tambien es posible, pero hay que ser consistente con el criterio elegido.
- El ejemplo de clase muestra una ventaja concreta de hacerlo en servicios: que un mail y un insert puedan quedar atados al mismo borde transaccional.

## Relacion con anotaciones concretas

- `@Transactional` es el caso mas importante para la materia.
- Spring reutiliza la misma idea de proxy para otros concerns:
  - `@Async`
  - `@Scheduled`
  - `@Cacheable`
- `@ControllerAdvice` tambien puede entenderse como una aplicacion del mismo enfoque a excepciones MVC.

## Trade-off real

- AOP limpia codigo repetitivo y centraliza politicas.
- A cambio, agrega capas de abstraccion y stack traces mas largos.
- Si no se entiende bien como funciona el proxy, es facil creer que una anotacion aplica cuando en realidad no pasa nada.

## Ver tambien

- [[tp1-vs-tpe2-final]]
- [[transactional]]
- [[manejo-excepciones]]
- [[logging]]
- [[resumen-apuntes]]
- [[resumen-notas]]
