# Proposal: Database-Driven Pages — Loại bỏ data mock/hardcode

## Context

Project LinhNamStore — Jakarta EE Servlet (JDK 17, GlassFish 6.x, JSP + Servlet, MySQL). Đã có 12 Controller, 63 JSP files, và các Service/Repository cho Auth, Product, Cart, Order, User.

**Vấn đề:** Nhiều page dùng data hardcode/mock thay vì lấy từ database. Cụ thể:
- 5 Controller là stub (không gọi service/DB)
- 20+ JSP file có bảng HTML hardcode row
- 2 chỗ hardcoded data được seed mỗi request (test product, voucher)

**Mục tiêu:** TẤT CẢ page phải lấy dữ liệu thực từ database. Không còn data mock/hardcode trên frontend.

## Audit: Trang nào đã OK, trang nào cần sửa

### Trang ĐÃ lấy data từ DB (không cần sửa)

| Page | URL | Controller | Status |
|------|-----|-----------|--------|
| Home | `/home` | ProductController | OK — ProductService query DB |
| Product Detail | `/product?id=` | ProductController | OK — ProductService + VariantService |
| Cart | `/cart` | CartController | OK — CartService query DB |
| Checkout | `/checkout` | OrderController | OK — OrderService + CartService |
| Pay | `/checkout?pay=` | OrderController | OK — OrderService query DB |
| Order History | `/orders` | OrderController | OK — OrderService query DB |
| Order Detail | `/order/detail` | OrderController | OK — OrderService query DB |
| Admin Products | `/admin/products` | ProductController | OK — ProductService query DB |
| Admin Users | `/admin/users` | UserController | OK — UserService query DB |
| Login/Register/Auth | `/auth` | AuthController | OK — AuthService query DB |
| Sepay Webhook | `/api/sepay/webhook` | SepayWebhookController | OK — JdbcHelper trực tiếp |

### Trang CẦN SỬA (stub hoặc hardcoded)

| # | Page | URL | Controller | Vấn đề | Mức độ |
|---|------|-----|-----------|--------|--------|
| 1 | Admin Dashboard | `/admin/dashboard` | AdminDashboardController | Stub — không gọi service, không load thống kê | Cao |
| 2 | Admin Brands list/form | `/admin/brands` | AdminCatalogPageController | Stub — không gọi service, CRUD không implement, JSP hardcode bảng | Cao |
| 3 | Admin Vouchers list/form | `/admin/vouchers` | AdminCatalogPageController | Stub — không gọi service, CRUD không implement, JSP hardcode bảng | Cao |
| 4 | Admin Orders JSP | `/admin/orders` | OrderController | Controller OK nhưng JSP (`admin/order-list.jsp`, `admin/order-detail.jsp`) dùng hardcode rows thay vì `${ordersResult}` | Trung bình |
| 5 | Wishlist | `/wishlist` | WishlistController | Stub — không gọi service, JSP hardcode sản phẩm | Trung bình |
| 6 | Profile | `/profile` | ProfileController | Stub — không load user từ DB, không update profile, success message hardcode | Trung bình |
| 7 | Contact POST | `/contact` (POST) | PublicPageController | Stub — không lưu contact vào DB, success message hardcode | Trung bình |
| 8 | About | `/about` | PublicPageController | Static page — OK (không cần DB) | Thấp |

## Design Decisions

### Tách AdminCatalogPageController
Hiện tại `/admin/brands` và `/admin/vouchers` chung 1 controller stub. Sẽ tách thành 2 controller riêng:
- `BrandController` → `/admin/brands`
- `VoucherController` → `/admin/vouchers`

Lý do: mỗi controller có service riêng, action riêng, dễ maintain.

### Tách PublicPageController
Hiện tại `/contact` và `/about` chung 1 controller. Sẽ tách:
- `ContactController` → `/contact`
- About giữ nguyên trong `PublicPageController` (static page)

### Reuse repository có sẵn
Product module đã có `BrandRepository.java` (trong `module/bussiness/product/repository/impl/`). Sẽ viết `BrandService` dùng repository này, KHÔNG tạo duplicate.

