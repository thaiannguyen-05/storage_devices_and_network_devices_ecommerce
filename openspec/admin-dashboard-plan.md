# Admin Dashboard + CRUD Implementation Plan

## Context

LinhNamStore e-commerce hiện chưa có trang quản trị admin. Entity đã có field `role` (ADMIN/USER), seed data có admin user, nhưng không có endpoint nào cho admin. Plan này tạo full admin page: dashboard thống kê + CRUD cho users, products, orders, payments, vouchers, reviews.

**Mục tiêu**: Admin đăng nhập, vào `/admin`, xem dashboard thống kê, quản lý toàn bộ data trong database.

## Kiến trúc

- **AdminController**: Single servlet `@WebServlet("/admin")`, không có `@Public`, action parameter routing
- **AdminService**: Orchestrate dashboard stats, delegate tới existing services/repos
- **Admin DTOs**: Request/Response DTOs cho mỗi CRUD action
- **Admin Repositories**: Chỉ tạo mới `AdminStatsRepository` + `ProductReviewRepository`. Tái sử dụng toàn bộ repo hiện có
- **JSP Views**: Reuse `layout.jsp` + `layout-end.jsp` includes, scriptlets + expressions (không JSTL)

## Auth Strategy

- AdminController KHÔNG có `@Public` annotation
- `AuthPayloadFilter` chạy tự động trên non-`@Public` routes, set `authUserRole`
- Đầu `doGet`/`doPost`: check session `authUserRole` != "ADMIN" → redirect `/auth?action=signin`
- Fallback: request attribute `authUserRole` (cookie auth path)

## Schema Mismatch Handling

| Issue | Cách xử lý |
|-------|-----------|
| Order table thiếu columns (variantId, quantity, phone, address) trong sto.sql | Query wrapped try/catch. Fail → log warning → return empty list. UI hiện "schema migration pending" |
| ProductReview table không tồn tại | Follow pattern `ProductService.getProductReviews()`: try/catch, return empty list |
| Status inconsistency (Vietnamese vs English) | Admin dùng English status values duy nhất (PENDING/CONFIRMED/SHIPPING/COMPLETED/CANCELLED) |

## Dashboard Aggregate Queries (AdminStatsRepository)

| Stat | SQL |
|------|-----|
| Total users | `SELECT COUNT(*) FROM User` |
| Total products | `SELECT COUNT(*) FROM Product` |
| Total orders | `SELECT COUNT(*) FROM \`Order\`` |
| Pending orders | `SELECT COUNT(*) FROM \`Order\` WHERE status = 'PENDING'` |
| Total revenue | `SELECT COALESCE(SUM(amount),0) FROM Payment WHERE status = 'SUCCESS'` |
| Revenue last 7/30 days | `SELECT DATE(createdAt), SUM(amount) FROM Payment WHERE status='SUCCESS' AND createdAt >= DATE_SUB(NOW(), INTERVAL N DAY) GROUP BY DATE(createdAt)` |
| Top selling products | `SELECT productId, SUM(quantity) FROM \`Order\` GROUP BY productId ORDER BY SUM(quantity) DESC LIMIT 10` |
| Low stock variants | `SELECT pv.*, p.name FROM ProductVariant pv JOIN Product p ON pv.productId = p.id WHERE pv.quantity <= 5 AND pv.status = 'ACTIVE' ORDER BY pv.quantity ASC LIMIT 10` |
| User registration trend | `SELECT DATE(createdAt), COUNT(*) FROM User WHERE createdAt >= DATE_SUB(NOW(), INTERVAL 30 DAY) GROUP BY DATE(createdAt)` |
| Recent orders | `SELECT * FROM \`Order\` ORDER BY createdAt DESC LIMIT 10` |

Tất cả query defensive: mỗi query trong try/catch riêng. Fail → "N/A", không crash page.

## Pagination

- Tất cả list views: LIMIT/OFFSET, 20 items/page
- Page number từ `?page=1` param
- Tái sử dụng `UserRepository.findAll(page, pageSize)` pattern hiện có

## File Structure

