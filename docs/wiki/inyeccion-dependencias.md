---
titulo: Inyeccion de Dependencias en Spring
tipo: concepto
fuentes: [raw/apuntes.txt]
creado: 2026-04-09
actualizado: 2026-04-13
---

# Inyeccion de Dependencias en Spring

Spring gestiona instancias (beans) y las inyecta automaticamente donde se necesiten, eliminando la necesidad de instanciar objetos manualmente.

## Idea central

Spring separa dos cosas:

- la **configuracion** de objetos y wiring
- el **uso** de esos objetos en controllers, services o repositories

Eso evita que cada clase tenga que crear manualmente sus dependencias.

## Mecanismo

1. **Configuracion**: `WebConfig.java` con `@Configuration` y `@ComponentScan("ar.edu.itba.paw")`
2. **Declaracion**: Clases anotadas con `@Controller`, `@Service`, `@Repository`, `@Component`
3. **Inyeccion**: `@Autowired` en el campo, constructor o setter

## Contexto web

El servlet dispatcher toma la configuracion basada en codigo (no XML):
- `contextConfigLocation` apunta a la clase WebConfig
- `load-on-startup` para inicializacion eager (no lazy)
- Contexto global via `ContextLoaderListener`

## Beans

- Spring crea y administra las instancias (singletons por defecto)
- Centraliza la configuracion — cambiar implementacion en un solo lugar
- Los tests pueden inyectar mocks en lugar de implementaciones reales
- Un bean tambien puede reemplazar defaults de Spring, como el `ViewResolver`

## @ComponentScan

Especificar el paquete permite a Spring ser eficiente y no configurar clases innecesarias. El paquete de configuraciones de Spring no necesita escanearse a si mismo — las clases ya estan mapeadas en web.xml.

## Impacto practico

- Un controller no deberia hacer `new ServicioImpl(...)`.
- Un service no deberia elegir a mano su DAO concreto dentro del codigo.
- Centralizar wiring hace mas barato cambiar implementaciones, testear y reemplazar defaults de infraestructura.

## Ver tambien
- [[comparacion-capas-web-services-persistence]]
- [[modelo-capas]]
- [[configuracion-maven]]
- [[spring-web-mvc]]
- [[resumen-transcripciones-clases-2-a-4]]
- [[resumen-notas]]
- [[resumen-apuntes]]
