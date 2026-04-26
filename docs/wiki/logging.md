---
titulo: Logging — Requisitos y Errores
tipo: concepto
fuentes: [raw/correcciones_tp1.md, raw/apuntes.txt, raw/notas.txt]
creado: 2026-04-09
actualizado: 2026-04-17
---

# Logging — Requisitos y Errores

El logging adecuado es un requerimiento core. `System.out` y `printStackTrace()` estan prohibidos.

## Errores detectados (error grave)

- `System.out.println()` en produccion
- `exception.printStackTrace()` en vez de logger
- Loguear a nivel DEBUG en produccion
- Loguear a INFO en STDOUT en produccion
- Incluir dependencias de logback pero sin configuracion — todo cae en `catalina.out`

## Patron correcto

- Usar SLF4J con Logback como implementacion
- Configurar `logback.xml` con niveles apropiados por entorno
- Root logger en `WARNING` o mas restrictivo para produccion; `DEBUG` solo para desarrollo
- Logger por clase: `private static final Logger LOGGER = LoggerFactory.getLogger(MiClase.class);`
- Loguear excepciones con `LOGGER.error("mensaje", exception)` — no printStackTrace

## Fundamentos de los apuntes

- **Logback**: sucesor de Log4j, buena integracion con Spring
- **Arbol jerarquico**: loggers siguen la jerarquia de paquetes. Se recorre hacia arriba buscando el logger configurado
- **Appender**: componente que escribe los logs (consola, archivo, red, etc.)
- **Root logger**: recibe todos los mensajes; filtrar por nivel

## Detalles practicos de las clases

### Historia: por que SLF4J y Logback
- `java.util.logging` tiene config global por JVM (no portable, no por-aplicacion)
- Log4J resolvio con XML embebido pero tenia bugs en appender de rotacion y la API forzaba concatenacion costosa
- SLF4J nacio como interfaz estandar con bridges para java.util.logging y Log4J
- Logback es la implementacion de referencia que programa nativamente contra SLF4J
- Spring usa java.util.logging internamente; se redirige via bridge `jul-to-slf4j`

### Interpolacion eficiente (evitar concatenacion)
- **Mal**: `LOGGER.debug("user " + user.getEmail())` — concatena aunque DEBUG este deshabilitado
- **Bien**: `LOGGER.debug("user {}", user.getEmail())` — SLF4J interpola solo si va a escribir
- **Mejor** (operaciones costosas): `LOGGER.debug("data {}", () -> heavyOperation())` — lambda diferida
- Esto elimina el anti-patron de envolver cada log con `if (LOGGER.isDebugEnabled())`

### Configuracion dual dev/prod
- `logback-test.xml` en `src/main/resources` para desarrollo (DEBUG, ConsoleAppender)
- `logback.xml` en `src/main/resources` para produccion (WARNING, RollingFileAppender)
- Logback prioriza `logback-test.xml` si existe; en el war se excluye
- Exclusion en pom de webapp: `<packagingExcludes>**/logback-test.xml</packagingExcludes>` en maven-war-plugin

### RollingFileAppender para produccion
- Politica basada en tiempo: un archivo por dia, historial de N dias
- Nombre unico por grupo para no mezclar logs en servidor compartido: `logs/paw2024a-03.warnings`
- Separar appenders: uno general (WARNING+) y uno especifico para el paquete de la app (DEBUG/INFO)

### Additivity
- Si un logger de paquete y el root logger capturan el mismo mensaje, aparece duplicado
- `additivity="false"` en el logger de paquete evita que el mensaje escale al root

### Pattern recomendado
- `%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n`
- Thread es util en contexto web (multiples requests en paralelo)
- `%logger{36}` reduce paquetes largos a iniciales (ej: `o.s.f` para `org.springframework`)

### Por que NO usar System.out
- Es sincronico: bloquea todos los hilos
- No tiene control de nivel ni rotacion
- Llena el disco y cuando el disco se satura puede corromper la base de datos

## Ver tambien
- [[configuracion-maven]]
- [[spring-aop]]
- [[spring-security]]
- [[resumen-enunciado]]
- [[resumen-correcciones]]
- [[resumen-apuntes]]
- [[resumen-notas]]

- [[plan-implementacion-reservas]]