```
src/java/module/bussiness/admin/
├── AdminController.java
├── AdminService.java
├── dto/
│   ├── AdminUserRequestDto.java
│   ├── AdminProductRequestDto.java
│   ├── AdminProductVariantRequestDto.java
│   ├── AdminOrderRequestDto.java
│   ├── AdminVoucherRequestDto.java
│   └── AdminDashboardStatsDto.java
├── response_dto/
│   ├── AdminUserResponseDto.java
│   ├── AdminProductResponseDto.java
│   ├── AdminOrderResponseDto.java
│   ├── AdminPaymentResponseDto.java
│   ├── AdminVoucherResponseDto.java
│   └── AdminDashboardResponseDto.java
└── repository/
    ├── interfaces/
    │   ├── IAdminStatsRepository.java
    │   └── IProductReviewRepository.java
    └── impl/
        ├── AdminStatsRepository.java
        └── ProductReviewRepository.java

web/views/admin/
├── dashboard.jsp
├── users.jsp
├── users-form.jsp
├── products.jsp
├── products-form.jsp
├── orders.jsp
├── order-detail.jsp
├── payments.jsp
├── vouchers.jsp
└── reviews.jsp

web/assets/css/admin.css
```

## AdminController Actions

### GET actions

| action | view | params |
|--------|------|--------|
| (default) | dashboard.jsp | — |
| users | users.jsp | page, search, roleFilter, statusFilter |
| users-edit | users-form.jsp | id |
| users-create | users-form.jsp | — |
| products | products.jsp | page, search, categoryFilter, statusFilter, brandFilter |
| products-edit | products-form.jsp | id |
| products-create | products-form.jsp | — |
| orders | orders.jsp | page, search, statusFilter |
| order-detail | order-detail.jsp | id |
| payments | payments.jsp | page, search, statusFilter |
| vouchers | vouchers.jsp | — |
| vouchers-edit | vouchers.jsp | id |
| vouchers-create | vouchers.jsp | — |
| reviews | reviews.jsp | page, search |

### POST actions

| action | behavior |
|--------|----------|
| users-save | Create/update user → redirect users list |
| users-delete | Delete user → redirect |
| users-activate | Set status=ACTIVE → redirect |
| users-ban | Set status=BANNED → redirect |
| products-save | Create/update product → redirect |
| products-delete | Delete product → redirect |
| products-change-status | Update status → redirect |
| products-variant-save | Create/update variant → redirect products-edit |
| products-variant-delete | Delete variant → redirect |
| orders-update-status | Update order status → redirect order-detail |
| orders-cancel | Set CANCELLED → redirect |
| payments-retry | Retry failed payment → redirect |
| vouchers-save | Create/update voucher → redirect |
| vouchers-delete | Delete voucher → redirect |
| reviews-delete | Delete review → redirect |
| reviews-moderate | Update review → redirect |

## DTO Specifications

### Request DTOs
- **AdminUserRequestDto**: name, email, role (ADMIN/USER), status (PENDING/ACTIVE/INACTIVE/BANNED), dateOfBirth
- **AdminProductRequestDto**: name, description, brandId, status (DRAFT/ACTIVE/INACTIVE/ARCHIVED), category
- **AdminProductVariantRequestDto**: price, imageUrl, sku, quantity, status (ACTIVE/INACTIVE/OUT_OF_STOCK)
- **AdminOrderRequestDto**: status (PENDING/CONFIRMED/SHIPPING/COMPLETED/CANCELLED)
- **AdminVoucherRequestDto**: percent, expTime, quantity, userId
- **AdminDashboardStatsDto**: POJO carrying all dashboard values

### Response DTOs
Thin wrappers thêm display-friendly fields: formatted dates, status labels, joined names (userName, productName).

## JSP View Details

### dashboard.jsp
- 4 stat cards (users, products, orders, revenue) grid layout
- 2 charts: revenue 7d, user registrations 30d (Chart.js CDN hoặc HTML bar chart đơn giản)
- Recent orders table (10 rows)
- Top products table (5 rows)
- Low stock alert table (variants qty <= 5)

### users.jsp
- Search bar (name, email), role filter, status filter
- Table: id, name, email, role, status, createdAt, actions (view, edit, activate/ban, delete)
- Pagination

### users-form.jsp
- Fields: name, email, role (select), status (select), dateOfBirth (date input)
- Edit mode: pre-fill values, show createdAt
- Create vs Edit dựa trên `id` presence

### products.jsp
- Search, category filter, status filter, brand filter dropdown
- Table: id, name, category, brand, status, variant count, total stock, actions
- Pagination

### products-form.jsp
- Product fields: name, description, brand (select), category (select), status (select)
- Variant section: list variants với edit/delete buttons
- Add variant inline form

