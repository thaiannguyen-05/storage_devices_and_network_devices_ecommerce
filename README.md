# Ecommerce (Java Servlet)

Backend web application cho hệ thống Ecommerce, build bằng **Ant/NetBeans**, chạy trên **Jakarta EE server** (GlassFish).

## 1) Tổng quan

- Ngôn ngữ: **Java 17**
- Kiến trúc hiện tại: Servlet + Service + Repository (đang ở mức khung)
- Build tool: **Ant** (`build.xml`)
- Output artifact: `dist/Ecommerce.war`
- Kết nối DB: MySQL + **HikariCP** connection pool

## 2) Quick Start

### Yêu cầu

- JDK 17
- Ant
- MySQL
- (Khuyến nghị) NetBeans + GlassFish để deploy WAR nhanh

### Cấu hình môi trường

Tạo file `.env` ở root project:

```env
DB_HOST=localhost
DB_PORT=3306
DB_NAME=ecommerce
DB_USER=root
DB_PASSWORD=your_password
```

> `DB_PORT` mặc định là `3306` nếu thiếu hoặc sai format.

### Build

```bash
ant -f build.xml clean
ant -f build.xml dist
```

Artifact sau khi build:

```text
dist/Ecommerce.war
```

## 3) Endpoint hiện có (skeleton)

| Endpoint | Controller |
|---|---|
| `/auth` | `src/java/module/core/auth/AuthController.java` |
| `/user` | `src/java/module/core/user/UserController.java` |
| `/product` | `src/java/module/bussiness/product/ProductController.java` |
| `/order` | `src/java/module/bussiness/order/OrderController.java` |
| `/payment` | `src/java/module/bussiness/payment/PaymentController.java` |

Hiện tại các controller chủ yếu trả HTML mẫu (skeleton), chưa phải API nghiệp vụ hoàn chỉnh.

## 4) Cấu trúc mã nguồn chính

```text
src/java/
├── common/                         # filter, middleware, response wrapper, logger
├── entity/                         # POJO entity (không dùng ORM annotation)
└── module/
    ├── core/
    │   ├── auth/
    │   ├── user/
    │   ├── config/                 # đọc biến môi trường (.env)
    │   └── sql/
    │       ├── ConnecDb.java       # HikariCP pool
    │       └── repository/         # repository classes (đang là khung)
    └── bussiness/
        ├── product/
        ├── order/
        └── payment/
```

## 5) Data layer hiện tại

- `ConnecDb` khởi tạo **HikariDataSource** dùng pool để cấp phát connection.
- Repository nằm trong `src/java/module/core/sql/repository/`.
- Nhiều repository/service hiện đang để khung để hoàn thiện dần logic CRUD/business.

## 6) Thư viện chính

- Jackson (`jackson-core`, `jackson-databind`, `jackson-annotations`)
- MySQL Connector/J
- `dotenv-java`
- HikariCP

## 7) Troubleshooting nhanh

- `ant: command not found`  
  => Cài Ant và thêm vào `PATH`.

- Lỗi kết nối DB khi chạy  
  => Kiểm tra `.env` và trạng thái MySQL (`DB_HOST/DB_PORT/DB_NAME/DB_USER/DB_PASSWORD`).

- WAR build thành công nhưng endpoint không chạy  
  => Kiểm tra context path sau deploy và mapping servlet (`@WebServlet`).

