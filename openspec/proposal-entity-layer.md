# Proposal: Entity Layer từ Database Schema (sto.sql)

## Context
Project cần tạo Java entity class tương ứng với 14 bảng trong `sto.sql`.
Hiện tại thư mục `src/java/entity/` trống.

## Goal
Tạo 14 Java record class, mỗi class tương ứng 1 bảng trong database.
Dùng Java 17 `record` — bất biến, gọn, tự sinh constructor/getter/equals/hashCode/toString.

## Spec

### Bảng cần tạo entity

| # | Table | Entity | Key Fields | Enums |
|---|-------|--------|------------|-------|
| 1 | `User` | `User` | id, name, dateOfBirth, hashPassword, email | UserStatus, UserRole |
| 2 | `OutBox` | `OutBox` | id, code, type, userId | OutBoxStatus |
| 3 | `EmailVerificationCode` | `EmailVerificationCode` | id, userId, codeHash, expiresAt, usedAt | — |
| 4 | `Brand` | `Brand` | id, name, userId, description | BrandStatus |
| 5 | `Product` | `Product` | id, name, description, brandId, userId, category | ProductStatus, ProductCategory |
| 6 | `ProductVariant` | `ProductVariant` | id, productId, price, imageUrl, sku, quantity | VariantStatus |
| 7 | `Order` | `Order` | id, userId, productId, status | OrderStatus |
| 8 | `Payment` | `Payment` | id, orderId, userId, amount, accessKey, partnerCode, redirectUrl, ipnUrl, extraData, requestType, signature | PaymentStatus |
| 9 | `Voucher` | `Voucher` | id, percent, userId, expTime, quantity | — |
| 10 | `OrderCart` | `OrderCart` | id, userId | — |
| 11 | `ItemCart` | `ItemCart` | id, cartId, productId, variantId, quantity | — |
| 12 | `SavedProduct` | `SavedProduct` | id, productId, quantity | — |
| 13 | `Session` | `Session` | id, hashRefreshToken, userId, ip | — |
| 14 | `PasswordResetToken` | `PasswordResetToken` | id, userId, tokenHash, expiresAt, usedAt | — |

### Type mapping

| SQL Type | Java Type |
|----------|-----------|
| `CHAR(36)` (UUID) | `String` |
| `VARCHAR(n)` | `String` |
| `INT` | `int` |
| `DECIMAL(12,2)` | `BigDecimal` |
| `DECIMAL(5,2)` | `BigDecimal` |
| `DATE` | `LocalDate` |
| `DATETIME` | `LocalDateTime` |
| `ENUM(...)` | `Java enum` (nested trong record) |

### Relations (FK)

```
User ← OutBox (userId)
User ← EmailVerificationCode (userId)
User ← Brand (userId)
User ← Product (userId)
User ← Order (userId)
User ← Payment (userId)
User ← Voucher (userId)
User ← OrderCart (userId)
User ← Session (userId)

Brand ← Product (brandId)
Product ← ProductVariant (productId)
Product ← Order (productId)
Product ← ItemCart (productId)
Product ← SavedProduct (productId)

ProductVariant ← ItemCart (variantId, nullable)

OrderCart ← ItemCart (cartId, CASCADE DELETE)
Order ← Payment (orderId)
```

### Design Decision: Record vs Class

**Chọn `record`** vì:
- Entity chỉ mang dữ liệu, không có behavior
- Java 17 sẵn có
- Tự sinh constructor, equals, hashCode, toString
- Bất biến — an toàn hơn khi truyền giữa layer

**Không chọn class thường** vì:
- Boilerplate getter/setter dài
- Project không dùng JPA/Hibernate (Jakarta EE Servlet thuần)
- Không cần lazy loading, proxy, hay mutable state

## Tasks

1. Tạo 14 file `.java` trong `src/java/entity/`
2. Mỗi file: `record` với đầy đủ field theo schema
3. Enum nested cho các cột ENUM
4. Package: `entity`
5. Import: `java.math.BigDecimal`, `java.time.LocalDate`, `java.time.LocalDateTime`

## Verification
- Compile: `javac` toàn bộ 14 file, không lỗi
- Kiểm tra field match column trong `sto.sql`
- Enum value match ENUM value trong SQL