### orders.jsp
- Search, status filter
- Table: id, userName, productName, quantity, total, status, createdAt, actions
- Pagination

### order-detail.jsp
- Order info + delivery info
- Status update form (select + submit)
- Cancel button (nếu không phải CANCELLED/COMPLETED)
- Payment info section

### payments.jsp
- Search, status filter
- Table: id, orderId, userId, amount, method, status, createdAt, actions (retry if FAILED)
- Pagination

### vouchers.jsp
- Table: id, percent, user, quantity, expTime, createdAt, actions
- Create/edit form trên cùng page

### reviews.jsp
- Search (reviewerName, productId)
- Table: id, productId, reviewerName, rating, comment, createdAt, actions
- Nếu table không tồn tại: hiện "Reviews table not yet created"

## Implementation Phases

### Phase 1: Infrastructure (1-2h)
1. `AdminStatsRepository` + `IAdminStatsRepository` — tất cả aggregate queries, defensive try/catch
2. `ProductReviewRepository` + `IProductReviewRepository` — defensive against missing table
3. `AdminDashboardStatsDto` + `AdminDashboardResponseDto`
4. `AdminService` với `getDashboardStats()`

### Phase 2: Dashboard (1h)
5. `AdminController` — auth guard + dashboard action
6. `dashboard.jsp` — stat cards, tables, charts
7. Test: deploy, `/admin`, verify stats, verify auth redirect

### Phase 3: Users CRUD (1-2h)
8. `AdminUserRequestDto` + `AdminUserResponseDto`
9. AdminService user CRUD methods (delegate `UserService` + `UserRepository`)
10. AdminController user actions
11. `users.jsp` + `users-form.jsp`
12. Test: list, search, filter, edit, delete, activate, ban

### Phase 4: Products CRUD (2-3h)
13. `AdminProductRequestDto` + `AdminProductResponseDto` + `AdminProductVariantRequestDto`
14. AdminService product admin methods (list filters, create, update, delete, status change)
15. Variant admin methods (create, update, delete)
16. AdminController product actions
17. `products.jsp` + `products-form.jsp`
18. Test: list, search, filter, create, edit, delete, variants, status

### Phase 5: Orders CRUD (1-2h)
19. `AdminOrderRequestDto` + `AdminOrderResponseDto`
20. AdminService order admin methods (list filters/pagination, findById, updateStatus, cancel)
21. AdminController order actions
22. `orders.jsp` + `order-detail.jsp`
23. Test: list, search, filter, view detail, update status, cancel

### Phase 6: Payments CRUD (1h)
24. `AdminPaymentResponseDto`
25. AdminService payment admin methods (list filters, findById, retry)
26. AdminController payment actions
27. `payments.jsp`
28. Test: list, search, filter, view, retry

### Phase 7: Vouchers CRUD (1h)
29. `AdminVoucherRequestDto` + `AdminVoucherResponseDto`
30. AdminService voucher admin methods (list, create, update, delete)
31. AdminController voucher actions
32. `vouchers.jsp`
33. Test: list, create, edit, delete

### Phase 8: Reviews CRUD (1h)
34. `ProductReviewRepository` hoàn chỉnh
35. AdminService review admin methods (list, delete, moderate)
36. AdminController review actions
37. `reviews.jsp`
38. Test: list, delete, moderate, graceful handling

### Phase 9: Polish (1-2h)
39. `admin.css` stylesheet
40. Admin nav items trong leftmenu.jsp
41. Cross-link giữa views
42. Pagination tất cả list views
43. Error/success messages
44. Final integration test

## Verification

### Build & Deploy
- [ ] `ant build` compile thành công
- [ ] Deploy GlassFish
- [ ] `/admin` → redirect signin (chưa login)
- [ ] Login USER → `/admin` → redirect signin
- [ ] Login ADMIN → `/admin` → dashboard hiện

### Dashboard
- [ ] 4 stat cards hiện số
- [ ] Revenue chart hiện data
- [ ] Recent orders table populated
- [ ] Top products populated
- [ ] Low stock alerts hiện

### Users CRUD
- [ ] List + pagination
- [ ] Search name/email
- [ ] Filter role/status
- [ ] Edit: pre-fill, save
- [ ] Activate/Ban: status change
- [ ] Delete: removed

### Products CRUD
- [ ] List + pagination
- [ ] Search + filter category/status/brand
- [ ] Create/Edit/Delete
- [ ] Add/Edit/Delete variant

