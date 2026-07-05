ALTER TABLE users RENAME COLUMN token TO validation_token;
ALTER TABLE users ADD COLUMN password_token VARCHAR(70);
ALTER TABLE users ADD COLUMN is_company BOOLEAN DEFAULT FALSE;

UPDATE users SET is_company = TRUE WHERE EXISTS(
    SELECT 1 FROM companies WHERE companies.owner_id = users.id
);

UPDATE categories SET icon_name = 'help-circle' WHERE id = 1;
UPDATE categories SET icon_name = 'beer' WHERE id = 2;
UPDATE categories SET icon_name = 'liquor' WHERE id = 3;
UPDATE categories SET icon_name = 'television' WHERE id = 4;
UPDATE categories SET icon_name = 'paw' WHERE id = 5;
UPDATE categories SET icon_name = 'spray-bottle' WHERE id = 6;
UPDATE categories SET icon_name = 'hamburger' WHERE id = 7;

ALTER TABLE documents ADD COLUMN is_public BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE documents
SET is_public = TRUE
WHERE id NOT IN (
    SELECT down_payment_document_id FROM requests WHERE down_payment_document_id IS NOT NULL
    UNION
    SELECT final_payment_document_id FROM requests WHERE final_payment_document_id IS NOT NULL
);

ALTER TABLE users ADD COLUMN preferred_language VARCHAR(5) NOT NULL DEFAULT 'en';

INSERT INTO migrations (id) VALUES (7);