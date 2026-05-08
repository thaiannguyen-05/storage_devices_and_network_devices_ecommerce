# Logger Service - Request/Body/Response Audit

## Context

Hiện tại project có `LoggerService` (JUL wrapper) chỉ log message đơn giản + request info cơ bản (method, path, requestId). Không có cơ chế nào capture request body, response body, execution time, hay query params để audit/debug.

Mục tiêu: tạo Servlet Filter bắt toàn bộ request/response, log chi tiết body, header, params, execution time, status code — ghi file JSON structured log.

## Scope

- Tạo mới `RequestAuditFilter` — Servlet Filter `/*`
- Tạo `ContentCachingRequestWrapper` — đọc lại request body nhiều lần
- Mở rộng `ContentCachingResponseWrapper` (đã có trong `TransformFilter`) — đọc lại response body
- Mở rộng `LoggerService` — thêm method `audit()` cho structured audit log
- Tất cả file mới đặt trong `openspec/` directory dưới dạng design spec, không code

## Existing Components to Reuse

| Component | Path | Reuse How |
|---|---|---|
| `LoggerService` | `common/logger/LoggerService.java` | Base logging, JSON format, file handler |
| `TransformFilter.ContentCachingResponseWrapper` | `common/interceptor/TransformFilter.java` | Tham khảo pattern wrapper response |
| `RequestIdFilter` | `common/middleware/RequestIdFilter.java` | Lấy requestId từ request attribute |
| `ExceptionFilter` | `common/exceptionFilter/ExceptionFilter.java` | Filter chain pattern, error handling |
| `ApiError` | `common/type/ApiError.java` | Error response structure |
| `Jackson ObjectMapper` | `lib/jackson-*.jar` | Serialize body JSON |

## File Structure

Mọi file code mới sẽ nằm trong package `common.logger.audit`:

```
src/java/common/logger/audit/
├── RequestAuditFilter.java           # Filter chính, bắt mọi request/response
├── ContentCachingRequestWrapper.java  # Wrapper cho request body đọc lại được
├── AuditLogEntry.java                # DTO chứa toàn bộ audit data
└── BodyExtractor.java                # Utility trích xuất body an toàn
```

## Design Details

### 1. `ContentCachingRequestWrapper`

**Vấn đề:** `HttpServletRequest.getInputStream()` chỉ đọc được 1 lần. Filter đọc xong thì controller không đọc được nữa.

**Giải pháp:** Wrapper đọc body vào `byte[]` buffer lúc khởi tạo, override `getInputStream()` trả về `ServletInputStream` từ buffer, override `getParameter()` đọc từ buffer nếu cần.

```
package common.logger.audit;

extends HttpServletRequestWrapper
- byte[] cachedBody
- constructor: read all bytes from original inputStream
- getInputStream(): returns new CachedServletInputStream(cachedBody)
- getReader(): returns new BufferedReader(new InputStreamReader(cachedBody))
- getCachedBody(): byte[]
```

**Edge cases:**
- Multipart/form-data (file upload): skip caching body, log placeholder `[binary content]`
- Body > 1MB: truncate, log `[truncated - size: X bytes]`
- Empty body: return empty string

### 2. `BodyExtractor`

Static utility class. Trích xuất body an toàn:

```
class BodyExtractor
+ extractContentType(HttpServletRequest): String
+ extractBody(HttpServletRequest): String
  - Đọc cachedBody, decode theo charset
  - Content-Type: application/json → format đẹp
  - Content-Type: application/x-www-form-urlencoded → decode params
  - Content-Type: multipart/* → `[multipart content skipped]`
  - Khác → raw string, max 10KB
+ extractResponse(HttpServletResponse, contentType): String
  - Đọc response body, giới hạn 50KB
  - Binary content types (image, pdf, zip) → `[binary response]`
```

### 3. `AuditLogEntry`

DTO chứa toàn bộ thông tin 1 request audit:

```
class AuditLogEntry
- String requestId
- String timestamp        // ISO-8601
- String method           // GET, POST, PUT, DELETE
- String uri              // /auth?login
- String queryString
- String contentType
- Map<String, String[]> parameters  // form/query params
- String requestBody      // đã extract, truncated nếu cần
- int responseStatus
- String responseBody     // đã extract, truncated nếu cần
- long durationMs         // execution time
- String clientIp
- String userAgent
- String authUserEmail    // từ request attribute
- String authUserRole
- boolean isError
- String errorMessage     // nếu có exception
```

### 4. `RequestAuditFilter`

Filter chính, mapping `/*`:

```
@WebFilter(urlPatterns = "/*", filterName = "RequestAuditFilter")
class RequestAuditFilter implements Filter

doFilter():
  1. Wrap request trong ContentCachingRequestWrapper
  2. Wrap response trong ContentCachingResponseWrapper (reuse pattern từ TransformFilter)
  3. Capture startTime = System.currentTimeMillis()
  4. chain.doFilter(wrappedRequest, wrappedResponse)
  5. Capture endTime, compute durationMs
  6. Build AuditLogEntry từ wrapped request/response
  7. Gọi LoggerService.logEntry(entry) → ghi JSON line
```

**Skip paths (không audit):**
- `/assets/*` — static files
- `*.css`, `*.js`, `*.png`, `*.jpg`, `*.ico`, `*.woff`, `*.woff2`, `*.ttf`, `*.svg`
- `/views/*` — JSP internal

