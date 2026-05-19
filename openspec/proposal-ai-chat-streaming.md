# Proposal: AI Chat Streaming — Google Gemini API (SSE)

## Context

LinhNamStore muốn tích hợp AI chat hỗ trợ khách hàng. Dùng Google Gemini API (API key, no cap) với streaming response qua Server-Sent Events (SSE). Cần cả backend (Servlet) và frontend (JSP + JS).

Pattern hiện tại: Jakarta EE Servlet, không DI, single servlet với `action` param, `new` trực tiếp dependencies.

## Goal

Xây dựng module AI chat với:
- Backend: Servlet proxy streaming từ Google Gemini API qua SSE
- Frontend: Trang chat UI với streaming real-time
- Không expose API key phía client — tất cả gọi qua server proxy
- Streaming token-by-token, không đợi full response

## Architecture

```
Browser (EventSource / fetch)
    │
    ▼
AiChatController (@WebServlet("/ai-chat"))
    │
    ├── action: chat-stream (POST) → AiChatService.streamChat(prompt, history)
    │                                  │
    │                                  ▼
    │                          Google Gemini API (streaming SSE)
    │                                  │
    │                                  ▼
    │                          HttpResponse InputStream → chunk → write SSE
    │
    └── action: chat (POST, non-stream) → AiChatService.chat(prompt, history)
                                           │
                                           ▼
                                   Google Gemini API (REST)
```

### Luồng dữ liệu

1. User nhập prompt → JS gửi `POST /ai-chat?action=chat-stream` với `Content-Type: text/event-stream`
2. Controller mở `HttpServletResponse` stream, set headers SSE
3. AiChatService gọi `https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:streamGenerateContent`
4. Đọc response chunk-by-chunk, parse JSON, extract text
5. Write SSE event: `data: { "token": "...", "done": false }`
6. Khi done: `data: { "token": "", "done": true }` → close stream

## Scope thay đổi

### 1. Module Structure — `module/bussiness/ai/`

```
module/bussiness/ai/
├── AiChatConfig.java              ← constants, API key, model config
├── AiChatController.java          ← @WebServlet(name="AiChat", urlPatterns={"/ai-chat"})
├── AiChatService.java             ← business logic, HTTP call Google API
├── dto/
│   ├── ChatRequestDto.java        ← prompt, chatHistory[], stream (boolean)
│   └── MessageDto.java            ← role (user/model), content
├── response_dto/
│   ├── ChatResponseDto.java       ← success, errorMessage, responseText
│   └── ChatStreamChunk.java       ← token, done (cho SSE)
└── repository/                    ← không cần (không persist chat)
```

### 2. AiChatConfig

```java
package module.bussiness.ai;

public class AiChatConfig {
    static final String API_BASE_URL = "https://generativelanguage.googleapis.com/v1beta";
    static final String MODEL_NAME = "gemini-2.0-flash";
    static final String API_KEY = System.getenv("GOOGLE_AI_API_KEY");
    static final String STREAM_ENDPOINT = API_BASE_URL + "/models/" + MODEL_NAME + ":streamGenerateContent";
    static final String CHAT_ENDPOINT = API_BASE_URL + "/models/" + MODEL_NAME + ":generateContent";

    static final String SYSTEM_PROMPT = "Bạn là trợ lý hỗ trợ khách hàng của LinhNamStore, một cửa hàng bán đồ công nghệ. Trả lời ngắn gọn, thân thiện, bằng tiếng Việt.";

    static final int MAX_TOKENS = 2048;
    static final double TEMPERATURE = 0.7;
}
```

### 3. DTO Classes

**ChatRequestDto:**
- `String prompt` — nội dung user nhập
- `List<MessageDto> chatHistory` — lịch sử hội thoại (tối đa 20 message gần nhất)
- `boolean stream` — true = streaming, false = normal

**MessageDto:**
- `String role` — "user" hoặc "model"
- `String content` — nội dung message

**ChatResponseDto:**
- `boolean success`
- `String errorMessage`
- `String responseText` — full response (cho non-stream)

**ChatStreamChunk:**
- `String token` — token hiện tại
- `boolean done` — true = kết thúc

