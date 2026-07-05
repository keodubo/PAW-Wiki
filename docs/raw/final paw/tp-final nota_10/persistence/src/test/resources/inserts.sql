
INSERT INTO categories(id, name, icon_name)
VALUES  (1, 'category1', 'icon1'),
        (2, 'category2', 'icon2'),
        (3, 'category3', 'icon3');

INSERT INTO locations(id, name)
VALUES  (1, 'location1'),
        (2, 'location2'),
        (3, 'location3');

INSERT INTO documents(id, bytes, filetype, name, is_public)
VALUES  (1, '1111', 'image/png', 'image1', TRUE),
        (2, '2222', 'image/png', 'image2', TRUE),
        (3, '3333', 'image/png', 'image3', TRUE),
        (4, '4444', 'image/png', 'image4', TRUE),
        (5, '5555', 'image/png', 'image5', TRUE),
        (6, '6666', 'image/png', 'image6', TRUE);

INSERT INTO users(id, email, password, first_name, last_name, location_id, validation_token, password_token, is_company, validated, admin, block_level, blocked_until, preferred_language)
VALUES  (1, 'email1', 'password1', 'user1', 'user1', 1, 'a', NULL, FALSE, TRUE, FALSE, 0, NULL, 'en'),
        (2, 'email2', 'password2', 'user2', 'user2', 2, 'b', NULL, FALSE, TRUE, FALSE, 0, NULL, 'en'),
        (3, 'email3', 'password3', 'user3', 'user3', 3, 'c', NULL, FALSE, TRUE, FALSE, 0, NULL, 'en'),
        (4, 'email4', 'password4', 'user4', 'user4', 1, 'd', NULL, FALSE, TRUE, FALSE, 0, NULL, 'en'),
        (5, 'email5', 'password5', 'user5', 'user5', 2, 'e', NULL, FALSE, FALSE, FALSE, 0, NULL, 'en'),
        (6, 'email6', 'password6', 'user6', 'user6', 3, 'f', NULL, FALSE, TRUE, TRUE, 0, NULL, 'en');

INSERT INTO companies(id, name, address, email, phone, validated, owner_id, document_id, cbu)
VALUES  (1, 'company1', 'address1', 'companyemail1', 'phone1', TRUE, 1, 1, 'cbu1'),
        (2, 'company2', 'address2', 'companyemail2', 'phone2', FALSE, 2, 2, 'cbu2');

INSERT INTO products(id, name, description, price, company_id, document_id, category_id, active)
VALUES  (1, 'product1', 'description1', 100, 1, 3, 1, TRUE),
        (2, 'product2', 'description2', 200, 1, 3, 2, TRUE),
        (3, 'product3', 'description3', 300, 1, 3, 3, FALSE);

INSERT INTO pools(id, min_quantity, product_id, location_id, status, created_at, down_payment, price)
VALUES  (1, 100, 1, 1, 'AVAILABLE', current_timestamp, 0, 100),
        (2, 100, 1, 1, 'DELIVERING', current_timestamp, 5, 200),
        (3, 100, 1, 1, 'FINISHED', current_timestamp, 10, 300);

INSERT INTO requests(id, quantity, user_id, pool_id, status, down_payment_document_id, final_payment_document_id)
VALUES  (1, 10, 3, 3, 'DELIVERED', 3, 4),
        (2, 10, 4, 3, 'DELIVERED', 5, 6),
        (3, 10, 3, 2, 'ACCEPTED', 3, NULL),
        (4, 10, 4, 2, 'ACCEPTED', 5, NULL),
        (5, 10, 3, 1, 'PENDING', NULL, NULL),
        (6, 10, 4, 1, 'PENDING', NULL, NULL);

INSERT INTO reports(id, description, created_at, user_reported, company_id, user_id)
VALUES  (1, 'description1', current_timestamp, TRUE, 1, 3),
        (2, 'description2', DATEADD(ms, 500, current_timestamp), TRUE, 1, 4);

INSERT INTO reviews(id, description, rating, product_id, reviewer_id, created_at)
VALUES  (1, 'description1', 5, 3, 3, current_timestamp),
        (2, 'description2', 4, 3, 4, current_timestamp);