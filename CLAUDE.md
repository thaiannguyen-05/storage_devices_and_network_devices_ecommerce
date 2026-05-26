# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

---

## LinhNamStore — Jakarta EE E-Commerce

Website bán thiết bị lưu trữ, mạng & phụ kiện. Jakarta EE 9.1 Servlet, không Spring/DI. Ant build, MySQL 8, GlassFish 6.1.

## Commands

```bash
ant build      # Compile & tạo WAR → dist/WebApplication3.war
ant run        # Deploy lên GlassFish
ant clean      # Xóa build artifacts
docker compose up -d   # MySQL + App (Payara Server)
```

CI: `.github/workflows/ci.yml` — compile check JDK 17. CD: `.github/workflows/deploy.yml` — build WAR, SCP lên VPS, docker compose deploy. **Không có test** — thư mục `test/` rỗng.

## Kiến trúc tổng quan

```
HTTP Request → GlobalExceptionFilter → AuthGuard → BaseController(@Route dispatch) → Controller → Service → Repository → JdbcHelper → MySQL
```

### Request pipeline
1. **GlobalExceptionFilter** (`@WebFilter("/*")`) — bắt mọi exception
2. **AuthGuard** — kiểm tra session `currentUser`, bỏ qua nếu controller có `@Public`, check role nếu `@RequiresRole("ADMIN")`
3. **BaseController** — reflection-based routing: quét `@Route(method, path)` annotation trên method → auto-dispatch. Hoặc controller override `doGet`/`doPost` trực tiếp, switch theo `action` param
4. **CSRF** — BaseController auto-generate/validate CSRF token cho POST/PUT/PATCH/DELETE. Form field: `csrfToken` hoặc header: `X-CSRF-Token`. API paths (`/api/*`) được miễn

### Session attributes
- `currentUser` — `UserPayload` (userId, role, name, email)
- `sessionId` — session identifier
- `csrfToken` — CSRF token
- `cartCount` — số lượng item trong giỏ

## Cấu trúc module

```
src/java/
├── common/                    # Hạ tầng: annotation, BaseController, AuthGuard, GlobalExceptionFilter, retry, Result<T>
├── entity/                    # 13 POJO entities (UUID CHAR(36) PKs)
└── module/
    ├── core/                  # Module lõi: config, sql, auth, user, outbox, mail, admin, page
    └── bussiness/             # Module nghiệp vụ: product, cart, order, ai, brand, contact, voucher, wishlist
```

**Lưu ý:** Package tên `bussiness` (thiếu 'i') — giữ nguyên để tương thích.

## Module generation pattern

```
module/{path}/{tên_module}/
├── {X}Config.java          ← cấu hình (constants, setup)
├── {X}Controller.java      ← extends BaseController hoặc HttpServlet, @WebServlet
├── {X}Service.java         ← business logic
├── dto/                    ← request DTOs (input)
│   └── {Action}RequestDto.java
├── response_dto/           ← response DTOs (output)
│   └── {Action}ResponseDto.java
└── repository/
    ├── interfaces/I{Entity}Repository.java
    └── impl/{Entity}Repository.java
```

### Controller
- `extends BaseController` hoặc `HttpServlet`, `@WebServlet(name, urlPatterns)`
- `@Public` nếu không cần auth, `@RequiresRole("ADMIN")` nếu cần admin
- Handler: nhận `request.getParameter()` → tạo DTO → gọi service → check `result.isSuccess()`:
  - Fail: `setAttribute("error", ...)` → forward JSP
  - Success: set session/cookie → `sendRedirect`
- Instantiates service trực tiếp: `private final XService xService = new XService();`

### Service
- Plain Java class, KHÔNG extends/implement gì
- Dependencies khởi tạo bằng `new` trực tiếp: `new XRepository()`, `new OutBoxService()`
- Method nhận DTO, trả về ResponseDto (có `isSuccess()`, `getErrorMessage()`, `getSuccessMessage()`)
- Gửi event qua `OutBoxService.publishEvent(TypeEvent.XXX)` → email bất đồng bộ
- Password hash: PBKDF2WithHmacSHA256, 120000 iterations, 256-bit salt

### Repository
- Interface + Impl pair, nhưng Service import thẳng `impl`, KHÔNG dùng interface trong code
- Dùng `JdbcHelper` static methods: `executeQuery(sql, RowMapper, params)`, `executeUpdate(sql, params)`, `executeBatch()`, `count()`
- Row mapping: lambda `rs -> new Entity(...)`
- Pagination: `LIMIT ? OFFSET ?`

### DTO
- Request: `module.{path}.dto.{Action}RequestDto` — private fields, getter/setter, KHÔNG validation annotation
- Response: `module.{path}.response_dto.{Action}ResponseDto` — extends `BaseResponse` hoặc có `boolean success`, `String errorMessage/successMessage`

### Imports
- Package: `module.core.{name}` hoặc `module.bussiness.{name}`
- Entity: `entity.{Name}`

