INSERT INTO locations (name) VALUES ('La Matanza') ON CONFLICT (name) DO NOTHING;
INSERT INTO locations (name) VALUES ('La Plata') ON CONFLICT (name) DO NOTHING;
INSERT INTO locations (name) VALUES ('Quilmes') ON CONFLICT (name) DO NOTHING;
INSERT INTO locations (name) VALUES ('Adrogué') ON CONFLICT (name) DO NOTHING;
INSERT INTO locations (name) VALUES ('Lanús') ON CONFLICT (name) DO NOTHING;
INSERT INTO locations (name) VALUES ('Tigre') ON CONFLICT (name) DO NOTHING;
INSERT INTO locations (name) VALUES ('Pilar') ON CONFLICT (name) DO NOTHING;
INSERT INTO locations (name) VALUES ('Avellaneda') ON CONFLICT (name) DO NOTHING;
INSERT INTO locations (name) VALUES ('Núñez, Belgrano, Colegiales') ON CONFLICT (name) DO NOTHING;
INSERT INTO locations (name) VALUES ('Palermo') ON CONFLICT (name) DO NOTHING;
INSERT INTO locations (name) VALUES ('Núñez, Belgrano, Colegiales') ON CONFLICT (name) DO NOTHING;
INSERT INTO locations (name) VALUES ('Coghlan, Saavedra, Villa Urquiza, Villa Pueyrredón') ON CONFLICT (name) DO NOTHING;
INSERT INTO locations (name) VALUES ('La Boca, Barracas, Parque Patricios, Nueva Pompeya') ON CONFLICT (name) DO NOTHING;
INSERT INTO locations (name) VALUES ('Caballito') ON CONFLICT (name) DO NOTHING;

ALTER TABLE categories ADD COLUMN IF NOT EXISTS icon_name VARCHAR(30) DEFAULT 'question' NOT NULL;
UPDATE categories SET icon_name = 'hamburger' WHERE name = 'Comida';
UPDATE categories SET icon_name = 'bottle-water' WHERE name = 'Bebidas';
UPDATE categories SET icon_name = 'wine-glass' WHERE name = 'Alcohol';
UPDATE categories SET icon_name = 'tv' WHERE name = 'Electro';
UPDATE categories SET icon_name = 'paw' WHERE name = 'Mascotas';
UPDATE categories SET icon_name = 'soap' WHERE name = 'Higiene';

ALTER TABLE companies ADD COLUMN IF NOT EXISTS image_id INTEGER;
ALTER TABLE requests ADD COLUMN IF NOT EXISTS delivered BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE pools ADD COLUMN IF NOT EXISTS canceled BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE pools ADD COLUMN IF NOT EXISTS finished BOOLEAN NOT NULL DEFAULT FALSE;
UPDATE pools SET finished = NOT active;
ALTER TABLE pools DROP COLUMN IF EXISTS active;

UPDATE requests a SET quantity = (
    SELECT SUM(quantity) FROM requests r
    WHERE r.user_id = a.user_id AND r.pool_id = a.pool_id
    GROUP BY user_id, pool_id
);

DELETE FROM requests
WHERE id NOT IN (
    SELECT MAX(id) FROM requests
    GROUP BY user_id, pool_id
);

INSERT INTO migrations (id) VALUES (2);