## Implementation Plan

### Bước 1: BrandService (tái dụng BrandRepository có sẵn)

**File mới:**
- `src/java/module/bussiness/brand/BrandService.java`
- `src/java/module/bussiness/brand/dto/CreateBrandDto.java`
- `src/java/module/bussiness/brand/dto/UpdateBrandDto.java`
- `src/java/module/bussiness/brand/dto/BrandResponseDto.java`
- `src/java/module/bussiness/brand/dto/ListBrandResponseDto.java`

**Tái dụng:**
- `module/bussiness/product/repository/interfaces/IBrandRepository.java` (đã tồn tại)
- `module/bussiness/product/repository/impl/BrandRepository.java` (đã tồn tại)

**Methods:**
- `getAllBrands()` → trả về list brand (dùng cho admin list + product form select)
- `getBrandById(id)` → trả về brand chi tiết
- `createBrand(dto)` → INSERT brand
- `updateBrand(dto)` → UPDATE brand
- `deleteBrand(id)` → DELETE brand
- `changeStatus(id, status)` → UPDATE status

### Bước 2: VoucherService + VoucherRepository

**File mới:**
- `src/java/module/bussiness/voucher/VoucherService.java`
- `src/java/module/bussiness/voucher/dto/CreateVoucherDto.java`
- `src/java/module/bussiness/voucher/dto/UpdateVoucherDto.java`
- `src/java/module/bussiness/voucher/dto/VoucherResponseDto.java`
- `src/java/module/bussiness/voucher/dto/ListVoucherResponseDto.java`
- `src/java/module/bussiness/voucher/repository/interfaces/IVoucherRepository.java`
- `src/java/module/bussiness/voucher/repository/impl/VoucherRepository.java`

**Methods:**
- `getAllVouchers()` → SELECT FROM vouchers
- `getVoucherById(id)` → SELECT WHERE id
- `createVoucher(dto)` → INSERT
- `updateVoucher(dto)` → UPDATE
- `deleteVoucher(id)` → DELETE
- `getActiveVouchersForUser(userId)` → SELECT WHERE userId + expTime > NOW + quantity > 0

### Bước 3: WishlistService + WishlistRepository

**File mới:**
- `src/java/module/bussiness/wishlist/WishlistService.java`
- `src/java/module/bussiness/wishlist/dto/WishlistItemDto.java`
- `src/java/module/bussiness/wishlist/dto/ListWishlistResponseDto.java`
- `src/java/module/bussiness/wishlist/repository/interfaces/IWishlistRepository.java`
- `src/java/module/bussiness/wishlist/repository/impl/WishlistRepository.java`

**Table:** `saved_product` (đã có trong SQL schema)

**Methods:**
- `getWishlistByUserId(userId)` → JOIN saved_product với product/variant
- `addToWishlist(userId, productId)` → INSERT
- `removeFromWishlist(userId, productId)` → DELETE

### Bước 4: ContactService + ContactRepository

**File mới:**
- `src/java/module/bussiness/contact/ContactService.java`
- `src/java/module/bussiness/contact/dto/ContactRequestDto.java`
- `src/java/module/bussiness/contact/repository/interfaces/IContactRepository.java`
- `src/java/module/bussiness/contact/repository/impl/ContactRepository.java`

**Table:** `contact` (đã có trong SQL schema: id, name, email, content, createdAt)

**Methods:**
- `submitContact(dto)` → INSERT contact, validate email format, fields not empty

### Bước 5: DashboardService

**File mới:**
- `src/java/module/core/dashboard/DashboardService.java`
- `src/java/module/core/dashboard/dto/DashboardStatsDto.java`

**Methods:**
- `getStats()` → COUNT users, orders, products, SUM(order total)
- `getRevenueLast7Days()` → SELECT DATE(createdAt), SUM(total) GROUP BY DATE
- `getRecentOrders(limit)` → SELECT FROM orders ORDER BY createdAt DESC LIMIT 5

### Bước 6: Sửa Controller

**Sửa:**

