# Proposal: Refactor UI phong cách Apple Store VN

## Context

LinhNamStore hiện tại dùng design system Tailwind-like: xanh primary (#1a56db), card có border + shadow, gradient hero, sidebar filter, footer 3 cột dark. Đã có dark mode, responsive 3 breakpoints, JS đầy đủ.

Muốn refactor sang phong cách Apple Store VN: minimal, premium, nhiều whitespace, typography lớn nặng, ảnh lớn, card không border thấy rõ, button pill-shaped, section background xen kẽ.

Giữ nguyên: cấu trúc JSP, responsive breakpoints, JS logic, dark mode toggle, layout 4 phần (banner, top menu, left menu, content, footer). Chỉ refactor CSS + tweak HTML class trong JSP.

## Apple Store Design System — Đặc trưng

| Element | Apple Style | LinhNamStore Hiện tại | Thay đổi |
|---------|------------|-----------------|----------|
| Màu chủ đạo | Đen/trắng, accent xanh dương (#0071e3) | Xanh (#1a56db), slate | Đổi palette |
| Button | Pill-shaped (radius 999px), fill xanh hoặc đen | Radius 8px, fill xanh | Tăng border-radius |
| Card | Không border thấy rõ, shadow rất mờ, bg trắng | Border 1px + shadow | Ẩn border, giảm shadow |
| Typography | SF Pro, heading lớn bold, body nhẹ | Segoe UI, heading vừa | Tăng size + weight heading |
| Section | Background xen kẽ trắng/xám nhạt/đen | Đồng nhất bg | Thêm section bg alternation |
| Navigation | Slim, minimal, link xám → đen khi hover | Border top, bg xám, underline | Slim down, color shift |
| Hero | Full-width ảnh lớn, text overlay tối đa | Grid 2 cột, gradient | Full-width, ảnh lớn |
| Product card | Ảnh lớn trung tâm, tên ngắn gọn, giá dưới | Ảnh 4:3, nhiều info | Clean up, focus ảnh |
| Badge | Ribbon nhỏ góc card, màu nhẹ | Pill badge | Giữ pill, tweak màu |
| Whitespace | Padding lớn (40-80px section) | Padding vừa (18-28px) | Tăng spacing |
| Input | Rounded, border mờ, focus ring xanh | Border rõ, focus ring | Mờ border, focus ring đẹp hơn |
| Footer | Dark, nhiều cột nhỏ, divider mờ | Dark 3 cột | Expand column count |

## Scope thay đổi

### 1. Color Palette — `:root` variables

```css
:root {
    /* Apple-inspired */
    --color-primary: #0071e3;          /* Apple blue */
    --color-primary-hover: #0077ed;
    --color-primary-active: #0064c9;

    --color-black: #1d1d1f;            /* Apple near-black */
    --color-dark: #2d2d2f;
    --color-gray-100: #f5f5f7;         /* Apple light gray bg */
    --color-gray-200: #e8e8ed;         /* Apple border */
    --color-gray-300: #d2d2d7;         /* Apple muted border */
    --color-gray-400: #86868b;         /* Apple text muted */
    --color-gray-500: #6e6e73;         /* Apple text secondary */

    --color-bg: #ffffff;               /* Default white bg */
    --color-bg-alt: #f5f5f7;           /* Alternate section bg */
    --color-surface: #ffffff;
    --color-surface-soft: #f5f5f7;

    --color-text: #1d1d1f;
    --color-text-secondary: #6e6e73;
    --color-text-muted: #86868b;

    --color-accent: #f56300;           /* Apple orange (sale badge) */
    --color-success: #00873a;          /* Apple green */
    --color-warning: #f5a623;
    --color-danger: #e30000;           /* Apple red */

    --color-border: #e8e8ed;

    --font-sans: -apple-system, "SF Pro Display", "SF Pro Text",
                 "Helvetica Neue", "Segoe UI", system-ui, sans-serif;

    --radius-sm: 6px;
    --radius-md: 12px;
    --radius-lg: 20px;
    --radius-pill: 999px;              /* Pill buttons */

    --shadow-sm: 0 1px 3px rgba(0,0,0,0.04);
    --shadow-md: 0 4px 12px rgba(0,0,0,0.06);
    --shadow-lg: 0 12px 40px rgba(0,0,0,0.1);
    --shadow-float: 0 20px 60px rgba(0,0,0,0.12);  /* Apple-like float */

    --transition: all 0.3s cubic-bezier(0.25, 0.1, 0.25, 1);  /* Apple easing */
    --max-width: 1200px;               /* Apple max-width, narrower */
    --section-padding: 60px;           /* Apple section spacing */
}
```

**Dark mode điều chỉnh:**
```css
[data-theme="dark"] {
    --color-primary: #2997ff;
    --color-primary-hover: #40a3ff;
    --color-primary-active: #1480cc;

    --color-black: #f5f5f7;
    --color-dark: #e8e8ed;
    --color-bg: #000000;
    --color-bg-alt: #1d1d1f;
    --color-surface: #1d1d1f;
    --color-surface-soft: #111113;
    --color-text: #f5f5f7;
    --color-text-secondary: #a1a1a6;
    --color-text-muted: #86868b;
    --color-border: #333336;

    --shadow-sm: 0 1px 3px rgba(0,0,0,0.3);
    --shadow-md: 0 4px 12px rgba(0,0,0,0.4);
    --shadow-lg: 0 12px 40px rgba(0,0,0,0.5);
}
```

### 2. Button — Pill shape, Apple style

```css
.button {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    min-height: 44px;                  /* Apple touch target */
    border: none;
    border-radius: var(--radius-pill); /* Pill shape */
    padding: 12px 22px;
    background: var(--color-primary);
    color: #fff;
    font-size: 0.95rem;
    font-weight: 600;
    letter-spacing: -0.01em;
    cursor: pointer;
    transition: var(--transition);
}

.button:hover {
    background: var(--color-primary-hover);
    transform: scale(1.02);
}

.button:active {
    background: var(--color-primary-active);
    transform: scale(0.98);
}

.button.secondary {
    background: transparent;
    color: var(--color-primary);
    border: none;                      /* No visible border */
}

.button.secondary:hover {
    background: var(--color-primary);
    color: #fff;
}

/* Link-style button (Apple "Learn more >" pattern) */
.button.link-style {
    background: transparent;
    color: var(--color-primary);
    padding: 0;
    min-height: auto;
    font-weight: 500;
}
.button.link-style::after {
    content: " ›";
    font-size: 1.1em;
}

/* Full-width on mobile */
@media (max-width: 767px) {
    .button {
        width: 100%;
    }
}
```

### 3. Card — No visible border, soft shadow

```css
.card,
.panel,
.table-wrap,
.auth-card {
    background: var(--color-surface);
    border: none;                      /* No visible border */
    border-radius: var(--radius-lg);   /* Larger radius: 20px */
    box-shadow: var(--shadow-sm);      /* Very soft shadow */
}

.product-card {
    position: relative;
    overflow: hidden;
    background: var(--color-surface);
    border: none;
    border-radius: var(--radius-lg);
    box-shadow: var(--shadow-sm);
    transition: var(--transition);
}

.product-card:hover {
    box-shadow: var(--shadow-float);   /* Apple-like float on hover */
    transform: translateY(-4px) scale(1.01);
}
```

### 4. Typography — Large, bold headings

```css
.page-title {
    margin: 0 0 24px;
    font-size: clamp(2rem, 5vw, 3rem);
    font-weight: 700;
    letter-spacing: -0.02em;
    color: var(--color-black);
    line-height: 1.1;
}

.section-title h2 {
    margin: 0;
    font-size: clamp(1.5rem, 4vw, 2.2rem);
    font-weight: 700;
    letter-spacing: -0.015em;
    color: var(--color-black);
    line-height: 1.15;
}

.section-title p {
    margin: 8px 0 0;
    font-size: 1.1rem;
    color: var(--color-text-secondary);
}

/* Apple-style hero */
.hero {
    display: grid;
    place-items: center;
    min-height: 500px;                 /* Taller hero */
    margin-bottom: var(--section-padding);
    border-radius: var(--radius-lg);
    overflow: hidden;
    text-align: center;
    color: #fff;
    background: var(--color-black);
    position: relative;
}

.hero-copy {
    display: grid;
    align-content: center;
    padding: 60px 40px;
    max-width: 700px;
    margin: 0 auto;
}

.hero h1 {
    margin: 0 0 16px;
    font-size: clamp(2.5rem, 6vw, 4rem);
    font-weight: 700;
    letter-spacing: -0.03em;
    line-height: 1.05;
}

.hero p {
    font-size: clamp(1.1rem, 2.5vw, 1.4rem);
    color: rgba(255,255,255,0.85);
    margin: 0 0 24px;
}

.hero .button + .button {
    margin-left: 12px;
}
```

### 5. Navigation — Slim, minimal

```css
.site-header {
    position: sticky;
    top: 0;
    z-index: 30;
    background: rgba(255, 255, 255, 0.85);  /* Apple frosted effect */
    backdrop-filter: saturate(180%) blur(20px);
    -webkit-backdrop-filter: saturate(180%) blur(20px);
    border-bottom: 1px solid var(--color-border);
}

[data-theme="dark"] .site-header {
    background: rgba(0, 0, 0, 0.85);
}

.banner {
    display: grid;
    grid-template-columns: auto 1fr auto;
    gap: 16px;
    align-items: center;
    max-width: var(--max-width);
    margin: 0 auto;
    padding: 12px 20px;              /* Slimmer padding */
}

.brand-logo {
    font-weight: 700;                /* Lighter weight */
    font-size: 1.2rem;
    letter-spacing: -0.02em;
    color: var(--color-black);
}

.logo-mark {
    width: 34px;
    height: 34px;
    border-radius: var(--radius-md);
}

/* Top menu: Apple-style slim nav */
.top-menu {
    border-top: none;                  /* No top border */
    background: transparent;
}

.top-menu-inner {
    display: flex;
    gap: 28px;
    max-width: var(--max-width);
    margin: 0 auto;
    padding: 0 20px;
}

.top-menu a {
    position: relative;
    padding: 14px 0;
    color: var(--color-text-secondary);  /* Muted by default */
    font-weight: 500;
    font-size: 0.88rem;
    letter-spacing: -0.01em;
    transition: color 0.2s ease;
}

.top-menu a:hover,
.top-menu a.active {
    color: var(--color-text);          /* Dark on hover */
}

.top-menu a::after {
    display: none;                     /* Remove underline */
}
```

### 6. Product Grid — Clean, image-focused

```css
.product-grid {
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 20px;
    margin-bottom: var(--section-padding);
}

.product-media {
    aspect-ratio: 1 / 1;               /* Square images (Apple style) */
    background: var(--color-gray-100);
    display: grid;
    place-items: center;
    padding: 20px;
}

.product-media img {
    width: auto;
    height: auto;
    max-width: 85%;
    max-height: 85%;
    object-fit: contain;               /* Contain, not cover */
    transition: transform 0.3s cubic-bezier(0.25, 0.1, 0.25, 1);
}

.product-card:hover .product-media img {
    transform: scale(1.05);
}

.product-body {
    display: grid;
    gap: 6px;
    padding: 16px 20px 20px;
    text-align: center;               /* Center text (Apple style) */
}

.product-name {
    min-height: auto;
    margin: 0;
    font-size: 0.95rem;
    font-weight: 600;
    letter-spacing: -0.01em;
    color: var(--color-black);
}

.product-code {
    font-size: 0.82rem;
    color: var(--color-text-muted);
}

.price {
    color: var(--color-text);
    font-size: 1rem;
    font-weight: 600;
}
```

### 7. Section Background Alternation

```css
/* Sections with alternating backgrounds */
.section {
    padding: var(--section-padding) 0;
}

.section--alt {
    background: var(--color-bg-alt);
}

/* Full-width section banners (Apple "tile" pattern) */
.section-tile {
    max-width: var(--max-width);
    margin: 0 auto var(--section-padding);
    padding: 0 20px;
}

.section-tile .promo-banner {
    min-height: 400px;
    border-radius: var(--radius-lg);
    overflow: hidden;
    display: grid;
    place-items: center;
    text-align: center;
    padding: 60px 40px;
    color: #fff;
}

.promo-banner--dark {
    background: var(--color-black);
}

.promo-banner--blue {
    background: var(--color-primary);
}

.promo-banner--gradient {
    background: linear-gradient(135deg, #1d1d1f 0%, #0071e3 100%);
}
```

### 8. Input / Form — Apple style

```css
.field input,
.field select,
.field textarea {
    width: 100%;
    border: 1px solid var(--color-border);
    border-radius: var(--radius-md);   /* 12px radius */
    padding: 14px 16px;
    background: var(--color-surface);
    color: var(--color-text);
    font-size: 1rem;
    outline: none;
    transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.field input:focus,
.field select:focus,
.field textarea:focus {
    border-color: var(--color-primary);
    box-shadow: 0 0 0 4px rgba(0, 113, 227, 0.15);  /* Apple focus ring */
}

.field label {
    font-weight: 600;
    font-size: 0.88rem;
    color: var(--color-text-secondary);
    margin-bottom: 6px;
}
```

### 9. Badge — Refined pill

```css
.badge {
    display: inline-flex;
    align-items: center;
    border-radius: var(--radius-pill);
    padding: 4px 10px;
    font-size: 0.72rem;
    font-weight: 600;
    letter-spacing: 0.02em;
    text-transform: uppercase;
}

.badge--new {
    background: var(--color-primary);
    color: #fff;
}

.badge--sale {
    background: var(--color-accent);
    color: #fff;
}

.badge--out {
    background: var(--color-gray-200);
    color: var(--color-text-muted);
}
```

### 10. Header Actions — Icon only

```css
.header-actions {
    display: flex;
    align-items: center;
    gap: 8px;
}

.icon-button {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 40px;
    height: 40px;
    border: none;
    border-radius: 50%;                /* Circle buttons */
    background: transparent;
    color: var(--color-text-secondary);
    cursor: pointer;
    transition: var(--transition);
}

.icon-button:hover {
    background: var(--color-gray-100);
    color: var(--color-text);
}

.cart-badge {
    top: -2px;
    right: -2px;
    min-width: 18px;
    height: 18px;
    font-size: 0.65rem;
    line-height: 18px;
    border: 2px solid var(--color-surface);  /* White border */
}
```

### 11. Footer — Apple multi-column

```css
.site-footer {
    margin-top: 0;
    background: var(--color-bg-alt);
    border-top: 1px solid var(--color-border);
    color: var(--color-text-muted);
}

.footer-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);  /* 4 columns */
    gap: 32px;
    max-width: var(--max-width);
    margin: 0 auto;
    padding: 40px 20px;
    font-size: 0.82rem;
}

.footer-grid h3 {
    margin: 0 0 10px;
    font-size: 0.78rem;
    font-weight: 600;
    color: var(--color-text);
    text-transform: uppercase;
    letter-spacing: 0.04em;
}

.footer-bottom {
    border-top: 1px solid var(--color-border);
    padding: 16px 20px;
    text-align: center;
    font-size: 0.78rem;
}
```

### 12. Left Menu / Filter — Apple sidebar

```css
.left-menu {
    align-self: start;
    position: sticky;
    top: 90px;
    background: transparent;
    border: none;
    box-shadow: none;
    padding: 0;
}

.filter-section + .filter-section {
    margin-top: 24px;
    padding-top: 20px;
}

.filter-section h3 {
    margin: 0 0 12px;
    font-size: 0.82rem;
    font-weight: 700;
    text-transform: uppercase;
    letter-spacing: 0.04em;
    color: var(--color-text);
}

.filter-list label {
    padding: 8px 0;
    color: var(--color-text-secondary);
    font-size: 0.9rem;
}

.filter-list input[type="checkbox"],
.filter-list input[type="radio"] {
    accent-color: var(--color-primary);
}
```

### 13. JSP Tweaks

| File | Thay đổi | Lý do |
|------|----------|-------|
| `web/layouts/header.jsp` | Header actions: thay text button bằng icon SVG (cart, user). Thêm `aria-label`. Remove "Cart" text. Bỏ border top trên top-menu. | Apple icon-only header |
| `web/layouts/header.jsp` | Thêm class `section` hoặc `section--alt` wrapper quanh content sections trên home page | Background alternation |
| `web/views/home.jsp` | Hero: đổi từ grid 2 cột sang full-width centered. Promo tiles thêm class `promo-banner--dark`, `promo-banner--blue` | Apple hero style |
| `web/views/product/detail.jsp` | Gallery: ảnh contain thay vì cover. Info panel text center. Button pill | Apple product page |
| `web/views/cart/cart.jsp` | Table header styling. Button pill | Consistency |
| `web/layouts/footer.jsp` | Footer 4 cột, thêm sub-section links, font nhỏ hơn | Apple footer pattern |
| Tất cả page JSP | Product card: remove badge text dài → dùng class `badge--new`, `badge--sale`, `badge--out` | Badge consistency |

### 14. Animation / Transition refinements

```css
/* Apple-style page fade-in */
.content {
    animation: fadeIn 0.4s ease;
}

@keyframes fadeIn {
    from { opacity: 0; transform: translateY(8px); }
    to { opacity: 1; transform: translateY(0); }
}

/* Smooth scroll for anchor links */
html {
    scroll-behavior: smooth;
}

/* Button press effect */
.button:active {
    transform: scale(0.97);
}

/* Card hover lift */
.product-card:hover {
    transform: translateY(-4px);
}

/* Image zoom on hover */
.product-card:hover .product-media img {
    transform: scale(1.05);
}
```

## Implementation Order

1. **CSS `:root` variables** — đổi toàn bộ color palette, font, radius, shadow, transition
2. **CSS components** — button, card, badge, input, navigation, hero, product grid, footer, left menu
3. **CSS layout** — section backgrounds, promo banners, page fade-in
4. **CSS responsive** — cập nhật breakpoints cho style mới
5. **JSP header** — icon-only actions, remove nav border, update logo
6. **JSP home** — hero full-width, promo tiles, section alternation
7. **JSP footer** — 4-column layout
8. **JSP product pages** — image contain, center text, pill buttons
9. **JSP cart/admin** — button pill, table styling
10. **JS** — cập nhật theme toggle nếu cần (giữ nguyên logic)

## Ràng buộc

- Giữ nguyên cấu trúc thư mục JSP hiện tại
- Giữ nguyên responsive breakpoints (1024px, 767px, 480px)
- Giữ nguyên dark mode toggle logic
- Giữ nguyên JS functionality (validation, cart, tabs, variant selector)
- Không thêm external library/CSS framework
- Không đổi class name quá nhiều — ưu tiên override CSS hơn là đổi HTML
- JSP chỉ sửa khi bắt buộc (icon replacement, section wrapper)

## Verification

1. Chrome DevTools: so sánh side-by-side với apple.com/vn/store
2. Test 4 breakpoints: 360px, 480px, 768px, 1024px, 1280px
3. Dark mode toggle hoạt động, màu đúng
4. Button pill shape trên mọi page
5. Card hover float effect mượt
6. Navigation slim, link xám → đen on hover
7. Hero full-width, text center
8. Footer 4 cột
9. Deploy GlassFish, kiểm tra không lỗi CSS syntax
10. Font rendering: heading lớn bold, letter-spacing negative
