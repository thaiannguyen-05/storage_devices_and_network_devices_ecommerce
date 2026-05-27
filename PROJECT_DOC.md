# LINH NAM STORE — Tài Liệu Dự Án & Hướng Dẫn Setup

## 1. Tổng quan

| Thuộc tính | Giá trị |
|---|---|
| **Tên dự án** | LinhNamStore |
| **Loại** | Jakarta EE 9.1 Web Application (Servlet 5.0, JSP 3.0) |
| **Mục đích** | E-commerce bán thiết bị lưu trữ, mạng, phụ kiện |
| **Build tool** | Apache Ant (qua NetBeans) |
| **Runtime** | GlassFish 6.1.0 / Payara 6.2023.1-jdk17 |
| **Database** | MySQL 8.0 |
| **Ngôn ngữ** | Java 11 (source), biên dịch JDK 17 |
| **Frontend** | JSP + Custom CSS/JS, không framework |

## 2. Kiến trúc

```
HTTP Request → GlobalExceptionFilter → AuthGuard → BaseController → Controller → Service → Repository → JdbcHelper → MySQL
```

- **Routing:** Reflection-based qua annotation `@Route` — mọi action vào 1 `@WebServlet`
- **Auth:** Session-based (Servlet HttpSession). Password PBKDF2, refresh token SHA-256 hash lưu bảng Session
- **DI:** Không framework, `new` trực tiếp
- **Email:** OutBox pattern (lưu event → xử lý bất đồng bộ)
- **Payment:** Sepay.vn webhook (bank transfer/QR)
- **AI:** Google AI API (chat streaming)

## 3. Database — 13 bảng

| Bảng | Mô tả |
|---|---|
| `User` | Người dùng (ADMIN/USER, PENDING/ACTIVE/INACTIVE/BANNED) |
| `Brand` | Thương hiệu (Samsung, WD, Synology, TP-Link, SanDisk) |
| `Product` | Sản phẩm (STORAGE_DEVICE/NETWORK_DEVICE/ACCESSORY) |
| `ProductVariant` | Biến thể (price, sku, quantity, imageUrl) |
| `Order` | Đơn hàng (PENDING/CONFIRMED/SHIPPING/COMPLETED/CANCELLED) |
| `Payment` | Thanh toán (Sepay integration) |
| `OrderCart` | Giỏ hàng (1:user) |
| `ItemCart` | Items trong giỏ |
| `Voucher` | Mã giảm giá |
| `Session` | Phiên đăng nhập (hash refresh token) |
| `OutBox` | Event queue (7 loại event) |
| `SavedProduct` | Sản phẩm yêu thích |
| `Contact` | Liên hệ |

## 4. Module chính

| Module | Package | Chức năng |
|---|---|---|
| Auth | `module.core.auth` | Login, register, forgot password, email verification |
| User | `module.core.user` | CRUD user (admin), profile |
| Product | `module.bussiness.product` | Home, search, detail, category, CRUD, variant management |
| Cart | `module.bussiness.cart` | Thêm/sửa/xóa giỏ hàng |
| Order | `module.bussiness.order` | Checkout, order history, Sepay webhook |
| AI Chat | `module.bussiness.ai` | Chat streaming với Google AI |
| Admin | `module.core.admin` | Dashboard analytics, quản lý đơn hàng |
| Voucher | `module.bussiness.voucher` | Quản lý mã giảm giá |
| Wishlist | `module.bussiness.wishlist` | Sản phẩm yêu thích |
| Contact | `module.bussiness.contact` | Form liên hệ |

## 5. Môi trường deployment

| Môi trường | URL |
|---|---|
| Local | `http://localhost:8080/WebApplication3` |
| Production | `https://ecommerce.anhemlinhnam.thaiandev.online` |

**Admin mặc định:**
- Email: `admin@linhnamstore.local`
- Password: `Admin@123!`

## 6. Cấu trúc thư mục

