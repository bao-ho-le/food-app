# 🍜 Foodie - Hệ thống Đặt Món Ăn & Gợi Ý Món Ăn

Dự án Backend + Frontend cho hệ thống đặt món ăn Foodie, xây dựng trên nền tảng **Spring Boot** kết nối **MySQL** qua **Spring Data JPA**, xác thực bằng **JWT**, tài liệu hoá API tự động với **Swagger / OpenAPI**, giao diện người dùng bằng **Next.js**, và được đóng gói hoàn chỉnh bằng **Docker & Docker Compose**.

---

## 🛠️ Công nghệ sử dụng (Tech Stack)

### Backend
- **Framework**: [Spring Boot 3.5.6](https://spring.io/projects/spring-boot) (Java 17)
- **Database**: [MySQL 8.4](https://www.mysql.com/)
- **ORM**: Spring Data JPA / Hibernate
- **Authentication**: Spring Security + JWT (`jjwt`)
- **File Storage**: Cloudinary (upload ảnh món ăn)
- **Documentation**: Springdoc OpenAPI (Swagger UI)
- **Validation**: Jakarta Validation (`spring-boot-starter-validation`)
- **Build tool**: Maven

### Frontend
- **Framework**: [Next.js 15](https://nextjs.org/) (React 18)
- **UI**: Tailwind CSS + Radix UI / shadcn-ui
- **Form**: React Hook Form

### Hạ tầng
- **Containerization**: Docker & Docker Compose (MySQL + Backend + Frontend)

---

## 📋 Yêu cầu hệ thống (Prerequisites)

- **Docker Desktop** (hoặc Docker Engine & Docker Compose v2+)
- (Tuỳ chọn, nếu chạy local không dùng Docker) **JDK 17**, **Maven**, **Node.js 20+**
- **Git**

---

## 🐳 Hướng dẫn chạy dự án với Docker

### 🌟 Cách 1: Chạy toàn bộ Stack trong Docker
> *Phù hợp khi test sản phẩm hoặc chạy nhanh mà không cần cài JDK/Node trên máy host.*

#### Bước 1: Cấu hình biến môi trường
File `.env` ở thư mục gốc đã có sẵn (cấu hình cổng & URL kết nối giữa các service). Tạo thêm file `backend/.env` (không được commit lên git) theo mẫu ở mục [Biến môi trường](#️-cấu-hình-biến-môi-trường-env) bên dưới.

#### Bước 2: Build và khởi chạy tất cả Services

```bash
docker compose up -d --build
```

Backend sẽ tự động đợi MySQL ở trạng thái `healthy` rồi mới khởi động (nhờ `depends_on.condition: service_healthy` trong `docker-compose.yml`), và tự tạo/đồng bộ schema (`spring.jpa.hibernate.ddl.auto=update`) nên không cần chạy migration thủ công.

---

### 🛠️ Cách 2: Chạy MySQL trên Docker, chạy Backend & Frontend Local (Development)
> *Phù hợp cho lập trình viên trong quá trình phát triển, hỗ trợ hot-reload.*

#### Bước 1: Khởi động MySQL

```bash
docker compose up mysql -d
```

#### Bước 2: Chạy Backend (Spring Boot)

```bash
cd backend
./mvnw spring-boot:run
```

#### Bước 3: Chạy Frontend (Next.js)

```bash
cd frontend
npm install
npm run dev
```

---

## 🌐 Các cổng dịch vụ & Truy cập (Access Endpoints)

| Dịch vụ | URL | Mô tả |
| :--- | :--- | :--- |
| **Backend API Base** | `http://localhost:8080/api/v1` | Root endpoint của Backend |
| **Swagger API Docs** | `http://localhost:8080/swagger-ui/index.html` | Giao diện kiểm thử & tài liệu API trực quan |
| **OpenAPI JSON** | `http://localhost:8080/v3/api-docs` | Đặc tả OpenAPI dạng JSON |
| **Frontend (Website)** | `http://localhost:3000` | Giao diện người dùng Next.js |
| **MySQL** | `localhost:3307` | Cổng MySQL map ra host (container nội bộ dùng `3306`) |

---

## 📦 Cấu trúc Services trong `docker-compose.yml`

```text
┌────────────────────┐      ┌──────────────────────┐      ┌────────────────┐
│    food-website     │─────▶│   food-app-backend   │─────▶│   mysql        │
│  (Next.js : 3000)   │      │ (Spring Boot : 8080) │      │ (MySQL : 3306) │
└────────────────────┘      └──────────────────────┘      └────────────────┘
```

1. **`food-app-backend`**: Ứng dụng Spring Boot, expose cổng `8080`, tự đợi `mysql` healthy trước khi khởi động.
2. **`mysql`**: Cơ sở dữ liệu MySQL 8.4, lưu dữ liệu qua volume `mysql_data`.
3. **`food-website`**: Giao diện Next.js, expose cổng `3000`, gọi API tới backend qua biến `NEXT_PUBLIC_SPRING_URL`.

---

## 📖 Danh sách API chính (Endpoints)

Tất cả endpoint bên dưới có tiền tố `/api/v1`. Các route đánh dấu 🔓 là public, còn lại yêu cầu Bearer Token (JWT).

### 👤 Users & Xác thực
| Method | Endpoint | Mô tả |
| :--- | :--- | :--- |
| `POST` | `/users/register` 🔓 | Đăng ký tài khoản người dùng |
| `POST` | `/users/register-admin` 🔓 | Đăng ký tài khoản quản trị |
| `POST` | `/users/login` 🔓 | Đăng nhập, trả về JWT |
| `POST` | `/users/logout` | Đăng xuất |
| `GET` | `/users/profiles` | Xem thông tin cá nhân |
| `PUT` | `/users/profiles` | Cập nhật thông tin cá nhân |
| `PUT` | `/users/password` | Đổi mật khẩu |
| `GET` | `/users/getAll` | Danh sách toàn bộ người dùng (admin) |
| `POST` | `/users/blocking/{id}/{type}` | Khoá/mở khoá tài khoản (admin) |

### 🏪 Restaurants & 🍽️ Dishes
| Method | Endpoint | Mô tả |
| :--- | :--- | :--- |
| `GET` | `/restaurants` | Danh sách nhà hàng |
| `POST` | `/restaurants` | Tạo nhà hàng |
| `PUT` | `/restaurants/{id}` | Cập nhật nhà hàng |
| `GET` | `/dishes` | Danh sách món ăn |
| `POST` | `/dishes` | Tạo món ăn |
| `GET` | `/dishes/{dishId}/tags` | Danh sách tag của món ăn |
| `GET` | `/dishes/average_rating` 🔓 | Điểm đánh giá trung bình các món |
| `GET` | `/dishes/allIds` 🔓 | Danh sách toàn bộ ID món ăn |
| `POST` | `/dishes/blocking/{id}/{type}` | Ẩn/hiện món ăn (admin) |

### 🏷️ Categories, Tags & Dish-Tag
| Method | Endpoint | Mô tả |
| :--- | :--- | :--- |
| `GET` | `/categories` | Danh sách danh mục món ăn |
| `GET` | `/tags` 🔓 | Danh sách tag món ăn |
| `POST` | `/dish-tag/{dish_id}` | Gắn tag cho món ăn |

### 🛒 Giỏ hàng & Đơn hàng
| Method | Endpoint | Mô tả |
| :--- | :--- | :--- |
| `GET` | `/user-dishes` | Xem giỏ hàng của người dùng |
| `POST` | `/user-dishes` | Thêm món vào giỏ hàng |
| `PUT` | `/user-dishes` | Cập nhật số lượng món trong giỏ |
| `DELETE` | `/user-dishes/{user_dish_id}` | Xoá món khỏi giỏ hàng |
| `GET` | `/orders/user` | Danh sách đơn hàng của người dùng |
| `GET` | `/orders/user/{order_id}` | Chi tiết một đơn hàng |
| `POST` | `/orders` | Tạo đơn hàng mới |

### ⭐ Reviews & Địa chỉ & Ảnh
| Method | Endpoint | Mô tả |
| :--- | :--- | :--- |
| `GET` | `/reviews/dish/{dish_id}` | Danh sách đánh giá của một món |
| `POST` | `/reviews/dish/{order_dish_id}` | Viết đánh giá cho món đã đặt |
| `GET` | `/address/user` | Danh sách địa chỉ của người dùng |
| `POST` | `/address/user` | Thêm địa chỉ mới |
| `PUT` | `/address/user/{address_id}` | Cập nhật địa chỉ |
| `DELETE` | `/address/user/{address_id}` | Xoá địa chỉ |
| `POST` | `/images` | Upload ảnh món ăn (multipart, lưu trên Cloudinary) |
| `GET` | `/images/{dish_id}` | Lấy ảnh của một món ăn |

### 🎯 Bias (Sở thích người dùng)
| Method | Endpoint | Mô tả |
| :--- | :--- | :--- |
| `GET` | `/bias` | Xem sở thích/khẩu vị đã lưu của người dùng |
| `POST` | `/bias` | Thêm sở thích |
| `PUT` | `/bias` | Cập nhật sở thích |

---

## ⚙️ Cấu hình biến môi trường (`.env`)

### `.env` (thư mục gốc — điều phối Docker Compose)

| Biến môi trường | Giá trị mặc định | Giải thích |
| :--- | :--- | :--- |
| `MYSQL_HOST_PORT` | `3307` | Cổng MySQL map ra máy host |
| `SPRINGBOOT_HOST_PORT` | `8080` | Cổng Backend map ra máy host |
| `NEXTJS_HOST_PORT` | `3000` | Cổng Frontend map ra máy host |
| `SPRING_URL` | `http://food-app-backend:8080` | URL nội bộ Frontend gọi tới Backend (server-side) |
| `NEXT_PUBLIC_SPRING_URL` | `http://food-app-backend:8080/api/v1` | URL Backend expose ra trình duyệt (client-side) |

### `backend/.env` (không commit — tự tạo khi setup)

| Biến môi trường | Giá trị mặc định (dev) | Giải thích |
| :--- | :--- | :--- |
| `SERVER_PORT` | `8080` | Cổng ứng dụng Spring Boot |
| `API_PREFIX` | `/api/v1` | Tiền tố chung cho tất cả API route |
| `JWT_SECRET` | *(chuỗi bí mật, tự sinh)* | Khoá ký JWT |
| `SPRING_SECURITY_USER_NAME` / `SPRING_SECURITY_USER_PASSWORD` | `root` / `123` | Tài khoản Spring Security mặc định |
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://mysql:3306/foodie` | Connection string tới MySQL |
| `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD` | `root` / *(mật khẩu MySQL)* | Tài khoản kết nối MySQL |
| `FRONTEND_URL` | `http://food-website:3000` | URL Frontend, dùng để cấu hình CORS |
| `MYSQL_DATABASE` | `foodie` | Tên database |
| `MYSQL_ROOT_PASSWORD` | *(mật khẩu MySQL root)* | Mật khẩu root MySQL |

---

## 🌟 Tính năng kỹ thuật nổi bật

1. **Spring Data JPA + MySQL**: Tự động đồng bộ schema qua `hibernate.ddl.auto=update`, không cần chạy migration thủ công khi phát triển.
2. **Bảo mật với Spring Security & JWT**: Xác thực stateless bằng Bearer Token, phân quyền theo route (user/admin).
3. **Upload ảnh qua Cloudinary**: Ảnh món ăn được lưu trữ và phân phối qua CDN thay vì lưu trực tiếp trên server.
4. **Swagger / OpenAPI**: Tài liệu API tương tác được sinh tự động tại `/swagger-ui/index.html`.
5. **Module Bias**: Lưu và cập nhật sở thích khẩu vị của người dùng, làm nền tảng cho việc gợi ý món ăn.

---

## 👥 Nhóm thực hiện - Nhóm 23

| STT | Họ và Tên | MSSV |
| :---: | :--- | :---: |
| 1 | Lê Võ | N22DCCN097 |
| 2 | Trần Nhật Nguyên | N22DCCN057 |
