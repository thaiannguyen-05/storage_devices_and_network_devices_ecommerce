# Developer Guide — Storage Devices & Network Devices Ecommerce

Tài liệu này là đặc tả kỹ thuật cho dev triển khai theo **từng feature** (rõ business flow, security, data model, API hướng dẫn). Nội dung bám theo đề bài tại [InitProject.md](InitProject.md) và trạng thái code hiện tại của repo.

---

## 1) Mục tiêu dự án

Xây dựng website e-commerce cho nhóm sản phẩm thiết bị lưu trữ và thiết bị mạng, gồm các feature chính:

1. Auth (đăng ký/đăng nhập/đăng xuất) an toàn
2. Quản lý sản phẩm theo mô hình **SPU/SKU**
3. Giỏ hàng + checkout + đặt hàng + thanh toán
4. Liên hệ
5. Trang chủ + trang chi tiết sản phẩm

Yêu cầu học phần: xem [InitProject.md](InitProject.md).

---

## 2) Stack và cấu trúc hiện tại

### 2.1 Stack

- Java 17
- Jakarta Servlet (Jakarta EE 9.1)
- Ant (NetBeans project)
- MySQL
- HikariCP
- Jackson
- dotenv-java

Tham chiếu:
- [build.xml](../build.xml)
- [nbproject/project.properties](../nbproject/project.properties)
- [lib/](../lib/)

### 2.2 Cấu trúc source

```text
src/java/
├── common/
├── entity/
└── module/
    ├── core/
    │   ├── auth/
    │   ├── user/
    │   ├── config/
    │   └── sql/
    └── bussiness/
        ├── product/
        ├── cart/
        ├── order/
        └── payment/
```

File nền tảng:
- DB schema: [sto.sql](../sto.sql)
- DB pool: [ConnecDb.java](../src/java/module/core/sql/ConnecDb.java)
- Env loader: [ConfigService.java](../src/java/module/core/config/ConfigService.java)

---

## 3) Setup local

### 3.1 Prerequisites

- JDK 17
- Ant
- MySQL 8+
- (khuyến nghị) NetBeans + GlassFish/Payara

### 3.2 `.env`

Tạo file `.env` từ [.env.example](../.env.example):

```env
DB_HOST=localhost
DB_PORT=3306
DB_NAME=ecommerce
DB_USER=root
DB_PASSWORD=your_password
```

### 3.3 Database + build

1. Tạo DB (ví dụ `ecommerce`)
2. Chạy [sto.sql](../sto.sql)
3. Build:

```bash
ant -f build.xml clean
ant -f build.xml dist
```

WAR output: `dist/Ecommerce.war`.

---

## 4) Feature Spec chi tiết

## 4.1 Feature: Authentication (Session-based + JWT)

### Mục tiêu

- Đăng ký/đăng nhập an toàn
- Dùng **Access Token + Refresh Token**
- Có session phía server để quản lý thiết bị/phiên đăng nhập
- Phát hiện IP lạ và gửi email cảnh báo

### Security bắt buộc

1. **Password bắt buộc hash trước khi lưu DB**
   - Dùng `Argon2id` (ưu tiên) hoặc `bcrypt`
   - Không lưu plain text password
2. **Refresh token bắt buộc hash trước khi lưu DB**
   - Không lưu refresh token raw
3. Access token JWT sống ngắn (ví dụ 15 phút)
4. Refresh token sống dài hơn (ví dụ 7-30 ngày), rotate mỗi lần refresh

### Luồng đăng ký

1. Validate input (email/password/dateOfBirth...)
2. Hash password
3. Tạo user với trạng thái phù hợp (`PENDING`/`ACTIVE` theo rule)
4. Trả response thành công (không trả hash)

### Luồng đăng nhập

1. Kiểm tra user theo email
2. Verify password hash
3. Thu thập fingerprint cơ bản:
   - `ipAddress`
   - `userAgent`
4. Kiểm tra IP bất thường:
   - Nếu IP chưa từng xuất hiện hoặc rủi ro cao ⇒ tạo security event
   - Gửi email cảnh báo: “Phát hiện đăng nhập từ IP lạ”
