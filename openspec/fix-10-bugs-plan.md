# Planning: Fix 10 Bugs — StoreIT Ecommerce

## Context
10 bugs reported from UI testing. Covers auth flow, cart, orders, categories, reviews, contact, and footer. All in Jakarta EE Servlet app with PostgreSQL.

---

## Bug 1: Quên mật khẩu — gửi email nhưng không có chỗ nhập code

**Root cause**: `AuthController.handleForgotPassword()` line 227 forwards back to `forgot-password.jsp` on success. That page only has email input, no code field. User receives code via email but nowhere to enter it.

**Fix**: After success, forward to `reset-password.jsp` which already has email+code+newPassword form.

**File**: `src/java/module/core/auth/AuthController.java:227`
- Change `/views/auth/forgot-password.jsp` → `/views/auth/reset-password.jsp`

**Note**: `verify-email.jsp` already has code input field. Registration flow works correctly.

---

## Bug 2: Thêm vào giỏ hàng — không thông báo

**Root cause**: `web/views/product/list.jsp` — AJAX error branch (line ~411) calls `showCartToast()` (success toast) even when the server returns error. Should show red error toast.

**Fix**:
1. Add `showCartErrorToast(message)` function in `list.jsp`
2. Replace `showCartToast()` in `catch` and `else` branches with `showCartErrorToast(message)`
3. Parse JSON response: `data.message` for error text

**File**: `web/views/product/list.jsp` (JS section ~lines 372-415)

---

## Bug 3: Đặt hàng — "Failed to update delivery info"

**Root cause**: `OrderRepository.updateDeliveryInfo()` line 91 wraps ALL exceptions in generic RuntimeException. No logging, no row count check. When 0 rows affected (no matching Order row), silent failure → upstream code assumes success.

**Fix**:
1. Catch `SQLException` specifically
2. Log userId, productId, variantId, status on failure
3. Check `executeUpdate()` return count, log warning if 0 rows
4. Return boolean or throw descriptive exception

**File**: `src/java/module/bussiness/order/repository/impl/OrderRepository.java:78-93`

---

## Bug 4: Danh mục — ấn vào như không ấn

**Root cause**: Subcategory links in `leftmenu.jsp` line ~87 use `?subcategory=HDD` without `category` param. `ProductController.filterByCategory()` compares "HDD" against "STORAGE_DEVICE" — never matches.

**Fix**:
1. `leftmenu.jsp`: Add `category` param to subcategory links:
   - HDD → `?category=STORAGE_DEVICE&subcategory=HDD`
   - SSD → `?category=STORAGE_DEVICE&subcategory=SSD`
   - NAS → `?category=NETWORK_DEVICE&subcategory=NAS`
   - Router → `?category=NETWORK_DEVICE&subcategory=ROUTER`
   - Switch → `?category=NETWORK_DEVICE&subcategory=SWITCH`
   - Cable → `?category=ACCESSORY&subcategory=CABLE`
   - Flash → `?category=ACCESSORY&subcategory=FLASH_DRIVE`
   - Card → `?category=ACCESSORY&subcategory=MEMORY_CARD`

**File**: `web/views/includes/leftmenu.jsp`

---

## Bug 5: Failed to create product review

**Root cause**: `ProductService.createReview()` — no productId validation, comment could be null (DB has NOT NULL), no try-catch.

**Fix**:
1. Validate productId non-blank
2. Default comment to `""` if null
3. Catch `SQLException` with logging
4. Return boolean instead of throwing RuntimeException

**File**: `src/java/module/bussiness/product/ProductService.java:356-368`

---

## Bug 6: Thêm giỏ hàng — Duplicate entry constraint violation

**Root cause**: TOCTOU race in `CartController.addItem()`: `findByCartIdAndProductAndVariant()` → if null → `create()`. Concurrent request inserts between check and insert.

**Fix**:
1. Add `upsert()` method to `IItemCartRepository` interface
2. Implement in `ItemCartRepository` using PostgreSQL `INSERT ... ON CONFLICT`:
   ```sql
   INSERT INTO "ItemCart" (id, "cartId", "productId", "variantId", quantity, "createdAt", "updatedAt")
   VALUES (?, ?, ?, ?, ?, NOW(), NOW())
   ON CONFLICT ("cartId", "productId", COALESCE("variantId", ''))
   DO UPDATE SET quantity = "ItemCart".quantity + EXCLUDED.quantity, "updatedAt" = NOW()
   ```
3. Replace check-then-create in `CartController.addItem()` (lines 225-230) with single `itemCartRepository.upsert()` call

