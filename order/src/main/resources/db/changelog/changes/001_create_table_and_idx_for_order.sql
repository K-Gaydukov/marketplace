-- liquibase formatted sql

-- changeset konstantin:1
CREATE TYPE order_status AS ENUM ('NEW', 'PAID', 'SENT', 'COMPLETED',
'CANCELLED');

-- rollback DROP TYPE order_status;

-- changeset konstantin:2
CREATE TABLE orders (
id BIGSERIAL PRIMARY KEY,
user_id BIGINT NOT NULL,  -- из JWT uid
user_fio VARCHAR(192) NOT NULL,  -- снапшот ФИО (из JWT fio)
status order_status NOT NULL DEFAULT 'NEW',
total_amount NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (total_amount >= 0),
created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- rollback DROP TABLE orders;

-- changeset konstantin:3
CREATE TABLE order_items (
id BIGSERIAL PRIMARY KEY,
order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
product_id BIGINT NOT NULL,  -- id из Catalog
product_name VARCHAR(128) NOT NULL,  -- снапшот
product_price NUMERIC(12,2) NOT NULL CHECK (product_price >= 0),
quantity INT NOT NULL CHECK (quantity > 0),
line_total NUMERIC(12,2) NOT NULL CHECK (line_total >= 0)
);

-- rollback DROP TABLE order_items;

-- changeset konstantin:4
CREATE INDEX idx_orders_user ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);

-- rollback DROP INDEX idx_orders_user;
-- rollback DROP INDEX idx_orders_status;