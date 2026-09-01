# 🍜 Foodie - Hệ thống Đặt Món Ăn

Dự án Backend + Frontend cho hệ thống đặt món ăn Foodie, xây dựng trên nền tảng **Spring Boot** kết nối **MySQL** qua **Spring Data JPA**, xác thực bằng **JWT**, tài liệu hoá API tự động với **Swagger / OpenAPI**, giao diện người dùng bằng **Next.js**, và được đóng gói hoàn chỉnh bằng **Docker & Docker Compose**.

Trọng tâm hiện tại của dự án không nằm ở việc thêm tính năng mới, mà ở **độ đúng đắn của hệ thống khi có nhiều người dùng thao tác đồng thời** — bài toán mà một ứng dụng đặt món thực tế luôn phải đối mặt.

🔗 **Demo trực tuyến:** [https://food-app-roan-nine.vercel.app](https://food-app-roan-nine.vercel.app)

---

## 🎯 Bài toán thực tế & Cách hệ thống giải quyết

Một hệ thống đặt món ăn khi chạy thật sẽ gặp những tình huống mà luồng xử lý tuần tự "happy path" không bao giờ lộ ra. Dưới đây là các vấn đề hệ thống đã xác định và xử lý:

### 1. Người dùng bấm "Đặt hàng" hai lần — đơn hàng bị nhân đôi

**Vấn đề:** Mạng chậm, người dùng bấm lại, hoặc client tự động retry khi timeout. Kết quả là một thao tác của người dùng tạo ra hai đơn hàng, trừ kho hai lần, tính tiền hai lần.

**Giải pháp — Idempotency Key:** Endpoint `POST /orders` nhận header `Idempotency-Key`. Hệ thống:
- **Đặt chỗ trước khi xử lý** (`IN_PROGRESS`) — request thứ hai đến sau sẽ thấy key đã tồn tại và bị từ chối thay vì chạy song song.
- **Ghi vân tay request bằng SHA-256** — cùng một key nhưng nội dung request khác nhau sẽ bị từ chối (`IDEMPOTENCY_KEY_REQUEST_MISMATCH`), tránh việc client vô tình tái sử dụng key cho đơn hàng khác.
- **Phát lại nguyên văn response cũ** khi request đã hoàn tất (`COMPLETED`) — client retry nhận đúng kết quả lần đầu, không tạo đơn mới.
- **Giải phóng key khi thất bại** — nếu việc tạo đơn ném lỗi, key được xoá để người dùng có thể thử lại.
- **Ràng buộc theo người dùng** — key của người này không thể bị người khác dùng để đọc đơn hàng.
- Bản ghi key được **dọn tự động sau 48 giờ** bằng scheduled task chạy lúc 3h sáng mỗi ngày.

Điểm mấu chốt kỹ thuật: các thao tác ghi trạng thái key dùng `@Transactional(propagation = REQUIRES_NEW)` — chúng phải commit **độc lập** với transaction tạo đơn hàng, nếu không chúng sẽ bị rollback theo và cơ chế mất tác dụng hoàn toàn.

### 2. Hai khách cùng mua món cuối cùng — bán vượt tồn kho (oversell)

**Vấn đề:** Hai request đọc `stock_quantity = 1` cùng lúc, cả hai đều thấy "còn hàng", cả hai đều trừ kho. Kết quả: kho về `-1`, hai khách cùng được xác nhận cho một suất ăn.

**Giải pháp — Khoá bi quan (Pessimistic Lock):** Trước khi kiểm tra và trừ kho, hệ thống khoá từng bản ghi `Dish` bằng `SELECT ... FOR UPDATE` (`@Lock(LockModeType.PESSIMISTIC_WRITE)`). Request thứ hai buộc phải chờ request thứ nhất commit xong mới đọc được giá trị kho, nên nó thấy đúng `stock = 0` và bị từ chối. Đúng một khách mua được.

### 3. Hai đơn hàng khoá chéo nhau — deadlock

**Vấn đề:** Đơn A khoá món 1 rồi chờ món 2; đơn B khoá món 2 rồi chờ món 1. Hai transaction chờ nhau vĩnh viễn, database phải tự ngắt một bên.

**Giải pháp — Thứ tự khoá xác định:** Toàn bộ ID món trong giỏ được **sắp xếp tăng dần trước khi khoá**. Mọi transaction trong hệ thống đều lấy khoá theo cùng một thứ tự, nên chu trình chờ vòng tròn không thể hình thành. Hai giỏ hàng chứa cùng tập món nhưng thêm vào theo thứ tự ngược nhau vẫn hoàn tất cả hai.

### 4. Admin và khách cùng đổi trạng thái một đơn — mất cập nhật (lost update)

**Vấn đề:** Admin chuyển đơn sang `PREPARING` trong khi khách bấm huỷ. Cả hai đọc trạng thái `PENDING`, cả hai ghi đè lên nhau — kết quả cuối cùng phụ thuộc vào việc ai ghi sau, và một trong hai thao tác biến mất không dấu vết.

**Giải pháp — Khoá lạc quan (Optimistic Lock):** Entity `Order` mang cột `@Version`. Khi hai transaction cùng sửa một đơn, transaction ghi sau sẽ thấy version đã đổi và ném `ObjectOptimisticLockingFailureException`, được dịch thành lỗi nghiệp vụ `ORDER_STATUS_CONFLICT` trả về cho client. Đúng một bên thắng, bên còn lại nhận lỗi rõ ràng thay vì âm thầm mất thao tác.

### 5. Tạo đơn hàng thất bại giữa chừng — dữ liệu rác

**Vấn đề:** Giỏ hàng có 3 món. Trừ kho xong món 1 và 2, đến món 3 thì phát hiện hết hàng. Nếu không có transaction, hệ thống để lại một đơn hàng dở dang, kho của món 1 và 2 bị trừ oan, giỏ hàng thì đã xoá mất.

**Giải pháp — All-or-nothing với `@Transactional`:** Toàn bộ quy trình tạo đơn (khoá món → kiểm tra khả dụng → kiểm tra tồn kho → trừ kho → tạo đơn → xoá giỏ) nằm trong **một transaction duy nhất**. Bất kỳ món nào không thoả điều kiện, toàn bộ thay đổi được rollback: không tạo đơn một phần, không trừ kho món nào, giỏ hàng giữ nguyên để người dùng sửa lại. Tương tự, khi huỷ đơn, kho được hoàn lại trong cùng transaction với việc đổi trạng thái.

### 6. Cùng một món được thêm vào giỏ hai lần cùng lúc — giỏ hàng nhân bản

**Vấn đề:** Hai request thêm cùng một món chạy song song, cả hai đều thấy "món chưa có trong giỏ", cả hai đều tạo dòng mới. Giỏ hàng xuất hiện hai dòng trùng cho một món.

**Giải pháp — Ràng buộc UNIQUE + upsert nguyên tử:** Bảng `user_dish` có ràng buộc `UNIQUE (user_id, dish_id)`, và thao tác thêm vào giỏ dùng `INSERT ... ON DUPLICATE KEY UPDATE quantity = quantity + VALUES(quantity)` của MySQL. Database tự đảm bảo tính nguyên tử — hai request đồng thời gộp thành đúng một dòng với số lượng cộng dồn chính xác.

### 7. Refresh token bị đánh cắp và dùng lại

**Vấn đề:** Nếu refresh token rò rỉ, kẻ tấn công có thể dùng nó để sinh access token vô thời hạn mà hệ thống không hề hay biết.

**Giải pháp — Xoay vòng token + Phát hiện tái sử dụng:** Mỗi lần refresh, token cũ bị thu hồi và token mới được cấp. Nếu một token **đã bị thu hồi** được dùng lại, hệ thống hiểu rằng token đã rò rỉ và **thu hồi toàn bộ token family của người dùng đó** — buộc đăng nhập lại. Việc thu hồi dùng `REQUIRES_NEW` để đảm bảo lệnh thu hồi được commit kể cả khi request đó kết thúc bằng một exception. Hai request refresh đồng thời với cùng token thì đúng một request thắng.

---

## 🌟 Tính năng kỹ thuật nổi bật

### Xử lý đồng thời & Toàn vẹn dữ liệu
1. **Khoá bi quan có thứ tự (`SELECT ... FOR UPDATE`)**: Chống bán vượt tồn kho, đồng thời sắp xếp ID món trước khi khoá để loại trừ deadlock.
2. **Khoá lạc quan (`@Version`)**: Chống mất cập nhật khi nhiều bên cùng đổi trạng thái đơn hàng, trả về lỗi `ORDER_STATUS_CONFLICT` thay vì âm thầm ghi đè.
3. **Idempotency Key**: Chống tạo đơn trùng, có vân tay request SHA-256, phát lại response, ràng buộc theo người dùng và tự dọn dẹp sau 48 giờ.
4. **Upsert nguyên tử + ràng buộc UNIQUE**: Giỏ hàng không bao giờ có dòng trùng, kể cả khi thêm món đồng thời.

### Quản lý Transaction
5. **Tạo đơn all-or-nothing**: Khoá món, kiểm tra tồn kho, trừ kho, tạo đơn và xoá giỏ nằm trong một transaction — hỏng một bước là rollback toàn bộ.
6. **Hoàn kho khi huỷ đơn trong cùng transaction**: Trạng thái đơn và số lượng tồn kho luôn nhất quán với nhau.
7. **`REQUIRES_NEW` cho các thao tác phải commit độc lập**: Ghi trạng thái idempotency key và thu hồi refresh token không bị rollback theo transaction của caller — đây là điều kiện bắt buộc để hai cơ chế này hoạt động đúng.
8. **Máy trạng thái đơn hàng có kiểm soát**: Mọi chuyển trạng thái đều đi qua `validateStatusTransition`; các bước nhảy cóc, lùi trạng thái, hoặc huỷ sau khi đã giao cho shipper đều bị chặn.

### Bảo mật
9. **Spring Security & JWT stateless**: Access/refresh token tách biệt (khác secret, khác kiểu), refresh token xoay vòng kèm phát hiện tái sử dụng, phân quyền theo route (`USER` / `ADMIN`).
10. **Thu hồi token theo sự kiện**: Đổi mật khẩu hoặc bị khoá tài khoản sẽ vô hiệu hoá toàn bộ refresh token đang hoạt động ngay lập tức.
11. **Kiểm soát quyền sở hữu dữ liệu**: Người dùng chỉ đọc/sửa được đơn hàng, địa chỉ và giỏ hàng của chính mình — kiểm tra ở tầng service chứ không chỉ dựa vào route.

### Dữ liệu & Vận hành
12. **Flyway migration**: Schema quản lý bằng 8 migration script có phiên bản (`V1` → `V8`), Hibernate chỉ `validate` chứ không tự sửa schema — tránh việc schema production bị thay đổi ngoài kiểm soát.
13. **Giữ lại dữ liệu đơn đã giao**: Đơn `DELIVERED` bị từ chối xoá để bảo toàn dữ liệu cho báo cáo và đối soát.
14. **Upload ảnh qua Cloudinary**: Ảnh món ăn lưu và phân phối qua CDN thay vì lưu trên server ứng dụng.
15. **Swagger / OpenAPI**: Tài liệu API tương tác sinh tự động tại `/swagger-ui/index.html`.

### Kiểm thử
16. **236 test case, chạy trên MySQL thật qua Testcontainers** (không dùng H2 giả lập cho các test quan trọng), phủ 6 tầng từ unit đến concurrency. Độ phủ đo bằng JaCoCo:

    | Metric | Coverage |
    | :--- | :--- |
    | Instructions | 74% (1,599 missed / 6,183) |
    | Branches | 62% (142 missed / 380) |

    > Con số đo trên phần code nghiệp vụ — các package `dto`, `entity`, `enums`, `config` được loại trừ khỏi phép đo vì chỉ chứa cấu trúc dữ liệu và cấu hình.

17. **Test đồng thời chạy đa luồng thật**: 11 test case dùng `TestRestTemplate` trên cổng thật với `CountDownLatch` đồng bộ điểm xuất phát, chứng minh các cơ chế khoá ở trên hoạt động đúng dưới tải song song — không phải chỉ đúng trên lý thuyết.

> **📌 Ghi chú về module `ai-service`**
>
> Module `ai-service` (gợi ý món ăn) **đã được gỡ hoàn toàn khỏi hệ thống đang chạy** — không còn nằm trong `docker-compose.yml`, không còn được backend hay frontend gọi tới.
>
> Lý do: nhóm quyết định **tập trung nguồn lực vào khả năng chịu tải và tính đúng đắn của hệ thống dưới truy cập đồng thời** thay vì phát triển tính năng AI. Bản thân tính năng gợi ý ở giai đoạn trước cũng chưa đạt mức hoàn thiện đủ để đưa vào sản phẩm.
>
> Thư mục `ai-service/` **vẫn được giữ lại trong repository** để lưu vết công việc đã thực hiện, nhưng không tham gia vào quá trình build, chạy hay triển khai hệ thống.

---

## 🛠️ Công nghệ sử dụng (Tech Stack)

### Backend
- **Framework**: [Spring Boot 3.5.6](https://spring.io/projects/spring-boot) (Java 17)
- **Database**: [MySQL 8.4](https://www.mysql.com/)
- **ORM**: Spring Data JPA / Hibernate (`ddl-auto=validate`)
- **Database Migration**: Flyway (`flyway-core` + `flyway-mysql`) — 8 phiên bản schema
- **Authentication**: Spring Security + JWT (`jjwt` 0.12.6, access/refresh token tách biệt)
- **File Storage**: Cloudinary (`cloudinary-http44` 1.33.0)
- **Documentation**: Springdoc OpenAPI 2.9.0 (Swagger UI)
- **Validation**: Jakarta Validation (`spring-boot-starter-validation`)
- **Boilerplate**: Lombok
- **Build tool**: Maven

### Kiểm thử (Backend)
- **Test framework**: JUnit 5 + Mockito + AssertJ (`spring-boot-starter-test`)
- **Security testing**: `spring-security-test`
- **Database testing**: [Testcontainers](https://testcontainers.com/) (MySQL 8.4 thật, dùng singleton container pattern)
- **Coverage**: JaCoCo 0.8.12

### Frontend
- **Framework**: [Next.js 15.2.4](https://nextjs.org/) (React 18.3, TypeScript 5)
- **Styling**: Tailwind CSS v4 + `tailwindcss-animate`
- **UI Components**: Radix UI / shadcn-ui, `lucide-react` (icons), `geist` (font)
- **Form & Validation**: React Hook Form + Zod (`@hookform/resolvers`)
- **State management**: Zustand
- **Charts**: Recharts (biểu đồ dashboard)
- **UX**: Sonner (toast), `next-themes` (dark mode), `embla-carousel-react`, `vaul`

### Hạ tầng
- **Containerization**: Docker & Docker Compose — 3 services: MySQL + Backend + Frontend

---

## 📋 Yêu cầu hệ thống (Prerequisites)

- **Docker Desktop** (hoặc Docker Engine & Docker Compose v2+)
- **Git**

---

## 🐳 Hướng dẫn chạy dự án

### Bước 1: Cấu hình biến môi trường

File `.env` ở thư mục gốc đã có sẵn (cấu hình cổng & URL kết nối giữa các service). Tạo thêm file `backend/.env` (không được commit lên git) theo mẫu ở mục [Biến môi trường](#️-cấu-hình-biến-môi-trường-env) bên dưới.

### Bước 2: Build và khởi chạy toàn bộ Stack

```bash
docker compose up -d --build
```

Backend sẽ tự động đợi MySQL ở trạng thái `healthy` rồi mới khởi động (nhờ `depends_on.condition: service_healthy` trong `docker-compose.yml`). Flyway tự chạy migration để tạo/nâng cấp schema khi ứng dụng khởi động, nên không cần thao tác database thủ công.

### Dừng hệ thống

```bash
docker compose down
```

---

## 🚀 Triển khai (Deployment)

Hệ thống chạy trên 2 nền tảng PaaS, không dùng VPS:

### URL public

| Dịch vụ | URL | Mô tả |
| :--- | :--- | :--- |
| **Frontend (Website)** | [food-app-roan-nine.vercel.app](https://food-app-roan-nine.vercel.app) | Giao diện người dùng Next.js |
| **Backend API Base** | [food-app-production-1b1f.up.railway.app/api/v1](https://food-app-production-1b1f.up.railway.app/api/v1) | Root endpoint Backend |
| **MySQL** | | Database, không public ra ngoài |

Swagger UI **không** được expose ở production — chỉ bật khi chạy local (xem mục Access Endpoints bên dưới).

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
│    food-website    │─────▶│   food-app-backend   │─────▶│     mysql      │
│  (Next.js : 3000)  │      │ (Spring Boot : 8080) │      │ (MySQL : 3306) │
└────────────────────┘      └──────────────────────┘      └────────────────┘
```

1. **`food-website`**: Giao diện Next.js, expose cổng `3000`, gọi API tới backend qua biến `NEXT_PUBLIC_SPRING_URL`.
2. **`food-app-backend`**: Ứng dụng Spring Boot, expose cổng `8080`, tự đợi `mysql` healthy trước khi khởi động.
3. **`mysql`**: Cơ sở dữ liệu MySQL 8.4, lưu dữ liệu qua volume `mysql_data`.

---

## 📖 Danh sách API chính (Endpoints)

Tất cả endpoint bên dưới có tiền tố `/api/v1`. Các route đánh dấu 🔓 là public; route đánh dấu 🔒 **ADMIN** yêu cầu Bearer Token với role `ADMIN`; các route còn lại yêu cầu Bearer Token (JWT) của user đã đăng nhập.

### 👤 Users & Xác thực
| Method | Endpoint | Mô tả |
| :--- | :--- | :--- |
| `POST` | `/users/register` 🔓 | Đăng ký tài khoản người dùng |
| `POST` | `/users/login` 🔓 | Đăng nhập, trả về JWT + refresh token cookie |
| `POST` | `/users/refresh` 🔓 | Xoay vòng refresh token, cấp access token mới |
| `POST` | `/users/logout` 🔓 | Đăng xuất, thu hồi refresh token |
| `GET` | `/users/profiles` | Xem thông tin cá nhân |
| `PUT` | `/users/profiles` | Cập nhật thông tin cá nhân |
| `PUT` | `/users/password` | Đổi mật khẩu (thu hồi toàn bộ refresh token) |
| `POST` | `/admin/users/register-admin` 🔒 ADMIN | Đăng ký tài khoản quản trị mới |
| `GET` | `/admin/users` 🔒 ADMIN | Danh sách toàn bộ người dùng |
| `POST` | `/admin/users/blocking/{id}/{type}` 🔒 ADMIN | Khoá/mở khoá tài khoản |
| `GET` | `/admin/users/{id}/addresses` 🔒 ADMIN | Danh sách địa chỉ của một người dùng |

### 🏪 Restaurants & 🍽️ Dishes
| Method | Endpoint | Mô tả |
| :--- | :--- | :--- |
| `GET` | `/dishes` 🔓 | Danh sách món ăn |
| `GET` | `/dishes/average_rating` 🔓 | Điểm đánh giá trung bình các món |
| `GET` | `/dishes/allIds` 🔓 | Danh sách toàn bộ ID món ăn |
| `GET` | `/dishes/{dishId}/tags` | Danh sách tag của món ăn |
| `GET` | `/restaurants` | Danh sách nhà hàng |
| `POST` | `/admin/restaurants` 🔒 ADMIN | Tạo nhà hàng |
| `PUT` | `/admin/restaurants/{id}` 🔒 ADMIN | Cập nhật nhà hàng |
| `POST` | `/admin/restaurants/blocking/{id}/{type}` 🔒 ADMIN | Ẩn/hiện nhà hàng |
| `POST` | `/admin/dishes` 🔒 ADMIN | Tạo món ăn |
| `POST` | `/admin/dishes/blocking/{id}/{type}` 🔒 ADMIN | Ẩn/hiện món ăn |
| `POST` | `/admin/dishes/{dishId}/stock` 🔒 ADMIN | Nhập thêm tồn kho cho món ăn |

### 🏷️ Categories, Tags & Dish-Tag
| Method | Endpoint | Mô tả |
| :--- | :--- | :--- |
| `GET` | `/tags` 🔓 | Danh sách tag món ăn |
| `GET` | `/categories` | Danh sách danh mục món ăn |
| `POST` | `/dish-tag/{dish_id}` 🔒 ADMIN | Gắn tag cho món ăn |

### 🛒 Giỏ hàng & Đơn hàng
| Method | Endpoint | Mô tả |
| :--- | :--- | :--- |
| `GET` | `/user-dishes` | Xem giỏ hàng của người dùng |
| `POST` | `/user-dishes` | Thêm món vào giỏ hàng (upsert nguyên tử, cộng dồn số lượng) |
| `PUT` | `/user-dishes` | Cập nhật số lượng món trong giỏ |
| `DELETE` | `/user-dishes/{user_dish_id}` | Xoá món khỏi giỏ hàng |
| `POST` | `/orders` | **Tạo đơn hàng mới** — yêu cầu header `Idempotency-Key` |
| `GET` | `/orders/user` | Danh sách đơn hàng của người dùng |
| `GET` | `/orders/user/{order_id}` | Chi tiết một đơn hàng |
| `PATCH` | `/orders/user/{id}/cancel` | Huỷ đơn của tôi (chỉ khi PENDING/PREPARING, hoàn kho) |
| `PATCH` | `/orders/user/{id}/confirm-received` | Xác nhận đã nhận đơn hàng |
| `GET` | `/admin/orders` 🔒 ADMIN | Danh sách toàn bộ đơn hàng |
| `GET` | `/admin/orders/{id}` 🔒 ADMIN | Chi tiết một đơn hàng |
| `GET` | `/admin/orders/{id}/items` 🔒 ADMIN | Danh sách món trong một đơn hàng |
| `PATCH` | `/admin/orders/{id}/status` 🔒 ADMIN | Cập nhật trạng thái đơn hàng |
| `DELETE` | `/admin/orders/{id}` 🔒 ADMIN | Xoá đơn hàng (từ chối nếu đã DELIVERED) |

### 📊 Dashboard (Admin)
| Method | Endpoint | Mô tả |
| :--- | :--- | :--- |
| `GET` | `/admin/dashboard/stats` 🔒 ADMIN | Thống kê tổng quan |
| `GET` | `/admin/dashboard/trend` 🔒 ADMIN | Xu hướng doanh thu/đơn hàng theo ngày (7/14/30) |
| `GET` | `/admin/dashboard/top-products` 🔒 ADMIN | Món ăn bán chạy nhất |

### ⭐ Reviews & Địa chỉ & Ảnh
| Method | Endpoint | Mô tả |
| :--- | :--- | :--- |
| `GET` | `/reviews/dish/{dish_id}` | Danh sách đánh giá của một món |
| `POST` | `/reviews/dish/{order_dish_id}` | Viết đánh giá cho món đã đặt |
| `GET` | `/address/user` | Danh sách địa chỉ của người dùng |
| `POST` | `/address/user` | Thêm địa chỉ mới |
| `PUT` | `/address/user/{address_id}` | Cập nhật địa chỉ |
| `DELETE` | `/address/user/{address_id}` | Xoá địa chỉ |
| `POST` | `/images` 🔒 ADMIN | Upload ảnh món ăn (multipart, lưu trên Cloudinary) |
| `GET` | `/images/{dish_id}` | Lấy ảnh của một món ăn |

---

## 🧪 Kiểm thử hệ thống (Testing)

Hệ thống có **236 test case, chạy đầy đủ và không có test nào thất bại**. Toàn bộ test cần database đều chạy trên **MySQL 8.4 thật thông qua Testcontainers** — không dùng H2 giả lập, để những hành vi phụ thuộc database (khoá bi quan, `ON DUPLICATE KEY UPDATE`, ràng buộc UNIQUE, kiểu dữ liệu) được kiểm chứng đúng như khi chạy production.

Test được tổ chức thành 6 tầng, mỗi tầng trả lời một câu hỏi khác nhau:

### Tầng 1 — Unit & Helper (77 test)
Kiểm thử logic thuần, không cần Spring context.

| Module | Test | Nội dung kiểm thử |
| :--- | :---: | :--- |
| Máy trạng thái đơn hàng | 25 | Toàn bộ ma trận chuyển trạng thái: 5 chuyển hợp lệ được chấp nhận, mọi bước nhảy cóc / lùi / giữ nguyên / huỷ sau khi đã giao đều bị chặn |
| JWT Service | 9 | Token hết hạn, sai chữ ký, thuật toán `alg=none`, dùng nhầm access token cho refresh và ngược lại, chuỗi rác không gây `NullPointerException` |
| Validation (Dish, Order, UserDish, Auth) | 26 | Giá trị `null`, giá trị âm, giá trị biên (0, 1, độ dài mật khẩu 5/6 ký tự) |
| Review Helper | 9 | Ràng buộc điểm đánh giá và nội dung review |
| User Details / User Helper | 8 | Ánh xạ role sang authority, trạng thái tài khoản, xử lý authentication rỗng |

**Đảm bảo:** Mọi quy tắc nghiệp vụ thuần và mọi giá trị biên đều đúng, kể cả các đầu vào bất thường.

### Tầng 2 — Service Component (56 test, dùng Mockito)
Kiểm thử logic nghiệp vụ ở tầng service với repository được mock.

| Module | Test | Nội dung kiểm thử |
| :--- | :---: | :--- |
| Order Service | 16 | Giỏ rỗng, địa chỉ không thuộc người gọi, món hết hàng ở giữa giỏ (rollback), tính tổng tiền, trừ kho từng món, xoá giỏ sau khi đặt, huỷ đơn + hoàn kho, xung đột khoá lạc quan |
| Idempotency Service | 11 | Lần đầu thực hiện, key đã tồn tại (`IN_PROGRESS` / `COMPLETED`), key rỗng, key vượt 36 ký tự, vân tay request không khớp |
| User Dish Service | 8 | Cộng dồn số lượng khi món đã có trong giỏ, kiểm tra tồn kho theo **tổng sau cộng dồn** chứ không phải phần thêm mới, cập nhật số lượng về 0 |
| Address Service | 6 | Trùng địa chỉ, chuyển địa chỉ mặc định, luôn còn đúng một địa chỉ mặc định |
| Dashboard Service | 6 | Chỉ tính doanh thu đơn `DELIVERED`, khoảng thời gian không có đơn, phần trăm thay đổi khi mẫu số bằng 0, món bán chạy đã bị xoá khỏi danh mục |
| Review / User Service | 9 | Ghi đánh giá, khoá tài khoản kèm thu hồi token, cập nhật hồ sơ |

**Đảm bảo:** Luồng nghiệp vụ đúng ở cả nhánh thành công lẫn mọi nhánh lỗi, và các thao tác phụ (trừ kho, xoá giỏ, thu hồi token) thực sự được gọi.

### Tầng 3 — Web / Controller (40 test, MockMvc)
Kiểm thử hợp đồng HTTP: mã trạng thái, cấu trúc JSON, ánh xạ lỗi.

| Module | Test | Nội dung kiểm thử |
| :--- | :---: | :--- |
| Error Response Contract | 15 | Mọi lỗi nghiệp vụ trả về đủ 5 trường, mã lỗi ánh xạ đúng HTTP status đã khai báo, sai kiểu path variable trả 400 |
| Order Controller | 7 | Truyền `Idempotency-Key` xuống service (có và không có header), response không rò rỉ dữ liệu nội bộ, 201 khi tạo thành công |
| Auth Controller | 7 | Body đăng ký hợp lệ/không hợp lệ, logout, thiếu refresh cookie |
| Review / UserDish / AdminDish Controller | 11 | Ràng buộc `@Valid`: thiếu trường bắt buộc, số lượng bằng 0, giá trị biên bằng 1 |

**Đảm bảo:** Client luôn nhận được response có cấu trúc nhất quán, và không có lỗi nào rơi ra ngoài dưới dạng 500 không kiểm soát.

### Tầng 4 — Repository (20 test, `@DataJpaTest` trên MySQL thật)
Kiểm thử câu truy vấn JPQL/native chạy đúng trên MySQL.

| Module | Test | Nội dung kiểm thử |
| :--- | :---: | :--- |
| Refresh Token Repository | 5 | Thu hồi token đang hoạt động, không thu hồi lại token đã thu hồi, không đụng token của người dùng khác |
| Order / OrderDish Repository | 5 | Tổng doanh thu chỉ tính đúng trạng thái, biên khoảng thời gian (bao gồm cả hai đầu), thống kê món bán chạy |
| Flyway Migration | 3 | 8 migration chạy được từ schema trống, schema kết quả khớp với entity (`ddl-auto=validate` không báo lỗi) |
| Review / User / Restaurant / Tag / Idempotency Repository | 7 | Truy vấn theo quan hệ, đếm bản ghi khả dụng, không phát sinh `LazyInitializationException` |

**Đảm bảo:** Truy vấn không chỉ biên dịch được mà chạy đúng ngữ nghĩa trên MySQL — điều mà H2 không đảm bảo được.

### Tầng 5 — System / End-to-End (32 test, Spring context đầy đủ + filter chain thật)
Kiểm thử hệ thống qua HTTP với Spring Security thật, không tắt filter.

| Module | Test | Nội dung kiểm thử |
| :--- | :---: | :--- |
| Security Rules | 10 | Ma trận endpoint × vai trò: route public gọi được khi ẩn danh, route thường trả 401, route admin trả 403 với user thường, JWT ký sai khoá bị từ chối, header `Basic` không gây 500 |
| Order Idempotency | 5 | Cùng key + cùng body trả về đúng response cũ, cùng key + khác body trả 422, key quá 36 ký tự trả 400, key đúng 36 ký tự được chấp nhận, người khác không đọc được đơn qua key của mình |
| Order Stock Accounting | 4 | Đặt hàng trừ kho và xoá giỏ, huỷ đơn hoàn kho, một món không khả dụng thì rollback toàn bộ, đặt đúng bằng số lượng tồn kho |
| Order Ownership | 4 | Không xem/huỷ được đơn của người khác, địa chỉ của người khác bị từ chối |
| Token Lifecycle | 5 | Đổi mật khẩu vô hiệu hoá refresh token cũ, tài khoản bị khoá bị chặn ngay dù access token còn hạn, thuộc tính cookie khi đăng nhập/đăng xuất |
| Refresh Token Reuse | 1 | Dùng lại refresh token đã thu hồi làm vô hiệu hoá cả token đã xoay vòng |
| Admin Order Retention | 2 | Đơn `DELIVERED` không xoá được, đơn `PENDING` xoá được |
| App Context | 1 | Toàn bộ Spring context khởi động thành công |

**Đảm bảo:** Phân quyền, idempotency và tính toàn vẹn tồn kho hoạt động đúng trên toàn bộ ngăn xếp thật, không phải chỉ ở tầng service cô lập.

### Tầng 6 — Concurrency (11 test, đa luồng thật trên cổng HTTP thật)
Kiểm thử các cơ chế chống race condition. Dùng `TestRestTemplate` với `CountDownLatch` đồng bộ điểm xuất phát để các luồng thực sự chạm database cùng lúc.

| Kịch bản | Test | Nội dung kiểm thử |
| :--- | :---: | :--- |
| Bán vượt tồn kho | 3 | Hai khách tranh suất cuối → đúng một người mua được; 5 khách đặt đồng thời khi kho có 5 → cả 5 thành công, kho về 0; đặt hàng và nhập kho đồng thời không mất cập nhật |
| Idempotency đồng thời | 2 | Cùng một key gửi song song → đúng một request thành công; retry sau khi cả hai kết thúc → nhận lại response đã lưu |
| Xung đột trạng thái đơn | 2 | Admin chuyển `PREPARING` và khách huỷ đồng thời → đúng một bên thắng; hai admin cùng thao tác → đúng một bên thắng |
| Thứ tự khoá / Deadlock | 1 | Hai giỏ hàng chứa cùng tập món nhưng thứ tự ngược nhau → cả hai đơn hoàn tất, không deadlock |
| Giỏ hàng đồng thời | 1 | Thêm cùng một món song song → gộp thành đúng một dòng, số lượng cộng dồn chính xác |
| Địa chỉ mặc định | 1 | Hai địa chỉ cùng đặt làm mặc định → còn đúng một địa chỉ mặc định |
| Refresh token đồng thời | 1 | Hai request refresh cùng token → đúng một request thắng |

**Đảm bảo:** Các cơ chế khoá bi quan, khoá lạc quan, upsert nguyên tử và idempotency không chỉ đúng trên lý thuyết mà đứng vững dưới truy cập đồng thời thật.

### Chạy test & xem báo cáo độ phủ

```bash
cd backend && mvn test
```

Test yêu cầu **Docker đang chạy** (Testcontainers cần khởi động container MySQL). Báo cáo JaCoCo được sinh tự động sau khi test chạy xong tại `backend/target/site/jacoco/index.html` — mở file này bằng trình duyệt để xem độ phủ theo từng package, từng class và từng dòng code.

---

## 🗄️ Cơ sở dữ liệu (Database Schema)

Hệ thống có **15 bảng**, quản lý hoàn toàn bằng **8 Flyway migration**. Bên dưới là bản mô tả ngắn gọn về các bảng hiện có:

### 👤 Người dùng & Xác thực

| Bảng | Cột đáng chú ý | Khóa ngoại (`ON DELETE`) | Mô tả |
| :--- | :--- | :--- | :--- |
| `role` | `role_name` (`ENUM('ADMIN','USER')`, unique) | — | Vai trò của người dùng (admin/khách hàng) |
| `user` | `email`, `phone_number` (unique), `password` (hash), `is_active` | → `role.id` (`RESTRICT`) | Tài khoản người dùng |
| `refresh_tokens` | `jti` (unique), `expires_at`, `revoked_at` | → `user.id` | Token cấp lại access token, hỗ trợ thu hồi khi đăng xuất/đổi mật khẩu |
| `address` | `address`, `is_default` | → `user.id` (`CASCADE`) | Địa chỉ giao hàng của người dùng |
| `idempotency_keys` | `scope`, `status`, `request_fingerprint` (SHA-256 hex), `response_body` | chỉ lưu `user_id` dạng snapshot, không ràng buộc FK | Chống xử lý trùng khi client gọi lại API tạo đơn |

### 🏪 Nhà hàng, Món ăn & Phân loại

| Bảng | Cột đáng chú ý | Khóa ngoại (`ON DELETE`) | Mô tả |
| :--- | :--- | :--- | :--- |
| `restaurant` | `name`, `is_available` | — | Nhà hàng bán món ăn |
| `dish` | `price`, `stock_quantity`, `is_available` | → `restaurant.id` (`RESTRICT`) | Món ăn thuộc một nhà hàng |
| `category` | `name` (unique) | — | Danh mục lớn để nhóm các tag |
| `tag` | `name` | → `category.id` (`RESTRICT`) | Nhãn gắn cho món ăn (vd: cay, chay, best-seller) |
| `dish_tag` | — | → `dish.id` (`CASCADE`), → `tag.id` (`CASCADE`) | Bảng nối n–n giữa `dish` và `tag` |
| `image` | `url`, `is_thumbnail`, `public_id`/`format`/`width`/`height` (Cloudinary) | → `dish.id` (`CASCADE`) | Ảnh của món ăn, lưu trên Cloudinary |

### 🛒 Giỏ hàng, Đơn hàng & Đánh giá

| Bảng | Cột đáng chú ý | Khóa ngoại (`ON DELETE`) | Mô tả |
| :--- | :--- | :--- | :--- |
| `user_dish` | `quantity`, unique `(user_id, dish_id)` | → `user.id` (`CASCADE`), → `dish.id` (`CASCADE`) | Giỏ hàng của người dùng |
| `orders` | `version` (optimistic lock), `status` (`ENUM`), `total_price`, `delivery_address` | → `user.id` (`RESTRICT`) | Đơn hàng |
| `order_dish` | `quantity`, `price` (chốt tại thời điểm đặt), unique `review_id` | → `orders.id` (`CASCADE`), → `dish.id` (`RESTRICT`), → `review.id` (`SET NULL`, 1–1) | Từng món trong một đơn hàng |
| `review` | `rating` (1–5), `comment` | — | Đánh giá của khách cho một món trong đơn hàng |

### Quyết định thiết kế đáng chú ý

- **`RESTRICT`** ở `user.role_id`, `dish.restaurant_id`, `orders.user_id`, `order_dish.dish_id`: chặn xoá bản ghi cha nếu còn đơn hàng/món tham chiếu, tránh mất dấu vết lịch sử và doanh thu.
- **`CASCADE`** ở `address`, `user_dish`, `dish_tag`, `image`, `order_dish` (theo `orders`): các bảng này không có giá trị tồn tại độc lập với bảng cha nên xoá cha thì xoá theo.
- **`order_dish.price`** lưu giá tại thời điểm đặt, tách biệt với `dish.price` — đơn hàng cũ không đổi giá khi món tăng/giảm giá sau này.
- **`orders.version`**: cột optimistic locking (`@Version`), chống mất cập nhật khi admin và khách cùng đổi trạng thái một đơn.
- **Unique `(user_id, dish_id)`** trên `user_dish` (thêm ở `V8`): đảm bảo mỗi người dùng chỉ có đúng một dòng giỏ hàng cho mỗi món, phục vụ upsert nguyên tử khi cộng dồn số lượng.
- **`idempotency_keys`**: không có FK tới user, chỉ lưu user_id dạng snapshot — key + fingerprint (SHA-256 của body) dùng để phát hiện request trùng.

---

## ⚙️ Cấu hình biến môi trường (`.env`)

### `.env` (thư mục gốc — điều phối Docker Compose)

| Biến môi trường | Giá trị mặc định | Giải thích |
| :--- | :--- | :--- |
| `MYSQL_HOST_PORT` | `3307` | Cổng MySQL map ra máy host |
| `SPRINGBOOT_HOST_PORT` | `8080` | Cổng Backend map ra máy host |
| `NEXTJS_HOST_PORT` | `3000` | Cổng Frontend map ra máy host |
| `SPRING_URL` | `http://food-app-backend:8080` | URL nội bộ Frontend gọi tới Backend (server-side) |
| `NEXT_PUBLIC_SPRING_URL` | `http://localhost:8080/api/v1` | URL Backend expose ra trình duyệt (client-side) |

### `backend/.env` (không commit — tự tạo khi setup)

| Biến môi trường | Giá trị mặc định (dev) | Giải thích |
| :--- | :--- | :--- |
| `SERVER_PORT` | `8080` | Cổng ứng dụng Spring Boot |
| `API_PREFIX` | `/api/v1` | Tiền tố chung cho tất cả API route |
| `JWT_ACCESS_TOKEN_SECRET` | *(chuỗi base64, tự sinh)* | Khoá ký access token |
| `JWT_ACCESS_TOKEN_EXPIRATION` | *(mili giây)* | Thời hạn access token |
| `JWT_REFRESH_TOKEN_SECRET` | *(chuỗi base64, tự sinh — phải khác access secret)* | Khoá ký refresh token |
| `JWT_REFRESH_TOKEN_EXPIRATION` | *(mili giây)* | Thời hạn refresh token |
| `COOKIE_SECURE` | `false` | `true` khi chạy production qua HTTPS |
| `ADMIN_EMAIL` / `ADMIN_PASSWORD` | `admin@foodie.local` / *(đổi giá trị thật)* | Tài khoản admin được seed tự động lần đầu khởi động |
| `ADMIN_FULL_NAME` / `ADMIN_PHONE_NUMBER` | `System Admin` / `0900000000` | Thông tin tài khoản admin seed |
| `SPRING_SECURITY_USER_NAME` / `SPRING_SECURITY_USER_PASSWORD` | `root` / *(đổi giá trị thật)* | Tài khoản Spring Security mặc định |
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://mysql:3306/foodie` | Connection string tới MySQL |
| `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD` | `root` / *(mật khẩu MySQL)* | Tài khoản kết nối MySQL |
| `FRONTEND_URL` | `http://food-website:3000` | URL Frontend, dùng để cấu hình CORS |
| `MYSQL_DATABASE` | `foodie` | Tên database |
| `MYSQL_ROOT_PASSWORD` | *(mật khẩu MySQL root)* | Mật khẩu root MySQL |

---

## 👥 Nhóm thực hiện - Nhóm 23

| STT | Họ và Tên | MSSV |
| :---: | :--- | :---: |
| 1 | Lê Võ | N22DCCN097 |
| 2 | Trần Nhật Nguyên | N22DCCN057 |
| 3 | Nguyễn Khánh Thiện | N22DCCN081 |
