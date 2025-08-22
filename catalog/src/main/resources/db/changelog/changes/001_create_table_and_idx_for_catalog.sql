-- liquibase formatted sql

-- changeset konstantin:1
CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(128) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- rollback DROP TABLE categories;

-- changeset konstantin:2
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    sku VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    description TEXT,
    price NUMERIC(12, 2) NOT NULL CHECK(price >= 0),
    stock INT NOT NULL DEFAULT 0 CHECK(stock >= 0),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    category_id BIGINT REFERENCES categories(id) ON DELETE RESTRICT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- rollback DROP TABLE products;

-- changeset konstantin:3
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_name ON products(name);

-- rollback DROP INDEX idx_products_category;
-- rollback DROP INDEX idx_products_name;