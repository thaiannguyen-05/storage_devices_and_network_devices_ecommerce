# Proposal: Common Layer Infrastructure

## Context

Dự án Jakarta EE 9.1 (Servlet 3.0+) trên GlassFish 6.x, Java 11 target. Entity layer đã tồn tại (14 POJOs trong `entity.*`). Chưa có controller, service, hay auth nào. Thư mục `src/java/common/` đã tạo sẵn với các thư mục con rỗng: `annotation/`, `controller/`, `retry/`, `type/`, `guard/`, `logger/`, `exceptionFilter/`, `interceptor/`, `middleware/`, `pipe/`.

Mục tiêu: Xây dựng common layer nền tảng — annotation điều khiển auth, base controller, cơ chế retry thông minh.

## Scope

### 1. Annotation Layer (`common.annotation`)

| File | Mục đích |
|------|----------|
| `Public.java` | `@Target(TYPE)`, marker. Controller có annotation này bỏ qua auth guard |
| `User.java` | `@Target(PARAMETER)`, `value()` = tên property. VD `@User("id")` extract userId từ UserEntity đang auth trong session |
| `Route.java` | `@Target(METHOD)`, `value()` = path pattern, `method()` = HTTP method. Cho phép dispatch handler method |
| `RequiresRole.java` | `@Target(TYPE)`, `value()` = role string. Giới hạn controller cho role cụ thể |

**Cách hoạt động:**
- `@Public` đặt trên class servlet, base controller check `getClass().isAnnotationPresent(Public.class)` để quyết định có chạy auth guard không
- `@User("property")` đặt trên parameter của handler method. Base controller dùng reflection để gọi `get<Property>()` trên UserEntity lấy từ session
- `@Route(value="/path", method="GET")` đặt trên handler method. Base controller match pathInfo từ URL để dispatch tới method đúng
- `@RequiresRole("admin")` đặt trên class servlet, base controller kiểm tra role của user hiện tại

### 2. Base Controller (`common.controller`)

**`BaseController.java`** — Abstract class, extends `HttpServlet`.

**Trách nhiệm chính:**
1. **Handler discovery** — `init()` quét các method có `@Route` qua reflection, cache vào `Map<String, Method>`
2. **Auth gate** — Override `service()`: check `@Public` → bỏ qua auth. Check `@RequiresRole` → validate role. Else: chạy AuthGuard
3. **Route dispatch** — Extract `pathInfo` từ request URL, match với `@Route` method, invoke qua reflection
4. **`@User` parameter resolution** — `resolveHandlerArgs()`: quét `@User` annotation trên handler params, dùng reflection gọi getter trên UserEntity từ session
5. **Auto-inject** `HttpServletRequest` / `HttpServletResponse` vào handler params nếu không có annotation
6. **Response helpers** — `sendJson()`, `sendError()`, `sendUnauthorized()`, `sendForbidden()`, `forward()`, `redirect()`, `setError()`, `setSuccess()`
7. **JSON serialization** — Dùng `jakarta.json.bind.JsonbBuilder` (có sẵn trong GlassFish 6.x)

**Ví dụ sử dụng:**
```java
@WebServlet("/api/user/*")
@RequiresRole("admin")
public class UserServlet extends BaseController {

    @Route(value = "/me", method = "GET")
    public Result<UserEntity> getMe(@User("id") String userId, HttpServletRequest req) {
        UserEntity user = userService.findById(userId);
        return Result.ok(user);
    }
}

@WebServlet("/api/auth/login")
@Public
public class LoginServlet extends BaseController {

    @Route(value = "/", method = "POST")
    public Result<SessionEntity> login(HttpServletRequest req, HttpServletResponse resp) {
        return Result.ok(session);
    }
}
```

### 3. Retry System (`common.retry`)

| File | Mục đích |
|------|----------|
| `BackoffStrategy.java` | Enum: `FIXED`, `EXPONENTIAL`, `FIBONACCI` |
| `RetryListener.java` | Interface: `onRetry(attempt, error)`, `onSuccess(attempt, result)`, `onFailure(attempt, error)` |
| `RetryResult.java` | POJO: `result`, `attemptCount`, `totalDelayMs`, `succeeded` |
| `RetryConfig.java` | Builder: `maxRetries`, `initialDelayMs`, `strategy`, `retryOn(Exception...)`, `addListener` |
| `RetryExecutor.java` | Core: `execute(Callable<T>)` với configurable backoff, exception filtering, callbacks |
| `RetryExhaustedException.java` | RuntimeException bao last failure sau khi hết retry |

**Cách hoạt động:**
- `RetryExecutor.execute(callable, config)` — gọi callable, nếu throw exception thuộc loại retryable thì retry với delay theo strategy
- Backoff: FIXED (delay cố định), EXPONENTIAL (delay * 2^n), FIBONACCI (delay * fib(n))
- Chỉ retry với exception type đã đăng ký trong `retryOn()`
- Listener callback để log/monitor mỗi lần retry

**Ví dụ sử dụng:**
```java
RetryExecutor.execute(
    () -> databaseService.findUser(id),
    RetryConfig.builder()
        .maxRetries(3)
        .initialDelayMs(500)
        .strategy(BackoffStrategy.EXPONENTIAL)
        .retryOn(SQLException.class)
        .build()
);
```

### 4. Supporting Components

| Path | File | Mục đích |
|------|------|----------|
| `common.type/Result.java` | Generic `Result<T>` — `ok(T)`, `ok()`, `fail(String)` |
| `common.logger/AppLogger.java` | Wrapper `java.util.logging.Logger` — `of(Class)`, `info()`, `warn()`, `error()`, `debug()` |
| `common.guard/AuthGuard.java` | `check(req, resp)` validate session + userId, `checkRole(req, resp, role)` validate role |
| `common.exceptionFilter/GlobalExceptionFilter.java` | `@WebFilter("/*")`, bắt unhandled exception, trả 500 JSON |

## Request Flow

```
HTTP Request
  → GlobalExceptionFilter (bắt mọi unhandled exception)
  → @WebServlet controller extends BaseController
  → BaseController.service()
     1. Check @Public → bỏ qua auth nếu có
     2. AuthGuard check (session + userId)
     3. @RequiresRole check (nếu có)
     4. PathInfo → match @Route method
     5. Resolve handler args (@User, HttpServletRequest, HttpServletResponse)
     6. Invoke handler method
     7. Handle result (JSON response)
```

## Thứ tự triển khai

1. **Annotations** (4 files) — không dependency
2. **Result.java** — không dependency
3. **AppLogger.java** — không dependency
4. **Retry** (6 files) — chỉ dùng `java.util.*`
5. **AuthGuard.java** — phụ thuộc `entity.UserEntity`
6. **GlobalExceptionFilter.java** — phụ thuộc Jakarta Servlet API
7. **BaseController.java** — phụ thuộc tất cả thành phần trên

## Ràng buộc

- Java 11 compatible (không dùng `record`, pattern matching, `var` lambda params)
- Không external library — chỉ Jakarta EE + JDK standard library
- JSON serialization qua `jakarta.json.bind.JsonbBuilder` (có sẵn trong GlassFish 6.x)

## Verification

1. Compile tất cả file — `javac` thành công
2. Deploy lên GlassFish — server start không lỗi annotation
3. Test `@Public` route — access được không cần session
4. Test protected route không session — trả 401
5. Test `@RequiresRole` — trả 403 cho sai role
6. Test `@User` parameter — extract đúng property từ session user
7. Test RetryExecutor — retry khi fail, backoff đúng, exhaust đúng
