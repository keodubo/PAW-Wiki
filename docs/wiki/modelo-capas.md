---
titulo: Modelo de Capas y Separacion de Responsabilidades
tipo: concepto
fuentes: [raw/apuntes.txt, raw/correcciones_tp1.md]
creado: 2026-04-09
actualizado: 2026-04-17
---

# Modelo de Capas y Separacion de Responsabilidades

La arquitectura del proyecto sigue DDD con separacion estricta en modulos Maven.

## Estructura de modulos

```
webapp          (war)  — Controllers, JSPs, Spring Security config, forms
  ↓ compile
interfaces      (jar)  — Contratos de servicios y DAOs
  ↓ compile
models          (jar)  — Entidades de dominio (POJOs)
  ↓ runtime
services        (jar)  — Implementacion de logica de negocio
  ↓ runtime
persistence     (jar)  — Implementacion JDBC de DAOs
```

## Scopes de Maven

La materia trabaja con un modulo `interfaces` compartido. En el material se menciona que partirlo en submodulos separados podria hacerse, pero no es el setup base asumido por la catedra.

Las implementaciones concretas se conectan via scope `runtime` para forzar programacion contra interfaces:

| Dependencia | Scope | Significado |
|------------|-------|-------------|
| webapp -> interfaces | compile | Controllers compilan contra contratos, no contra implementaciones |
| webapp -> models | compile | La capa web puede usar el modelo de dominio |
| webapp -> services | **runtime** | Necesario al ejecutar, no visible al compilar |
| webapp -> persistence | **runtime** | Entra transitivamente en runtime para que Spring pueda inyectar implementaciones |
| services -> interfaces | compile | Servicios implementan contratos y consumen contratos de DAO |
| services -> models | compile | La logica de negocio opera sobre entidades |
| services -> persistence | **runtime** | La implementacion JDBC entra en ejecucion, no en compilacion |
| persistence -> interfaces | compile | DAOs implementan contratos definidos en `interfaces` |
| persistence -> models | compile | Los DAOs leen y escriben entidades del dominio |
| hsqldb | **test** | Solo para tests, nunca en produccion |

## Regla central

> "Ciertos errores logicos que son faciles de cometer se convierten en errores de compilacion"

Si webapp depende de services en `compile`, podria accidentalmente usar clases de persistencia (por dependencia transitiva). Con `runtime`, esto es un error de compilacion.

## Responsabilidades por capa

| Capa | Responsabilidad | Prohibido |
|------|----------------|-----------|
| webapp | Recibir request, validar con @Valid, elegir vista | Logica de negocio, instanciar modelos, conocer java.sql |
| services | Logica de negocio, @Transactional, orquestar DAOs | Conocer vistas, templates, paths de webapp |
| persistence | JDBC, RowMappers, queries SQL | Logica de negocio, acceder a webapp |
| models | Entidades de dominio (POJOs) | java.sql, Spring, dependencias de infraestructura |
| interfaces | Contratos (interfaces) | Dependencias de implementacion |

## Ver tambien
- [[comparacion-capas-web-services-persistence]]
- [[configuracion-maven]]
- [[logica-en-controllers]]
- [[mailing]]
- [[persistencia-jdbc]]
- [[inyeccion-dependencias]]
- [[resumen-transcripciones-clases-2-a-4]]
- [[spring-web-mvc]]
- [[spring-security]]
- [[resumen-apuntes]]

- [[plan-implementacion-reservas]]
- [[2026-04-24_auditoria-implementacion-contra-wiki_v1]]
