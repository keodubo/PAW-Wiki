---
titulo: Resumen del Enunciado TPE1
tipo: fuente
fuentes: [raw/enunciado.txt, raw/pdfs/Enunciado_TPE1.pdf]
creado: 2026-04-09
actualizado: 2026-04-13
---

# Resumen del Enunciado TPE1

Especificacion oficial del Trabajo Practico Especial - Primera Entrega de PAW.

Nota de fuente: el `.txt` usado para la ingesta coincide con el PDF original tras normalizar espacios y caracteres tipograficos.

## Objetivo

Crear una aplicacion web con Java 21, metodologia Scrum, y dejarla productiva en un servidor web.

## Stack obligatorio

- Java 21
- Spring Web MVC (patron MVC + Front Controller)
- JSP con JSTL (sin codigo Java en JSPs)
- JDBC para persistencia
- PostgreSQL como base de datos
- Maven para build
- Git (repositorio de la catedra)
- Arquitectura Domain Driven Design con inyeccion de dependencias via Spring

## Requerimientos funcionales obligatorios

Independientemente de la tematica elegida:

1. **Diferentes niveles de acceso** por permisos de usuario (ver: [[spring-security]])
2. **Internacionalizacion** del contenido (ver: [[internacionalizacion]])
3. **Manejo de logging** (ver: [[logging]])
4. **Mailing** (ver: [[mailing]])
5. **Queries no triviales** (busquedas verticales o similares) (ver: [[n-plus-1-joins-java]])
6. **Manejo de imagenes**

## Iteraciones y calendario

(ver: [[calendario-entregas]])

- Iteraciones de 2 semanas
- Demo de fin de iteracion **obligatoria**
- Cada iteracion debe incorporar:
  - Feedback completo de la catedra de la entrega anterior
  - Iteracion de flujos previos (complejizarlos)
  - Nuevos flujos core (no solo ABMs)

## Entrega

- Codigo completo en el repositorio git
- Sin archivos innecesarios (IDE configs, dependencias, archivos generados)
- `mvn clean package` debe producir un `.war` funcional
- Al deployar con PostgreSQL, debe autogenerar todas las tablas
- `README.md` con credenciales para cada nivel de acceso
- Entrega via email con hash del commit deployado

## Migraciones

> "Sera responsabilidad del equipo realizar todas las migraciones de base de datos necesarias para garantizar que no se pierda informacion alguna."

Perdida de informacion se penaliza con **2 puntos**.

## Evaluacion

(ver: [[criterios-evaluacion]])

## Bibliografia recomendada

- *Effective Java, 3rd Edition* — Joshua Bloch
- *Domain Driven Design* — Eric Evans

## Ver tambien
- [[tp1-vs-tpe2-final]]
- [[criterios-evaluacion]]
- [[calendario-entregas]]
- [[internacionalizacion]]
- [[logging]]
- [[mailing]]
- [[n-plus-1-joins-java]]
- [[spring-security]]
- [[configuracion-maven]]
- [[testing-unitario]]
- [[resumen-apuntes]]
- [[resumen-notas]]
- [[resumen-notas-sprint-1]]
- [[resumen-correcciones]]
