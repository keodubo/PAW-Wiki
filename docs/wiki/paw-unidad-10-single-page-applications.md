---
titulo: PAW Unidad 10 - Single Page Applications
tipo: fuente
fuentes: [raw/apuntes.txt, raw/pdfs/PAW - Apuntes.pdf]
creado: 2026-05-11
actualizado: 2026-05-11
---

# PAW Unidad 10 - Single Page Applications

Esta unidad introduce el cambio de TP final: el frontend deja de ser una coleccion de JSP renderizadas por servidor y pasa a ser un cliente con estado propio que consume una API.

## Thin client vs thick client

- En TP1, el servidor renderiza vistas y concentra gran parte de la logica de presentacion.
- En SPA, el navegador maneja mas estado, input e interaccion.
- El backend entrega datos y contratos; el cliente decide como presentar.
- Se reducen algunos round-trips, pero crece la complejidad de frontend.
- Validaciones, errores, auth e i18n deben pensarse de nuevo.

## Autenticacion y seguridad

- Login, registro, cambio de password, expiracion y acceso pasan a tener tratamiento del lado cliente.
- El frontend debe reaccionar ante 401/403 sin depender de una JSP de error.
- El apunte contrasta cookies con almacenamiento/control del cliente.
- Cookies se envian automaticamente y pueden sumar overhead o riesgos segun configuracion.
- Local storage da control, pero aumenta el impacto de XSS.
- La decision final debe explicar trade-offs y mantenerse consistente con la API.

## Internacionalizacion

- `MessageSource` ya no alcanza para toda la UI.
- La SPA necesita una libreria frontend de i18n.
- La migracion debe conservar placeholders, fallback y catalogos por idioma.
- Se pueden usar herramientas para extraer keys.
- La regla sigue siendo traducir frases completas, no concatenar fragmentos.

## Testing frontend

- La unidad marca que ahora hay logica significativa en frontend.
- La capa de estado/servicios del frontend es critica.
- Jest y Vitest aparecen como herramientas de testing.
- Nock y Sinon aparecen como herramientas de mocking.
- No necesariamente se exige Cypress/end-to-end completo, pero si testear la logica que orquesta estado y requests.

## Optimizacion

- Una SPA descarga JS/CSS/assets y luego opera sobre esa carga.
- Minificar reduce peso sin cambiar comportamiento.
- Que un framework "lo haga" no alcanza: hay que verificar el build.
- Lighthouse mide performance, accesibilidad, SEO, buenas practicas y mobile.
- La experiencia de primer load importa porque el usuario espera antes de tener UI interactiva.

## Hosting y Maven

- La entrega final debe poder empaquetar frontend junto al backend.
- `mvn package` deberia producir un artefacto deployable con assets construidos.
- `frontend-maven-plugin` aparece como mecanismo de integracion.
- El modulo frontend debe ejecutarse antes del packaging web.
- `webapp` debe servir recursos estaticos generados.
- No conviene depender de pasos manuales para copiar bundles.

## SSR

- Server-side rendering se menciona como posibilidad, no como requisito.
- Permite primera impresion rapida y posterior carga/hidratacion del cliente.
- En ese primer render no existen `window` ni `localStorage`.
- Cualquier componente que dependa de APIs del browser debe contemplar ese limite.

## Cache

- Cache no mejora el primer load; mejora visitas recurrentes.
- Cache condicional evita descarga, pero conserva round-trip.
- Cache no condicional usa copia local mientras sea valida.
- `index.html` o `/` no debe cachearse como inmutable.
- Assets versionados pueden cachearse agresivamente.
- File revving cambia nombres por hash/contenido para invalidar cache cuando corresponde.
- Spring debe servir headers coherentes con esa estrategia.

## Riesgos a auditar en TP final

- Rehacer frontend sin reimplementar errores, auth e i18n.
- Cachear el root y dejar usuarios atrapados en una version vieja.
- No incluir el build frontend en el WAR.
- No testear la capa de estado.
- Dejar credenciales en almacenamiento inseguro sin razon ni mitigacion.

## Ver tambien

- [[resumen-apuntes]]
- [[single-page-applications]]
- [[api-rest]]
- [[internacionalizacion]]
- [[ux-flows]]
- [[paw-unidad-09-hibernate-jpa]]
- [[paw-unidad-11-api-rest]]