| File | Thay đổi |
|------|----------|
| `AdminDashboardController.java` | Gọi DashboardService, set `dashboardStats`, `recentOrders`, `revenueChart` vào request |
| `AdminCatalogPageController.java` | Xóa. Thay bằng `BrandController.java` + `VoucherController.java` riêng |
| `BrandController.java` (mới) | @WebServlet `/admin/brands`, gọi BrandService, CRUD actions |
| `VoucherController.java` (mới) | @WebServlet `/admin/vouchers`, gọi VoucherService, CRUD actions |
| `WishlistController.java` | Gọi WishlistService, set `wishlistProducts` vào request |
| `ProfileController.java` | doGet: load user từ UserService; doPost `update-profile`: gọi UserService.updateUser(); doPost `change-password`: gọi UserService.changePassword() |
| `ContactController.java` (mới) | @WebServlet `/contact`, doGet forward JSP, doPost gọi ContactService.submitContact() |
| `PublicPageController.java` | Giữ lại `/about`, xóa phần `/contact` (đã tách) |
| `ProductController.java` | Xóa method `checkAndCreateTestProduct()` (dòng 243-276) |
| `OrderController.java` | Xóa voucher seeding hardcode (dòng 102-138) |

### Bước 7: Sửa JSP — thay hardcode bằng dynamic data

**JSP cần sửa (thay bảng hardcode → JSTL loop):**

| File | Thay đổi |
|------|----------|
| `web/admin/dashboard.jsp` | Stats cards: `${stats.totalUsers}`, `${stats.totalOrders}`, ... |
| `web/admin/brand-list.jsp` | `<c:forEach>` qua `${brandsResult.brands}` |
| `web/admin/brand-form.jsp` | Pre-fill `${brand}` khi edit |
| `web/admin/voucher-list.jsp` | `<c:forEach>` qua `${vouchersResult.vouchers}` |
| `web/admin/voucher-form.jsp` | Pre-fill `${voucher}` khi edit |
| `web/admin/order-list.jsp` | `<c:forEach>` qua `${ordersResult.orders}` |
| `web/admin/order-detail.jsp` | Hiển thị `${orderResult.order}` |
| `web/pages/wishlist.jsp` | `<c:forEach>` qua `${wishlistProducts}` |
| `web/pages/profile.jsp` | Hiển thị `${currentUser}` từ session, error/success từ request |
| `web/views/admin/dashboard.jsp` |同上 |
| `web/views/admin/brands/list.jsp` |同上 |
| `web/views/admin/brands/create.jsp` |同上 |
| `web/views/admin/brands/edit.jsp` |同上 |
| `web/views/admin/vouchers/list.jsp` |同上 |
| `web/views/admin/vouchers/create.jsp` |同上 |
| `web/views/admin/vouchers/edit.jsp` |同上 |
| `web/views/admin/orders/list.jsp` |同上 |
| `web/views/admin/orders/detail.jsp` |同上 |
| `web/views/home.jsp` | Xóa 8 product hardcode, dùng `${newProducts}` từ controller |
| `web/views/cart/cart.jsp` | Xóa 2 cart items hardcode, dùng `${cartResult.items}` |
| `web/views/order/checkout.jsp` | Xóa product hardcode, dùng `${checkoutItems}` |
| `web/views/order/detail.jsp` | Xóa ORD-0001/0002 hardcode, dùng `${orderResult.order}` |

### Bước 8: SQL schema bổ sung (nếu cần)

Kiểm tra bảng `saved_product` có column `user_id` chưa. Nếu chưa, cần ALTER TABLE để wishlist theo từng user.

Kiểm tra bảng `contact` đã đủ column chưa (id, name, email, content, createdAt).

## File Summary

### File mới cần tạo (~30 files)

