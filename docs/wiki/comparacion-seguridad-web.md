---
titulo: Comparacion — Seguridad Web
tipo: comparacion
fuentes: [raw/correcciones.md, raw/apuntes.txt, raw/notas.txt, raw/enunciado.txt]
creado: 2026-04-09
actualizado: 2026-04-09
---

# Comparacion — Seguridad Web

La catedra evalua la seguridad en capas distintas. Conviene separar que problema resuelve cada una para no mezclar responsabilidades.

## Mapa rapido

| Area | Resuelve | Herramienta principal | Anti-patron tipico |
|------|----------|-----------------------|--------------------|
| Autenticacion y autorizacion | Quien entra y que puede hacer | [[spring-security]] | `if` manuales en controllers |
| Validacion de entrada | Si los datos recibidos tienen formato/reglas correctas | [[validacion-formularios]] | Validar a mano en controllers |
| Escapado y salida | Si el contenido renderizado puede romper la vista o inyectar scripts | [[xss-prevencion]] | Imprimir `${...}` sin `<c:out>` |
| Distribucion de responsabilidades | Donde vive cada chequeo | [[logica-en-controllers]] | Mezclar seguridad, validacion y negocio en el mismo endpoint |

## Que cubre cada capa

### 1. Spring Security

- Protege rutas, sesiones, login/logout, roles y ownership
- Decide si una accion esta permitida o no
- Debe concentrar la autorizacion, no repartirla por el codigo

### 2. Bean Validation

- Valida input de formularios
- Evita ruido de parsing y reglas basicas en controllers
- No reemplaza controles de acceso

### 3. Escapado/XSS

- Protege la salida HTML y las busquedas con caracteres especiales
- Aplica incluso si el formulario ya valido el input
- No reemplaza autorizacion ni validacion

## Errores de mezcla que la catedra penaliza

| Mezcla incorrecta | Por que esta mal | Ubicacion correcta |
|-------------------|------------------|--------------------|
| Verificar permisos con `if` en controller | Duplica reglas y usa magic strings | [[spring-security]] |
| Validar password/email a mano en controller | Ensucia MVC y complica UX | [[validacion-formularios]] |
| Confiar en patterns del form para evitar XSS | La salida sigue siendo insegura | [[xss-prevencion]] |
| Hacer todo junto en el endpoint | Rompe capas y dificulta transacciones | [[logica-en-controllers]] |

## Recomendacion practica para el TPE1

1. Usar Spring Security para acceso y ownership.
2. Usar form beans con `@Valid` para input.
3. Usar `<c:out>` y sanitizacion de LIKE para output y busquedas.
4. Mantener controllers finos: reciben, delegan y responden.

## Ver tambien
- [[spring-security]]
- [[validacion-formularios]]
- [[xss-prevencion]]
- [[logica-en-controllers]]
- [[criterios-evaluacion]]
- [[resumen-correcciones]]
