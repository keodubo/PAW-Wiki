ALTER TABLE pools ALTER COLUMN status TYPE varchar(12);
ALTER TABLE requests ALTER COLUMN status TYPE varchar(12);

UPDATE pools SET status = (
    CASE
        WHEN status = '0' THEN 'AVAILABLE'
        WHEN status = '1' THEN 'DELIVERING'
        WHEN status = '2' THEN 'PAUSED'
        WHEN status = '3' THEN 'CANCELLED'
        WHEN status = '4' THEN 'FINISHED'
    END
);

UPDATE requests SET status = (
    CASE
        WHEN status = '0' THEN 'PENDING'
        WHEN status = '1' THEN 'ACCEPTED'
        WHEN status = '2' THEN 'REJECTED'
        WHEN status = '3' THEN 'DELIVERED'
    END
);

CREATE INDEX idx_companies_document_id ON companies(document_id);
CREATE INDEX idx_companies_owner_id ON companies(owner_id);
CREATE INDEX idx_products_document_id ON products(document_id);
CREATE INDEX idx_products_company_id ON products(company_id);
CREATE INDEX idx_products_category_id ON products(category_id);
CREATE INDEX idx_pools_product_id ON pools(product_id);
CREATE INDEX idx_pools_location_id ON pools(location_id);
CREATE INDEX idx_requests_down_payment_document_id ON requests(down_payment_document_id);
CREATE INDEX idx_requests_final_payment_document_id ON requests(final_payment_document_id);
CREATE INDEX idx_requests_user_id ON requests(user_id);
CREATE INDEX idx_requests_pool_id ON requests(pool_id);
CREATE INDEX idx_reports_company_id ON reports(company_id);
CREATE INDEX idx_reports_user_id ON reports(user_id);
CREATE INDEX idx_reviews_reviewer_id ON reviews(reviewer_id);
CREATE INDEX idx_reviews_product_id ON reviews(product_id);
CREATE INDEX idx_users_location_id ON users(location_id);

INSERT INTO migrations (id) VALUES (8);
