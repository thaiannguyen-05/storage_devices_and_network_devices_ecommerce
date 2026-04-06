# Sprint 1 - Hướng dẫn quy trình viết CRUD (User, Product, Order)

## 1) Mục tiêu Sprint 1
Sprint 1 tập trung vào **quy trình chuẩn** khi thêm CRUD:
1. Khai báo method trong `interfaces`
2. Implement method trong `repository`
3. Gọi repository trong `service`
4. Dùng DTO để truyền dữ liệu vào create/update

> Chỉ viết hướng dẫn quy trình, chưa yêu cầu hoàn thiện controller/request validation trong tài liệu này.

---

## 2) Thứ tự thực hiện (bắt buộc)

### Bước 1: Đăng ký method trong `src/java/module/core/sql/interfaces/`
- Tạo hoặc cập nhật interface tương ứng (`IUserRepository`, `IProductRepository`, `IOrderRepository`)
- Khai báo đầy đủ chữ ký method CRUD trước
- Repository class sẽ `implements` interface này

### Bước 2: Viết SQL + implement trong `src/java/module/core/sql/repository/`
- Tại class repository tương ứng (`UserRepository`, `ProductRepository`, `OrderRepository`):
  - `implements` interface
  - Viết SQL cho từng method
  - Dùng `PreparedStatement`
  - Dùng `try-with-resources` cho `Connection`, `PreparedStatement`, `ResultSet`
  - Dùng `ConnecDb.getConnection()`

### Bước 3: Viết/hoàn thiện DTO
- `CreateXxxDto`: field cần khi tạo mới
- `UpdateXxxDto`: field cho phép cập nhật
- Không dùng Entity làm input từ request

### Bước 4: Viết Service gọi Repository
- Service chỉ điều phối logic nghiệp vụ cơ bản
- Input service dùng DTO
- Service gọi repository và trả kết quả entity/list/boolean

---

## 3) Vị trí file theo module

### User
- Interface: `src/java/module/core/sql/interfaces/IUserRepository.java`
- Repository: `src/java/module/core/sql/repository/UserRepository.java`
- Service: `src/java/module/core/user/UserService.java`
- DTO: `src/java/module/core/user/dto/`

### Product
- Interface: `src/java/module/core/sql/interfaces/IProductRepository.java`
- Repository: `src/java/module/core/sql/repository/ProductRepository.java`
- Service: `src/java/module/bussiness/product/ProductService.java`
- DTO: `src/java/module/bussiness/product/dto/`

### Order
- Interface: `src/java/module/core/sql/interfaces/IOrderRepository.java`
- Repository: `src/java/module/core/sql/repository/OrderRepository.java`
- Service: `src/java/module/bussiness/order/OrderService.java`
- DTO: `src/java/module/bussiness/order/dto/`

---

## 4) Hướng dẫn chi tiết theo quy trình

## 4.1 User CRUD

### A. Khai báo trong interface (`IUserRepository`)
```java
public interface IUserRepository {
    UserEntity create(CreateUserDto dto);
    UserEntity findById(String id);
    UserEntity findByEmail(String email);
    UserEntity update(String id, UpdateUserDto dto);
    boolean delete(String id);
}
```

### B. Implement trong repository (`UserRepository`)
- Class phải `implements IUserRepository`
- Viết SQL cho từng method:

```sql
-- Create
INSERT INTO users (id, name, date_of_birth, hash_password, status, email, created_at, updated_at)
VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW());

-- Read by id
SELECT id, name, date_of_birth, hash_password, status, email, created_at, updated_at
FROM users
WHERE id = ?;

-- Read by email
SELECT id, name, date_of_birth, hash_password, status, email, created_at, updated_at
FROM users
WHERE email = ?;

-- Update
UPDATE users
SET name = ?, date_of_birth = ?, status = ?, email = ?, updated_at = NOW()
WHERE id = ?;

-- Delete
DELETE FROM users
WHERE id = ?;
```

### C. Gọi trong service (`UserService`)
```java
public UserEntity createUser(CreateUserDto dto) {
    return userRepository.create(dto);
}

public UserEntity getUserById(String id) {
    return userRepository.findById(id);
}

public UserEntity updateUser(String id, UpdateUserDto dto) {
    return userRepository.update(id, dto);
}

public boolean deleteUser(String id) {
    return userRepository.delete(id);
}
```

