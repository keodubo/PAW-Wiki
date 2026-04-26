---
titulo: Uso de @Transactional
tipo: concepto
fuentes: [raw/correcciones_tp1.md, raw/notas.txt, raw/apuntes.txt]
creado: 2026-04-09
actualizado: 2026-04-17
---

# Uso de @Transactional

Segun el criterio de la catedra, la capa de servicios debe quedar consistentemente anotada con `@Transactional`; no alcanza con marcar solo algunos metodos de escritura.

## Regla

- La expectativa de la catedra es anotar los metodos publicos de servicio con `@Transactional`
- Metodos de solo lectura: `@Transactional(readOnly = true)`
- Metodos que modifican datos: `@Transactional`

## Errores detectados

- No usar `@Transactional` en ningun lugar de la aplicacion
- Usarlo solo en 4 metodos (insuficiente)
- Omitir `readOnly = true` en selectores
- Logica de negocio en controllers que evita toda transaccionalidad
- `EmailServiceImpl` marcado como `@Transactional` pese a no usar DB y tener metodos `@Async`

## Impacto

Sin `@Transactional`, multiples operaciones de escritura no son atomicas. Si una falla a mitad de camino, los datos quedan en estado inconsistente.

## Mecanismo AOP (segun clases)

### Como funciona internamente
- Spring genera un **Proxy** (decorator) alrededor de los servicios que tienen metodos anotados
- El proxy intercepta las llamadas: abre transaccion antes, commit/rollback despues
- Se basa en `java.lang.reflect.Proxy` — no modifica bytecode, genera wrappers en runtime

### Configuracion requerida
- `@EnableTransactionManagement` en WebConfig
- Bean `PlatformTransactionManager` con el DataSource

### Limitacion critica del proxy
- Solo aplica a **metodos publicos** de la interfaz (el proxy se genera contra la interfaz)
- `@Transactional` en metodos privados no tiene efecto (sin error ni warning)
- Si un metodo llama a `this.otroMetodo()`, el proxy no intercepta esa llamada interna (el `this` es el servicio real, no el proxy)
- Solucion: asegurar que el punto de entrada desde el controller tenga la anotacion correcta

### Otros aspectos AOP disponibles
- `@Async`: ejecuta en background thread (solo void, fire-and-forget). Requiere `@EnableAsync`
- `@Scheduled`: ejecucion programada (cron). Requiere `@EnableScheduling`
- `@Cacheable`: cachea retorno en memoria. Requiere `@EnableCaching`

### Nota sobre stack traces
- Con proxies, los stack traces son mas largos (capas de abstraccion adicionales entre controller y service)
- Es normal — no alarmar

## Ver tambien
- [[comparacion-testing-servicios-y-daos]]
- [[logica-en-controllers]]
- [[manejo-excepciones]]
- [[n-plus-1-joins-java]]
- [[persistencia-jdbc]]
- [[spring-aop]]
- [[hibernate-jpa]]
- [[testing-unitario]]
- [[resumen-apuntes]]
- [[resumen-correcciones]]
- [[resumen-notas]]

- [[plan-implementacion-reservas]]
- [[tp1-vs-tpe2-final]]
