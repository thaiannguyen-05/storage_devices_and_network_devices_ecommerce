# Proposal: MVP Modules — Auth, Product, Cart, Order

## Context

Project Jakarta EE Servlet (GlassFish 6.x, JSP + Servlet, no Spring/DI). Entity layer (14 classes) đã hoàn chỉnh. Tất cả module directory trống. SQL schema có sẵn với seed data (10 products, 5 brands, 1 admin user).

MVP scope: **auth + product + cart + order** + infrastructure chung (common, config, sql, outbox, user). Các module contact, notification, payment bỏ qua.

## Goal

Xây dựng đầy đủ layer: `common → config → sql → repository → service → controller` cho 9 module MVP, tuân thủ pattern đã định trong CLAUDE.md.

## Architecture

```
common/                    ← infrastructure dùng chung
  annotation/Public.java
  controller/BaseController.java
  guard/AuthGuard.java
  type/UserPayload.java
  exceptionFilter/GlobalExceptionFilter.java

core/config/               ← DB connection, constants
  DbConfig.java
  AppConfig.java

core/sql/                  ← JDBC helper
  JdbcHelper.java

core/auth/                 ← login, register, JWT, session, verify email
  AuthController.java
  AuthService.java
  AuthTokenService.java
  repository/interfaces/IAuthRepository.java
  repository/impl/AuthRepository.java
  dto/LoginRequestDto.java
  dto/RegisterRequestDto.java
  dto/VerifyEmailRequestDto.java
  response_dto/LoginResponseDto.java
  response_dto/RegisterResponseDto.java
  response_dto/VerifyEmailResponseDto.java

core/user/                 ← CRUD user (admin)
  UserController.java
  UserService.java
  repository/interfaces/IUserRepository.java
  repository/impl/UserRepository.java
  dto/CreateUserDto.java
  dto/UpdateUserDto.java
  response_dto/CreateUserResponseDto.java
  response_dto/GetUserResponseDto.java
  response_dto/UpdateUserResponseDto.java
  response_dto/ListUserResponseDto.java
  response_dto/DeleteUserResponseDto.java

core/outbox/               ← event outbox pattern
  OutBoxService.java
  TypeEvent.java
  repository/interfaces/IOutBoxRepository.java
  repository/impl/OutBoxRepository.java

bussiness/product/         ← product CRUD, browse, search, variant, brand
  ProductController.java
  ProductService.java
  VariantService.java
  repository/interfaces/IProductRepository.java
  repository/interfaces/IVariantRepository.java
  repository/interfaces/IBrandRepository.java
  repository/interfaces/ISavedProductRepository.java
  repository/impl/ProductRepository.java
  repository/impl/VariantRepository.java
  repository/impl/BrandRepository.java
  repository/impl/SavedProductRepository.java
  dto/CreateProductDto.java
  dto/UpdateProductDto.java
  dto/CreateVariantDto.java
  dto/UpdateVariantDto.java
  response_dto/ListProductResponseDto.java
  response_dto/GetProductResponseDto.java
  response_dto/SearchProductResponseDto.java
  response_dto/CreateProductResponseDto.java
  response_dto/UpdateProductResponseDto.java
  response_dto/DeleteProductResponseDto.java
  response_dto/ListBrandResponseDto.java

bussiness/cart/            ← shopping cart operations
  CartController.java
  CartService.java
  repository/interfaces/ICartRepository.java
  repository/impl/CartRepository.java
  dto/AddToCartDto.java
  dto/UpdateCartItemDto.java
  response_dto/GetCartResponseDto.java
  response_dto/AddToCartResponseDto.java
  response_dto/UpdateCartItemResponseDto.java
  response_dto/RemoveFromCartResponseDto.java

bussiness/order/           ← checkout, order history, status management
  OrderController.java
  OrderService.java
  repository/interfaces/IOrderRepository.java
  repository/impl/OrderRepository.java
  dto/CheckoutDto.java
  dto/CheckoutItemDto.java
  response_dto/CheckoutResponseDto.java
  response_dto/GetOrderResponseDto.java
  response_dto/ListOrderResponseDto.java
  response_dto/UpdateOrderStatusResponseDto.java
  response_dto/CancelOrderResponseDto.java
```

**Tổng: ~72 files**

## Quy ước xuyên suốt

| Rule | Detail |
|------|--------|
| Pattern | interface → repository impl → service → controller |
| Controller | `extends HttpServlet`, `@WebServlet(name, urlPatterns)`, `@Public` nếu không cần auth |
| Service | Nhận DTO, trả về ResponseDto (`isSuccess()`, `getErrorMessage()`, `getSuccessMessage()`) |
| Repository | Service import `impl` trực tiếp, KHÔNG dùng interface trong code |
| UUID | `UUID.randomUUID().toString()` — không AUTO_INCREMENT |
| Password | PBKDF2WithHmacSHA256, 120000 iterations, 256-bit salt |
| OutBox | Mọi action quan trọng gửi event qua `OutBoxService.publishEvent()` |
| User creation | Qua `UserService.createUser(CreateUserDto)` |
| Instantiation | `new` trực tiếp trong class, không DI |
| Single servlet | Mọi action dồn vào 1 `@WebServlet`, phân luồng qua `action` param |

