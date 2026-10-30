-- =======================================================
-- HỆ THỐNG QUẢN LÝ BÁN HÀNG & QUÁN CAFE (CHINTYN COFFEE)
-- Cơ Sở Dữ Liệu: MySQL 8.x / MariaDB 10.4+ (XAMPP)
-- Bộ mã ký tự: utf8mb4_unicode_ci (Hỗ trợ tiếng Việt đầy đủ)
-- =======================================================

CREATE DATABASE IF NOT EXISTS `cafe`
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE `cafe`;

-- 1. XÓA BẢNG CŨ THEO THỨ TỰ RÀNG BUỘC KHÓA NGOẠI
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS `invoice_details`;
DROP TABLE IF EXISTS `invoices`;
DROP TABLE IF EXISTS `users`;
DROP TABLE IF EXISTS `products`;
DROP TABLE IF EXISTS `categories`;
DROP TABLE IF EXISTS `cafe_tables`;
DROP TABLE IF EXISTS `customers`;
DROP TABLE IF EXISTS `employees`;
SET FOREIGN_KEY_CHECKS = 1;

-- 2. TẠO BẢNG NHÂN VIÊN (EMPLOYEES)
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

-- 3. TẠO BẢNG TÀI KHOẢN NGƯỜI DÙNG (USERS)
-- Mật khẩu được băm theo chuẩn PBKDF2WithHmacSHA256 (khớp với UserServiceImpl.java)
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

