# Frontend Plan — LinhNamStore (Jakarta EE JSP/Servlet)

## Context

Dự án: LinhNamStore — e-commerce bán storage devices (SSD, HDD, NAS) và network devices (router, switch, accessories).
Stack: JDK 17, GlassFish 6.x, JSP + Servlet, MySQL. Không có framework CSS/JS nào. Không Spring DI.
Hiện tại: `web/` trống (chỉ có index.html placeholder + glassfish-web.xml). 14 entity đã tồn tại. Module backend trống.
Seed data: 10 sản phẩm, 5 brands, 24 variants, 1 admin user.
Yêu cầu CLAUDE.md: tối thiểu 30 sản phẩm, 4 layout section cố định (Banner, Top Menu, Left Menu, Content, Footer).

## Design Decisions

### CSS Strategy
- **Không dùng framework** (Bootstrap, Tailwind) — project yêu cầu tự viết CSS.
- **Single stylesheet**: `web/assets/css/style.css` — dễ maintain cho project size nhỏ.
- CSS custom properties (variables) cho theme colors, spacing, typography.
- Responsive: CSS Grid + Flexbox, media queries cho mobile/tablet/desktop.

### JS Strategy
- **Vanilla JS** — không npm, không bundler. Project dùng Ant build đơn giản.
- **Single script**: `web/assets/js/app.js` — form validation, cart localStorage, menu toggle, search debounce.
- Không dùng framework JS (Vue, React, Alpine).

### JSP Structure
- **JSP trong `web/` root** — trực tiếp, không qua WEB-INF (Servlet controller sẽ forward).
- **Layout fragments**: `web/layouts/` chứa các phần chung (banner.jsp, top-menu.jsp, left-menu.jsp, footer.jsp).
- Mỗi page JSP dùng `<jsp:include>` để nhúng fragments.
- JSTL (c:forEach, c:if, fmt:formatNumber) — có sẵn trong Jakarta EE.

## File Structure

```
web/
├── assets/
│   ├── css/
│   │   └── style.css                  ← toàn bộ CSS
│   ├── js/
│   │   └── app.js                     ← vanilla JS utilities
│   └── images/
│       └── logo.png                   ← logo LinhNamStore
├── layouts/
│   ├── header.jsp                     ← Banner + Top Menu + Left Menu
│   ├── footer.jsp                     ← Footer
│   └── main-layout.jsp               ← wrapper: include header + content + footer
├── pages/
│   ├── home.jsp                       ← Trang chủ: sản phẩm theo category
│   ├── product-detail.jsp            ← Chi tiết sản phẩm + variant + reviews
│   ├── cart.jsp                       ← Giỏ hàng (OrderCart)
│   ├── checkout.jsp                   ← Thanh toán
│   ├── order-history.jsp             ← Lịch sử đơn hàng
│   ├── profile.jsp                    ← Thông tin user
│   ├── wishlist.jsp                   ← Sản phẩm đã lưu
│   ├── contact.jsp                    ← Form liên hệ
│   ├── login.jsp                      ← Đăng nhập
│   ├── register.jsp                   ← Đăng ký
│   ├── forgot-password.jsp           ← Quên mật khẩu
│   ├── verify-email.jsp              ← Xác thực email
│   ├── about.jsp                      ← Giới thiệu nhóm
│   └── error/
│       ├── 404.jsp
│       └── 500.jsp
├── admin/
│   ├── dashboard.jsp                  ← Admin dashboard
│   ├── product-list.jsp              ← CRUD sản phẩm
│   ├── product-form.jsp              ← Thêm/sửa sản phẩm
│   ├── brand-list.jsp                ← CRUD thương hiệu
│   ├── brand-form.jsp                ← Thêm/sửa thương hiệu
│   ├── order-list.jsp                ← Quản lý đơn hàng
│   ├── order-detail.jsp              ← Chi tiết đơn hàng
│   ├── user-list.jsp                 ← Quản lý user
│   ├── user-form.jsp                 ← Thêm/sửa user
│   ├── voucher-list.jsp              ← CRUD voucher
│   └── voucher-form.jsp              ← Thêm/sửa voucher
├── WEB-INF/
│   └── glassfish-web.xml              ← có sẵn
├── META-INF/
│   └── context.xml                    ← (nếu cần JDBC resource)
└── index.html                         ← redirect tới home controller
```

