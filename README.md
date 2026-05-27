# LinhNamStore — Jakarta EE E-Commerce Platform

> Website thương mại điện tử bán thiết bị lưu trữ, mạng & phụ kiện — Đồ án môn học

## Mục lục

- [Tổng quan](#tổng-quan)
- [Công nghệ](#công-nghệ)
- [Kiến trúc hệ thống](#kiến-trúc-hệ-thống)
- [Cấu trúc dự án](#cấu-trúc-dự-án)
- [Cơ sở dữ liệu](#cơ-sở-dữ-liệu)
- [Module chi tiết](#module-chi-tiết)
- [Luồng nghiệp vụ](#luồng-nghiệp-vụ)
- [Bảo mật](#bảo-mật)
- [Tích hợp thanh toán](#tích-hợp-thanh-toán)
- [AI Chat](#ai-chat)
- [CI/CD & Docker](#cicd--docker)
- [Cài đặt & vận hành](#cài-đặt--vận-hành)
- [Thông tin nhóm](#thông-tin-nhóm)

---

## Tổng quan

**LinhNamStore** là nền tảng thương mại điện tử hoàn chỉnh, xây dựng theo mô hình **MVC tùy biến** trên **Jakarta EE 9.1 Servlet**, không sử dụng Spring hay bất kỳ DI framework nào. Dự án thể hiện khả năng tự xây dựng framework web từ nền tảng Servlet thuần, với hệ thống routing dựa trên annotation, xác thực session-based, và tích hợp thanh toán qua ngân hàng.

### Tính năng người dùng

| Tính năng | Mô tả |
|---|---|
| Đăng ký / Đăng nhập | Email + password, xác thực qua email, PBKDF2 hash |
| Quên mật khẩu | Gửi mã OTP qua email, reset mật khẩu |
| Duyệt sản phẩm | Trang chủ, danh mục, tìm kiếm, autocomplete, lọc (brand, giá, trạng thái, sort) |
| Xem chi tiết | Thông tin sản phẩm, variants (SKU, giá, tồn kho), đánh giá |
| Giỏ hàng | Thêm/sửa/xóa/clear, kiểm tra tồn kho, pagination |
| Wishlist | Lưu sản phẩm yêu thích |
| Đặt hàng | Checkout với thông tin giao hàng, chọn phương thức thanh toán |
| Thanh toán | QR/Bank transfer qua Sepay.vn |
| Lịch sử đơn hàng | Xem chi tiết, trạng thái, hủy đơn |
| Profile | Quản lý thông tin cá nhân, đổi mật khẩu |
| Liên hệ | Gửi form liên hệ, lưu database |
| AI Chat | Trợ lý AI tư vấn sản phẩm, ngữ cảnh cửa hàng |

### Tính năng quản trị

| Tính năng | Mô tả |
|---|---|
| Dashboard | Thống kê doanh thu, đơn hàng, người dùng, sản phẩm, biểu đồ |
| Quản lý sản phẩm | CRUD sản phẩm, variants (SKU, giá, tồn kho, hình ảnh) |
| Quản lý thương hiệu | CRUD brand, trạng thái active/inactive |
| Quản lý người dùng | CRUD, phân quyền, ban/unban, pagination |
| Quản lý đơn hàng | Danh sách, chi tiết, cập nhật trạng thái, hủy đơn |
| Quản lý voucher | CRUD mã giảm giá, phần trăm, số lượng, hạn dùng |
| Quản lý tồn kho | Theo dõi số lượng variant, cảnh báo hết hàng |
| Đánh giá sản phẩm | Duyệt, phê duyệt/reject đánh giá |

---

## Công nghệ

| Thành phần | Công nghệ | Ghi chú |
|---|---|---|
| **Backend** | Jakarta EE 9.1 (Servlet 5.0, JSP 3.0, JSTL, JSON-B, Jakarta Mail) | Không Spring, không DI framework |
| **JDK** | Java 17 (source 11, CI 17) | |
| **Application Server** | GlassFish 6.1.0 / Payara 6 | Local dev: GlassFish, Docker: Payara |
| **Database** | MySQL 8.0 | InnoDB, utf8mb4, HikariCP connection pool |
| **Build Tool** | Apache Ant (NetBeans) | `build.xml` |
| **IDE** | Apache NetBeans | |
| **CI/CD** | GitHub Actions | Syntax check + auto deploy VPS |
| **Payment** | Sepay.vn | Bank transfer / QR code |
| **AI** | OpenAI-compatible API | Trợ lý chat tư vấn sản phẩm |
| **Frontend** | Custom CSS/JS | Không framework, JSP layout composition |
| **Container** | Docker + Docker Compose | Payara Server + MySQL |

### Thư viện bổ sung

| Thư viện | Vai trò |
|---|---|
| HikariCP 5.1.0 | Connection pooling |
| MySQL Connector/J 8.0.33 | JDBC driver |

### Tại sao không dùng Spring?

Dự án tuân thủ yêu cầu môn học: **Jakarta EE Servlet thuần**. Mọi dependency injection thực hiện thủ công qua `new` trong constructor/field. Điều này giúp hiểu sâu cơ chế Servlet, filter, session management mà không phụ thuộc framework.

---

## Kiến trúc hệ thống

### Request pipeline

```
HTTP Request
    │
    ▼
┌──────────────────────────┐
│  GlobalExceptionFilter   │  ← @WebFilter("/*"), bắt mọi exception
│  (error handling)        │     log error, forward error page
└────────────┬─────────────┘
             ▼
┌──────────────────────────┐
│       AuthGuard          │  ← Filter kiểm tra session
│  (authentication)        │     @Public → bỏ qua
│                          │     @RequiresRole → check role
└────────────┬─────────────┘
             ▼
┌──────────────────────────┐
│     BaseController       │  ← Abstract HttpServlet
│  (routing + CSRF)        │     @Route scan → method dispatch
│                          │     CSRF auto-generate/validate
└────────────┬─────────────┘
             ▼
┌──────────────────────────┐     ┌──────────────┐     ┌──────────────┐
│      Controller          │────▶│   Service    │────▶│  Repository  │
│  (@WebServlet)           │     │ (business)   │     │  (JDBC)      │
└──────────────────────────┘     └──────────────┘     └──────┬───────┘
         │                                                   │
         ▼                                                   ▼
┌──────────────────┐     ┌───────────────┐     ┌──────────────────┐
│  Request DTO     │     │ Response DTO  │     │  JdbcHelper      │
│  (input)         │     │ (output)      │     │  (SQL executor)  │
└──────────────────┘     └───────────────┘     └──────────────────┘
```

### Routing mechanism

**BaseController** sử dụng reflection để quét annotation `@Route(method, path)` trên method → tự động dispatch HTTP request. Không cần khai báo servlet riêng cho từng action — mọi action dồn vào 1 `@WebServlet`.

Tuy nhiên, phần lớn controller dùng pattern `action` parameter:

```java
@Override
protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
    String action = req.getParameter("action");
    switch (action != null ? action : "list") {
        case "detail" -> handleDetail(req, resp);
        case "search" -> handleSearch(req, resp);
        default -> handleList(req, resp);
    }
}
```

### CSRF Protection

- `BaseController` tự động generate token cho mỗi session
- Validate trên mọi unsafe method (POST/PUT/PATCH/DELETE)
- API paths (`/api/*`) được miễn CSRF
- Form field: `csrfToken` hoặc header: `X-CSRF-Token`

### Session attributes

| Key | Type | Mô tả |
|---|---|---|
| `currentUser` | `UserPayload` | userId, email, role, name |
| `sessionId` | String | Session identifier |
| `csrfToken` | String | CSRF protection token |
| `cartCount` | Integer | Số lượng item trong giỏ |

### Retry framework

Hệ thống retry với 3 chiến lược backoff:

| Strategy | Mô tả |
|---|---|
| `FIXED` | Delay cố định giữa các lần retry |
| `EXPONENTIAL` | Delay tăng theo cấp số nhân |
| `FIBONACCI` | Delay theo dãy Fibonacci |

Cấu hình: max attempts, initial delay, max delay, retry predicate (điều kiện retry).

### OutBox Pattern

Thay vì gửi email đồng bộ, hệ thống dùng **OutBox table** để lưu event → xử lý bất đồng bộ:

```
User đăng ký → OutBoxService.publishEvent()
    → OutBoxRepository.insert(event)
    → OutBoxService.processPending()
    → EmailService.send()
```

7 loại event:

| Event | Kích hoạt |
|---|---|
| `USER_REGISTERED` | Đăng ký tài khoản mới |
| `USER_CREATED` | Admin tạo user |
| `PASSWORD_RESET_REQUESTED` | Quên mật khẩu |
| `ORDER_CREATED` | Đặt hàng thành công |
| `PAYMENT_COMPLETED` | Thanh toán qua Sepay |
| `USER_BANNED` | Admin ban user |
| `ORDER_STATUS_UPDATED` | Admin đổi trạng thái đơn |

### DTO Pattern

| Layer | Package | Vai trò |
|---|---|---|
| **Request DTO** | `module.xxx.dto` | Nhận dữ liệu từ `request.getParameter()`, không validation annotation |
| **Response DTO** | `module.xxx.response_dto` | Kết quả nghiệp vụ, extends `BaseResponse` (`isSuccess()`, `getErrorMessage()`, `getSuccessMessage()`) |
| **View DTO** | `module.xxx` | ViewModel cho JSP display (`ProductCardView`, `CartItemView`, `WishlistItemView`) |
| **Entity** | `entity` | POJO domain model, map database row |
| **Result<T>** | `common.type` | Generic success/failure wrapper cho API response |

---

## Cấu trúc dự án

```
WebApplication3/
├── .env                          ← Biến môi trường (DB, SMTP, AI, App URL)
├── .gitignore
├── build.xml                     ← Ant build script
├── sto.sql                       ← Database schema + seed data (14 bảng, 30+ sản phẩm)
├── Dockerfile                    ← Payara Server 6 + JDK 17
├── docker-compose.yml            ← MySQL 8.0 + App
├── index.html                    ← Redirect → /home
│
├── .github/workflows/
│   ├── ci.yml                    ← CI: syntax check JDK 17
│   └── deploy.yml                ← CD: build WAR, scp VPS, docker deploy
│
├── lib/                          ← External JARs
│   ├── HikariCP-5.1.0.jar
│   ├── mysql-connector-j-8.0.33.jar
│   └── org-netbeans-modules-*.jar
│
├── src/java/
│   ├── common/                   ← Hạ tầng chung (framework-level)
│   │   ├── annotation/           ← @Public, @RequiresRole, @Route, @User
│   │   ├── controller/           ← BaseController (reflection routing, CSRF)
│   │   ├── exceptionFilter/      ← GlobalExceptionFilter (catch-all)
│   │   ├── guard/                ← AuthGuard (session auth + role check)
│   │   ├── logger/               ← AppLogger (java.util.logging wrapper)
│   │   ├── retry/                ← Retry framework (3 backoff strategies)
│   │   └── type/                 ← Result<T>, UserPayload
│   │
│   ├── entity/                   ← 14 Entity classes (UUID PKs)
│   │   ├── BrandEntity, ItemCartEntity, OrderCartEntity, OrderEntity
│   │   ├── OutBoxEntity, PaymentEntity, ProductEntity, ProductReviewEntity
│   │   ├── ProductVariantEntity, SavedProductEntity, SessionEntity
│   │   ├── UserEntity, VoucherEntity, ReviewView
│   │
│   └── module/
│       ├── core/                 ← Module lõi (infrastructure)
│       │   ├── admin/            ← AdminAnalyticsService, AdminOrderController
│       │   ├── auth/             ← AuthController, AuthService, PasswordService
│       │   ├── config/           ← ConfigService (.env), DbConfig (HikariCP), AppConfig
│       │   ├── dashboard/        ← DashboardService, DashboardStatsDto
│       │   ├── mail/             ← EmailService (SMTP Gmail)
│       │   ├── outbox/           ← OutBoxService, TypeEvent (7 events)
│       │   ├── page/             ← Public pages, Admin pages, Profile, Wishlist
│       │   ├── sql/              ← JdbcHelper, RowMapper
│       │   ├── user/             ← UserController, UserService, UserRepository
│       │   └── common/           ← BaseResponse
│       │
│       └── bussiness/            ← Module nghiệp vụ (domain)
│           ├── ai/               ← AiChatController, AiChatService, AiShopContextService
│           ├── brand/            ← BrandService
│           ├── cart/             ← CartController, CartService, CartItemView
│           ├── contact/          ← ContactController, ContactService
│           ├── notification/     ← NotificationService, NotificationType
│           ├── order/            ← OrderController, OrderService, SepayWebhookController
│           ├── product/          ← ProductController, ProductService, ReviewService, VariantService
│           ├── voucher/          ← VoucherService, VoucherRepository
│           └── wishlist/         ← WishlistController, WishlistService
│
├── web/
│   ├── index.html
│   ├── migrate-order.jsp         ← Runtime schema migration
│   ├── assets/
│   │   ├── css/style.css         ← Custom stylesheet
│   │   └── js/
│   │       ├── app.js            ← Main client-side JS
│   │       ├── admin-dashboard.js← Admin dashboard interactions
│   │       └── ai-chat.js        ← AI chat widget
│   ├── layouts/
│   │   ├── header.jsp            ← Top menu, navigation
│   │   ├── footer.jsp            ← Footer, member info
│   │   ├── main-layout.jsp       ← Layout composition
│   │   ├── admin-layout.jsp      ← Admin sidebar layout
│   │   └── pagination.jsp        ← Pagination component
│   ├── pages/                    ← Public pages (17 files)
│   │   ├── home.jsp, cart.jsp, checkout.jsp, pay.jsp
│   │   ├── login.jsp, register.jsp, forgot-password.jsp
│   │   ├── product-detail.jsp, profile.jsp, wishlist.jsp
│   │   ├── order-detail.jsp, order-history.jsp
│   │   ├── about.jsp, contact.jsp
│   │   ├── verify-email.jsp, reset-password.jsp
│   │   └── error/404.jsp, error/500.jsp
│   ├── views/
│   │   ├── auth/                 ← login, register, forgot-password
│   │   └── admin/                ← dashboard, brands/*, products/*, users/*, vouchers/*, orders/*
│   └── WEB-INF/
│       └── glassfish-web.xml     ← Context root, JSP config
│
└── nbproject/                    ← NetBeans project configuration
```

**Thống kê:** 155 file Java, 43 file JSP, 14 bảng database, 3 chiến lược retry, 7 loại event.

---

## Cơ sở dữ liệu

### Tổng quan

| Thuộc tính | Giá trị |
|---|---|
| **Engine** | MySQL InnoDB |
| **Charset** | utf8mb4_unicode_ci |
| **Primary Key** | CHAR(36) — UUID format |
| **Connection** | HikariCP connection pool |
| **Bảng** | 14 |
| **Foreign Keys** | 20+ constraints |

### Sơ đồ quan hệ

```
User (1) ──┬── (N) Brand ── (N) Product ── (N) ProductVariant
           │                                     │
           ├── (N) OutBox                        │
           ├── (N) Session                       │
           ├── (N) Voucher                       │
           ├── (1) OrderCart ── (N) ItemCart ────┘
           ├── (N) Order ────────────────────────┘
           │        │
           │        └── (N) Payment
           │
           ├── (N) SavedProduct
           ├── (N) ProductReview ── (N) Product (CASCADE DELETE)
           └── (N) Contact
```

### Chi tiết bảng

#### User — Người dùng

| Column | Type | Mô tả |
|---|---|---|
| `id` | CHAR(36) | UUID primary key |
| `name` | VARCHAR(255) | Họ tên |
| `email` | VARCHAR(255) | Unique, dùng đăng nhập |
| `hashPassword` | VARCHAR(255) | PBKDF2WithHmacSHA256 (120k iterations) |
| `dateOfBirth` | DATE | Ngày sinh |
| `role` | ENUM('ADMIN','USER') | Phân quyền |
| `status` | ENUM('PENDING','ACTIVE','INACTIVE','BANNED') | Trạng thái tài khoản |
| `createdAt` | DATETIME | Tự động khi tạo |
| `updatedAt` | DATETIME | Auto-update |

**Index:** email (unique)

**Seed:** 1 admin (`admin@linhnamstore.local` / `Admin@123!`)

#### Brand — Thương hiệu

| Column | Type | Mô tả |
|---|---|---|
| `id` | CHAR(36) | UUID |
| `name` | VARCHAR(255) | Tên thương hiệu |
| `description` | VARCHAR(255) | Mô tả |
| `userId` | CHAR(36) | FK → User (người tạo) |
| `status` | ENUM('ACTIVE','INACTIVE') | Trạng thái |
| `createdAt` | DATETIME | |
| `updatedAt` | DATETIME | |

**Seed:** 3 brands — Kingston, Samsung, Cisco

#### Product — Sản phẩm

| Column | Type | Mô tả |
|---|---|---|
| `id` | CHAR(36) | UUID |
| `name` | VARCHAR(255) | Tên sản phẩm |
| `description` | VARCHAR(255) | Mô tả |
| `brandId` | CHAR(36) | FK → Brand |
| `userId` | CHAR(36) | FK → User (người tạo) |
| `category` | ENUM('STORAGE','NETWORK','ACCESSORY') | Danh mục |
| `status` | ENUM('DRAFT','ACTIVE','INACTIVE','ARCHIVED') | Trạng thái |
| `createdAt` | DATETIME | |
| `updatedAt` | DATETIME | |

**Seed:** 30+ sản phẩm

#### ProductVariant — Biến thể sản phẩm

| Column | Type | Mô tả |
|---|---|---|
| `id` | CHAR(36) | UUID |
| `productId` | CHAR(36) | FK → Product |
| `price` | DECIMAL(12,2) | Giá VND |
| `sku` | VARCHAR(255) | Unique stock keeping unit |
| `quantity` | INT | Tồn kho |
| `imageUrl` | VARCHAR(255) | URL hình ảnh |
| `status` | ENUM('ACTIVE','INACTIVE','OUT_OF_STOCK') | Trạng thái |
| `createdAt` | DATETIME | |
| `updatedAt` | DATETIME | |

**Seed:** ~65 variants (mỗi sản phẩm 2-3 variants)

#### ProductReview — Đánh giá sản phẩm

| Column | Type | Mô tả |
|---|---|---|
| `id` | CHAR(36) | UUID |
| `productId` | CHAR(36) | FK → Product (ON DELETE CASCADE) |
| `userId` | CHAR(36) | FK → User (ON DELETE CASCADE) |
| `rating` | INT | 1-5 (CHECK constraint) |
| `comment` | VARCHAR(1000) | Nội dung đánh giá |
| `status` | ENUM('PENDING','APPROVED','REJECTED') | Trạng thái duyệt |
| `createdAt` | DATETIME | |
| `updatedAt` | DATETIME | |

**Unique:** (userId, productId) — mỗi user chỉ review 1 lần/sản phẩm

#### Order — Đơn hàng

| Column | Type | Mô tả |
|---|---|---|
| `id` | CHAR(36) | UUID |
| `userId` | CHAR(36) | FK → User |
| `productId` | CHAR(36) | FK → Product |
| `variantId` | CHAR(36) | FK → ProductVariant (nullable) |
| `quantity` | INT | Số lượng |
| `phone` | VARCHAR(20) | SĐT người nhận |
| `address` | VARCHAR(500) | Địa chỉ giao hàng |
| `customerName` | VARCHAR(255) | Tên người nhận |
| `email` | VARCHAR(255) | Email người nhận |
| `note` | TEXT | Ghi chú |
| `status` | ENUM('PENDING','PAID','PROCESSING','SHIPPED','DELIVERED','CANCELLED') | Trạng thái |
| `paymentMethod` | VARCHAR(50) | Phương thức thanh toán |
| `voucherId` | VARCHAR(36) | FK → Voucher (nullable) |
| `totalAmount` | DECIMAL(12,2) | Tổng tiền |
| `createdAt` | DATETIME | |
| `updatedAt` | DATETIME | |

#### Payment — Thanh toán

| Column | Type | Mô tả |
|---|---|---|
| `id` | CHAR(36) | UUID |
| `orderId` | CHAR(36) | FK → Order |
| `userId` | CHAR(36) | FK → User |
| `amount` | DECIMAL(12,2) | Số tiền |
| `accessKey` | VARCHAR(255) | Access key từ payment gateway |
| `partnerCode` | VARCHAR(255) | Mã đối tác |
| `redirectUrl` | VARCHAR(255) | URL redirect sau thanh toán |
| `ipnUrl` | VARCHAR(255) | IPN webhook URL |
| `status` | VARCHAR(50) | Trạng thái thanh toán |
| `createdAt` | DATETIME | |
| `updatedAt` | DATETIME | |

#### OrderCart — Giỏ hàng (1 giỏ/user)

| Column | Type | Mô tả |
|---|---|---|
| `id` | CHAR(36) | UUID |
| `userId` | CHAR(36) | FK → User, UNIQUE |
| `createdAt` | DATETIME | |
| `updatedAt` | DATETIME | |

#### ItemCart — Items trong giỏ

| Column | Type | Mô tả |
|---|---|---|
| `id` | CHAR(36) | UUID |
| `cartId` | CHAR(36) | FK → OrderCart (ON DELETE CASCADE) |
| `productId` | CHAR(36) | FK → Product |
| `variantId` | CHAR(36) | FK → ProductVariant (nullable) |
| `quantity` | INT | Số lượng |
| `createdAt` | DATETIME | |
| `updatedAt` | DATETIME | |

**Unique:** (cartId, productId, variantId)

#### Voucher — Mã giảm giá

| Column | Type | Mô tả |
|---|---|---|
| `id` | CHAR(36) | UUID |
| `percent` | DECIMAL(5,2) | Phần trăm giảm |
| `userId` | CHAR(36) | FK → User |
| `quantity` | INT | Số lượng |
| `expTime` | DATE | Ngày hết hạn |
| `createdAt` | DATETIME | |

#### Session — Phiên đăng nhập

| Column | Type | Mô tả |
|---|---|---|
| `id` | CHAR(36) | UUID |
| `hashRefreshToken` | VARCHAR(255) | SHA-256 hash của refresh token |
| `userId` | CHAR(36) | FK → User |
| `ip` | VARCHAR(45) | IP client (IPv4/IPv6) |
| `createdAt` | DATETIME | |
| `updatedAt` | DATETIME | |

#### OutBox — Event queue

| Column | Type | Mô tả |
|---|---|---|
| `id` | CHAR(36) | UUID |
| `code` | VARCHAR(255) | Mã xác thực / reset code |
| `type` | ENUM(7 loại) | Loại event |
| `userId` | CHAR(36) | FK → User |
| `status` | ENUM('PENDING','PROCESSED','FAILED') | Trạng thái |
| `expiresAt` | DATETIME | Thời gian hết hạn |
| `usedAt` | DATETIME | Thời gian sử dụng |
| `createdAt` | DATETIME | |
| `updatedAt` | DATETIME | |

**Index:** (userId, type, status, createdAt), (userId, type, code, usedAt, expiresAt)

#### SavedProduct — Sản phẩm yêu thích

| Column | Type | Mô tả |
|---|---|---|
| `id` | CHAR(36) | UUID |
| `userId` | CHAR(36) | FK → User |
| `productId` | CHAR(36) | FK → Product |
| `quantity` | INT | |
| `createdAt` | DATETIME | |

**Unique:** (userId, productId)

#### Contact — Liên hệ

| Column | Type | Mô tả |
|---|---|---|
| `id` | CHAR(36) | UUID |
| `name` | VARCHAR(255) | Tên người gửi |
| `email` | VARCHAR(255) | Email người gửi |
| `content` | TEXT | Nội dung liên hệ |
| `createdAt` | DATETIME | |

---

## Module chi tiết

### Servlet mappings

| Controller | URL | Auth | Vai trò |
|---|---|---|---|
| `AuthController` | `/auth` | @Public | Đăng nhập, đăng ký, quên mật khẩu, xác thực email |
| `ProductController` | `/home`, `/product`, `/products`, `/admin/products` | @Public | Trang chủ, chi tiết, tìm kiếm, lọc, admin CRUD |
| `CartController` | `/cart` | Auth | Giỏ hàng: thêm, sửa, xóa, clear |
| `OrderController` | `/checkout`, `/order/detail`, `/orders` | Auth | Checkout, chi tiết đơn, lịch sử |
| `SepayWebhookController` | `/api/sepay/webhook` | @Public | Nhận webhook thanh toán Sepay |
| `AdminDashboardController` | `/admin/dashboard`, `/admin/dashboard/api/stats` | ADMIN | Dashboard thống kê |
| `AdminOrderController` | `/admin/orders` | ADMIN | Quản lý đơn hàng admin |
| `UserController` | `/admin/users` | ADMIN | CRUD người dùng |
| `ProfileController` | `/profile` | Auth | Quản lý profile |
| `PublicPageController` | `/about` | @Public | Trang giới thiệu |
| `BrandController` | `/admin/brands` | ADMIN | CRUD thương hiệu |
| `ContactController` | `/contact` | @Public | Form liên hệ |
| `VoucherController` | `/admin/vouchers` | ADMIN | CRUD voucher |
| `WishlistController` | `/wishlist` | Auth | Wishlist |
| `AiChatController` | `/ai-chat` | @Public | AI chat tư vấn |

### Module lõi (core)

#### Config — Cấu hình (`module.core.config`)

| Class | Vai trò |
|---|---|
| `ConfigService` | Singleton đọc `.env`, env vars override. Methods: `get()`, `getInt()`, `require()` |
| `DbConfig` | HikariCP connection pool, auto schema migration (thêm column thiếu) |
| `AppConfig` | Constants: PBKDF2 iterations, page size, Sepay config, currency |

#### SQL — Database access (`module.core.sql`)

| Class | Vai trò |
|---|---|
| `JdbcHelper` | Static JDBC wrapper: `executeQuery(sql, RowMapper, params)`, `executeUpdate(sql, params)`, `executeBatch()`, `count()`, `executeRaw()`. Auto-bind LocalDate/LocalDateTime |
| `RowMapper<T>` | Functional interface: `T map(ResultSet rs)` |

#### Auth — Xác thực (`module.core.auth`)

| Class | Vai trò |
|---|---|
| `AuthController` | Xử lý: login, register, verify-email, forgot-password, reset-password, logout |
| `AuthService` | Đăng ký (tạo user + cart + outbox event), đăng nhập (verify + session), xác thực email, reset mật khẩu |
| `PasswordService` | PBKDF2WithHmacSHA256, 120k iterations, 256-bit salt, timing-safe comparison |
| `AuthTokenService` | Hash refresh token SHA-256 |
| `AuthRepository` | JDBC: findByEmail, findById, saveSession, deleteSession, updatePassword, createCartForUser |

#### User — Quản lý người dùng (`module.core.user`)

| Class | Vai trò |
|---|---|
| `UserController` | CRUD qua `action` param: create, edit, delete, changeStatus, list (pagination) |
| `UserService` | createUser, getUserById, updateUser, listUsers (pagination), deleteUser, changeStatus |
| `UserRepository` | JDBC: insert, findById, findByEmail, findAll (paginated), countAll, update, updateStatus, delete |

#### OutBox & Mail — Event-driven email (`module.core.outbox`, `module.core.mail`)

| Class | Vai trò |
|---|---|
| `OutBoxService` | `publishEvent()` → lưu DB, `processPending()` → gửi email qua EmailService |
| `EmailService` | SMTP Gmail (Jakarta Mail), HTML template cho verification code & password reset |
| `TypeEvent` | Enum 7 loại event |
| `OutBoxRepository` | JDBC: insert, findPending, findValidCode, markProcessed, markFailed, markUsed |

#### Admin — Thống kê (`module.core.admin`)

| Class | Vai trò |
|---|---|
| `AdminAnalyticsService` | Tổng hợp doanh thu, đơn hàng, người dùng, sản phẩm theo khoảng thời gian |
| `AdminOrderController` | Quản lý đơn hàng: list, detail, update-status, cancel |
| `AdminAnalyticsRepository` | Query thống kê: revenue, order count, top products, user growth |

#### Dashboard (`module.core.dashboard`)

| Class | Vai trò |
|---|---|
| `DashboardService` | Tổng quan: tổng doanh thu, đơn hôm nay, sản phẩm mới, user mới |
| `DashboardStatsDto` | DTO chứa các chỉ số dashboard |

### Module nghiệp vụ (bussiness)

#### Product — Sản phẩm (`module.bussiness.product`)

| Class | Vai trò |
|---|---|
| `ProductController` | Home page, search, detail, autocomplete, category, CRUD, variant management |
| `ProductService` | getHomePage, listProducts, getProductDetail, searchProducts, autocomplete, filterProducts (category, brand, price range, status, sort), CRUD |
| `ReviewService` | Tạo, duyệt, lấy danh sách đánh giá sản phẩm |
| `VariantService` | createVariant, updateVariant, deleteVariant |
| `FilterResult` | Wrapper kết quả lọc sản phẩm |
| `ProductRepository` | JDBC: insert, findById, findActive (paginated), search, findByCategory, findBestSelling, countActive, update, delete |
| `VariantRepository` | JDBC: insert, findById, findBySku, findByProductId, update, delete |
| `BrandRepository` | JDBC: findAll |
| `ProductReviewRepository` | JDBC: insert, findByProductId, findByUserId, updateStatus, count |

#### Cart — Giỏ hàng (`module.bussiness.cart`)

| Class | Vai trò |
|---|---|
| `CartController` | Actions: add, update, remove, clear |
| `CartService` | getCart (paginated), addToCart (kiểm tra tồn kho), updateQuantity, removeItem, clearCart |
| `CartItemView` | ViewModel: productId, productName, sku, quantity, stockQuantity, price, getLineTotal() |
| `CartRepository` | JDBC: getOrCreateCart, getItemsByCartId (paginated), findItem, addItem, updateQuantity, removeItem, clearCart, countItems |

#### Order — Đơn hàng (`module.bussiness.order`)

| Class | Vai trò |
|---|---|
| `OrderController` | Checkout, detail, history, admin list/detail, update-status, cancel |
| `OrderService` | checkout (tạo order từ cart), getOrderDetail, getOrderHistory, listAllOrders, updateStatus, cancelOrder |
| `SepayWebhookController` | Nhận webhook Sepay, match order qua mã "DH" trong nội dung CK, cập nhật PENDING → PAID |
| `SepayWebhookPayload` | DTO nhận webhook (hỗ trợ camelCase và snake_case JSON) |
| `OrderRepository` | JDBC: insert, findById, findByUserId (paginated), findAll (paginated), countAll, updateStatus |

#### AI Chat — Trợ lý AI (`module.bussiness.ai`)

| Class | Vai trò |
|---|---|
| `AiChatController` | Endpoint `/ai-chat`, nhận JSON request, trả về stream response |
| `AiChatService` | Gọi OpenAI-compatible API, quản lý conversation history |
| `AiShopContextService` | Xây dựng context sản phẩm, danh mục, brand cho AI prompt |
| `ChatRequestDto` | Request DTO: messages, userId |
| `ChatResponseDto` | Response DTO: reply, conversationId |
| `ChatStreamChunk` | Stream response chunk |

#### Voucher — Mã giảm giá (`module.bussiness.voucher`)

| Class | Vai trò |
|---|---|
| `VoucherService` | CRUD voucher, kiểm tra hạn dùng, số lượng |
| `VoucherRepository` | JDBC: insert, findById, findByUserId, findAll, update, delete, count |

#### Wishlist — Sản phẩm yêu thích (`module.bussiness.wishlist`)

| Class | Vai trò |
|---|---|
| `WishlistController` | Actions: add, remove, list |
| `WishlistService` | addToWishlist, removeFromWishlist, getWishlist |
| `WishlistItemView` | ViewModel: productId, productName, imageUrl, price |
| `WishlistRepository` | JDBC: insert, delete, findByUserId, exists |

#### Contact — Liên hệ (`module.bussiness.contact`)

| Class | Vai trò |
|---|---|
| `ContactController` | Nhận form liên hệ, validate, lưu DB |
| `ContactService` | submitContact, getRecentContacts |
| `ContactRepository` | JDBC: insert, findAll, count |

#### Notification — Thông báo (`module.bussiness.notification`)

| Class | Vai trò |
|---|---|
| `NotificationService` | Gửi thông báo trong hệ thống |
| `NotificationType` | Enum các loại thông báo |

---

## Luồng nghiệp vụ

### 1. Đăng ký tài khoản

```
POST /auth?action=register
    │
    ▼
AuthController.handleRegister()
    │
    ▼
AuthService.register(RegisterRequestDto)
    ├── Validate: email không trùng, mật khẩu khớp, đúng format
    ├── Hash password (PBKDF2WithHmacSHA256, 120k iterations)
    ├── UserRepository.insert(user)
    ├── AuthRepository.createCartForUser(userId)  ← Tự tạo giỏ hàng
    └── OutBoxService.publishEvent(USER_REGISTERED)
            │
            ▼
        OutBoxRepository.insert(event)
        EmailService.send(verification code)
            │
            ▼
        Forward register.jsp với thông báo
```

### 2. Đăng nhập

```
POST /auth?action=login
    │
    ▼
AuthController.handleLogin()
    │
    ▼
AuthService.login(LoginRequestDto)
    ├── AuthRepository.findByEmail(email)
    ├── PasswordService.verify(password, hashPassword)
    ├── Tạo refresh token → SHA-256 hash → lưu Session table
    ├── Set session: currentUser (UserPayload), sessionId, csrfToken
    └── Redirect → /home
```

### 3. Đặt hàng & thanh toán

```
POST /checkout
    │
    ▼
OrderController.handleCheckout()
    │
    ▼
OrderService.checkout(userId, CheckoutDto)
    ├── CartService.getCart(userId)  ← Lấy items từ giỏ
    ├── Validate: tồn kho đủ cho mỗi item
    ├── OrderRepository.insert(order) cho mỗi item
    ├── CartService.clearCart(userId)
    ├── OutBoxService.publishEvent(ORDER_CREATED)
    └── Redirect → /checkout?action=pay  ← Sepay QR page

POST /api/sepay/webhook  (từ Sepay.vn)
    │
    ▼
SepayWebhookController.doPost()
    ├── Parse SepayWebhookPayload từ JSON
    ├── Extract mã "DH" từ nội dung chuyển khoản
    ├── Tìm order có mã phù hợp, status = PENDING
    ├── OrderRepository.updateStatus(orderId, "PAID")
    └── OutBoxService.publishEvent(PAYMENT_COMPLETED)
```

### 4. Thêm vào giỏ hàng

```
POST /cart?action=add
    │
    ▼
CartController.handleAdd()
    │
    ▼
CartService.addToCart(AddToCartDto)
    ├── CartRepository.getOrCreateCart(userId)
    ├── Kiểm tra tồn kho (VariantRepository.findById)
    ├── CartRepository.findItem(cartId, productId, variantId)
    ├── Nếu tồn tại: update quantity
    ├── Nếu mới: CartRepository.addItem()
    └── Update session: cartCount
```

### 5. Quên mật khẩu

```
POST /auth?action=forgot-password
    │
    ▼
AuthController.handleForgotPassword()
    ├── AuthRepository.findByEmail(email)
    ├── Tạo random code (6 ký tự)
    ├── OutBoxService.publishEvent(PASSWORD_RESET_REQUESTED)
    │       → EmailService.send(reset code + link)
    └── Forward forgot-password.jsp với thông báo

POST /auth?action=reset-password
    ├── Validate reset code, expiry
    ├── PasswordService.hash(newPassword)
    ├── AuthRepository.updatePassword(userId, newHash)
    └── Redirect → /auth?action=login
```

---

## Bảo mật

### Authentication

| Cơ chế | Mô tả |
|---|---|
| **Session-based** | `HttpSession` attribute `currentUser` = `UserPayload` |
| **Password hash** | PBKDF2WithHmacSHA256, 120,000 iterations, 32-byte salt |
| **Refresh token** | SHA-256 hash, lưu Session table, kèm IP address |
| **AuthGuard** | Filter interceptor: check `@Public`, `@RequiresRole`, session validation |
| **CSRF** | Auto-generate/validate token cho unsafe methods, miễn `/api/*` |

### Authorization

| Annotation | Vai trò |
|---|---|
| `@Public` | Class bypass auth hoàn toàn |
| `@RequiresRole("ADMIN")` | Class yêu cầu role ADMIN (case-insensitive) |

### Password format

```
pbkdf2:iterations:salt:hash
```

Ví dụ: `pbkdf2:120000:a1b2c3...:d4e5f6...`

### Input validation

- Form validation phía client (JavaScript) + server-side (manual check)
- SQL injection prevention: prepared statements qua `JdbcHelper`
- XSS prevention: JSTL `<c:out>` auto-escape

---

## Tích hợp thanh toán

### Sepay.vn Gateway

| Thuộc tính | Giá trị |
|---|---|
| **Type** | Bank transfer / QR code |
| **Bank** | MSB (Maritime Bank) |
| **Account** | 80003058262 |
| **Account Name** | NGUYEN THAI AN |
| **Webhook endpoint** | `POST /api/sepay/webhook` |
| **Matching logic** | Parse nội dung CK, tìm mã prefix "DH", match với order PENDING |
| **Payload** | Hỗ trợ camelCase và snake_case JSON keys |

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
    │
    ▼
EmailService.send(payment confirmation)
```

---

## AI Chat

### Tổng quan

Trợ lý AI tư vấn sản phẩm tích hợp qua OpenAI-compatible API. Hệ thống xây dựng ngữ cảnh cửa hàng (sản phẩm, danh mục, brand) để AI trả lời chính xác.

### Components

| Component | Vai trò |
|---|---|
| `AiChatController` | Endpoint `/ai-chat`, nhận JSON, trả về SSE stream |
| `AiChatService` | Quản lý conversation, gọi AI API, xử lý stream response |
| `AiShopContextService` | Xây dựng system prompt với thông tin sản phẩm, danh mục, brand |
| `ai-chat.js` | Client-side chat widget, SSE streaming |

### Flow

```
User gửi câu hỏi → AiChatController
    │
    ▼
AiShopContextService.buildContext()
    ├── Lấy danh mục, brand, sản phẩm nổi bật
    └── Xây dựng system prompt
    │
    ▼
AiChatService.chat(messages, context)
    ├── Gọi OpenAI-compatible API (streaming)
    ├── Lưu conversation history
    └── Trả về stream response
    │
    ▼
Client nhận SSE chunks → hiển thị từng phần
```

---

## CI/CD & Docker

### GitHub Actions

#### CI (`.github/workflows/ci.yml`)

| Thuộc tính | Giá trị |
|---|---|
| **Trigger** | Push/PR to `main`, `master`, `dev` |
| **JDK** | Temurin 17 |
| **Job** | Download Jakarta EE 9.1 API jar → `javac` compile toàn bộ `src/java/**/*.java` |
| **Test** | Chưa có (thư mục `test/` rỗng) |

#### CD (`.github/workflows/deploy.yml`)

| Thuộc tính | Giá trị |
|---|---|
| **Trigger** | Push to `main` |
| **Build** | Ant `clean dist` → tạo WAR |
| **Deploy** | SCP WAR + Dockerfile + docker-compose.yml + sto.sql → VPS |
| **Run** | `docker compose up -d` trên VPS |

### Docker

#### Dockerfile

```dockerfile
FROM payara/server-full:6.2023.1-jdk17
COPY dist/WebApplication3.war ${DEPLOY_DIR}/ROOT.war
```

- Payara Server 6 (tương thích GlassFish), JDK 17
- Deploy WAR với context root `/`

#### docker-compose.yml

| Service | Image | Ports | Volume |
|---|---|---|---|
| `mysql` | mysql:8.0 | 3306 (localhost only) | `mysql_data:/var/lib/mysql`, `./sto.sql:/docker-entrypoint-initdb.d/init.sql` |
| `app` | Custom (Dockerfile) | 8080 (web), 4848 (admin) | — |

---

## Cài đặt & vận hành

### Yêu cầu

| Thành phần | Phiên bản |
|---|---|
| JDK | 11+ (khuyến nghị 17) |
| GlassFish | 6.1.0 |
| MySQL | 8.x |
| Apache NetBeans | (khuyến nghị) |
| Docker | (tùy chọn, cho deployment) |

### Cách 1: Local development (NetBeans + GlassFish)

**Bước 1: Tạo database**

```sql
CREATE DATABASE ecommerce CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ecommerce;
SOURCE sto.sql;
```

**Bước 2: Cấu hình `.env`**

```env
DB_HOST=localhost
DB_PORT=3306
DB_NAME=ecommerce
DB_USER=root
DB_PASSWORD=your_password

SMTP_USER=your_email@gmail.com
SMTP_PASS=your_app_password

APP_BASE_URL=http://localhost:8080/WebApplication3

# AI Chat (tùy chọn)
AI_API_KEY=your_openai_api_key
AI_API_URL=https://api.openai.com/v1/chat/completions
AI_MODEL=gpt-3.5-turbo
```

**Bước 3: Thêm MySQL Connector**

Copy `mysql-connector-java-8.x.x.jar` vào `web/WEB-INF/lib/`

**Bước 4: Build & Deploy**

```bash
# NetBeans
Right-click project → Deploy

# Command line
ant build      # Compile & tạo WAR
ant run        # Deploy lên GlassFish
ant clean      # Xóa build artifacts
```

**Bước 5: Truy cập**

| URL | Mô tả |
|---|---|
| `http://localhost:8080/WebApplication3/home` | Trang chủ |
| `http://localhost:8080/WebApplication3/admin/dashboard` | Admin dashboard |
| `http://localhost:8080/WebApplication3/contact` | Liên hệ |

### Cách 2: Docker

```bash
# Build & chạy
docker compose up -d

# Xem log
docker compose logs -f app

# Dừng
docker compose down
```

### Tài khoản mặc định

| Role | Email | Mật khẩu |
|---|---|---|
| ADMIN | admin@linhnamstore.local | Admin@123! |

### Migration

```
web/migrate-order.jsp  ← Runtime schema migration (thêm column thiếu cho Order table)
```

---

## Thông tin nhóm

> Điền thông tin thành viên

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
- **HikariCP connection pool** thay vì connection thuần: mỗi query tạo connection mới từ pool, không phải mở/đóng TCP connection. Tăng performance đáng kể so với raw JDBC.
`─────────────────────────────────────────────────`
