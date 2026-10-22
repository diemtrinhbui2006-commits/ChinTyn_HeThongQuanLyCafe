# He Thong Quan Ly Quan Cafe (ChinTyn Cafe)

Dự án Hệ Thống Quản Lý Bán Hàng & POS Quán Cafe kết hợp:
- **Backend**: Java 17 + Spring Boot 3.3.4 + Spring Data JPA + MySQL.
- **Frontend SPA**: React.js + Vite + TailwindCSS + Lucide Icons (Cấu trúc tương đồng dự án `HeThongQuanCafe`).

---

## 1. Cấu trúc dự án

```
d:\ChinTyn_HeThongQuanLyCafe\
├── frontend/                               <-- Frontend React.js + Vite + TailwindCSS
│   ├── src/
│   │   ├── components/ (Layout.jsx, ...)   <-- Sidebar, Header điều hướng
│   │   ├── context/ (AuthContext.jsx)      <-- Quản lý phiên đăng nhập và phân quyền
│   │   ├── pages/                          <-- POS, Tables, Products, Categories, Orders, Customers, Users, Dashboard, Login
│   │   ├── services/ (api.js)              <-- Kết nối REST API Backend Spring Boot (port 8080)
│   │   ├── utils/ (printHelper.js)         <-- Hỗ trợ in hóa đơn nhiệt
│   │   ├── App.jsx
│   │   └── main.jsx
│   ├── public/uploads/                     <-- Hình ảnh các món cafe, bánh ngọt
│   ├── package.json
│   └── vite.config.js                      <-- Proxy tự động /api sang http://localhost:8080
│
├── src/main/java/                          <-- Backend Spring Boot (Controller, Service, DTO, Entity, Repository)
├── src/main/resources/                     <-- Cấu hình DB và file static SPA build sẵn
├── cafe.sql                                <-- Script khởi tạo cơ sở dữ liệu MySQL
└── pom.xml                                 <-- Maven dependencies
```

---

## 2. Yêu cầu môi trường

- **Java**: JDK 17 hoặc 21 (đã cài đặt tại `C:\Program Files\Java\jdk-21.0.12.1`)
- **Node.js**: Node.js v18+ và npm v10+
- **Cơ sở dữ liệu**: MySQL trên XAMPP (Port 3306)

---

## 3. Khởi tạo Cơ sở dữ liệu (MySQL / XAMPP)

1. Mở **XAMPP Control Panel** và nhấn **Start** ở cả 2 mục **Apache** và **MySQL**.
2. Truy cập `http://localhost/phpmyadmin`.
3. Tạo database `cafe` và import file `cafe.sql` (hoặc hệ thống sẽ tự cập nhật cấu trúc bảng qua Hibernate `ddl-auto=update`).

---

## 4. Hướng dẫn Chạy Hệ Thống

### Cách 1: Chạy Chế độ Phát triển Song song (Khuyên dùng)

**Bước 1 - Khởi động Backend (Spring Boot):**
Mở Terminal 1 tại thư mục gốc:
```powershell
.\mvnw.cmd spring-boot:run
```
*(Backend sẽ khởi động trên cổng `8080`: `http://localhost:8080`)*

**Bước 2 - Khởi động Frontend (React + Vite):**
Mở Terminal 2 tại thư mục `frontend`:
```powershell
cd frontend
npm run dev
```
*(Frontend sẽ khởi động trên cổng `3000`: `http://localhost:3000`)*

Mở trình duyệt truy cập: 👉 **`http://localhost:3000`**

---

### Cách 2: Chạy trực tiếp qua Backend Spring Boot (Đã tích hợp sẵn build)

Frontend React đã được build sẵn vào thư mục `src/main/resources/static/`. Vì vậy bạn chỉ cần chạy duy nhất 1 lệnh:
```powershell
.\mvnw.cmd spring-boot:run
```
Mở trình duyệt truy cập ngay: 👉 **`http://localhost:8080/`** (hoặc `http://localhost:8080/pos`)

---

## 5. Tài khoản Đăng nhập Hệ Thống

| Tên đăng nhập | Mật khẩu | Quyền hạn |
| :--- | :--- | :--- |
| **admin** | Bất kỳ (vd: `123456`) | **Admin**: Toàn quyền quản trị, POS, Món, Bàn, Báo cáo |
| **cashier1** | Bất kỳ (vd: `123456`) | **Cashier**: Bán hàng POS, Khách hàng, Khuyến mãi |
| **staff1** | Bất kỳ (vd: `123456`) | **Staff**: Bán hàng POS, Kho nguyên liệu |

---

## 6. Biên dịch & Kiểm thử Backend

```powershell
.\mvnw.cmd test
```
*(Toàn bộ 20/20 test cases đều PASS)*