## Layout Detail

### Banner (header.jsp)
- Logo LinhNamStore bên trái (link tới home)
- Thanh tìm kiếm ở giữa (input + button search)
- Bên phải: icon giỏ hàng (số lượng badge), link login/register hoặc avatar user + dropdown menu (profile, logout)
- Sticky top, z-index cao nhất
- Mobile: hamburger menu toggle

### Top Menu (header.jsp)
- Horizontal nav bar dưới banner
- Items: Trang chủ | Sản phẩm | Liên hệ | Giới thiệu
- Admin (nếu role=ADMIN): thêm "Quản trị" link tới /admin/dashboard
- Hover effect: color transition + underline scale animation
- Active state: border-bottom highlight

### Left Menu (header.jsp)
- Sidebar cố định bên trái content
- Sections:
  - **Danh mục**: STORAGE_DEVICE, NETWORK_DEVICE, ACCESSORY (radio/checkbox filter)
  - **Thương hiệu**: Samsung, Western Digital, Synology, TP-Link, SanDisk (checkbox filter)
  - **Khoảng giá**: <1tr, 1tr-3tr, 3tr-5tr, 5tr-10tr, >10tr (range filter)
  - **Trạng thái**: ACTIVE, OUT_OF_STOCK (toggle)
  - **Sắp xếp**: Giá tăng, Giá giảm, Mới nhất, Bán chạy (select dropdown)
- Mobile: collapse thành off-canvas drawer, toggle bằng nút filter trên top menu

### Footer (footer.jsp)
- 3 cột:
  - Cột 1: Logo + mô tả ngắn LinhNamStore
  - Cột 2: Link nhanh (Home, Contact, About, Chính sách)
  - Cột 3: Thông tin nhóm (tên thành viên + ngày sinh)
- Copyright line phía dưới

## Page Specifications

### 1. Trang chủ (home.jsp)
**URL**: `/` hoặc `/home`
**Controller**: HomeController (sẽ viết ở backend phase)
**Data**: List<ProductEntity> + List<ProductVariantEntity> grouped by category

Sections:
- **Banner/Hero**: 1-2 slide ảnh banner (có thể carousel đơn giản bằng CSS animation)
- **Hàng mới về**: Grid sản phẩm (ProductEntity.createdAt DESC, limit 8)
- **Bán chạy**: Grid sản phẩm (dựa trên Order count, limit 8)
- **Giảm giá**: Grid sản phẩm (so sánh giá variant, limit 8)
- **Tất cả sản phẩm**: Grid full, có pagination

Product card:
- Ảnh (ProductVariantEntity.imageUrl, lấy variant đầu tiên)
- Tên sản phẩm (ProductEntity.name)
- Mã SP (ProductEntity.id — 8 ký tự cuối)
- Giá (ProductVariantEntity.price — format VNĐ)
- Badge: "Mới" (createdAt < 7 ngày), "Giảm X%" (nếu có so sánh), "Hết hàng" (variant.quantity == 0)
- Hover: card scale up 1.03, shadow tăng, button "Xem chi tiết" hiện ra
- Click → redirect `/product?id={productId}`

### 2. Trang chi tiết sản phẩm (product-detail.jsp)
**URL**: `/product?id={uuid}`
**Data**: ProductEntity, List<ProductVariantEntity>, List<ProductReviewEntity>, BrandEntity

Layout 2 cột:
- **Cột trái**: 
  - Ảnh chính (imageUrl của selected variant)
  - Thumbnail các variant khác (click đổi ảnh)
- **Cột phải**:
  - Tên sản phẩm (h1)
  - Mã SP + Thương hiệu (link tới brand filter)
  - Mô tả (description)
  - **Chọn variant**: Dropdown/select các variant (SKU, giá, quantity). Khi chọn: cập nhật giá, hiển thị "Còn X sản phẩm" hoặc "Hết hàng"
  - Số lượng (input number, min=1, max=variant.quantity)
  - Button: "Thêm vào giỏ" (POST tới CartController), "Mua ngay" (POST → checkout)
  - Sản phẩm đã lưu (wishlist) — icon heart

