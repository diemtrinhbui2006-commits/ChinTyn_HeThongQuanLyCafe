-- He thong quan ly quan cafe
-- MySQL 8.x / MariaDB 10.4+
-- Chay toan bo tep nay truoc khi khoi dong ung dung.

CREATE DATABASE IF NOT EXISTS cafe
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE cafe;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS invoice_details;
DROP TABLE IF EXISTS invoices;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS cafe_tables;
DROP TABLE IF EXISTS customers;
DROP TABLE IF EXISTS employees;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE employees (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    position VARCHAR(100) NOT NULL,
    salary DECIMAL(14, 2) NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_employees_email (email),
    CONSTRAINT chk_employees_salary CHECK (salary >= 0)
) ENGINE = InnoDB;

CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    employee_id BIGINT,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(50) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_username (username),
    UNIQUE KEY uk_users_employee (employee_id),
    CONSTRAINT fk_users_employee
        FOREIGN KEY (employee_id) REFERENCES employees (id)
        ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE = InnoDB;

CREATE TABLE customers (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    address VARCHAR(255),
    PRIMARY KEY (id),
    KEY idx_customers_phone (phone)
) ENGINE = InnoDB;

CREATE TABLE categories (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    PRIMARY KEY (id),
    UNIQUE KEY uk_categories_name (name)
) ENGINE = InnoDB;

CREATE TABLE products (
    id BIGINT NOT NULL AUTO_INCREMENT,
    category_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    price DECIMAL(14, 2) NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_products_name (name),
    KEY idx_products_category (category_id),
    CONSTRAINT chk_products_price CHECK (price > 0),
    CONSTRAINT chk_products_quantity CHECK (quantity >= 0),
    CONSTRAINT fk_products_category
        FOREIGN KEY (category_id) REFERENCES categories (id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE = InnoDB;

CREATE TABLE cafe_tables (
    id BIGINT NOT NULL AUTO_INCREMENT,
    table_number INT NOT NULL,
    capacity INT NOT NULL DEFAULT 1,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    PRIMARY KEY (id),
    UNIQUE KEY uk_cafe_tables_number (table_number),
    KEY idx_cafe_tables_status (status),
    CONSTRAINT chk_cafe_tables_number CHECK (table_number > 0),
    CONSTRAINT chk_cafe_tables_capacity CHECK (capacity > 0),
    CONSTRAINT chk_cafe_tables_status
        CHECK (status IN ('AVAILABLE', 'OCCUPIED', 'RESERVED', 'OUT_OF_SERVICE'))
) ENGINE = InnoDB;

CREATE TABLE invoices (
    id BIGINT NOT NULL AUTO_INCREMENT,
    table_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    customer_id BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    paid_at DATETIME,
    subtotal DECIMAL(14, 2) NOT NULL DEFAULT 0,
    discount DECIMAL(14, 2) NOT NULL DEFAULT 0,
    total_amount DECIMAL(14, 2) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    payment_method VARCHAR(20),
    PRIMARY KEY (id),
    KEY idx_invoices_table_status (table_id, status),
    KEY idx_invoices_employee (employee_id),
    KEY idx_invoices_customer (customer_id),
    KEY idx_invoices_created_at (created_at),
    CONSTRAINT chk_invoices_money
        CHECK (subtotal >= 0 AND discount >= 0 AND total_amount >= 0),
    CONSTRAINT chk_invoices_status
        CHECK (status IN ('OPEN', 'PAID', 'CANCELLED')),
    CONSTRAINT chk_invoices_payment_method
        CHECK (payment_method IS NULL OR payment_method IN ('CASH', 'BANK_TRANSFER', 'CARD', 'E_WALLET')),
    CONSTRAINT fk_invoices_table
        FOREIGN KEY (table_id) REFERENCES cafe_tables (id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_invoices_employee
        FOREIGN KEY (employee_id) REFERENCES employees (id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_invoices_customer
        FOREIGN KEY (customer_id) REFERENCES customers (id)
        ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE = InnoDB;

CREATE TABLE invoice_details (
    id BIGINT NOT NULL AUTO_INCREMENT,
    invoice_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(14, 2) NOT NULL,
    line_total DECIMAL(14, 2) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_invoice_detail_invoice_product (invoice_id, product_id),
    KEY idx_invoice_details_product (product_id),
    CONSTRAINT chk_invoice_details_quantity CHECK (quantity > 0),
    CONSTRAINT chk_invoice_details_unit_price CHECK (unit_price >= 0),
    CONSTRAINT chk_invoice_details_line_total CHECK (line_total >= 0),
    CONSTRAINT fk_invoice_details_invoice
        FOREIGN KEY (invoice_id) REFERENCES invoices (id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_invoice_details_product
        FOREIGN KEY (product_id) REFERENCES products (id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE = InnoDB;

-- Du lieu mau toi thieu de kiem tra he thong.
INSERT INTO employees (name, email, phone, position, salary) VALUES
('Nguyen Van Quan Ly', 'quanly@cafe.local', '0900000001', 'MANAGER', 12000000),
('Tran Thi Nhan Vien', 'nhanvien@cafe.local', '0900000002', 'STAFF', 7000000);

INSERT INTO categories (name, description) VALUES
('Ca phe', 'Cac mon ca phe Viet Nam va ca phe may'),
('Tra', 'Tra trai cay va tra sua'),
('Nuoc khac', 'Nuoc ep, sinh to va nuoc dong chai');

INSERT INTO products (category_id, name, description, price, quantity) VALUES
(1, 'Ca phe den', 'Ca phe den pha phin', 20000, 100),
(1, 'Ca phe sua', 'Ca phe sua da', 25000, 100),
(2, 'Tra dao', 'Tra dao cam sa', 35000, 100),
(3, 'Nuoc suoi', 'Chai 500 ml', 10000, 100);

INSERT INTO cafe_tables (table_number, capacity, status) VALUES
(1, 2, 'AVAILABLE'),
(2, 4, 'AVAILABLE'),
(3, 4, 'AVAILABLE'),
(4, 6, 'AVAILABLE'),
(5, 8, 'OUT_OF_SERVICE');
