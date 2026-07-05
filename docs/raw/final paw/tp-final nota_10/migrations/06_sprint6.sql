ALTER TABLE pools ADD COLUMN IF NOT EXISTS price DECIMAL(10,2) NOT NULL DEFAULT 0;
UPDATE pools SET price = (SELECT p.price FROM products p WHERE p.id = pools.product_id) WHERE price = 0;

DELETE FROM reviews r1 WHERE id NOT IN (SELECT id FROM reviews r2 WHERE r2.reviewer_id = r1.reviewer_id AND r1.product_id = r2.product_id LIMIT 1);

CREATE TABLE IF NOT EXISTS reports (
    id SERIAL PRIMARY KEY,
    description VARCHAR(1024) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    user_reported BOOLEAN NOT NULL,
    company_id INTEGER REFERENCES companies(id) ON DELETE CASCADE NOT NULL,
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE NOT NULL
);

INSERT INTO migrations (id) VALUES (6);