## Spec chi tiết từng module

### 1. Common Infrastructure

**`common/annotation/Public.java`**
- `@Retention(RUNTIME)`, `@Target(TYPE)`
- Đánh dấu servlet không cần auth check

**`common/controller/BaseController.java`**
- Abstract class, extends HttpServlet
- Helpers: `getUserFromSession(req)`, `sendJson()`, `forwardToJsp()`, `redirect()`
- Override `service()`: check `@Public` annotation → nếu không có → AuthGuard

**`common/guard/AuthGuard.java`**
- Static method: `check(HttpServletRequest req, HttpServletResponse res)` → boolean
- Nếu không có `UserPayload` trong session → redirect `/auth?action=login`

**`common/type/UserPayload.java`**
- Plain class: userId, email, role, name
- Lưu trong HttpSession sau login

**`common/exceptionFilter/GlobalExceptionFilter.java`**
- Jakarta Filter `@WebFilter("/*")`
- Catch exception → log → forward `/error.jsp`

### 2. Core Config

**`module/core/config/DbConfig.java`**
- Static constants: JDBC URL, username, password, driver class
- Static `Connection getConnection()` — dùng `DriverManager` hoặc JNDI
- Pool: đơn giản, không Hikari (GlassFish đã có built-in pool)

**`module/core/config/AppConfig.java`**
- Constants: `JWT_SECRET`, `JWT_EXPIRY_MS`, `PBKDF2_ITERATIONS = 120000`, `SALT_LENGTH = 32`
- `PAGE_SIZE = 12`, `MAX_CART_ITEMS = 50`, `CURRENCY = "VND"`
- JSP path resolver: `resolveJsp(String action)` → `/views/{module}/{action}.jsp`

### 3. Core SQL

**`module/core/sql/JdbcHelper.java`**
- Static utils: `executeQuery(sql, params...)`, `executeUpdate(sql, params...)`
- Try-with-resources, auto-close Connection/PreparedStatement/ResultSet
- Helper: `setParams(PreparedStatement, Object...)` — map Java type → SQL type

### 4. Core Auth

**Controller:** `@WebServlet(name="Auth", urlPatterns={"/auth"})`
- `@Public`: login, register, verify-email, refresh-token
- `doGet`: forward JSP theo `action` (login.jsp, register.jsp)
- `doPost`: switch `action` → handler

**Actions:**
| Action | Method | Auth | Flow |
|--------|--------|------|------|
| login | POST | @Public | Validate → AuthService.login() → set session → redirect `/` |
| register | POST | @Public | Validate → AuthService.register() → redirect `/auth?action=login&registered=1` |
| logout | POST | Required | AuthService.logout() → invalidate session → redirect `/auth?action=login` |
| verify-email | GET | @Public | AuthService.verifyEmail(code, userId) → redirect `/auth?action=login&verified=1` |

**AuthService methods:**
- `login(LoginRequestDto, ip)` → check email + password (PBKDF2) → create JWT + session → LoginResponseDto
- `register(RegisterRequestDto)` → validate (email unique, password strength, match) → hash password → create User(status=PENDING) → generate email code → outbox event → RegisterResponseDto
- `verifyEmail(code, userId)` → check code hash + expiry → user status=ACTIVE → VerifyEmailResponseDto
- `logout(sessionId)` → delete session

**AuthTokenService:**
- `createAccessToken(UserEntity)` → JWT string
- `validateToken(String)` → UserPayload hoặc null
- `hashRefreshToken(String)` → SHA-256 hash

**AuthRepository:**
- `findByEmail()`, `findById()`, `saveSession()`, `deleteSession()`, `saveEmailCode()`, `findCodeByUserId()`

### 5. Core User

**Controller:** `@WebServlet(name="AdminUsers", urlPatterns={"/admin/users"})` — KHÔNG @Public

**Actions:** `list`, `create`, `edit`, `delete`, `change-status`

**UserService methods:**
- `createUser(CreateUserDto)` → validate → hash password → insert → outbox event
- `getUserById(id)` → findById
- `updateUser(UpdateUserDto)` → validate → update
- `listUsers(page, size)` → findAll + count → pagination
- `deleteUser(id)` → delete
- `changeStatus(id, status)` → updateStatus

### 6. Core Outbox

**TypeEvent.java:**
- `USER_REGISTERED`, `ORDER_CREATED`, `PAYMENT_COMPLETED`, `USER_BANNED`

**OutBoxService:**
- `publishEvent(code, type, userId)` → insert OutBoxEntity
- `processPending()` → lấy pending → xử lý theo type → mark processed/failed

### 7. Business Product

**Controller:** `@WebServlet(name="Product", urlPatterns={"/products", "/admin/products"})`
- `@Public` cho: `list`, `detail`, `search`, `category`
- KHÔNG `@Public` cho admin CRUD

