# Proposal: Product Review/Comment Module

## Context

Product detail page (`web/pages/product-detail.jsp`) đã có form review (POST `action=review`) nhưng không có backend handler, không có database table, không có repository/service. `ProductReviewEntity` tồn tại nhưng incomplete — dùng `reviewerName` string thay vì `userId` FK. Cần module review hoàn chỉnh: tạo/hiển thị đánh giá sản phẩm với rating 1-5.

**User choice:** Login required để review, auto-approve (không cần admin duyệt).

## Goal

Xây dựng đầy đủ layer cho product review: database table → entity → repository → service → controller actions → JSP display.

## Architecture

```
module/bussiness/product/          ← thêm vào module product có sẵn
  ReviewService.java               ← business logic review
  dto/CreateReviewDto.java         ← input DTO
  response_dto/CreateReviewResponseDto.java
  response_dto/GetReviewsResponseDto.java
  repository/interfaces/IProductReviewRepository.java
  repository/impl/ProductReviewRepository.java

entity/
  ProductReviewEntity.java         ← update (thêm userId, status, updatedAt)
  ReviewView.java                  ← view model (JOIN User để lấy reviewerName)
```

**Tổng: 8 file mới, 3 file chỉnh sửa**

## Quy ước

| Rule | Detail |
|------|--------|
| Module location | Trong `module.bussiness.product` — review gắn liền với product |
| Pattern | interface → repository impl → service → controller actions |
| Service | Nhận DTO, trả về ResponseDto (extends `BaseResponse`) |
| Repository | Import `impl` trực tiếp, KHÔNG dùng interface trong code |
| UUID | `UUID.randomUUID().toString()` |
| Instantiation | `new` trực tiếp, không DI |
| Auth | Login required — lấy userId từ session |
| Status | Default `APPROVED` — auto-approve, không moderation |

## Spec chi tiết

### 1. Database — ProductReview table

**File:** `sto.sql` (EDIT)

```sql
CREATE TABLE ProductReview (
    id CHAR(36) PRIMARY KEY,
    productId CHAR(36) NOT NULL,
    userId CHAR(36) NOT NULL,
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment VARCHAR(1000),
    status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'APPROVED',
    createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    updatedAt DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (productId) REFERENCES Product(id) ON DELETE CASCADE,
    FOREIGN KEY (userId) REFERENCES User(id) ON DELETE CASCADE,
    INDEX idx_product_status (productId, status, createdAt DESC)
);
```

Thêm seed data: 10-15 review across various products.

### 2. Entity — Update + New View Model

**File:** `src/java/entity/ProductReviewEntity.java` (EDIT)

Fields: `id` (String UUID), `productId`, `userId`, `rating` (int), `comment` (String), `status` (enum String), `createdAt` (LocalDateTime), `updatedAt` (LocalDateTime). No-arg + all-args constructors. Getters/setters.

**File:** `src/java/entity/ReviewView.java` (NEW)

View model cho display — như `ProductCardView`, `CartItemView`.
Fields: `id`, `rating`, `comment`, `createdAt`, `reviewerName` (from User JOIN).

### 3. DTOs

**File:** `src/java/module/bussiness/product/dto/CreateReviewDto.java` (NEW)

Fields: `productId` (String), `userId` (String), `rating` (int), `comment` (String).

**File:** `src/java/module/bussiness/product/response_dto/CreateReviewResponseDto.java` (NEW)

Extends `BaseResponse`.

**File:** `src/java/module/bussiness/product/response_dto/GetReviewsResponseDto.java` (NEW)

Extends `BaseResponse`. Thêm: `List<ReviewView> reviews`, `double averageRating`, `int totalReviews`.

### 4. Repository

**File:** `src/java/module/bussiness/product/repository/interfaces/IProductReviewRepository.java` (NEW)

**File:** `src/java/module/bussiness/product/repository/impl/ProductReviewRepository.java` (NEW)

Methods:
- `insert(ProductReviewEntity)` — INSERT review
- `findByProductIdApproved(String productId)` — SELECT approved reviews ORDER BY createdAt DESC, JOIN User để lấy name/email → map sang `ReviewView`
- `findByUserIdAndProductId(String userId, String productId)` — check duplicate review
- `countByProductId(String productId)` — COUNT approved reviews
- `calculateAverageRating(String productId)` — SELECT AVG(rating) WHERE approved
- `existsByUserIdAndProductId(String userId, String productId)` — boolean