```
src/java/module/bussiness/brand/
├── BrandService.java
├── BrandController.java
└── dto/
    ├── CreateBrandDto.java
    ├── UpdateBrandDto.java
    ├── BrandResponseDto.java
    └── ListBrandResponseDto.java

src/java/module/bussiness/voucher/
├── VoucherService.java
├── VoucherController.java
├── dto/
│   ├── CreateVoucherDto.java
│   ├── UpdateVoucherDto.java
│   ├── VoucherResponseDto.java
│   └── ListVoucherResponseDto.java
└── repository/
    ├── interfaces/IVoucherRepository.java
    └── impl/VoucherRepository.java

src/java/module/bussiness/wishlist/
├── WishlistService.java
├── WishlistController.java
├── dto/
│   ├── WishlistItemDto.java
│   └── ListWishlistResponseDto.java
└── repository/
    ├── interfaces/IWishlistRepository.java
    └── impl/WishlistRepository.java

src/java/module/bussiness/contact/
├── ContactService.java
├── ContactController.java
├── dto/ContactRequestDto.java
└── repository/
    ├── interfaces/IContactRepository.java
    └── impl/ContactRepository.java

src/java/module/core/dashboard/
├── DashboardService.java
└── dto/DashboardStatsDto.java
```

### File cần sửa (~12 files)

```
src/java/module/core/page/AdminDashboardController.java  ← gọi DashboardService
src/java/module/core/page/AdminCatalogPageController.java ← XÓA (tách thành 2 controller)
src/java/module/core/page/WishlistController.java         ← gọi WishlistService
src/java/module/core/page/ProfileController.java          ← gọi UserService
src/java/module/core/page/PublicPageController.java       ← xóa phần /contact
src/java/module/bussiness/product/ProductController.java  ← xóa test product seed
src/java/module/bussiness/order/OrderController.java      ← xóa voucher seed
web/admin/dashboard.jsp                                   ← dynamic stats
web/admin/brand-list.jsp                                  ← JSTL loop
web/admin/voucher-list.jsp                                ← JSTL loop
web/admin/order-list.jsp                                  ← JSTL loop
web/pages/wishlist.jsp                                    ← JSTL loop
```

+ 12 file `web/views/admin/**/*.jsp` và `web/views/home/cart/order/*.jsp` sửa tương tự

### Không cần sửa (đã OK)

- `AuthController.java` + toàn bộ auth JSP
- `ProductController.java` (trừ xóa test product seed) — home, product detail đã query DB
- `CartController.java` + cart JSP
- `OrderController.java` (trừ xóa voucher seed) — checkout, order history đã query DB
- `UserController.java` + admin user JSP
- `SepayWebhookController.java`

## Implementation Order

1. **DashboardService** + sửa AdminDashboardController + sửa dashboard.jsp (nhỏ nhất)
2. **BrandService + BrandController** + sửa brand JSP
3. **VoucherService + VoucherController + VoucherRepository** + sửa voucher JSP
4. **WishlistService + WishlistController + WishlistRepository** + sửa wishlist.jsp
5. **ContactService + ContactController + ContactRepository** + sửa contact flow
6. **Sửa ProfileController** — load/update user từ UserService
7. **Sửa Admin Orders JSP** — thay hardcode bằng `${ordersResult}`
8. **Xóa hardcoded data** — test product seed, voucher seed, views hardcode
9. **Sửa tất cả views/*.jsp** — xóa product/cart/order hardcode

## Verification

1. Build project, deploy lên GlassFish
2. Test từng page:
   - `/admin/dashboard` → stats từ DB (count thực user/order/product)
   - `/admin/brands` → CRUD brand, data persist DB
   - `/admin/vouchers` → CRUD voucher, data persist DB
   - `/admin/orders` → bảng đơn hàng từ DB (không hardcode row)
   - `/wishlist` → sản phẩm yêu thích từ DB
   - `/profile` → thông tin user từ DB, update được
   - `/contact` → submit form, lưu vào DB
   - `/home` → sản phẩm từ DB (không hardcode trong views/home.jsp)
3. Query DB trực tiếp: INSERT/UPDATE/DELETE đúng
4. Console log: KHÔNG còn "checkAndCreateTestProduct" hoặc voucher seeding
5. Kiểm tra views/ folder: KHÔNG còn file nào có hardcode product/cart/order rows
