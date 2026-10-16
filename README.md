# He thong quan ly quan cafe

Project Java Spring Boot quan ly nhan vien, tai khoan, mon, ban va hoa don cafe.

## Yeu cau

- Java 17
- MySQL 8.x hoac MariaDB 10.4+

## Khoi tao database

1. Mo MySQL/phpMyAdmin.
2. Chay toan bo tep `cafe.sql`.
3. Neu tai khoan MySQL khac mac dinh, khai bao cac bien moi truong:
   - `DB_URL` (mac dinh: `jdbc:mysql://localhost:3306/cafe`)
   - `DB_USERNAME` (mac dinh: `root`)
   - `DB_PASSWORD` (mac dinh: rong)

Luu y: `cafe.sql` xoa va tao lai cac bang cua ung dung, vi vay hay sao luu du lieu truoc khi chay lai.
Neu database `cafe` da ton tai va can giu du lieu, chay mot lan lenh sau de bo sung trang thai mon:

```sql
ALTER TABLE products ADD COLUMN available BOOLEAN NOT NULL DEFAULT TRUE;
```

Ung dung quan ly nhan vien/tai khoan tai `/api/employees` va `/api/users`. Khi tao/cap nhat tai khoan, co the gui `employeeId` de gan tai khoan voi nhan vien. Quan ly mon tai `/api/products`; cap nhat mon bang `PUT /api/products/{id}` va bat/tat ban bang `PATCH /api/products/{id}/availability?available=false`.

## Chay ung dung

Tren Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Mac dinh ung dung chay REST API. De mo giao dien desktop Java Swing cung backend:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--app.gui.enabled=true --server.port=8081"
```

Giao dien dung chung `InvoiceGuiBinding` va `InvoiceService` de goi mon, cap nhat ton kho, tinh hoa don va thanh toan xuong database. Chi bat che do desktop tren may co moi truong do hoa.

Bien dich va kiem tra:

```powershell
.\mvnw.cmd test
```

Tai lieu phan tich nghiep vu va ERD nam tai `docs/PHAN_TICH_NGHIEP_VU_VA_ERD.md`.