**Actions public:**
| Action | Flow |
|--------|------|
| list | Lấy active products (paginate) → forward product-list.jsp |
| detail | Get product + all variants → forward product-detail.jsp |
| search | Keyword search → forward product-list.jsp |
| category | Filter by category → forward product-list.jsp |
| home | Lấy hàng mới, bán chạy, giảm giá → forward home.jsp |

**Actions admin:** `create`, `edit`, `delete`, `toggle-status`, `add-variant`, `edit-variant`, `delete-variant`

**ProductService:**
- `getHomePage()` → map: hàng mới (recent ACTIVE), bán chạy (join Order count), giảm giá (variants price thấp)
- `getProductDetail(id)` → product + variants
- `searchProducts(keyword, page)` → LIKE name/description
- `createProduct(dto)` → insert product
- `updateProduct(dto)` → update
- `deleteProduct(id)` → delete
- `getAllBrands()` → list brands

**VariantService:**
- `createVariant(dto)` → insert, check SKU unique
- `updateVariant(dto)` → update
- `deleteVariant(id)` → delete

**Repositories:**
- ProductRepository: CRUD + findByCategory + findActive + findBestSelling + search
- VariantRepository: CRUD + findByProductId + findBySku
- BrandRepository: CRUD + findAll
- SavedProductRepository: CRUD + findAll

### 8. Business Cart

**Controller:** `@WebServlet(name="Cart", urlPatterns={"/cart"})` — KHÔNG @Public

**Actions:**
| Action | Method | Flow |
|--------|--------|------|
| view | GET | Get cart + items → forward cart.jsp |
| add | POST | Validate → addToCart → redirect `/cart` |
| update | POST | Update quantity → redirect `/cart` |
| remove | POST | Remove item → redirect `/cart` |
| clear | POST | Clear cart → redirect `/cart` |

**CartService:**
- `getCart(userId)` → getOrCreate cart → get items → join product/variant info → tính total
- `addToCart(userId, productId, variantId, quantity)` → check stock → insert/update item
- `updateQuantity(userId, itemId, quantity)` → validate > 0, <= stock
- `removeItem(userId, itemId)` → delete
- `clearCart(userId)` → delete all items

**CartRepository:**
- `getOrCreateCart(userId)` — UNIQUE constraint, nếu chưa có thì INSERT
- `addItem()`, `updateQuantity()`, `removeItem()`, `getItemsByCartId()`, `clearCart()`

### 9. Business Order

**Controller:** `@WebServlet(name="Order", urlPatterns={"/orders", "/admin/orders"})` — KHÔNG @Public

**Actions user:** `history`, `detail`, `checkout`, `cancel`
**Actions admin:** `list`, `update-status`

**OrderService:**
- `checkout(userId, items, phone, address)`
  - Validate: items không trống, phone format, address không trống
  - Với mỗi item: check stock đủ → trừ stock
  - Tạo OrderEntity (status=PENDING)
  - Clear cart items đã checkout
  - OutBox event ORDER_CREATED
- `getOrderDetail(orderId, userId)` → check ownership → return
- `getOrderHistory(userId, page)` → paginate
- `listAllOrders(page)` → admin only
- `updateStatus(orderId, status)` → admin only, outbox event
- `cancelOrder(orderId, userId)` → chỉ cancel PENDING, hoàn stock

**⚠️ Lưu ý: OrderEntity có fields (variantId, quantity, phone, address) KHÔNG có trong SQL table `Order`.**
- Giải pháp: Với MVP, Order table chỉ lưu: userId, productId, status, createdAt. Phone/address lưu trong session hoặc table riêng. VariantId/quantity lưu qua OrderCart items.
- **Cần sửa SQL** thêm columns cho Order table hoặc tạo OrderItem table.

## Discrepancies cần resolve trước khi implement

| Issue | Entity | SQL | Action |
|-------|--------|-----|--------|
| Missing columns | OrderEntity: variantId, quantity, phone, address, updatedAt | Order table không có | Thêm columns vào SQL hoặc sửa entity |
| Missing entity | PaymentEntity | Payment table có | Tạo PaymentEntity.java |
| Missing table | PasswordResetToken | DROP có, CREATE không có | Tạo bảng hoặc xóa entity reference |
| No userId | SavedProductEntity | SavedProduct table không có userId | Giữ nguyên (global wishlist) hoặc thêm userId |

## Tasks thứ tự thực hiện

1. **Common** — 5 files infrastructure
2. **Core config** — DbConfig, AppConfig
3. **Core sql** — JdbcHelper
4. **Core auth** — Repository, Service, TokenService, Controller, DTOs
5. **Core user** — Repository, Service, Controller, DTOs
6. **Core outbox** — Repository, Service, TypeEvent
7. **Business product** — Repositories, Services, Controller, DTOs
8. **Business cart** — Repository, Service, Controller, DTOs
9. **Business order** — Repository, Service, Controller, DTOs

## Verification

1. **Compile:** `ant compile` — không lỗi
2. **Deploy:** deploy lên GlassFish 6.x
3. **Test flow:**
   - Register → verify email → login → browse products → add to cart → checkout → view order history
   - Admin: login → manage products → view orders → update order status
4. **Database:** kiểm tra records insert/update đúng, outbox events được tạo
