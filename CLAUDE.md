# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Dự án: LinhNamStore — Jakarta EE E-Commerce

Website thương mại điện tử bán thiết bị lưu trữ, mạng & phụ kiện. Jakarta EE 9.1 Servlet thuần, không Spring/DI. Build bằng Apache Ant qua NetBeans. Runtime: GlassFish 6.1.0 / Payara 6.2023.1-jdk17.

## Commands quan trọng

### Build & Run
```bash
ant build      # Compile, tạo WAR → dist/WebApplication3.war
ant clean      # Xóa build artifacts
ant run        # Deploy lên GlassFish (nếu server config sẵn)
```

### CI Build (standalone, không cần NetBeans)
```bash
# Download Jakarta EE API + compile
mkdir -p /tmp/jars && cd /tmp/jars
curl -sL -o jakartaee-api.jar "https://repo1.maven.org/maven2/jakarta/platform/jakarta.jakartaee-api/9.1.0/jakarta.jakartaee-api-9.1.0.jar"
cd /path/to/project
find src/java -name '*.java' > /tmp/sources.txt
javac -cp "/tmp/jars/jakartaee-api.jar:web/WEB-INF/lib/mysql-connector-j-8.4.0.jar" -d /tmp/classes -source 11 -target 11 -encoding UTF-8 @/tmp/sources.txt
```

### Docker
```bash
docker compose up -d    # MySQL 8 + Payara Server (app)
docker compose down     # Stop containers
```

### Database
```bash
mysql -u root -p < sto.sql   # Import schema + seed data (30+ sản phẩm)
```

## Cấu trúc thư mục

```
src/java/
├── common/                    ← Hạ tầng chung
│   ├── annotation/            ← @Public, @RequiresRole, @Route, @User
│   ├── controller/            ← BaseController (reflection-based routing)
│   ├── exceptionFilter/       ← GlobalExceptionFilter
│   ├── guard/                 ← AuthGuard (xác thực & phân quyền)
│   ├── logger/                ← AppLogger
│   ├── retry/                 ← Retry framework (FIXED, EXPONENTIAL, FIBONACCI)
│   └── type/                  ← Result<T>, UserPayload
├── entity/                    ← 13 POJO entities (không JPA annotation)
└── module/
    ├── core/                  ← Module lõi: auth, user, config, sql, mail, outbox, page
    └── bussiness/             ← Module nghiệp vụ: product, cart, order, brand, contact, voucher, wishlist, ai
web/
├── assets/css/style.css       ← Custom CSS (không framework)
├── assets/js/app.js           ← Custom JS
├── layouts/                   ← header.jsp, footer.jsp, main-layout.jsp
├── pages/                     ← home, cart, checkout, profile, wishlist, ...
├── views/                     ← auth/, admin/, order/ JSP templates
└── WEB-INF/lib/               ← mysql-connector-j-8.4.0.jar
```

## Kiến trúc cốt lõi

### Reflection-based Routing (BaseController)
Mọi request qua 1 `@WebServlet`. `BaseController` quét annotation `@Route(path, method)` trên controller method → dispatch tự động. Không cần `web.xml` mapping.

```java
@Route(path = "detail", method = "GET")
public void handleDetail(HttpServletRequest req, HttpServletResponse resp) { ... }
```

### Auth Flow
- `@Public` annotation → bỏ qua AuthGuard
- `@RequiresRole("ADMIN")` → check role từ session
- Session lưu `currentUser` (UserPayload)
- CSRF token auto-generated/validated trong BaseController cho POST requests
- Password: PBKDF2WithHmacSHA256, 120000 iterations, 256-bit salt
- Refresh token: SHA-256 hash → lưu Session table

### OutBox Pattern
Thay vì gửi email đồng bộ → lưu event vào OutBox table → xử lý bất đồng bộ với retry. 7 loại event: `USER_REGISTERED`, `USER_CREATED`, `PASSWORD_RESET_REQUESTED`, `ORDER_CREATED`, `PAYMENT_COMPLETED`, `USER_BANNED`, `ORDER_STATUS_UPDATED`.

### SQL Layer
Raw JDBC qua `JdbcHelper` + `RowMapper<T>`. Không ORM, không connection pool (dùng `DriverManager` trực tiếp).

## Quy ước tạo module mới

### Cấu trúc
```
module/{path}/{tên_module}/
├── {X}Config.java          ← constants, setup
├── {X}Controller.java      ← extends HttpServlet, @WebServlet
├── {X}Service.java         ← business logic
├── dto/                    ← request DTOs (input)
├── response_dto/           ← response DTOs (output)
└── repository/
    ├── interfaces/         ← I{Entity}Repository.java
    └── impl/               ← {Entity}Repository.java
```

### Quy ước
- **Không dùng Spring/DI** — dependency khởi tạo bằng `new` trực tiếp
- **Single servlet pattern** — action param phân luồng
- **Controller**: `doGet` → forward JSP, `doPost` → switch action → handler → service → redirect/forward
- **Service**: nhận DTO, trả về ResponseDto có `isSuccess()`, `getErrorMessage()`, `getSuccessMessage()`
- **DTO**: `dto/` cho input, `response_dto/` cho output, không dùng chung
- **Repository**: service import `impl` trực tiếp, KHÔNG dùng interface trong code
- **Package**: `module.core.{ten}` hoặc `module.bussiness.{ten}` (lưu ý typo "bussiness")
- **Response pattern**: `BaseResponse` hoặc self-contained success/error fields

## UI/UX Layout

Mọi page có 4 phần cố định: Banner, Top Menu, Left Menu, Content, Footer.
Trang chủ: hiển thị sản phẩm theo mục (mới, bán chạy, giảm giá).
Trang chi tiết: click sản phẩm → xem đầy đủ thông tin.
Tối thiểu 30 sản phẩm.

## Database

MySQL 8, InnoDB, utf8mb4_unicode_ci. 13 bảng, PK CHAR(36) UUID format. Schema + seed data trong `sto.sql`.

Admin mặc định: `admin@linhnamstore.local` / `Admin@123!`

## Testing

Chưa có test framework. Không thư mục test.

## CI/CD

GitHub Actions: `ci.yml` (syntax check), `deploy.yml` (build WAR → SCP → VPS → Docker deploy).
