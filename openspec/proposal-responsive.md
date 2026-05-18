# Proposal: Chuan Responsive

## Context

CSS (`web/assets/css/style.css`) đã có:
- CSS variables, reset, layout grid/flexbox
- 2 breakpoints: `max-width: 1024px` (tablet), `max-width: 767px` (mobile)
- Left menu → off-canvas drawer, hamburger toggle
- Product grid: 4→2→1 cột
- Hero, footer, form, product-detail: 1 cột trên mobile
- Auth card padding giảm trên mobile

JS (`web/assets/js/app.js`) đã có:
- `LinhNamStore` namespace: formatCurrency, debounce, validateEmail, validatePhone
- `initHeader()`: filter toggle, Escape key close, search debounce
- `initForms()`: required validation, email/phone check, password match
- `initProductDetail()`: variant selector, image switcher, tabs
- `initCart()`: quantity recalc, line total

## Vấn đề còn thiếu

| Vấn đề | Mức độ | Mô tả |
|--------|--------|-------|
| Không có overlay backdrop | Cao | Mở left menu trên mobile không có overlay mờ phía sau, tap ngoài không đóng |
| Touch targets < 44px | Cao | Button, link, checkbox label trên mobile khó tap (dưới 44px theo WCAG) |
| Table chỉ overflow-x | Trung bình | Table scroll ngang trên mobile nhưng khó đọc, không có data-label |
| Mobile nhỏ (< 480px) | Trung bình | Header bị truncate, cart badge overlap, brand logo quá to |
| Admin pages chưa responsive | Trung bình | Stats grid, table, form admin chưa có breakpoint riêng |
| Font heading không scale | Thấp | Page title, section title không dùng clamp(), quá to trên mobile |
| Pagination chưa responsive | Thấp | Pagination links không co lại trên mobile |
| Cart badge text chiếm chỗ | Thấp | "Cart" text trên mobile nhỏ tốn diện tích |

## Scope thay đổi

### 1. CSS — Bổ sung vào `web/assets/css/style.css`

#### a) Overlay backdrop (dưới 1024px)
```css
@media (max-width: 1024px) {
    .mobile-overlay {
        position: fixed;
        inset: 0;
        background: rgba(0, 0, 0, 0.35);
        z-index: 35;
        opacity: 0;
        pointer-events: none;
        transition: opacity 0.2s ease;
    }
    body.filter-open .mobile-overlay {
        opacity: 1;
        pointer-events: auto;
    }
}
```

#### b) Touch targets 44px (dưới 768px)
```css
@media (max-width: 767px) {
    .filter-list label {
        padding: 10px 0;
        min-height: 44px;
        align-items: center;
    }
    .top-menu a {
        min-height: 44px;
        display: inline-flex;
        align-items: center;
    }
    .button {
        min-height: 44px;
    }
    .field select,
    .field input,
    .field textarea {
        min-height: 44px;
    }
    .icon-button {
        min-width: 44px;
        min-height: 44px;
    }
    .table-actions .button {
        min-height: 38px;
        padding: 6px 10px;
    }
}
```

#### c) Table card layout (dưới 768px)
```css
@media (max-width: 767px) {
    .table-wrap {
        overflow: visible;
    }
    .table-wrap table,
    .table-wrap thead,
    .table-wrap tbody,
    .table-wrap th,
    .table-wrap td,
    .table-wrap tr {
        display: block;
    }
    .table-wrap thead tr {
        position: absolute;
        top: -9999px;
        left: -9999px;
    }
    .table-wrap tr {
        border: 1px solid var(--color-border);
        border-radius: var(--radius-md);
        padding: 12px;
        margin-bottom: 12px;
        background: var(--color-surface);
    }
    .table-wrap td {
        border: none;
        padding: 6px 0 6px 40%;
        position: relative;
    }
    .table-wrap td::before {
        content: attr(data-label);
        position: absolute;
        left: 0;
        top: 6px;
        font-weight: 700;
        color: var(--color-text-muted);
        font-size: 0.85rem;
    }
    .table-wrap tr:last-child td {
        border-bottom: none;
    }
}
```

#### d) Mobile nhỏ refinement (dưới 480px)
```css
@media (max-width: 480px) {
    .banner {
        grid-template-columns: auto 1fr;
        gap: 10px;
        padding: 10px 14px;
    }
    .brand-logo {
        font-size: 1.1rem;
    }
    .logo-mark {
        width: 32px;
        height: 32px;
        font-size: 0.85rem;
    }
    .search-form input {
        padding: 8px 10px;
    }
    .search-form .button {
        padding: 8px 10px;
        min-height: 38px;
    }
    .cart-link .icon-button {
        min-width: 38px;
    }
    .header-actions .button {
        font-size: 0.82rem;
        padding: 8px 10px;
    }
    .page-title {
        font-size: clamp(1.3rem, 5vw, 1.75rem);
    }
    .section-title h2 {
        font-size: clamp(1.05rem, 4vw, 1.35rem);
    }
    .hero-copy {
        padding: 20px;
    }
    .hero h1 {
        font-size: clamp(1.6rem, 6vw, 2.5rem);
    }
    .product-grid {
        gap: 12px;
    }
    .product-body {
        padding: 10px;
    }
    .auth-card {
        padding: 14px;
    }
    .form-grid {
        gap: 10px;
    }
}
```