Dưới cùng:
- **Tabs**: Mô tả chi tiết | Thông số kỹ thuật | Đánh giá
  - Tab đánh giá: form submit review (rating 1-5 stars, comment, reviewerName), list review hiện có
- **Sản phẩm liên quan**: Grid 4 sản phẩm cùng category/brand

### 3. Trang giỏ hàng (cart.jsp)
**URL**: `/cart`
**Data**: OrderCartEntity, List<ItemCartEntity> với ProductEntity + ProductVariantEntity join

Layout:
- Bảng danh sách item trong giỏ:
  - Cột: Ảnh (thumb), Tên SP, Variant (SKU), Đơn giá, Số lượng (input +/-), Thành tiền, Xóa (button)
  - Footer bảng: Tổng tiền, Tổng số lượng
- Button: "Tiếp tục mua sắm" (→ home), "Thanh toán" (→ checkout)
- Giỏ hàng trống: hiển thị message + link "Mua ngay"
- Cart data lưu ở database (ItemCartEntity), sync với session userId
- JS: cập nhật quantity inline (AJAX POST), xóa item (AJAX DELETE), realtime update tổng tiền

### 4. Trang thanh toán (checkout.jsp)
**URL**: `/checkout`
**Data**: Cart items, user info, voucher list

Layout:
- **Form thông tin giao hàng**:
  - Họ tên, email, số điện thoại (validate: không trống, sđt đúng format)
  - Địa chỉ (textarea, không trống)
  - Ghi chú (textarea, optional)
- **Review đơn hàng**:
  - List sản phẩm, số lượng, đơn giá, thành tiền
  - Tổng tiền
- **Chọn voucher** (nếu có):
  - Dropdown voucher còn hạn, còn quantity, thuộc userId
  - Hiển thị % giảm và số tiền giảm
  - Áp dụng → cập nhật tổng tiền
- **Phương thức thanh toán**:
  - Radio: COD (thanh toán khi nhận) | MoMo (ví điện tử)
  - Nếu MoMo: redirect tới payment URL (MoMo gateway)
- Button: "Đặt hàng" → POST OrderController
- Sau khi đặt hàng thành công → redirect `/order/success?id={orderId}`

### 5. Trang lịch sử đơn hàng (order-history.jsp)
**URL**: `/orders`
**Data**: List<OrderEntity> của userId hiện tại

Layout:
- Bảng danh sách đơn hàng:
  - Cột: Mã đơn, Ngày đặt, Sản phẩm, Tổng tiền, Trạng thái (badge color theo status), Hành động (xem chi tiết)
  - Status badges: PENDING (yellow), CONFIRMED (blue), SHIPPING (orange), COMPLETED (green), CANCELLED (red)
- Filter theo status (select dropdown)
- Pagination
- Click "Xem chi tiết" → `/order/detail?id={orderId}`
- Order detail page: thông tin đơn hàng + payment status + button hủy (nếu PENDING)

### 6. Trang liên hệ (contact.jsp)
**URL**: `/contact`
**Controller**: ContactController

Layout:
- 2 cột:
  - **Cột trái**: Form liên hệ
    - Họ tên (input, required)
    - Email (input, required, format validation)
    - Nội dung (textarea, required)
    - Button "Gửi liên hệ"
    - Server-side validation + hiển thị error message
  - **Cột phải**: Thông tin liên hệ (email, phone, address — placeholder)
- Success message: "Cảm ơn bạn đã liên hệ!"
- Form submit → POST ContactController → lưu database → redirect với success message

### 7. Trang đăng ký (register.jsp)
**URL**: `/register`
**Controller**: AuthController (action=register)

Layout:
- Form center card:
  - Tên tài khoản (input, required)
  - Tên đăng nhập (input, required, unique validation)
  - Email (input, required, format, unique)
  - Số điện thoại (input, required, format regex: `^[0-9]{9,11}$`)
  - Địa chỉ (input, required)
  - Mật khẩu (password, required, min 8 ký tự)
  - Nhập lại mật khẩu (password, required, phải khớp)
  - Button "Đăng ký"
- Client-side validation (JS): highlight field đỏ khi sai, show error message dưới field
- Server-side validation: trả về error message qua request attribute, forward lại form
- Link "Đã có tài khoản? Đăng nhập" → /login

