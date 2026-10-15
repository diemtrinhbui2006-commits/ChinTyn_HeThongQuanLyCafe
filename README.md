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

## Chay ung dung

Tren Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Bien dich va kiem tra:

```powershell
.\mvnw.cmd test
```

Tai lieu phan tich nghiep vu va ERD nam tai `docs/PHAN_TICH_NGHIEP_VU_VA_ERD.md`.