### Orders CRUD
- [ ] List + pagination
- [ ] Search + filter status
- [ ] View detail
- [ ] Update status
- [ ] Cancel order

### Payments CRUD
- [ ] List + pagination
- [ ] Search + filter status
- [ ] Retry failed

### Vouchers CRUD
- [ ] List, Create, Edit, Delete

### Reviews CRUD
- [ ] Nếu table tồn tại: list, delete, moderate
- [ ] Nếu table không tồn tại: hiện message, không 500

## Key Design Decisions

1. **No BaseController**: HttpServlet trực tiếp với action routing — match existing pattern
2. **No DB migration**: Admin handle missing columns/tables gracefully
3. **Single AdminController**: Consistent project pattern
4. **Direct repo instantiation**: No DI, match existing pattern
5. **JSP includes layout.jsp**: Reuse existing wrapper
6. **English status values**: Avoid Vietnamese/English confusion
7. **Defensive stats**: Dashboard không crash, individual stat fail → "N/A"
8. **Pagination 20/page**: Phù hợp e-commerce admin
9. **No JSTL**: Scriptlets + expressions, match existing style

## Risks & Mitigations

| Risk | Mitigation |
|------|-----------|
| Order table missing columns | Try/catch per query. Log warning. Return empty list. |
| ProductReview table missing | Defensive pattern, return empty list |
| Large datasets | Add indexes separately. Admin queries dùng indexed columns |
| Concurrent status updates | No optimistic locking cho MVP. Add version column sau |
| XSS | Output escape, validate on save |
| Session fixation | Existing auth handles. Admin chỉ check role |

## Files to Modify (Existing)

- `web/views/includes/leftmenu.jsp` — thêm admin navigation link
- `web/views/includes/topmenu.jsp` — thêm admin link cho admin users

## Files to Create (New)

- `src/java/module/bussiness/admin/AdminController.java`
- `src/java/module/bussiness/admin/AdminService.java`
- `src/java/module/bussiness/admin/dto/AdminUserRequestDto.java`
- `src/java/module/bussiness/admin/dto/AdminProductRequestDto.java`
- `src/java/module/bussiness/admin/dto/AdminProductVariantRequestDto.java`
- `src/java/module/bussiness/admin/dto/AdminOrderRequestDto.java`
- `src/java/module/bussiness/admin/dto/AdminVoucherRequestDto.java`
- `src/java/module/bussiness/admin/dto/AdminDashboardStatsDto.java`
- `src/java/module/bussiness/admin/response_dto/AdminUserResponseDto.java`
- `src/java/module/bussiness/admin/response_dto/AdminProductResponseDto.java`
- `src/java/module/bussiness/admin/response_dto/AdminOrderResponseDto.java`
- `src/java/module/bussiness/admin/response_dto/AdminPaymentResponseDto.java`
- `src/java/module/bussiness/admin/response_dto/AdminVoucherResponseDto.java`
- `src/java/module/bussiness/admin/response_dto/AdminDashboardResponseDto.java`
- `src/java/module/bussiness/admin/repository/interfaces/IAdminStatsRepository.java`
- `src/java/module/bussiness/admin/repository/interfaces/IProductReviewRepository.java`
- `src/java/module/bussiness/admin/repository/impl/AdminStatsRepository.java`
- `src/java/module/bussiness/admin/repository/impl/ProductReviewRepository.java`
- `web/views/admin/dashboard.jsp`
- `web/views/admin/users.jsp`
- `web/views/admin/users-form.jsp`
- `web/views/admin/products.jsp`
- `web/views/admin/products-form.jsp`
- `web/views/admin/orders.jsp`
- `web/views/admin/order-detail.jsp`
- `web/views/admin/payments.jsp`
- `web/views/admin/vouchers.jsp`
- `web/views/admin/reviews.jsp`
- `web/assets/css/admin.css`

## Existing Files to Reference (Pattern)

- `AuthController.java` — servlet action routing pattern
- `ProductController.java` — doGet/doPost pattern
- `ProductService.java` — repo instantiation, DTO mapping
- `UserRepository.java` — pagination pattern
- `ProductRepository.java` — manual ResultSet mapping
- `views/auth/login.jsp` — JSP layout include pattern
- `views/includes/layout.jsp` — layout wrapper pattern
- `views/includes/leftmenu.jsp` — admin nav injection point