### 4. AiChatController

**URL:** `@WebServlet(name="AiChat", urlPatterns={"/ai-chat"})`
**Annotation:** `@Public` (không cần auth để chat)

**doGet:** forward tới `web/ai-chat.jsp`

**doPost:** `action` switch:
- `chat-stream` → gọi `handleStreamChat()`
- `chat` → gọi `handleChat()`

**handleStreamChat()** — SSE streaming:
```
response.setContentType("text/event-stream");
response.setCharacterEncoding("UTF-8");
response.setHeader("Cache-Control", "no-cache");
response.setHeader("Connection", "keep-alive");
response.setHeader("X-Accel-Buffering", "no");  // disable nginx buffering

PrintWriter out = response.getWriter();
AiChatService.streamChat(dto, (chunk) -> {
    out.write("data: " + toJson(chunk) + "\n\n");
    out.flush();
    return !chunk.isDone();  // continue nếu chưa done
});
out.close();
```

**handleChat()** — normal REST:
```
ChatResponseDto result = aiChatService.chat(dto);
if (result.isSuccess()) {
    response.setStatus(200);
    writeJson(response, result);
} else {
    response.setStatus(400);
    writeJson(response, result);
}
```

### 5. AiChatService

**chat(ChatRequestDto dto) → ChatResponseDto:**
- Build JSON body cho Google API (contents + systemInstruction + generationConfig)
- `HttpURLConnection` POST tới `CHAT_ENDPOINT?key={API_KEY}`
- Read full response body, parse JSON
- Extract `candidates[0].content.parts[0].text`
- Return ChatResponseDto với full text

**streamChat(ChatRequestDto dto, ChunkCallback callback) → void:**
- Build JSON body (tương tự chat)
- `HttpURLConnection` POST tới `STREAM_ENDPOINT?key={API_KEY}`
- Set header: `Accept: text/event-stream`
- Đọc InputStream byte-by-byte (buffered, 4KB buffer)
- Parse SSE format: `data: {...}`
- Hoặc parse chunked JSON format (Google trả về array of JSON objects)
- Với mỗi chunk: extract text → gọi `callback.onChunk(chunk)`
- Nếu callback return `false` → stop streaming (user cancel)

**JSON body format cho Google Gemini API:**
```json
{
  "systemInstruction": {
    "parts": [{ "text": "Bạn là trợ lý..." }]
  },
  "contents": [
    { "role": "user", "parts": [{ "text": "prompt" }] },
    { "role": "model", "parts": [{ "text": "reply" }] }
  ],
  "generationConfig": {
    "maxOutputTokens": 2048,
    "temperature": 0.7
  }
}
```

**Streaming response format từ Google:**
```
data: {"candidates": [{"content": {"parts": [{"text": "Xin"}]}}], "usageMetadata": {...}}
data: {"candidates": [{"content": {"parts": [{"text": " chào"}]}}], "usageMetadata": {...}}
data: {"candidates": [{"content": {"parts": [{"text": "!"}]}, "finishReason": "STOP"}]}
```

### 6. Frontend — `web/ai-chat.jsp`

**Layout:** Chat UI, Apple-style minimal

```html
<div class="chat-container">
    <div class="chat-header">
        <h2>💬 Trợ lý AI</h2>
    </div>
    <div class="chat-messages" id="chatMessages">
        <!-- Messages append here -->
    </div>
    <div class="chat-input-area">
        <textarea id="chatInput" placeholder="Nhập tin nhắn..." rows="1"></textarea>
        <button id="sendBtn" class="btn btn-primary">Gửi</button>
        <button id="stopBtn" class="btn btn-secondary" style="display:none">Dừng</button>
    </div>
</div>
```

**CSS:**
- Chat bubble: user = blue (right), model = gray (left)
- Input area: fixed bottom, auto-grow textarea
- Streaming indicator: typing animation (3 dots)
- Apple-style: rounded bubbles, clean typography
- Responsive: full-width mobile, max-width 700px desktop

### 7. Frontend JS — Streaming Client

