---
titulo: Manejo de Imagenes
tipo: concepto
fuentes: [raw/apuntes.txt, raw/correcciones_tp1.md]
creado: 2026-04-09
actualizado: 2026-04-13
---

# Manejo de Imagenes

El manejo de imagenes es un requerimiento core obligatorio del TPE1.

## Arquitectura de almacenamiento

Segun los apuntes, separar entidades e imagenes en dos tablas:

```
entidad (id, name, ..., image_id FK)
imagen  (id, content bytea)
```

La imagen se almacena como `bytea` en PostgreSQL con una tabla dedicada.

## Consideraciones

### Upload (Multipart)
- Validar extension genuina del archivo subido (no confiar solo en el nombre)
- Grupos que filtraron extensiones no genuinas desde Multipart fueron elogiados

### Cache
- **No cachear imagenes** con `ConcurrentMapCacheManager` — no evicta, causa OutOfMemory
- Usar cache con politica de eviccion o servir directamente

### Controller
- Endpoint dedicado para servir imagenes (retorna bytes con content-type correcto)
- No instanciar modelos de imagen en el controller

## Ver tambien
- [[http-y-sesiones]]
- [[api-rest]]
- [[persistencia-jdbc]]
- [[validacion-formularios]]
- [[resumen-apuntes]]
- [[buenas-practicas]]
- [[resumen-notas-sprint-1]]
