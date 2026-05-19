# Admin Module Refactor — Sepay SaaS Fintech Style UI

## Context

Admin dashboard hiện tại (`/admin/dashboard`) dùng design cũ: dark gradient sidebar (#0f172a → #172554), border-radius 24px quá lớn, gradient stat cards, shadow nặng. Không phù hợp trang quản trị e-commerce. Yêu cầu: redesign toàn bộ admin page theo phong cách Sepay (my.sepay.vn) — SaaS fintech, sidebar sáng, card sạch, mật độ data cao, icon navigation, compact, chuyên nghiệp.

## Goal

Refactor giao diện admin module theo style Sepay: light sidebar, crisp cards (border-radius nhỏ), data-dense tables, icon-based navigation, topbar với user info/notification. Backend (analytics repository, service, DTO, controller) giữ nguyên — chỉ thay frontend + CSS admin.

## Sepay Design Pattern — Áp dụng cho Admin

### Layout tổng thể
```
┌──────────────────────────────────────────────────┐
│  TOPBAR: Logo | Search | 🔔 | User dropdown     │  ← white, border-bottom subtle
├─────────┬────────────────────────────────────────┤
│ SIDEBAR │  CONTENT AREA                          │
│  light  │  ┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐     │  ← #F8FAFC background
│  icon   │  │Card1│ │Card2│ │Card3│ │Card4│     │  ← stat cards white, thin border
│  +text  │  └─────┘ └─────┘ └─────┘ └─────┘     │
│  nav    │                                        │
│         │  ┌──────────────────────────────────┐  │
│         │  │  Revenue Chart / Recent Orders   │  │  ← clean white panels
│         │  │  Data table, compact rows        │  │
│         │  └──────────────────────────────────┘  │
└─────────┴────────────────────────────────────────┘
```

### Design tokens mới (admin only)
| Property | Hiện tại | Sepay style |
|---|---|---|
| Sidebar bg | `linear-gradient(#0f172a, #172554)` | `#FFFFFF` border-right `1px #E2E8F0` |
| Sidebar text | `#dbeafe` | `#1E293B` active `#3B82F6` |
| Card radius | `24px` | `12px` |
| Card bg | `linear-gradient(135deg, #fff, #e0f2fe)` | `#FFFFFF` border `1px #E5E7EB` |
| Card shadow | `0 12px 30px rgba(0,0,0,0.07)` | `0 1px 3px rgba(0,0,0,0.06)` |
| Page bg | `#f3f6fb` | `#F8FAFC` |
| Stat number | `1.9rem` | `1.5rem` font-weight `700` |
| Table row padding | default | `12px 16px` compact |
| Icon | Không có | SVG icons trong sidebar + stat cards |

### Sidebar navigation (Sepay style)
```
┌─ LN Admin ──────────────────┐
│                             │
│ 📊 Dashboard                │ ← active: bg #EFF6FF, color #3B82F6
│ 📦 Đơn hàng          (24)   │ ← badge count
│ 👥 Người dùng        (128)  │
│ 🛍️ Sản phẩm          (350)  │
│ 🏷️ Brand             (12)   │
│ 🎫 Voucher            (5)   │
│ ⚙️ Cài đặt                  │
│                             │
│─────────────────────────────│
│ 🚪 Đăng xuất                │
└─────────────────────────────┘
```
- Mỗi mục: SVG icon 20px + text 14px + optional badge count
- Active state: background `#EFF6FF`, text `#3B82F6`, left border `3px solid #3B82F6`
- Hover: background `#F1F5F9`
- Compact: padding 10px 16px (thay vì 12px 14px hiện tại)

### Topbar (Sepay style)
```
┌─────────────────────────────────────────────────────────────┐
│  📊 Dashboard    🔍 search...       🔔(3)   👤 Admin Name ▼│
└─────────────────────────────────────────────────────────────┘
```
- White background, border-bottom `1px #E5E7EB`
- Height: 64px
- Page title left, user info right
- Notification badge (số lượng đơn PENDING)
- User dropdown: Profile, Logout

### Stat Cards (Sepay style)
```
┌─────────────────┐
│ 👥  Tổng users  │  ← icon left, label top, number big
│  128            │
│  ↑ +12 this week│  ← small trend indicator
└─────────────────┘
```
- White card, border `1px #E5E7EB`, radius `12px`, subtle shadow
- SVG icon 24px, colored: users=blue, orders=orange, revenue=green, products=purple
- Number: `1.5rem` bold
- Trend line nhỏ: "+X" màu xanh hoặc "-X" màu đỏ

### Data Table (Sepay style)
- Compact row: height `48px`, padding `12px 16px`
- Zebra striping hoặc hover highlight `#F9FAFB`
- Status badge: `10px 12px`, radius `6px`, font `12px`
  - PENDING: `#FEF3C7` text `#92400E`
  - CONFIRMED: `#DBEAFE` text `#1E40AF`
  - DELIVERED/COMPLETED: `#D1FAE5` text `#065F46`
  - CANCELLED: `#FEE2E2` text `#991B1B`
- Action column: icon buttons (eye/edit/trash) thay vì text links
- Header: sticky top, uppercase, font `11px`, color `#6B7280`

## Part 1: CSS Refactor — Admin Design System

### 1.1 Replace CSS variables cho admin
**File:** `web/assets/css/style.css` — thay thế section admin (line ~1076–1394)

New admin CSS variables (scoped, không ảnh hưởng public site):
```css
:root {
    --admin-sidebar-bg: #FFFFFF;
    --admin-sidebar-text: #1E293B;
    --admin-sidebar-active: #3B82F6;
    --admin-sidebar-active-bg: #EFF6FF;
    --admin-sidebar-hover: #F1F5F9;
    --admin-sidebar-width: 260px;
    --admin-content-bg: #F8FAFC;
    --admin-card-bg: #FFFFFF;
    --admin-card-border: #E5E7EB;
    --admin-card-radius: 12px;
    --admin-card-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
    --admin-topbar-height: 64px;
    --admin-table-row-height: 48px;
    --admin-text-primary: #1E293B;
    --admin-text-secondary: #64748B;
    --admin-text-muted: #94A3B8;
}
```

### 1.2 New sidebar CSS
- Background: `#FFFFFF` (thay gradient dark)
- Border-right: `1px solid #E5E7EB`
- Nav items: SVG icon + text inline-flex, gap 10px
- Active: bg `#EFF6FF`, color `#3B82F6`, border-left `3px solid #3B82F6`
- Hover: bg `#F1F5F9`
- Brand: compact 56px height, logo 36px
- Footer: border-top, small logout button

### 1.3 New topbar CSS
- White bg, border-bottom `1px solid #E5E7EB`
- Height 64px, flex row, space-between
- Page title `1.25rem` font-weight `600`
- User avatar circle 36px + name + dropdown chevron
- Notification bell with count badge

### 1.4 New stat cards CSS
- White bg, border `1px solid #E5E7EB`, radius `12px`, shadow `0 1px 3px`
- Icon container: 44x44px, rounded `12px`, colored bg
  - Users: bg `#EFF6FF`, icon `#3B82F6`
  - Orders: bg `#FFF7ED`, icon `#F97316`
  - Revenue: bg `#F0FDF4`, icon `#22C55E`
  - Products: bg `#F5F3FF`, icon `#8B5CF6`
- Number `1.5rem` bold
- Trend text `0.8rem` colored green/red

### 1.5 New table CSS
- Header: sticky, uppercase `11px`, color `#6B7280`, border-bottom
- Row: height 48px, hover `#F9FAFB`
- Cell: padding `12px 16px`, font `14px`
- Action buttons: 32px icon buttons, hover bg
- Status badges: compact `10px 12px`, radius `6px`

### 1.6 New chart CSS
- Clean bars, no gradient
- Color `#3B82F6` solid
- Grid lines: `#E5E7EB`
- Labels: `12px`, color `#64748B`

### 1.7 Responsive
- Mobile: sidebar overlay với backdrop blur
- Hamburger button trong topbar
- Stat cards: 1 column
- Table: card layout (giống hiện tại)

## Part 2: Layout JSP Refactor

### 2.1 Refactor admin-layout.jsp
**File:** `web/layouts/admin-layout.jsp`

Thay đổi chính:
- Sidebar: white bg, SVG icons inline, badge counts
- Topbar: page title + notification bell + user dropdown
- Remove dark theme references
- Add `admin-body` class cho CSS scope

Sidebar structure mới:
```jsp
<aside class="admin-sidebar">
    <div class="admin-brand">
        <div class="admin-brand-mark">LN</div>
        <div><strong>LinhNamStore</strong><small>Admin</small></div>
    </div>
    <nav class="admin-nav">
        <a href="/admin/dashboard" class="${activePage == 'admin-dashboard' ? 'active' : ''}">
            <svg><!-- dashboard icon --></svg>
            <span>Dashboard</span>
        </a>
        <a href="/admin/orders?action=list" class="...">
            <svg><!-- package icon --></svg>
            <span>Đơn hàng</span>
            <span class="admin-nav-badge">${orderPendingCount}</span>
        </a>
        ...
    </nav>
    <div class="admin-sidebar-footer">
        <form action="/auth?action=logout" method="POST">
            <button type="submit" class="admin-logout-btn">
                <svg><!-- logout icon --></svg>
                <span>Đăng xuất</span>
            </button>
        </form>
    </div>
</aside>
```

Topbar structure mới:
```jsp
<header class="admin-topbar">
    <div class="admin-topbar-left">
        <button class="admin-menu-toggle" id="menuToggle">
            <svg><!-- hamburger icon --></svg>
        </button>
        <h1 class="admin-page-title">${pageTitle}</h1>
    </div>
    <div class="admin-topbar-right">
        <button class="admin-notification-btn">
            <svg><!-- bell icon --></svg>
            <span class="admin-notification-badge">${pendingOrdersCount}</span>
        </button>
        <div class="admin-user-dropdown">
            <button>
                <div class="admin-avatar">${userInitial}</div>
                <span>${userName}</span>
                <svg><!-- chevron --></svg>
            </button>
            <div class="admin-dropdown-menu">
                <a href="/profile">Tài khoản</a>
                <form action="/auth?action=logout" method="POST">
                    <button type="submit">Đăng xuất</button>
                </form>
            </div>
        </div>
    </div>
</header>
```

## Part 3: Page Refactor

### 3.1 Dashboard Page
**File:** `web/views/admin/dashboard.jsp`

Layout:
- 4 stat cards row (users, orders, revenue, products) — mỗi card có icon SVG + trend
- 2-column: Revenue chart (7 ngày) | Recent Orders table
- Top Products table

Stat card example:
```jsp
<div class="admin-stat-card">
    <div class="admin-stat-icon" style="bg:#EFF6FF; color:#3B82F6">
        <svg><!-- users icon --></svg>
    </div>
    <div class="admin-stat-body">
        <span class="admin-stat-label">Tổng người dùng</span>
        <strong class="admin-stat-value">${stats.totalUsers}</strong>
        <span class="admin-stat-trend up">↑ +${userGrowth} tuần này</span>
    </div>
</div>
```

### 3.2 Order List Page
**File:** `web/views/admin/orders/list.jsp`

- Filter bar: compact, inline-flex, border-radius `8px`
- Table: compact rows, icon action buttons
- Pagination: `8px` border-radius buttons

### 3.3 Order Detail Page
**File:** `web/views/admin/orders/detail.jsp`

- 2-column: order info (left) | status update form (right)
- Status timeline với colored dots
- Product list table compact

### 3.4 User List/Edit Pages
**Files:** `web/views/admin/users/list.jsp`, `web/views/admin/users/edit.jsp`

- Same compact table style
- Role badge: `ADMIN` (purple bg), `USER` (blue bg)
- Status badge: `ACTIVE` (green), `BANNED` (red), `PENDING` (yellow)

### 3.5 Product/Brand/Voucher Pages
**Files:** Existing admin pages (refactor CSS class names)

- Không cần thay đổi structure nhiều — chỉ đổi CSS classes sang style mới

## Part 4: SVG Icon System

### 4.1 Inline SVG icons
Không thêm thư viện — dùng inline SVG trong JSP layout. Icons cần:
- Dashboard (grid/bars)
- Package/Box (đơn hàng)
- Users (người dùng)
- Shopping bag (sản phẩm)
- Tag/Label (brand)
- Ticket (voucher)
- Bell (notification)
- User (profile)
- Logout
- Search
- Eye/Detail
- Edit/Pencil
- Trash/Delete
- Chevron down
- Hamburger menu
- Trend up/down arrows

Mỗi icon: 20x20px, `currentColor`, stroke `1.5`, fill none

## Part 5: Backend — Minor Updates

### 5.1 AdminAnalyticsService — thêm data cho UI
Thêm methods:
- `getPendingOrderCount()` — số đơn PENDING (cho notification badge)
- `getUserGrowthThisWeek()` — trend số cho stat card
- `getStatsByCategory()` — data cho dashboard breakdown

### 5.2 AdminDashboardController — thêm attributes
Set thêm:
- `orderPendingCount` → notification badge
- `userInitial` → avatar letter
- `userName` → topbar display

## Implementation Order

1. **CSS refactor** — thay toàn bộ admin CSS section trong `style.css`
2. **SVG icon system** — tạo các inline SVG cho sidebar, stat cards, tables
3. **Refactor admin-layout.jsp** — sidebar light, topbar mới, notification
4. **Refactor dashboard.jsp** — stat cards với icon + trend
5. **Refactor order list/detail** — compact table, icon actions
6. **Refactor user list/edit** — badge colors, compact rows
7. **Refactor product/brand/voucher pages** — CSS class update
8. **Backend minor** — thêm pending count, trend data

## Files to Modify

| File | Change |
|---|---|
| `web/assets/css/style.css` | Replace admin CSS (~320 lines) — Sepay design tokens |
| `web/layouts/admin-layout.jsp` | White sidebar, SVG icons, topbar mới |
| `web/views/admin/dashboard.jsp` | Stat cards với icon + trend |
| `web/views/admin/orders/list.jsp` | Compact table, icon actions |
| `web/views/admin/orders/detail.jsp` | 2-column layout, status timeline |
| `web/views/admin/users/list.jsp` | Compact table, role/status badges |
| `web/views/admin/users/edit.jsp` | Form styling mới |
| `web/views/admin/products/list.jsp` | CSS class update |
| `web/views/admin/brands/list.jsp` | CSS class update |
| `web/views/admin/vouchers/list.jsp` | CSS class update |
| `module/core/page/AdminDashboardController.java` | Thêm notification count, user initial |
| `module/core/admin/AdminAnalyticsService.java` | Thêm getPendingOrderCount, getUserGrowthThisWeek |

## Design Comparison

### Trước (dark admin)
- Sidebar: dark gradient `#0f172a → #172554`
- Cards: `border-radius: 24px`, gradient `#fff → #e0f2fe`, shadow nặng
- Font stat: `1.9rem` quá lớn
- Không có icon
- Không có notification badge
- Data density thấp

### Sau (Sepay style)
- Sidebar: white `#FFFFFF`, border `#E5E7EB`, SVG icons
- Cards: `border-radius: 12px`, white `#FFFFFF`, border `1px #E5E7EB`, shadow nhẹ
- Font stat: `1.5rem` gọn gàng
- Icon system xuyên suốt
- Notification badge trên sidebar + topbar
- Data density cao, compact rows
- Professional fintech SaaS look

## Verification

1. Deploy, login ADMIN → `/admin/dashboard`
2. Sidebar: trắng, có icon SVG, active state xanh `#3B82F6`
3. Stat cards: 4 card, icon màu riêng biệt, số liệu real
4. Topbar: title trái, notification + user phải
5. Table: compact rows, hover highlight, icon action buttons
6. Badge status: đúng màu (pending=yellow, confirmed=blue, delivered=green, cancelled=red)
7. Mobile: hamburger menu, sidebar overlay
8. Console không error, network không 500
