---
titulo: Configuracion Maven Multi-Modulo
tipo: concepto
fuentes: [raw/correcciones_tp1.md, raw/enunciado.txt, raw/apuntes.txt]
creado: 2026-04-09
actualizado: 2026-04-27
---

# Configuracion Maven Multi-Modulo

El proyecto debe ser multi-modulo con separacion estricta por responsabilidades.

## Fundamentos de la cursada

- Maven no se presenta solo como gestor de dependencias sino como gestor del ciclo de vida del proyecto.
- En el flujo del curso se usa `mvn archetype:generate` para crear el root project y luego los modulos.
- `groupId` representa la organizacion o dominio invertido.
- `artifactId` representa el artefacto concreto que se construye.
- `1.0-SNAPSHOT` indica una version mutable de trabajo; un release no deberia seguir editandose.

## Herencia y centralizacion

- El pom padre define propiedades y versiones comunes.
- Los hijos heredan esa configuracion y solo declaran las dependencias que realmente usan.
- `dependencyManagement` permite fijar versiones sin obligar a todos los modulos a incorporar la dependencia.
- Si una version cambia, deberia tocarse una sola vez en el padre.

## Errores detectados

### Versiones y dependencias
- Definir versiones en poms hijos en vez de usar `dependencyManagement` del padre (mencionado 2 veces)
- Modulos hijos que declaran `<version>` explicitamente cuando se hereda del padre
- Dependencias duplicadas (ej: validation-api)
- Pom de models declarando Java 17 cuando el global es 21

### Fugas entre capas
- `spring-jdbc` o `spring-web` en la capa de interfaces — viola limites de capa
- SpringFramework en capa de interfaces innecesariamente
- Dependencia de spring-boot cuando se debe usar spring-context puro
- `hsqldb` en scope `compile` en vez de `test`

### Archivos innecesarios
- `.vscode/`, `.PVS-Studio/`, `bin/`, `out/` trackeados en git
- Archivos vacios como `jvm.config` y `maven.config` pusheados (mencionado 2 veces)

## Requisitos del enunciado

- `mvn clean package` debe producir un `.war` funcional
- Al deployar con PostgreSQL, la app debe autogenerar todas las tablas
- `README.md` con credenciales para cada nivel de acceso
- No incluir archivos innecesarios en el repo (IDE configs, dependencias, archivos generados)
- Forma de entrega incorrecta penaliza hasta **1 punto** (ver: [[criterios-evaluacion]])

## Patron correcto

- Un unico `dependencyManagement` en pom padre
- Modulos heredan versiones sin redeclararlas
- `webapp` compila contra `interfaces` y `models`; `services` y `persistence` entran en runtime
- `services` compila contra `interfaces` y `models`; consume `persistence` via `runtime`
- `persistence` compila contra `interfaces` y `models`
- `interfaces` centraliza contratos de servicios y DAOs; separarlo mas es opcional, no obligatorio
- Dependencias de test solo en scope `test`
- `.gitignore` completo que excluya archivos de IDE y build

## Ver tambien
- [[comparacion-capas-web-services-persistence]]
- [[modelo-capas]]
- [[inyeccion-dependencias]]
- [[spring-web-mvc]]
- [[resumen-clases-paw-2026]]
- [[resumen-transcripciones-clases-2-a-4]]
- [[java-style]]
- [[persistencia-jdbc]]
- [[logging]]
- [[resumen-correcciones]]
- [[resumen-apuntes]]
- [[resumen-notas]]
- [[criterios-evaluacion]]
- [[resumen-enunciado]]

- [[2026-04-13_auditoria-repo-docs_v1]]
- [[auditoria-extrema-cumplimiento-paw]]
- [[plan-ejecucion-remediacion-auditoria]]
- [[2026-04-24_auditoria-implementacion-contra-wiki_v1]]