**Body capture rules:**
- GET/HEAD: không log request body (thường empty)
- POST/PUT/PATCH: log request body
- Response: log nếu content-type là `application/json` hoặc `text/*`
- Binary responses: skip body, chỉ log status + size

**Performance:**
- Không block chain — audit log async (dùng `CompletableFuture.runAsync` với default executor)
- Body size limit: request 10KB, response 50KB
- Timeout: nếu chain > 30s, log warning

**Error handling:**
- Nếu chain throw exception: catch, set `isError=true`, `errorMessage=...`
- Vẫn log audit entry trước khi re-throw cho ExceptionFilter xử lý

### 5. Mở rộng `LoggerService`

Thêm method vào `LoggerService.java`:

```java
public void audit(AuditLogEntry entry) {
    Map<String, Object> payload = buildAuditMap(entry);
    logger.log(Level.INFO, toJson(payload));
}
```

Không thay đổi cấu trúc existing handler. Audit log cùng file `logs/application.log`.

### 6. Filter Execution Order

Thứ tự filter quan trọng. Hiện tại tất cả filter dùng `@WebFilter("/*")` — container quyết định order theo alphabet name.

Order hiện tại (theo filterName alphabet):
1. `AuthPayloadFilter`
2. `CharacterEncodingFilter`
3. `ExceptionFilter`
4. `pipeService`
5. `RequestIdFilter`
6. `TransformFilter`

**Đề xuất:** Đổi tên filterName thành có số thứ tự để control order:

```
01_RequestIdFilter      → sinh requestId trước
02_AuthPayloadFilter    → auth sau khi có requestId
03_RequestAuditFilter   → audit sau auth, trước business logic
04_pipeService          → trim params
05_CharacterEncodingFilter
06_TransformFilter      → wrap response
07_ExceptionFilter      → catch exception cuối cùng
```

Cách khác: dùng `web.xml` `<filter-mapping>` với thứ tự cụ thể. Nhưng project không có `web.xml`, dùng annotation nên rename filterName là giải pháp tối thiểu.

### 7. Log Format (JSON)

Mỗi audit entry = 1 dòng JSON trong `logs/application.log`:

```json
{
  "timestamp": "2026-05-08T10:30:00.123Z",
  "type": "AUDIT",
  "requestId": "abc-123-def",
  "method": "POST",
  "uri": "/auth",
  "queryString": "action=login",
  "contentType": "application/x-www-form-urlencoded",
  "parameters": {"email": ["user@test.com"], "password": ["***"]},
  "requestBody": "{\"email\":\"user@test.com\",\"password\":\"***\"}",
  "responseStatus": 200,
  "responseBody": "{\"success\":true,\"data\":{...}}",
  "durationMs": 45,
  "clientIp": "192.168.1.1",
  "userAgent": "Mozilla/5.0...",
  "authUserEmail": "user@test.com",
  "authUserRole": "customer",
  "isError": false
}
```

**Sensitive data masking:**
- Field chứa `password`, `token`, `secret`, `key`, `creditCard` → replace value với `***`
- Áp dụng cho cả request body, query params, form params

## Implementation Steps

1. Tạo `openspec/request-audit-spec.md` — file spec này (hiện tại)
2. Tạo `ContentCachingRequestWrapper.java` — wrapper request body
3. Tạo `BodyExtractor.java` — utility trích body
4. Tạo `AuditLogEntry.java` — DTO audit data
5. Mở rộng `LoggerService.java` — thêm `audit(AuditLogEntry)` method
6. Tạo `RequestAuditFilter.java` — filter chính
7. Rename filterName các filter hiện tại để control execution order
8. Test: deploy, gọi API, kiểm tra `logs/application.log` có audit entry

## Key Files to Modify

| File | Action | Reason |
|---|---|---|
| `common/logger/LoggerService.java` | Edit | Thêm `audit()` method |
| `common/logger/audit/` (new dir) | Create | Chứa audit classes |
| `common/middleware/RequestIdFilter.java` | Edit | Rename filterName → `01_RequestIdFilter` |
| `common/middleware/AuthPayloadFilter.java` | Edit | Rename filterName → `02_AuthPayloadFilter` |
| `common/interceptor/TransformFilter.java` | Edit | Rename filterName → `06_TransformFilter` |
| `common/exceptionFilter/ExceptionFilter.java` | Edit | Rename filterName → `07_ExceptionFilter` |
| `common/pipe/pipeService.java` | Edit | Rename filterName → `04_pipeService` |
| `common/middleware/CharacterEncodingFilter.java` | Edit | Rename filterName → `05_CharacterEncodingFilter` |

## Verification

1. Deploy lên GlassFish
2. Gửi POST request tới `/auth` với body login
3. Gửi GET request tới `/product`
4. Gửi request gây lỗi (ví dụ: invalid JSON)
5. Kiểm tra `logs/application.log`:
   - Mỗi request = 1 JSON line
   - Có requestId, method, uri, body, response, durationMs
   - Password fields masked `***`
   - Static assets không có trong log
6. Verify request vẫn hoạt động bình thường (body đọc được ở controller)
7. Performance test: 100 request liên tiếp, không slowdown đáng kể

## Trade-offs

- **Async logging vs sync:** Async không block request nhưng có thể mất log nếu JVM crash trước khi flush. Sync an toàn hơn nhưng chậm hơn. Chọn async với small batch.
- **Body size limit:** Cắt body > 10KB để tránh log file phình to. Trade-off: không debug được large payload.
- **Filter order:** Rename filterName hacky nhưng không cần `web.xml`. Nếu sau này thêm filter mới, cần đánh số lại.
