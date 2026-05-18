# LinhNamStore — Jakarta EE E-Commerce Web Application

> Đồ án môn học — Website thương mại điện tử bán thiết bị lưu trữ, mạng & phụ kiện

## Mục lục

- [Tổng quan](#tổng-quan)
- [Công nghệ sử dụng](#công-nghệ-sử-dụng)
- [Cấu trúc dự án](#cấu-trúc-dự-án)
- [Kiến trúc phần mềm](#kiến-trúc-phần-mềm)
- [Cơ sở dữ liệu](#cơ-sở-dữ-liệu)
- [Các module chính](#các-module-chính)
- [Luồng nghiệp vụ](#luồng-nghiệp-vụ)
- [Bảo mật & xác thực](#bảo-mật--xác-thực)
- [Tích hợp thanh toán](#tích-hợp-thanh-toán)
- [CI/CD](#cicd)
- [Hướng dẫn cài đặt](#hướng-dẫn-cài-đặt)
- [Thông tin nhóm](#thông-tin-nhóm)

---

## Tổng quan

**LinhNamStore** là website thương mại điện tử bán lẻ thiết bị công nghệ (SSD, HDD, NAS, router, phụ kiện). Dự án được xây dựng theo mô hình **MVC tùy biến** trên nền **Jakarta EE 9.1 Servlet**, không sử dụng Spring hay bất kỳ DI framework nào — mọi dependency được khởi tạo trực tiếp bằng `new`.

### Tính năng chính

| Người dùng | Quản trị viên |
|---|---|
| Đăng ký / đăng nhập / quên mật khẩu | CRUD thương hiệu, sản phẩm, người dùng |
| Duyệt sản phẩm theo danh mục | Quản lý đơn hàng, voucher |
| Tìm kiếm & autocomplete | Dashboard thống kê |
| Giỏ hàng (thêm/sửa/xóa) | Quản lý tồn kho |
| Đặt hàng & chọn phương thức thanh toán | Xem chi tiết đơn hàng |
| Thanh toán qua Sepay.vn (QR/bank transfer) | Đổi trạng thái đơn hàng |
| Xem lịch sử đơn hàng | |
| Quản lý profile & wishlist | |

---

## Công nghệ sử dụng

| Thành phần | Công nghệ | Phiên bản |
|---|---|---|
| **Nền tảng** | Jakarta EE (Servlet 5.0, JSP 3.0, JSTL, JSON-B, Jakarta Mail) | 9.1-web |
| **JDK** | Java SE | 11 (source), 17 (CI) |
| **Application Server** | GlassFish | 6.1.0 |
| **Build Tool** | Apache Ant (qua NetBeans) | — |
| **Database** | MySQL | 8.x |
| **JDBC** | MySQL Connector/J | — |
| **IDE** | Apache NetBeans | — |
| **CI/CD** | GitHub Actions | — |
| **Payment Gateway** | Sepay.vn | — |
| **CSS/JS** | Custom (không dùng framework) | — |
| **Image Hosting** | Unsplash (external URLs) | — |

### Tại sao không dùng Spring?

Dự án tuân thủ yêu cầu môn học: Jakarta EE Servlet thuần, không DI container. Pattern dependency injection thủ công qua constructor/field initialization.

---

## Cấu trúc dự án

```
WebApplication3/
├── .env                          ← Biến môi trường (DB, SMTP, JWT)
├── .gitignore
├── build.xml                     ← Ant build script
├── sto.sql                       ← Database schema + seed data (30+ sản phẩm)
├── index.html                    ← Redirect → /home
│
├── .github/workflows/
│   └── ci.yml                    ← GitHub Actions CI (syntax check)
│
├── src/
│   ├── conf/MANIFEST.MF
│   └── java/
│       ├── common/               ← Hạ tầng chung (annotation, controller, filter, guard)
│       │   ├── annotation/       ← @Public, @RequiresRole, @Route, @User
│       │   ├── controller/       ← BaseController (reflection-based routing)
│       │   ├── exceptionFilter/  ← GlobalExceptionFilter
│       │   ├── guard/            ← AuthGuard (xác thực & phân quyền)
│       │   ├── logger/           ← AppLogger
│       │   ├── retry/            ← Retry framework (backoff strategies)
│       │   └── type/             ← Result<T>, UserPayload
│       │
│       ├── entity/               ← 13 Entity classes (User, Product, Order, ...)
│       │
│       └── module/
│           ├── core/             ← Module lõi
│           │   ├── config/       ← ConfigService (.env parser), DbConfig, AppConfig
│           │   ├── sql/          ← JdbcHelper, RowMapper
│           │   ├── auth/         ← AuthController, AuthService, PasswordService
│           │   ├── user/         ← UserController, UserService, UserRepository
│           │   ├── outbox/       ← OutBoxService, TypeEvent (event-driven email)
│           │   ├── mail/         ← EmailService (SMTP/Gmail)
│           │   └── page/         ← Public page, Admin page, Profile, Wishlist controllers
│           │
│           └── bussiness/        ← Module nghiệp vụ
│               ├── product/      ← ProductController, ProductService, VariantService
│               ├── cart/         ← CartController, CartService
│               └── order/        ← OrderController, OrderService, SepayWebhookController
│
├── web/
│   ├── index.html
│   ├── migrate-order.jsp         ← Runtime schema migration
│   ├── assets/
│   │   ├── css/style.css
│   │   └── js/app.js
│   ├── layouts/                  ← header.jsp, footer.jsp, main-layout.jsp
│   ├── pages/                    ← home, cart, checkout, pay, profile, wishlist, ...
│   ├── views/
│   │   ├── auth/                 ← login, register, forgot-password
│   │   ├── admin/                ← brands, products, users, vouchers, orders, dashboard
│   │   └── order/                ← detail, history
│   └── WEB-INF/
│       └── glassfish-web.xml
│
└── nbproject/                    ← NetBeans project config
    ├── project.properties
    ├── project.xml
    ├── build-impl.xml
    └── ant-deploy.xml
```

**Tổng cộng: 94 file Java**, 30+ file JSP, 13 bảng database.

---

## Kiến trúc phần mềm

### Mô hình MVC tùy biến

```
HTTP Request
    │
    ▼
┌─────────────────┐
│  GlobalException│ ← Filter bắt mọi exception
│     Filter      │
└────────┬────────┘
         ▼
┌─────────────────┐
│    AuthGuard    │ ← Kiểm tra session, @RequiresRole
└────────┬────────┘
         ▼
┌─────────────────┐
│  BaseController │ ← Reflection trên @Route → dispatch tới method
└────────┬────────┘
         ▼
┌─────────────────┐     ┌──────────────┐     ┌──────────────┐
│   Controller    │────▶│   Service    │────▶│  Repository  │
│  (@WebServlet)  │     │ (business)   │     │  (JDBC raw)  │
└─────────────────┘     └──────────────┘     └──────┬───────┘
         │                                           │
         ▼                                           ▼
┌─────────────────┐     ┌──────────────┐     ┌──────────────┐
│  Request DTO    │     │ Response DTO │     │  JdbcHelper  │
│  (input)        │     │ (output)     │     │  (SQL exec)  │
└─────────────────┘     └──────────────┘     └──────────────┘
```

### Reflection-based Routing

`BaseController` sử dụng annotation `@Route(path, method)` để tự động map HTTP request → method:

```java
@Route(path = "detail", method = "GET")
public void handleDetail(HttpServletRequest req, HttpServletResponse resp) { ... }
```

Không cần khai báo từng servlet trong `web.xml` — mọi action dồn vào 1 `@WebServlet`.

### DTO Pattern

- **Request DTO** (`module.xxx.dto`): Nhận dữ liệu từ `request.getParameter()`
- **Response DTO** (`module.xxx.response_dto`): Kết quả nghiệp vụ, kế thừa `BaseResponse` với `isSuccess()`, `getErrorMessage()`, `getSuccessMessage()`
- **View DTO** (Cart, Product): ViewModel cho JSP display

### OutBox Pattern

Thay vì gửi email đồng bộ, hệ thống dùng **OutBox table** để lưu event → xử lý bất đồng bộ:

```
User đăng ký → OutBoxService.publishEvent() → OutBoxRepository.insert()
                                                   │
                                                   ▼
                                          OutBoxService.processPending()
                                                   │
                                                   ▼
                                          EmailService.send()
```

7 loại event: `USER_REGISTERED`, `USER_CREATED`, `PASSWORD_RESET_REQUESTED`, `ORDER_CREATED`, `PAYMENT_COMPLETED`, `USER_BANNED`, `ORDER_STATUS_UPDATED`

---

## Cơ sở dữ liệu

### Tổng quan

- **Engine**: MySQL InnoDB
- **Charset**: utf8mb4_unicode_ci
- **Primary Key**: CHAR(36) — UUID format
- **13 bảng** với foreign key constraints

### Sơ đồ quan hệ

```
User (1) ──┬── (N) Brand ── (N) Product ── (N) ProductVariant
           │                                                   │
           ├── (N) OutBox                                      │
           ├── (N) Session                                     │
           ├── (N) Voucher                                     │
           ├── (1) OrderCart ── (N) ItemCart ──────────────────┘
           │                                    │
           ├── (N) Order ───────────────────────┘
           │        │
           │        └── (N) Payment
           │
           └── (N) SavedProduct
```

### Chi tiết từng bảng

#### 1. `User` — Người dùng

| Column | Type | Mô tả |
|---|---|---|
| `id` | CHAR(36) | UUID primary key |
| `name` | VARCHAR(255) | Họ tên |
| `email` | VARCHAR(255) | Unique, đăng nhập |
| `hashPassword` | VARCHAR(255) | PBKDF2WithHmacSHA256 (120k iterations) |
| `dateOfBirth` | DATE | Ngày sinh |
| `role` | ENUM('ADMIN','USER') | Phân quyền |
| `status` | ENUM('PENDING','ACTIVE','INACTIVE','BANNED') | Trạng thái |
| `createdAt` | DATETIME | Tự động |
| `updatedAt` | DATETIME | Auto-update |

**Seed data**: 1 admin account (`admin@linhnamstore.local` / `Admin@123!`)

#### 2. `Brand` — Thương hiệu

| Column | Type | Mô tả |
|---|---|---|
| `id` | CHAR(36) | UUID |
| `name` | VARCHAR(255) | Tên thương hiệu |
| `description` | VARCHAR(255) | Mô tả |
| `userId` | CHAR(36) | FK → User (người tạo) |
| `status` | ENUM('ACTIVE','INACTIVE') | |

**Seed data**: 5 brands — Samsung, Western Digital, Synology, TP-Link, SanDisk

#### 3. `Product` — Sản phẩm

| Column | Type | Mô tả |
|---|---|---|
| `id` | CHAR(36) | UUID |
| `name` | VARCHAR(255) | Tên sản phẩm |
| `description` | VARCHAR(255) | Mô tả |
| `brandId` | CHAR(36) | FK → Brand |
| `userId` | CHAR(36) | FK → User |
| `category` | ENUM('STORAGE_DEVICE','NETWORK_DEVICE','ACCESSORY') | Danh mục |
| `status` | ENUM('DRAFT','ACTIVE','INACTIVE','ARCHIVED') | |

**Seed data**: 30 sản phẩm (10 gốc + 20 bổ sung)

#### 4. `ProductVariant` — Biến thể sản phẩm

| Column | Type | Mô tả |
|---|---|---|
| `id` | CHAR(36) | UUID |
| `productId` | CHAR(36) | FK → Product |
| `price` | DECIMAL(12,2) | Giá VND |
| `sku` | VARCHAR(255) | Unique stock keeping unit |
| `quantity` | INT | Tồn kho |
| `imageUrl` | VARCHAR(255) | URL hình ảnh |
| `status` | ENUM('ACTIVE','INACTIVE','OUT_OF_STOCK') | |

**Seed data**: ~65 variants (mỗi sản phẩm 2-3 variants)

#### 5. `Order` — Đơn hàng

| Column | Type | Mô tả |
|---|---|---|
| `id` | CHAR(36) | UUID |
| `userId` | CHAR(36) | FK → User |
| `productId` | CHAR(36) | FK → Product |
| `variantId` | CHAR(36) | FK → ProductVariant (nullable) |
| `quantity` | INT | Số lượng |
| `phone` | VARCHAR(20) | SĐT người nhận |
| `address` | VARCHAR(500) | Địa chỉ giao |
| `status` | ENUM('PENDING','CONFIRMED','SHIPPING','COMPLETED','CANCELLED') | |

#### 6. `Payment` — Thanh toán

| Column | Type | Mô tả |
|---|---|---|
| `id` | CHAR(36) | UUID |
| `orderId` | CHAR(36) | FK → Order |
| `userId` | CHAR(36) | FK → User |
| `amount` | DECIMAL(12,2) | Số tiền |
| `partnerCode` | VARCHAR(255) | Mã đối tác thanh toán |
| `signature` | VARCHAR(255) | Chữ ký xác thực |
| `status` | ENUM('PENDING','SUCCESS','FAILED','CANCELLED') | |
| ... | | Các trường Sepay/MoMo compatible |

#### 7. `OrderCart` — Giỏ hàng (1 giỏ / user)

| Column | Type | Mô tả |
|---|---|---|
| `id` | CHAR(36) | UUID |
| `userId` | CHAR(36) | FK → User, UNIQUE |

#### 8. `ItemCart` — Items trong giỏ

| Column | Type | Mô tả |
|---|---|---|
| `id` | CHAR(36) | UUID |
| `cartId` | CHAR(36) | FK → OrderCart (ON DELETE CASCADE) |
| `productId` | CHAR(36) | FK → Product |
| `variantId` | CHAR(36) | FK → ProductVariant (nullable) |
| `quantity` | INT | Số lượng |

Unique constraint: `(cartId, productId, variantId)`

#### 9. `Voucher` — Mã giảm giá

| Column | Type | Mô tả |
|---|---|---|
| `id` | CHAR(36) | UUID |
| `percent` | DECIMAL(5,2) | Phần trăm giảm |
| `userId` | CHAR(36) | FK → User |
| `expTime` | DATE | Ngày hết hạn |
| `quantity` | INT | Số lượng |

#### 10. `Session` — Phiên đăng nhập

| Column | Type | Mô tả |
|---|---|---|
| `id` | CHAR(36) | UUID |
| `hashRefreshToken` | VARCHAR(255) | SHA-256 hash của refresh token |
| `userId` | CHAR(36) | FK → User |
| `ip` | VARCHAR(45) | IP client (IPv4/IPv6) |

#### 11. `OutBox` — Event queue

| Column | Type | Mô tả |
|---|---|---|
| `id` | CHAR(36) | UUID |
| `code` | VARCHAR(255) | Mã xác thực / reset code |
| `type` | VARCHAR(100) | Loại event |
| `userId` | CHAR(36) | FK → User |
| `status` | ENUM('PENDING','PROCESSED','FAILED') | |
| `expiresAt` | DATETIME | Thời gian hết hạn |
| `usedAt` | DATETIME | Thời gian sử dụng |

#### 12. `SavedProduct` — Sản phẩm yêu thích

| Column | Type | Mô tả |
|---|---|---|
| `id` | CHAR(36) | UUID |
| `productId` | CHAR(36) | FK → Product |
| `quantity` | INT | |

### Index strategy

Mỗi bảng có composite index cho query thường dùng:
- `OutBox`: `(userId, type, status, createdAt)`, `(userId, type, code, usedAt, expiresAt)`
- `Session`: `(userId, ip, createdAt)`
- `Order`: `userId`, `productId`, `variantId`

---

## Các module chính

### 1. Core — Cấu hình (`module.core.config`)

| Class | Vai trò |
|---|---|
| `ConfigService` | Singleton đọc `.env` file, cache key-value. `get()`, `getInt()`, `require()` |
| `DbConfig` | Tạo JDBC Connection từ `.env`. Auto-repair schema cho Order table |
| `AppConfig` | Constants: JWT secret, expiry, PBKDF2 iterations, page size, Sepay gateway URLs |

### 2. Core — SQL (`module.core.sql`)

| Class | Vai trò |
|---|---|
| `JdbcHelper` | Wrapper JDBC: `executeQuery(sql, RowMapper, params)`, `executeUpdate(sql, params)`, `executeBatch()`, `count()`, `executeRaw()` |
| `RowMapper<T>` | Interface functional: `T map(ResultSet rs)` |

### 3. Core — Auth (`module.core.auth`)

| Class | Vai trò |
|---|---|
| `AuthController` | `@WebServlet("/auth")`, `@Public`. Xử lý: login, register, verify-email, forgot-password, reset-password, logout |
| `AuthService` | Business logic: đăng ký (tạo user + cart + outbox event), đăng nhập (verify password + tạo session), xác thực email, reset mật khẩu |
| `PasswordService` | Hash: PBKDF2WithHmacSHA256, 120000 iterations, 256-bit salt. Verify: timing-safe comparison |
| `AuthTokenService` | Hash refresh token bằng SHA-256 |
| `AuthRepository` | JDBC: findByEmail, findById, saveSession, deleteSession, updatePassword, createCartForUser |

### 4. Core — User (`module.core.user`)

| Class | Vai trò |
|---|---|
| `UserController` | `@WebServlet("/admin/users")`, `@RequiresRole("ADMIN")`. CRUD qua `action` param |
| `UserService` | createUser, getUserById, updateUser, listUsers (pagination), deleteUser, changeStatus |
| `UserRepository` | JDBC: insert, findById, findByEmail, findAll (paginated), countAll, update, updateStatus, delete, createCartForUser |

### 5. Core — OutBox & Mail (`module.core.outbox`, `module.core.mail`)

| Class | Vai trò |
|---|---|
| `OutBoxService` | `publishEvent()` → lưu event, `processPending()` → gửi email qua EmailService |
| `EmailService` | Gửi email qua SMTP Gmail (Jakarta Mail). HTML template cho verification code & password reset |
| `TypeEvent` | Enum 7 loại event |
| `OutBoxRepository` | JDBC: insert, findPending, findValidCode, markProcessed, markFailed, markUsed |

### 6. Business — Product (`module.bussiness.product`)

| Class | Vai trò |
|---|---|
| `ProductController` | `@WebServlet({"/home", "/product", "/products", "/admin/products"})`. Actions: home, search, detail, autocomplete, category, CRUD, variant management |
| `ProductService` | getHomePage, listProducts (paginated), getProductDetail, searchProducts, autocomplete, findByCategory, CRUD, filterProducts (by category, brand, price range, status, sort) |
| `VariantService` | createVariant, updateVariant, deleteVariant |
| `ProductCardView` | ViewModel: id, name, imageUrl, price — dùng cho home page card display |
| `ProductRepository` | JDBC: insert, findById, findActive (paginated), search, findByCategory, findBestSelling, countActive, update, delete |
| `VariantRepository` | JDBC: insert, findById, findBySku, findByProductId, update, delete |
| `BrandRepository` | JDBC: findAll |
| `SavedProductRepository` | Stub — chưa triển khai đầy đủ |

### 7. Business — Cart (`module.bussiness.cart`)

| Class | Vai trò |
|---|---|
| `CartController` | `@WebServlet("/cart")`. Actions: add, update, remove, clear |
| `CartService` | getCart (paginated), addToCart (kiểm tra tồn kho), updateQuantity, removeItem, clearCart |
| `CartItemView` | ViewModel: productId, productName, sku, quantity, stockQuantity, price, getLineTotal() |
| `CartRepository` | JDBC: getOrCreateCart, getItemsByCartId (paginated), findItem, addItem, updateQuantity, removeItem, clearCart, countItems |

### 8. Business — Order (`module.bussiness.order`)

| Class | Vai trò |
|---|---|
| `OrderController` | `@WebServlet({"/checkout", "/order/detail", "/orders", "/admin/orders"})`. Actions: checkout, check/pay (Sepay redirect), detail, history, admin list/detail, update-status, cancel |
| `OrderService` | checkout (tạo order từ cart items), getOrderDetail, getOrderHistory (paginated), listAllOrders, updateStatus, cancelOrder |
| `SepayWebhookController` | `@WebServlet("/api/sepay/webhook")`, `@Public`. Nhận webhook từ Sepay.vn, match order qua mã "DH" trong nội dung chuyển khoản, cập nhật trạng thái PENDING → PAID |
| `SepayWebhookPayload` | DTO nhận webhook Sepay (hỗ trợ cả camelCase và snake_case JSON) |
| `OrderRepository` | JDBC: insert, findById, findByUserId (paginated), findAll (paginated), countAll, updateStatus |

---

## Luồng nghiệp vụ

### Đăng ký tài khoản

```
POST /auth?action=register
    │
    ▼
AuthController.doPost → handleRegister()
    │
    ▼
AuthService.register(RegisterRequestDto)
    ├── Validate: email không trùng, mật khẩu khớp, đúng format
    ├── Hash password (PBKDF2WithHmacSHA256)
    ├── UserRepository.insert(user)
    ├── AuthRepository.createCartForUser(userId)
    └── OutBoxService.publishEvent(USER_REGISTERED)
            │
            ▼
        OutBoxRepository.insert(event)
        EmailService.send(verification code)
```

### Đặt hàng & thanh toán

```
POST /checkout
    │
    ▼
OrderController.doPost → handleCheckout()
    │
    ▼
OrderService.checkout(userId, CheckoutDto)
    ├── Lấy items từ CartService.getCart(userId)
    ├── Validate: tồn kho đủ
    ├── OrderRepository.insert(order) cho mỗi item
    ├── Clear cart
    └── OutBoxService.publishEvent(ORDER_CREATED)
            │
            ▼
        Redirect tới /checkout?action=pay → Sepay QR page

POST /api/sepay/webhook (từ Sepay.vn)
    │
    ▼
SepayWebhookController.doPost()
    ├── Parse SepayWebhookPayload từ JSON
    ├── Extract mã "DH" từ transfer content
    ├── Tìm order có mã phù hợp, status = PENDING
    └── OrderRepository.updateStatus(orderId, "PAID")
```

---

## Bảo mật & xác thực

### Authentication Flow

1. **Login**: Email + password → verify PBKDF2 hash → tạo refresh token → SHA-256 hash → lưu Session table → set session attribute `currentUser` (UserPayload)
2. **AuthGuard**: Intercept mọi request (trừ `@Public`). Kiểm tra session `currentUser`. Nếu `@RequiresRole("ADMIN")` → check role
3. **CSRF Protection**: BaseController tự động generate/validate CSRF token cho POST requests
4. **Session Management**: Session table lưu hash refresh token + IP address

### Password Security

- **Algorithm**: PBKDF2WithHmacSHA256
- **Iterations**: 120,000
- **Salt**: 256-bit random
- **Format**: `pbkdf2:iterations:salt:hash`

### Retry Protection

Hệ thống retry với 3 chiến lược backoff:
- `FIXED` — delay cố định
- `EXPONENTIAL` — delay tăng theo cấp số nhân
- `FIBONACCI` — delay theo dãy Fibonacci

---

## Tích hợp thanh toán

### Sepay.vn Gateway

- **Type**: Bank transfer / QR code payment
- **Webhook endpoint**: `POST /api/sepay/webhook`
- **Matching logic**: Parse nội dung chuyển khoản, tìm mã đơn hàng prefix "DH", so khớp với order PENDING
- **Payload**: Hỗ trợ cả camelCase (`transferAmount`) và snake_case (`transfer_amount`) JSON keys

### Flow thanh toán

```
User checkout → Redirect Sepay QR page
    │
    ▼
User quét QR & chuyển khoản
    │
    ▼
Sepay gửi webhook → /api/sepay/webhook
    │
    ▼
Match order by "DH" code → Update status → PAID
    │
    ▼
OutBoxService.publishEvent(PAYMENT_COMPLETED)
```

---

## CI/CD

### GitHub Actions (`.github/workflows/ci.yml`)

| Thuộc tính | Giá trị |
|---|---|
| **Trigger** | Push/PR to `main`, `master`, `dev` |
| **JDK** | Temurin 17 |
| **Job** | Download Jakarta EE 9.1 API jar → `javac` compile tất cả `src/java/**/*.java` |
| **Test** | Chưa có test |

### Build local

```bash
# Build qua Ant (NetBeans)
ant build      # Compile & tạo WAR
ant run        # Deploy lên GlassFish
ant clean      # Xóa build artifacts
```

---

## Hướng dẫn cài đặt

### Yêu cầu

- JDK 11+ (khuyến nghị 17)
- GlassFish 6.1.0
- MySQL 8.x
- Apache NetBeans (khuyến nghị)

### Bước 1: Tạo database

```sql
CREATE DATABASE ecommerce CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ecommerce;
SOURCE sto.sql;
```

### Bước 2: Cấu hình `.env`

Copy file `.env` và chỉnh sửa:

```env
DB_HOST=localhost
DB_PORT=3306
DB_NAME=ecommerce
DB_USER=root
DB_PASSWORD=your_password

SMTP_USER=your_email@gmail.com
SMTP_PASS=your_app_password

APP_BASE_URL=http://localhost:8080/WebApplication3
```

### Bước 3: Thêm MySQL Connector

Copy `mysql-connector-java-8.x.x.jar` vào `web/WEB-INF/lib/`

### Bước 4: Deploy

**Cách 1 — NetBeans:**
1. Mở project trong NetBeans
2. Right-click → Deploy
3. GlassFish tự động start & deploy

**Cách 2 — Manual:**
1. `ant build` → tạo `dist/WebApplication3.war`
2. Copy WAR vào `glassfish/domains/domain1/autodeploy/`

### Bước 5: Truy cập

- **Trang chủ**: `http://localhost:8080/WebApplication3/home`
- **Admin**: `http://localhost:8080/WebApplication3/admin/dashboard`
- **Đăng nhập admin**: `admin@linhnamstore.local` / `Admin@123!`

### Tài khoản mặc định

| Role | Email | Mật khẩu |
|---|---|---|
| ADMIN | admin@linhnamstore.local | Admin@123! |

---

## Thông tin nhóm

> Điền thông tin thành viên vào đây

| STT | Họ tên | MSSV | Vai trò |
|---|---|---|---|
| 1 | | | |
| 2 | | | |
| 3 | | | |
| 4 | | | |

---

`★ Insight ─────────────────────────────────────`
- **Reflection-based routing** thay vì `web.xml` mapping: `BaseController` quét annotation `@Route` trên method → tự dispatch HTTP request. Giảm boilerplate, dễ mở rộng action mới.
- **OutBox pattern** đảm bảo email luôn được gửi ngay cả khi SMTP down: event được lưu DB trước, xử lý sau với retry. Đây là pattern phổ biến trong microservices để đảm bảo eventual consistency.
- **Không dùng DI framework** → dependency được `new` trực tiếp trong constructor/field. Đơn giản cho đồ án nhưng khó test unit — không thể mock dependency dễ dàng.
`─────────────────────────────────────────────────`