**Files**:
- `src/java/module/bussiness/cart/repository/interfaces/IItemCartRepository.java` — add upsert signature
- `src/java/module/bussiness/cart/repository/impl/ItemCartRepository.java` — add upsert implementation
- `src/java/module/bussiness/cart/CartController.java:222-231` — replace with upsert call

---

## Bug 7: Mua ngay → thêm vào giỏ hàng (thay vì checkout)

**Root cause**: `list.jsp` "MUA NGAY" button POSTs to `/cart?action=buyNow`. `CartController` switch has no `buyNow` case — falls to default (no-op), redirects to `/cart`.

**Fix**:
1. Add `buyNow` case in `CartController`: call `addItem()`, then redirect to `/payment?source=buyNow`
2. For AJAX: return JSON with `"redirectUrl": "/payment?source=buyNow"`
3. Update `list.jsp` JS to handle buyNow redirect

**Files**:
- `src/java/module/bussiness/cart/CartController.java:93-110` — add buyNow case
- `web/views/product/list.jsp` — handle buyNow redirect in JS

---

## Bug 8: Không liên hệ được

**Root cause**: `ContactController.doPost()` line 51: `// TODO: Save to database`. No Contact table, entity, or repository.

**Fix**:
1. Add Contact table to `sto.sql` (before `ProductReview`):
   ```sql
   CREATE TABLE "Contact" (
       "id" CHAR(36) PRIMARY KEY,
       "fullName" VARCHAR(255) NOT NULL,
       "email" VARCHAR(255) NOT NULL,
       "subject" VARCHAR(255) NOT NULL,
       "message" TEXT NOT NULL,
       "status" VARCHAR(20) NOT NULL DEFAULT 'NEW' CHECK ("status" IN ('NEW', 'READ', 'RESPONDED', 'ARCHIVED')),
       "createdAt" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
   );
   CREATE INDEX "contact_email_index" ON "Contact" ("email");
   ```
2. Create `ContactEntity.java` in `src/java/entity/`
3. Create `ContactRepository.java` with `save(ContactEntity)` method
4. Wire into `ContactController.doPost()` — replace TODO with actual save call

**New files**:
- `src/java/entity/ContactEntity.java`
- `src/java/module/bussiness/contact/repository/impl/ContactRepository.java`

**Modified files**:
- `sto.sql`
- `src/java/module/bussiness/contact/ContactController.java`

---

## Bug 9: Bỏ "Đăng nhập" ở footer

**Root cause**: `footer.jsp` line ~14 has login link. Footer only shown to logged-in users.

**Fix**: Remove the `<li><a href="...login">Đăng nhập</a></li>` line.

**File**: `web/views/includes/footer.jsp`

---

## Bug 10: Danh mục chưa hiện số lượng sản phẩm

**Root cause**: `ProductController` computes `categoryCounts` map but `leftmenu.jsp` never reads it.

**Fix**:
1. `ProductController`: Ensure `categoryCounts` is set as request attribute with correct keys (STORAGE_DEVICE, NETWORK_DEVICE, ACCESSORY)
2. `leftmenu.jsp`: Read `categoryCounts` from request, display count next to each category name

**Files**:
- `src/java/module/bussiness/product/ProductController.java` — verify categoryCounts
- `web/views/includes/leftmenu.jsp` — add count display

---

## Implementation Order

1. **Bug 9** (trivial, 1 line removal)
2. **Bug 6** (cart duplicate — affects all cart testing)
3. **Bug 1** (forgot password redirect)
4. **Bug 2** (cart toast error)
5. **Bug 7** (buy now action)
6. **Bug 5** (review creation)
7. **Bug 3** (order delivery info)
8. **Bug 8** (contact persistence — needs new files)
9. **Bug 4** (category links)
10. **Bug 10** (category counts)

---

## Verification

After all changes:
1. **Bug 1**: Forgot password → enter email → see success → page should show code+newPassword form
2. **Bug 2**: Add to cart (AJAX) → see green toast on success, red toast on error
3. **Bug 3**: Place order with delivery info → no RuntimeException, order saved
4. **Bug 4**: Click subcategory (SSD, HDD, NAS) → filter results shown
5. **Bug 5**: Submit product review → saved to DB, appears on page
6. **Bug 6**: Add same product to cart twice → quantity increments, no duplicate error
7. **Bug 7**: Click "MUA NGAY" from product list → goes to checkout page
8. **Bug 8**: Submit contact form → row inserted in Contact table
9. **Bug 9**: Footer should not have "Đăng nhập" link
10. **Bug 10**: Left menu shows "Thiết bị lưu trữ (15)" etc. with counts