### 8. Trang đăng nhập (login.jsp)
**URL**: `/login`
**Controller**: AuthController (action=login)

Layout:
- Form center card:
  - Email (input, required)
  - Mật khẩu (password, required)
  - Checkbox "Ghi nhớ đăng nhập"
  - Button "Đăng nhập"
- Error message: "Email hoặc mật khẩu không đúng" (không tiết lộ cụ thể bên nào sai)
- Link "Quên mật khẩu?" → /forgot-password
- Link "Chưa có tài khoản? Đăng ký" → /register

### 9. Trang quên mật khẩu (forgot-password.jsp)
**URL**: `/forgot-password`
**Controller**: AuthController (action=forgot-password)

Layout:
- Form: Email input → submit → gửi email verification code
- Form nhập code + mật khẩu mới:
  - Code (input 6 ký tự)
  - Mật khẩu mới (password)
  - Nhập lại mật khẩu mới
- Client-side: validate code không trống, mật khẩu mới >= 8 ký tự, khớp nhau

### 10. Trang xác thực email (verify-email.jsp)
**URL**: `/verify-email`
**Controller**: AuthController (action=verify-email)

Layout:
- Nhập code xác thực (6 ký tự) → submit
- Hoặc link từ email: `/verify-email?code={uuid}` → auto verify
- Success: "Xác thực thành công! Chuyển đến trang chủ..." (redirect sau 3s)
- Error: "Mã xác thực không đúng hoặc đã hết hạn"

### 11. Trang profile (profile.jsp)
**URL**: `/profile`
**Controller**: UserController

Layout:
- Form cập nhật thông tin:
  - Tên tài khoản (readonly)
  - Tên hiển thị (editable)
  - Email (readonly — đã verify)
  - Số điện thoại (editable)
  - Địa chỉ (editable)
  - Button "Cập nhật"
- Section đổi mật khẩu:
  - Mật khẩu hiện tại
  - Mật khẩu mới
  - Nhập lại mật khẩu mới
- Section quản lý session:
  - List active sessions (ip, createdAt)
  - Button "Đăng xuất tất cả thiết bị khác"

### 12. Trang wishlist (wishlist.jsp)
**URL**: `/wishlist`
**Controller**: SavedProductController

Layout:
- Grid sản phẩm đã lưu (tương tự product card ở home)
- Button: "Thêm vào giỏ" (mỗi item), "Xóa" (mỗi item), "Xóa tất cả"
- Empty state: "Chưa có sản phẩm yêu thích"

### 13. Trang giới thiệu (about.jsp)
**URL**: `/about`

Layout:
- Giới thiệu dự án LinhNamStore
- Bảng thông tin thành viên nhóm:
  - Họ tên | MSSV | Ngày sinh | Vai trò
- Ảnh/mockup sản phẩm

## Error Pages

### 404.jsp
- "Trang không tìm thấy"
- Button "Quay lại trang chủ"

### 500.jsp
- "Đã xảy ra lỗi hệ thống"
- Button "Thử lại"
- Không hiển thị stack trace (production safety)

## Admin Pages

### dashboard.jsp
- Stats cards: Tổng user, tổng đơn hàng, tổng doanh thu, tổng sản phẩm
- Chart placeholder (doanh thu 7 ngày gần nhất — có thể dùng CSS bar chart đơn giản)
- Bảng: Đơn hàng gần đây (5 đơn mới nhất)

### product-list.jsp / product-form.jsp
- Bảng CRUD sản phẩm: ID, tên, category, brand, status, actions (sửa/xóa)
- Form: tên, description, category (select), brandId (select), status (select)
- Variant sub-form: thêm/sửa/xóa variant ngay trong trang product

### brand-list.jsp / brand-form.jsp
- Bảng CRUD brand: ID, tên, userId, description, status
- Form: tên, description, status

### order-list.jsp / order-detail.jsp
- Bảng đơn hàng: ID, userId, productId, status, createdAt
- Filter + update status (select dropdown + submit)
- Detail: thông tin order + payment info

### user-list.jsp / user-form.jsp
- Bảng user: ID, name, email, role, status, createdAt
- Update role (ADMIN/USER), status (ACTIVE/INACTIVE/BANNED)