```
WebApplication3/
├── .env                          ← Biến môi trường (DB, SMTP, JWT)
├── .gitignore
├── build.xml                     ← Ant build script
├── sto.sql                       ← Database schema + seed data (30+ sản phẩm)
├── index.html                    ← Redirect → /home
├── docker-compose.yml            ← MySQL 8 + Payara Server
├── Dockerfile                    ← Payara Server 6.2023.1-jdk17
├── CLAUDE.md                     ← Hướng dẫn cho Claude Code
├── README.md                     ← Tài liệu dự án
│
├── .github/workflows/
│   ├── ci.yml                    ← GitHub Actions CI (syntax check)
│   └── deploy.yml                ← GitHub Actions CD (build → SCP → VPS Docker)
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
│       │   ├── retry/            ← Retry framework (FIXED, EXPONENTIAL, FIBONACCI)
│       │   └── type/             ← Result<T>, UserPayload
│       │
│       ├── entity/               ← 13 Entity classes (POJO, không JPA)
│       │
│       └── module/
│           ├── core/             ← Module lõi
│           │   ├── config/       ← ConfigService (.env parser), DbConfig, AppConfig
│           │   ├── sql/          ← JdbcHelper, RowMapper
│           │   ├── auth/         ← AuthController, AuthService, PasswordService
│           │   ├── user/         ← UserController, UserService, UserRepository
│           │   ├── outbox/       ← OutBoxService, TypeEvent (event-driven email)
│           │   ├── mail/         ← EmailService (SMTP/Gmail)
│           │   ├── page/         ← Public page, Admin page, Profile, Wishlist controllers
│           │   └── admin/        ← AdminOrderController, AdminAnalyticsService
│           │
│           └── bussiness/        ← Module nghiệp vụ
│               ├── product/      ← ProductController, ProductService, VariantService
│               ├── cart/         ← CartController, CartService
│               ├── order/        ← OrderController, OrderService, SepayWebhookController
│               ├── brand/        ← BrandService
│               ├── voucher/      ← VoucherService
│               ├── wishlist/     ← WishlistService
│               ├── contact/      ← ContactService
│               └── ai/           ← AiChatController, AiChatService
│
├── web/
│   ├── index.html
│   ├── migrate-order.jsp         ← Runtime schema migration
│   ├── assets/
│   │   ├── css/style.css         ← Custom CSS
│   │   └── js/app.js             ← Custom JS
│   ├── layouts/                  ← header.jsp, footer.jsp, main-layout.jsp
│   ├── pages/                    ← home, cart, checkout, pay, profile, wishlist, ...
│   ├── views/
│   │   ├── auth/                 ← login, register, forgot-password
│   │   ├── admin/                ← brands, products, users, vouchers, orders, dashboard
│   │   └── order/                ← detail, history
│   └── WEB-INF/
│       ├── glassfish-web.xml
│       └── lib/
│           └── mysql-connector-j-8.4.0.jar
│
└── nbproject/                    ← NetBeans project config
    ├── project.properties
    ├── project.xml
    ├── build-impl.xml
    └── ant-deploy.xml
```

---

# HƯỚNG DẪN SETUP

## Cách 1: Docker (nhanh nhất, khuyến nghị)

**Yêu cầu:** Docker + Docker Compose đã cài đặt

```bash
cd /home/andev/dev/projects/storage_devices_and_network_devices_ecommerce
```

### Bước 1: Tạo file `.env`

```env
DB_PASSWORD=your_secure_password
SMTP_USER=your_email@gmail.com
SMTP_PASS=your_app_password
SMTP_FROM=your_email@gmail.com
GOOGLE_AI_API_KEY=your_google_ai_key
```

### Bước 2: Build WAR file

```bash
# Cần NetBeans hoặc GlassFish đã cài để có Jakarta EE API
ant clean dist
```

Nếu không có NetBeans, cài đặt JDK 17 + tải GlassFish:

```bash
wget https://github.com/eclipse-ee4j/glassfish/releases/download/6.2.5/glassfish-6.2.5.zip
unzip glassfish-6.2.5.zip
ant -Dj2ee.server.home=$PWD/glassfish6/glassfish clean dist
```

### Bước 3: Chạy Docker Compose

```bash
docker compose up -d --build
```

### Bước 4: Kiểm tra

```bash
docker compose ps                    # Xem container status
docker compose logs -f app           # Xem log ứng dụng
docker compose logs -f db            # Xem log database
```

**Truy cập:**
- App: `http://localhost:8080`
- MySQL: `127.0.0.1:3306` (chỉ bind local)
- Database name: `sto_db`

Dữ liệu mẫu tự động import từ `sto.sql` khi container DB khởi động lần đầu.

