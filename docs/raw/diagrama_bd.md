```mermaid
erDiagram
    images {
        BIGINT id PK
        BLOB data
        VARCHAR mime_type
    }

    users {
        BIGINT id PK
        VARCHAR name
        VARCHAR username UK
        VARCHAR email UK
        VARCHAR password_hash
        VARCHAR preferred_locale
        BIGINT profile_image_id FK
        TEXT profile_description
        TIMESTAMP created_at
    }

    user_roles {
        BIGINT user_id PK, FK
        VARCHAR role PK
    }

    user_claim_tokens {
        BIGINT id PK
        BIGINT user_id FK
        CHAR token_hash UK
        TIMESTAMP expires_at
        TIMESTAMP consumed_at
        TIMESTAMP created_at
    }

    user_password_reset_tokens {
        BIGINT id PK
        BIGINT user_id FK
        CHAR token_hash UK
        TIMESTAMP expires_at
        TIMESTAMP consumed_at
        TIMESTAMP created_at
    }

    restaurants {
        BIGINT id PK
        VARCHAR name
        VARCHAR slug UK
        VARCHAR cuisine
        VARCHAR neighborhood
        VARCHAR address
        TEXT description
        VARCHAR image_url
        VARCHAR price_range
        VARCHAR phone
        VARCHAR email
        VARCHAR tags
        TEXT menu
        BOOLEAN is_open
        BIGINT image_id FK
        BIGINT owner_id FK
        VARCHAR status
        TEXT rejection_reason
        TIMESTAMP submitted_at
        TIMESTAMP reviewed_at
        BOOLEAN accepts_reservations
        VARCHAR reservation_disabled_mode
        VARCHAR reservation_external_link
        VARCHAR reservation_confirmation_mode
        SMALLINT reservation_slot_duration_minutes
    }

    restaurant_hours {
        BIGINT id PK
        BIGINT restaurant_id FK
        SMALLINT day_of_week
        SMALLINT shift
        TIME open_time
        TIME close_time
    }

    reviews {
        BIGINT id PK
        BIGINT restaurant_id FK
        BIGINT user_id FK
        NUMERIC rating
        TEXT comment
        TIMESTAMP created_at
        BIGINT reservation_id FK, UK
    }

    restaurant_tables {
        BIGINT id PK
        BIGINT restaurant_id FK
        INTEGER table_number
        SMALLINT capacity
        TIMESTAMP created_at
    }

    reservations {
        BIGINT id PK
        BIGINT restaurant_id FK
        BIGINT user_id FK
        BIGINT table_id FK
        DATE reservation_date
        TIME slot_start
        TIME slot_end
        SMALLINT party_size
        TEXT comment
        VARCHAR status
        BOOLEAN no_show
        BOOLEAN cancelled_by_user
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    reservation_notifications {
        BIGINT id PK
        BIGINT reservation_id FK
        VARCHAR notification_type
        TIMESTAMP sent_at
    }

    users ||--o| images : "profile_image_id"
    user_roles }|--|| users : "user_id"
    user_claim_tokens }|--|| users : "user_id"
    user_password_reset_tokens }|--|| users : "user_id"
    restaurants ||--o| images : "image_id"
    restaurants }|--o| users : "owner_id"
    restaurant_hours }|--|| restaurants : "restaurant_id"
    reviews }|--|| restaurants : "restaurant_id"
    reviews }|--|| users : "user_id"
    restaurants ||--o{ restaurant_tables : "restaurant_id"
    restaurants ||--o{ reservations : "restaurant_id"
    users ||--o{ reservations : "user_id"
    restaurant_tables ||--o{ reservations : "table_id"
    reservations ||--o{ reservation_notifications : "reservation_id"
    reservations ||--o| reviews : "reservation_id"
```

## Notas funcionales para reservas

- PENDING y CONFIRMED bloquean el slot de la mesa.
- CANCELLED y COMPLETED no bloquean disponibilidad.
- `no_show` es un flag, no un estado principal.
- Una reserva se completa automáticamente cuando termina su franja horaria y estaba CONFIRMED.
- Un restaurante no puede cambiar su layout si tiene reservas PENDING o CONFIRMED.
- Los slots se derivan de `restaurant_hours` + `reservation_slot_duration_minutes`.
- La review solo puede existir sobre una reserva completada.