5. Tạo session server-side
6. Phát hành:
   - accessToken (JWT)
   - refreshToken (raw chỉ trả 1 lần cho client)
7. Lưu `hashRefreshToken` vào bảng session

### Luồng refresh token

1. Nhận refresh token
2. So khớp với hash trong session
3. Nếu hợp lệ: rotate refresh token (hash mới), cấp access token mới
4. Nếu không hợp lệ: revoke session + buộc đăng nhập lại

### Luồng logout

- Logout 1 thiết bị: revoke session hiện tại
- Logout all: revoke toàn bộ session của user

### Đề xuất endpoint Auth

- `POST /auth/register`
- `POST /auth/login`
- `POST /auth/refresh`
- `POST /auth/logout`
- `POST /auth/logout-all`

### Đề xuất schema liên quan

Bảng hiện có: `User`, `Session` trong [sto.sql](../sto.sql).

Để phục vụ IP lạ/email cảnh báo rõ ràng, nên bổ sung:
- Trong `Session`:
  - `ipAddress`
  - `userAgent`
  - `lastSeenAt`
  - `revokedAt`
- Hoặc thêm bảng `LoginHistory`/`SecurityEvent` để audit

---

## 4.2 Feature: Product Catalog theo SPU/SKU

### Khái niệm bắt buộc

- **SPU (Standard Product Unit)**: sản phẩm chính (model tổng)
- **SKU (Stock Keeping Unit)**: biến thể bán thực tế (dung lượng/màu/phiên bản...)

Trong code hiện tại:
- SPU tương ứng `Product`
- SKU tương ứng `ProductVariant`

Tham chiếu:
- [ProductEntity.java](../src/java/entity/ProductEntity.java)
- [ProductVariantEntity.java](../src/java/entity/ProductVariantEntity.java)
- [ProductRepository.java](../src/java/module/core/sql/repository/ProductRepository.java)

### Rule nghiệp vụ

1. Một SPU có nhiều SKU
2. SKU có `sku` duy nhất toàn hệ thống
3. Giá, tồn kho quản lý ở cấp SKU
4. Trang chi tiết sản phẩm hiển thị:
   - thông tin SPU (name/description/brand/category)
   - danh sách SKU (sku, price, quantity, imageUrl, trạng thái)

### Đề xuất endpoint Product

- `GET /product` (list SPU)
- `GET /product?id={spuId}` (detail SPU)
- `POST /product` (create SPU)
- `PUT /product?id={spuId}` (update SPU)
- `DELETE /product?id={spuId}` (soft delete/hard delete theo rule)
- (bổ sung) `POST /product/{spuId}/sku`
- (bổ sung) `PUT /product/sku/{skuId}`

### Data validation bắt buộc

- `sku` không trống + unique
- `price >= 0`
- `quantity >= 0`
- `brandId` và `userId` tồn tại

---

## 4.3 Feature: Cart & Checkout & Order & Payment

### Mục tiêu

Tạo luồng mua hàng rõ ràng, theo state machine để dev triển khai nhất quán.

### Workflow chuẩn

1. **Add to cart**
   - User chọn SKU + quantity
   - Validate tồn kho
   - Lưu vào cart item
2. **Review cart**
   - Tính subtotal, discount, phí ship (nếu có)
3. **Checkout**
   - Khóa snapshot giá/tồn tại thời điểm checkout
   - Tạo order `PENDING`
4. **Payment**
   - Tạo payment request
   - Chờ callback/IPN từ cổng thanh toán
5. **Payment success**
   - Order chuyển `CONFIRMED`
   - Trừ tồn kho SKU
6. **Fulfillment**
   - `SHIPPING` → `COMPLETED`
7. **Failure/Cancel**
   - Payment fail hoặc user cancel ⇒ `CANCELLED`

### Order state đề xuất

`PENDING -> CONFIRMED -> SHIPPING -> COMPLETED`

Nhánh lỗi:
- `PENDING -> CANCELLED`
- `CONFIRMED -> CANCELLED` (nếu chưa giao)

### Điểm cần rõ trong code

