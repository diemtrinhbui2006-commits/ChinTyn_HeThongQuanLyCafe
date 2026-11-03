-- =======================================================
-- HỆ THỐNG QUẢN LÝ BÁN HÀNG & QUÁN CAFE (CHINTYN COFFEE)
-- Cơ Sở Dữ Liệu: MySQL 8.x / MariaDB 10.4+ (XAMPP)
-- Bộ mã ký tự: utf8mb4_unicode_ci (Hỗ trợ tiếng Việt đầy đủ)
-- Dữ liệu mẫu chuẩn hóa: >= 10 dòng cho mỗi bảng chức năng
-- =======================================================

CREATE DATABASE IF NOT EXISTS `cafe`
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE `cafe`;

-- 1. XÓA BẢNG CŨ THEO THỨ TỰ RÀNG BUỘC KHÓA NGOẠI
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS `loyalty_entries`;
DROP TABLE IF EXISTS `loyalty_rewards`;
DROP TABLE IF EXISTS `qr_guest_orders`;
DROP TABLE IF EXISTS `invoice_details`;
DROP TABLE IF EXISTS `invoices`;
DROP TABLE IF EXISTS `expenses`;
DROP TABLE IF EXISTS `stock_movements`;
DROP TABLE IF EXISTS `ingredients`;
DROP TABLE IF EXISTS `suppliers`;
DROP TABLE IF EXISTS `table_reservations`;
DROP TABLE IF EXISTS `work_shifts`;
DROP TABLE IF EXISTS `promotions`;
DROP TABLE IF EXISTS `users`;
DROP TABLE IF EXISTS `products`;
DROP TABLE IF EXISTS `categories`;
DROP TABLE IF EXISTS `cafe_tables`;
DROP TABLE IF EXISTS `customers`;
DROP TABLE IF EXISTS `employees`;
SET FOREIGN_KEY_CHECKS = 1;

-- =======================================================
-- 2. ĐỊNH NGHĨA CẤU TRÚC BẢNG (DDL)
-- =======================================================

-- 2.1. BẢNG NHÂN VIÊN (EMPLOYEES)
CREATE TABLE `employees` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL,
    `email` VARCHAR(100),
    `phone` VARCHAR(20),
    `position` VARCHAR(100) NOT NULL,
    `salary` DECIMAL(14, 2) NOT NULL DEFAULT 0.00,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_employees_email` (`email`),
    CONSTRAINT `chk_employees_salary` CHECK (`salary` >= 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 2.2. BẢNG TÀI KHOẢN NGƯỜI DÙNG (USERS)
-- Mật khẩu hash chuẩn PBKDF2WithHmacSHA256 (Mật khẩu mặc định: 123456)
CREATE TABLE `users` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `employee_id` BIGINT,
    `username` VARCHAR(50) NOT NULL,
    `password` VARCHAR(100) NOT NULL,
    `role` VARCHAR(50) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_users_username` (`username`),
    UNIQUE KEY `uk_users_employee` (`employee_id`),
    CONSTRAINT `fk_users_employee`
        FOREIGN KEY (`employee_id`) REFERENCES `employees` (`id`)
        ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 2.3. BẢNG KHÁCH HÀNG (CUSTOMERS)