-- 4. TẠO BẢNG KHÁCH HÀNG (CUSTOMERS)
CREATE TABLE `customers` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL,
    `email` VARCHAR(100),
    `phone` VARCHAR(20),
    `address` VARCHAR(255),
    PRIMARY KEY (`id`),
    KEY `idx_customers_phone` (`phone`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 5. TẠO BẢNG DANH MỤC SẢN PHẨM (CATEGORIES)
CREATE TABLE `categories` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL,
    `description` VARCHAR(500),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_categories_name` (`name`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 6. TẠO BẢNG SẢN PHẨM / THỰC ĐƠN (PRODUCTS)
CREATE TABLE `products` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `category_id` BIGINT NOT NULL,
    `name` VARCHAR(100) NOT NULL,
    `description` VARCHAR(500),
    `price` DECIMAL(14, 2) NOT NULL,
    `quantity` INT NOT NULL DEFAULT 0,
    `available` BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (`id`),
    KEY `idx_products_name` (`name`),
    KEY `idx_products_category` (`category_id`),
    CONSTRAINT `chk_products_price` CHECK (`price` > 0),
    CONSTRAINT `chk_products_quantity` CHECK (`quantity` >= 0),
    CONSTRAINT `fk_products_category`
        FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 7. TẠO BẢNG BÀN ĂN (CAFE_TABLES)
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

-- 8. TẠO BẢNG HÓA ĐƠN BÁN HÀNG (INVOICES)
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

-- 9. TẠO BẢNG CHI TIẾT HÓA ĐƠN (INVOICE_DETAILS)
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
    UNIQUE KEY `uk_invoice_detail_variant` (`invoice_id`, `product_id`, `option_key`),
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


-- =======================================================
-- 10. DỮ LIỆU KHỞI TẠO MẪU (SEED DATA ĐẦY ĐỦ TIẾNG VIỆT)
-- =======================================================

-- 10.1. Danh Sách Nhân Viên
INSERT INTO `employees` (`id`, `name`, `email`, `phone`, `position`, `salary`) VALUES
(1, 'Nguyễn Quản Trị', 'admin@cafe.vn', '0912345678', 'MANAGER', 15000000.00),
(2, 'Trần Thu Ngân', 'thungan@cafe.vn', '0987654321', 'CASHIER', 8500000.00),
(3, 'Lê Phục Vụ', 'phucvu@cafe.vn', '0905123456', 'STAFF', 7000000.00);

-- 10.2. Danh Sách Tài Khoản Người Dùng (Mật khẩu mặc định: 123456)
-- Hash PBKDF2: pbkdf2$120000$BlSayiU+vb3TgzhMkdPreQ==$QHGBSgDtiLE4Z3HkMHvs1yJypc66q6TTC7A5QGJdnYw=
INSERT INTO `users` (`id`, `employee_id`, `username`, `password`, `role`) VALUES
(1, 1, 'admin', 'pbkdf2$120000$BlSayiU+vb3TgzhMkdPreQ==$QHGBSgDtiLE4Z3HkMHvs1yJypc66q6TTC7A5QGJdnYw=', 'ADMIN'),
(2, 2, 'cashier', 'pbkdf2$120000$BlSayiU+vb3TgzhMkdPreQ==$QHGBSgDtiLE4Z3HkMHvs1yJypc66q6TTC7A5QGJdnYw=', 'CASHIER'),
(3, 3, 'staff', 'pbkdf2$120000$BlSayiU+vb3TgzhMkdPreQ==$QHGBSgDtiLE4Z3HkMHvs1yJypc66q6TTC7A5QGJdnYw=', 'STAFF');

-- 10.3. Danh Sách Khách Hàng Thân Thiết
INSERT INTO `customers` (`id`, `name`, `email`, `phone`, `address`) VALUES
(1, 'Nguyễn Văn An', 'an.nguyen@gmail.com', '0901112222', '123 Nguyễn Huệ, Phường Bến Nghé, Quận 1, TP.HCM'),
(2, 'Trần Thị Bình', 'binh.tran@gmail.com', '0903334444', '45 Lê Lợi, Phường Bến Thành, Quận 1, TP.HCM'),
(3, 'Phạm Hoàng Cường', 'cuong.pham@gmail.com', '0905556666', '78 Pasteur, Phường Võ Thị Sáu, Quận 3, TP.HCM'),
(4, 'Đỗ Mỹ Duyên', 'duyen.do@gmail.com', '0907778888', '12 Nam Kỳ Khởi Nghĩa, Quận 3, TP.HCM');

-- 10.4. Danh Mục Món
INSERT INTO `categories` (`id`, `name`, `description`) VALUES
(1, 'Cà phê', 'Các sản phẩm cà phê truyền thống Việt Nam và cà phê pha máy'),
(2, 'Trà & Trà Sữa', 'Trà trái cây nhiệt đới tươi mát và trà sữa trân châu đậm vị'),
(3, 'Sinh tố & Đá xay', 'Thức uống hoa quả tươi nguyên chất và đá xay béo mịn mát lạnh'),
(4, 'Bánh ngọt & Tráng miệng', 'Bánh ngọt Âu nướng tươi mỗi ngày dùng kèm trà và cà phê');

-- 10.5. Danh Sách Thực Đơn (Khớp ảnh thực tế trong uploads/)
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

-- 10.6. Sơ Đồ Bàn Ăn (10 bàn đa dạng sức chứa)
INSERT INTO `cafe_tables` (`id`, `table_number`, `capacity`, `status`) VALUES
(1, 1, 2, 'AVAILABLE'),
(2, 2, 4, 'AVAILABLE'),
(3, 3, 4, 'AVAILABLE'),
(4, 4, 6, 'AVAILABLE'),
(5, 5, 4, 'AVAILABLE'),
(6, 6, 2, 'AVAILABLE'),
(7, 7, 4, 'AVAILABLE'),
(8, 8, 4, 'AVAILABLE'),
(9, 9, 6, 'AVAILABLE'),
(10, 10, 8, 'AVAILABLE');

-- 10.7. Hóa Đơn Mẫu Đã Bán Ra (Để hiển thị báo cáo & Dashboard ngay khi cài đặt)
INSERT INTO `invoices` (`id`, `table_id`, `employee_id`, `customer_id`, `created_at`, `paid_at`, `subtotal`, `discount`, `total_amount`, `status`, `payment_method`) VALUES
(1, 1, 2, 1, DATE_SUB(NOW(), INTERVAL 3 HOUR), DATE_SUB(NOW(), INTERVAL 2 HOUR), 54000.00, 0.00, 54000.00, 'PAID', 'CASH'),
(2, 2, 2, 2, DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 1 HOUR), 105000.00, 10500.00, 94500.00, 'PAID', 'BANK_TRANSFER'),
(3, 3, 2, 3, DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_SUB(NOW(), INTERVAL 30 MINUTE), 74000.00, 0.00, 74000.00, 'PAID', 'CARD'),
(4, 4, 2, NULL, DATE_SUB(NOW(), INTERVAL 45 MINUTE), DATE_SUB(NOW(), INTERVAL 10 MINUTE), 84000.00, 0.00, 84000.00, 'PAID', 'E_WALLET');

-- 10.8. Chi Tiết Các Món Trong Hóa Đơn Mẫu
INSERT INTO `invoice_details` (`invoice_id`, `product_id`, `quantity`, `unit_price`, `line_total`) VALUES
-- HĐ 1 (Bàn 1: 1 Cà phê Đen Đá + 1 Cà phê Sữa Đá = 54k)
(1, 1, 1, 25000.00, 25000.00),
(1, 2, 1, 29000.00, 29000.00),

-- HĐ 2 (Bàn 2: 2 Trà sữa trân châu + 1 Bánh Tiramisu = 105k - giảm 10% = 94.5k)
(2, 7, 2, 35000.00, 70000.00),
(2, 15, 1, 35000.00, 35000.00),

-- HĐ 3 (Bàn 3: 1 Bạc Xỉu + 1 Sinh tố bơ = 74k)
(3, 3, 1, 29000.00, 29000.00),
(3, 12, 1, 45000.00, 45000.00),

-- HĐ 4 (Bàn 4: 1 Cà phê Muối + 1 Matcha Đá Xay = 84k)
(4, 6, 1, 35000.00, 35000.00),
(4, 13, 1, 49000.00, 49000.00);
