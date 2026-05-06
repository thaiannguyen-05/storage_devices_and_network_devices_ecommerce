# Module Generation Pattern (Jakarta EE Servlet)

## Cấu trúc thư mục

```
module/{path}/{tên_module}/
├── {X}Config.java          ← cấu hình (constants, setup)
├── {X}Controller.java      ← extends HttpServlet, @WebServlet
├── {X}Service.java         ← business logic
├── {X}Payload.java         ← auth data carrier (nếu cần)
├── {X}TokenService.java    ← JWT / token handling (nếu cần)
├── dto/                    ← request DTOs (input)
│   ├── {Action}RequestDto.java
│   └── ...
├── response_dto/           ← response DTOs (output)
│   ├── {Action}ResponseDto.java
│   └── ...
└── repository/
    ├── interfaces/         ← interface định nghĩa
    │   └── I{Entity}Repository.java
    └── impl/               ← triển khai
        └── {Entity}Repository.java
```

## Quy ước từng loại file

### Controller
- `extends HttpServlet`, annotate `@WebServlet(name = "ten", urlPatterns = {"/ten"})`
- `@Public` nếu không cần auth
- `doGet` → forward tới JSP theo `action` param
- `doPost` → `request.setCharacterEncoding("UTF-8")` → switch theo `action` → gọi handler
- Mỗi handler: tạo DTO từ `request.getParameter("...")` → gọi service → check `result.isSuccess()`:
  - Fail: `setAttribute("error", ...)` → forward lại JSP
  - Success: set session/cookie → `sendRedirect`
- Instantiates service trực tiếp: `private final XService xService = new XService();`

### Service
- Plain Java class, KHÔNG extends/implement gì đặc biệt
- Instantiates dependencies trực tiếp: `new SessionRepository()`, `new OutBoxRepository()`, v.v.
- Mỗi method nhận DTO, trả về ResponseDto
- Pattern response: có `isSuccess()`, `getErrorMessage()`, `getSuccessMessage()`
- Password hash: PBKDF2WithHmacSHA256, 120000 iterations, 256-bit salt
- Gửi event qua `OutBoxRepository` + `TypeEvent`
- Tạo user qua `UserService.createUser(CreateUserDto)`

### DTO (request)
- Package: `module.{path}.dto`
- Class: `{Action}RequestDto`
- Private fields, getter/setter
- Không validation annotation — validate trong service

### Response DTO
- Package: `module.{path}.response_dto`
- Class: `{Action}ResponseDto`
- Có `boolean success`, `String errorMessage`, `String successMessage`
- Thêm field tùy nghiệp vụ (userEmail, userRole, accessToken, ...)

### Repository
- Interface: `I{Entity}Repository.java` (trong `repository/interfaces/`)
- Impl: `{Entity}Repository.java` (trong `repository/impl/`)
- Service import `impl` trực tiếp, KHÔNG dùng interface

### Imports
- Package pattern: `module.core.{ten_module}` hoặc `module.bussiness.{ten_module}`
- DTO: `module.{path}.dto.{Name}`
- Response DTO: `module.{path}.response_dto.{Name}`
- Entity: `entity.{Name}`

## Key Principles
- **Không dùng Spring/DI** — Jakarta EE (Servlet), dependency khởi tạo bằng `new` trực tiếp
- **Single servlet pattern** — mọi action dồn vào 1 `@WebServlet`, phân luồng qua `action` param
- **Request/Response DTO tách riêng** — `dto/` cho input, `response_dto/` cho output, không dùng chung
- **Repository không dùng interface trong code** — interface tồn tại nhưng service import thẳng `impl`

# UI/UX Layout Requirements

## Cấu trúc trang chung
Mọi page đều có 4 phần cố định:
- **Banner** — header trên cùng
- **Top Menu** — thanh điều hướng chính
- **Left Menu** — sidebar điều hướng phụ
- **Content** — phần nội dung chính giữa (thay đổi theo từng chức năng)
- **Footer** — tên và ngày sinh các thành viên trong nhóm

## Top Menu & Left Menu
- Có hiệu ứng hover/transition phù hợp (CSS transition, color change, scale, v.v.)
- Link đến đúng chức năng tương ứng

## Trang chủ (Home)
- Hiển thị: hình ảnh sản phẩm, tên/mã sản phẩm, giá
- Chia sản phẩm theo mục: hàng mới, hàng bán chạy, hàng giảm giá, v.v.

## Trang liên hệ (Contact)
- Form nhập liệu: họ tên, email, nội dung liên hệ
- Validate: không để trống, đúng định dạng email
- Nếu hợp lệ → lưu thông tin vào database

## Trang đăng ký (Register)
- Tối thiểu: tên tài khoản, tên đăng nhập, email, số điện thoại, địa chỉ, mật khẩu, nhập lại mật khẩu
- Validate: không trống, email đúng format, mật khẩu khớp, sđt đúng format
- Nếu hợp lệ → lưu vào database

## Trang chi tiết sản phẩm (Product Detail)
- Trigger: click vào sản phẩm từ trang chủ
- Hiển thị đầy đủ thông tin chi tiết sản phẩm (lấy từ database)

## Dữ liệu sản phẩm
- Tối thiểu 30 sản phẩm trên toàn bộ trang web