- Cart thao tác ở **SKU level**, không ở SPU level
- Trừ kho sau khi thanh toán thành công (hoặc giữ kho tạm có timeout)
- Chống double-submit callback payment (idempotency key)

### Đề xuất endpoint nghiệp vụ

- `POST /cart/items`
- `GET /cart`
- `PUT /cart/items/{itemId}`
- `DELETE /cart/items/{itemId}`
- `POST /checkout`
- `GET /order/{id}`
- `POST /payment/create`
- `POST /payment/ipn`

---

## 4.4 Feature: Contact

Theo đề bài, form liên hệ cần:

- Validate bắt buộc (không để trống)
- Validate định dạng (email/sđt)
- Lưu DB khi hợp lệ

Đề xuất endpoint:
- `POST /contact`
- `GET /contact` (admin)

---

## 4.5 Feature: Home & Product Detail

### Home

Hiển thị tối thiểu:
- ảnh sản phẩm
- tên/mã
- giá
- phân nhóm: hàng mới/bán chạy/giảm giá (khuyến khích)

### Product detail

Hiển thị dữ liệu lấy từ DB:
- thông tin SPU
- danh sách SKU + tồn kho + giá

---

## 5) Chuẩn code cho team

### 5.1 Layering rule

- Controller: parse request + validate cơ bản + response
- Service: business logic
- Repository: SQL + mapping

Không để logic nghiệp vụ lớn ở controller.

### 5.2 Security rule

- Không log password/token raw
- Không trả hash ra response
- Dùng `PreparedStatement`
- Validate input ở boundary

### 5.3 Naming rule

- SPU: `Product`
- SKU: `ProductVariant`
- Trạng thái dùng enum thống nhất với DB

---

## 6) Trạng thái hiện tại của codebase

Các controller còn skeleton/TODO:
- [AuthController.java](../src/java/module/core/auth/AuthController.java)
- [UserController.java](../src/java/module/core/user/UserController.java)
- [OrderController.java](../src/java/module/bussiness/order/OrderController.java)
- [PaymentController.java](../src/java/module/bussiness/payment/PaymentController.java)
- [CartController.java](../src/java/module/bussiness/cart/CartController.java)

Product đã có CRUD nền tảng:
- [ProductController.java](../src/java/module/bussiness/product/ProductController.java)
- [ProductRepository.java](../src/java/module/core/sql/repository/ProductRepository.java)

---

## 7) Implementation checklist theo feature

### Auth

- [ ] Register: hash password
- [ ] Login: verify hash + issue JWT pair
- [ ] Session: lưu hash refresh token
- [ ] Refresh: rotate refresh token
- [ ] Logout/logout-all
- [ ] Detect IP lạ + gửi email cảnh báo

### Product (SPU/SKU)

- [ ] CRUD SPU
- [ ] CRUD SKU
- [ ] Unique SKU
- [ ] Product detail trả cả SPU + SKU

### Cart/Order/Payment

- [ ] Add/update/remove cart item theo SKU
- [ ] Checkout tạo order
- [ ] Payment create + IPN callback
- [ ] Chuyển trạng thái order đúng workflow
- [ ] Trừ tồn kho khi payment thành công

### Contact/UI

- [ ] Contact form validate + lưu DB
- [ ] Trang chủ + trang chi tiết chuẩn đề bài
- [ ] Đồng bộ layout Banner/Top/Left/Footer
- [ ] Đạt tối thiểu 30 sản phẩm

---

## 8) Troubleshooting nhanh

- `ant: command not found` → cài Ant
- Lỗi DB connection → kiểm tra `.env` + MySQL service
- Lỗi mapping endpoint → kiểm tra `@WebServlet`
- Lỗi FK/enum → đối chiếu [sto.sql](../sto.sql)

---

## 9) Tài liệu nên đọc đầu tiên cho dev mới

1. [InitProject.md](InitProject.md)
2. [sto.sql](../sto.sql)
3. [ProductController.java](../src/java/module/bussiness/product/ProductController.java)
4. [ProductRepository.java](../src/java/module/core/sql/repository/ProductRepository.java)
5. [ConnecDb.java](../src/java/module/core/sql/ConnecDb.java)
