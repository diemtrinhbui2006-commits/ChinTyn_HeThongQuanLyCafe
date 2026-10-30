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
| **admin** | `123456` (mật khẩu dữ liệu mẫu) | **Admin**: Quản trị tài khoản, nhân viên, món, danh mục và bàn; sử dụng POS |
| **cashier** | `123456` (mật khẩu dữ liệu mẫu) | **Cashier**: Gọi món, xem đơn, thanh toán, xem khách hàng và mở/đóng ca của mình |
| **staff** | `123456` (mật khẩu dữ liệu mẫu) | **Staff**: Chọn bàn, gọi món và kết nối điện thoại; không được thanh toán hoặc sửa dữ liệu quản trị |

Đăng nhập được xác thực ở backend và phiên được lưu bằng cookie HttpOnly. Các mật khẩu trên chỉ dùng cho dữ liệu mẫu sau khi import `cafe.sql`; hãy đổi chúng trước khi sử dụng dữ liệu thật. Tài khoản mới cần mật khẩu tối thiểu 8 ký tự. Khi triển khai qua HTTPS, đặt biến môi trường `COOKIE_SECURE=true` và cấu hình thông tin MySQL riêng qua `DB_USERNAME`/`DB_PASSWORD`.

---

## 6. Biên dịch & Kiểm thử Backend

```powershell
.\mvnw.cmd test
```
*(Bộ kiểm thử bao gồm xác nhận khuyến mãi/điểm thưởng, luồng duyệt QR và bảo vệ API.)*

---

## 7. Vận hành nâng cao

Trang **Vận hành nâng cao** cung cấp quản lý nguyên liệu và cảnh báo tồn thấp, đặt/hủy bàn,
mở/đóng ca, đối soát tiền mặt, nhà cung cấp, chi phí/nhập kho, mã khuyến mãi, phần thưởng
đổi điểm và hàng chờ gọi món QR. Các bản ghi được lưu bằng JPA; Hibernate cập nhật schema
theo cấu hình hiện tại.

- Ca tiền mặt dự kiến = tiền đầu ca + hóa đơn tiền mặt đã thanh toán trong ca − chi phí tiền mặt trong ca.
- Khách nhận 1 điểm cho mỗi 10.000 ₫ đã thanh toán; điểm và lịch sử chỉ được ghi nhận sau khi checkout thành công.
- Mã khuyến mãi quản lý được qua `/api/promotions` và được kiểm tra khi áp dụng vào checkout.
- Nhân viên tạo và hiển thị mã QR PNG tại bàn từ trang vận hành. Khách dùng liên kết `/guest-order.html?token=...`
  để xem menu/gửi yêu cầu. Yêu cầu chỉ nằm trong hàng chờ `/api/qr/orders/pending`; nhân viên phải
  duyệt mới thêm món vào hóa đơn đang mở. Token dùng 256 bit ngẫu nhiên; API duyệt yêu cầu cần đăng nhập.

## Phân quyền và mật khẩu quản trị

Admin được quản lý dữ liệu và tài khoản. Thu ngân và phục vụ chỉ được thực hiện nghiệp vụ được cấp; API kiểm tra vai trò độc lập với giao diện. Thu ngân không được thao tác ca của người khác.

Tài khoản Admin dùng mật khẩu mẫu phải tự đặt mật khẩu mới dài 12 đến 128 ký tự trước khi sử dụng quản trị, sau đó đăng nhập lại. QR kết nối không cấp quyền hay tự đăng nhập. Vai trò và phiên được kiểm tra lại ở máy chủ; đổi vai trò có hiệu lực ở yêu cầu tiếp theo, đặt lại mật khẩu thu hồi phiên cũ.
