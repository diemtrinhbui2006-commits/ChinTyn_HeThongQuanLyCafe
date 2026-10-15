# PHAN TICH NGHIEP VU VA THIET KE CO SO DU LIEU

## 1. Pham vi chuc nang

He thong quan ly quan cafe gom cac nghiep vu cot loi sau:

1. **Dang nhap**
   - Nhan vien dang nhap bang ten tai khoan va mat khau.
   - He thong xac dinh quyen su dung theo vai tro (quan ly/nhan vien).
2. **Quan ly ban**
   - Hien thi danh sach ban va trang thai: trong, dang phuc vu, da dat, ngung su dung.
   - Khi tao hoa don tai ban, ban chuyen sang trang thai dang phuc vu.
   - Khi thanh toan, ban tro lai trang thai trong.
3. **Goi mon tai ban**
   - Chon ban, chon mon, so luong va them vao hoa don dang mo.
   - Gia tai thoi diem goi mon duoc luu vao chi tiet hoa don de khong bi thay doi khi gia menu thay doi.
4. **Tinh tien va in hoa don**
   - Tong tien hang = tong (so luong x don gia) cua cac dong chi tiet.
   - Tong thanh toan = tong tien hang - giam gia.
   - Luu thoi gian thanh toan, nhan vien thanh toan va phuong thuc thanh toan.
   - Du lieu hoa don da thanh toan la nguon de in hoa don.
5. **Quan ly du lieu danh muc**
   - CRUD nhan vien, tai khoan, danh muc mon, mon va ban.
   - CRUD hoa don va chi tiet hoa don trong giai doan goi mon.

## 2. Quy tac nghiep vu chinh

- Moi tai khoan co ten dang nhap duy nhat va co the gan cho toi da mot nhan vien.
- Ma/so ban la duy nhat trong quan.
- Tai mot thoi diem, mot ban chi co toi da mot hoa don o trang thai `OPEN`.
- Moi chi tiet hoa don thuoc dung mot hoa don va mot mon.
- `unit_price` tren chi tiet hoa don la gia chot tai luc goi mon.
- Hoa don da `PAID` hoac `CANCELLED` khong duoc tiep tuc them mon.
- Khi thanh toan phai ghi nhan `paid_at`, `payment_method` va `total_amount`.

## 3. Mo hinh ERD

```mermaid
erDiagram
    EMPLOYEES ||--o| USERS : "co tai khoan"
    CATEGORIES ||--o{ PRODUCTS : "phan loai"
    CAFE_TABLES ||--o{ INVOICES : "phat sinh"
    EMPLOYEES ||--o{ INVOICES : "lap va thanh toan"
    CUSTOMERS ||--o{ INVOICES : "mua hang"
    INVOICES ||--|{ INVOICE_DETAILS : "gom"
    PRODUCTS ||--o{ INVOICE_DETAILS : "duoc goi"

    EMPLOYEES {
        bigint id PK
        varchar name
        varchar email
        varchar phone
        varchar position
        decimal salary
    }
    USERS {
        bigint id PK
        bigint employee_id FK_UQ
        varchar username UK
        varchar password
        varchar role
    }
    CATEGORIES {
        bigint id PK
        varchar name UK
        varchar description
    }
    PRODUCTS {
        bigint id PK
        bigint category_id FK
        varchar name
        decimal price
        int quantity
        varchar description
    }
    CAFE_TABLES {
        bigint id PK
        int table_number UK
        int capacity
        varchar status
    }
    CUSTOMERS {
        bigint id PK
        varchar name
        varchar email
        varchar phone
        varchar address
    }
    INVOICES {
        bigint id PK
        bigint table_id FK
        bigint employee_id FK
        bigint customer_id FK
        datetime created_at
        datetime paid_at
        decimal subtotal
        decimal discount
        decimal total_amount
        varchar status
        varchar payment_method
    }
    INVOICE_DETAILS {
        bigint id PK
        bigint invoice_id FK
        bigint product_id FK
        int quantity
        decimal unit_price
        decimal line_total
    }
```

## 4. Chuyen doi tu thuat ngu

| Nghiep vu | Lop/bang trong ma nguon |
|---|---|
| NhanVien | `Employee` / `employees` |
| BanAn | `CafeTable` / `cafe_tables` |
| MonAn | `Product` / `products` |
| HoaDon | `Invoice` / `invoices` |
| ChiTietHoaDon | `InvoiceDetail` / `invoice_details` |

Ten tieng Anh duoc giu dong bo voi project Spring Boot hien tai.
