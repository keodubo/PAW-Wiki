# PAW Service Contracts Layer Rules

## Source Pages

Read current service contracts plus:

- `PAW-Wiki/docs/wiki/modelo-capas.md`
- `PAW-Wiki/docs/wiki/comparacion-capas-web-services-persistence.md`
- `PAW-Wiki/docs/wiki/logica-en-controllers.md`
- `PAW-Wiki/docs/wiki/transactional.md`
- `PAW-Wiki/docs/wiki/manejo-excepciones.md`
- `PAW-Wiki/docs/wiki/mailing.md`

## Allowed Responsibilities

- Interfaces that model business use cases.
- Command/data DTOs that let controllers pass one coherent request to services.
- Domain/business exceptions that services throw and webapp maps.
- Mail service contracts and template-intent methods.

## Forbidden In Contracts

- JSP names, redirect targets, servlet/session/request/response types.
- DAO implementation classes or JDBC details.
- Async vs sync implementation distinction in public API unless the product behavior itself depends on it.
- Controller form beans as service API.

## DTO Rules

- Prefer immutable command/data objects for multi-field operations.
- Use domain types/enums from `models` for validated concepts.
- Keep DTO names tied to use cases: `SaveRestaurantData`, `CreateReservationData`, `ChangePasswordData`.
- Do not make controllers assemble domain entities directly when a DTO can carry the request cleanly.

## Exception Rules

- Use specific exceptions for domain failure modes: not found, invalid state, disabled feature, unavailable slot, invalid current password.
- Put exceptions here when `webapp` needs to map them in `ErrorHandlingAdvice`.
- Do not make webapp parse exception messages.

## Review Checklist

- Does this contract let the controller call one service method?
- Are domain failure modes explicit?
- Can the service implementation remain transactional around the full use case?
- Are web/JDBC details absent from the API?
