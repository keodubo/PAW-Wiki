CREATE TABLE IF NOT EXISTS reviews (
    id SERIAL PRIMARY KEY,
    reviewer_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    product_id INTEGER REFERENCES products(id) ON DELETE CASCADE,
    rating DECIMAL(10, 2) NOT NULL,
    description VARCHAR(1024) NOT NULL
);

ALTER TABLE products ADD COLUMN IF NOT EXISTS active boolean DEFAULT TRUE NOT NULL;

CREATE TABLE IF NOT EXISTS documents(
    id SERIAL PRIMARY KEY,
    name VARCHAR(256) NOT NULL,
    filetype VARCHAR(100) NOT NULL,
    bytes BYTEA NOT NULL
);

INSERT INTO documents (id, name, filetype, bytes)
SELECT id, 'image', 'image/png', bytes FROM images;

ALTER TABLE products DROP CONSTRAINT IF EXISTS product_image_id_fkey;
ALTER TABLE products RENAME COLUMN image_id TO document_id;
ALTER TABLE products ADD CONSTRAINT product_document_id_fkey FOREIGN KEY (document_id) REFERENCES documents(id) ON DELETE CASCADE;

ALTER TABLE companies DROP CONSTRAINT IF EXISTS company_image_id_fkey;
ALTER TABLE companies RENAME COLUMN image_id TO document_id;
ALTER TABLE companies ADD CONSTRAINT company_document_id_fkey FOREIGN KEY (document_id) REFERENCES documents(id) ON DELETE CASCADE;

DROP TABLE images;

ALTER TABLE requests ADD COLUMN IF NOT EXISTS document_id INTEGER REFERENCES documents(id) ON DELETE CASCADE;

ALTER TABLE pools ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL;
ALTER TABLE pools ADD COLUMN IF NOT EXISTS down_payment INTEGER DEFAULT 0 NOT NULL;

ALTER TABLE companies ADD COLUMN IF NOT EXISTS cbu VARCHAR(22) DEFAULT '' NOT NULL;

INSERT INTO migrations (id) VALUES (4);