## Key infrastructure classes

| Class | Path | Vai trò |
|---|---|---|
| `BaseController` | `common.controller` | Abstract HttpServlet, reflection routing qua `@Route`, CSRF validation, session helpers (`getCurrentUserId`) |
| `AuthGuard` | `common.guard` | Filter interceptor, check `@Public`, `@RequiresRole`, session validation |
| `GlobalExceptionFilter` | `common.exceptionFilter` | `@WebFilter("/*")`, catch all exceptions |
| `JdbcHelper` | `module.core.sql` | Static JDBC wrapper: query, update, batch, count, raw execute. Auto-bind LocalDate/LocalDateTime |
| `RowMapper<T>` | `module.core.sql` | Functional interface: `T map(ResultSet rs)` |
| `ConfigService` | `module.core.config` | Singleton đọc `.env` file, env vars override. `get()`, `getInt()`, `require()` |
| `DbConfig` | `module.core.config` | JDBC connection factory từ `.env`, auto schema migration (thêm column thiếu cho Order table) |
| `AppConfig` | `module.core.config` | Static constants: JWT secret, expiry, PBKDF2 iterations, page size, Sepay config |
| `OutBoxService` | `module.core.outbox` | Event-driven email: `publishEvent()` → lưu DB → `processPending()` → gửi email |
| `EmailService` | `module.core.mail` | SMTP Gmail sender, HTML templates |
| `PasswordService` | `module.core.auth` | PBKDF2WithHmacSHA256 hash/verify |
| `Result<T>` | `common.type` | Generic success/failure wrapper |
| `UserPayload` | `common.type` | Session user data carrier |

## Database

- **MySQL 8**, InnoDB, utf8mb4_unicode_ci
- **13 bảng**, UUID CHAR(36) PKs, foreign key constraints
- Schema + seed data: `sto.sql` (1 admin, 5 brands, 30+ products, ~65 variants)
- **Không connection pooling** — mỗi query tạo connection mới qua `DbConfig.getConnection()`
- `.env` config: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `DB_PARAMS`

### Entity relationships
```
User (1) ──┬── (N) Brand ── (N) Product ── (N) ProductVariant
           ├── (N) OutBox, (N) Session, (N) Voucher
           ├── (1) OrderCart ── (N) ItemCart ─┘
           ├── (N) Order ────────────────────┘ → (N) Payment
           └── (N) SavedProduct
```

## JSP Views

- **53 JSP files**, JSTL (`<c:forEach>`, `<c:if>`, `<c:choose>`)
- Layout composition: `<jsp:include page="../layouts/header.jsp" />` ... content ... `<jsp:include page="../layouts/footer.jsp" />`
- **Layouts:** `web/layouts/header.jsp` (top menu, left sidebar), `footer.jsp`, `admin-layout.jsp`
- **Public pages:** `web/pages/` — home, login, register, cart, checkout, profile, wishlist, product-detail, contact, about
- **Admin views:** `web/views/admin/` — dashboard, brands/*, products/*, users/*, vouchers/*, orders/*
- **CSS/JS:** `web/assets/css/style.css` (custom, no framework), `web/assets/js/app.js`
- UTF-8 encoding throughout

## Authentication & Authorization

- Session-based, `HttpSession` attribute `currentUser` = `UserPayload`
- Login: email + password → PBKDF2 verify → refresh token SHA-256 hash → lưu `Session` table
- `@Public` trên class → skip auth hoàn toàn
- `@RequiresRole("ADMIN")` trên class → enforce admin role
- Một số controller (OrderController, ProfileController) check session thủ công bằng `getCurrentUserId(req)`
- Password format: `pbkdf2:iterations:salt:hash`

## Docker

- **Dockerfile:** `payara/server-full:6.2023.1-jdk17`, deploy WAR as ROOT.war (context "/")
- **docker-compose.yml:** MySQL 8 (port 3306 localhost only) + App service, volume `mysql_data`

## UI/UX Layout Requirements

Mọi page có 4 phần: **Banner** → **Top Menu** → **Left Menu** → **Content** → **Footer** (tên + ngày sinh thành viên). Top/Left menu có hover/transition.

### Trang chủ
Hiển thị sản phẩm: hình, tên/mã, giá. Chia mục: hàng mới, bán chạy, giảm giá.

### Trang chi tiết sản phẩm
Click từ trang chủ → hiển thị đầy đủ thông tin + variants.

### Trang liên hệ
Form: họ tên, email, nội dung. Validate: không trống, email đúng format. Lưu DB.

### Trang đăng ký
Tối thiểu: tên tài khoản, username, email, SĐT, địa chỉ, mật khẩu, xác nhận. Validate: không trống, email format, mật khẩu khớp, SĐT format. Lưu DB.

### Dữ liệu sản phẩm
Tối thiểu 30 sản phẩm (đã có trong `sto.sql`).