Dùng `JdbcHelper.executeQuery()`, `JdbcHelper.executeUpdate()`. Private `map(ResultSet rs)` method.

### 5. Service

**File:** `src/java/module/bussiness/product/ReviewService.java` (NEW)

Dependencies: `private final ProductReviewRepository reviewRepository = new ProductReviewRepository();`

**Methods:**

`createReview(CreateReviewDto)` → `CreateReviewResponseDto`
- Validate: productId hợp lệ (ProductEntity tồn tại)
- Validate: user chưa review sản phẩm này (existsByUserIdAndProductId)
- Validate: rating 1-5, comment không blank, maxLength 1000
- Tạo `ProductReviewEntity`, set status=APPROVED, UUID
- `reviewRepository.insert(entity)`
- Return success

`getReviewsByProductId(String productId)` → `GetReviewsResponseDto`
- `reviewRepository.findByProductIdApproved(productId)` → List<ReviewView>
- `reviewRepository.calculateAverageRating(productId)` → double
- `reviewRepository.countByProductId(productId)` → int
- Populate response DTO, return

### 6. Controller — Add Actions

**File:** `src/java/module/bussiness/product/ProductController.java` (EDIT)

Thêm field: `private final ReviewService reviewService = new ReviewService();`

**POST action = "review":**
- Check login: `getCurrentUserId(req)` → null → redirect `/auth?action=login`
- Build `CreateReviewDto` từ request params: productId, userId (from session), rating, comment
- `reviewService.createReview(dto)`
- Success: `sendRedirect` về product detail URL
- Fail: `setAttribute("error", result.getErrorMessage())` → forward detail JSP

**GET action = "reviews":**
- Lấy productId từ param
- `reviewService.getReviewsByProductId(productId)`
- `setAttribute("reviews", response.getReviews())`
- `setAttribute("averageRating", response.getAverageRating())`
- `setAttribute("totalReviews", response.getTotalReviews())`
- Forward detail JSP

### 7. JSP — Display Reviews

**File:** `web/pages/product-detail.jsp` (EDIT)

Thêm section dưới review form:
- Nếu user chưa login: hiển thị "Vui lòng đăng nhập để đánh giá"
- Nếu user đã review: hiển thị "Bạn đã đánh giá sản phẩm này"
- Hiển thị average rating (★★★★☆ style) + total review count
- `<c:forEach>` qua `reviews` list: mỗi review hiển thị reviewerName, date (format), rating stars, comment
- Hiển thị error/success messages từ flash attributes
- Pagination nếu nhiều review (tùy chọn)

### 8. Seed Data

**File:** `sto.sql` (EDIT)

Thêm 10-15 INSERT INTO ProductReview records với userId từ seed data, productId từ seed products, rating random 3-5, comment mẫu tiếng Việt.

## File Summary

| Action | File |
|---|---|
| EDIT | `sto.sql` |
| EDIT | `src/java/entity/ProductReviewEntity.java` |
| CREATE | `src/java/entity/ReviewView.java` |
| CREATE | `src/java/module/bussiness/product/dto/CreateReviewDto.java` |
| CREATE | `src/java/module/bussiness/product/response_dto/CreateReviewResponseDto.java` |
| CREATE | `src/java/module/bussiness/product/response_dto/GetReviewsResponseDto.java` |
| CREATE | `src/java/module/bussiness/product/repository/interfaces/IProductReviewRepository.java` |
| CREATE | `src/java/module/bussiness/product/repository/impl/ProductReviewRepository.java` |
| CREATE | `src/java/module/bussiness/product/ReviewService.java` |
| EDIT | `src/java/module/bussiness/product/ProductController.java` |
| EDIT | `web/pages/product-detail.jsp` |

## Verification

1. `ant build` — compile không lỗi
2. Deploy app, vào product detail page
3. Login, submit review → redirect về, review hiển thị
4. Submit review trùng → lỗi "Bạn đã đánh giá sản phẩm này"
5. Không login → redirect đến trang login
6. Average rating + review count hiển thị đúng
7. Database: ProductReview table có seed data