#### e) Admin pages responsive (dưới 768px)
```css
@media (max-width: 767px) {
    .toolbar {
        flex-direction: column;
        align-items: stretch;
    }
    .toolbar .button {
        width: 100%;
        justify-content: center;
    }
    .bar-chart {
        min-height: 120px;
        gap: 6px;
        padding: 10px;
    }
    .stat-card strong {
        font-size: 1.4rem;
    }
    .stat-card {
        padding: 14px;
    }
}
```

#### f) Font scale clamp cho heading
```css
.page-title {
    font-size: clamp(1.4rem, 4vw, 1.75rem);
}
.section-title h2 {
    font-size: clamp(1.1rem, 3vw, 1.35rem);
}
.hero h1 {
    font-size: clamp(1.8rem, 5vw, 3.4rem);
}
```

#### g) Cart button text ẩn trên mobile nhỏ
```css
@media (max-width: 480px) {
    .cart-link .icon-button {
        font-size: 0;
    }
    .cart-badge {
        top: -4px;
        right: -4px;
        min-width: 18px;
        height: 18px;
        line-height: 18px;
        font-size: 0.68rem;
    }
}
```

### 2. JS — Bổ sung vào `web/assets/js/app.js`

#### a) Overlay close — tap ngoài sidebar đóng menu
Thêm vào `initHeader()`:
```js
document.addEventListener("click", (event) => {
    const sidebar = document.querySelector(".left-menu");
    if (sidebar && !sidebar.contains(event.target)
        && !event.target.hasAttribute("data-filter-toggle")
        && document.body.classList.contains("filter-open")) {
        document.body.classList.remove("filter-open");
    }
});
```

### 3. JSP Updates — Thêm `data-label` cho table cells

Mỗi `<td>` trong table cần `data-label="Tên cột"` để CSS card layout hiển thị label.

**File cần sửa:**
| File | Cột cần data-label |
|------|-------------------|
| `web/views/cart/cart.jsp` | Ảnh, Tên, Variant, Giá, Số lượng, Thành tiền, Xóa |
| `web/views/order/history.jsp` | Mã đơn, Ngày, Sản phẩm, Tổng, Trạng thái, Hành động |
| `web/views/order/detail.jsp` | Tên, Variant, Số lượng, Đơn giá, Thành tiền |
| `web/views/admin/users/list.jsp` | ID, Tên, Email, Role, Status, Ngày tạo, Hành động |
| `web/views/admin/products/list.jsp` | ID, Tên, Category, Brand, Status, Hành động |
| `web/views/admin/orders/list.jsp` | ID, User, Sản phẩm, Tổng, Status, Ngày, Hành động |
| `web/views/admin/brands/list.jsp` | ID, Tên, Description, Status, Hành động |
| `web/views/admin/vouchers/list.jsp` | ID, %, User, Hạn, SL, Ngày tạo, Hành động |

**Ví dụ:**
```html
<!-- Trước -->
<td>${product.name}</td>
<td>${LinhNamStore.formatCurrency(variant.price)}</td>

<!-- Sau -->
<td data-label="Tên">${product.name}</td>
<td data-label="Giá">${LinhNamStore.formatCurrency(variant.price)}</td>
```

### 4. Header JSP — Thêm overlay div

`web/layouts/header.jsp`: sau `</nav>` thêm:
```html
<div class="mobile-overlay"></div>
```

## Thứ tự triển khai

1. **CSS enhancements** — bổ sung 7 mục (a→g) vào `style.css`
2. **JS overlay close** — thêm vào `app.js`
3. **Header overlay** — thêm div vào `header.jsp`
4. **Table data-labels** — sửa 8 file JSP table

## Verification

1. Chrome DevTools Device Mode: test 360px (iPhone SE), 480px, 768px (iPad), 1024px, 1280px
2. Tap hamburger → sidebar mở + overlay hiện → tap overlay → đóng
3. Table trên mobile → card layout với data-label hiển thị đúng
4. Tất cả button/link trên mobile >= 44px touch target
5. Cart badge trên mobile nhỏ (< 480px) → ẩn text "Cart", chỉ hiện badge số
6. Admin stats grid responsive đúng
7. Heading font scale mượt theo viewport width