---

## 4.2 Product CRUD

### A. Khai báo trong interface (`IProductRepository`)
```java
public interface IProductRepository {
    ProductEntity create(CreateProductDto dto);
    ProductEntity findById(String id);
    List<ProductEntity> findAll();
    ProductEntity update(String id, UpdateProductDto dto);
    boolean delete(String id);
}
```

### B. Implement trong repository (`ProductRepository`)
```sql
-- Create
INSERT INTO products (id, name, description, brand_id, status, user_id, created_at, updated_at, category)
VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW(), ?);

-- Read by id
SELECT id, name, description, brand_id, status, user_id, created_at, updated_at, category
FROM products
WHERE id = ?;

-- List
SELECT id, name, description, brand_id, status, user_id, created_at, updated_at, category
FROM products
ORDER BY created_at DESC;

-- Update
UPDATE products
SET name = ?, description = ?, brand_id = ?, status = ?, category = ?, updated_at = NOW()
WHERE id = ?;

-- Delete
DELETE FROM products
WHERE id = ?;
```

### C. Gọi trong service (`ProductService`)
```java
public ProductEntity createProduct(CreateProductDto dto) {
    return productRepository.create(dto);
}

public ProductEntity getProductById(String id) {
    return productRepository.findById(id);
}

public List<ProductEntity> getAllProducts() {
    return productRepository.findAll();
}

public ProductEntity updateProduct(String id, UpdateProductDto dto) {
    return productRepository.update(id, dto);
}

public boolean deleteProduct(String id) {
    return productRepository.delete(id);
}
```

---

## 4.3 Order CRUD

### A. Khai báo trong interface (`IOrderRepository`)
```java
public interface IOrderRepository {
    OrderEntity create(CreateOrderDto dto);
    OrderEntity findById(String id);
    List<OrderEntity> findByUserId(String userId);
    OrderEntity update(String id, UpdateOrderDto dto);
    boolean delete(String id);
}
```

### B. Implement trong repository (`OrderRepository`)
```sql
-- Create
INSERT INTO orders (id, user_id, product_id, created_at, status)
VALUES (?, ?, ?, NOW(), ?);

-- Read by id
SELECT id, user_id, product_id, created_at, status
FROM orders
WHERE id = ?;

-- List by user
SELECT id, user_id, product_id, created_at, status
FROM orders
WHERE user_id = ?
ORDER BY created_at DESC;

-- Update
UPDATE orders
SET status = ?
WHERE id = ?;

-- Delete
DELETE FROM orders
WHERE id = ?;
```

### C. Gọi trong service (`OrderService`)
```java
public OrderEntity createOrder(CreateOrderDto dto) {
    return orderRepository.create(dto);
}

public OrderEntity getOrderById(String id) {
    return orderRepository.findById(id);
}

public List<OrderEntity> getOrdersByUserId(String userId) {
    return orderRepository.findByUserId(userId);
}

public OrderEntity updateOrder(String id, UpdateOrderDto dto) {
    return orderRepository.update(id, dto);
}

public boolean deleteOrder(String id) {
    return orderRepository.delete(id);
}
```

---

## 5) Quy tắc DTO

### `CreateXxxDto`
- Chỉ chứa field bắt buộc khi tạo mới
- Không chứa `created_at`, `updated_at` nếu DB tự set

### `UpdateXxxDto`
- Chỉ chứa field cho phép sửa
- Không để client sửa các field hệ thống không cho phép

### Luồng dùng DTO
`Controller -> Service(dto) -> Repository(dto -> SQL params) -> Entity result`

---

## 6) Checklist hoàn thành cho mỗi module
- [ ] Interface đã có đủ method CRUD
- [ ] Repository đã `implements` interface
- [ ] SQL create/read/update/delete chạy đúng
- [ ] Service đã gọi đầy đủ method repository
- [ ] Create/Update dùng DTO thay vì truyền Entity trực tiếp
- [ ] Dùng `PreparedStatement` + `try-with-resources`
- [ ] Update có set `updated_at = NOW()` (nếu bảng có cột này)