### Stop

```bash
docker compose down                  # Stop containers, giữ data
docker compose down -v               # Stop + xóa volume (mất data)
```

---

## Cách 2: Chạy local với GlassFish (phát triển)

**Yêu cầu:**
- JDK 17
- GlassFish 6.1.0 hoặc 6.2.5
- MySQL 8.0 đang chạy
- Apache NetBeans (khuyến nghị)

### Bước 1: Tạo database

```bash
mysql -u root -p
```

```sql
CREATE DATABASE sto_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE sto_db;
SOURCE sto.sql;
```

### Bước 2: Tạo file `.env` ở thư mục gốc dự án

```env
DB_HOST=localhost
DB_PORT=3306
DB_NAME=sto_db
DB_USER=root
DB_PASSWORD=your_mysql_password
SMTP_USER=your_email@gmail.com
SMTP_PASS=your_app_password
SMTP_FROM=your_email@gmail.com
GOOGLE_AI_API_KEY=your_google_ai_key
APP_BASE_URL=http://localhost:8080/WebApplication3
```

### Bước 3: Copy MySQL Connector vào `web/WEB-INF/lib/`

File `mysql-connector-j-8.4.0.jar` đã có sẵn trong `web/WEB-INF/lib/`. Nếu thiếu:

```bash
curl -L -o web/WEB-INF/lib/mysql-connector-j-8.4.0.jar \
  "https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.4.0/mysql-connector-j-8.4.0.jar"
```

### Bước 4: Build

```bash
ant clean build
```

Hoặc trong NetBeans: Right-click project → Build

### Bước 5: Deploy

**Cách 5a — NetBeans:**
1. Right-click project → Deploy
2. GlassFish tự start + deploy

**Cách 5b — Thủ công:**

```bash
# Build WAR
ant clean dist

# Copy WAR vào GlassFish autodeploy
cp dist/WebApplication3.war /path/to/glassfish/glassfish/domains/domain1/autodeploy/

# Hoặc dùng asadmin
/path/to/glassfish/glassfish/bin/asadmin start-domain
/path/to/glassfish/glassfish/bin/asadmin deploy dist/WebApplication3.war
```

### Bước 6: Truy cập

- Trang chủ: `http://localhost:8080/WebApplication3/home`
- Admin: `http://localhost:8080/WebApplication3/admin/dashboard`
- Login admin: `admin@linhnamstore.local` / `Admin@123!`

---

## Cách 3: CI/CD qua GitHub Actions

**Trigger:** Push lên `main`, `master`, hoặc `dev`

**Yêu cầu secrets trong GitHub repo:**

| Secret | Mô tả |
|---|---|
| `VPS_HOST` | IP/domain VPS |
| `VPS_USERNAME` | SSH username |
| `VPS_PASSWORD` | SSH password |
| `JWT_SECRET` | JWT signing key |
| `SMTP_USER` | Gmail SMTP user |
| `SMTP_PASS` | Gmail app password |
| `SMTP_FROM` | Email gửi đi |
| `GOOGLE_AI_API_KEY` | Google AI API key |

**Flow:**
1. Checkout code → Setup JDK 17 → Download GlassFish → `ant clean dist`
2. SCP WAR + Dockerfile + docker-compose.yml + sto.sql → VPS
3. SSH VPS → generate `.env` → `docker build` → `docker compose up -d`

---

## Checklist kiểm tra sau setup

| Bước | Command | Kết quả mong đợi |
|---|---|---|
| Database import | `mysql sto_db < sto.sql` | Không lỗi, 13 bảng tạo xong |
| Check tables | `mysql sto_db -e "SHOW TABLES;"` | 13 bảng |
| Check seed data | `mysql sto_db -e "SELECT COUNT(*) FROM Product;"` | 30+ sản phẩm |
| Check admin | `mysql sto_db -e "SELECT email, role FROM User WHERE role='ADMIN';"` | `admin@linhnamstore.local` |
| Build WAR | `ant clean dist` | Tạo `dist/WebApplication3.war` |
| Docker up | `docker compose up -d` | 2 containers running |
| App accessible | `curl http://localhost:8080/home` | HTML response, không 500 |
| Login admin | Trình duyệt → `/auth` → login | Vào được admin dashboard |
