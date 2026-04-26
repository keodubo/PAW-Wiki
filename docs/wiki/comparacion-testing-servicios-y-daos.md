---
titulo: Comparacion — Testing de Servicios y DAOs
tipo: comparacion
fuentes: [raw/correcciones_tp1.md, raw/apuntes.txt, raw/notas.txt, raw/enunciado.txt]
creado: 2026-04-09
actualizado: 2026-04-13
---

# Comparacion — Testing de Servicios y DAOs

La materia distingue claramente entre testear logica de negocio y testear persistencia. Confundir ambos niveles baja calidad y cobertura real.

## Mapa rapido

| Tipo de test | Objeto bajo prueba | Dependencias | Validacion correcta | Error tipico |
|--------------|--------------------|--------------|---------------------|--------------|
| Servicio | Caso de uso / facade | DAOs mockeados | Estado final y escenarios | `verify()` sobre implementacion |
| DAO / persistencia | Query y mapping JDBC | HSQLDB + schema compartido | Estado real de la BD | Setear precondiciones con DAOs |

## Servicios vs DAOs

### Tests de servicio

- Foco: reglas de negocio
- Herramientas: `Mockito`, `@Mock`, `@InjectMocks`
- Lo importante: asserts sobre el resultado, happy path y edge cases

### Tests de DAO

- Foco: inserts, updates, queries, rowmappers, integridad
- Herramientas: HSQLDB, `JdbcTestUtils`, `@Rollback`, `DataSourceInitializer`
- Lo importante: contar rows, consultar estado post-operacion, independencia entre tests

## Donde entra `@Transactional`

| Capa | Rol de `@Transactional` | Implicancia para tests |
|------|--------------------------|------------------------|
| Services | Agrupa operaciones del caso de uso | Conviene testear que el servicio orquesta bien, no la implementacion interna |
| Persistence | No reemplaza test de DAO | Igual hay que validar la BD de verdad |

## Anti-patrones que aparecen juntos

| Anti-patron | Se ve en | Consecuencia |
|-------------|-----------|--------------|
| `verify()` / `never()` | Servicios | Test acoplado a implementacion |
| DAOs para preparar estado de otros DAOs | Persistencia | Test deja de ser aislado |
| Falta de `@Rollback` o limpieza | Persistencia | Dependencias entre tests |
| Cobertura solo de DAOs o solo de services | Ambas | Huecos de calidad |

## Regla operativa

Si queres saber si una regla de negocio funciona, testea el servicio.  
Si queres saber si la query y el mapping funcionan, testea el DAO contra BD.

## Ver tambien
- [[testing-unitario]]
- [[persistencia-jdbc]]
- [[transactional]]
- [[manejo-excepciones]]
- [[criterios-evaluacion]]
- [[resumen-notas]]
- [[resumen-correcciones]]