CREATE TABLE `customers` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL,
    `email` VARCHAR(100),
    `phone` VARCHAR(20),
    `address` VARCHAR(255),
    `loyalty_points` INT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_customers_phone` (`phone`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 2.4. BẢNG BÀN ĂN (CAFE_TABLES)
CREATE TABLE `cafe_tables` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `table_number` INT NOT NULL,
    `capacity` INT NOT NULL DEFAULT 1,
    `status` VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_cafe_tables_number` (`table_number`),
    KEY `idx_cafe_tables_status` (`status`),
    CONSTRAINT `chk_cafe_tables_number` CHECK (`table_number` > 0),
    CONSTRAINT `chk_cafe_tables_capacity` CHECK (`capacity` > 0),
    CONSTRAINT `chk_cafe_tables_status`
        CHECK (`status` IN ('AVAILABLE', 'OCCUPIED', 'RESERVED', 'OUT_OF_SERVICE'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 2.5. BẢNG DANH MỤC SẢN PHẨM (CATEGORIES)
CREATE TABLE `categories` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL,
    `description` VARCHAR(500),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_categories_name` (`name`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 2.6. BẢNG SẢN PHẨM / THỰC ĐƠN (PRODUCTS)
CREATE TABLE `products` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `category_id` BIGINT NOT NULL,
    `name` VARCHAR(100) NOT NULL,
    `description` VARCHAR(500),
    `price` DECIMAL(14, 2) NOT NULL,
    `quantity` INT NOT NULL DEFAULT 0,
    `available` BOOLEAN NOT NULL DEFAULT TRUE,
    `deleted` BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (`id`),
    KEY `idx_products_name` (`name`),
    KEY `idx_products_category` (`category_id`),
    CONSTRAINT `chk_products_price` CHECK (`price` > 0),
    CONSTRAINT `chk_products_quantity` CHECK (`quantity` >= 0),
    CONSTRAINT `fk_products_category`
        FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 2.7. BẢNG ĐẶT BÀN TRƯỚC (TABLE_RESERVATIONS)
CREATE TABLE `table_reservations` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `cafe_table_id` BIGINT NOT NULL,
    `customer_name` VARCHAR(100) NOT NULL,
    `phone` VARCHAR(20),
    `reserved_for` DATETIME NOT NULL,
    `party_size` INT NOT NULL,
    `status` VARCHAR(20) NOT NULL DEFAULT 'BOOKED',
    `note` VARCHAR(500),
    PRIMARY KEY (`id`),
    KEY `idx_reservations_table` (`cafe_table_id`),
    KEY `idx_reservations_status` (`status`),
    CONSTRAINT `fk_reservations_table`
        FOREIGN KEY (`cafe_table_id`) REFERENCES `cafe_tables` (`id`)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 2.8. BẢNG CA LÀM VIỆC & ĐỐI SOÁT KÉT (WORK_SHIFTS)
CREATE TABLE `work_shifts` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `employee_id` BIGINT NOT NULL,
    `opened_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `closed_at` DATETIME,
    `opening_cash` DECIMAL(14, 2) NOT NULL DEFAULT 0.00,
    `closing_cash` DECIMAL(14, 2),
    `expected_cash` DECIMAL(14, 2),
    `note` VARCHAR(500),
    PRIMARY KEY (`id`),
    KEY `idx_work_shifts_employee` (`employee_id`),
    CONSTRAINT `fk_work_shifts_employee`
        FOREIGN KEY (`employee_id`) REFERENCES `employees` (`id`)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 2.9. BẢNG NHÀ CUNG CẤP (SUPPLIERS)
CREATE TABLE `suppliers` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(120) NOT NULL,
    `contact_name` VARCHAR(100),
    `phone` VARCHAR(30),
    `email` VARCHAR(150),
    `address` VARCHAR(500),
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 2.10. BẢNG NGUYÊN VẬT LIỆU KHO (INGREDIENTS)
CREATE TABLE `ingredients` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(120) NOT NULL,
    `unit` VARCHAR(30) NOT NULL,
    `quantity` DECIMAL(14, 3) NOT NULL DEFAULT 0.000,
    `low_stock_threshold` DECIMAL(14, 3) NOT NULL DEFAULT 0.000,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 2.11. BẢNG BIẾN ĐỘNG KHO (STOCK_MOVEMENTS)
CREATE TABLE `stock_movements` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `ingredient_id` BIGINT NOT NULL,
    `quantity` DECIMAL(14, 3) NOT NULL,
    `movement_type` VARCHAR(20) NOT NULL,
    `note` VARCHAR(500),
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_stock_movements_ingredient` (`ingredient_id`),
    CONSTRAINT `fk_stock_movements_ingredient`
        FOREIGN KEY (`ingredient_id`) REFERENCES `ingredients` (`id`)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 2.12. BẢNG CHI PHÍ HOẠT ĐỘNG (EXPENSES)
CREATE TABLE `expenses` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `description` VARCHAR(120) NOT NULL,
    `category` VARCHAR(80) NOT NULL,
    `amount` DECIMAL(14, 2) NOT NULL,
    `payment_method` VARCHAR(20) NOT NULL DEFAULT 'CASH',
    `supplier_id` BIGINT,
    `ingredient_id` BIGINT,
    `stock_quantity` DECIMAL(14, 3),
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_expenses_supplier` (`supplier_id`),
    KEY `idx_expenses_ingredient` (`ingredient_id`),
    CONSTRAINT `fk_expenses_supplier`
        FOREIGN KEY (`supplier_id`) REFERENCES `suppliers` (`id`)
        ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT `fk_expenses_ingredient`
        FOREIGN KEY (`ingredient_id`) REFERENCES `ingredients` (`id`)
        ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 2.13. BẢNG MÃ KHUYẾN MÃI (PROMOTIONS)
CREATE TABLE `promotions` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `code` VARCHAR(40) NOT NULL,
    `name` VARCHAR(120) NOT NULL,
    `discount_type` VARCHAR(12) NOT NULL DEFAULT 'PERCENT',
    `discount_value` DECIMAL(14, 2) NOT NULL,
    `minimum_spend` DECIMAL(14, 2),
    `starts_at` DATETIME,
    `ends_at` DATETIME,
    `active` BOOLEAN NOT NULL DEFAULT TRUE,
    `usage_limit` INT,
    `usage_count` INT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_promotions_code` (`code`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 2.14. BẢNG QUÀ TẶNG ĐỔI ĐIỂM (LOYALTY_REWARDS)
CREATE TABLE `loyalty_rewards` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(120) NOT NULL,
    `description` VARCHAR(500),
    `points_cost` INT NOT NULL,
    `active` BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 2.15. BẢNG HÓA ĐƠN BÁN HÀNG (INVOICES)
CREATE TABLE `invoices` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `table_id` BIGINT NOT NULL,
    `employee_id` BIGINT NOT NULL,
    `customer_id` BIGINT,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `paid_at` DATETIME,
    `subtotal` DECIMAL(14, 2) NOT NULL DEFAULT 0.00,
    `discount` DECIMAL(14, 2) NOT NULL DEFAULT 0.00,
    `total_amount` DECIMAL(14, 2) NOT NULL DEFAULT 0.00,
    `promotion_code` VARCHAR(40),
    `status` VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    `payment_method` VARCHAR(20),
    PRIMARY KEY (`id`),
    KEY `idx_invoices_table_status` (`table_id`, `status`),
    KEY `idx_invoices_employee` (`employee_id`),
    KEY `idx_invoices_customer` (`customer_id`),
    KEY `idx_invoices_created_at` (`created_at`),
    CONSTRAINT `chk_invoices_money`
        CHECK (`subtotal` >= 0 AND `discount` >= 0 AND `total_amount` >= 0),
    CONSTRAINT `chk_invoices_status`
        CHECK (`status` IN ('OPEN', 'PAID', 'CANCELLED')),
    CONSTRAINT `chk_invoices_payment_method`
        CHECK (`payment_method` IS NULL OR `payment_method` IN ('CASH', 'BANK_TRANSFER', 'CARD', 'E_WALLET')),
    CONSTRAINT `fk_invoices_table`
        FOREIGN KEY (`table_id`) REFERENCES `cafe_tables` (`id`)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT `fk_invoices_employee`
        FOREIGN KEY (`employee_id`) REFERENCES `employees` (`id`)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT `fk_invoices_customer`
        FOREIGN KEY (`customer_id`) REFERENCES `customers` (`id`)
        ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 2.16. BẢNG CHI TIẾT HÓA ĐƠN (INVOICE_DETAILS)
CREATE TABLE `invoice_details` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `invoice_id` BIGINT NOT NULL,
    `product_id` BIGINT NOT NULL,
    `quantity` INT NOT NULL,
    `unit_price` DECIMAL(14, 2) NOT NULL,
    `line_total` DECIMAL(14, 2) NOT NULL,
    `sugar_percent` INT DEFAULT 100,
    `ice_option` VARCHAR(10) DEFAULT 'NORMAL',
    `note` VARCHAR(500) DEFAULT '',
    `option_key` VARCHAR(64) NOT NULL DEFAULT 'DEFAULT',
    PRIMARY KEY (`id`),
    KEY `idx_invoice_details_invoice` (`invoice_id`),
    KEY `idx_invoice_details_product` (`product_id`),
    CONSTRAINT `chk_invoice_details_quantity` CHECK (`quantity` > 0),
    CONSTRAINT `chk_invoice_details_unit_price` CHECK (`unit_price` >= 0),
    CONSTRAINT `chk_invoice_details_line_total` CHECK (`line_total` >= 0),
    CONSTRAINT `fk_invoice_details_invoice`
        FOREIGN KEY (`invoice_id`) REFERENCES `invoices` (`id`)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT `fk_invoice_details_product`
        FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 2.17. BẢNG NHẬT KÝ ĐIỂM THƯỞNG (LOYALTY_ENTRIES)
CREATE TABLE `loyalty_entries` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `customer_id` BIGINT NOT NULL,
    `invoice_id` BIGINT,
    `points` INT NOT NULL,
    `entry_type` VARCHAR(20) NOT NULL,
    `description` VARCHAR(250),
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_loyalty_customer` (`customer_id`),
    KEY `idx_loyalty_invoice` (`invoice_id`),
    CONSTRAINT `fk_loyalty_customer`
        FOREIGN KEY (`customer_id`) REFERENCES `customers` (`id`)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT `fk_loyalty_invoice`
        FOREIGN KEY (`invoice_id`) REFERENCES `invoices` (`id`)
        ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 2.18. BẢNG ĐƠN KHÁCH ĐẶT MÓN QUA QR (QR_GUEST_ORDERS)
CREATE TABLE `qr_guest_orders` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `token` VARCHAR(64) NOT NULL,
    `cafe_table_id` BIGINT NOT NULL,
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    `items_json` VARCHAR(4000) NOT NULL,
    `guest_name` VARCHAR(100),
    `guest_phone` VARCHAR(20),
    `approved_invoice_id` BIGINT,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_qr_guest_orders_token` (`token`),
    KEY `idx_qr_guest_orders_table` (`cafe_table_id`),
    KEY `idx_qr_guest_orders_status` (`status`),
    CONSTRAINT `fk_qr_guest_orders_table`
        FOREIGN KEY (`cafe_table_id`) REFERENCES `cafe_tables` (`id`)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT `fk_qr_guest_orders_invoice`
        FOREIGN KEY (`approved_invoice_id`) REFERENCES `invoices` (`id`)
        ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;


-- =======================================================
-- 3. DỮ LIỆU KHỞI TẠO MẪU (CHUẨN HÓA >= 10 DÒNG MỖI BẢNG)
-- =======================================================

-- 3.1. Danh Sách Nhân Viên (3 nhân viên chuẩn tương ứng 3 vai trò)
INSERT INTO `employees` (`id`, `name`, `email`, `phone`, `position`, `salary`) VALUES
(1, 'Nguyễn Quản Trị', 'admin@cafe.vn', '0912345678', 'MANAGER', 15000000.00),
(2, 'Trần Thu Ngân', 'thungan@cafe.vn', '0987654321', 'CASHIER', 8500000.00),
(3, 'Lê Phục Vụ', 'phucvu@cafe.vn', '0905123456', 'STAFF', 7000000.00);

-- 3.2. Danh Sách Tài Khoản Người Dùng (Đúng 3 tài khoản: admin, cashier, staff - Mật khẩu mặc định: 123456)
-- Hash PBKDF2: pbkdf2$120000$BlSayiU+vb3TgzhMkdPreQ==$QHGBSgDtiLE4Z3HkMHvs1yJypc66q6TTC7A5QGJdnYw=
INSERT INTO `users` (`id`, `employee_id`, `username`, `password`, `role`) VALUES
(1, 1, 'admin', 'pbkdf2$120000$BlSayiU+vb3TgzhMkdPreQ==$QHGBSgDtiLE4Z3HkMHvs1yJypc66q6TTC7A5QGJdnYw=', 'ADMIN'),
(2, 2, 'cashier', 'pbkdf2$120000$BlSayiU+vb3TgzhMkdPreQ==$QHGBSgDtiLE4Z3HkMHvs1yJypc66q6TTC7A5QGJdnYw=', 'CASHIER'),
(3, 3, 'staff', 'pbkdf2$120000$BlSayiU+vb3TgzhMkdPreQ==$QHGBSgDtiLE4Z3HkMHvs1yJypc66q6TTC7A5QGJdnYw=', 'STAFF');

-- 3.3. Danh Sách Khách Hàng (12 khách hàng)
INSERT INTO `customers` (`id`, `name`, `email`, `phone`, `address`, `loyalty_points`) VALUES
(1, 'Nguyễn Văn An', 'an.nguyen@gmail.com', '0901112222', '123 Nguyễn Huệ, Phường Bến Nghé, Quận 1, TP.HCM', 154),
(2, 'Trần Thị Bình', 'binh.tran@gmail.com', '0903334444', '45 Lê Lợi, Phường Bến Thành, Quận 1, TP.HCM', 294),
(3, 'Phạm Hoàng Cường', 'cuong.pham@gmail.com', '0905556666', '78 Pasteur, Phường Võ Thị Sáu, Quận 3, TP.HCM', 74),
(4, 'Đỗ Mỹ Duyên', 'duyen.do@gmail.com', '0907778888', '12 Nam Kỳ Khởi Nghĩa, Quận 3, TP.HCM', 84),
(5, 'Lê Quốc Hùng', 'hung.le@gmail.com', '0912111333', '88 Hai Bà Trưng, Phường Đa Kao, Quận 1, TP.HCM', 320),
(6, 'Vũ Phương Mai', 'mai.vu@gmail.com', '0913222444', '215 Điện Biên Phủ, Phường 15, Quận Bình Thạnh, TP.HCM', 61),
(7, 'Hoàng Trọng Nghĩa', 'nghia.hoang@gmail.com', '0914333555', '360 Xô Viết Nghệ Tĩnh, Phường 25, Quận Bình Thạnh, TP.HCM', 112),
(8, 'Ngô Thanh Thảo', 'thao.ngo@gmail.com', '0915444666', '54 Võ Văn Tần, Phường 6, Quận 3, TP.HCM', 78),
(9, 'Đinh Gia Bảo', 'bao.dinh@gmail.com', '0916555777', '180 Phan Đăng Lưu, Phường 3, Quận Phú Nhuận, TP.HCM', 450),
(10, 'Lý Nhã Kim', 'kim.ly@gmail.com', '0917666888', '99 Nguyễn Thị Minh Khai, Phường Bến Thành, Quận 1, TP.HCM', 520),
(11, 'Cao Bá Quát', 'quat.cao@gmail.com', '0918777999', '204 Lê Văn Sỹ, Phường 14, Quận 3, TP.HCM', 95),
(12, 'Dương Thúy Vi', 'vi.duong@gmail.com', '0919888000', '15 Trần Hưng Đạo, Phường Cầu Ông Lãnh, Quận 1, TP.HCM', 180);

-- 3.4. Danh Mục Món (4 danh mục chuẩn)
INSERT INTO `categories` (`id`, `name`, `description`) VALUES
(1, 'Cà phê', 'Các sản phẩm cà phê truyền thống Việt Nam và cà phê pha máy'),
(2, 'Trà & Trà Sữa', 'Trà trái cây nhiệt đới tươi mát và trà sữa trân châu đậm vị'),
(3, 'Sinh tố & Đá xay', 'Thức uống hoa quả tươi nguyên chất và đá xay béo mịn mát lạnh'),
(4, 'Bánh ngọt & Tráng miệng', 'Bánh ngọt Âu nướng tươi mỗi ngày dùng kèm trà và cà phê');

-- 3.5. Danh Sách Thực Đơn (18 sản phẩm chi tiết)
INSERT INTO `products` (`id`, `category_id`, `name`, `description`, `price`, `quantity`, `available`) VALUES
-- Nhóm Cà phê
(1, 1, 'Cà phê Đen Đá', 'Cà phê Robusta Đắk Lắk pha phin truyền thống đậm đà.', 25000.00, 100, TRUE),
(2, 1, 'Cà phê Sữa Đá', 'Cà phê phin hòa quyện cùng sữa đặc béo ngậy ngọt ngào.', 29000.00, 100, TRUE),
(3, 1, 'Bạc Xỉu', 'Nhiều sữa đặc ngọt béo hòa cùng một chút cà phê thơm nhẹ.', 29000.00, 100, TRUE),
(4, 1, 'Cappuccino', 'Cà phê Espresso pha máy kiểu Ý kết hợp lớp bọt sữa bông mịn màng.', 45000.00, 100, TRUE),
(5, 1, 'Cà phê Cốt Dừa', 'Cà phê đậm vị hòa cùng cốt dừa đá xay thơm bùi mát lạnh.', 39000.00, 100, TRUE),
(6, 1, 'Cà phê Muối', 'Vị đắng cà phê phối hợp lớp kem mặn béo ngậy độc đáo xứ Huế.', 35000.00, 100, TRUE),

-- Nhóm Trà & Trà Sữa
(7, 2, 'Trà sữa Trân châu truyền thống', 'Trà đen đậm đà kết hợp trân châu đen dai giòn ngọt dịu.', 35000.00, 100, TRUE),
(8, 2, 'Trà sữa Matcha', 'Matcha Nhật Bản thượng hạng thơm thanh hòa quyện sữa tươi.', 39000.00, 100, TRUE),
(9, 2, 'Trà sữa Ô Long', 'Vị trà Ô Long nướng thơm lừng đậm đà hậu vị ngọt thanh.', 39000.00, 100, TRUE),
(10, 2, 'Trà Đào Cam Sả', 'Trà đào thanh mát với miếng đào giòn ngọt và hương sả nồng nàn.', 39000.00, 100, TRUE),
(11, 2, 'Trà Vải', 'Trà lài ngát hương kết hợp cùng những trái vải ngọt lịm mọng nước.', 39000.00, 100, TRUE),

-- Nhóm Sinh tố & Đá xay
(12, 3, 'Sinh tố Bơ', 'Bơ sáp Đắk Lắk dẻo thơm xay sữa đặc béo ngậy.', 45000.00, 100, TRUE),
(13, 3, 'Matcha Đá Xay', 'Bột matcha xay đá tuyết mát lạnh phủ kem tươi mềm mịn.', 49000.00, 100, TRUE),
(14, 3, 'Nước Cam Ép', '100% cam sành vắt tươi nguyên chất giàu vitamin C.', 35000.00, 100, TRUE),

-- Nhóm Bánh ngọt
(15, 4, 'Bánh Tiramisu', 'Bánh ngọt truyền thống kiểu Ý đượm vị cà phê và phô mai mềm tan.', 35000.00, 50, TRUE),
(16, 4, 'Bánh Phô Mai Việt Quất', 'Cheesecake nướng béo ngậy phủ sốt mứt việt quất chua ngọt.', 42000.00, 50, TRUE),
(17, 4, 'Bánh Mousse Chanh Dây', 'Lớp mousse mịn màng cùng sốt chanh dây tươi chua thanh kích thích vị giác.', 38000.00, 50, TRUE),
(18, 4, 'Croissant Bơ Tỏi', 'Bánh sừng bò ngàn lớp nướng giòn rụm thơm phức bơ tỏi.', 29000.00, 50, TRUE);

-- 3.6. Sơ Đồ Bàn Ăn (10 bàn đa dạng sức chứa, có bàn RESERVED để test giao diện viền vàng nét đứt)
INSERT INTO `cafe_tables` (`id`, `table_number`, `capacity`, `status`) VALUES
(1, 1, 2, 'AVAILABLE'),
(2, 2, 4, 'AVAILABLE'),
(3, 3, 4, 'RESERVED'),
(4, 4, 6, 'AVAILABLE'),
(5, 5, 4, 'OCCUPIED'),
(6, 6, 2, 'AVAILABLE'),
(7, 7, 4, 'RESERVED'),
(8, 8, 4, 'AVAILABLE'),
(9, 9, 6, 'AVAILABLE'),
(10, 10, 8, 'AVAILABLE');

-- 3.7. Danh Sách Đặt Bàn Trước (10 đơn đặt bàn)
INSERT INTO `table_reservations` (`id`, `cafe_table_id`, `customer_name`, `phone`, `reserved_for`, `party_size`, `status`, `note`) VALUES
(1, 3, 'Nguyễn Văn An', '0901112222', DATE_ADD(NOW(), INTERVAL 2 HOUR), 4, 'BOOKED', 'Khách hẹn sinh nhật, trang trí nhẹ'),
(2, 7, 'Trần Thị Bình', '0903334444', DATE_ADD(NOW(), INTERVAL 4 HOUR), 3, 'BOOKED', 'Khách ưu tiên vị trí gần cửa sổ'),
(3, 1, 'Lê Quốc Hùng', '0912111333', DATE_ADD(NOW(), INTERVAL 1 DAY), 2, 'BOOKED', 'Bàn 2 người bàn việc công chứng'),
(4, 4, 'Vũ Phương Mai', '0913222444', DATE_ADD(NOW(), INTERVAL 1 DAY), 6, 'BOOKED', 'Nhóm bạn họp lớp đại học'),
(5, 10, 'Hoàng Trọng Nghĩa', '0914333555', DATE_ADD(NOW(), INTERVAL 2 DAY), 8, 'BOOKED', 'Tiệc liên hoan phòng kinh doanh'),
(6, 2, 'Ngô Thanh Thảo', '0915444666', DATE_SUB(NOW(), INTERVAL 1 DAY), 4, 'COMPLETED', 'Khách đã dùng xong và thanh toán'),
(7, 5, 'Đinh Gia Bảo', '0916555777', DATE_SUB(NOW(), INTERVAL 2 DAY), 4, 'COMPLETED', 'Đúng giờ, dịch vụ tốt'),
(8, 6, 'Lý Nhã Kim', '0917666888', DATE_SUB(NOW(), INTERVAL 3 DAY), 2, 'CANCELLED', 'Khách báo bận việc đột xuất nên hủy'),
(9, 8, 'Cao Bá Quát', '0918777999', DATE_ADD(NOW(), INTERVAL 3 DAY), 4, 'BOOKED', 'Hẹn gặp đối tác ký hợp đồng'),
(10, 9, 'Dương Thúy Vi', '0919888000', DATE_ADD(NOW(), INTERVAL 4 DAY), 5, 'BOOKED', 'Cần chuẩn bị trước 1 đĩa bánh ngọt');

-- 3.8. Danh Sách Ca Làm Việc & Đối Soát Két (10 ca làm việc gần đây)
INSERT INTO `work_shifts` (`id`, `employee_id`, `opened_at`, `closed_at`, `opening_cash`, `closing_cash`, `expected_cash`, `note`) VALUES
(1, 2, DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 5 DAY), INTERVAL 8 HOUR), 1000000.00, 4250000.00, 4250000.00, 'Ca sáng chuẩn, tiền két khớp hoàn toàn'),
(2, 2, DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 4 DAY), INTERVAL 8 HOUR), 1000000.00, 5600000.00, 5600000.00, 'Ca chiều đông khách, đối soát đủ tiền'),
(3, 2, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 3 DAY), INTERVAL 8 HOUR), 1000000.00, 3800000.00, 3800000.00, 'Ca sáng thứ hai đầu tuần ổn định'),
(4, 2, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 3 DAY), INTERVAL 8 HOUR), 1000000.00, 6100000.00, 6100000.00, 'Ca tối chạy chương trình khuyến mãi'),
(5, 2, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 2 DAY), INTERVAL 8 HOUR), 1000000.00, 4950000.00, 4950000.00, 'Két tiền chuẩn xác 100%'),
(6, 2, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 2 DAY), INTERVAL 8 HOUR), 1000000.00, 5200000.00, 5200000.00, 'Ca chiều kiểm ngân hoàn thành tốt'),
(7, 2, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 1 DAY), INTERVAL 8 HOUR), 1000000.00, 4700000.00, 4700000.00, 'Ca sáng chủ nhật lượng khách vừa phải'),
(8, 2, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 1 DAY), INTERVAL 8 HOUR), 1000000.00, 6850000.00, 6850000.00, 'Ca tối chủ nhật doanh thu cao nhất tuần'),
(9, 2, DATE_SUB(NOW(), INTERVAL 8 HOUR), DATE_SUB(NOW(), INTERVAL 1 HOUR), 1000000.00, 3950000.00, 3950000.00, 'Ca sáng hôm nay đã bàn giao két'),
(10, 2, DATE_SUB(NOW(), INTERVAL 1 HOUR), NULL, 1000000.00, NULL, 1000000.00, 'Ca chiều hiện tại đang mở phục vụ khách');

-- 3.9. Danh Sách Nhà Cung Cấp (10 đối tác cung ứng uy tín)
INSERT INTO `suppliers` (`id`, `name`, `contact_name`, `phone`, `email`, `address`) VALUES
(1, 'Công ty TNHH Cà Phê Cầu Đất Farm', 'Nguyễn Hoàng Nam', '02633888999', 'caudatfarm@coffee.vn', 'Thôn Trường Thọ, Xã Trạm Hành, TP. Đà Lạt, Lâm Đồng'),
(2, 'Công ty Cổ Phần Sữa Vinamilk', 'Trần Phương Thảo', '02854155555', 'vinamilk.b2b@vinamilk.com.vn', '10 Tân Trào, Phường Tân Phú, Quận 7, TP.HCM'),
(3, 'Công ty TNHH Trà & Cà phê Phúc Long', 'Lê Hữu Đạt', '02838323456', 'order@phuclong.com.vn', '42/24 - 42/26 Đường 643 Tạ Quang Bửu, Phường 4, Quận 8, TP.HCM'),
(4, 'Hợp Tác Xã Nông Sản Sạch Đà Lạt', 'Phạm Văn Hưng', '02633555777', 'nongsandalat@freshfarm.vn', 'Đường Vạn Hạnh, Phường 8, TP. Đà Lạt, Lâm Đồng'),
(5, 'Công ty TNHH Bao Bì Xanh Eco Cup', 'Vũ Thị Thanh', '02862778899', 'lienhe@ecocup.vn', 'Lô C2, Đường số 5, KCN Tân Bình, Quận Tân Phú, TP.HCM'),
(6, 'Công ty TNHH Tân Nhất Hương Bakery', 'Đỗ Thành Vinh', '02862903738', 'sales@tannhathuong.com', '61A Trần Quang Diệu, Phường 13, Quận 3, TP.HCM'),
(7, 'Công ty TNHH Fonterra Brands Vietnam (Anchor)', 'Nguyễn Lan Hương', '02838243388', 'anchor.vn@fonterra.com', 'Tòa nhà Bitexco, 2 Hải Triều, Quận 1, TP.HCM'),
(8, 'Công ty Nước Đá Tinh Khiết Sài Gòn', 'Bùi Văn Hậu', '0908123789', 'nuocdasaigon@icemail.vn', '128 Huỳnh Tấn Phát, Phường Tân Thuận Đông, Quận 7, TP.HCM'),
(9, 'Nhà phân phối Nguyên liệu Syrup Monin', 'Trương Vĩnh Kỳ', '02838229988', 'monin.distributor@barista.vn', '89 Nam Kỳ Khởi Nghĩa, Bến Nghé, Quận 1, TP.HCM'),
(10, 'Công ty TNHH Thiết Bị Cafe Chuyên Nghiệp', 'Lê Hoàng Long', '0938765432', 'kythuat@procafemachine.vn', '45 Bis Nguyễn Thị Minh Khai, Phường Bến Nghé, Quận 1, TP.HCM');

-- 3.10. Danh Sách Nguyên Vật Liệu Kho (10 mặt hàng nguyên liệu trọng yếu)
INSERT INTO `ingredients` (`id`, `name`, `unit`, `quantity`, `low_stock_threshold`) VALUES
(1, 'Hạt Cà phê Robusta Đắk Lắk', 'Kg', 45.500, 10.000),
(2, 'Hạt Cà phê Arabica Cầu Đất', 'Kg', 28.000, 8.000),
(3, 'Sữa tươi tiệt trùng không đường Vinamilk', 'Hộp 1L', 120.000, 20.000),
(4, 'Sữa đặc Ngôi Sao Phương Nam', 'Lon', 85.000, 15.000),
(5, 'Trà đen khô hảo hạng', 'Kg', 18.500, 5.000),
(6, 'Bột trà xanh Matcha Uji Nhật Bản', 'Kg', 6.200, 2.000),
(7, 'Đường cát trắng Biên Hòa', 'Kg', 60.000, 15.000),
(8, 'Bơ sáp Đắk Lắk tuyển chọn', 'Kg', 22.000, 5.000),
(9, 'Cam sành Tiền Giang tươi', 'Kg', 35.000, 10.000),
(10, 'Phô mai Mascarpone làm Tiramisu', 'Hộp 500g', 14.000, 4.000);

-- 3.11. Danh Sách Biến Động Nhập / Xuất Kho (10 phiếu luân chuyển kho)
INSERT INTO `stock_movements` (`id`, `ingredient_id`, `quantity`, `movement_type`, `note`, `created_at`) VALUES
(1, 1, 50.000, 'IN', 'Nhập đầu tuần hạt Robusta Đắk Lắk từ Cầu Đất Farm', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(2, 2, 30.000, 'IN', 'Nhập hạt Arabica Cầu Đất phục vụ pha Espresso', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(3, 3, 150.000, 'IN', 'Nhập sữa tươi Vinamilk thùng 12 hộp từ nhà phân phối', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(4, 4, 100.000, 'IN', 'Nhập sữa đặc Phương Nam lon đỏ', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(5, 1, 5.000, 'OUT', 'Xuất cà phê hạt xay phục vụ quầy Barista', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(6, 5, 20.000, 'IN', 'Nhập trà đen nấu trà sữa và trà đào', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(7, 7, 80.000, 'IN', 'Nhập đường cát trắng Biên Hòa bao 50kg', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(8, 8, 30.000, 'IN', 'Nhập bơ sáp tươi phục vụ sinh tố bơ', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(9, 3, 30.000, 'OUT', 'Xuất sữa tươi cho quầy pha chế ca cuối tuần', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(10, 10, 15.000, 'IN', 'Nhập phô mai làm bánh ngọt Tiramisu Âu', DATE_SUB(NOW(), INTERVAL 1 DAY));

-- 3.12. Danh Sách Chi Phí Hoạt Động (10 khoản chi phí vận hành & nhập kho)
INSERT INTO `expenses` (`id`, `description`, `category`, `amount`, `payment_method`, `supplier_id`, `ingredient_id`, `stock_quantity`, `created_at`) VALUES
(1, 'Nhập 50kg Cà phê hạt Robusta Đắk Lắk', 'INGREDIENTS', 6500000.00, 'BANK_TRANSFER', 1, 1, 50.000, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(2, 'Nhập 30kg Cà phê hạt Arabica Cầu Đất cao cấp', 'INGREDIENTS', 7200000.00, 'BANK_TRANSFER', 1, 2, 30.000, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(3, 'Thanh toán tiền điện quán tháng vừa rồi (EVN)', 'UTILITIES', 4850000.00, 'BANK_TRANSFER', NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(4, 'Nhập 150 hộp Sữa tươi Vinamilk tiệt trùng', 'INGREDIENTS', 4500000.00, 'BANK_TRANSFER', 2, 3, 150.000, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(5, 'Mua nước đá tinh khiết phục vụ tuần này', 'UTILITIES', 650000.00, 'CASH', 8, NULL, NULL, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(6, 'Nhập 10.000 ly giấy và ống hút bã mía thân thiện', 'PACKAGING', 3200000.00, 'BANK_TRANSFER', 5, NULL, NULL, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(7, 'Nhập 30kg Bơ sáp dẻo ngon từ Đắk Lắk', 'INGREDIENTS', 1800000.00, 'CASH', 4, 8, 30.000, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(8, 'Bảo dưỡng định kỳ máy pha cà phê Espresso La Marzocco', 'MAINTENANCE', 1500000.00, 'BANK_TRANSFER', 10, NULL, NULL, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(9, 'Thanh toán cước Internet cáp quang Viettel tốc độ cao', 'UTILITIES', 550000.00, 'BANK_TRANSFER', NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(10, 'Nhập nguyên liệu phô mai Mascarpone làm bánh Tiramisu', 'INGREDIENTS', 2100000.00, 'CASH', 6, 10, 15.000, DATE_SUB(NOW(), INTERVAL 1 DAY));

-- 3.13. Danh Sách Mã Khuyến Mãi (10 chương trình voucher đa dạng)
INSERT INTO `promotions` (`id`, `code`, `name`, `discount_type`, `discount_value`, `minimum_spend`, `starts_at`, `ends_at`, `active`, `usage_limit`, `usage_count`) VALUES
(1, 'CHINTYN10', 'Giảm 10% tổng hóa đơn tri ân khách hàng', 'PERCENT', 10.00, 50000.00, DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_ADD(NOW(), INTERVAL 60 DAY), TRUE, 500, 85),
(2, 'KHAITRUONG', 'Giảm 20% mừng khai trương chi nhánh', 'PERCENT', 20.00, 100000.00, DATE_SUB(NOW(), INTERVAL 15 DAY), DATE_ADD(NOW(), INTERVAL 15 DAY), TRUE, 200, 142),
(3, 'COFFEE15K', 'Giảm 15.000đ trực tiếp cho hóa đơn từ 60.000đ', 'FIXED', 15000.00, 60000.00, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_ADD(NOW(), INTERVAL 30 DAY), TRUE, 300, 68),
(4, 'VIPMEMBER', 'Ưu đãi thành viên VIP giảm 15% không giới hạn', 'PERCENT', 15.00, 80000.00, DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_ADD(NOW(), INTERVAL 90 DAY), TRUE, 1000, 210),
(5, 'HAPPYHOUR', 'Khung giờ vàng 14h-17h giảm 25% cà phê', 'PERCENT', 25.00, 40000.00, DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_ADD(NOW(), INTERVAL 30 DAY), TRUE, 150, 43),
(6, 'BANMOI', 'Tặng 10.000đ cho khách hàng đăng ký thành viên mới', 'FIXED', 10000.00, 30000.00, DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_ADD(NOW(), INTERVAL 60 DAY), TRUE, 500, 126),
(7, 'WEEKEND20K', 'Giảm 20.000đ đơn cuối tuần gia đình sum vầy', 'FIXED', 20000.00, 150000.00, DATE_SUB(NOW(), INTERVAL 14 DAY), DATE_ADD(NOW(), INTERVAL 45 DAY), TRUE, 200, 55),
(8, 'SINHNHAT', 'Quà tặng sinh nhật thành viên giảm 30%', 'PERCENT', 30.00, 100000.00, DATE_SUB(NOW(), INTERVAL 60 DAY), DATE_ADD(NOW(), INTERVAL 120 DAY), TRUE, 100, 28),
(9, 'FREESHIP5K', 'Hỗ trợ 5.000đ phí giao hàng lân cận', 'FIXED', 5000.00, 50000.00, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_ADD(NOW(), INTERVAL 30 DAY), TRUE, 300, 94),
(10, 'COMBOBAKERY', 'Giảm 12% khi gọi combo Trà và Bánh ngọt', 'PERCENT', 12.00, 70000.00, DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_ADD(NOW(), INTERVAL 25 DAY), TRUE, 200, 37);

-- 3.14. Danh Sách Quà Tặng Đổi Điểm (10 phần quà hấp dẫn cho khách thân thiết)
INSERT INTO `loyalty_rewards` (`id`, `name`, `description`, `points_cost`, `active`) VALUES
(1, 'Móc khóa da lưu niệm Chintyn Coffee', 'Móc khóa da bò khắc laser logo Chintyn tinh xảo', 100, TRUE),
(2, 'Voucher tiền mặt trị giá 20.000đ', 'Áp dụng giảm trực tiếp vào hóa đơn lần mua tiếp theo', 200, TRUE),
(3, '01 Ly Cà phê Sữa Đá / Đen Đá miễn phí', 'Tùy chọn cà phê pha phin truyền thống thơm ngon', 250, TRUE),
(4, 'Túi vải Canvas Chintyn phong cách vintage', 'Túi vải dệt thân thiện môi trường, đựng vừa tài liệu và laptop', 350, TRUE),
(5, '01 Ly Trà Đào Cam Sả / Trà sữa trân châu', 'Thức uống thanh mát giải nhiệt size L', 350, TRUE),
(6, 'Voucher giảm giá 50.000đ', 'Áp dụng cho mọi đơn hàng tại quầy hoặc mang đi', 450, TRUE),
(7, '01 Phần Bánh ngọt Tiramisu / Cheesecake', 'Bánh tươi làm trong ngày ăn kèm cà phê', 500, TRUE),
(8, 'Bình nước thủy tinh chịu nhiệt Chintyn 500ml', 'Bình nước thủy tinh cao cấp kèm bọc chống nóng sang trọng', 650, TRUE),
(9, 'Ly giữ nhiệt inox 304 Chintyn Coffee 500ml', 'Giữ nhiệt nóng lạnh liên tục 8-12 tiếng chuẩn cao cấp', 900, TRUE),
(10, 'Thẻ đặc quyền 01 tháng miễn phí Up-size ly lớn', 'Nâng size ly từ M lên L hoàn toàn miễn phí trong 30 ngày', 1200, TRUE);

-- 3.15. Danh Sách Hóa Đơn Bán Hàng (10 hóa đơn đa dạng trạng thái & hình thức thanh toán)
INSERT INTO `invoices` (`id`, `table_id`, `employee_id`, `customer_id`, `created_at`, `paid_at`, `subtotal`, `discount`, `total_amount`, `promotion_code`, `status`, `payment_method`) VALUES
(1, 1, 2, 1, DATE_SUB(NOW(), INTERVAL 4 HOUR), DATE_SUB(NOW(), INTERVAL 3 HOUR), 54000.00, 0.00, 54000.00, NULL, 'PAID', 'CASH'),
(2, 2, 2, 2, DATE_SUB(NOW(), INTERVAL 3 HOUR), DATE_SUB(NOW(), INTERVAL 2 HOUR), 105000.00, 10500.00, 94500.00, 'CHINTYN10', 'PAID', 'BANK_TRANSFER'),
(3, 3, 2, 3, DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 1 HOUR), 74000.00, 0.00, 74000.00, NULL, 'PAID', 'CARD'),
(4, 4, 2, 4, DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 1 HOUR), 84000.00, 0.00, 84000.00, NULL, 'PAID', 'E_WALLET'),
(5, 5, 2, 5, DATE_SUB(NOW(), INTERVAL 45 MINUTE), NULL, 113000.00, 0.00, 113000.00, NULL, 'OPEN', NULL),
(6, 6, 2, 6, DATE_SUB(NOW(), INTERVAL 90 MINUTE), DATE_SUB(NOW(), INTERVAL 40 MINUTE), 68000.00, 6800.00, 61200.00, 'CHINTYN10', 'PAID', 'CASH'),
(7, 7, 2, 7, DATE_SUB(NOW(), INTERVAL 75 MINUTE), DATE_SUB(NOW(), INTERVAL 25 MINUTE), 127000.00, 15000.00, 112000.00, 'COFFEE15K', 'PAID', 'BANK_TRANSFER'),
(8, 8, 2, 8, DATE_SUB(NOW(), INTERVAL 60 MINUTE), DATE_SUB(NOW(), INTERVAL 15 MINUTE), 78000.00, 0.00, 78000.00, NULL, 'PAID', 'CARD'),
(9, 9, 2, 9, DATE_SUB(NOW(), INTERVAL 30 MINUTE), DATE_SUB(NOW(), INTERVAL 5 MINUTE), 158000.00, 15800.00, 142200.00, 'CHINTYN10', 'PAID', 'E_WALLET'),
(10, 10, 2, 10, DATE_SUB(NOW(), INTERVAL 20 MINUTE), DATE_SUB(NOW(), INTERVAL 2 MINUTE), 205000.00, 20500.00, 184500.00, 'CHINTYN10', 'PAID', 'BANK_TRANSFER');

-- 3.16. Chi Tiết Các Món Trong Hóa Đơn (22 món trong 10 hóa đơn)
INSERT INTO `invoice_details` (`id`, `invoice_id`, `product_id`, `quantity`, `unit_price`, `line_total`, `sugar_percent`, `ice_option`, `note`, `option_key`) VALUES
-- HĐ 1 (Bàn 1: 1 Cà phê Đen + 1 Cà phê Sữa = 54k)
(1, 1, 1, 1, 25000.00, 25000.00, 100, 'NORMAL', 'Ít đắng', '1-100-NORMAL'),
(2, 1, 2, 1, 29000.00, 29000.00, 70, 'NORMAL', 'Nhiều sữa', '2-70-NORMAL'),

-- HĐ 2 (Bàn 2: 2 Trà sữa trân châu + 1 Bánh Tiramisu = 105k)
(3, 2, 7, 2, 35000.00, 70000.00, 100, 'NORMAL', 'Trân châu giòn', '7-100-NORMAL'),
(4, 2, 15, 1, 35000.00, 35000.00, 100, 'NORMAL', 'Bánh tươi', '15-100-NORMAL'),

-- HĐ 3 (Bàn 3: 1 Bạc Xỉu + 1 Sinh tố bơ = 74k)
(5, 3, 3, 1, 29000.00, 29000.00, 100, 'NORMAL', '', '3-100-NORMAL'),
(6, 3, 12, 1, 45000.00, 45000.00, 70, 'LESS_ICE', 'Ít ngọt', '12-70-LESS_ICE'),

-- HĐ 4 (Bàn 4: 1 Cà phê Muối + 1 Matcha Đá Xay = 84k)
(7, 4, 6, 1, 35000.00, 35000.00, 100, 'NORMAL', 'Kem mặn vừa', '6-100-NORMAL'),
(8, 4, 13, 1, 49000.00, 49000.00, 100, 'NORMAL', 'Thêm kem tươi', '13-100-NORMAL'),

-- HĐ 5 (Bàn 5 - Đang mở: 1 Cappuccino + 1 Trà Đào Cam Sả + 1 Croissant = 113k)
(9, 5, 4, 1, 45000.00, 45000.00, 100, 'NORMAL', 'Tạo hình hoa tuy-líp', '4-100-NORMAL'),
(10, 5, 10, 1, 39000.00, 39000.00, 50, 'NORMAL', 'Miếng đào giòn', '10-50-NORMAL'),
(11, 5, 18, 1, 29000.00, 29000.00, 100, 'NORMAL', 'Hâm nóng giòn bánh', '18-100-NORMAL'),

-- HĐ 6 (Bàn 6: 1 Cà phê Cốt Dừa + 1 Croissant = 68k)
(12, 6, 5, 1, 39000.00, 39000.00, 100, 'NORMAL', '', '5-100-NORMAL'),
(13, 6, 18, 1, 29000.00, 29000.00, 100, 'NORMAL', '', '18-100-NORMAL'),

-- HĐ 7 (Bàn 7: 1 Trà sữa Matcha + 1 Trà Vải + 1 Cheesecake = 127k)
(14, 7, 8, 1, 39000.00, 39000.00, 70, 'NORMAL', 'Matcha thơm đậm', '8-70-NORMAL'),
(15, 7, 11, 1, 39000.00, 39000.00, 50, 'NORMAL', 'Thêm trái vải', '11-50-NORMAL'),
(16, 7, 16, 1, 49000.00, 49000.00, 100, 'NORMAL', 'Ăn tại chỗ', '16-100-NORMAL'),

-- HĐ 8 (Bàn 8: 1 Trà Đào Cam Sả + 1 Bánh Mousse = 78k)
(17, 8, 10, 1, 39000.00, 39000.00, 70, 'NORMAL', '', '10-70-NORMAL'),
(18, 8, 17, 1, 39000.00, 39000.00, 100, 'NORMAL', '', '17-100-NORMAL'),

-- HĐ 9 (Bàn 9: 2 Cà phê Muối + 2 Nước Cam Ép = 140k + 18k = 158k)
(19, 9, 6, 2, 35000.00, 70000.00, 100, 'NORMAL', 'Kem mặn béo ngậy', '6-100-NORMAL'),
(20, 9, 14, 2, 35000.00, 70000.00, 70, 'NO_ICE', 'Cam sành vắt nguyên chất', '14-70-NO_ICE'),
(21, 9, 18, 1, 18000.00, 18000.00, 100, 'NORMAL', '', '18-100-NORMAL'),

-- HĐ 10 (Bàn 10: 2 Trà sữa Ô Long + 2 Cappuccino + 1 Tiramisu = 205k)
(22, 10, 9, 2, 39000.00, 78000.00, 50, 'NORMAL', 'Ô Long nướng thơm', '9-50-NORMAL'),
(23, 10, 4, 2, 45000.00, 90000.00, 100, 'NORMAL', 'Espresso nóng', '4-100-NORMAL'),
(24, 10, 15, 1, 37000.00, 37000.00, 100, 'NORMAL', '', '15-100-NORMAL');

-- 3.17. Nhật Ký Điểm Thưởng Khách Hàng (10 giao dịch tích / đổi điểm)
INSERT INTO `loyalty_entries` (`id`, `customer_id`, `invoice_id`, `points`, `entry_type`, `description`, `created_at`) VALUES
(1, 1, 1, 54, 'EARN', 'Tích điểm tự động từ hóa đơn thanh toán #1 (54.000đ)', DATE_SUB(NOW(), INTERVAL 3 HOUR)),
(2, 2, 2, 94, 'EARN', 'Tích điểm tự động từ hóa đơn thanh toán #2 (94.500đ)', DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(3, 3, 3, 74, 'EARN', 'Tích điểm tự động từ hóa đơn thanh toán #3 (74.000đ)', DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(4, 4, 4, 84, 'EARN', 'Tích điểm tự động từ hóa đơn thanh toán #4 (84.000đ)', DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(5, 1, NULL, -100, 'REDEEM', 'Đổi quà: Móc khóa da lưu niệm Chintyn Coffee', DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(6, 6, 6, 61, 'EARN', 'Tích điểm tự động từ hóa đơn thanh toán #6 (61.200đ)', DATE_SUB(NOW(), INTERVAL 40 MINUTE)),
(7, 7, 7, 112, 'EARN', 'Tích điểm tự động từ hóa đơn thanh toán #7 (112.000đ)', DATE_SUB(NOW(), INTERVAL 25 MINUTE)),
(8, 8, 8, 78, 'EARN', 'Tích điểm tự động từ hóa đơn thanh toán #8 (78.000đ)', DATE_SUB(NOW(), INTERVAL 15 MINUTE)),
(9, 2, NULL, -200, 'REDEEM', 'Đổi quà: Voucher tiền mặt 20.000đ tại quầy', DATE_SUB(NOW(), INTERVAL 30 MINUTE)),
(10, 10, 10, 184, 'EARN', 'Tích điểm tự động từ hóa đơn thanh toán #10 (184.500đ)', DATE_SUB(NOW(), INTERVAL 2 MINUTE));

-- 3.18. Danh Sách Đơn Khách Đặt Món Qua QR (10 yêu cầu gọi món)
INSERT INTO `qr_guest_orders` (`id`, `token`, `cafe_table_id`, `status`, `items_json`, `guest_name`, `guest_phone`, `approved_invoice_id`, `created_at`) VALUES
(1, 'qr_token_table1_001', 1, 'APPROVED', '[{"productId":1,"quantity":1,"sugarPercent":100,"iceOption":"NORMAL"},{"productId":2,"quantity":1,"sugarPercent":70,"iceOption":"NORMAL"}]', 'Nguyễn Văn An', '0901112222', 1, DATE_SUB(NOW(), INTERVAL 4 HOUR)),
(2, 'qr_token_table2_002', 2, 'APPROVED', '[{"productId":7,"quantity":2,"sugarPercent":100,"iceOption":"NORMAL"},{"productId":15,"quantity":1,"sugarPercent":100,"iceOption":"NORMAL"}]', 'Trần Thị Bình', '0903334444', 2, DATE_SUB(NOW(), INTERVAL 3 HOUR)),
(3, 'qr_token_table3_003', 3, 'PENDING', '[{"productId":3,"quantity":1,"sugarPercent":100,"iceOption":"NORMAL"},{"productId":12,"quantity":1,"sugarPercent":70,"iceOption":"LESS_ICE"}]', 'Phạm Hoàng Cường', '0905556666', NULL, DATE_SUB(NOW(), INTERVAL 15 MINUTE)),
(4, 'qr_token_table4_004', 4, 'PENDING', '[{"productId":6,"quantity":1,"sugarPercent":100,"iceOption":"NORMAL"},{"productId":13,"quantity":1,"sugarPercent":100,"iceOption":"NORMAL"}]', 'Đỗ Mỹ Duyên', '0907778888', NULL, DATE_SUB(NOW(), INTERVAL 12 MINUTE)),
(5, 'qr_token_table5_005', 5, 'PENDING', '[{"productId":4,"quantity":1,"sugarPercent":100,"iceOption":"NORMAL"},{"productId":10,"quantity":1,"sugarPercent":50,"iceOption":"NORMAL"},{"productId":18,"quantity":1,"sugarPercent":100,"iceOption":"NORMAL"}]', 'Lê Quốc Hùng', '0912111333', NULL, DATE_SUB(NOW(), INTERVAL 8 MINUTE)),
(6, 'qr_token_table6_006', 6, 'PENDING', '[{"productId":5,"quantity":1,"sugarPercent":100,"iceOption":"NORMAL"},{"productId":18,"quantity":1,"sugarPercent":100,"iceOption":"NORMAL"}]', 'Vũ Phương Mai', '0913222444', NULL, DATE_SUB(NOW(), INTERVAL 5 MINUTE)),
(7, 'qr_token_table7_007', 7, 'PENDING', '[{"productId":8,"quantity":1,"sugarPercent":70,"iceOption":"NORMAL"},{"productId":11,"quantity":1,"sugarPercent":50,"iceOption":"NORMAL"},{"productId":16,"quantity":1,"sugarPercent":100,"iceOption":"NORMAL"}]', 'Hoàng Trọng Nghĩa', '0914333555', NULL, DATE_SUB(NOW(), INTERVAL 4 MINUTE)),
(8, 'qr_token_table8_008', 8, 'PENDING', '[{"productId":10,"quantity":1,"sugarPercent":70,"iceOption":"NORMAL"},{"productId":17,"quantity":1,"sugarPercent":100,"iceOption":"NORMAL"}]', 'Ngô Thanh Thảo', '0915444666', NULL, DATE_SUB(NOW(), INTERVAL 3 MINUTE)),
(9, 'qr_token_table9_009', 9, 'PENDING', '[{"productId":6,"quantity":2,"sugarPercent":100,"iceOption":"NORMAL"},{"productId":14,"quantity":2,"sugarPercent":70,"iceOption":"NO_ICE"}]', 'Đinh Gia Bảo', '0916555777', NULL, DATE_SUB(NOW(), INTERVAL 2 MINUTE)),
(10, 'qr_token_table10_010', 10, 'PENDING', '[{"productId":9,"quantity":2,"sugarPercent":50,"iceOption":"NORMAL"},{"productId":4,"quantity":2,"sugarPercent":100,"iceOption":"NORMAL"}]', 'Lý Nhã Kim', '0917666888', NULL, DATE_SUB(NOW(), INTERVAL 1 MINUTE));