### voucher-list.jsp / voucher-form.jsp
- Bảng voucher: ID, percent, userId, expTime, quantity, createdAt
- Form: percent (number), userId, expTime (date input), quantity (number)

## CSS Architecture

### Variables (style.css)
```css
:root {
  --color-primary: #1a56db;
  --color-primary-dark: #1e40af;
  --color-secondary: #0f172a;
  --color-accent: #f59e0b;
  --color-success: #16a34a;
  --color-warning: #f59e0b;
  --color-danger: #dc2626;
  --color-bg: #f8fafc;
  --color-surface: #ffffff;
  --color-text: #1e293b;
  --color-text-muted: #64748b;
  --color-border: #e2e8f0;
  --font-sans: 'Segoe UI', system-ui, -apple-system, sans-serif;
  --font-mono: 'Consolas', 'Monaco', monospace;
  --radius-sm: 4px;
  --radius-md: 8px;
  --radius-lg: 12px;
  --shadow-sm: 0 1px 2px rgba(0,0,0,0.05);
  --shadow-md: 0 4px 6px rgba(0,0,0,0.07);
  --shadow-lg: 0 10px 15px rgba(0,0,0,0.1);
  --transition: all 0.2s ease;
  --max-width: 1280px;
}
```

### CSS Structure (trong style.css)
1. Reset + base (box-sizing, font, body)
2. Layout (header, top-menu, left-menu, content, footer grid)
3. Components (button, card, badge, form, table, modal, alert)
4. Pages (home, product-detail, cart, checkout, admin)
5. Utilities (text-center, mt-4, flex, gap-2, v.v.)
6. Media queries (mobile < 768px, tablet 768-1024px, desktop > 1024px)

## JS Architecture (app.js)

### Modules (IIFE pattern)
```js
// 1. Utils: formatCurrency, debounce, formatDate, validateEmail, validatePhone
// 2. Header: search toggle, mobile menu, user dropdown
// 3. Cart: localStorage sync, quantity update, remove item, total calc
// 4. Forms: client-side validation, password match check, star rating
// 5. Product: variant selector, image switcher, review form
// 6. Admin: bulk actions, confirm delete, status update
```

### Key functions
- `formatCurrency(amount)` → "1.234.000 ₫"
- `validateEmail(email)` → regex check
- `validatePhone(phone)` → `^[0-9]{9,11}$`
- `debounce(fn, delay)` → cho search input
- `toggleMobileMenu()` → hamburger toggle
- `switchVariant(variantId)` → đổi ảnh + giá + quantity display

## Seed Data Extension

Hiện tại sto.sql có 10 sản phẩm. Yêu cầu: tối thiểu 30 sản phẩm.
Cần INSERT thêm 20+ sản phẩm vào sto.sql:
- 10 STORAGE_DEVICE thêm (SSD, HDD, USB từ các brand hiện có)
- 5 NETWORK_DEVICE thêm (router, switch, access point)
- 5 ACCESSORY thêm (cáp, case, adapter)
- Mỗi sản phẩm có ít nhất 2 variants

## Implementation Order

### Phase 1: Foundation
1. `web/assets/css/style.css` — toàn bộ CSS variables, reset, layout, components
2. `web/assets/js/app.js` — utility functions, form validation, cart
3. `web/layouts/header.jsp` — banner + top menu + left menu
4. `web/layouts/footer.jsp` — footer
5. `web/layouts/main-layout.jsp` — wrapper template

### Phase 2: Public Pages
6. `web/pages/home.jsp` — trang chủ + product cards
7. `web/pages/product-detail.jsp` — chi tiết sản phẩm
8. `web/pages/contact.jsp` — form liên hệ
9. `web/pages/login.jsp` — đăng nhập
10. `web/pages/register.jsp` — đăng ký
11. `web/pages/forgot-password.jsp` — quên mật khẩu
12. `web/pages/verify-email.jsp` — xác thực email
13. `web/pages/about.jsp` — giới thiệu
14. `web/pages/error/404.jsp` + `500.jsp`

### Phase 3: User Pages
15. `web/pages/cart.jsp` — giỏ hàng
16. `web/pages/checkout.jsp` — thanh toán
17. `web/pages/order-history.jsp` — lịch sử đơn hàng
18. `web/pages/profile.jsp` — thông tin user
19. `web/pages/wishlist.jsp` — sản phẩm yêu thích

