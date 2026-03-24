DROP DATABASE IF EXISTS heptathlon;
CREATE DATABASE heptathlon;
USE heptathlon;

CREATE TABLE IF NOT EXISTS products (
    reference VARCHAR(50) PRIMARY KEY,
    family VARCHAR(100) NOT NULL,
    unit_price DOUBLE NOT NULL,
    stock_quantity INT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS invoices (
    id INT AUTO_INCREMENT PRIMARY KEY,
    client_name VARCHAR(120) NOT NULL,
    total_amount DOUBLE NOT NULL,
    payment_mode VARCHAR(20) NULL,
    billing_date DATE NOT NULL,
    paid BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS invoice_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    invoice_id INT NOT NULL,
    product_reference VARCHAR(50) NOT NULL,
    quantity INT NOT NULL,
    unit_price DOUBLE NOT NULL,
    CONSTRAINT fk_invoice_items_invoice
        FOREIGN KEY (invoice_id) REFERENCES invoices(id),
    CONSTRAINT fk_invoice_items_product
        FOREIGN KEY (product_reference) REFERENCES products(reference)
);

INSERT INTO products (reference, family, unit_price, stock_quantity)
VALUES
    ('BALLON-001', 'football', 25.99, 20),
    ('BALLON-002', 'football', 19.99, 0),
    ('BALLON-003', 'football', 34.99, 7),
    ('BALLON-004', 'football', 29.99, NULL),
    ('RAQUETTE-002', 'tennis', 79.90, 10),
    ('RAQUETTE-003', 'tennis', 99.90, 0),
    ('TAPIS-003', 'fitness', 39.50, 15),
    ('TAPIS-004', 'fitness', 49.90, 0),
    ('HALTERE-001', 'musculation', 15.00, 12),
    ('HALTERE-002', 'musculation', 29.00, 0),
    ('VELO-001', 'cardio', 499.99, 3)
ON DUPLICATE KEY UPDATE
    family = VALUES(family),
    unit_price = VALUES(unit_price),
    stock_quantity = VALUES(stock_quantity);