**Không dùng EventSource** vì cần POST body. Dùng `fetch` + `ReadableStream`:

```js
async function streamChat(prompt, history) {
    const response = await fetch('/ai-chat?action=chat-stream', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ prompt, chatHistory: history, stream: true })
    });

    const reader = response.body.getReader();
    const decoder = new TextDecoder();
    let buffer = '';

    while (true) {
        const { done, value } = await reader.read();
        if (done) break;

        buffer += decoder.decode(value, { stream: true });
        const lines = buffer.split('\n');
        buffer = lines.pop(); // incomplete last line

        for (const line of lines) {
            if (line.startsWith('data: ')) {
                const json = JSON.parse(line.slice(6));
                if (json.done) return;
                appendToken(json.token);
            }
        }
    }
}
```

**AbortController cho stop button:**
```js
let abortController = null;

function stopStreaming() {
    if (abortController) {
        abortController.abort();
        abortController = null;
    }
}
```

**Chat history management:**
- Lưu trong JS array: `[{ role: 'user', content: '...' }, { role: 'model', content: '...' }]`
- Gửi tối đa 20 message gần nhất
- Clear history button

### 8. Environment Variable

Thêm `GOOGLE_AI_API_KEY` vào:
- `docker-compose.yml` environment
- `.env` (gitignore)
- GlassFish domain.xml (production)

### 9. Error Handling

| Error | Handling |
|-------|----------|
| API key rỗng | Return SSE error: `data: {"error":"API key chưa cấu hình"}` |
| Google API timeout (30s) | Close SSE stream, show error UI |
| Google API rate limit (429) | Return error message "Đang quá tải, thử lại sau" |
| Network error | Catch IOException, return 500 |
| Invalid JSON response | Parse fallback, nếu fail → error |
| User disconnect | Check `out.checkError()` → stop streaming |

## Request Flow

```
User typing → Enter key
    │
    ▼
JS: collect prompt + history (max 20)
    │
    ▼
fetch POST /ai-chat?action=chat-stream
    │
    ▼
AiChatController.doPost → handleStreamChat()
    │
    ├── set SSE headers
    ├── AiChatService.streamChat(dto, callback)
    │       │
    │       ├── HttpURLConnection → Google API
    │       ├── read chunk → parse JSON → extract text
    │       └── callback.onChunk(chunk) → out.write SSE → flush
    │
    └── close stream
    │
    ▼
JS: ReadableStream reader → parse SSE → append token → DOM update
```

## Implementation Order

1. **AiChatConfig** — constants, API key
2. **DTO classes** — ChatRequestDto, MessageDto, ChatResponseDto, ChatStreamChunk
3. **AiChatService** — chat() + streamChat() methods
4. **AiChatController** — servlet, SSE headers, action handlers
5. **ai-chat.jsp** — chat UI HTML/CSS
6. **ai-chat.js** — streaming client, chat history, abort control
7. **docker-compose.yml** — add GOOGLE_AI_API_KEY env
8. **Testing** — streaming, error cases, mobile responsive

## Ràng buộc

- Không expose API key phía client — tất cả qua server proxy
- Streaming phải dùng SSE (text/event-stream), không dùng WebSocket
- Không persist chat history (stateless, client-side only)
- Google API key no cap — không cần rate limit phía server
- Giữ nguyên pattern: single servlet, action param, @Public
- Không thêm external library — chỉ dùng Java standard library (HttpURLConnection)
- JSP chat UI phải responsive, Apple-style minimal
- JS không dùng framework — vanilla fetch + ReadableStream

## Verification

1. Deploy GlassFish, set GOOGLE_AI_API_KEY
2. Truy cập `/ai-chat` → thấy chat UI
3. Nhập prompt → streaming token-by-token, không delay
4. Nút "Dừng" abort stream giữa chừng
5. Chat history được giữ, gửi đúng 20 message gần nhất
6. Test error: sai API key → hiển thị lỗi
7. Mobile responsive: 360px, 480px, 768px
8. Dark mode: chat bubble màu đúng
9. Network tab: thấy SSE stream, mỗi chunk ~50-200 bytes
10. Không có API key leak trong browser dev tools