### Phase 4: Admin Pages
20. `web/admin/dashboard.jsp`
21. `web/admin/product-list.jsp` + `product-form.jsp`
22. `web/admin/brand-list.jsp` + `brand-form.jsp`
23. `web/admin/order-list.jsp` + `order-detail.jsp`
24. `web/admin/user-list.jsp` + `user-form.jsp`
25. `web/admin/voucher-list.jsp` + `voucher-form.jsp`

### Phase 5: Data
26. Extend `sto.sql` — thêm 20+ sản phẩm + variants

## Controller URL Mapping (để frontend reference đúng)

| Page | URL Pattern | Controller | Method |
|------|------------|------------|--------|
| Home | `/home` | ProductController | action=home |
| Product Detail | `/product?id={uuid}` | ProductController | action=detail |
| Cart | `/cart` | CartController | action=view |
| Cart Add | `/cart` (POST) | CartController | action=add |
| Cart Remove | `/cart` (POST) | CartController | action=remove |
| Checkout | `/checkout` | OrderController | action=checkout |
| Order Success | `/order/success?id={uuid}` | OrderController | action=success |
| Order History | `/orders` | OrderController | action=list |
| Order Detail | `/order/detail?id={uuid}` | OrderController | action=detail |
| Login | `/auth` (action=login) | AuthController | action=login |
| Register | `/auth` (action=register) | AuthController | action=register |
| Logout | `/auth` (action=logout) | AuthController | action=logout |
| Forgot Password | `/auth` (action=forgot-password) | AuthController | action=forgotPassword |
| Verify Email | `/auth` (action=verify-email) | AuthController | action=verifyEmail |
| Profile | `/profile` | UserController | action=profile |
| Contact | `/contact` | ContactController | action=form |
| Wishlist | `/wishlist` | SavedProductController | action=list |
| Admin Dashboard | `/admin/dashboard` | AdminController | action=dashboard |
| Admin Products | `/admin/products` | AdminController | action=products |
| Admin Orders | `/admin/orders` | AdminController | action=orders |
| Admin Users | `/admin/users` | AdminController | action=users |
| Admin Brands | `/admin/brands` | AdminController | action=brands |
| Admin Vouchers | `/admin/vouchers` | AdminController | action=vouchers |

## Security Frontend Considerations

- Form CSRF token: hidden input `<input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">`
- XSS prevention: dùng JSTL `<c:out>` khi render user input, không dùng `${var}` raw
- Password field: `type="password"`, autocomplete="new-password" cho register
- Sensitive pages (checkout, profile, admin): JSP check session attribute, nếu null → redirect login
- Error messages: không hiển thị stack trace, chỉ hiển thị message thân thiện

## Responsive Breakpoints

| Breakpoint | Width | Layout |
|-----------|-------|--------|
| Mobile | < 768px | 1 cột, left menu → off-canvas drawer, top menu → hamburger |
| Tablet | 768-1024px | 2 cột product grid, left menu collapse thành accordion |
| Desktop | > 1024px | 4 cột product grid, full sidebar + top menu |

## Verification Checklist

- [ ] Tất cả JSP compile được trên GlassFish 6.x
- [ ] CSS responsive trên 3 breakpoint
- [ ] Form validation client-side + server-side hoạt động
- [ ] Product card hiển thị đúng: ảnh, tên, mã, giá, badge
- [ ] Product detail: variant selector hoạt động, cập nhật giá real-time
- [ ] Cart: thêm/xóa/cập nhật số lượng hoạt động
- [ ] Checkout: form validation, voucher apply, payment method select
- [ ] Login/Register: validation, error display, redirect sau khi thành công
- [ ] Admin pages: CRUD table, form, status update hoạt động
- [ ] Footer: hiển thị đủ thông tin thành viên nhóm
- [ ] Tối thiểu 30 sản phẩm hiển thị trên trang chủ
- [ ] JSTL taglib hoạt động (forEach, if, out, formatNumber)
- [ ] Không có hardcoded secret/key trong JSP
- [ ] Tất cả link trong menu trỏ đúng URL pattern
