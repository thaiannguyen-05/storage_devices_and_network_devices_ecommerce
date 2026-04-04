# Storage Devices and Network Devices Ecommerce

Tài liệu này mô tả **cấu trúc dự án** dựa trên source code hiện tại.

## 1) Tổng quan kiến trúc

Dự án là một Jakarta Servlet Web Application (Java 17), build bằng Ant/NetBeans, đóng gói WAR để deploy lên GlassFish.

- Kiểu project: `org.netbeans.modules.web.project` (NetBeans web project)
- Build script chính: `build.xml`
- Cấu hình project: `nbproject/`
- Mã nguồn Java: `src/java/`
- Tài nguyên web: `web/`
- Thư viện ngoài (Jackson): `lib/`

## 2) Cây thư mục chính

```text
storage_devices_and_network_devices_ecommerce/
├── build.xml
├── lib/
│   ├── jackson-annotations-2.17.0.jar
│   ├── jackson-core-2.17.0.jar
│   ├── jackson-databind-2.17.0.jar
│   └── ...
├── nbproject/
│   ├── project.xml
│   ├── project.properties
│   ├── build-impl.xml
│   ├── ant-deploy.xml
│   └── genfiles.properties
├── src/
│   ├── conf/
│   │   └── MANIFEST.MF
│   └── java/
│       ├── common/
│       │   ├── exceptionFilter/
│       │   │   └── ExceptionFilter.java
│       │   ├── interceptor/
│       │   │   └── TransformFilter.java
│       │   ├── middleware/
│       │   │   └── RequestIdFilter.java
│       │   ├── pipe/
│       │   │   └── pipeService.java
│       │   ├── type/
│       │   │   ├── ApiError.java
│       │   │   └── ApiResponse.java
│       │   ├── guard/
│       │   │   └── AuthGuard.java
│       │   └── logger/
│       │       └── LoggerService.java
│       └── module/
│           ├── core/auth/
│           │   ├── AuthController.java
│           │   └── AuthService.java
│           ├── user/
│           │   ├── UserController.java
│           │   └── UserService.java
│           ├── product/
│           │   ├── ProductController.java
│           │   └── ProductService.java
│           ├── order/
│           │   ├── OrderController.java
│           │   └── OrderService.java
│           └── payment/
│               ├── PaymentController.java
│               └── PaymentService.java
└── web/
    └── WEB-INF/
        └── glassfish-web.xml
```

## 3) Vai trò từng khối

### `src/java/common/`
Chứa các thành phần dùng chung toàn ứng dụng (cross-cutting concerns):

- `middleware/RequestIdFilter.java`  
  Gắn `X-Request-Id` cho request/response, tạo mới nếu client chưa gửi.

- `exceptionFilter/ExceptionFilter.java`  
  Bắt exception toàn cục và trả về chuẩn lỗi `ApiError` dạng JSON.

- `interceptor/TransformFilter.java`  
  Chuẩn hoá response thành `ApiResponse<T>` cho các response thành công.

- `pipe/pipeService.java`  
  Wrapper request để trim khoảng trắng của input params.

- `type/ApiError.java`, `type/ApiResponse.java`  
  DTO chuẩn cho response lỗi/thành công.

- `guard/AuthGuard.java`, `logger/LoggerService.java`  
  Hiện đang là placeholder (chưa có logic triển khai).

### `src/java/module/`
Chứa các domain/module nghiệp vụ theo nhóm chức năng:

- `core/auth/`
- `user/`
- `product/`
- `order/`
- `payment/`

Mỗi module đang theo cấu trúc cơ bản:

- `*Controller.java`: Servlet endpoint (ví dụ `/auth`, `/user`, `/product`, ...).
- `*Service.java`: lớp service nghiệp vụ (hiện đa số còn trống).

### `web/`
Chứa cấu hình web app runtime:

- `WEB-INF/glassfish-web.xml`: cấu hình dành cho GlassFish.

### `nbproject/`
Metadata/cấu hình NetBeans + Ant:

- `project.xml`: định nghĩa loại project web, libraries.
- `project.properties`: Java version, classpath, artifact WAR, server type.
- `build-impl.xml`, `ant-deploy.xml`: logic build/deploy do NetBeans sinh.

## 4) Luồng xử lý request (mức khái quát)

Ở mức tổng quát, request đi qua chuỗi filter trong `common/`, sau đó vào các `*Controller` trong `module/`. Response được chuẩn hoá theo `ApiResponse` (thành công) hoặc `ApiError` (lỗi).

> Lưu ý: thứ tự filter thực tế phụ thuộc container (annotation scanning / registration). Nếu cần kiểm soát chặt thứ tự lifecycle/filter-chain, nên cấu hình explicit order tại tầng đăng ký filter.

## 5) Thư viện chính

- Jackson 2.17.0 (`jackson-core`, `jackson-databind`, `jackson-annotations`) để xử lý JSON.

## 6) Trạng thái hiện tại (theo code hiện có)

- Các controller đang ở mức skeleton/demo response HTML.
- Nhiều service chưa có nghiệp vụ.
- Khối `common` là phần đã có logic rõ nhất cho chuẩn hoá request/response và xử lý lỗi.

---
Nếu bạn muốn, mình có thể viết thêm phần README thứ 2: **"Conventions & Development Guide"** (naming, cách thêm module mới, cách chuẩn hoá response/error, checklist khi thêm filter).