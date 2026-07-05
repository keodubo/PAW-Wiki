
ALTER TABLE customer RENAME TO users_old;
ALTER TABLE company RENAME TO companies;
ALTER TABLE image RENAME TO images;
ALTER TABLE product RENAME TO products;
ALTER TABLE pool RENAME TO pools;
ALTER TABLE request RENAME TO requests;

CREATE TABLE IF NOT EXISTS categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(30) UNIQUE NOT NULL
);
CREATE TABLE IF NOT EXISTS locations (
     id SERIAL PRIMARY KEY,
     name VARCHAR(100) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(30) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    first_name VARCHAR(30) NOT NULL DEFAULT '',
    last_name VARCHAR(30) NOT NULL DEFAULT '',
    company_id INTEGER REFERENCES companies(id) ON DELETE CASCADE,
    location_id INTEGER DEFAULT 1 REFERENCES locations(id) ON DELETE SET DEFAULT
);

INSERT INTO locations (name) VALUES ('Desconocido') ON CONFLICT (name) DO NOTHING;

INSERT INTO categories (name) VALUES ('Otros') ON CONFLICT (name) DO NOTHING;
INSERT INTO categories (name) VALUES ('Bebidas') ON CONFLICT (name) DO NOTHING;
INSERT INTO categories (name) VALUES ('Alcohol') ON CONFLICT (name) DO NOTHING;
INSERT INTO categories (name) VALUES ('Electro') ON CONFLICT (name) DO NOTHING;
INSERT INTO categories (name) VALUES ('Mascotas') ON CONFLICT (name) DO NOTHING;
INSERT INTO categories (name) VALUES ('Higiene') ON CONFLICT (name) DO NOTHING;
INSERT INTO categories (name) VALUES ('Comida') ON CONFLICT (name) DO NOTHING;

INSERT INTO users (id, email, password, first_name, last_name, company_id, location_id)
SELECT id, email, '1234', '', '', NULL, 1 FROM users_old;

ALTER TABLE requests DROP CONSTRAINT IF EXISTS request_customer_id_fkey;
ALTER TABLE requests RENAME COLUMN customer_id TO user_id;
ALTER TABLE requests ADD CONSTRAINT requests_user_id_fkey FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

DROP TABLE users_old;

ALTER TABLE companies ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE products ADD COLUMN IF NOT EXISTS category_id INTEGER
REFERENCES categories(id) ON DELETE CASCADE
DEFAULT 1;

ALTER TABLE pools ADD COLUMN IF NOT EXISTS location_id INTEGER
REFERENCES locations(id) ON DELETE CASCADE
DEFAULT 1;

ALTER TABLE pools ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT TRUE;

INSERT INTO migrations (id) VALUES (1);